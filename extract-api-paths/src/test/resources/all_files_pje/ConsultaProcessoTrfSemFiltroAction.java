package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.event.PhaseId;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.ComplementoClasseManager;
import br.jus.cnj.pje.nucleo.manager.ConsultaProcessoTrfSemFiltroManager;
import br.jus.cnj.pje.nucleo.manager.JurisdicaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradorManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrfSemFiltro;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoVisibilidadeSegredo;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name("consultaProcessoTrfSemFiltroAction")
@Scope(ScopeType.PAGE)
public class ConsultaProcessoTrfSemFiltroAction extends BaseAction<ConsultaProcessoTrfSemFiltro>{

	private static final long serialVersionUID = 1L;
	
	@In
	private ParametroService parametroService;
	
	private Jurisdicao jurisdicao;
	private Integer idProcessoTrf;
	private String nomeParte;
	private String documentoParte;
	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private Integer numeroOrigem;
	private ClasseJudicial classe;
	private OrgaoJulgadorColegiado orgaoColegiado;
	private OrgaoJulgador orgaoJulgador;
	private Date dataAutuacaoInicio;
	private Date dataAutuacaoFim;
	private Boolean hasAvisoAcessoProcesso = false;
	private String respectivoTribunal;
	private String objetoProcesso;
	private String ramoJustica;
	private Boolean hasDadosOcultos = Boolean.FALSE;
	
	@In
	private Identity identity;
	
	@In
	private ConsultaProcessoTrfSemFiltroManager consultaProcessoTrfSemFiltroManager;
	
	@In
	private JurisdicaoManager jurisdicaoManager;
	
	@In
	private ProcuradorManager procuradorManager;
	
	@In(value=Authenticator.USUARIO_LOCALIZACAO_ATUAL, scope=ScopeType.SESSION)
	private UsuarioLocalizacao localizacaoAtual;
	
	@In(create = true)
	private ComplementoClasseManager complementoClasseManager;
	
	private EntityDataModel<ConsultaProcessoTrfSemFiltro> model;
	
	@RequestParameter(value="processosGridCount")
	private Integer processosGridCount;
	
	@RequestParameter(value="idProcessoSelecionado")
	private Integer idProcessoSelecionado;
	
	@RequestParameter(value="iframe")
	private Boolean iframe;
	
	@RequestParameter(value="botao")
	private String botao;
	
	@In
	private ProcessoParteManager processoParteManager;
	
	@Create
	public void Init(){
		if(iframe != null && iframe){
			Contexts.getConversationContext().set("showTopoSistema",false);
		}
		else{
			Contexts.getConversationContext().set("showTopoSistema",true);
		}
		Contexts.getConversationContext().set("botao", botao);
		
		String numeroOrgaoJustica = parametroService.valueOf("numeroOrgaoJustica");
		if(numeroOrgaoJustica != null){
			this.ramoJustica = numeroOrgaoJustica.substring(0, 1);
			this.respectivoTribunal = numeroOrgaoJustica.substring(1);
		}
		//Para não executar a grid ao entrar na página a primeira vez
		if (org.jboss.seam.contexts.FacesLifecycle.getPhaseId() != PhaseId.RENDER_RESPONSE){
			model = new EntityDataModel<ConsultaProcessoTrfSemFiltro>(ConsultaProcessoTrfSemFiltro.class, super.facesContext, getRetriever());
		}
	}

