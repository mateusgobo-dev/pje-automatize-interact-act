package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.component.suggest.NumeroProcessoTrfSuggestBean;
import br.com.infox.cliente.component.suggest.PessoaMagistradoMunicipioSuggestBean;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.entidades.vo.LinkLocalizacaoProcesso;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.entidades.Caixa;
import br.jus.pje.nucleo.entidades.CaixaFiltro;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfUsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.enums.TitularidadeMagistradoEnum;

@Name("pessoaMagistradoHome")
@BypassInterceptors
public class PessoaMagistradoHome extends AbstractPessoaMagistradoHome<PessoaMagistrado> {

	private static final long serialVersionUID = 1L;

	private Estado estado;
	private String oldCpf;
	private Localizacao localizacao;
	private String exibirInformacao;
	private ProcessoTrf processoTrf;
	private PessoaMagistradoManager pessoaMagistradoManager;
	private List<LinkLocalizacaoProcesso> linksLocProc;
	
	public String nomeOrgaoJulgadorMagistrado;
	
	public String getNomeOrgaoJulgadorMagistrado(){
		return nomeOrgaoJulgadorMagistrado;
	}
	
	public void setNomeOrgaoJulgadorMagistrado(String nomeOrgaoJulgadorMagistrado){
		this.nomeOrgaoJulgadorMagistrado = nomeOrgaoJulgadorMagistrado;
	}
	
	public List<LinkLocalizacaoProcesso> getLinksLocProc() {
		if (linksLocProc == null) {
			linksLocProc = new ArrayList<LinkLocalizacaoProcesso>(0);
		}
		return linksLocProc;
	}

	public void setLinksLocProc(List<LinkLocalizacaoProcesso> linksLocProc) {
		this.linksLocProc = linksLocProc;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		if (this.estado != null) {
			if ((estado == null) || (!this.estado.getEstado().equals(estado.getEstado()))) {
				getPessoaMagistradoMunicipioSuggest().setInstance(null);
			}
		}
		this.estado = estado;
	}

	private PessoaMagistradoMunicipioSuggestBean getPessoaMagistradoMunicipioSuggest() {
		PessoaMagistradoMunicipioSuggestBean pessoaMagistradoMunicipioSuggest = 
			(PessoaMagistradoMunicipioSuggestBean) Component.getInstance("pessoaMagistradoMunicipioSuggest");
		return pessoaMagistradoMunicipioSuggest;
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
			getPessoaMagistradoMunicipioSuggest().setInstance(getInstance().getMunicipioNascimento());
		}
		if (id == null) {
			getPessoaMagistradoMunicipioSuggest().setInstance(null);
			estado = null;
		}

		if (getInstance() != null && oldCpf == null) {
			oldCpf = getInstance().getNumeroCPF();
		}
		
