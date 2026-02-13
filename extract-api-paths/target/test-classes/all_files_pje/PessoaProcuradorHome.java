package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.component.suggest.EntidadeLocalizacaoSuggestBean;
import br.com.infox.cliente.component.suggest.PessoaProcuradorMunicipioSuggestBean;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.UsuarioLocalizacaoHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.service.PapelService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name(PessoaProcuradorHome.NAME)
@BypassInterceptors
public class PessoaProcuradorHome extends AbstractPessoaProcuradorHome<PessoaProcurador> {
	public static final String NAME = "pessoaProcuradorHome";

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final LogProvider log = Logging.getLogProvider(PessoaProcuradorHome.class);

	private Estado estado;
	private Papel papel;
	private String oldCpf;

	private PapelService papelService;
	private ParametroService parametroService;
	private UsuarioService usuarioService;
	
	private PessoaProcuradorManager pessoaProcuradorManager;
	
	public PessoaProcuradorHome(){	
		papelService = (PapelService) Component.getInstance("papelService");
		parametroService = (ParametroService) Component.getInstance("parametroService");
		usuarioService = (UsuarioService) Component.getInstance("usuarioService");
	}

	@Override
	public void newInstance() {
		oldCpf = null;
		MeioContatoHome.instance().newInstance();
		setPapel(null);
		super.clearInstance(true);
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		if (this.estado != null) {
			if ((estado == null) || (!this.estado.getEstado().equals(estado.getEstado()))) {
				getPessoaProcuradorMunicipioSuggest().setInstance(null);
			}
		}
		this.estado = estado;
	}

	private PessoaProcuradorMunicipioSuggestBean getPessoaProcuradorMunicipioSuggest() {
		PessoaProcuradorMunicipioSuggestBean pessoaProcuradorMunicipioSuggest = (PessoaProcuradorMunicipioSuggestBean) Component
				.getInstance("pessoaProcuradorMunicipioSuggest");
		return pessoaProcuradorMunicipioSuggest;
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setMunicipioNascimento(getPessoaProcuradorMunicipioSuggest().getInstance());
		if (isManaged()) {
			if (Strings.isEmpty(oldCpf) || !oldCpf.equals(getInstance().getNumeroCPF())) {
				instance.setAssinatura(null);
				instance.setCertChain(null);
			}
		}
		return true;
	}

	public static PessoaProcuradorHome instance() {
		return ComponentUtil.getComponent(NAME);
	}

	@Override
	public String update() {
		if(beforePersistOrUpdate()){			
			try {
				pessoaProcuradorManager = (PessoaProcuradorManager)getComponent(PessoaProcuradorManager.NAME);
				pessoaProcuradorManager.persistAndFlush(getInstance());
				setInstance(getInstance());
				updatedMessage();
				
				
				EntityManager em = getEntityManager();
				for (PessoaDocumentoIdentificacao documento : getInstance().getPessoaDocumentoIdentificacaoList()) {
					em.persist(documento);
					EntityUtil.flush(em);
				}
				
				atualizarEspecializacao(instance.getProcuradorAtivo(), instance.getPessoa(), PessoaProcurador.class);
				getInstance().getPessoa().setNome(getInstance().getNome());
				PessoaHome.instance().atualizarNomeLocalizacao(getInstance().getPessoa());
				
				refreshGrid("pessoaDocumentoIdentificacaoCadastroGrid");
				
				if (isCadastroAlterado()) {
					Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
				}
				
				return afterPersistOrUpdate("update");
			} catch (PJeBusinessException e) {
				reportMessage(e);
				FacesMessages.instance().clear();
				FacesMessages.instance().addFromResourceBundle(Severity.INFO, "perfil.erro");
			}
		}
		
		return null;
	}
	
	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		PessoaHome.instance().setId(id);

		if (changed) {
			if (getInstance().getMunicipioNascimento() != null) {
				estado = getInstance().getMunicipioNascimento().getEstado();
			}
			getPessoaProcuradorMunicipioSuggest().setInstance(getInstance().getMunicipioNascimento());
			papel = papelService.findById(parametroService.valueOf(Parametros.ID_PAPEL_PROCURADOR));
			if (recuperaLocalizacaoProcurador(getInstance().getPessoa(), papel) == null) {
				papel = null;
			}
		}
		if (id == null) {
			getPessoaProcuradorMunicipioSuggest().setInstance(null);
			estado = null;
			papel = null;
		}
		if (getInstance().getMunicipioNascimento() != null) {
			if ((!changed) && (id != null) && (estado != getInstance().getMunicipioNascimento().getEstado())) {
				estado = getInstance().getMunicipioNascimento().getEstado();
				getPessoaProcuradorMunicipioSuggest().setInstance(getInstance().getMunicipioNascimento());
			}
		}
		if (getInstance() != null && oldCpf == null) {
			oldCpf = getInstance().getNumeroCPF();
		}
		