	public void pesquisar(){
		try {
			String nrProcesso = "";
			if (informouNrProcesso() ) {
				nrProcesso = NumeroProcessoUtil.formatNumeroProcesso(getNumeroSequencia(), getDigitoVerificador(),
						getAno(), Integer.valueOf(getRamoJustica() + getRespectivoTribunal()), getNumeroOrigem());
			}
			if(!NumeroProcessoUtil.numeroProcessoValido(nrProcesso)) {
				throw new PJeBusinessException("Número do processo inválido");
			}
			model = new EntityDataModel<ConsultaProcessoTrfSemFiltro>(ConsultaProcessoTrfSemFiltro.class, super.facesContext, getRetriever());

			List<Criteria> criterios = new ArrayList<Criteria>(0);
			criterios.addAll(getCriteriosTelaPesquisa());
			criterios.addAll(getCriteriosNegociais());
			criterios.add(Criteria.equals("numeroProcesso", nrProcesso));

			model.setCriterias(criterios);
			model.addOrder("o.dataAutuacao", Order.DESC);

			setHasDadosOcultos(Boolean.FALSE);
			List<ConsultaProcessoTrfSemFiltro> listaProcesso =  model.retreiver.list(model.search);
			for (ConsultaProcessoTrfSemFiltro consultaProcessoTrfSemFiltro : listaProcesso) {
				ProcessoTrf ProcessoTrf = consultaProcessoTrfSemFiltro.getProcessoTrf();
				verificarRegrasDeOcultacao(ProcessoTrf);					
			}				
			setIdProcessoTrf(0);
		} catch (PJeBusinessException el) {
			facesMessages.add(Severity.ERROR, el.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, e.getMessage());
		}
	}
	
	/**
	 * [PJEII-20838] - Alterar a tela de pesquisa por processos para solicitar a habilitação nos autos.
	 * - Permissão de pesquisar processos (em que não se é parte) apenas se indicado todo o número do processo
	 * - Permissão de listar processos sigilosos (se o usuário tiver logado com certificado), mostrando a informação apenas do número do processo, quando o usuário não tiver visibilidade do processo;
	 * @return
	 * @throws Exception
	 */
	public List<Criteria> getCriteriosNegociais() throws Exception{
		
		Usuario usuarioLogado 	= localizacaoAtual.getUsuario();
		
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		criterios.add(Criteria.equals("processoStatus", ProcessoStatusEnum.D));
	    

		if (!informouNrProcesso()){
			criterios.add(Criteria.equals("processoTrf.processoParteList.pessoa.idPessoa", usuarioLogado.getIdUsuario()));
		}
			
		return criterios;
	}

	private boolean informouNrProcesso() {
		return (numeroSequencia != null && digitoVerificador != null && ano != null && numeroOrigem != null);
	}