		setUsuarioAtivoInicial(instance.getAtivo());
		setPerfilAtivoInicial(instance.getMagistradoAtivo());
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		instance.setMunicipioNascimento(getPessoaMagistradoMunicipioSuggest().getInstance());
		if (isManaged()) {
			if (oldCpf != null && !oldCpf.equals(getInstance().getNumeroCPF())) {
				instance.setAssinatura(null);
				instance.setCertChain(null);
			}
		}
		return super.beforePersistOrUpdate();
	}

	public String removeCertificado() {
		getInstance().setCertChain(null);
		getInstance().setAssinatura(null);
		return update();
	}

	@Override
	public void newInstance() {
		oldCpf = null;
		Contexts.removeFromAllContexts("pessoaMagistradoMunicipioSuggest");
		super.clearInstance(true);
		// Para o uso no cadastro de Localização que invoca o home
		PessoaHome.instance().newInstance();
	}

	@Override
	public String persist() {
		if (beforePersistOrUpdate()) {
			try {
				pessoaMagistradoManager = getComponent(PessoaMagistradoManager.NAME);
				pessoaMagistradoManager.persistAndFlush(getInstance());
				setInstance(getInstance());
				createdMessage();
				return afterPersistOrUpdate("persisted");
			} catch (PJeBusinessException e) {
				reportMessage(e);
			}
		}

		refreshGrid("pessoaMagistradoGrid");

		return null;
	}

	@Override
	public String update() {
		if (beforePersistOrUpdate()) {
			try {
				pessoaMagistradoManager = getComponent(PessoaMagistradoManager.NAME);
				pessoaMagistradoManager.persistAndFlush(getInstance());
				setInstance(getInstance());
				updatedMessage();

				EntityManager em = getEntityManager();
				for (PessoaDocumentoIdentificacao documento : getInstance().getPessoaDocumentoIdentificacaoList()) {
					em.persist(documento);
					EntityUtil.flush(em);
				}
				refreshGrid("pessoaDocumentoIdentificacaoCadastroGrid");

				atualizarEspecializacao(instance.getMagistradoAtivo(),  instance.getPessoa(), PessoaMagistrado.class);
				getInstance().getPessoa().setNome(getInstance().getNome());
				PessoaHome.instance().atualizarNomeLocalizacao(getInstance().getPessoa());
				
				if (isCadastroAlterado()) {
					Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
				}
				
				return afterPersistOrUpdate("persisted");
			} catch (PJeBusinessException e) {
				reportMessage(e);
				FacesMessages.instance().clear();
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getLocalizedMessage());
			}
		}

		return null;
	}

	public static PessoaMagistradoHome instance() {
		return ComponentUtil.getComponent("pessoaMagistradoHome");
	}

	public TitularidadeMagistradoEnum[] getTitularidadeMagistradoEnumValues() {
		return TitularidadeMagistradoEnum.values();
	}

	/**
	 * Metodo que intercepta o evento e remove da lista de localizações do
	 * usuário na memoria os UsuarioLocalizacaoMagistradoServidor não válidos
	 * 
	 * @param usuarioLocalizacaoList
	 */
	@SuppressWarnings("unchecked")
	@Observer(Authenticator.SET_USUARIO_LOCALIZACAO_LIST_EVENT)
	public void setUsuarioLocalizacaoEvento(List<UsuarioLocalizacao> usuarioLocalizacaoList) {
		List<Integer> idUsuarioLocalizacaoList = getIdUsuarioLocalizacaoList(usuarioLocalizacaoList);
		if (idUsuarioLocalizacaoList.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from UsuarioLocalizacaoMagistradoServidor ");
			sb.append("o where o.usuarioLocalizacao.idUsuarioLocalizacao IN (:usuarioLocalizacaoList) ");
			sb.append("and NOT (current_date >= cast(o.dtInicio as date) ");
			sb.append("and (cast(o.dtFinal as date) is null OR cast(o.dtFinal as date) > current_date))");
			sb.append("order by o.usuarioLocalizacao.localizacaoFisica, o.usuarioLocalizacao.papel ");
			Query query = getEntityManager().createQuery(sb.toString());
			query.setParameter("usuarioLocalizacaoList", idUsuarioLocalizacaoList);
			List<UsuarioLocalizacaoMagistradoServidor> resultList = query.getResultList();
			for (UsuarioLocalizacaoMagistradoServidor locMagistradoServidor : resultList) {
				usuarioLocalizacaoList.remove(locMagistradoServidor.getUsuarioLocalizacao());
			}
		}
		if (usuarioLocalizacaoList.isEmpty()) {
			FacesMessages.instance().add(Severity.INFO, "Usuário não possui localização válida");
			Identity.instance().unAuthenticate();
		}
	}

	private List<Integer> getIdUsuarioLocalizacaoList(List<UsuarioLocalizacao> usuarioLocalizacaoList) {
		List<Integer> resp = new ArrayList<Integer>();
		for (UsuarioLocalizacao ul : usuarioLocalizacaoList) {
			resp.add(ul.getIdUsuarioLocalizacao());
		}
		return resp;
	}

	public void setExibirInformacao(String exibirInformacao) {
		this.exibirInformacao = exibirInformacao;
	}

	public String getExibirInformacao() {
		return exibirInformacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@SuppressWarnings("unchecked")	
	public void localizarCaixa() {
		if (getProcessoTrf() == null) {
			setExibirInformacao("Processo não encontrado!");
			return;
		}
		String query = "SELECT o FROM SituacaoProcesso AS o WHERE o.processoTrf.idProcessoTrf = :idProcesso";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("idProcesso", getProcessoTrf().getIdProcessoTrf());

		List<SituacaoProcesso> ret = q.getResultList();
		if (ret.isEmpty()) {
			setExibirInformacao("Processo não encontrado!");
		} else {
			montarLinksAbrirTarefa(ret);
		}
	}

	/**
	 * @author t317549- Antonio Francisco Osorio Jr/TJDFT
	 * 
	 * Método responsável por montar os links que permitirão:
	 * 	<ul>
	 * 		<li>Abrir os detalhes do processo, ou</li>
	 * 		<li>Abrir a tarefa a qual o processo se encontra.</li>
	 *	</ul>
	 * 
	 * @param situacoesDosProcessos
	 */
	private void montarLinksAbrirTarefa(List<SituacaoProcesso> SituacaoProcessoList) {
		LinkLocalizacaoProcesso link = null;
		Set<LinkLocalizacaoProcesso> links = new TreeSet<LinkLocalizacaoProcesso>();
		
		for (SituacaoProcesso situacaoProcesso : SituacaoProcessoList) {
			getEntityManager().refresh(situacaoProcesso);
			
			link = new LinkLocalizacaoProcesso();
			link.setNomeTarefa(situacaoProcesso.getNomeTarefa());
			
			if (situacaoProcesso.getIdCaixa() != null) {
				Caixa caixa = situacaoProcesso.getProcessoTrf().getProcesso().getCaixa();
				link.setIdCaixa(caixa.getIdCaixa());
				CaixaFiltro caixaFiltro = getEntityManager().find(CaixaFiltro.class, caixa.getIdCaixa());
				link.setNomeCaixa(caixaFiltro.getNomeCaixa());
				link.setMensagem(situacaoProcesso.getNomeTarefa() + " / " + caixaFiltro.getNomeCaixa());
				link.setIdTarefa(situacaoProcesso.getIdTarefa());
			} else {
				link.setMensagem(situacaoProcesso.getNomeTarefa());
			}
			
			if (situacaoProcesso.getProcessoTrf().getJurisdicao() == null) {
				setExibirInformacao("Processo não encontrado!");
				break;
			}
			link.setIdProcesso(situacaoProcesso.getIdProcesso());
			link.setNumeroSequencia(situacaoProcesso.getProcessoTrf().getNumeroSequencia());
			links.add(link);
		}
		getLinksLocProc().addAll(links);
	}

	public void setarNumeroProcesso(ProcessoTrf processo) {
		setLinksLocProc(null);
		setProcessoTrf(processo);
		getNumeroProcessoTrfSuggest().setInstance(getProcessoTrf());
		localizarCaixa();
	}

	private NumeroProcessoTrfSuggestBean getNumeroProcessoTrfSuggest() {
		return getComponent("numeroProcessoTrfSuggest");
	}

	public void limparPesquisa() {
		setExibirInformacao(null);
		setProcessoTrf(null);
		getNumeroProcessoTrfSuggest().setInstance(null);
		setLinksLocProc(null);
		
	}

	public void limparCamposBrasileiro() {
		if (instance.getBrasileiro() == true) {
			instance.setNumeroPassaporte(null);
		} else {
			setEstado(null);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public String inactive(PessoaMagistrado instance) {
		PessoaService pessoaService = ComponentUtil.getComponent("pessoaService");
		try {
			PessoaFisica pessoaFisica = (PessoaFisica) pessoaService.desespecializa(instance.getPessoa(), PessoaMagistrado.class);
			instance = pessoaFisica.getPessoaMagistrado();
			instance.setMagistradoAtivo(Boolean.FALSE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pessoaMagistrado.perfil.inativado");
			Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, e.getCode());
			e.printStackTrace();
		}
		return "update";
	}

	/**
	 * Este método reativa o magistrado.
	 * 
	 * @param pessoaServidor
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ativarMagistrado(PessoaMagistrado instance) {
		PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.NAME);
		try {
			PessoaFisica pessoaFisica = (PessoaFisica) pessoaService.especializa(instance.getPessoa(), PessoaMagistrado.class);
			instance = pessoaFisica.getPessoaMagistrado();
			instance.setMagistradoAtivo(Boolean.TRUE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pessoaMagistrado.perfil.ativado");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "perfil.erro");
			e.printStackTrace();
		}
		return "update";
	}

	public void gerarNovaSenha() {
		// setando como nulo, o manager inativa a senha atual, gera nova senha e hash de ativacao.
		getInstance().setHashAtivacaoSenha(PjeUtil.instance().gerarHashAtivacao(getInstance().getLogin()));
		if (getInstance().getIdUsuario() == null) {
			persist();
		} else {
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

	public void checkCPF() {
		pessoaMagistradoManager = getComponent(PessoaMagistradoManager.NAME);
		Boolean isCadastrado = pessoaMagistradoManager.checkCPF(getInstance().getNumeroCPF(), getInstance().getIdUsuario());

		if (isCadastrado) {
			FacesMessages.instance().addToControl("numeroCPFCpf", StatusMessage.Severity.ERROR, "CPF já cadastrado!");
		}
	}

	@SuppressWarnings("unchecked")
	public Boolean verificaProcessoMagistrado(PessoaMagistrado pessoaMagistrado) {
		StringBuilder sb = new StringBuilder();
		sb.append("select 1 from ProcessoTrfUsuarioLocalizacaoMagistradoServidor o ");
		sb.append("where o.usuarioLocalizacaoMagistradoServidor.usuarioLocalizacao.usuario.idUsuario = :idUsuario");		
			
		EntityManager em = EntityUtil.getEntityManager();
		Query query = em.createQuery(sb.toString());
		query.setParameter("idUsuario", pessoaMagistrado.getPessoa().getIdPessoa());
			
		List<ProcessoTrfUsuarioLocalizacaoMagistradoServidor> list = query.getResultList();  
		return list.isEmpty();		
	}
	
	/**
	 * Retorna true se o magistrado é titular em algum órgão julgador.
	 * 
	 * @param pessoaMagistrado PessoaMagistrado
	 * @return booleano
	 */
	public boolean isMagistradoTitular(PessoaMagistrado pessoaMagistrado){
		PessoaMagistradoManager pessoaMagistradoManager = getComponent(PessoaMagistradoManager.NAME);
		List<OrgaoJulgador> ojs = pessoaMagistradoManager.consultarOrgaoJulgadorMagistradoTitular(
				pessoaMagistrado);
		Boolean isMagistradoTitular = ProjetoUtil.isNotVazio(ojs);
		
		if (isMagistradoTitular) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (OrgaoJulgador oj : ojs) {
				sb.append(oj.getOrgaoJulgador()).append(",");
			}
			sb.deleteCharAt(sb.lastIndexOf(","));
			sb.append(")");
			this.nomeOrgaoJulgadorMagistrado = sb.toString();
		}
		
		return isMagistradoTitular;
	}
	
	public boolean isApresentaImpedimentoSuspeicao() {
		return this.isManaged() && Authenticator.isAdministradorAutuacao();
	}
}