		setUsuarioAtivoInicial(instance.getAtivo());
		setPerfilAtivoInicial(instance.getProcuradorAtivo());
	}
	
	public UsuarioLocalizacao recuperaLocalizacaoProcurador(PessoaFisica pessoa, Papel papel){
		List<UsuarioLocalizacao> ret = usuarioService.getLocalizacoesAtivas(pessoa, papel);
		if(ret.size() > 0){
			return ret.get(0);
		}else{
			return null;
		}
	}

	@Override
	public String persist() {
		if(beforePersistOrUpdate()){
			PessoaProcurador pessoaProcurador = getInstance();
			pessoaProcurador.setTipoPessoa(ParametroUtil.instance().getTipoPessoaFisica());
				
			try {
				pessoaProcuradorManager = (PessoaProcuradorManager)getComponent(PessoaProcuradorManager.NAME);
				pessoaProcuradorManager.persistAndFlush(pessoaProcurador);
				setInstance(pessoaProcurador);
				String persist = afterPersistOrUpdate("persisted");
				if(persist.equals("persisted")){
					FacesMessages.instance().clear();
					FacesMessages.instance().add(Severity.INFO, "Procurador cadastrado com sucesso.");
				}
				
				createdMessage();
				return persist;
			} catch (PJeBusinessException e) {
				reportMessage(e);
			}
		}
		
		return null;
	}

	/* Adicionando novas localizações ao usuário */
	public void persistUsuarioLocalizacao() {
		UsuarioLocalizacao uLoc = UsuarioLocalizacaoHome.instance().getInstance();

		EntidadeLocalizacaoSuggestBean suggest = getEntidadeLocalizacaoSuggest();

		uLoc.setLocalizacaoFisica(suggest.getInstance().getLocalizacao());

		uLoc.setPapel(papel);
		uLoc.setUsuario(getInstance().getPessoa());

		getEntityManager().persist(uLoc);
		getEntityManager().flush();

		UsuarioLocalizacaoHome.instance().newInstance();
		suggest.setInstance(null);
		refreshGrid("usuarioLocalizacaoProcuradorGrid");
	}

	private EntidadeLocalizacaoSuggestBean getEntidadeLocalizacaoSuggest() {
		EntidadeLocalizacaoSuggestBean entidadeLocalizacaoSuggest = (EntidadeLocalizacaoSuggestBean) Component
				.getInstance("entidadeLocalizacaoSuggest");
		return entidadeLocalizacaoSuggest;
	}

	public boolean checkCPF() {
		return PessoaAdvogadoHome.instance().checkCPF(getInstance().getNumeroCPF(), getInstance().getIdUsuario());
	}
	
	public boolean isPossuiPermissaoAtivarDesativar() {
		return Authenticator.isPermissaoCadastroTodosPapeis();
	}
	
	public boolean isPermiteAlterarEmail() {
		if(getInstance() != null && getInstance().getPessoa() != null && getInstance().getPessoa().getIdPessoa() != null) {
			return Authenticator.isPermiteAlterarEmail(getInstance().getPessoa());			
		}
		return false;
	}

	public void newInstanceUsuarioLocalizacao() {
		UsuarioLocalizacaoHome localizacaoHome = getComponent("usuarioLocalizacaoHome");
		localizacaoHome.newInstance();
		localizacaoHome.getInstance().setUsuario(getInstance().getPessoa());
	}

	@Observer("br.com.infox.ibpm.entity.UsuarioLocalizacao.afterRemove")
	public void atualizarLocalizacaoGrid(UsuarioLocalizacao usuarioLocalizacao) {
		refreshGrid("usuarioLocalizacaoProcuradorGrid");
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	public Papel getPapel() {
		return papel;
	}

	public Procuradoria procuradoriaUsuarioLogado() {
		try {
			Pessoa p = Authenticator.getPessoaLogada();
			if(p instanceof PessoaFisica){
				return Authenticator.getProcuradoriaAtualUsuarioLogado();
			}
		} catch (ClassCastException c) {
			return null;
		}
		return null;
	}
	
	public List<Procuradoria> getListProcuradorias(){
		Identity identity = Identity.instance();

		if(!Authenticator.isPapelAdministrador() && !identity.hasRole(Papeis.ADMINISTRADOR) && 
				!identity.hasRole(Papeis.PJE_ADMINISTRADOR_PROCURADORIA)){
			
			try {
				Pessoa p = Authenticator.getPessoaLogada();
				if(p instanceof PessoaFisica){
					PessoaFisica pf = (PessoaFisica)p;
					if(pf.getPessoaProcurador() != null){
						List <Procuradoria> list = new ArrayList<Procuradoria>();
						Procuradoria procuradoriaAtual = Authenticator.getProcuradoriaAtualUsuarioLogado();
						if(procuradoriaAtual != null) {
							list.add(procuradoriaAtual);
						}
						return  list;
					}	
				}
			} catch (ClassCastException c) {
				c.printStackTrace();
				return null;
			}
		}else{
			ProcuradoriaManager procManager = (ProcuradoriaManager) Component.getInstance("procuradoriaManager");
			return procManager.getlistProcuradorias();
		}
		return null;
	}	
	
	public String removeCertificado() {
		getInstance().setCertChain(null);
		getInstance().setAssinatura(null);
		return update();
	}

	/**
	 * Método utilizado para a exclusão de todas as produradorias viculadas a
	 * este produrador quando o mesmo mudar de produradoria
	 * 
	 * @param procurador
	 */
	public void removerEntidadesViculadasProdurador(PessoaProcurador procurador) {
		StringBuffer sb = new StringBuffer();
		sb.append("delete from tb_pess_procrdor_prcrdoria ");
		sb.append("where id_pessoa_fisica = :id ");
		String sql = sb.toString();
		sql = sql.replaceAll(":id", String.valueOf(procurador.getIdUsuario()));
		EntityUtil.createNativeQuery(getEntityManager(), sql, "tb_pess_procrdor_prcrdoria").executeUpdate();
		EntityUtil.flush(getEntityManager());
		refreshGrid("pessoaProcuradorProcuradoriaGrid");
	}

	@SuppressWarnings("unchecked")
	public List<Pessoa> getEntidadesRepresentadas(PessoaProcurador pessoaProcurador) {
		String sql = "select o.pessoaProcuradoriaEntidade.pessoa from "
				+ "PessoaProcuradorProcuradoria o where o.pessoaProcurador = :pessoaProcurador";
		Query query = getEntityManager().createQuery(sql);
		query.setParameter("pessoaProcurador", pessoaProcurador);
		return query.getResultList();
	}

	/**
	 * Método que verifica se o Usuario logado pode cadastrar um procurador MP
	 */
	public Boolean podeCadastrarProcuradorMP() {
		try {
			return Authenticator.isRepresentanteGestor();
		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * 
	 * Método que verifica se o Procurador logado acompanha sessão.
	 * 
	 */
	public boolean verificaProcuradorAcompanhaSessao() {
		if (Pessoa.instanceOf(Authenticator.getPessoaLogada(), PessoaProcurador.class)) {
			PessoaProcurador pessoaProcurador = ((PessoaFisica) Authenticator.getPessoaLogada()).getPessoaProcurador();
			if (pessoaProcurador.getProcuradorMpSessao() && verificaVinculacaoProcuradorSessao()) {
				return true;
			}
		}
		return false;
	}

	private boolean verificaVinculacaoProcuradorSessao() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(s) from Sessao s ");
		sb.append("where s.pessoaProcurador.idUsuario = :idUsuario ");
		sb.append("and s.dataRealizacaoSessao is null ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idUsuario", Authenticator.getUsuarioLogado().getIdUsuario());
		return (Long) EntityUtil.getSingleResult(q) > 0;
	}
	
	public boolean isProcurador(Integer id){
		
			EntityManager em = getEntityManager();
			
			PessoaProcurador pp = em.find(PessoaProcurador.class, id);
			
			return (pp != null);
	}
	
	/* (non-Javadoc)
	 * @see br.com.itx.component.AbstractHome#inactive(java.lang.Object)
	 */
	@Override
	public String inactive(PessoaProcurador instance) {
		try {
			pessoaProcuradorManager = (PessoaProcuradorManager)getComponent(PessoaProcuradorManager.NAME);
			pessoaProcuradorManager.inactive(instance);
			return "update";
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}
		
		return null;
	}
	
	public void gerarNovaSenha(){
		//setando como nulo, o manager inativa a senha atual, gera nova senha e hash de ativacao
		getInstance().setHashAtivacaoSenha(PjeUtil.instance().gerarHashAtivacao(getInstance().getLogin()));
		if(getInstance().getIdUsuario() == null){
			persist();
		}else{
			update();
		}
		
		try {
			UsuarioService usuarioService = getComponent("usuarioService");
			usuarioService.enviarEmailSenha(getInstance());
			reportMessage("pje.pessoaFisicaHome.info.emailEnviadoComSucesso", null, getInstance().getEmail());
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}
	}
	
	public boolean podeAlterarOrgaoRepresentacao() {
		if(Authenticator.isPermissaoCadastroTodosPapeis()) {
			return true;
		} else if (Authenticator.isRepresentanteGestor()){
			return Authenticator.getUsuarioLogado().getIdUsuario() != instance.getIdUsuario();
		}
		return false;
	}

}
