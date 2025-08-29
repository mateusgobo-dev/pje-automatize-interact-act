package br.com.infox.pje.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.ProcessoDocumentoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.PapelManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoPeticaoNaoLidaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteCentralMandadoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.RevisorProcessoTrfDevolvidoManager;
import br.jus.cnj.pje.nucleo.manager.RevisorProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoVisibilidadeManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.jt.enums.SituacaoHabilitacaoEnum;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.RevisorProcessoTrf;
import br.jus.pje.nucleo.entidades.RevisorProcessoTrfDevolvido;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.filters.ProcessoTrfFilter;
import br.jus.pje.nucleo.enums.FiltroTempoAgrupadoresEnum;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Classe responsável pelo controle de requisições da aba "Agrupadores".
 */
@Name(AgrupadoresAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class AgrupadoresAction extends BaseAction<ProcessoTrf> {

	private static final long serialVersionUID = 4808926811649582572L;
	
	public static final String NAME = "agrupadoresAction";
	public static final String DATA_JUNTADA = "dataJuntada";
	
	private static final int QTD_LISTA_VAZIA = 0;
	
	private String numeroProcesso;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assunto;
	private OrgaoJulgador orgaoJulgador;
	private String numeroCPF;
	private String numeroCNPJ;
	private boolean documentoCPF = false;
	private String nomeParte;
	private String numeroOab;
	private String letraOab;
	private Estado ufOab;
	private boolean processoComParteSemCPFCNPJ = false;
	private FiltroTempoAgrupadoresEnum filtroTempoAgrupadores = FiltroTempoAgrupadoresEnum.ULTIMOS_15_DIAS;
	
	private UsuarioLocalizacaoMagistradoServidor localizacaoServidor;
	private Boolean existeDefinicaoVisibilidadePorCargo;
	
	private boolean permiteVisualizarAbaAgrupadores = false;
	private boolean permiteVisualizarAgrupadorProcessoComPedidoDeSigilo = false;
	private boolean permiteVisualizarAgrupadorDocumentoComPedidoDeSigilo = false;
	private boolean permiteVisualizarAgrupadorPedidosDeJusticaGratuita = false;
	private boolean permiteVisualizarAgrupadorPedidoDeLiminarTutela = false;
	private boolean permiteVisualizarAgrupadorHabilitacaoNosAutos = false;
	private boolean permiteVisualizarAgrupadorAnaliseDePrevencao = false;
	private boolean permiteVisualizarAgrupadorDocumentosNaoLidos = false;
	private boolean permiteVisualizarAgrupadorMandadosDevolvidos = false;
	private boolean permiteVisualizarAgrupadorAguardandoRevisao = false;
	private boolean permiteVisualizarAgrupadorPeticoesAvulsas = false;
	
	private EntityDataModel<ProcessoTrf> processoPedidoSegredoNaoApreciado;
	private EntityDataModel<ProcessoDocumento> processoPedidoSigiloDocumentoNaoApreciado;
	private EntityDataModel<ProcessoTrf> processoPedidoAssistenciaJudiciariaGratuitaNaoApreciado;
	private EntityDataModel<ProcessoTrf> processoPedidoLiminarAntecipacaoTutelaNaoApreciado;
	private EntityDataModel<ProcessoDocumentoPeticaoNaoLida> processoHabilitacaoAutosNaoLidos;
	private EntityDataModel<ProcessoTrf> processoAnalisePrevencao;
	private EntityDataModel<ProcessoDocumento> processoDocumentoNaoLido;
	private EntityDataModel<ProcessoExpedienteCentralMandado> processoMandadoDevolvido;
	private EntityDataModel<RevisorProcessoTrf> processoAguardandoRevisao;
	private EntityDataModel<RevisorProcessoTrf> processoRevisado;
	private EntityDataModel<RevisorProcessoTrfDevolvido> processoDevolvidoRevisao;
	private EntityDataModel<ProcessoDocumentoPeticaoNaoLida> processoPeticoesAvulsasNaoLidas;
	
	private Boolean processoPedidoSegredoNaoApreciadoCheckAll;
	private Boolean processoPedidoSigiloDocumentoNaoApreciadoCheckAll;
	private Boolean processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheckAll;
	private Boolean processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheckAll;
	private Boolean processoHabilitacaoAutosNaoLidosCheckAll;
	private Boolean processoDocumentoNaoLidoCheckAll;
	private Boolean processoMandadoDevolvidoCheckAll;
	private Boolean processoPeticoesAvulsasNaoLidasCheckAll;
	
	private Map<ProcessoTrf, Boolean> processoPedidoSegredoNaoApreciadoCheck;
	private Map<ProcessoDocumento, Boolean> processoPedidoSigiloDocumentoNaoApreciadoCheck;
	private Map<ProcessoTrf, Boolean> processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheck;
	private Map<ProcessoTrf, Boolean> processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheck;
	private Map<ProcessoDocumentoPeticaoNaoLida, Boolean> processoHabilitacaoAutosNaoLidosCheck;
	private Map<ProcessoDocumento, Boolean> processoDocumentoNaoLidoCheck;
	private Map<ProcessoExpedienteCentralMandado, Boolean> processoMandadoDevolvidoCheck;
	private Map<ProcessoDocumentoPeticaoNaoLida, Boolean> processoPeticoesAvulsasNaoLidasCheck;

	public AgrupadoresAction() {
		this.processoPedidoSegredoNaoApreciadoCheckAll = Boolean.FALSE;
		this.processoPedidoSigiloDocumentoNaoApreciadoCheckAll = Boolean.FALSE;
		this.processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheckAll = Boolean.FALSE;
		this.processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheckAll = Boolean.FALSE;
		this.processoHabilitacaoAutosNaoLidosCheckAll = Boolean.FALSE;
		this.processoDocumentoNaoLidoCheckAll = Boolean.FALSE;
		this.processoMandadoDevolvidoCheckAll = Boolean.FALSE;
		this.processoPeticoesAvulsasNaoLidasCheckAll  = Boolean.FALSE;

		this.processoPedidoSegredoNaoApreciadoCheck = new LinkedHashMap<ProcessoTrf, Boolean>(0);
		this.processoPedidoSigiloDocumentoNaoApreciadoCheck = new LinkedHashMap<ProcessoDocumento, Boolean>(0);
		this.processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheck = new LinkedHashMap<ProcessoTrf, Boolean>(0);
		this.processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheck = new LinkedHashMap<ProcessoTrf, Boolean>(0);
		this.processoHabilitacaoAutosNaoLidosCheck = new LinkedHashMap<ProcessoDocumentoPeticaoNaoLida, Boolean>(0);
		this.processoDocumentoNaoLidoCheck = new LinkedHashMap<ProcessoDocumento, Boolean>(0);
		this.processoMandadoDevolvidoCheck = new LinkedHashMap<ProcessoExpedienteCentralMandado, Boolean>(0);
		this.processoPeticoesAvulsasNaoLidasCheck = new LinkedHashMap<ProcessoDocumentoPeticaoNaoLida, Boolean>(0);
	}
	
	@Override
	protected BaseManager<ProcessoTrf> getManager() {
		return ComponentUtil.getComponent(ProcessoJudicialManager.class);
	}

	@Override
	public EntityDataModel<ProcessoTrf> getModel() {
		return null;
	}

	/**
	 * Método responsável pela inicialização da classe.
	 */
	@Create
	public void init() {
		pesquisar();
		inicializaPermissoesAgrupadores();
	}
	
	private void inicializaPermissoesAgrupadores() {
		initPermissoesAbaAgrupadores();
		initPermissoesVisualizarAgrupadorAguardandoRevisao();
		initPermissoesVisualizarAgrupadorAnaliseDePrevencao();
		initPermissoesVisualizarAgrupadorDocumentoComPedidoDeSigilo();
		initPermissoesVisualizarAgrupadorDocumentosNaoLidos();
		initPermissoesVisualizarAgrupadorHabilitacaoNosAutos();
		initPermissoesVisualizarAgrupadorMandadosDevolvidos();
		initPermissoesVisualizarAgrupadorPedidoDeLiminarTutela();
		initPermissoesVisualizarAgrupadorPedidosDeJusticaGratuita();
		initPermissoesVisualizarAgrupadorPeticoesAvulsas();
		initPermissoesVisualizarAgrupadorProcessoComPedidoDeSigilo();
	}
	
	private void initPermissoesAbaAgrupadores() {
		setPermiteVisualizarAbaAgrupadores(!Authenticator.hasRole(Papeis.OCULTAR_AGRUPADOR));
	}

	private void initPermissoesVisualizarAgrupadorProcessoComPedidoDeSigilo() {
		setPermiteVisualizarAgrupadorProcessoComPedidoDeSigilo(isPermiteVisualizarAbaAgrupadores() && 
				Authenticator.isPapelAtualMagistrado() && !Authenticator.hasRole(Papeis.OCULTAR_AGRUPADOR_PROCESSO_SIGILOSO));
	}

	private void initPermissoesVisualizarAgrupadorDocumentoComPedidoDeSigilo() {
		setPermiteVisualizarAgrupadorDocumentoComPedidoDeSigilo(isPermiteVisualizarAbaAgrupadores() && 
				Authenticator.isPapelAtualMagistrado() && !Authenticator.hasRole(Papeis.OCULTAR_AGRUPADOR_DOCUMENTO_SIGILOSO));
	}

	private void initPermissoesVisualizarAgrupadorPedidosDeJusticaGratuita() {
		setPermiteVisualizarAgrupadorPedidosDeJusticaGratuita(isPermiteVisualizarAbaAgrupadores() && 
				!Authenticator.hasRole(Papeis.OCULTAR_AGRUPADOR_PEDIDO_JUSTICA_GRATUITA));
	}

	private void initPermissoesVisualizarAgrupadorPedidoDeLiminarTutela() {
		setPermiteVisualizarAgrupadorPedidoDeLiminarTutela(isPermiteVisualizarAbaAgrupadores() && 
				Authenticator.hasRole(Papeis.AGRUPADOR_PEDIDO_TUTELA_LIMINAR));
	}

	private void initPermissoesVisualizarAgrupadorHabilitacaoNosAutos() {
		setPermiteVisualizarAgrupadorHabilitacaoNosAutos(isPermiteVisualizarAbaAgrupadores() && 
				!Authenticator.hasRole(Papeis.OCULTAR_AGRUPADOR_PEDIDO_HABILITACAO_AUTOS));
	}

	private void initPermissoesVisualizarAgrupadorAnaliseDePrevencao() {
		setPermiteVisualizarAgrupadorAnaliseDePrevencao(isPermiteVisualizarAbaAgrupadores() && 
				!Authenticator.hasRole(Papeis.OCULTAR_AGRUPADOR_ANALISE_PREVENCAO));
	}

	private void initPermissoesVisualizarAgrupadorDocumentosNaoLidos() {
		setPermiteVisualizarAgrupadorDocumentosNaoLidos(isPermiteVisualizarAbaAgrupadores() && 
				!Authenticator.hasRole(Papeis.OCULTAR_AGRUPADOR_DOCUMENTOS_NAO_LIDOS));
	}

	private void initPermissoesVisualizarAgrupadorMandadosDevolvidos() {
		setPermiteVisualizarAgrupadorMandadosDevolvidos(isPermiteVisualizarAbaAgrupadores() && 
				!Authenticator.hasRole(Papeis.OCULTAR_AGRUPADOR_MANDADOS_DEVOLVIDOS));
	}

	private void initPermissoesVisualizarAgrupadorAguardandoRevisao() {
		setPermiteVisualizarAgrupadorAguardandoRevisao(isPermiteVisualizarAbaAgrupadores() && 
				(!ParametroUtil.instance().isPrimeiroGrau()) && 
				!Authenticator.hasRole(Papeis.OCULTAR_AGRUPADOR_AGUARDANDO_REVISAO));
	}

	private void initPermissoesVisualizarAgrupadorPeticoesAvulsas() {
		setPermiteVisualizarAgrupadorPeticoesAvulsas(isPermiteVisualizarAbaAgrupadores() && 
				!Authenticator.hasRole(Papeis.OCULTAR_AGRUPADOR_PETICOES_AVULSAS));
	}
	
	/**
	 * Método responsável por realizar a pesquisa dos processos de cada agrupador.
	 */
	public void pesquisar() {
		localizacaoServidor = Authenticator.getUsuarioLocalizacaoMagistradoServidorAtual();		
		existeDefinicaoVisibilidadePorCargo = ComponentUtil.getComponent(
				UsuarioLocalizacaoVisibilidadeManager.class).temOrgaoVisivel(localizacaoServidor);
		
		aplicarFiltroCargoVisibilidade();

		pesquisarProcessoPedidoSigiloDocumentoNaoApreciado();
		pesquisarProcessoPeticoesAvulsasNaoLidas();
		pesquisarProcessosHabilitacaoAutosNaoLidos();
		pesquisarProcessoDocumentoNaoLido();
		pesquisarProcessoMandadoDevolvido();
		pesquisarProcessoPedidoLiminarAntecipacaoTutelaNaoApreciado();
		pesquisarProcessoPedidoSegredoNaoApreciado();
		pesquisarProcessoPedidoAssistenciaJudiciariaGratuitaNaoApreciado();
		pesquisarProcessoAnalisePrevencao();
		pesquisarProcessoAguardandoRevisao();
		pesquisarProcessoRevisado();
		pesquisarProcessoDevolvidoRevisao();
	}
	
	private void aplicarFiltroCargoVisibilidade() {			
		if(existeDefinicaoVisibilidadePorCargo){
			HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_ORGAO_JULGADOR_CARGO, 
					"idUsuarioLocalizacao", localizacaoServidor.getIdUsuarioLocalizacaoMagistradoServidor());
			
			HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_ORGAO_JULGADOR_CARGO, 
					"dataAtual", DateUtils.truncate(new Date(), Calendar.DATE));
		}
	}
	
	private void aplicarCriteriaCargoVisibilidade(List<Criteria> criterias, String campoOrgaoJulgadorCargo) {
		if(existeDefinicaoVisibilidadePorCargo){
			String dataAtual = new String("'" + new Timestamp(DateUtils.truncate(new Date(), Calendar.DATE).getTime()).toString() + "'");
			criterias.add(Criteria.and(
					Criteria.exists(" select 1 from UsuarioLocalizacaoVisibilidade ulv " +
						" where ulv.usuarioLocalizacaoMagistradoServidor = " + localizacaoServidor.getIdUsuarioLocalizacaoMagistradoServidor() +
						" and ulv.orgaoJulgadorCargo = " + campoOrgaoJulgadorCargo +
						" and ulv.dtInicio <= " + dataAtual +
						" and ( ulv.dtFinal is null or (ulv.dtFinal >= " + dataAtual + "))"
					)
			));
		}		
	}
	
	/**
	 * Método responsável por inicializar os campos de pesquisa.
	 */
	public void limpar() {
		this.numeroProcesso = null;
		this.classeJudicial = null;
		this.assunto = null;
		this.orgaoJulgador = null;
		this.nomeParte = null;
		this.ufOab = null;
		this.numeroOab = null;
		this.letraOab = null;
		this.processoComParteSemCPFCNPJ = Boolean.FALSE;
		this.filtroTempoAgrupadores = FiltroTempoAgrupadoresEnum.ULTIMOS_15_DIAS;
		this.limparNumeroIdentificacao();
	}
	
	/**
	 * Método responsável por inicializar os campos de pesquisa CPF e CNPJ.
	 */
	public void limparNumeroIdentificacao() {
		this.numeroCPF = null;
		this.numeroCNPJ = null;
	}
	
	/**
	 * Método responsável por pesquisar os processos com pedido de segredo não apreciado.
	 */
	public void pesquisarProcessoPedidoSegredoNaoApreciado() {
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		criterias.add(Criteria.equals("processoStatus", ProcessoStatusEnum.D));
		criterias.add(Criteria.equals("apreciadoSegredo", ProcessoTrfApreciadoEnum.A));
		
		aplicarCriteriaFiltroTempoAgrupadores(criterias, "dataAutuacao");
		aplicarCriteriaRestricaoOJRelatoria(criterias, "");
		aplicarCriteriaVisibilidadeSigiloProcesso(criterias, "");
		
		DataRetriever<ProcessoTrf> retriever = new ProcessoTrfRetriever(
				ComponentUtil.getComponent(ProcessoJudicialManager.class), this.facesMessages, criterias);
		
		this.processoPedidoSegredoNaoApreciado = new EntityDataModel<ProcessoTrf>(
				ProcessoTrf.class, this.facesContext, retriever);
	}
	
	/**
	 * Método responsável por pesquisar os processos com pedido de sigilo nos documentos não apreciado.
	 */
	public void pesquisarProcessoPedidoSigiloDocumentoNaoApreciado() {
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		criterias.add(Criteria.in("papel", ComponentUtil.getComponent(PapelManager.class).getPapeisParaDocumentosNaoLidos().toArray()));

		criterias.add(Criteria.equals("ativo", Boolean.TRUE));
		criterias.add(Criteria.isNull("documentoPrincipal"));
		criterias.add(Criteria.not(Criteria.isNull("dataJuntada")));
		criterias.add(Criteria.equals("processoTrf.processoStatus", ProcessoStatusEnum.D));
		criterias.add(Criteria.equals("documentoSigiloso", Boolean.TRUE));
		criterias.add(Criteria.equals("lido", Boolean.FALSE));
		
		aplicarCriteriaFiltroTempoAgrupadores(criterias, "dataJuntada");
		aplicarCriteriaRestricaoOJRelatoria(criterias, "processoTrf");
		aplicarCriteriaVisibilidadeSigiloDocumentos(criterias, "");
		
		DataRetriever<ProcessoDocumento> retriever = new ProcessoDocumentoRetriever(
				ComponentUtil.getComponent(ProcessoDocumentoManager.class), this.facesMessages, criterias, "o.dataJuntada", Order.ASC);
		
		this.processoPedidoSigiloDocumentoNaoApreciado = new EntityDataModel<ProcessoDocumento>(
				ProcessoDocumento.class, this.facesContext, retriever);

	}
	
	/**
	 * Método responsável por pesquisar os processos com pedido de assistencia judiciária gratuita não apreciado.
	 */
	public void pesquisarProcessoPedidoAssistenciaJudiciariaGratuitaNaoApreciado() {
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		criterias.add(Criteria.equals("processoStatus", ProcessoStatusEnum.D));
		criterias.add(Criteria.equals("justicaGratuita", Boolean.TRUE));
		criterias.add(Criteria.equals("apreciadoJusticaGratuita", Boolean.FALSE));

		aplicarCriteriaFiltroTempoAgrupadores(criterias, "dataAutuacao");
		aplicarCriteriaRestricaoOJRelatoria(criterias, "");
		aplicarCriteriaVisibilidadeSigiloProcesso(criterias, "");
		
		DataRetriever<ProcessoTrf> retriever = new ProcessoTrfRetriever(
				ComponentUtil.getComponent(ProcessoJudicialManager.class), this.facesMessages, criterias);
		
		this.processoPedidoAssistenciaJudiciariaGratuitaNaoApreciado = new EntityDataModel<ProcessoTrf>(
				ProcessoTrf.class,	this.facesContext, retriever);
	}
	
	/**
	 * Método responsável por pesquisar os processos com pedido de liminar ou de antecipação de tutela não apreciado.
	 */
	public void pesquisarProcessoPedidoLiminarAntecipacaoTutelaNaoApreciado() {
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		criterias.add(Criteria.equals("processoStatus", ProcessoStatusEnum.D));
		criterias.add(Criteria.equals("tutelaLiminar", Boolean.TRUE));
		criterias.add(Criteria.or(Criteria.equals("apreciadoTutelaLiminar", Boolean.FALSE), Criteria.isNull("apreciadoTutelaLiminar")));

		aplicarCriteriaFiltroTempoAgrupadores(criterias, "dataAutuacao");
		aplicarCriteriaRestricaoOJRelatoria(criterias, "");
		aplicarCriteriaVisibilidadeSigiloProcesso(criterias, "");
		
		DataRetriever<ProcessoTrf> retriever = new ProcessoTrfRetriever(
				ComponentUtil.getComponent(ProcessoJudicialManager.class), this.facesMessages, criterias);
		
		this.processoPedidoLiminarAntecipacaoTutelaNaoApreciado = new EntityDataModel<ProcessoTrf>(
				ProcessoTrf.class, this.facesContext, retriever);
	}
	
	/**
	 * Método responsável por pesquisar os processos com pedido de habilitação nos autos não apreciado.
	 */
	public void pesquisarProcessosHabilitacaoAutosNaoLidos() {
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		criterias.add(Criteria.equals("retirado", Boolean.FALSE));
		criterias.add(Criteria.equals("habilitacaoAutos.situacaoHabilitacao", SituacaoHabilitacaoEnum.A));
		
		aplicarCriteriaFiltroProcessoDocumentoNaoLido(criterias);
		aplicarCriteriaFiltroTempoAgrupadores(criterias, "processoDocumento.dataJuntada");
		aplicarCriteriaRestricaoOJRelatoria(criterias, "processoDocumento.processoTrf");
		aplicarCriteriaVisibilidadeSigiloDocumentos(criterias, "processoDocumento");
						
		DataRetriever<ProcessoDocumentoPeticaoNaoLida> retriever = new ProcessoDocumentoPeticaoNaoLidaRetriever(
			ComponentUtil.getComponent(ProcessoDocumentoPeticaoNaoLidaManager.class), this.facesMessages, 
				criterias, "o.processoDocumento.dataJuntada", Order.ASC);
		
		this.processoHabilitacaoAutosNaoLidos = new EntityDataModel<ProcessoDocumentoPeticaoNaoLida>(
				ProcessoDocumentoPeticaoNaoLida.class,this.facesContext, retriever);
	}
	
	/**
	 * Método responsável por pesquisar os processos sob análise de prevenção.
	 */
	public void pesquisarProcessoAnalisePrevencao() {
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		criterias.add(Criteria.isNull("processoTrfConexaoList.dtValidaPrevencao"));
		criterias.add(Criteria.equals("processoTrfConexaoList.tipoConexao", TipoConexaoEnum.PR));

		aplicarCriteriaFiltroTempoAgrupadores(criterias, "dataAutuacao");
		aplicarCriteriaRestricaoOJRelatoria(criterias, "");
		aplicarCriteriaVisibilidadeSigiloProcesso(criterias, "");

		DataRetriever<ProcessoTrf> retriever = new ProcessoTrfRetriever(
				ComponentUtil.getComponent(ProcessoJudicialManager.class), this.facesMessages, criterias);
		
		this.processoAnalisePrevencao = new EntityDataModel<ProcessoTrf>(
				ProcessoTrf.class, this.facesContext, retriever);
	}
	
	/**
	 * Método responsável por pesquisar os processos com documentos não lidos
	 * Elimina documentos incluídos antes da autuação, não adianta colocar validação para documentos juntados antes da autuacao,
	 * pois quando o processo é protocolado o sistema seta explicitamente a data de juntada dos documentos para um horário posterior
	 * à data de autuação, assim, verificar a data de juntada do documento como sendo superior à autuação não é util e prejudica o desempenho.
	 */
	public void pesquisarProcessoDocumentoNaoLido() {
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		criterias.add(Criteria.equals("lido", Boolean.FALSE));
		criterias.add(Criteria.in("papel", ComponentUtil.getComponent(PapelManager.class).getPapeisParaDocumentosNaoLidos().toArray()));

		criterias.add(Criteria.equals("ativo", Boolean.TRUE));
		criterias.add(Criteria.isNull("documentoPrincipal"));
		criterias.add(Criteria.equals("processoTrf.processoStatus", ProcessoStatusEnum.D));

		criterias.add(Criteria.greater(Criteria.path("dataInclusao"), Criteria.path("processoTrf.dataAutuacao")));
		criterias.add(Criteria.not(Criteria.isNull(DATA_JUNTADA)));
		
		aplicarCriteriaFiltroTempoAgrupadores(criterias, "dataJuntada");
		aplicarCriteriaRestricaoOJRelatoria(criterias, "processoTrf");
		aplicarCriteriaVisibilidadeSigiloDocumentos(criterias, "");

		
		DataRetriever<ProcessoDocumento> retriever = new ProcessoDocumentoRetriever(
				ComponentUtil.getComponent(ProcessoDocumentoManager.class), this.facesMessages, criterias, "to_char(o.dataJuntada, 'yyyy-mm-dd')", Order.ASC);
		
		this.processoDocumentoNaoLido = new EntityDataModel<ProcessoDocumento>(
				ProcessoDocumento.class, this.facesContext, retriever);
	}
	
	/**
	 * Método responsável por pesquisar os processos com mandados devolvidos.
	 */
	public void pesquisarProcessoMandadoDevolvido() {
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		if(ParametroUtil.instance().getTipoResultadoDiligenciaRedistribuicao() == null) {
			FacesMessages.instance().add(Severity.INFO, 
					"O parâmetro 'idTipoResultadoDiligenciaRedistribuicao' não foi cadastrado ou "
							+ "está configurado com um valor sem correspondência na tabela de tipos de resultado de diligência.");
			
		}else {
			criterias.add(Criteria.notEquals("diligenciaList.tipoResultadoDiligencia.idTipoResultadoDiligencia", 
					ParametroUtil.instance().getTipoResultadoDiligenciaRedistribuicao().getIdTipoResultadoDiligencia()));
		}
		
		criterias.add(Criteria.equals("processoExpediente.processoTrf.mandadoDevolvido", Boolean.TRUE));

		aplicarCriteriaFiltroTempoAgrupadores(criterias, "dtDistribuicaoExpediente");
		aplicarCriteriaRestricaoOJRelatoria(criterias, "processoExpediente.processoTrf");
		aplicarCriteriaVisibilidadeSigiloProcesso(criterias, "processoExpediente.processoTrf");
				
		DataRetriever<ProcessoExpedienteCentralMandado> retriever = new ProcessoExpedienteCentralMandadoRetriever(
			ComponentUtil.getComponent(ProcessoExpedienteCentralMandadoManager.class), this.facesMessages, 
				criterias, "o.dtDistribuicaoExpediente", Order.ASC);
		
		this.processoMandadoDevolvido = new EntityDataModel<ProcessoExpedienteCentralMandado>(
				ProcessoExpedienteCentralMandado.class, this.facesContext, retriever);
	}
	
	/**
	 * Método responsável por pesquisar os processos aguardando revisão.
	 */
	public void pesquisarProcessoAguardandoRevisao() {
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		criterias.add(Criteria.equals("processoStatus", ProcessoStatusEnum.D));
		criterias.add(Criteria.equals("tutelaLiminar", Boolean.TRUE));
		criterias.add(Criteria.or(Criteria.equals("apreciadoTutelaLiminar", Boolean.FALSE), Criteria.isNull("apreciadoTutelaLiminar")));

		aplicarCriteriaFiltroTempoAgrupadores(criterias, "dataAutuacao");
		aplicarCriteriaRestricaoOJRelatoria(criterias, "");

		DataRetriever<RevisorProcessoTrf> retriever = new RevisorProcessoTrfRetriever(
				ComponentUtil.getComponent(RevisorProcessoTrfManager.class), this.facesMessages, criterias);
	
		this.processoAguardandoRevisao = new EntityDataModel<RevisorProcessoTrf>(
				RevisorProcessoTrf.class, this.facesContext, retriever);
	}
	
	/**
	 * Método responsável por pesquisar os processos revisados.
	 */
	public void pesquisarProcessoRevisado() {
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		criterias.add(Criteria.equals("processoStatus", ProcessoStatusEnum.D));
		criterias.add(Criteria.equals("tutelaLiminar", Boolean.TRUE));
		criterias.add(Criteria.or(Criteria.equals("apreciadoTutelaLiminar", Boolean.FALSE), Criteria.isNull("apreciadoTutelaLiminar")));

		aplicarCriteriaFiltroTempoAgrupadores(criterias, "dataAutuacao");
		aplicarCriteriaRestricaoOJRelatoria(criterias, "");

		DataRetriever<RevisorProcessoTrf> retriever = new RevisorProcessoTrfRetriever(
				ComponentUtil.getComponent(RevisorProcessoTrfManager.class), this.facesMessages, criterias);
		
		this.processoRevisado = new EntityDataModel<RevisorProcessoTrf>(
				RevisorProcessoTrf.class, this.facesContext, retriever);
	}
	
	/**
	 * Método responsável por pesquisar os processos devolvidos pelo revisor.
	 */
	public void pesquisarProcessoDevolvidoRevisao() {
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		criterias.add(Criteria.equals("processoStatus", ProcessoStatusEnum.D));
		criterias.add(Criteria.equals("tutelaLiminar", Boolean.TRUE));
		criterias.add(Criteria.or(Criteria.equals("apreciadoTutelaLiminar", Boolean.FALSE), Criteria.isNull("apreciadoTutelaLiminar")));

		aplicarCriteriaFiltroTempoAgrupadores(criterias, "dataAutuacao");
		aplicarCriteriaRestricaoOJRelatoria(criterias, "");

		DataRetriever<RevisorProcessoTrfDevolvido> retriever = new RevisorProcessoTrfDevolvidoRetriever(
				ComponentUtil.getComponent(RevisorProcessoTrfDevolvidoManager.class), this.facesMessages, criterias);
		
		this.processoDevolvidoRevisao = new EntityDataModel<RevisorProcessoTrfDevolvido>(
				RevisorProcessoTrfDevolvido.class, this.facesContext, retriever);
	}
	
	/**
	 * Método responsável por pesquisar os processos com petição avulsa não lida.
	 * 
	 * Critérios:
	 * 		Documentos não retirados do agrupador;
	 * 		Apenas de usuarios não habilitados nos autos;
	 * 		Documentos cuja relatoria seja do OJ ou do OJC do usuário atual;
	 * 		Documentos que o usuário atual tenha permissão de sigilo.
	 */
	public void pesquisarProcessoPeticoesAvulsasNaoLidas() {
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		criterias.add(Criteria.equals("retirado", Boolean.FALSE));
		
		Criteria habilitacaoAutosAtiva = Criteria.notEquals("habilitacaoAutos.situacaoHabilitacao", SituacaoHabilitacaoEnum.A);
		habilitacaoAutosAtiva.setRequired("habilitacaoAutos", false);
		
		criterias.add(Criteria.or(Criteria.isNull("habilitacaoAutos"), habilitacaoAutosAtiva));
		criterias.add(Criteria.equals("processoDocumento.ativo", Boolean.TRUE));
		
		aplicarCriteriaFiltroTempoAgrupadores(criterias, "processoDocumento.dataJuntada");
		aplicarCriteriaRestricaoOJRelatoria(criterias, "processoDocumento.processoTrf");
		aplicarCriteriaVisibilidadeSigiloDocumentos(criterias, "processoDocumento");
		aplicarCriteriaFiltroProcessoDocumentoNaoLido(criterias);
		
		DataRetriever<ProcessoDocumentoPeticaoNaoLida> dataRetriever = new ProcessoDocumentoPeticaoNaoLidaRetriever(
			ComponentUtil.getComponent(ProcessoDocumentoPeticaoNaoLidaManager.class), this.facesMessages, 
				criterias, "o.processoDocumento.dataJuntada", Order.ASC);
		
		this.processoPeticoesAvulsasNaoLidas = new EntityDataModel<ProcessoDocumentoPeticaoNaoLida>(
				ProcessoDocumentoPeticaoNaoLida.class,this.facesContext, dataRetriever);
	}

	/**
	 * Identifica os critérios de visibilidade dos documentos nos agrupadores do usuário interno.
	 * 
	 * @param criterias
	 * @param colunaProcessoDocumento
	 */
	private void aplicarCriteriaVisibilidadeSigiloDocumentos(List<Criteria> criterias, String colunaProcessoDocumento) {
		if (!Authenticator.isVisualizaSigiloso()) {
			if (!colunaProcessoDocumento.isEmpty()) {
				colunaProcessoDocumento = colunaProcessoDocumento + ".";
			}
			this.aplicarCriteriaVisibilidadeSigiloProcesso(criterias, colunaProcessoDocumento + "processoTrf");
	
			Criteria documentoPublico = Criteria.equals(colunaProcessoDocumento + "documentoSigiloso", false);
			Criteria usuarioListaVisualizadoresDocumento = Criteria.exists(
				"select 1 from ProcessoDocumentoVisibilidadeSegredo pdvs where pdvs.processoDocumento.idProcessoDocumento = o." +
					colunaProcessoDocumento + "idProcessoDocumento and pdvs.pessoa.idPessoa = " + Authenticator.getIdUsuarioLogado());
			
			Criteria usuarioJuntouDocumento = Criteria.equals(
					colunaProcessoDocumento + "usuarioJuntada.idUsuario", Authenticator.getIdUsuarioLogado());
		
			criterias.add(Criteria.or(documentoPublico, usuarioListaVisualizadoresDocumento, usuarioJuntouDocumento));
		}
	}
	
	private void aplicarCriteriaVisibilidadeSigiloProcesso(List<Criteria> criterias, String colunaProcessoTrf) {
		if (!Authenticator.isVisualizaSigiloso()) {
			if (!colunaProcessoTrf.isEmpty()) {
				colunaProcessoTrf = colunaProcessoTrf + ".";
			}
			Criteria processosPublicos = Criteria.equals(colunaProcessoTrf + "segredoJustica", false);
			Criteria usuarioListaVisualizadoresProcesso = Criteria.exists(
				"select 1 from ProcessoVisibilidadeSegredo pvs where pvs.idPessoa = " + 
					Authenticator.getIdUsuarioLogado() + " and pvs.processo.idProcesso = o." + colunaProcessoTrf + "idProcessoTrf");
			
			criterias.add(Criteria.or(processosPublicos, usuarioListaVisualizadoresProcesso));
		}
	}
	
	/**
	 * Verifica se há um critério de restrição de filtro de tempo no agrupador
	 * 
	 * @param criterias
	 * @param colunaData
	 */
	private void aplicarCriteriaFiltroTempoAgrupadores(ArrayList<Criteria> criterias, String colunaData) {
		if (this.getFiltroTempoAgrupadores() != null && !colunaData.trim().isEmpty()) {
			Date dataReferenciaFiltro = this.getFiltroTempoAgrupadores().getDataReferenciaFiltroTempo();
			if (dataReferenciaFiltro != null) {
				criterias.add(Criteria.greaterOrEquals(colunaData.trim(), dataReferenciaFiltro));
			}
		}
	}
	
	/**
	 * Aplica filtros Criteria 'and processoDocumento.lido = false' 
	 * e 'and processoDocumento.papel in (1005, 5200, 1655)'
	 * e 'and processoDocumento.documentoPrincipal is null'
	 * 
	 * @param criterias
	 */
	private void aplicarCriteriaFiltroProcessoDocumentoNaoLido(ArrayList<Criteria> criterias) {
		criterias.add(Criteria.equals("processoDocumento.lido", Boolean.FALSE));
		criterias.add(Criteria.in("processoDocumento.papel", ComponentUtil.getComponent(PapelManager.class).getPapeisParaDocumentosNaoLidos().toArray()));
		criterias.add(Criteria.isNull("processoDocumento.documentoPrincipal"));
	}

	/**
	 * Identifica os critérios para o usuário poder visualizar processos de relatoria do seu OJ ou seu OJC
	 * 
	 * @param criterias
	 * @param tabelaProcessoTrf
	 */
	private void aplicarCriteriaRestricaoOJRelatoria(ArrayList<Criteria> criterias, String tabelaProcessoTrf) {
		if (!tabelaProcessoTrf.isEmpty()) {
			tabelaProcessoTrf = tabelaProcessoTrf + ".";
		}
		ArrayList<Criteria> criteriasLocalizacao = new ArrayList<Criteria>();
		
		if(!Authenticator.isServidorExclusivoColegiado()) {
			List<Integer> idsLocalizacoesFisicas = Authenticator.getIdsLocalizacoesFilhasAtuaisList();
			if(CollectionUtilsPje.isNotEmpty(idsLocalizacoesFisicas)) {
				criteriasLocalizacao.add(Criteria.in(tabelaProcessoTrf+"orgaoJulgador.localizacao.idLocalizacao", idsLocalizacoesFisicas.toArray()));
			}			
		}

		OrgaoJulgadorColegiado orgaoJulgadorColegiado = Authenticator.getOrgaoJulgadorColegiadoAtual();
		if (orgaoJulgadorColegiado != null) {
			criteriasLocalizacao.add(Criteria.equals(
				tabelaProcessoTrf + "orgaoJulgadorColegiado.idOrgaoJulgadorColegiado", orgaoJulgadorColegiado.getIdOrgaoJulgadorColegiado()));
		}
		criterias.add(Criteria.and(criteriasLocalizacao.toArray(new Criteria[criteriasLocalizacao.size()])));
		aplicarCriteriaCargoVisibilidade(criterias, "o." + tabelaProcessoTrf + "orgaoJulgadorCargo");
	}
	
	/**
	 * Método responsável por aplicar os filtros de pesquisa ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @param prefixo Caminho para se chegar até um atributo do tipo {@link ProcessoTrf}.
	 * @throws NoSuchFieldException Caso algum campo declarado não esteja mapeado.
	 */
	private void aplicarFiltros(Search search, String prefixo) throws NoSuchFieldException {
		aplicarFiltroProcesso(search, prefixo);
		aplicarFiltroClasseJudicial(search, prefixo);
		aplicarFiltroAssunto(search, prefixo);
		aplicarFiltroOrgaoJulgador(search, prefixo);
		aplicarFiltroNumeroIdentificacao(search, prefixo);
		aplicarFiltroNomeParte(search, prefixo);
		aplicarFiltroOAB(search, prefixo);
		aplicarFiltroProcessoParteSemNumeroIdentificacao(search, prefixo);
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Processo" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @param prefixo Caminho para se chegar até um atributo do tipo {@link ProcessoTrf}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroProcesso(Search search, String prefixo) throws NoSuchFieldException {
		if (StringUtils.isNotBlank(this.numeroProcesso)) {
			search.addCriteria(Criteria.contains(prefixo + "processo.numeroProcesso", this.numeroProcesso));
		}
	}	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Classe judicial" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @param prefixo Caminho para se chegar até um atributo do tipo {@link ProcessoTrf}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroClasseJudicial(Search search, String prefixo) throws NoSuchFieldException {
		if (this.classeJudicial != null) {
			search.addCriteria(Criteria.equals(prefixo + "classeJudicial", this.classeJudicial));
		}
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Assunto" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @param prefixo Caminho para se chegar até um atributo do tipo {@link ProcessoTrf}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroAssunto(Search search, String prefixo) throws NoSuchFieldException {
		if (this.assunto != null) {
			search.addCriteria(Criteria.equals(prefixo + "assuntoTrfList.idAssuntoTrf", this.assunto.getIdAssuntoTrf()));
		}
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Órgão julgador" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @param prefixo Caminho para se chegar até um atributo do tipo {@link ProcessoTrf}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroOrgaoJulgador(Search search, String prefixo) throws NoSuchFieldException {
		if( this.orgaoJulgador != null) {
			search.addCriteria(Criteria.equals(prefixo + "orgaoJulgador", this.orgaoJulgador));
		}
	}	

	/**
	 * Método responsável por aplicar o filtro de pesquisa "CPF ou CNPJ" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @param prefixo Caminho para se chegar até um atributo do tipo {@link ProcessoTrf}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroNumeroIdentificacao(Search search, String prefixo) throws NoSuchFieldException {
		String numeroIdentificacao = StringUtils.isNotBlank(this.numeroCPF) ? 
				this.numeroCPF : StringUtils.isNotBlank(this.numeroCNPJ) ? this.numeroCNPJ : null;
				
		if (InscricaoMFUtil.validarCpfCnpj(numeroIdentificacao)) {
			search.addCriteria(Criteria.equals(
					prefixo + "processoParteList.inSituacao", ProcessoParteSituacaoEnum.A));
			
			search.addCriteria(Criteria.equals(
				prefixo + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.numeroDocumento", numeroIdentificacao));
		}
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Nome da parte" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @param prefixo Caminho para se chegar até um atributo do tipo {@link ProcessoTrf}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroNomeParte(Search search, String prefixo) throws NoSuchFieldException {
		if (StringUtils.isNotBlank(this.nomeParte)) {
			search.addCriteria(Criteria.equals(prefixo + "processoParteList.inSituacao", ProcessoParteSituacaoEnum.A));
			search.addCriteria(Criteria.contains(prefixo + "processoParteList.pessoa.nome", this.nomeParte));
		}
	}

	/**
	 * Método responsável por aplicar o filtro de pesquisa "OAB" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @param prefixo Caminho para se chegar até um atributo do tipo {@link ProcessoTrf}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroOAB(Search search, String prefixo) throws NoSuchFieldException {
		try {
			if (this.ufOab != null && StringUtils.isNotBlank(this.numeroOab)) {
				PessoaAdvogado advogado = ComponentUtil.getComponent(
						PessoaAdvogadoManager.class).recuperarAdvogado(this.ufOab, this.numeroOab, this.letraOab);

				search.addCriteria(Criteria.equals(prefixo + "processoParteList.inSituacao", ProcessoParteSituacaoEnum.A));
				search.addCriteria(Criteria.equals(prefixo + "processoParteList.pessoa.idPessoa", advogado != null ? advogado.getIdUsuario() : -1));
			}
		} catch (PJeBusinessException ex) {
			this.logger.error(ex.getMessage());
		}
	}
	
	/**
	 * Método responsável por aplicar o filtro de pesquisa "Processo com parte sem CPF/CNPJ" ao objeto {@link Search}.
	 * 
	 * @param search {@link Search}.
	 * @param prefixo Caminho para se chegar até um atributo do tipo {@link ProcessoTrf}.
	 * @throws NoSuchFieldException Caso o campo declarado não esteja mapeado.
	 */
	private void aplicarFiltroProcessoParteSemNumeroIdentificacao(Search search, String prefixo) throws NoSuchFieldException {
		if (this.processoComParteSemCPFCNPJ == true) {
			search.addCriteria(Criteria.equals(prefixo + "processoParteList.inSituacao", ProcessoParteSituacaoEnum.A));		
			
			search.addCriteria(Criteria.notEquals(
				prefixo + "processoParteList.pessoa.tipoPessoa.idTipoPessoa", ParametroUtil.instance().getIdTipoPessoaAutoridade()));
			
			search.addCriteria(Criteria.notExists("select 1 from ProcessoParte pp, PessoaDocumentoIdentificacao pdi"
				+ " where pp.processoTrf.idProcessoTrf = o." + prefixo + "idProcessoTrf"
				+ " and (pdi.tipoDocumento.codTipo = 'CPJ' or pdi.tipoDocumento.codTipo = 'CPF')"
				+ " and pdi.ativo = true and pdi.pessoa.idPessoa = pp.pessoa.idPessoa"));
		}
	}
	
	/**
	 * Método responsável por verificar se algum componente checkbox da lista está selecionado.
	 * 
	 * @return Verdadeiro se algum componente checkbox da lista está selecionado. Falso, caso contrário.
	 */
	public boolean verificarCheck(Map<Object, Boolean> map) {
		for (Map.Entry<Object, Boolean> entry : map.entrySet()) {
			if (entry.getValue() == true) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Método responsável por selecionar (ou retirar a seleção) de todos os componentes checkbox da lista.
	 * 
	 * @param entityDataModel Componente de paginação de dados.
	 * @param map Variável que armazena o status dos componentes checkbox da lista.
	 * @param status Variável que indica qual será o status dos componentes checkbox da lista.
	 */
	public <T> void selecionarTodosCheck(EntityDataModel<T> entityDataModel, Map<T, Boolean> map, boolean status) {
		List<T> list = entityDataModel.getPage();
		for (T element : list) {
			map.put(element, status);
		}
	}

	/**
	 * Método responsável por retirar de destaque os processos selecionados da lista de
	 * processos com habilitação nos autos não lidos.
	 */
	public void retirarDestaqueProcessoHabilitacaoAutosNaoLidos() {
		if (this.processoHabilitacaoAutosNaoLidosCheck.containsValue(Boolean.TRUE)) {
			ProcessoDocumentoPeticaoNaoLidaManager processoDocumentoPeticaoNaoLidaManager = ComponentUtil.getComponent(
					ProcessoDocumentoPeticaoNaoLidaManager.class);
			
			for (Map.Entry<ProcessoDocumentoPeticaoNaoLida, Boolean> entry : this.processoHabilitacaoAutosNaoLidosCheck.entrySet()) {
				if (entry.getValue() == true) {
					processoDocumentoPeticaoNaoLidaManager.retirarDestaque(entry.getKey());
				}
			}
			pesquisarProcessosHabilitacaoAutosNaoLidos();
			this.processoHabilitacaoAutosNaoLidosCheck = new LinkedHashMap<ProcessoDocumentoPeticaoNaoLida, Boolean>(0);
		}
	}	
	
	/**
	 * Método responsável por retirar de destaque os processos selecionados da lista de 
	 * processos com pedido de liminar ou de antecipação de tutela não apreciado.
	 */
	public void retirarDestaqueProcessoPedidoLiminarAntecipacaoTutelaNaoApreciado() {
		if (this.processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheck.containsValue(Boolean.TRUE)) {
			for (Map.Entry<ProcessoTrf, Boolean> entry : this.processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheck.entrySet()) {
				if (entry.getValue() == true) {
					ProcessoTrf processoTrf = entry.getKey();
					processoTrf.setApreciadoTutelaLiminar(Boolean.TRUE);
					EntityUtil.getEntityManager().merge(processoTrf);
				}
			}
			EntityUtil.getEntityManager().flush();
			pesquisarProcessoPedidoLiminarAntecipacaoTutelaNaoApreciado();
			this.processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheck = new LinkedHashMap<ProcessoTrf, Boolean>(0);
		}
	}
	
	/**
	 * Método responsável por retirar de destaque os processos selecionados da lista de
	 * processos com mandados devolvidos.
	 */
	public void retirarDestaqueProcessoMandadoDevolvido() {
		if (this.processoMandadoDevolvidoCheck.containsValue(Boolean.TRUE)) {
			for (Map.Entry<ProcessoExpedienteCentralMandado, Boolean> entry : this.processoMandadoDevolvidoCheck.entrySet()) {
				if (entry.getValue() == true) {
					ProcessoTrf processoTrf = entry.getKey().getProcessoExpediente().getProcessoTrf();
					processoTrf.setMandadoDevolvido(Boolean.FALSE);
					EntityUtil.getEntityManager().merge(processoTrf);
				}
			}
			EntityUtil.getEntityManager().flush();
			pesquisarProcessoMandadoDevolvido();
			this.processoMandadoDevolvidoCheck = new LinkedHashMap<ProcessoExpedienteCentralMandado, Boolean>(0);
		}
	}
	
	/**
	 * Método responsável por retirar de destaque os processos selecionados da lista de
	 * processos com documentos não lidos.
	 */
	public void retirarDestaqueProcessoDocumentoNaoLido() {
		if (this.processoDocumentoNaoLidoCheck.containsValue(Boolean.TRUE)) {
			Pessoa pessoa = Authenticator.getPessoaLogada();
			Date dataLeitura = Calendar.getInstance().getTime();	
			ProcessoDocumentoTrfHome processoDocumentoTrfHome = ComponentUtil.getComponent(ProcessoDocumentoTrfHome.class);
			for (Map.Entry<ProcessoDocumento, Boolean> entry : this.processoDocumentoNaoLidoCheck.entrySet()) {
				if (entry.getValue() == true) {
					processoDocumentoTrfHome.gravarAlteracoesLeituraDocumento(entry.getKey(), pessoa, dataLeitura);
				}
			}
			EntityUtil.getEntityManager().flush();
			pesquisarProcessoDocumentoNaoLido();
			this.processoDocumentoNaoLidoCheck = new LinkedHashMap<ProcessoDocumento, Boolean>(0);
		}
	}
	
	/**
	 * Método responsável por retirar de destaque os processos selecionados da lista de processos 
	 * com pedido de assistência judiciária gratuita não apreciado.
	 */
	public void retirarDestaqueProcessoPedidoAssistenciaJudiciariaGratuitaNaoApreciado() {
		if (this.processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheck.containsValue(Boolean.TRUE)) {
			for (Map.Entry<ProcessoTrf, Boolean> entry : this.processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheck.entrySet()) {
				if (entry.getValue() == true) {
					ProcessoTrf processoTrf = entry.getKey();
					processoTrf.setApreciadoJusticaGratuita(Boolean.TRUE);
					EntityUtil.getEntityManager().merge(processoTrf);
				}
			}
			EntityUtil.getEntityManager().flush();
			pesquisarProcessoPedidoAssistenciaJudiciariaGratuitaNaoApreciado();
			this.processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheck = new LinkedHashMap<ProcessoTrf, Boolean>(0);
		}
	}
	
	/**
	 * Método responsável por retirar de destaque os documentos selecionados da lista de documentos com segredo não apreciado.
	 */
	public void retirarDestaqueProcessoPedidoSigiloDocumentoNaoApreciado(){
		if (this.processoPedidoSigiloDocumentoNaoApreciadoCheck.containsValue(Boolean.TRUE)) {
			Pessoa pessoa = Authenticator.getPessoaLogada();
			Date dataLeitura = Calendar.getInstance().getTime();	
			ProcessoDocumentoTrfHome processoDocumentoTrfHome = ComponentUtil.getComponent(ProcessoDocumentoTrfHome.class);
			for (Map.Entry<ProcessoDocumento, Boolean> entry : this.processoPedidoSigiloDocumentoNaoApreciadoCheck.entrySet()) {
				if (entry.getValue() == true) {
					processoDocumentoTrfHome.gravarAlteracoesLeituraDocumento(entry.getKey(), pessoa, dataLeitura);
				}
			}
			EntityUtil.getEntityManager().flush();
			pesquisarProcessoPedidoSigiloDocumentoNaoApreciado();
			this.processoPedidoSigiloDocumentoNaoApreciadoCheck = new LinkedHashMap<ProcessoDocumento, Boolean>(0);
		}
	}
	
	/**
	 * Método responsável por retirar de destaque os processos selecionados da lista de processos com pedido de segredo não apreciado.
	 */
	public void retirarDestaqueProcessoPedidoSegredoNaoApreciado(){
		if (this.processoPedidoSegredoNaoApreciadoCheck.containsValue(Boolean.TRUE)) {
			for (Map.Entry<ProcessoTrf, Boolean> entry : this.processoPedidoSegredoNaoApreciadoCheck.entrySet()) {
				if (entry.getValue() == true) {
					ProcessoTrf processoTrf = entry.getKey();
					processoTrf.setApreciadoSegredo(ProcessoTrfApreciadoEnum.S);
					EntityUtil.getEntityManager().merge(processoTrf);
				}
			}
			EntityUtil.getEntityManager().flush();
			pesquisarProcessoPedidoSegredoNaoApreciado();
			this.processoPedidoSegredoNaoApreciadoCheck = new LinkedHashMap<ProcessoTrf, Boolean>(0);
		}
	}
	
	/**
	 * Método responsável por retirar de destaque os processos selecionados da lista de processos com petições avulsas não lidas.
	 */
	public void retirarDestaqueProcessoPeticoesAvulsasNaoLidas() {
		if (this.processoPeticoesAvulsasNaoLidasCheck.containsValue(Boolean.TRUE)) {
			ProcessoDocumentoPeticaoNaoLidaManager processoDocumentoPeticaoNaoLidaManager = ComponentUtil.getComponent(
					ProcessoDocumentoPeticaoNaoLidaManager.class);
			
			for (Map.Entry<ProcessoDocumentoPeticaoNaoLida, Boolean> entry : this.processoPeticoesAvulsasNaoLidasCheck.entrySet()) {
				if (entry.getValue() == true) {
					processoDocumentoPeticaoNaoLidaManager.retirarDestaque(entry.getKey());
				}
			}
			pesquisarProcessoPeticoesAvulsasNaoLidas();
			this.processoPeticoesAvulsasNaoLidasCheck = new LinkedHashMap<ProcessoDocumentoPeticaoNaoLida, Boolean>(0);
		}
	}
	
	/**
	 * Classe interna responsável pela pesquisa de processos.
	 */
	private class ProcessoTrfRetriever implements DataRetriever<ProcessoTrf>{
		private Long count;
		private FacesMessages facesMessages;		
		private ProcessoJudicialManager processoJudicialManager;
		private List<Criteria> criterias;
		
		public ProcessoTrfRetriever(ProcessoJudicialManager processoJudicialManager, FacesMessages facesMessages, List<Criteria> criterias){
			this.processoJudicialManager = processoJudicialManager;
			this.facesMessages = facesMessages;
			this.criterias = criterias;
		}
		
		@Override
		public Object getId(ProcessoTrf obj) {
			return this.processoJudicialManager.getId(obj);
		}

		@Override
		public ProcessoTrf findById(Object id) throws Exception {
			return this.processoJudicialManager.findById(id);
		}

		@Override
		public List<ProcessoTrf> list(Search search) {
			List<ProcessoTrf> retorno = new ArrayList<ProcessoTrf>(0);
			try {
				atualizarDadosPesquisa(search, 10);
				retorno = this.processoJudicialManager.list(search);
			} catch (NoSuchFieldException nsfe){
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os registros.");
			}
			return retorno;
		}

		@Override
		public long count(Search search) {
			if(this.count == null){
				try {
					atualizarDadosPesquisa(search, 0);
					this.count = this.processoJudicialManager.count(search);
				} catch (Exception e) {
					this.facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar o número de registros.");
					return QTD_LISTA_VAZIA;
				}
			}
			return this.count;
		}
		
		private void atualizarDadosPesquisa(Search search, int qtdRegistros) throws NoSuchFieldException {
			aplicarFiltros(search, StringUtils.EMPTY);
			
			search.addCriteria(this.criterias);
			search.setDistinct(true);
			search.addOrder("o.dataAutuacao", Order.ASC);
			search.setMax(qtdRegistros);
		}
	}
	
	/**
	 * Classe interna responsável pela pesquisa de documentos do processo.
	 */
	private class ProcessoDocumentoRetriever implements DataRetriever<ProcessoDocumento> {
		private Long count;
		private FacesMessages facesMessages;		
		private ProcessoDocumentoManager processoDocumentoManager;
		private List<Criteria> criterias;
		private String attributeOrder;
		private Order order;
		
		public ProcessoDocumentoRetriever(ProcessoDocumentoManager processoDocumentoManager, 
				FacesMessages facesMessages, List<Criteria> criterias, String attributeOrder, Order order){
			
			this.processoDocumentoManager = processoDocumentoManager;
			this.facesMessages = facesMessages;
			this.criterias = criterias;
			this.attributeOrder = attributeOrder;
			this.order = order;
		}
		
		@Override
		public Object getId(ProcessoDocumento obj) {
			return this.processoDocumentoManager.getId(obj);
		}

		@Override
		public ProcessoDocumento findById(Object id) throws Exception {
			return this.processoDocumentoManager.findById(id);
		}

		@Override
		public List<ProcessoDocumento> list(Search search) {
			List<ProcessoDocumento> retorno = new ArrayList<ProcessoDocumento>(0);
			try {
				atualizarDadosPesquisa(search, 10);
				retorno = this.processoDocumentoManager.list(search);
			} catch (NoSuchFieldException nsfe){
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os registros.");
			}
			return retorno;
		}

		@Override
		public long count(Search search) {
			if(this.count == null){
				try {
					atualizarDadosPesquisa(search, 0);
					this.count = this.processoDocumentoManager.count(search);
				} catch (Exception e) {
					this.facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar o número de registros.");
					return QTD_LISTA_VAZIA;
				}
			}
			return this.count;
		}
		
		private void atualizarDadosPesquisa(Search search, int qtdRegistros) throws NoSuchFieldException {
			aplicarFiltros(search, "processoTrf.");
			
			search.addCriteria(this.criterias);
			search.addOrder(this.attributeOrder, this.order);
			search.setMax(qtdRegistros);
		}
	}
	
	/**
	 * Classe interna responsável pela pesquisa de expedientes.
	 */
	private class ProcessoExpedienteCentralMandadoRetriever implements DataRetriever<ProcessoExpedienteCentralMandado>{
		private Long count;
		private FacesMessages facesMessages;		
		private ProcessoExpedienteCentralMandadoManager processoExpedienteCentralMandadoManager;
		private List<Criteria> criterias;
		private String attributeOrder;
		private Order order;
		
		public ProcessoExpedienteCentralMandadoRetriever(ProcessoExpedienteCentralMandadoManager processoExpedienteCentralMandadoManager, 
				FacesMessages facesMessages, List<Criteria> criterias, String attributeOrder, Order order){
			
			this.processoExpedienteCentralMandadoManager = processoExpedienteCentralMandadoManager;
			this.facesMessages = facesMessages;
			this.criterias = criterias;
			this.attributeOrder = attributeOrder;
			this.order = order;
		}
		
		@Override
		public Object getId(ProcessoExpedienteCentralMandado obj) {
			return this.processoExpedienteCentralMandadoManager.getId(obj);
		}

		@Override
		public ProcessoExpedienteCentralMandado findById(Object id) throws Exception {
			return this.processoExpedienteCentralMandadoManager.findById(id);
		}

		@Override
		public List<ProcessoExpedienteCentralMandado> list(Search search) {
			try {
				atualizarDadosPesquisa(search, 10);
				return this.processoExpedienteCentralMandadoManager.list(search);
			} catch (NoSuchFieldException nsfe){
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os registros.");
			}
			return Collections.emptyList();
		}

		@Override
		public long count(Search search) {
			if(this.count == null){
				try {
					atualizarDadosPesquisa(search, 0);
					this.count = this.processoExpedienteCentralMandadoManager.count(search);
				} catch (Exception e) {
					this.facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar o número de registros.");
					return QTD_LISTA_VAZIA;
				}
			}
			return this.count;
		}
		
		private void atualizarDadosPesquisa(Search search, int qtdRegistros) throws NoSuchFieldException {
			aplicarFiltros(search, "processoExpediente.processoTrf.");
			
			search.addCriteria(this.criterias);
			search.setDistinct(true);
			search.addOrder(this.attributeOrder, this.order);
			search.setMax(qtdRegistros);
		}
	}

	/**
	 * Classe interna responsável pela pesquisa de documentos não lidos.
	 */
	private class ProcessoDocumentoPeticaoNaoLidaRetriever implements DataRetriever<ProcessoDocumentoPeticaoNaoLida>{
		private Long count;
		private FacesMessages facesMessages;		
		private ProcessoDocumentoPeticaoNaoLidaManager processoDocumentoPeticaoNaoLidaManager;
		private List<Criteria> criterias;
		private String attributeOrder;
		private Order order;
		
		public ProcessoDocumentoPeticaoNaoLidaRetriever(ProcessoDocumentoPeticaoNaoLidaManager processoDocumentoPeticaoNaoLidaManager, 
				FacesMessages facesMessages, List<Criteria> criterias, String attributeOrder, Order order){
			
			this.processoDocumentoPeticaoNaoLidaManager = processoDocumentoPeticaoNaoLidaManager;
			this.facesMessages = facesMessages;
			this.criterias = criterias;
			this.attributeOrder = attributeOrder;
			this.order = order;
		}
		
		@Override
		public Object getId(ProcessoDocumentoPeticaoNaoLida obj) {
			return this.processoDocumentoPeticaoNaoLidaManager.getId(obj);
		}

		@Override
		public ProcessoDocumentoPeticaoNaoLida findById(Object id) throws Exception {
			return this.processoDocumentoPeticaoNaoLidaManager.findById(id);
		}

		@Override
		public List<ProcessoDocumentoPeticaoNaoLida> list(Search search) {
			List<ProcessoDocumentoPeticaoNaoLida> retorno = new ArrayList<ProcessoDocumentoPeticaoNaoLida>(0);
			try {
				atualizarDadosPesquisa(search, 10);
				retorno = this.processoDocumentoPeticaoNaoLidaManager.list(search);
			} catch (NoSuchFieldException nsfe){
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os registros.");
			}
			return retorno;
		}

		@Override
		public long count(Search search) {
			if(this.count == null){
				try {
					atualizarDadosPesquisa(search, 0);
					this.count = this.processoDocumentoPeticaoNaoLidaManager.count(search);
				} catch (Exception e) {
					this.facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar o número de registros.");
					return QTD_LISTA_VAZIA;
				}
			}
			return this.count;
		}
		
		private void atualizarDadosPesquisa(Search search, int qtdRegistros) throws NoSuchFieldException {
			aplicarFiltros(search, "processoDocumento.processoTrf.");
			
			search.addCriteria(this.criterias);
			search.addOrder(this.attributeOrder, this.order);
			search.setMax(qtdRegistros);
		}
	}
	
	/**
	 * Classe interna responsável pela pesquisa de processos sob revisão.
	 */
	private class RevisorProcessoTrfRetriever implements DataRetriever<RevisorProcessoTrf> {
		private Long count;
		private FacesMessages facesMessages;		
		private RevisorProcessoTrfManager revisorProcessoTrfManager;
		private List<Criteria> criterias;
		
		public RevisorProcessoTrfRetriever(RevisorProcessoTrfManager revisorProcessoTrfManager, 
				FacesMessages facesMessages, List<Criteria> criterias) {
			
			this.revisorProcessoTrfManager = revisorProcessoTrfManager;
			this.facesMessages = facesMessages;
			this.criterias = criterias;
		}
		
		@Override
		public Object getId(RevisorProcessoTrf obj) {
			return this.revisorProcessoTrfManager.getId(obj);
		}

		@Override
		public RevisorProcessoTrf findById(Object id) throws Exception {
			return this.revisorProcessoTrfManager.findById(id);
		}

		@Override
		public List<RevisorProcessoTrf> list(Search search) {
			List<RevisorProcessoTrf> retorno = new ArrayList<RevisorProcessoTrf>(0);
			try {
				atualizarDadosPesquisa(search, 10);
				retorno = this.revisorProcessoTrfManager.list(search);
			} catch (NoSuchFieldException nsfe){
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os registros.");
			}
			return retorno;
		}

		@Override
		public long count(Search search) {
			if(this.count == null){
				try {
					atualizarDadosPesquisa(search, 0);
					this.count = this.revisorProcessoTrfManager.count(search);
				} catch (Exception e) {
					this.facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar o número de registros.");
					return QTD_LISTA_VAZIA;
				}
			}
			return this.count;
		}
		
		private void atualizarDadosPesquisa(Search search, int qtdRegistros) throws NoSuchFieldException {
			aplicarFiltros(search, "processoTrf.");
			search.addCriteria(this.criterias);
			search.setMax(qtdRegistros);
		}
	}
	
	/**
	 * Classe interna responsável pela pesquisa de processos devolvidos pelo revisor.
	 */
	private class RevisorProcessoTrfDevolvidoRetriever implements DataRetriever<RevisorProcessoTrfDevolvido> {
		private Long count;
		private FacesMessages facesMessages;		
		private RevisorProcessoTrfDevolvidoManager revisorProcessoTrfDevolvidoManager;
		private List<Criteria> criterias;
		
		public RevisorProcessoTrfDevolvidoRetriever(RevisorProcessoTrfDevolvidoManager revisorProcessoTrfDevolvidoManager, 
				FacesMessages facesMessages, List<Criteria> criterias) {
			
			this.revisorProcessoTrfDevolvidoManager = revisorProcessoTrfDevolvidoManager;
			this.facesMessages = facesMessages;
			this.criterias = criterias;
		}
		
		@Override
		public Object getId(RevisorProcessoTrfDevolvido obj) {
			return this.revisorProcessoTrfDevolvidoManager.getId(obj);
		}

		@Override
		public RevisorProcessoTrfDevolvido findById(Object id) throws Exception {
			return this.revisorProcessoTrfDevolvidoManager.findById(id);
		}

		@Override
		public List<RevisorProcessoTrfDevolvido> list(Search search) {
			List<RevisorProcessoTrfDevolvido> retorno = new ArrayList<RevisorProcessoTrfDevolvido>(0);
			try {
				atualizarDadosPesquisa(search, 10);
				retorno = this.revisorProcessoTrfDevolvidoManager.list(search);
			} catch (NoSuchFieldException nsfe){
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os registros.");
			}
			return retorno;
		}

		@Override
		public long count(Search search) {
			if(this.count == null){
				try {
					atualizarDadosPesquisa(search, 0);
					this.count = this.revisorProcessoTrfDevolvidoManager.count(search);
				} catch (Exception e) {
					this.facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar o número de registros.");
					return QTD_LISTA_VAZIA;
				}
			}
			return this.count;
		}
		
		private void atualizarDadosPesquisa(Search search, int qtdRegistros) throws NoSuchFieldException {
			aplicarFiltros(search, "revisorProcessoTrf.processoTrf.");
			search.addCriteria(this.criterias);
			search.setMax(qtdRegistros);
		}
	}
	
	// GETTERs AND SETTERs
	
	public String getNumeroProcesso() {
		return numeroProcesso;
	}
 
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}
	
	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}
	
	public AssuntoTrf getAssunto() {
		return assunto;
	}

	public void setAssunto(AssuntoTrf assunto) {
		this.assunto = assunto;
	}
	
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public String getNumeroCPF() {
		return numeroCPF;
	}

	public void setNumeroCPF(String numeroCPF) {
		this.numeroCPF = numeroCPF;
	}

	public String getNumeroCNPJ() {
		return numeroCNPJ;
	}

	public void setNumeroCNPJ(String numeroCNPJ) {
		this.numeroCNPJ = numeroCNPJ;
	}

	public boolean isDocumentoCPF() {
		return documentoCPF;
	}

	public void setDocumentoCPF(boolean documentoCPF) {
		this.documentoCPF = documentoCPF;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getNumeroOab() {
		return numeroOab;
	}

	public void setNumeroOab(String numeroOab) {
		this.numeroOab = numeroOab;
	}

	public String getLetraOab() {
		return letraOab;
	}

	public void setLetraOab(String letraOab) {
		this.letraOab = letraOab;
	}

	public Estado getUfOab() {
		return ufOab;
	}

	public void setUfOab(Estado ufOab) {
		this.ufOab = ufOab;
	}
	
	public FiltroTempoAgrupadoresEnum[] getFiltrosTempoAgrupadores() {
		return FiltroTempoAgrupadoresEnum.values();
	}
	
	public FiltroTempoAgrupadoresEnum getFiltroTempoAgrupadores() {
		return filtroTempoAgrupadores;
	}

	public void setFiltroTempoAgrupadores(FiltroTempoAgrupadoresEnum filtroTempoAgrupadores) {
		this.filtroTempoAgrupadores = filtroTempoAgrupadores;
	}

	public boolean isProcessoComParteSemCPFCNPJ() {
		return processoComParteSemCPFCNPJ;
	}

	public void setProcessoComParteSemCPFCNPJ(boolean processoComParteSemCPFCNPJ) {
		this.processoComParteSemCPFCNPJ = processoComParteSemCPFCNPJ;
	}

	public EntityDataModel<ProcessoTrf> getProcessoPedidoSegredoNaoApreciado() {
		return processoPedidoSegredoNaoApreciado;
	}
	
	public EntityDataModel<ProcessoDocumento> getProcessoPedidoSigiloDocumentoNaoApreciado() {
		return processoPedidoSigiloDocumentoNaoApreciado;
	}

	public EntityDataModel<ProcessoTrf> getProcessoPedidoAssistenciaJudiciariaGratuitaNaoApreciado() {
		return processoPedidoAssistenciaJudiciariaGratuitaNaoApreciado;
	}

	public EntityDataModel<ProcessoTrf> getProcessoPedidoLiminarAntecipacaoTutelaNaoApreciado() {
		return processoPedidoLiminarAntecipacaoTutelaNaoApreciado;
	}
	
	public EntityDataModel<ProcessoDocumentoPeticaoNaoLida> getProcessoHabilitacaoAutosNaoLidos() {
		return processoHabilitacaoAutosNaoLidos;
	}
	
	public EntityDataModel<ProcessoTrf> getProcessoAnalisePrevencao() {
		return processoAnalisePrevencao;
	}
	
	public EntityDataModel<ProcessoDocumento> getProcessoDocumentoNaoLido() {
		return processoDocumentoNaoLido;
	}
	
	public EntityDataModel<ProcessoExpedienteCentralMandado> getProcessoMandadoDevolvido() {
		return processoMandadoDevolvido;
	}

	public EntityDataModel<RevisorProcessoTrf> getProcessoAguardandoRevisao() {
		return processoAguardandoRevisao;
	}

	public EntityDataModel<RevisorProcessoTrf> getProcessoRevisado() {
		return processoRevisado;
	}

	public EntityDataModel<RevisorProcessoTrfDevolvido> getProcessoDevolvidoRevisao() {
		return processoDevolvidoRevisao;
	}
	
	public EntityDataModel<ProcessoDocumentoPeticaoNaoLida> getProcessoPeticoesAvulsasNaoLidas() {
		return processoPeticoesAvulsasNaoLidas;
	}

	public Boolean getProcessoPedidoSegredoNaoApreciadoCheckAll() {
		return processoPedidoSegredoNaoApreciadoCheckAll;
	}

	public void setProcessoPedidoSegredoNaoApreciadoCheckAll(Boolean processoPedidoSegredoNaoApreciadoCheckAll) {
		this.processoPedidoSegredoNaoApreciadoCheckAll = processoPedidoSegredoNaoApreciadoCheckAll;
	}

	public Boolean getProcessoPedidoSigiloDocumentoNaoApreciadoCheckAll() {
		return processoPedidoSigiloDocumentoNaoApreciadoCheckAll;
	}

	public void setProcessoPedidoSigiloDocumentoNaoApreciadoCheckAll(Boolean processoPedidoSigiloDocumentoNaoApreciadoCheckAll) {
		this.processoPedidoSigiloDocumentoNaoApreciadoCheckAll = processoPedidoSigiloDocumentoNaoApreciadoCheckAll;
	}

	public Boolean getProcessoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheckAll() {
		return processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheckAll;
	}

	public void setProcessoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheckAll(
			Boolean processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheckAll) {
		
		this.processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheckAll = processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheckAll;
	}

	public Boolean getProcessoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheckAll() {
		return processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheckAll;
	}

	public void setProcessoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheckAll(
			Boolean processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheckAll) {
		
		this.processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheckAll = processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheckAll;
	}

	public Boolean getProcessoHabilitacaoAutosNaoLidosCheckAll() {
		return processoHabilitacaoAutosNaoLidosCheckAll;
	}

	public void setProcessoHabilitacaoAutosNaoLidosCheckAll(Boolean processoHabilitacaoAutosNaoLidosCheckAll) {
		this.processoHabilitacaoAutosNaoLidosCheckAll = processoHabilitacaoAutosNaoLidosCheckAll;
	}
	
	public Boolean getProcessoDocumentoNaoLidoCheckAll() {
		return processoDocumentoNaoLidoCheckAll;
	}

	public void setProcessoDocumentoNaoLidoCheckAll(Boolean processoDocumentoNaoLidoCheckAll) {
		this.processoDocumentoNaoLidoCheckAll = processoDocumentoNaoLidoCheckAll;
	}

	public Boolean getProcessoMandadoDevolvidoCheckAll() {
		return processoMandadoDevolvidoCheckAll;
	}

	public void setProcessoMandadoDevolvidoCheckAll(Boolean processoMandadoDevolvidoCheckAll) {
		this.processoMandadoDevolvidoCheckAll = processoMandadoDevolvidoCheckAll;
	}

	public Boolean getProcessoPeticoesAvulsasNaoLidasCheckAll() {
		return processoPeticoesAvulsasNaoLidasCheckAll;
	}

	public void setProcessoPeticoesAvulsasNaoLidasCheckAll(Boolean processoPeticoesAvulsasNaoLidasCheckAll) {
		this.processoPeticoesAvulsasNaoLidasCheckAll = processoPeticoesAvulsasNaoLidasCheckAll;
	}

	public Map<ProcessoTrf, Boolean> getProcessoPedidoSegredoNaoApreciadoCheck() {
		return processoPedidoSegredoNaoApreciadoCheck;
	}

	public void setProcessoPedidoSegredoNaoApreciadoCheck(Map<ProcessoTrf, Boolean> processoPedidoSegredoNaoApreciadoCheck) {
		this.processoPedidoSegredoNaoApreciadoCheck = processoPedidoSegredoNaoApreciadoCheck;
	}
	
	public Map<ProcessoDocumento, Boolean> getProcessoPedidoSigiloDocumentoNaoApreciadoCheck() {
		return processoPedidoSigiloDocumentoNaoApreciadoCheck;
	}

	public void setProcessoPedidoSigiloDocumentoNaoApreciadoCheck(Map<ProcessoDocumento, Boolean> processoPedidoSigiloDocumentoNaoApreciadoCheck) {
		this.processoPedidoSigiloDocumentoNaoApreciadoCheck = processoPedidoSigiloDocumentoNaoApreciadoCheck;
	}

	public Map<ProcessoTrf, Boolean> getProcessoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheck() {
		return processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheck;
	}

	public void setProcessoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheck(
			Map<ProcessoTrf, Boolean> processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheck) {
		
		this.processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheck = processoPedidoAssistenciaJudiciariaGratuitaNaoApreciadoCheck;
	}

	public Map<ProcessoTrf, Boolean> getProcessoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheck() {
		return processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheck;
	}

	public void setProcessoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheck(
			Map<ProcessoTrf, Boolean> processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheck) {
		
		this.processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheck = processoPedidoLiminarAntecipacaoTutelaNaoApreciadoCheck;
	}

	public Map<ProcessoDocumentoPeticaoNaoLida, Boolean> getProcessoHabilitacaoAutosNaoLidosCheck() {
		return processoHabilitacaoAutosNaoLidosCheck;
	}

	public void setProcessoHabilitacaoAutosNaoLidosCheck(Map<ProcessoDocumentoPeticaoNaoLida, Boolean> processoHabilitacaoAutosNaoLidosCheck) {
		this.processoHabilitacaoAutosNaoLidosCheck = processoHabilitacaoAutosNaoLidosCheck;
	}

	public Map<ProcessoDocumento, Boolean> getProcessoDocumentoNaoLidoCheck() {
		return processoDocumentoNaoLidoCheck;
	}

	public void setProcessoDocumentoNaoLidoCheck(Map<ProcessoDocumento, Boolean> processoDocumentoNaoLidoCheck) {
		this.processoDocumentoNaoLidoCheck = processoDocumentoNaoLidoCheck;
	}

	public Map<ProcessoExpedienteCentralMandado, Boolean> getProcessoMandadoDevolvidoCheck() {
		return processoMandadoDevolvidoCheck;
	}

	public void setProcessoMandadoDevolvidoCheck(Map<ProcessoExpedienteCentralMandado, Boolean> processoMandadoDevolvidoCheck) {
		this.processoMandadoDevolvidoCheck = processoMandadoDevolvidoCheck;
	}

	public Map<ProcessoDocumentoPeticaoNaoLida, Boolean> getProcessoPeticoesAvulsasNaoLidasCheck() {
		return processoPeticoesAvulsasNaoLidasCheck;
	}

	public void setProcessoPeticoesAvulsasNaoLidasCheck(Map<ProcessoDocumentoPeticaoNaoLida, Boolean> processoPeticoesAvulsasNaoLidasCheck) {
		this.processoPeticoesAvulsasNaoLidasCheck = processoPeticoesAvulsasNaoLidasCheck;
	}

	public boolean isPermiteVisualizarAbaAgrupadores() {
		return permiteVisualizarAbaAgrupadores;
	}

	public void setPermiteVisualizarAbaAgrupadores(boolean permiteVisualizarAbaAgrupadores) {
		this.permiteVisualizarAbaAgrupadores = permiteVisualizarAbaAgrupadores;
	}

	public boolean isPermiteVisualizarAgrupadorProcessoComPedidoDeSigilo() {
		return permiteVisualizarAgrupadorProcessoComPedidoDeSigilo;
	}

	public void setPermiteVisualizarAgrupadorProcessoComPedidoDeSigilo(boolean permiteVisualizarAgrupadorProcessoComPedidoDeSigilo) {
		this.permiteVisualizarAgrupadorProcessoComPedidoDeSigilo = permiteVisualizarAgrupadorProcessoComPedidoDeSigilo;
	}

	public boolean isPermiteVisualizarAgrupadorDocumentoComPedidoDeSigilo() {
		return permiteVisualizarAgrupadorDocumentoComPedidoDeSigilo;
	}

	public void setPermiteVisualizarAgrupadorDocumentoComPedidoDeSigilo(boolean permiteVisualizarAgrupadorDocumentoComPedidoDeSigilo) {
		this.permiteVisualizarAgrupadorDocumentoComPedidoDeSigilo = permiteVisualizarAgrupadorDocumentoComPedidoDeSigilo;
	}
	
	public boolean isPermiteVisualizarAgrupadorPedidosDeJusticaGratuita() {
		return permiteVisualizarAgrupadorPedidosDeJusticaGratuita;
	}

	public void setPermiteVisualizarAgrupadorPedidosDeJusticaGratuita(boolean permiteVisualizarAgrupadorPedidosDeJusticaGratuita) {
		this.permiteVisualizarAgrupadorPedidosDeJusticaGratuita = permiteVisualizarAgrupadorPedidosDeJusticaGratuita;
	}
	
	public boolean isPermiteVisualizarAgrupadorPedidoDeLiminarTutela() {
		return permiteVisualizarAgrupadorPedidoDeLiminarTutela;
	}

	public void setPermiteVisualizarAgrupadorPedidoDeLiminarTutela(boolean permiteVisualizarAgrupadorPedidoDeLiminarTutela) {
		this.permiteVisualizarAgrupadorPedidoDeLiminarTutela = permiteVisualizarAgrupadorPedidoDeLiminarTutela;
	}
	
	public boolean isPermiteVisualizarAgrupadorHabilitacaoNosAutos() {
		return permiteVisualizarAgrupadorHabilitacaoNosAutos;
	}

	public void setPermiteVisualizarAgrupadorHabilitacaoNosAutos(boolean permiteVisualizarAgrupadorHabilitacaoNosAutos) {
		this.permiteVisualizarAgrupadorHabilitacaoNosAutos = permiteVisualizarAgrupadorHabilitacaoNosAutos;
	}
	
	public boolean isPermiteVisualizarAgrupadorAnaliseDePrevencao() {
		return permiteVisualizarAgrupadorAnaliseDePrevencao;
	}

	public void setPermiteVisualizarAgrupadorAnaliseDePrevencao(boolean permiteVisualizarAgrupadorAnaliseDePrevencao) {
		this.permiteVisualizarAgrupadorAnaliseDePrevencao = permiteVisualizarAgrupadorAnaliseDePrevencao;
	}
	
	public boolean isPermiteVisualizarAgrupadorDocumentosNaoLidos() {
		return permiteVisualizarAgrupadorDocumentosNaoLidos;
	}

	public void setPermiteVisualizarAgrupadorDocumentosNaoLidos(boolean permiteVisualizarAgrupadorDocumentosNaoLidos) {
		this.permiteVisualizarAgrupadorDocumentosNaoLidos = permiteVisualizarAgrupadorDocumentosNaoLidos;
	}
	
	public boolean isPermiteVisualizarAgrupadorMandadosDevolvidos() {
		return permiteVisualizarAgrupadorMandadosDevolvidos;
	}

	public void setPermiteVisualizarAgrupadorMandadosDevolvidos(boolean permiteVisualizarAgrupadorMandadosDevolvidos) {
		this.permiteVisualizarAgrupadorMandadosDevolvidos = permiteVisualizarAgrupadorMandadosDevolvidos;
	}
	
	public boolean isPermiteVisualizarAgrupadorAguardandoRevisao() {
		return permiteVisualizarAgrupadorAguardandoRevisao;
	}

	public void setPermiteVisualizarAgrupadorAguardandoRevisao(boolean permiteVisualizarAgrupadorAguardandoRevisao) {
		this.permiteVisualizarAgrupadorAguardandoRevisao = permiteVisualizarAgrupadorAguardandoRevisao;
	}

	public boolean isPermiteVisualizarAgrupadorPeticoesAvulsas() {
		return permiteVisualizarAgrupadorPeticoesAvulsas;
	}

	public void setPermiteVisualizarAgrupadorPeticoesAvulsas(boolean permiteVisualizarAgrupadorPeticoesAvulsas) {
		this.permiteVisualizarAgrupadorPeticoesAvulsas = permiteVisualizarAgrupadorPeticoesAvulsas;
	}
	
}
