package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.PessoaOficialJusticaMunicipioSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PessoaOficialJusticaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaServidorManager;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.GrupoOficialJustica;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;
import br.jus.pje.nucleo.entidades.PessoaServidor;

@Name("pessoaOficialJusticaHome")
@BypassInterceptors
public class PessoaOficialJusticaHome extends AbstractPessoaOficialJusticaHome<PessoaOficialJustica> {

	private static final long serialVersionUID = 1L;
	private Estado estado;
	private PessoaServidor pessoaCpfCadastrado;
	private String oldCpf;
	
	private PessoaOficialJusticaManager pessoaOficialJusticaManager;
	private PessoaServidorManager pessoaServidorManager;
	

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		if (this.estado != null) {
			if ((estado == null) || (!this.estado.getEstado().equals(estado.getEstado()))) {
				getPessoaOficialJusticaMunicipioSuggest().setInstance(null);
			}
		}
		this.estado = estado;
	}

	private PessoaOficialJusticaMunicipioSuggestBean getPessoaOficialJusticaMunicipioSuggest() {
		PessoaOficialJusticaMunicipioSuggestBean pessoaOficialJusticaMunicipioSuggest = (PessoaOficialJusticaMunicipioSuggestBean) Component
				.getInstance("pessoaOficialJusticaMunicipioSuggest");
		return pessoaOficialJusticaMunicipioSuggest;
	}

	public List<PessoaOficialJustica> listPessoaOficialJusticaPorGrupoOficialJustica( GrupoOficialJustica grupoOficialJustica){
		pessoaOficialJusticaManager = (PessoaOficialJusticaManager)getComponent(PessoaOficialJusticaManager.NAME);
		
		return pessoaOficialJusticaManager.listPessoaOficialJusticaByGrupoOficialJustica(grupoOficialJustica);
	}
	
	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		// Para o uso no cadastro de Localização que invoca o home
		PessoaHome.instance().setId(id);
		if (changed) {
			if (getInstance().getMunicipioNascimento() != null) {
				estado = getInstance().getMunicipioNascimento().getEstado();
			}
			getPessoaOficialJusticaMunicipioSuggest().setInstance(getInstance().getMunicipioNascimento());
		}
		if (id == null) {
			getPessoaOficialJusticaMunicipioSuggest().setInstance(null);
			estado = null;
		}

		if (getInstance() != null && oldCpf == null) {
			oldCpf = getInstance().getNumeroCPF();
		}
		
		setUsuarioAtivoInicial(instance.getAtivo());
		setPerfilAtivoInicial(instance.getOficialJusticaAtivo());
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		instance.setMunicipioNascimento(getPessoaOficialJusticaMunicipioSuggest().getInstance());
		if (isManaged()) {
			if (!oldCpf.equals(getInstance().getNumeroCPF())) {
				instance.setAssinatura(null);
				instance.setCertChain(null);
			}
		}
		return super.beforePersistOrUpdate();
	}

	@Override
	public void newInstance() {
		oldCpf = null;
		Contexts.removeFromAllContexts("pessoaOficialJusticaMunicipioSuggest");
		// Para o uso no cadastro de Localização que invoca o home
		PessoaHome.instance().newInstance();
		super.clearInstance(true);
	}

	@Override
	public String persist() {
		String persist = null;
		pessoaOficialJusticaManager = (PessoaOficialJusticaManager)getComponent(PessoaOficialJusticaManager.NAME);
		if (!isCpfCadastrado()) {
			if(beforePersistOrUpdate()){
				try {
					PessoaOficialJustica oficialJustica = getInstance();
					pessoaOficialJusticaManager.persistAndFlush(oficialJustica);
					setInstance(oficialJustica);
					createdMessage();
					persist = afterPersistOrUpdate("persisted");
				} catch (PJeBusinessException e) {
					reportMessage(e);
				}
			}
		} else if (pessoaCpfCadastrado != null) {
			if(beforePersistOrUpdate()){				
				try {
					PessoaOficialJustica oficialJustica = getInstance();
					pessoaOficialJusticaManager.persistAndFlush(oficialJustica);
					setInstance(oficialJustica);
					createdMessage();
					persist = afterPersistOrUpdate("persisted");
					pessoaCpfCadastrado = null;
				} catch (PJeBusinessException e) {
					reportMessage(e);
				}
			}
		}
		
		return persist;
	}

	@Override
	public String update() {
		if(beforePersistOrUpdate()){			
			try {
				pessoaOficialJusticaManager = (PessoaOficialJusticaManager)getComponent(PessoaOficialJusticaManager.NAME);
				pessoaOficialJusticaManager.persistAndFlush(getInstance());
				setInstance(getInstance());
				updatedMessage();
				
				EntityManager em = getEntityManager();		
				for (PessoaDocumentoIdentificacao documento : getInstance().getPessoaDocumentoIdentificacaoList()) {
					em.persist(documento);
					EntityUtil.flush(em);
				}
				
				atualizarEspecializacao(instance.getOficialJusticaAtivo(), instance.getPessoa(), PessoaOficialJustica.class);
				getInstance().getPessoa().setNome(getInstance().getNome());
				PessoaHome.instance().atualizarNomeLocalizacao(getInstance().getPessoa());
				
				if (isCadastroAlterado()) {
					Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
				}
				
				refreshGrid("pessoaDocumentoIdentificacaoCadastroGrid");
				refreshGrid("oficialJusticaCentralMandadoGrid");
				return afterPersistOrUpdate("update");
			} catch (PJeBusinessException e) {
				reportMessage(e);
				FacesMessages.instance().clear();
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "perfil.erro");
			}
		}
		
		return null;
	}

	public void setarInstancia() {
		try {
			newInstance();
			getInstance().setIdUsuario(pessoaCpfCadastrado.getIdUsuario());
			getInstance().setNome(pessoaCpfCadastrado.getNome());
			getInstance().setNumeroCPF(pessoaCpfCadastrado.getNumeroCPF());
			getInstance().setLogin(pessoaCpfCadastrado.getLogin());
			getInstance().setEmail(pessoaCpfCadastrado.getEmail());
			getInstance().setAtivo(pessoaCpfCadastrado.getAtivo());
			getInstance().setEtnia(pessoaCpfCadastrado.getEtnia());
			getInstance().setEstadoCivil(pessoaCpfCadastrado.getEstadoCivil());
			getInstance().setProfissao(pessoaCpfCadastrado.getProfissao());
			getInstance().setEscolaridade(pessoaCpfCadastrado.getEscolaridade());
			getInstance().setSexo(pessoaCpfCadastrado.getSexo());
			getInstance().setDataNascimento(pessoaCpfCadastrado.getDataNascimento());
			getInstance().setMunicipioNascimento(pessoaCpfCadastrado.getMunicipioNascimento());
			getInstance().setNomeGenitor(pessoaCpfCadastrado.getNomeGenitor());
			getInstance().setNomeGenitora(pessoaCpfCadastrado.getNomeGenitora());
			getInstance().setDataObito(pessoaCpfCadastrado.getDataObito());
			getInstance().setDddCelular(pessoaCpfCadastrado.getDddCelular());
			getInstance().setDddComercial(pessoaCpfCadastrado.getDddComercial());
			getInstance().setDddResidencial(pessoaCpfCadastrado.getDddResidencial());
			getInstance().setNumeroMatricula(pessoaCpfCadastrado.getNumeroMatricula());
			getInstance().setDataPosse(pessoaCpfCadastrado.getDataPosse());
			getInstance().setSenha(pessoaCpfCadastrado.getSenha());
		} catch (Exception e) {
			
		}
	}

	public Boolean checkCPF() {
		return isCpfCadastrado();
	}

	private boolean isCpfCadastrado() {
		pessoaOficialJusticaManager = (PessoaOficialJusticaManager)getComponent(PessoaOficialJusticaManager.NAME);
		PessoaOficialJustica pf = pessoaOficialJusticaManager.findByCPF(getInstance().getNumeroCPF());
		if(pf != null && !pf.getIdUsuario().equals(getInstance().getIdUsuario())){
			pessoaServidorManager = (PessoaServidorManager)getComponent(PessoaServidorManager.NAME);
			PessoaServidor ps = pessoaServidorManager.retornaByCPF(getInstance().getNumeroCPF(), getInstance().getIdUsuario());
			setPessoaCpfCadastrado(ps);
		}
		
		return (pessoaCpfCadastrado != null);
	}

	public static PessoaOficialJusticaHome instance() {
		return ComponentUtil.getComponent("pessoaOficialJusticaHome");
	}

	public String removeCertificado() {
		getInstance().setCertChain(null);
		getInstance().setAssinatura(null);
		return update();
	}

	public PessoaServidor getPessoaCpfCadastrado() {
		return pessoaCpfCadastrado;
	}

	public void setPessoaCpfCadastrado(PessoaServidor pessoaCpfCadastrado) {
		this.pessoaCpfCadastrado = pessoaCpfCadastrado;
	}
	
	/* (non-Javadoc)
	 * @see br.com.itx.component.AbstractHome#inactive(java.lang.Object)
	 */
	@Override
	public String inactive(PessoaOficialJustica instance) {
		instance.getPessoa().suprimePessoaEspecializada(instance);
		getEntityManager().merge(instance.getPessoa());
		getEntityManager().flush();
		FacesMessages.instance().add(StatusMessage.Severity.ERROR, getInactiveSuccess());
		return "update";
	}

	public String selectPorPerfil() {
		
		if (!Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("admin") ||
				!Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("administrador")) {
			
			LocalizacaoService localizacaoService = ComponentUtil.getComponent("localizacaoService");
			
			String subQueryIdsLocalizacao = 
				String.format("select cml.localizacao.idLocalizacao from CentralMandadoLocalizacao cml where cml.localizacao.idLocalizacao in %s", 
				localizacaoService.getTreeIds(Authenticator.getLocalizacaoFisicaAtual()));
			
			String subQueryIdsUsuario = 
				String.format("select ul.usuario.idUsuario from UsuarioLocalizacao ul where ul.localizacaoFisica.idLocalizacao in (%s)", 
				subQueryIdsLocalizacao);
			
			return " and o.idUsuario in (" + subQueryIdsUsuario + ") ";
		}
			
		return StringUtils.EMPTY;
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
}