	/**
	 * PJEII-20838
	 * Método responsável por verificar se determinado processo deve ter seus detalhes ocultos caso seja sigiloso e o advogado não faça parte do mesmo.
	 * @param processo Processo a ser verificado
	 */
	private void verificarRegrasDeOcultacao(ProcessoTrf processo){
		if (processo.getSegredoJustica() && !verificaSeAdvogadoComVisibilidade(processo)){
			setHasDadosOcultos(Boolean.TRUE);	
			FacesMessages.instance().add(Severity.WARN, "O processo selecionado é sigiloso e você não possui visibilidade a ele, por isso não é possível visualizar seus detalhes.");
		}
	}
	
	
	/**
	 * PJEII-20838
	 * Método responsável por verificar se o advogado logado faz parte de um determinado processo
	 * @param processo Processo a ser verificado
	 * @return TRUE caso o advogado faça parte do respectivo processo.
	 */
	private boolean verificaSeAdvogadoComVisibilidade(ProcessoTrf processo){
 		Usuario usuarioLogado 	= localizacaoAtual.getUsuario();
		boolean ehVisualizadorEAdvogadoDoProcesso = false;
				
		for (ProcessoVisibilidadeSegredo visualizador  : processo.getVisualizadores()) {
			if(visualizador.getPessoa().getIdUsuario().equals(usuarioLogado.getIdUsuario())) {
				for (ProcessoParte processoParte : processo.getProcessoParteList()) {
					if(processoParte.getPessoa().getIdUsuario().equals(usuarioLogado.getIdUsuario()) && processoParte.getIsAtivo() && processoParte.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())){
						ehVisualizadorEAdvogadoDoProcesso = true;
						break;
					}
				}	
				break;
			}
		}
		return ehVisualizadorEAdvogadoDoProcesso;
	}
	
	public List<Criteria> getCriteriosTelaPesquisa() throws Exception{
		
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		
		if (numeroSequencia != null && numeroSequencia > 0){
			criterios.add(Criteria.equals("numeroSequencia",numeroSequencia));
		}
		
		if (digitoVerificador != null && digitoVerificador > 0){
			criterios.add(Criteria.equals("numeroDigitoVerificador",digitoVerificador));
		}
		
		if (ano != null && ano > 0){
			criterios.add(Criteria.equals("ano",ano));
		}
		
		if (numeroOrigem != null && numeroOrigem > 0){
			criterios.add(Criteria.equals("numeroOrigem",numeroOrigem));
		}
		
		if (criterios.size() == 0){
			throw new Exception("Pelo menos um dos critérios de pesquisa deve ser informado.");
		}
		return criterios;
		
	}
	
	public void verificarVisualizacaoProcesso(){
		
		boolean isAdmin = identity.hasRole("admin") || identity.hasRole("administrador");
		boolean isAdvogado = identity.hasRole("advogado") || identity.hasRole("assistAdvogado"); 
		boolean isJusPostulandi = identity.hasRole("jusPostulandi");
		boolean logarAcessoProcessoTerceiro = false;
		
		if (isAdvogado && !isAdmin){
			
			Query q = EntityUtil.getEntityManager().createQuery("select 1 from ProcessoParte o " +
																"where o.pessoa.idUsuario = :idUsuario " +
																"and o.processoTrf.idProcessoTrf = :idProcessoTrf " +
																"and o.tipoParte.idTipoParte = :idTipoParte " +
																"and o.inSituacao = 'A' ");
			q.setParameter("idUsuario", Authenticator.getUsuarioLogado().getIdUsuario());
			q.setParameter("idProcessoTrf", idProcessoSelecionado);
			q.setParameter("idTipoParte", ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte());
			logarAcessoProcessoTerceiro = q.getResultList().size() == 0;
		}
		
		if (identity.hasRole("procurador") && !isAdmin){
			
			Integer idProcuradoria = Authenticator.getIdProcuradoriaAtualUsuarioLogado();
			Usuario usuario = Authenticator.getPessoaLogada();
			logarAcessoProcessoTerceiro = (processoParteManager.isParte(idProcessoSelecionado, usuario, idProcuradoria) == false);
		}

		if(isJusPostulandi && !isAdmin){
			Query queryVerificaParte = EntityUtil.getEntityManager().createQuery(
					"select 1 from ProcessoParte o " +
					"where o.pessoa.idUsuario = :idUsuario " +
					"and o.processoTrf.idProcessoTrf = :idProcessoTrf " +
					"and o.inSituacao = :processoParteSituacaoEnum1");

			queryVerificaParte.setParameter("idUsuario", Authenticator.getUsuarioLogado().getIdUsuario());
			queryVerificaParte.setParameter("idProcessoTrf", idProcessoSelecionado);
			queryVerificaParte.setParameter("processoParteSituacaoEnum1", ProcessoParteSituacaoEnum.A);
			
			boolean isParteProcesso = queryVerificaParte.getResultList().size() > 0;			
			
			Query queryVerificaExpedienteAberto = EntityUtil.getEntityManager().createQuery(
					"select 1 from ProcessoParteExpediente ppe " + 
					"where ppe.processoJudicial.idProcessoTrf = :idProcessoTrf and " + 
					"ppe.pessoaParte.idPessoa = :idUsuario and (ppe.dtCienciaParte is null or ppe.resposta is null)");
			
			queryVerificaExpedienteAberto.setParameter("idUsuario", Authenticator.getUsuarioLogado().getIdUsuario());
			queryVerificaExpedienteAberto.setParameter("idProcessoTrf",idProcessoSelecionado);
			
			boolean possuiExpedienteAberto = queryVerificaExpedienteAberto.getResultList().size() > 0;

			logarAcessoProcessoTerceiro = !(isParteProcesso || possuiExpedienteAberto);
		}
		
		hasAvisoAcessoProcesso = logarAcessoProcessoTerceiro;
		
	}
	
	@Override
	protected DataRetriever<ConsultaProcessoTrfSemFiltro> getRetriever() {
		
		final ConsultaProcessoTrfSemFiltroManager manager = (ConsultaProcessoTrfSemFiltroManager)getManager();
		final Integer tableCount = processosGridCount;
		DataRetriever<ConsultaProcessoTrfSemFiltro> retriever = new DataRetriever<ConsultaProcessoTrfSemFiltro>() {
			@Override
			public ConsultaProcessoTrfSemFiltro findById(Object id) throws Exception {
				try {
					return manager.findById(id);
				} catch (PJeBusinessException e) {
					throw new Exception(e);
				}
			}
			@Override
			public List<ConsultaProcessoTrfSemFiltro> list(Search search) {
				if (search.getCriterias().size() == 0){
					return Collections.emptyList();
				}
				return manager.list(search);
			}
			@Override
			public long count(Search search) {
				if (tableCount != null && tableCount >=0){
					return tableCount;
				}
				
				if (search.getCriterias().size() == 0){
					return 0;
				}
				return manager.count(search);
			}
			@Override
			public Object getId(ConsultaProcessoTrfSemFiltro obj){
				return manager.getId(obj);
			}
		};
		return retriever;
	}
	
  	public void limparCamposPesquisa() {
		this.numeroSequencia = null;
		this.digitoVerificador = null;
		this.ano = null;
		this.numeroOrigem = null;
  	}

	
	/**
	 * Retorna true se for para exibir a mensagem da resolução 121. 
	 * A mensagem é renderizada pelo componente javascriptMensagemResolucacao121.xhtml.
	 * 
	 * @return booleano
	 */
	public Boolean isExibirMensagemResolucao121() {
		return 	(identity.hasRole("jusPostulandi") == false) &&
				(getHasAvisoAcessoProcesso()) &&
				(getIdProcessoSelecionado() != null);
	}
	
	@Override
	protected BaseManager<ConsultaProcessoTrfSemFiltro> getManager() {
		return consultaProcessoTrfSemFiltroManager;
	}

	@Override
	public EntityDataModel<ConsultaProcessoTrfSemFiltro> getModel() {
		return this.model;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public Integer getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(Integer idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}
	
	public ClasseJudicial getClasse() {
		return classe;
	}

	public void setClasse(ClasseJudicial classe) {
		this.classe = classe;
	}

	public String getDocumentoParte() {
		return documentoParte;
	}

	public void setDocumentoParte(String documentoParte) {
		this.documentoParte = documentoParte;
	}

	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	public Integer getDigitoVerificador() {
		return digitoVerificador;
	}

	public void setDigitoVerificador(Integer digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public Date getDataAutuacaoInicio() {
		return dataAutuacaoInicio;
	}
	  

	public void setDataAutuacaoInicio(Date dataAutuacaoInicio) {
		this.dataAutuacaoInicio = dataAutuacaoInicio;
	}
	
	public Date getDataAutuacaoFim() {
		return dataAutuacaoFim;
	}

	public void setDataAutuacaoFim(Date dataAutuacaoFim) {
		this.dataAutuacaoFim = dataAutuacaoFim;
	}

	public OrgaoJulgadorColegiado getOrgaoColegiado() {
		return orgaoColegiado;
	}

	public void setOrgaoColegiado(OrgaoJulgadorColegiado orgaoColegiado) {
		this.orgaoColegiado = orgaoColegiado;
	}

	public Boolean getHasAvisoAcessoProcesso() {
		return hasAvisoAcessoProcesso;
	}

	public void setHasAvisoAcessoProcesso(Boolean hasAvisoAcessoProcesso) {
		this.hasAvisoAcessoProcesso = hasAvisoAcessoProcesso;
	}

	public Integer getIdProcessoSelecionado() {
		return idProcessoSelecionado;
	}

	public String getRespectivoTribunal() {
		return respectivoTribunal;
	}

	public void setRespectivoTribunal(String respectivoTribunal) {
		this.respectivoTribunal = respectivoTribunal;
	}

	public String getRamoJustica() {
		return ramoJustica;
	}
	
	public void setRamoJustica(String ramoJustica) {
		this.ramoJustica = ramoJustica;
	}

	public String getObjetoProcesso() {
		return objetoProcesso;
	}

	public void setObjetoProcesso(String objetoProcesso) {
		this.objetoProcesso = objetoProcesso;
	}

	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	public Boolean getHasDadosOcultos() {
		return hasDadosOcultos;
	}

	public void setHasDadosOcultos(Boolean hasDadosOcultos) {
		this.hasDadosOcultos = hasDadosOcultos;
	}
 	
}
