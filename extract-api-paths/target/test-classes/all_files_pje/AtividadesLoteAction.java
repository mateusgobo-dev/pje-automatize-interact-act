package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Strings;
import org.jbpm.JbpmContext;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.graph.def.Transition;
import org.jbpm.persistence.db.DbPersistenceService;
import org.jbpm.taskmgmt.def.TaskController;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.component.ValidacaoAssinaturaProcessoDocumento;
import br.com.infox.cliente.home.PainelUsuarioHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.jbpm.actions.JbpmEventsHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.com.infox.ibpm.component.tree.EventoBean;
import br.com.infox.ibpm.component.tree.EventsLoteTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.EventoAgrupamentoManager;
import br.com.itx.component.Util;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.entidades.vo.MiniPacVO;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.enums.TipoProcessoDocumentoEnum;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.HistoricoMovimentacaoLoteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.FluxoService;
import br.jus.cnj.pje.nucleo.service.MiniPacService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualImpl;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.servicos.AtividadesLoteService;
import br.jus.cnj.pje.servicos.EditorEstiloService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.util.ParAssinatura;
import br.jus.cnj.pje.view.fluxo.ComunicacaoProcessualAction;
import br.jus.cnj.pje.view.fluxo.PreparaAtoComunicacaoAction;
import br.jus.cnj.pje.vo.AcordaoCompilacao;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.cnj.pje.webservice.client.criminal.ProcessoCriminalRestClient;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.csjt.pje.view.action.TipoProcessoDocumentoAction;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.HistoricoMovimentacaoLote;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.AtividadesLoteEnum;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Componente de controle de atividades realizadas em lote.
 * 
 * @author Guilherme Bispo
 * @author Bernardo Gouveia
 * 
 * @author Thiago de Andrade Vieira
 * @author cristof
 * @author leonardo.borges
 *
 */
@Name(AtividadesLoteAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class AtividadesLoteAction implements Serializable, ArquivoAssinadoUploader {

	private static final String MSG_NAO_EXISTE_ATO_PROFERIDO_NESTE_PROCESSO = "Não existe ato proferido neste processo";
	private static final String MSG_PARTES_SEM_INTIMAR = "Existe(m) parte(s) que não pode(m) ser intimada(s)";
	private static final String MSG_PARTES_SEM_CITAR = "Existe(m) parte(s) que não pode(m) ser citada(s)";
	private static final String MSG_NENHUMA_PARTE_INTIMACAO = "Nenhuma parte apta para receber a intimação";
	private static final String MSG_NENHUMA_PARTE_CITACAO = "Nenhuma parte apta para receber a citação";
	private static final String TODAS = "Todas";
	private static final String POLO_ATIVO = "Polo ativo";
	private static final String POLO_PASSIVO = "Polo passivo";
	private static final String POLO_TERCEIROS = "Outros Participantes";
	private static final String TIPOS_DE_POLO = TODAS + "," + POLO_ATIVO + "," + POLO_PASSIVO + "," + POLO_TERCEIROS;

	public static final String VARIAVEL_ERROS_CONECTOR_DJE = "variavelErrosConectorDJE";

	private static final long serialVersionUID = -7996471303065942395L;
	public static final String NAME = "atividadesLoteAction";

	private static final String TIPO_DOCUMENTO_TEXTO = "texto";
	private static final String TIPO_DOCUMENTO_BINARIO = "binario";

	public static final String PARAMETRO_VALIDA_CADASTRO_ADVOGADO_DIARIO = "pje:intimacaoLote:validaCadastroAdvogadoDiario";

	public static final String PARAMETRO_UTILIZA_PARAMETRO_ATO_PROFERIDO = "pje:intimacaoLote:utilizaParametroAtoProferido";

	public static final String PARAMETRO_TIPOS_DOCUMENTO_ULTIMO_ATO = "pje:intimacaoLote:tiposDocumentoUltimoAtoProferido";

	public static final String PARAMETRO_IDS_COMPETENCIA_CRIMINAL = "tjrj:intimacaoLote:ListaIdsCompetenciasCriminais";
	
	public static final String PARAMETRO_ID_MODELO_CITACAO = "pje:intimacaoLote:idModeloCitacao";
	
	public static final String PARAMETRO_ID_MODELO_CITACAO_ATO_PROFERIDO = "pje:intimacaoLote:idModeloCitacaoAtoProferido";


	public static final String PARAMETRO_ID_OUTROS_ANEXOS = "pje:intimacaoLote:idOutrosAnexos";

	Integer idPeticaoInicial = ParametroUtil.instance().getTipoProcessoDocumentoPeticaoInicial()
			.getIdTipoProcessoDocumento();

	@Logger
	private Log log;

	@In(create = true, required = true)
	private transient AtividadesLoteService atividadesLoteService;

	@In
	private FluxoService fluxoService;

	@In
	private AtoComunicacaoService atoComunicacaoService;

	@In(create = true, required = false)
	private ComunicacaoProcessualAction comunicacaoProcessualAction;

	@In
	private ParametroService parametroService;

	@In
	private TramitacaoProcessualService tramitacaoProcessualService;

	@In(create = true, required = false)
	private EventsLoteTreeHandler eventsLoteTree;

	@In(create = true, required = false)
	private FacesMessages facesMessages;

	@In(create = false, required = false)
	private GridQuery consultaProcessoGrid;

	@In(create = true, required = false)
	private JbpmEventsHandler jbpmEventsHandler;

	@In
	private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;

	@RequestParameter
	private String[] idsProcessoSelecionado;

	private List<TipoProcessoDocumento> tipoDocumentoItems;

	private Integer agrupamento;

	private Map<Integer, Boolean> agrupamentoPossuiMovimentos = new HashMap<>();

	private String encodedCertChain;

	private Integer idTarefa;

	private Long idTask;

	private String modeloDocumento;

	private String nomeTaskInstance = "";

	private Long idTransicaoPadrao;

	private Transition transicaoPadrao;

	@RequestParameter
	private AtividadesLoteEnum tipoAtividadeReq;

	private AtividadesLoteEnum tipoAtividade;

	private TipoProcessoDocumento tipoProcessoDocumentoSelecionado;

	// Utilizada para diminiur consultas ao banco
	private HashMap<String, List<AtividadesLoteEnum>> aptoAtividadeLoteMap = new HashMap<String, List<AtividadesLoteEnum>>();

	private List<ProcessoTrf> processoList = new ArrayList<ProcessoTrf>(0);

	private Map<Long, ProcessoTrf> processoListMap = new HashMap<Long, ProcessoTrf>();

	private Map<Integer, String> msgIntimacaoMap = new HashMap<Integer, String>();

	private Map<Integer, ProcessoDocumento> ultimoAtoProferidoMap = new HashMap<Integer, ProcessoDocumento>();

	private Map<Integer, Selecionado> processosMap = new HashMap<Integer, Selecionado>();

	private List<Long> processoListElegiveis = new ArrayList<Long>(0);

	private List<Long> processoListElegiveisSelecionados = new ArrayList<Long>(0);
	private Map<Long, Boolean> processosSelecionadosCheck = new HashMap<Long, Boolean>();

	public Map<Long, Boolean> getProcessosSelecionadosCheck() {
		return processosSelecionadosCheck;
	}

	public void setProcessosSelecionadosCheck(Map<Long, Boolean> processosSelecionadosCheck) {
		this.processosSelecionadosCheck = processosSelecionadosCheck;
	}

	private Map<Long, List<String>> processoNaoElegiveisListMap = new HashMap<Long, List<String>>();

	private Map<Long, Transition> taskTransitionMap = new HashMap<Long, Transition>();

	private List<AtividadesLoteEnum> atividadesHabilitadas = new ArrayList<AtividadesLoteEnum>();

	private List<ModeloDocumento> modelosDocumentosDisponiveis;

	private Map<Long, Set<ProcessoDocumento>> documentosProcesso = new HashMap<Long, Set<ProcessoDocumento>>();

	private List<ProcessoDocumento> intimacoes = new ArrayList<ProcessoDocumento>();

	private LinkedHashSet<Transition> transicoesPossiveis = new LinkedHashSet<Transition>(0);

	private List<String> tiposParteIntimacao = new ArrayList<String>();

	private Integer prazoIntimacao = 0;

	private ExpedicaoExpedienteEnum meioIntimacao = ExpedicaoExpedienteEnum.E;

	private Boolean ultimoAtoProferido = Boolean.TRUE;

	private boolean conteudoAlterado = false;

	private boolean conteudoVazio = true;

	private boolean intimacaoEmLoteFinalizada = false;

	// Assinatura em lote
	private ArrayList<ParAssinatura> assinaturas;

	private Boolean isUsandoMiniPac = Boolean.FALSE;
	private List<ProcessoParteParticipacaoEnum> polosSelecionadosList = new ArrayList<ProcessoParteParticipacaoEnum>(3);
	private List<ExpedicaoExpedienteEnum> meiosIndicadosFluxo = new ArrayList<ExpedicaoExpedienteEnum>(3);
	private List<ExpedicaoExpedienteEnum> meiosSelecionadosList = new ArrayList<ExpedicaoExpedienteEnum>(3);
	private Map<ExpedicaoExpedienteEnum, List<ProcessoTrf>> miniPacTempMap = new HashMap<ExpedicaoExpedienteEnum, List<ProcessoTrf>>();
	private String prazoGeral;
	private List<MiniPacVO> miniPacList;
	private Boolean intimacaoPessoal = Boolean.FALSE;

	private List<AcordaoCompilacao> acordaosCompilacoes = new ArrayList<AcordaoCompilacao>();

	// ck editor
	private List<ModeloDocumento> listaModelosDocumentoPorTipoDocumento = new ArrayList<ModeloDocumento>();
	private ProtocolarDocumentoBean protocolarDocumentoBean;
	@In(create = true, required = true)
	private TaskInstanceUtil taskInstanceUtil;
	@In(create = true)
	private transient DocumentoJudicialService documentoJudicialService;
	@In(create = true)
	private EditorEstiloService editorEstiloService;
	@In
	private ProcessoDocumentoManager processoDocumentoManager;

	/**
	 * Define a lista de arquivos assinados pelo assinador, os arquivos assinados e
	 * enviados pelo assinador sera armazenados nesta lista para posterior validacao
	 * e persistencia.
	 */
	private Map<Long, List<ArquivoAssinadoHash>> arquivosAssinados = new HashMap<Long, List<ArquivoAssinadoHash>>();
	private Boolean selecionarTodos = true;
	private Boolean naoPodeIntimar = true;
	private Boolean partePendente = true;
	private Boolean podeIntimar = true;
	private String errosDJE;
	private Boolean conectorDJEDisponivel = false;
	private Boolean parametroValidaCadastroAdvogadoDiario = false;
	private String idsCompetenciasCriminais;

	private TipoProcessoDocumento tipoComunicacao;
	private List<ExpedicaoExpedienteEnum> meios;
	private List<String> tiposPolosDisponiveis = new ArrayList<>();
	private String tiposPolosDisponiveisString;

	@Create
	public void load() throws Exception {

		limpaTiposPolo();

		if (processoListElegiveis.size() == 0 && idsProcessoSelecionado != null && idsProcessoSelecionado.length > 0) {
			tipoAtividade = tipoAtividadeReq != null ? tipoAtividadeReq : AtividadesLoteEnum.E;

			TaskInstance taskInstance = null;
			for (String id : idsProcessoSelecionado) {
				Long idTaskInstance = new Long(id);
				processoListElegiveis.add(idTaskInstance);
				processoListElegiveisSelecionados.add(idTaskInstance);
				taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTaskInstance);
				ProcessoTrf processoTrf = TaskInstanceUtil.instance()
						.getProcesso(taskInstance.getProcessInstance().getId());
				processoListMap.put(idTaskInstance, processoTrf);
				avaliaVariaveisFluxo(idTaskInstance, ((Long) taskInstance.getTask().getId()).intValue());

				if (tipoAtividade.equals(AtividadesLoteEnum.I)) {
					// verificar partes para intimar no load
					getPartesAptasParaIntimacao(processoTrf);
				}

			}

			tipoDocumentoItems = atividadesLoteService.getTipoDocumentoItems(taskInstance);

			selecionarProcessosElegiveis();
			setProtocolarDocumentoBean(new ProtocolarDocumentoBean(
					taskInstanceUtil.getProcesso(taskInstance.getProcessInstance().getId()).getIdProcessoTrf(),
					ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL
							| ProtocolarDocumentoBean.RECUPERA_DOCUMENTO_FLUXO,
					NAME));
			verificaCarregamentoDocumento();

			if (tipoAtividade.equals(AtividadesLoteEnum.I)) {
				tipoDocumentoItems = new ArrayList<>();
				TipoProcessoDocumento tipoIntimacao = ComponentUtil.getTipoProcessoDocumentoManager()
						.findByDescricaoTipoDocumento(TipoProcessoDocumentoEnum.I.getLabel());
				tipoDocumentoItems.add(tipoIntimacao);
				tipoDocumentoItems.add(ComponentUtil.getTipoProcessoDocumentoManager()
						.findByDescricaoTipoDocumento(TipoProcessoDocumentoEnum.C.getLabel()));
				tipoComunicacao = tipoIntimacao;

				meios = new ArrayList<>();
				meios.add(ExpedicaoExpedienteEnum.E);

				verificaConectorDJEDisponivel();
				if(this.conectorDJEDisponivel) {
					meios.add(ExpedicaoExpedienteEnum.P);
				}
				
				parametroValidaCadastroAdvogadoDiario = parametroService
						.valueOf(PARAMETRO_VALIDA_CADASTRO_ADVOGADO_DIARIO) == null
								? false
								: parametroService.valueOf(PARAMETRO_VALIDA_CADASTRO_ADVOGADO_DIARIO).equals("true")
										? true
										: false;

				filtrarProcesso();
				idsCompetenciasCriminais = parametroService.valueOf(PARAMETRO_IDS_COMPETENCIA_CRIMINAL);
			}

		}

	}

	public void verificaDocumentoAlterado() {
		setConteudoAlterado(true);
	}

	private void verificaConectorDJEDisponivel() {
		this.conectorDJEDisponivel = atoComunicacaoService.verificarServicoDisponivel(ExpedicaoExpedienteEnum.P);

	}

	public Boolean getConectorDJEDisponivel() {
		return conectorDJEDisponivel;
	}

	public void setConectorDJEDisponivel(Boolean conectorDJEDisponivel) {
		this.conectorDJEDisponivel = conectorDJEDisponivel;
	}

	public TipoProcessoDocumento getTipoComunicacao() {
		return tipoComunicacao;
	}

	public void setTipoComunicacao(TipoProcessoDocumento tipoComunicacao) {
		this.tipoComunicacao = tipoComunicacao;
	}

	public List<ExpedicaoExpedienteEnum> getMeios() {
		return meios;
	}

	public void setMeios(List<ExpedicaoExpedienteEnum> meios) {
		this.meios = meios;
	}

	public SelectItem[] getMeiosEnum() {

		SelectItem[] itens = new SelectItem[getMeios().size()];
		Integer i = 0;
		for (ExpedicaoExpedienteEnum meio : getMeios()) {
			itens[i] = new SelectItem(meio, meio.getLabel());
			i++;
		}
		return itens;
	}

	/**
	 * Verifica se há apenas um processo selecionado ou se há mais de um processo
	 * selecionado, caso seja apenas um processo, recupera seu conteúdo, tipo de
	 * documento e movimentação já selecionada
	 * 
	 * @throws PJeBusinessException
	 */
	private void verificaCarregamentoDocumento() throws PJeBusinessException {
		this.carregarDocumentoPrincipalNovo();

		if (this.processoListMap != null && this.processoListMap.values().size() == 1) {
			Long idTaskInstance = (Long) this.processoListMap.keySet().toArray()[0];
			Integer idMinutaEmElaboracao = JbpmUtil.instance()
					.recuperarIdMinutaEmElaboracao(ManagedJbpmContext.instance().getTaskInstance(idTaskInstance));

			this.carregarDadosUnicoProcessoSelecionado(idMinutaEmElaboracao, idTaskInstance);
		}
	}

	private void carregarDocumentoPrincipalNovo() throws PJeBusinessException {
		getProtocolarDocumentoBean().setDocumentoPrincipal(documentoJudicialService.getDocumento());

		// nullificado o tipo e modelo de documento na abertura da minuta em lote para
		// retornar um novo documento
		ProcessoHome.instance().setTipoProcessoDocumento(null);
		TipoProcessoDocumentoAction.instance().setModelosDocumentosDisponiveis(null);
	}

	/**
	 * Recupera a minuta que já esteja em elaboração no processo indicado e caso
	 * haja, seleciona o agrupamento de movimentos do tipo de documento
	 * pre-selecionado e os movimentos já selecionados anteriormente
	 * 
	 * @param idMinutaEmElaboracao
	 * @param idTaskInstance
	 * @throws PJeBusinessException
	 */
	private void carregarDadosUnicoProcessoSelecionado(Integer idMinutaEmElaboracao, Long idTaskInstance)
			throws PJeBusinessException {
		if (idMinutaEmElaboracao != null && idMinutaEmElaboracao > 0) {
			ProcessoDocumento minutaEmElaboracao = documentoJudicialService.getDocumento(idMinutaEmElaboracao);

			if (minutaEmElaboracao != null && minutaEmElaboracao.getTipoProcessoDocumento() != null
					&& minutaEmElaboracao.getDataJuntada() == null
					&& minutaEmElaboracao.getProcessoDocumentoBin() != null
					&& !ComponentUtil.getComponent(ProcessoDocumentoBinManager.class).existemSignatarios(
							minutaEmElaboracao.getProcessoDocumentoBin().getIdProcessoDocumentoBin())) {

				this.setModeloDocumento(minutaEmElaboracao.getProcessoDocumentoBin().getModeloDocumento());
				this.setTipoProcessoDocumentoSelecionado(minutaEmElaboracao.getTipoProcessoDocumento());

				getProtocolarDocumentoBean().setDocumentoPrincipal(minutaEmElaboracao);
				ProcessoHome.instance().setTipoProcessoDocumento(minutaEmElaboracao.getTipoProcessoDocumento());

				if (minutaEmElaboracao.getTipoProcessoDocumento().getAgrupamento() != null) {
					this.setNovoAgrupamentoMovimentos(
							minutaEmElaboracao.getTipoProcessoDocumento().getAgrupamento().getIdAgrupamento());
					org.jbpm.graph.exe.ProcessInstance processInstance = ManagedJbpmContext.instance()
							.getTaskInstance(idTaskInstance).getProcessInstance();

					if (processInstance != null) {
						eventsLoteTree.setEventoBeanList(
								LancadorMovimentosService.instance().getMovimentosTemporarios(processInstance));
					}
				}
			}
		}
	}

	/**
	 * Observa o evento de seleção de tarefa pelo usuário.
	 * 
	 * @param map o mapa contendo os identificadores das {@link TaskInstance} e das
	 *            respectivas {@link Tarefa}
	 */
	@Observer(Eventos.SELECIONADA_TAREFA)
	public void observadorTarefasTreeHandler(Map<String, Object> map) {
		preparar_(map);
	}

	/**
	 * Observa o evento de seleção de caixa de tarefa pelo usuário.
	 * 
	 * @param map o mapa contendo os identificadores das {@link TaskInstance} e das
	 *            respectivas {@link Tarefa}
	 */
	@Observer(Eventos.SELECIONADA_CAIXA_DE_TAREFA)
	public void observadorTarefasTreeHandlerCaixa(Map<String, Object> map) {
		preparar_(map);
		PainelUsuarioHome painel = (PainelUsuarioHome) Component.getInstance("painelUsuarioHome");
		painel.getSelected().put("nomeTarefa", map.get("nomeTarefa"));
	}

	/**
	 * Inicializa o objeto para eventual execução em lote.
	 * 
	 * @param map o mapa contendo os identificadores de {@link TaskInstance} e
	 *            {@link Tarefa}.
	 */
	private void preparar_(Map<String, Object> map) {
		limparProcessosSelecionados();
		nomeTaskInstance = "";
		tipoAtividade = null;
		this.idTask = ((Long) map.get("id"));
		this.idTarefa = ((Integer) map.get("idTask"));
	}

	/**
	 * Redefine todos os objetos relevantes para o contexto atual da tela,
	 * permitindo o reaproveitamento do objeto ainda que em conversação.
	 */
	private void limparProcessosSelecionados() {
		Contexts.removeFromAllContexts("consultaProcessoGrid");
		resetData_();
	}

	/**
	 * @return the assinaturas
	 */
	public List<ParAssinatura> getAssinaturas() {
		if ((assinaturas == null) || (assinaturas.size() != getProcessoListElegiveis().size())) {
			assinaturas = new ArrayList<ParAssinatura>();
			String contents;
			List<ProcessoDocumento> documentosParaAssinatura;
			ParAssinatura pa;
			for (Long idTask : getProcessoListElegiveis()) {
				try {
					documentosParaAssinatura = atividadesLoteService.getDocumentoProntoParaAssinatura(idTask);
					if (documentosParaAssinatura != null && documentosParaAssinatura.size() > 0) {
						for (ProcessoDocumento processoDocumentoProntoParaAssinatura : documentosParaAssinatura) {
							contents = new String(SigningUtilities.base64Encode(processoDocumentoProntoParaAssinatura
									.getProcessoDocumentoBin().getModeloDocumento().getBytes()));
							if ((contents != null) && !"".equals(contents)) {
								pa = new ParAssinatura();
								pa.setConteudo(contents);
								assinaturas.add(pa);
							} else {
								facesMessages.add(Severity.ERROR, "Favor verificar se há documentos não preenchidos.");
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return assinaturas;
	}

	public void setAssinaturas(ArrayList<ParAssinatura> assinaturas) {
		this.assinaturas = assinaturas;
	}

	/**
	 * Adiciona ou remove o processo referido da lista de processos tratados para o
	 * lote.
	 * 
	 * @param processo o processo a ser incluído ou excluído
	 */
	public void adicionaRemoveProcesso(ConsultaProcessoVO processo) {
		if (processoList.contains(processo.getConsultaProcesso().getProcessoTrf())) {
			removerProcesso(processo);
		} else {
			adicionarProcesso(processo);
		}
	}

	/**
	 * Acrescenta todos os processos da página apresentada em tela à lista de
	 * processos a serem tratados em lote.
	 */
	@SuppressWarnings("unchecked")
	public void adicionaTodosPagina() {
		List<ConsultaProcessoVO> processos = consultaProcessoGrid.getResultList();
		adicionaTodos(processos);
	}

	/**
	 * Acrestenta à lista de processos a serem tratados em lote todos os da tarefa.
	 */
	@SuppressWarnings("unchecked")
	public void adicionaTodosTarefa() {
		List<ConsultaProcessoVO> processos = consultaProcessoGrid.getFullList();
		adicionaTodos(processos);
	}

	/**
	 * Adiciona à lista de processos a serem tratados em lote todos os processos da
	 * lista dada.
	 * 
	 * @param processos a lista de processos a serem adicionados.
	 */
	private void adicionaTodos(List<ConsultaProcessoVO> processos) {
		for (ConsultaProcessoVO p : processos) {
			adicionarProcesso(p);
		}
	}

	/**
	 * Inverte a seleção de processos da página, fazendo com que os atualmente
	 * selecionados sejam tornados não selecionados e vice-versa.
	 */
	@SuppressWarnings("unchecked")
	public void inverteSelecaoPagina() {
		List<ConsultaProcessoVO> processos = consultaProcessoGrid.getResultList();
		inverteSelecao(processos);
	}

	/**
	 * Inverte a seleção de processos da tarefa, fazendo com que os atualmente
	 * selecionados sejam tornados não selecionados e vice-versa.
	 */
	@SuppressWarnings("unchecked")
	public void inverteSelecaoTarefa() {
		List<ConsultaProcessoVO> processos = consultaProcessoGrid.getFullList();
		inverteSelecao(processos);
	}

	/**
	 * Faz com que os processos informados sejam marcados selecionados caso ainda
	 * não estejam selecionados para tratamento em lote, e aqueles anteriormente
	 * selecionados sejam excluídos desse tratamento.
	 * 
	 * @param processos os processos cuja inversão de seleção se deseja.
	 */
	private void inverteSelecao(List<ConsultaProcessoVO> processos) {
		for (ConsultaProcessoVO p : processos) {
			if (processoList.contains(p.getConsultaProcesso().getProcessoTrf())) {
				removerProcesso(p);
			} else {
				adicionarProcesso(p);
			}
		}
	}

	/**
	 * Acrescenta um processo à lista de processos a serem tratados em lote.
	 * 
	 * @param proc o processo a ser incluído na lista daqueles a serem tratados.
	 */
	private void adicionarProcesso(ConsultaProcessoVO proc) {
		ProcessoTrf p = proc.getConsultaProcesso().getProcessoTrf();
		if (!processoList.contains(p)) {
			processoList.add(p);
			processosMap.put(proc.getIdProcessoTrf(), new Selecionado(Boolean.TRUE));
			processoListMap.put(proc.getIdTaskInstance(), p);
		}
	}

	/**
	 * Remove um processo da lista de processos a serem tratados em lote.
	 * 
	 * @param proc o processo a ser excluído da lista daqueles a serem tratados.
	 */
	private void removerProcesso(ConsultaProcessoVO proc) {
		ProcessoTrf p = proc.getConsultaProcesso().getProcessoTrf();
		if (processoList.contains(p)) {
			processosMap.remove(p.getIdProcessoTrf());
			processoList.remove(p);
			processoListMap.remove(proc.getIdTaskInstance());
		}
	}

	/**
	 * Remove todos os processos da lista de processos a serem tratados em lote.
	 */
	public void removeTodos() {
		resetData_();
	}

	/**
	 * Limpa todos os dados de processos a serem tratados em lote.
	 */
	private void resetData_() {
		processoList.clear();
		processoListMap.clear();
		msgIntimacaoMap.clear();
		ultimoAtoProferidoMap.clear();
		taskTransitionMap.clear();
		processosMap.clear();
		processoListElegiveis.clear();
		processoNaoElegiveisListMap.clear();
		atividadesHabilitadas = null;
		transicoesPossiveis.clear();
		meiosIndicadosFluxo.clear();
		meiosSelecionadosList.clear();
		miniPacTempMap.clear();
		intimacaoPessoal = Boolean.FALSE;
	}

	/**
	 * Recupera a lista de processos selecionados para tratamento em lote.
	 * 
	 * @return a lista de processos
	 */
	public List<ProcessoTrf> getProcessoList() {
		return processoList;
	}

	/**
	 * Atribui a este objeto uma lista de processos para tratamento em lote.
	 * 
	 * @param processos a lista de processos
	 */
	public void setProcessoList(List<ProcessoTrf> processos) {
		this.processoList = processos;
	}

	/**
	 * Recupera o tipo de atividade em lote atualmente selecionada para execução.
	 * 
	 * @return o tipo de atividade
	 * 
	 * @see AtividadesLoteEnum#A
	 * @see AtividadesLoteEnum#E
	 * @see AtividadesLoteEnum#M
	 */
	public AtividadesLoteEnum getTipoAtividade() {
		return tipoAtividade;
	}

	/**
	 * Define o tipo de atividade em lote a ser executada.
	 * 
	 * @param tipoAtividade o tipo de atividade a ser executada.
	 * 
	 * @see AtividadesLoteEnum#A
	 * @see AtividadesLoteEnum#E
	 * @see AtividadesLoteEnum#M
	 */
	public void setTipoAtividade(AtividadesLoteEnum tipoAtividade) {
		if (tipoAtividade != null) {
			Contexts.removeFromAllContexts("consultaProcessoGrid");
		}
		nomeTaskInstance = getNomeTarefaPainelUsuario();
		this.tipoAtividade = tipoAtividade;
		limparProcessosSelecionados();
	}

	/**
	 * Indica se o componente de controle de tela está em modo de execução em lote.
	 * 
	 * @return true, se o tipo de atividade estiver definida e o nome da tarefa for
	 *         igual ao nome da tarefa do usuário
	 */
	public Boolean estaEmLote() {
		return getTipoAtividade() != null && nomeTaskInstance.equals(getNomeTarefaPainelUsuario());
	}

	/**
	 * Indica se o componente de controle de tela está em modo de assinatura em
	 * lote.
	 * 
	 * @return true, se o tipo de atividade estiver definida e igual a
	 *         {@link AtividadesLoteEnum#A} e o nome da tarefa for igual ao nome da
	 *         tarefa do usuário
	 */
	public Boolean estaEmAssinaturaEmLote() {
		return estaEmLote() && this.tipoAtividade == AtividadesLoteEnum.A;
	}

	/**
	 * Recupera o nome da tarefa atualmente selecionada pelo usuário.
	 * 
	 * @return o nome da tarefa
	 * 
	 * @see PainelUsuarioHome#getSelected()
	 */
	public String getNomeTarefaPainelUsuario() {
		PainelUsuarioHome painel = (PainelUsuarioHome) Component.getInstance("painelUsuarioHome");
		Map<String, Object> selected = painel.getSelected();
		if (selected != null) {
			if (selected.containsKey("nomeTarefa")) {
				return (String) selected.get("nomeTarefa");
			}
			return "-1";
		}
		return "-1";
	}

	/**
	 * Verifica se a {@link TaskInstance} atual, quando combinada com a
	 * {@link Tarefa} respectiva, pode ser executada em algum dos três contextos de
	 * lote:
	 * <li>movimentar em lote processos no fluxo</li>
	 * <li>minutar documentos em lote</li>
	 * <li>assinar documentos minutados em lote</li>
	 * 
	 * @return true, se a atividade estiver apta para realização em algum dos
	 *         contextos de lote
	 * @see PJEII-848
	 */
	public Boolean aptoAtividadeLote() {
		if (idTask == null || idTarefa == null) {
			return false;
		}

		String key = idTask.toString() + "@" + idTarefa.toString();
		List<AtividadesLoteEnum> atividades = aptoAtividadeLoteMap.get(key);
		if (atividades == null) {
			atividades = getAtividadesLote();
			aptoAtividadeLoteMap.put(key, atividades);
		}
		return !atividades.isEmpty();
	}

	/**
	 * Recupera as atividades em lote que podem ser executadas na
	 * {@link TaskInstance} e {@link Tarefa} atuais.
	 * 
	 * @return a lista de atividades
	 */
	public List<AtividadesLoteEnum> getAtividadesLote() {
		if (atividadesHabilitadas == null) {
			atividadesHabilitadas = avaliaVariaveisFluxo(this.idTask, this.idTarefa);
		}
		return atividadesHabilitadas;
	}

	/**
	 * Verifica se a tarefa possui a variavel que habilita o lancamento de
	 * movimentos em lote.
	 * 
	 * @return true se sim, false se nao
	 */
	public Boolean aptoLancarMovimentoEmLote() {
		return getAtividadesLote().contains(AtividadesLoteEnum.MM);
	}

	/**
	 * Verifica quais operações em lote podem ser executadas na {@link TaskInstance}
	 * atual, quando combinada com a {@link Tarefa} respectiva.
	 * 
	 * @return a lista de atividades suportadas
	 * @see PJEII-848
	 */
	private List<AtividadesLoteEnum> avaliaVariaveisFluxo(Long idTask, Integer idTarefa) {
		List<AtividadesLoteEnum> ret = new ArrayList<AtividadesLoteEnum>();
		if (idTask == null || idTarefa == null) {
			return ret;
		}

		SituacaoProcesso situacaoProcesso = atividadesLoteService.getSituacaoProcessoByIdSituacaoIdTarefa(idTask,
				idTarefa);
		if (situacaoProcesso == null) {
			try {
				situacaoProcesso = atividadesLoteService.getSituacaoProcessoById(idTask);
			} catch (Exception e) {
				return ret;
			}
		}

		TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(situacaoProcesso.getIdTaskInstance());

		if (taskInstance == null) {
			return ret;
		}
		TaskController taskController = taskInstance.getTask().getTaskController();
		isUsandoMiniPac = Boolean.FALSE;

		if (taskController != null && taskController.getVariableAccesses() != null) {
			String[] tokens;
			for (VariableAccess var : taskController.getVariableAccesses()) {
				if (var.isReadable() && (var.getMappedName() != null)) {
					tokens = var.getMappedName().split(":");
					if ((tokens != null) && (tokens.length > 0)) {
						if (tokens[0].equals("movimentarLote")) {
							ret.add(AtividadesLoteEnum.M);
						}

						if (tokens[0].equals("minutarLote")) {
							ret.add(AtividadesLoteEnum.E);
						}

						if (tokens[0].equals("assinarLote")) {
							ret.add(AtividadesLoteEnum.A);
						}

						if (tokens[0].equals("assinarInteiroTeorLote")) {
							ret.add(AtividadesLoteEnum.T);
						}

						if (tokens[0].equals("lancadorMovimentoLote")) {
							ret.add(AtividadesLoteEnum.MM);
						} else if (tokens[0].equals("miniPAC")) {
							isUsandoMiniPac = Boolean.TRUE;
						}
					}
				}
				if (ret.size() == 4) {
					break;
				}
			}
		}

		if (isUsandoMiniPac && meiosIndicadosFluxo.isEmpty()) {
			String meiosFluxo = (String) taskInstance.getVariable(PreparaAtoComunicacaoAction.VAR_MEIOSCOMUNICACOES);
			if (StringUtil.isSet(meiosFluxo)) {
				for (int i = 0; i < meiosFluxo.split(",").length; i++) {
					meiosIndicadosFluxo.add(ExpedicaoExpedienteEnum.valueOf(meiosFluxo.split(",")[i]));
				}
			} else {
				meiosIndicadosFluxo.add(ExpedicaoExpedienteEnum.E);
				meiosIndicadosFluxo.add(ExpedicaoExpedienteEnum.P);
				meiosIndicadosFluxo.add(ExpedicaoExpedienteEnum.M);
			}
			prazoGeral = (String) taskInstance.getVariable(Variaveis.PJE_FLUXO_MINI_PAC_PRAZO_GERAL);
			if (!StringUtil.isSet(prazoGeral)) {
				prazoGeral = "15";
			}
		}
		return ret;
	}

	public Map<Integer, Selecionado> getProcessosMap() {
		return processosMap;
	}

	public void setProcessosMap(Map<Integer, Selecionado> processosMap) {
		this.processosMap = processosMap;
	}

	/**
	 * Identifica quais dos processos selecionados pelo usuário podem e não podem
	 * ser afetados à realização da atividade em lote selecionada.
	 */
	public void selecionarProcessosElegiveis() {
		processoListElegiveis.clear();
		processoNaoElegiveisListMap.clear();
		documentosProcesso.clear();
		acordaosCompilacoes.clear();
		arquivosAssinados.clear();
		meiosSelecionadosList.clear();
		polosSelecionadosList.clear();
		miniPacTempMap.clear();
		intimacaoPessoal = Boolean.FALSE;
		for (Entry<Long, ProcessoTrf> e : getProcessoListMap().entrySet()) {
			fluxoService.iniciarProcesso(e.getValue().getProcesso(), e.getKey());
			if (!verificaRestricoes(e.getKey(), e.getValue())) {
				if (!processoListElegiveis.contains(e.getKey())) {
					processoListElegiveis.add(e.getKey());
					if (tipoAtividade.equals(AtividadesLoteEnum.I)) {
						carregarUltimoAtoProferidoMap(e.getValue());
					}
				}
			}
		}
		Collections.sort(processoListElegiveis);
		log.debug("Processos selecionados com sucesso");
	}

	private void carregarUltimoAtoProferidoMap(ProcessoTrf processoTrf) {
		int idProcessoTrf = processoTrf.getIdProcessoTrf();
		ProcessoDocumento ultimoAtoProferido = null;
		if (parametroService.valueOf(PARAMETRO_UTILIZA_PARAMETRO_ATO_PROFERIDO) != null
				&& parametroService.valueOf(PARAMETRO_UTILIZA_PARAMETRO_ATO_PROFERIDO).equals("true")) {
			ultimoAtoProferido = buscarAtosProferidosParametro(processoTrf.getProcesso());
		} else {
			ultimoAtoProferido = processoDocumentoManager.getUltimoAtoProferido(idProcessoTrf);
		}
		if (ultimoAtoProferido == null) {
			msgIntimacaoMap.put(idProcessoTrf, MSG_NAO_EXISTE_ATO_PROFERIDO_NESTE_PROCESSO);
		} else {
			ultimoAtoProferidoMap.put(idProcessoTrf, ultimoAtoProferido);
		}
	}

	private ProcessoDocumento buscarAtosProferidosParametro(Processo processo) {
		ProcessoDocumento ultimoAtoProferido = null;
		String tipos = parametroService.valueOf(PARAMETRO_TIPOS_DOCUMENTO_ULTIMO_ATO);
		List<Integer> idsTiposDoc = Arrays.stream(tipos.split(",")).map(Integer::parseInt).collect(Collectors.toList());
		try {
			ultimoAtoProferido = processoDocumentoManager.getUltimoProcessoDocumento(
					ComponentUtil.getTipoProcessoDocumentoManager().findTiposIn(idsTiposDoc), processo);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return ultimoAtoProferido;
	}

	/**
	 * Avalia se a {@link TaskInstance} do processo dado é elegível para realização
	 * de atividade em lote. São condições de elegibilidade:
	 * <li>haver pelo menos uma transição de saída potencialmente utilizável</li>
	 * 
	 * @param idTask   o identificador da {@link TaskInstance}
	 * @param processo o processo judicial potencialmente objeto da operação em lote
	 * @return true, se ele é elegível
	 */
	private boolean verificaRestricoes(Long idTask, ProcessoTrf processo) {
		boolean retorno = false;
		TaskInstance ti = atividadesLoteService.getTaskInstanceById(idTask);

		TaskInstanceHome.instance().setTaskId(idTask);

		List<String> listaPendencias = new ArrayList<String>();
		carregaAgrupamento();
		// Se nao tiver transicao de saida nao deixa movimentar em lote
		if (!temTransicoes(ti, processo, listaPendencias)) {
			retorno = true;
		}
		switch (tipoAtividade) {
		case A:
			retorno = temRestricoesAssinarLote(ti, processo, listaPendencias);
			break;
		case E:
			retorno = temRestricoesMinutarLote(idTask, ti, processo, listaPendencias);
			break;
		case T:
			retorno = temRestricoesAssinarInteiroTeorLote(ti, processo, listaPendencias);
			break;
		default:
			retorno = temRestricoesMovimentacao(ti.getTask().getName(), processo, listaPendencias);
		}

		if (Actor.instance() != null) {
			if ((processo != null) && (processo.getProcesso() != null)) {
				String ultimoAcessarProcesso = processo.getProcesso().getActorId();
				String actorIdCorrente = Actor.instance().getId();

				if ((ultimoAcessarProcesso != null) && (actorIdCorrente != null)
						&& !ultimoAcessarProcesso.equalsIgnoreCase(actorIdCorrente)) {
					listaPendencias.add("Processo bloqueado pelo usuário " + ultimoAcessarProcesso);
					retorno = true;
				}
			}
		}

		if (retorno && !processoNaoElegiveisListMap.containsKey(idTask)) {
			processoNaoElegiveisListMap.put(idTask, listaPendencias);
		}

		return retorno;
	}

	private void carregaAgrupamento() {
		Integer agrupamento = atividadesLoteService.getAgrupamento(this.getTipoProcessoDocumentoSelecionado());

		if (agrupamento != null && agrupamento > 0) {
			this.agrupamento = agrupamento;
		}
	}

	private boolean temTransicoes(TaskInstance task, ProcessoTrf proc, List<String> pendencias) {
		// Se nao tiver transicao de saida nao deixa movimentar em lote
		if (getTransitions(task.getId()).isEmpty()) {
			pendencias.add("Processo não possui transições de saída");
			return false;
		}
		return true;
	}

	private boolean temRestricoesMovimentacao(String taskName, ProcessoTrf proc, List<String> pendencias) {
		// se nao tiver lancador de movimento em lote, verifica se
		// a tarefa possui lancamento de movimento manual. Se sim, nao permite
		// movimentar em lote
		boolean ret = false;
		if (agrupamento == null) {
			if (atividadesLoteService.hasEvents(proc, taskName)) {
				pendencias.add("Processo possui lançador de movimentos manual");
				ret = true;
			}
		}

		// Se tem o lancador de movimentos em lote,
		// Verifica se o processo ja tem um movimento lancado, se sim impede ele
		// executar em lote.
		if (agrupamento != null) {
			if (atividadesLoteService.existeMovimentoRegistrado(proc, taskName)) {
				ret = true;
				pendencias.add("Processo possui movimento(s) já lançado(s)");
			}
		}
		return ret;
	}

	private boolean temRestricoesMinutarLote(Long idTask, TaskInstance taskInstance, ProcessoTrf proc,
			List<String> pendencias) {
		// Tipo de Atividade: Execução em Lote (inclui Minutar em Lote)
		// verificar se existe uma minuta salva ou movimentos lançados
		// caso exista, o processo não é elegível.
		boolean ret = false;
		if (existeMinutaSalva(idTask)) {
			ret = true;
			pendencias.add("Processo possui uma minuta salva");
		}

		if (atividadesLoteService.existeMovimentoRegistrado(proc, taskInstance.getTask().getName())) {
			ret = true;
			pendencias.add("Processo possui movimento(s) já lançado(s)");
		}
		return ret;
	}

	private boolean temRestricoesAssinarInteiroTeorLote(TaskInstance taskInstance, ProcessoTrf proc,
			List<String> pendencias) {

		List<String> restricoes = new ArrayList<String>();

		if (atividadesLoteService.hasEvents(proc, taskInstance.getTask().getName())) {
			restricoes.add("Processo possui lançador de movimentos manual");
		}

		List<EventoBean> eventoBeanList = eventsLoteTree.getListaMovimentosNecessitamHomologacao(proc.getProcesso(),
				taskInstance.getId());
		for (EventoBean eventoBean : eventoBeanList) {
			if ((eventoBean.getTemComplemento() != null) && (eventoBean.getTemComplemento() == true)) {
				if (!eventoBean.getValido()) {
					restricoes.add("Processo possui complementos não preenchidos");
				}
			}
		}

		SessaoPautaProcessoTrf sppt = sessaoPautaProcessoTrfManager
				.getSessaoPautaProcessoTrfJulgado(ProcessoJbpmUtil.getProcessoTrf());

		if (sppt == null) {
			restricoes.add("Não foi possível recuperar a sessão de julgamento do processo!");
			return true;
		}

		AcordaoCompilacao acordaoCompilacao = sessaoPautaProcessoTrfManager.recuperarAcordaoCompilacao(sppt,
				taskInstance);

		List<String> restricoesParaParaAssinatura = acordaoCompilacao.verificarRestricoesParaParaAssinatura();

		if (restricoesParaParaAssinatura.isEmpty()) {

			List<ProcessoDocumento> docs = acordaoCompilacao.getProcessoDocumentosParaAssinatura();

			if (!docs.isEmpty()) {
				for (ProcessoDocumento pd : docs) {
					if (!atividadesLoteService.liberaCertificacao(pd)) {
						restricoes.add("Usuário não possui permissão para assinar o documento: "
								+ pd.getTipoProcessoDocumento().getTipoProcessoDocumento());
					}
				}
			} else {
				restricoes.add("Nenhum documento para assinatura!");
			}

			documentosProcesso.put(taskInstance.getId(), new HashSet<ProcessoDocumento>(docs));
		} else {
			restricoes.addAll(restricoesParaParaAssinatura);

			documentosProcesso.put(taskInstance.getId(), new HashSet<ProcessoDocumento>());
		}

		pendencias.addAll(restricoes);

		boolean temRestricoes = !restricoes.isEmpty();

		if (!temRestricoes) {
			acordaosCompilacoes.add(acordaoCompilacao);
		}

		return temRestricoes;
	}

	private boolean temRestricoesAssinarLote(TaskInstance task, ProcessoTrf proc, List<String> pendencias) {
		boolean ret = false;
		// Se não existir documento salvo, pronto para ser assinado
		if (!existeDocumentoProntoParaAssinatura(task.getId())) {
			ret = true;
			pendencias.add("Processo não possui documentos prontos para serem assinados");
		}

		if (atividadesLoteService.hasEvents(proc, task.getTask().getName())) {
			ret = true;
			pendencias.add("Processo possui lançador de movimentos manual");
		}

		ret = incluirDocumentosElegiveis(task, pendencias, ret);

		// Verifica se o processo possui movimentos sem complementos preenchidos
		List<EventoBean> eventoBeanList = eventsLoteTree.getListaMovimentosNecessitamHomologacao(proc.getProcesso(),
				task.getId());
		for (EventoBean eventoBean : eventoBeanList) {
			if ((eventoBean.getTemComplemento() != null) && (eventoBean.getTemComplemento() == true)) {
				if (!eventoBean.getValido()) {
					pendencias.add("Processo possui complementos não preenchidos");
					ret = true;
				}
			}
		}
		return ret;
	}

	/**
	 * Método preenche a lista de documentos elegiveis para assinar na taskInstance,
	 * inclui novas pendencias na lista de pendencias caso existam e indica
	 * restrição na assinatura caso já exista ou caso algum documento possua
	 * restrição de assinatura
	 * 
	 * @param taskInstance
	 * @param pendencias               lista de pendencias encontradas
	 * @param temRestricoesAssinarLote define que o metodo possui pendencias na
	 *                                 assinatura
	 * @return verdadeiro se já existe restrição ou se encontrar alguma restrição em
	 *         algum documento
	 */
	private boolean incluirDocumentosElegiveis(TaskInstance taskInstance, List<String> pendencias,
			boolean temRestricoesAssinarLote) {
		Set<ProcessoDocumento> documentosProntosParaAssinar = new HashSet<ProcessoDocumento>();
		documentosProntosParaAssinar
				.addAll(atividadesLoteService.getDocumentoProntoParaAssinatura(taskInstance.getId()));

		Set<ProcessoDocumento> documentosElegiveis = new HashSet<ProcessoDocumento>();

		for (ProcessoDocumento documentoProntoParaAssinar : documentosProntosParaAssinar) {
			if (!atividadesLoteService.liberaCertificacao(documentoProntoParaAssinar)) {
				pendencias.add("Usuário não possui permissão para assinar o documento: "
						+ documentoProntoParaAssinar.getTipoProcessoDocumento().getTipoProcessoDocumento());
				temRestricoesAssinarLote = true;
			} else {
				documentosElegiveis.add(documentoProntoParaAssinar);
			}
		}

		documentosProcesso.put(taskInstance.getId(), documentosElegiveis);

		return temRestricoesAssinarLote;
	}

	private boolean existeDocumentoProntoParaAssinatura(Long idTask) {
		return atividadesLoteService.existeDocumentoProntoParaAssinatura(idTask);
	}

	private boolean existeMinutaSalva(Long idTask) {
		return atividadesLoteService.verificaExistenciaDeMinuta(idTask);
	}

	public List<Long> getProcessoListMapKey() {
		List<Long> ret = new ArrayList<Long>();
		ret.addAll(getProcessoListMap().keySet());

		return ret;
	}

	public Boolean getNaoPodeIntimar() {
		return naoPodeIntimar;
	}

	public void setNaoPodeIntimar(Boolean naoPodeIntimar) {
		this.naoPodeIntimar = naoPodeIntimar;
	}

	public Boolean getPartePendente() {
		return partePendente;
	}

	public void setPartePendente(Boolean partePendente) {
		this.partePendente = partePendente;
	}

	public Boolean getPodeIntimar() {
		return podeIntimar;
	}

	public void setPodeIntimar(Boolean podeIntimar) {
		this.podeIntimar = podeIntimar;
	}

	public Map<Long, ProcessoTrf> getProcessoListMap() {
		return processoListMap;
	}

	public void setProcessoListMap(Map<Long, ProcessoTrf> processoListMap) {
		this.processoListMap = processoListMap;
	}

	public String getNomeTaskInstance() {
		return nomeTaskInstance;
	}

	public void setNomeTaskInstance(String nomeTaskInstance) {
		this.nomeTaskInstance = nomeTaskInstance;
	}

	public Map<Long, Transition> getTaskTransitionMap() {
		return taskTransitionMap;
	}

	public void setTaskTransitionMap(Map<Long, Transition> taskTransitionMap) {
		this.taskTransitionMap = taskTransitionMap;
	}

	public List<Transition> getTransitions(Long idTaskInstance) {
		ProcessoTrf processoTrf = getProcessoListMap().get(idTaskInstance);
		ProcessoTrfHome.instance().setId(processoTrf.getIdProcessoTrf());
		List<Transition> ret = atividadesLoteService
				.getAvaliableTransitions(atividadesLoteService.getTaskInstanceById(idTaskInstance));

		if (this.tipoAtividade == AtividadesLoteEnum.A || this.tipoAtividade == AtividadesLoteEnum.E
				|| this.tipoAtividade == AtividadesLoteEnum.T) {
			Transition frameDefaultLeavingTransition = atividadesLoteService
					.getFrameDefaultLeavingTransition(idTaskInstance);
			if (frameDefaultLeavingTransition != null
					&& !"#{true}".equals(frameDefaultLeavingTransition.getCondition())) {
				ret.add(frameDefaultLeavingTransition);
			}
			if (AtividadesLoteEnum.A.equals(this.tipoAtividade)) {
				taskTransitionMap.put(idTaskInstance, frameDefaultLeavingTransition);
			}
		}
		LinkedHashSet<Transition> aux = new LinkedHashSet<Transition>(ret);

		if (transicoesPossiveis.isEmpty()) {
			transicoesPossiveis.addAll(aux);
		} else {
			transicoesPossiveis.retainAll(aux);
		}
		return ret;
	}

	public void cancelar() {
		log.debug("Cancelada operação em lote.");
		this.agrupamento = null;
		this.modeloDocumento = null;
		eventsLoteTree.clearList();
		eventsLoteTree.clearTree();
	}

	public void obtemModeloDocumento() {
		modeloDocumento = atividadesLoteService.obtemModeloDocumento();
	}

	/**
	 * Verifica se ha minuta em aberto na lista dos processo elegiveis
	 * 
	 * @return resultado da verificação
	 */
	public boolean hasMinutaEmAberto() {
		boolean hasMinutaEmAberto = false;

		List<Entry<Long, Transition>> entrySetList = ordenaProcessosGrid();
		ProcessoTrf processo;
		Long taskId;
		for (Map.Entry<Long, Transition> entry : entrySetList) {
			if (!hasMinutaEmAberto) {
				taskId = entry.getKey();
				processo = processoListMap.get(taskId);
				if (processo != null) {
					hasMinutaEmAberto = hasMinutaEmAberto || atividadesLoteService.hasMinutaEmAberto(taskId, processo);
					if (hasMinutaEmAberto) {
						break;
					}
				}
			}
		}
		return hasMinutaEmAberto;
	}

	public void carregarMeiosComunicacao() {
		meios.clear();
		meios.add(ExpedicaoExpedienteEnum.E);
		if (this.conectorDJEDisponivel) {
			meios.add(ExpedicaoExpedienteEnum.P);
		}
		meios.add(ExpedicaoExpedienteEnum.C);
	}

	public void onChangeMeioComunicacao() {
		tiposParteIntimacao.clear();
		tiposPolosDisponiveis.clear();
		if(Objects.isNull(meios)) {
			meios = new ArrayList<>();
		} else {
			meios.clear();
		}

		if (this.tipoComunicacao.getTipoProcessoDocumento().equals(TipoProcessoDocumentoEnum.C.getLabel())) {
			meios.add(ExpedicaoExpedienteEnum.E);
			meios.add(ExpedicaoExpedienteEnum.C);

			tiposPolosDisponiveis.add(POLO_PASSIVO);
			tiposParteIntimacao.add(POLO_PASSIVO);
			StringJoiner joiner = new StringJoiner(", ");
			for (String str : tiposPolosDisponiveis) {
				joiner.add(str);
			}
			tiposPolosDisponiveisString = joiner.toString();

		} else {
			meios.add(ExpedicaoExpedienteEnum.E);
			if (this.conectorDJEDisponivel) {
				meios.add(ExpedicaoExpedienteEnum.P);
			}
			limpaTiposPolo();
		}

		onChangeMeioIntimacao();

	}

	private void limpaTiposPolo() {
		tiposParteIntimacao.clear();
		String[] split = TIPOS_DE_POLO.split(",");
		tiposPolosDisponiveis.addAll(Arrays.asList(split));
		tiposParteIntimacao.addAll(Arrays.asList(split));
		tiposPolosDisponiveisString = TIPOS_DE_POLO;
	}

	public void onChangeConteudoIntimacao() {
		ProcessoHome.instance().setTipoProcessoDocumento(null);
		limpaTiposPolo();
		setTipoComunicacao(null);
		setModeloDocumento(null);
		carregarMeiosComunicacao();
		if (ultimoAtoProferido) {
			for (Long idTask : processoListElegiveis) {

				carregarUltimoAtoProferidoMap(getProcessoListMap().get(idTask));

				if (processosSelecionadosCheck.get(idTask) != null && processosSelecionadosCheck.get(idTask) == true) {

					TaskInstance ti = atividadesLoteService.getTaskInstanceById(idTask);
					Integer idDocEmElaboracao = (Integer) ti.getVariableLocally(Variaveis.INTIMACAO_EM_ELABORACAO);
					removerDocumento(idDocEmElaboracao);
				}
			}
		} else {

			for (Long idTask : processoListElegiveis) {
				ProcessoTrf processo = processoListMap.get(idTask);
				if (processo != null) {
					if (msgIntimacaoMap.get(processo.getIdProcessoTrf()) != null && msgIntimacaoMap
							.get(processo.getIdProcessoTrf()).equals(MSG_NAO_EXISTE_ATO_PROFERIDO_NESTE_PROCESSO)) {
						msgIntimacaoMap.remove(processo.getIdProcessoTrf());
					}
				}

			}
			
		}

	}

	private boolean eventosPreenchidos() {
		if (ultimoAtoProferido) {
			return true;
		} else {
			boolean lancadorDeMovimentosExibido = possuiAgrupamentos()
					&& isAgrupamentoPossuiMovimentos(this.agrupamento);
			if ((lancadorDeMovimentosExibido) && (eventsLoteTree.getEventoBeanList().size() == 0)) {
				facesMessages.clear();
				facesMessages.add(Severity.ERROR, "É necessário lançar ao menos 1 movimento");
				setConteudoAlterado(true);
				return false;
			}
			return true;
		}
	}

	public void intimarEmLote() {
		List<Integer> docsParaRemocao = new ArrayList<Integer>();
		for (Entry<Long, ProcessoTrf> e : getProcessoListMap().entrySet()) {
			if (processosSelecionadosCheck.get(e.getKey()) != null
					&& processosSelecionadosCheck.get(e.getKey()) == true) {
				try {
					ProcessoTrf processoTrf = e.getValue();
					fluxoService.iniciarHomesProcessos(processoTrf);

					TaskInstance ti = atividadesLoteService.getTaskInstanceById(e.getKey());
					Integer idDocEmElaboracao = (Integer) ti.getVariableLocally(Variaveis.INTIMACAO_EM_ELABORACAO);
					if (idDocEmElaboracao != null) {
						docsParaRemocao.add(idDocEmElaboracao);
					}

					if (ultimoAtoProferido) {
						ProcessoDocumento intimacao = ultimoAtoProferidoMap.get(processoTrf.getIdProcessoTrf());

						if (intimacao != null) {

							String extensao = intimacao.getProcessoDocumentoBin().getExtensao();

							if (extensao.equals("application/pdf")
									&& this.meioIntimacao.equals(ExpedicaoExpedienteEnum.E)) {

								msgIntimacaoMap.put(processoTrf.getIdProcessoTrf(),
										"Expediente não gerado, pois não é possível publicar no DJE. O último ato proferido é um PDF.");

							} else {

								List<ProcessoParte> partesParaIntimcacao = new ArrayList<>();

								List<ProcessoDocumento> documentosVinculados = new ArrayList<>();
								if (this.tipoComunicacao.getTipoProcessoDocumento()
										.equals(TipoProcessoDocumentoEnum.C.getLabel())
										&& this.meioIntimacao == ExpedicaoExpedienteEnum.E) {

									documentosVinculados.add(processoDocumentoManager.getUltimoProcessoDocumento(
											ComponentUtil.getTipoProcessoDocumentoManager().findById(idPeticaoInicial),
											processoTrf.getProcesso()));
								}

								TipoProcessoDocumentoEnum tipoEnum = this.tipoComunicacao.getTipoProcessoDocumento()
										.equals(TipoProcessoDocumentoEnum.C.getLabel()) ? TipoProcessoDocumentoEnum.C
												: TipoProcessoDocumentoEnum.I;
								partesParaIntimcacao = verificaPartesAptas(processoTrf);
								if (!partesParaIntimcacao.isEmpty()) {
									if (this.tipoComunicacao.getTipoProcessoDocumento()
											.equals(TipoProcessoDocumentoEnum.C.getLabel())
											&& this.meioIntimacao == ExpedicaoExpedienteEnum.C) {
										tramitacaoProcessualService.gravaVariavel("pje:intimacaoEmLote:linkAtoProferido", gerarLinkUltimoAtoProferido(processoTrf));
										Integer idMinutaCitacao = documentoJudicialService.gerarMinuta(processoTrf.getIdProcessoTrf(), null, null, Integer.parseInt(parametroService.valueOf(PARAMETRO_ID_OUTROS_ANEXOS)), Integer.parseInt(parametroService.valueOf(PARAMETRO_ID_MODELO_CITACAO_ATO_PROFERIDO)));										

										documentoJudicialService.juntarDocumento(idMinutaCitacao, ti.getId(),
												ti.getProcessInstance().getId());
										ProcessoDocumento pd = documentoJudicialService.getDocumento(idMinutaCitacao);
										documentosVinculados.add(pd);
										

										criarIntimacoes(processoTrf, intimacao, partesParaIntimcacao, true, tipoEnum,
												documentosVinculados);

										lancarMovimentoExpedicaoAR();
				
							

									} else {
										criarIntimacoes(processoTrf, intimacao, partesParaIntimcacao, true, tipoEnum,
												documentosVinculados);
										lancarMovimentoExpedicaoDocumentos();		

									}
								}
								
								fluxoService.iniciarBusinessProcess(e.getKey());
								movimentarProcesso(e.getKey(), processoTrf, ti);
							}
						}

					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			removerDocumentos(docsParaRemocao);
			setIntimacaoEmLoteFinalizada(true);
		}
	}

	
	private String gerarLinkUltimoAtoProferido(ProcessoTrf processoJudicial) {
		String link = "";
		try {
			String tipos = parametroService.valueOf(PARAMETRO_TIPOS_DOCUMENTO_ULTIMO_ATO);
			List<Integer> idsTiposDoc = Arrays.stream(tipos.split(",")).map(Integer::parseInt).collect(Collectors.toList());
			ProcessoDocumento ultimoAtoProferido = ProcessoDocumentoManager.instance().getUltimoProcessoDocumento(
					ComponentUtil.getTipoProcessoDocumentoManager().findTiposIn(idsTiposDoc), processoJudicial.getProcesso());
			StringBuilder url = new StringBuilder();
			url.append(new br.com.itx.component.Util().getUrlProject() + "/Processo/ConsultaDocumento/listView.seam");
			
				
			url.append("?x=" + ValidacaoAssinaturaProcessoDocumento.instance().getCodigoValidacaoDocumento(ultimoAtoProferido.getProcessoDocumentoBin()));
			link = url.toString();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return link;	
	}
	

	private void lancarMovimentoExpedicaoDocumentos() {
		MovimentoAutomaticoService.preencherMovimento().deCodigo(60).comComplementoDeCodigo(4).doTipoDominio()
				.preencherComElementoDeCodigo(80).lancarMovimento();
	}

	private void lancarMovimentoExpedicaoAR() {
		MovimentoAutomaticoService.preencherMovimento().deCodigo(60).comComplementoDeCodigo(4).doTipoDominio()
				.preencherComElementoDeCodigo(74).lancarMovimento();
	}
	
	
	private void removerDocumentos(List<Integer> docsParaRemocao) {
		for (Integer idDocRemocao : docsParaRemocao) {
			removerDocumento(idDocRemocao);
		}
	}

	private void removerDocumento(Integer idDoc) {
		if (idDoc != null) {
			ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class, idDoc);
			if (pd != null) {

				try {
					ComponentUtil.getProcessoDocumentoHome().removerDocumentoSemTratamentoDeErro(pd,
							pd.getProcessoTrf().getIdProcessoTrf());
				} catch (PJeBusinessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void gravarDocumentoEmLote() {
		documentosProcesso.clear();
		intimacoes.clear();
		if (eventosPreenchidos()) {
			for (Entry<Long, ProcessoTrf> e : getProcessoListMap().entrySet()) {
				if (processosSelecionadosCheck.get(e.getKey()) != null
						&& processosSelecionadosCheck.get(e.getKey()) == true) {
					try {
						Long taskId = e.getKey();
						fluxoService.iniciarHomesProcessos(e.getValue());
						TaskInstance ti = atividadesLoteService.getTaskInstanceById(taskId);
						
					LancadorMovimentosService.instance().setMovimentosTemporarios(ti.getProcessInstance(),
								eventsLoteTree.getEventoBeanList());
						Integer idDocEmElaboracao = (Integer) ti.getVariableLocally(Variaveis.INTIMACAO_EM_ELABORACAO);
							
									
						ProcessoDocumento intimacao = atividadesLoteService.juntarDocumentoAoProcesso(taskId,
								modeloDocumento, e.getValue(), ProcessoHome.instance(), idDocEmElaboracao);
						
						if(!(this.tipoComunicacao.getTipoProcessoDocumento().equals(TipoProcessoDocumentoEnum.C.getLabel()) && this.meioIntimacao == ExpedicaoExpedienteEnum.C)) {		
							LancadorMovimentosService.instance().setMovimentosTemporarios(ti.getProcessInstance(),
									MovimentoAutomaticoService.preencherMovimento().deCodigo(60).comComplementoDeCodigo(4)
											.doTipoDominio().preencherComElementoDeCodigo(80));
						}	else {
							LancadorMovimentosService.instance().setMovimentosTemporarios(ti.getProcessInstance(),
									MovimentoAutomaticoService.preencherMovimento().deCodigo(60).comComplementoDeCodigo(4)
											.doTipoDominio().preencherComElementoDeCodigo(74));
						  
						}		
				

						ti.setVariableLocally(Variaveis.INTIMACAO_EM_ELABORACAO, intimacao.getIdProcessoDocumento());
						List<ProcessoDocumento> listPd = new ArrayList<ProcessoDocumento>();
						listPd.add(intimacao);
						intimacoes.add(intimacao);
						documentosProcesso.put(taskId, new HashSet<ProcessoDocumento>(listPd));
						setConteudoAlterado(false);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
	
	public void assinarEIntimarEmLote() {
    	if (eventosPreenchidos()) {
	    	for (Entry<Long, List<ArquivoAssinadoHash>> entry : arquivosAssinados.entrySet()) {
	    		for (ArquivoAssinadoHash as : entry.getValue()) {
	    			try {
	    				int idProcessoDocumento = Integer.parseInt(as.getId());
						ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class, idProcessoDocumento);
						ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, pd.getProcesso().getIdProcesso());
						for (Entry<Long, ProcessoTrf> e: getProcessoListMap().entrySet()) {
							 
				    		if(processosSelecionadosCheck.get(e.getKey()) != null && processosSelecionadosCheck.get(e.getKey()) == true) {

								if (e.getValue().getIdProcessoTrf() == processoTrf.getIdProcessoTrf()) {
									TaskInstance ti = atividadesLoteService.getTaskInstanceById(e.getKey());
									fluxoService.iniciarBusinessProcess(e.getKey());
	
									//verifica processo criminal				
								    String numeroProcedimentoOrigem = buscaInformacaoProcessoCriminal(processoTrf);
									if(this.meioIntimacao.equals(ExpedicaoExpedienteEnum.E) && numeroProcedimentoOrigem != null && pd.getTipoProcessoDocumento().getTipoProcessoDocumento().equals(TipoProcessoDocumentoEnum.I.getLabel())){
										pd.setJson(numeroProcedimentoOrigem);
									}
									
									List<ProcessoDocumento> documentosVinculados = new ArrayList<>();

									LancadorMovimentosService.instance().lancarMovimentosTemporariosAssociandoAoDocumento(ti.getProcessInstance(), pd);
								 	
									if(this.tipoComunicacao.getTipoProcessoDocumento().equals(TipoProcessoDocumentoEnum.C.getLabel()) && this.meioIntimacao == ExpedicaoExpedienteEnum.C) {
										Integer idMinutaCitacao = documentoJudicialService.gerarMinuta(processoTrf.getIdProcessoTrf(), null, null, Integer.parseInt(parametroService.valueOf(PARAMETRO_ID_OUTROS_ANEXOS)), Integer.parseInt(parametroService.valueOf(PARAMETRO_ID_MODELO_CITACAO)));										
										ProcessoDocumento outrosAnexos = documentoJudicialService.getDocumento(idMinutaCitacao);
										outrosAnexos.setDocumentoPrincipal(pd);
										
										documentoJudicialService.persist(outrosAnexos, pd.getProcessoTrf(), true);
										documentosVinculados.add(outrosAnexos);
											
										pd.getDocumentosVinculados().add(outrosAnexos);
										documentoJudicialService.juntarDocumento(idMinutaCitacao, ti.getId(), ti.getProcessInstance().getId());	 
										 
									 }
									if(this.tipoComunicacao.getTipoProcessoDocumento().equals(TipoProcessoDocumentoEnum.C.getLabel()) && this.meioIntimacao == ExpedicaoExpedienteEnum.E) {						
										documentosVinculados.add(processoDocumentoManager.getUltimoProcessoDocumento(
											ComponentUtil.getTipoProcessoDocumentoManager().findById(idPeticaoInicial),
											processoTrf.getProcesso()));
									}
									 documentoJudicialService.gravarAssinatura(as.getId(), as.getCodIni(), as.getHash(), as.getAssinatura(), as.getCadeiaCertificado(), Authenticator.getPessoaLogada());
									pd.setDataJuntada(new Date());
									
									EntityUtil.flush();	
									
									List<ProcessoParte> partesParaIntimcacao = new ArrayList<>();
									
									partesParaIntimcacao =	verificaPartesAptas(processoTrf); 	
									
									
									if (!partesParaIntimcacao.isEmpty()) {
										criarIntimacoes(processoTrf, pd, partesParaIntimcacao, false, null, documentosVinculados);
									} 
									movimentarProcesso(e.getKey(), processoTrf, ti);
									break;
								}
					    	}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
	    		}
	    	}
	    	facesMessages.clear();
	    	setIntimacaoEmLoteFinalizada(true);
		}
    }



	private String buscaInformacaoProcessoCriminal(ProcessoTrf processoTrf) {
		String numeroProcedimentoOrigem = null;
		if (br.com.infox.cliente.Util.listaContem(idsCompetenciasCriminais,
				String.valueOf(processoTrf.getCompetencia().getIdCompetencia()))) {
			ProcessoCriminalRestClient processoCriminalRestClient = ComponentUtil
					.getComponent(ProcessoCriminalRestClient.class);
			if (processoCriminalRestClient != null) {
				try {
					ProcessoCriminalDTO processoCriminalDTO = processoCriminalRestClient
							.getResourceByProcesso(processoTrf.getNumeroProcesso());
					if (processoCriminalDTO != null) {
						if (processoCriminalDTO.getProcessoProcedimentoOrigemList().size() > 0) {
							String numeroPPO = processoCriminalDTO.getProcessoProcedimentoOrigemList().get(0)
									.getNumero();
							String anoPPO = processoCriminalDTO.getProcessoProcedimentoOrigemList().get(0).getAno()
									.toString();
							numeroProcedimentoOrigem = "{".concat("\"").concat("NumeroProcedimentoOrigem").concat("\"")
									.concat(": ").concat("\"").concat(numeroPPO).concat("/").concat(anoPPO).concat("\"")
									.concat("}");
						}
					}
				} catch (PJeException e) {
					e.printStackTrace();
				}
			}
		}
		return numeroProcedimentoOrigem;
	}

	private void movimentarProcesso(Long taskId, ProcessoTrf processoTrf, TaskInstance ti) {
		try {
			ti.deleteVariableLocally(Variaveis.INTIMACAO_EM_ELABORACAO);
			Object defaultTransition = taskInstanceUtil.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
			if (defaultTransition != null) {
				boolean possuiParteComPendencia = msgIntimacaoMap.get(processoTrf.getIdProcessoTrf()) != null
						&& (msgIntimacaoMap.get(processoTrf.getIdProcessoTrf()).equals(MSG_PARTES_SEM_INTIMAR)
								|| msgIntimacaoMap.get(processoTrf.getIdProcessoTrf())
										.equals(MSG_NENHUMA_PARTE_INTIMACAO)
								|| msgIntimacaoMap.get(processoTrf.getIdProcessoTrf())
										.equals(MSG_NENHUMA_PARTE_CITACAO));
				atividadesLoteService.movimentarAposIntimar(taskId, defaultTransition.toString(), processoTrf, null,
						possuiParteComPendencia);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void criarIntimacoes(ProcessoTrf processoTrf, ProcessoDocumento pd,
			List<ProcessoParte> partesParaIntimcacao, Boolean isProcDocExistente,
			TipoProcessoDocumentoEnum tipoProcessoDocumento, List<ProcessoDocumento> documentosVinculados) {
		int prazoInformado = prazoIntimacao != null ? prazoIntimacao : 0;

		Collection<ProcessoExpediente> processosExp = atoComunicacaoService.criarAtosComunicacao(
				processoTrf, pd, atoComunicacaoService.recuperaInformacoesPartes(partesParaIntimcacao,
						this.meioIntimacao, prazoInformado),
				documentosVinculados, isProcDocExistente, tipoProcessoDocumento);
		List<ProcessoExpediente> expedientes = new ArrayList<>(processosExp);
		tramitacaoProcessualService.gravaVariavel("tjrj:fluxo:intimacaoLote:expedientes", expedientes);
		
		List<Object> idsExpedientes = new ArrayList<>();
		for (ProcessoExpediente pe : processosExp) {
			idsExpedientes.add(pe.getIdProcessoExpediente());
			
		}
        tramitacaoProcessualService.gravaVariavel(Variaveis.VARIAVEL_EXPEDIENTE, StringUtil.concatList(CollectionUtils.collect(processosExp, new BeanToPropertyValueTransformer("idProcessoExpediente")), ","));

		if (this.meioIntimacao == ExpedicaoExpedienteEnum.P) {
			publicaDJE(processosExp, processoTrf);
		}
	}

	

	private void publicaDJE(Collection<ProcessoExpediente> processosExp, ProcessoTrf processoTrf) {
		tramitacaoProcessualService.gravaVariavel("tjrj:fluxo:origemParaDJE", "minipac");
		for (ProcessoExpediente pe : processosExp) {
			pe.getProcessoTrf().getNumeroProcesso();
			comunicacaoProcessualAction.enviarExpedientesDJE(pe);
			errosDJE = (String) tramitacaoProcessualService.recuperaVariavel(VARIAVEL_ERROS_CONECTOR_DJE);
			if (errosDJE != null) {
				msgIntimacaoMap.put(processoTrf.getIdProcessoTrf(),
						"Não foi possível publicar o processo no DJE, o processo foi encaminhado a tarefa 'Tratar expedientes não enviados ao diário oficial [RPD]'");
				log.error("Erro ao publicar processo:{1} no DJE intimação em lote {0}", errosDJE,
						processoTrf.getNumeroProcesso());
			}
		}

	}

	// Diário
	private List<ProcessoParte> getPartesAptasParaIntimacaoDiario(ProcessoTrf processoTrf) {
		List<ProcessoParte> partesParaIntimar = new ArrayList<ProcessoParte>();
		if (tiposParteIntimacao.contains(POLO_ATIVO)) {
			partesParaIntimar
					.addAll(getPartesAptasParaIntimacaoDiario(processoTrf.getProcessoPartePoloAtivoSemAdvogadoList()));
		}
		if (tiposParteIntimacao.contains(POLO_PASSIVO)) {
			partesParaIntimar.addAll(
					getPartesAptasParaIntimacaoDiario(processoTrf.getProcessoPartePoloPassivoSemAdvogadoList()));
		}
		if (tiposParteIntimacao.contains(POLO_TERCEIROS)) {
			partesParaIntimar.addAll(
					getPartesAptasParaIntimacaoDiario(processoTrf.getProcessoOutrosParticipantesSemAdvogadoList()));
		}
		if (partesParaIntimar.isEmpty()) {
			msgIntimacaoMap.put(processoTrf.getIdProcessoTrf(), MSG_NENHUMA_PARTE_INTIMACAO);
		}
		return partesParaIntimar;
	}

	private List<ProcessoParte> getPartesAptasParaIntimacaoDiario(List<ProcessoParte> partes) {
		List<ProcessoParte> partesParaIntimar = new ArrayList<ProcessoParte>();
		for (ProcessoParte processoParte : partes) {
			// valida de acordo com o parametro configurado, caso o parametro seja false ou
			// não esteja configurado não verifica se tem advogado
			if (!parametroValidaCadastroAdvogadoDiario) {
				partesParaIntimar.add(processoParte);
			} else {
				if (!processoParte.getProcessoParteRepresentanteListAtivos().isEmpty()) {
					partesParaIntimar.add(processoParte);
				} else {
					int idProcessoTrf = processoParte.getProcessoTrf().getIdProcessoTrf();
					msgIntimacaoMap.put(idProcessoTrf, MSG_PARTES_SEM_INTIMAR);
				}
			}
		}
		return partesParaIntimar;
	}

	private List<ProcessoParte> getPartesAptasParaIntimacao(ProcessoTrf processoTrf) {
		List<ProcessoParte> partesParaIntimar = new ArrayList<ProcessoParte>();
		if (tiposParteIntimacao.contains(POLO_ATIVO)) {
			partesParaIntimar
					.addAll(getPartesAptasParaIntimacao(processoTrf.getProcessoPartePoloAtivoSemAdvogadoList()));
		}
		if (tiposParteIntimacao.contains(POLO_PASSIVO)) {
			partesParaIntimar
					.addAll(getPartesAptasParaIntimacao(processoTrf.getProcessoPartePoloPassivoSemAdvogadoList()));
		}
		if (tiposParteIntimacao.contains(POLO_TERCEIROS)) {
			partesParaIntimar
					.addAll(getPartesAptasParaIntimacao(processoTrf.getProcessoOutrosParticipantesSemAdvogadoList()));
		}
		if (partesParaIntimar.isEmpty()) {
			msgIntimacaoMap.put(processoTrf.getIdProcessoTrf(), MSG_NENHUMA_PARTE_INTIMACAO);
		}
		return partesParaIntimar;
	}

	private List<ProcessoParte> getPartesAptasParaIntimacao(List<ProcessoParte> partes) {
		List<ProcessoParte> partesParaIntimar = new ArrayList<ProcessoParte>();
		for (ProcessoParte processoParte : partes) {
			if(atoComunicacaoService.verificarPossibilidadeIntimacaoEletronica(processoParte, false, false, true)) {
				partesParaIntimar.add(processoParte);
			} else {
				int idProcessoTrf = processoParte.getProcessoTrf().getIdProcessoTrf();
				msgIntimacaoMap.put(idProcessoTrf, MSG_PARTES_SEM_INTIMAR);
			}
		}
		return partesParaIntimar;
	}

	// Citações
	private List<ProcessoParte> getPartesAptasParaCitacao(ProcessoTrf processoTrf) {
		List<ProcessoParte> partesParaIntimar = new ArrayList<ProcessoParte>();

		if (tiposParteIntimacao.contains(POLO_PASSIVO)) {
			List<ProcessoParte> partesPoloPassivo = new ArrayList<ProcessoParte>();
			for (ProcessoParte parte : processoTrf.getProcessoPartePoloPassivoSemAdvogadoList()) {
				if (parte.getPessoa().getTipoPessoaResumidoAsString().equals("Pessoa Jurídica")
						&& atoComunicacaoService.isPodeSerIntimadoEletronicamente(parte, false)
						&& this.meioIntimacao.equals(ExpedicaoExpedienteEnum.E)) {
					partesPoloPassivo.add(parte);
				}
				if (parte.getPessoa().getTipoPessoaResumidoAsString().equals("Pessoa Física")
						&& !parte.getIsEnderecoDesconhecido() && this.meioIntimacao.equals(ExpedicaoExpedienteEnum.C)) {
					partesPoloPassivo.add(parte);
				}
			}
			partesParaIntimar.addAll(partesPoloPassivo);

		}

		if (partesParaIntimar.isEmpty()) {
			msgIntimacaoMap.put(processoTrf.getIdProcessoTrf(), MSG_NENHUMA_PARTE_CITACAO);
		}
		if (partesParaIntimar.size() != 0
				&& partesParaIntimar.size() != processoTrf.getProcessoPartePoloPassivoSemAdvogadoList().size()) {
			msgIntimacaoMap.put(processoTrf.getIdProcessoTrf(), MSG_PARTES_SEM_CITAR);
		}
		return partesParaIntimar;
	}

	

	public void minutarLote() {
		int numeroDocumentosInseridos = 0;

		// verificar se o lançador de complementos foi devidamente preenchido
		// 1. verificar se o lançador de movimentos foi exibido na tela
		boolean lancadorDeMovimentosExibido = possuiAgrupamentos() && isAgrupamentoPossuiMovimentos(this.agrupamento);

		// 2. se o lançador de movimentos foi exibido, é obrigatório colocar ao menos um
		// movimento.
		if ((lancadorDeMovimentosExibido) && (eventsLoteTree.getEventoBeanList().size() == 0)) {
			facesMessages.add(Severity.ERROR, "É necessário lançar ao menos 1 movimento");
		} else if (eventsLoteTree.validarComplementos() == true) {
			if (isUsandoMiniPac) {
				sincronizarVariavelVOContexto();
			}
			// ordena transições de saída
			List<Entry<Long, Transition>> entrySetList = ordenaProcessosGrid();

			// Cria registro do lote para fins de histórico
			HistoricoMovimentacaoLote historicoMovimentacao = atividadesLoteService
					.adicionaHistoricoMovimentacao(tipoAtividade.getLabel());

			Long taskId;
			Transition transit;
			ProcessoTrf processoTrf;
			TaskInstance ti;
			ProcessoJudicialManager processoJudicialManager = ComponentUtil.getComponent(ProcessoJudicialManager.class);
			HistoricoMovimentacaoLoteManager historicoMovimentacaoLoteManager = ComponentUtil
					.getComponent(HistoricoMovimentacaoLoteManager.class);

			// usado para limpar o escopo para cada processo (bug: o mesmo texto era gerado
			// para varios processos)
			Set<String> els = getElsModelo(modeloDocumento);

			// efetivamente transita o processo entre nós
			for (Map.Entry<Long, Transition> entry : entrySetList) {
				taskId = entry.getKey();
				transit = entry.getValue();
				processoTrf = processoListMap.get(taskId);
				try {
					// Para assegurar que está gerenciado
					processoTrf = processoJudicialManager.findById(processoTrf.getIdProcessoTrf());
					historicoMovimentacao = historicoMovimentacaoLoteManager
							.findById(historicoMovimentacao.getIdHistoricoMovimentacaoLote());
					ti = atividadesLoteService.getTaskInstanceById(taskId);
					atividadesLoteService.minutarEMovimentarProcesso(taskId, modeloDocumento, processoTrf,
							transit.getId(), historicoMovimentacao);
					eventsLoteTree.sincronizarEventoBeanListContexto(eventsLoteTree.getEventoBeanList(),
							ti.getProcessInstance());
					numeroDocumentosInseridos++;

					for (String ct : els) {
						Contexts.getConversationContext().remove(ct);
					}
				} catch (Throwable e) {
					facesMessages.add(Severity.ERROR, "Erro ao salvar minuta de documento para o processo {0}",
							processoTrf.getNumeroProcesso());
					log.error("Erro ao salvar minuta de documento para o processo {0}: {1}", e,
							processoTrf.getNumeroProcesso(), e.getMessage());
				}
			}
			facesMessages.add(Severity.INFO, "{0} processo(s) encaminhado(s) e minuta(s) salva(s) com sucesso.",
					numeroDocumentosInseridos);
			log.debug("{0} processo(s) encaminhado(s) e minuta(s) salva(s) com sucesso", numeroDocumentosInseridos);
		}
	}

	private Set<String> getElsModelo(String modeloDocumento) {
		Pattern p = Pattern.compile("#\\{[\\w\\d \\.]+\\}");
		Matcher m = p.matcher(modeloDocumento);
		Set<String> els = new HashSet<String>(0);
		while (m.find()) {
			String el = m.group();
			el = el.replace("#{", "").replace("}", "").trim();
			String action = el;
			if (action.contains(".")) {
				action = action.substring(0, action.indexOf('.'));
			}

			els.add(action);
		}
		return els;
	}

	public void movimentarLote_() {
		int cont = 0;
		boolean exigeMovimentos = possuiAgrupamentos() && isAgrupamentoPossuiMovimentos(this.agrupamento);
		boolean transicaoValidada = true;

		List<Entry<Long, Transition>> entries = ordenaProcessosGrid();

		if (exigeMovimentos && eventsLoteTree.getEventoBeanList().isEmpty()) {
			TramitacaoProcessualService tramitacaoProcessualService = ComponentUtil
					.getComponent(TramitacaoProcessualImpl.class);
			for (Map.Entry<Long, Transition> entry : entries) {
				String transition = atividadesLoteService.getTransitionById(entry.getValue().getId(),
						atividadesLoteService.getTaskInstanceById(entry.getKey())).getName();
				if (!tramitacaoProcessualService.isTransicaoDispensaRequeridos(transition)) {
					ProcessoTrf processoTrf = processoListMap.get(entry.getKey());
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"É necessário lançar ao menos 1 movimento para movimentar o processo " + processoTrf,
							"Erro"));
					transicaoValidada = false;
					return;
				}
			}
		}

		if (transicaoValidada || eventsLoteTree.validarComplementos()) {
			int total = entries.size();
			Map<Long, Map<String, Object>> dadosMovimentacoes = new HashMap<Long, Map<String, Object>>(total);
			Map<String, Object> data;
			for (Entry<Long, Transition> e : entries) {
				data = new HashMap<String, Object>(2);
				data.put("idProcesso", processoListMap.get(e.getKey()).getIdProcessoTrf());
				data.put("idTransition", e.getValue().getId());
				dadosMovimentacoes.put(e.getKey(), data);
				cont++;
			}
			try {
				HistoricoMovimentacaoLote hist = atividadesLoteService
						.adicionaHistoricoMovimentacao(tipoAtividade.getLabel());
				Conversation conv = Conversation.instance();
				ProcessoJudicialManager processoJudicialManager = ComponentUtil
						.getComponent(ProcessoJudicialManager.class);
				processoJudicialManager.flush();
				Events.instance().raiseAsynchronousEvent("MOVIMENTAR_LOTE", conv.getId(),
						hist.getIdHistoricoMovimentacaoLote(), dadosMovimentacoes);
			} catch (PJeBusinessException e1) {
				e1.printStackTrace();
			}
		}
		log.info("{0} processo{1} encaminhado{1} para movimentação.", cont, cont > 0 ? "s" : "");
		facesMessages.add(Severity.INFO, "{0} processo{1} encaminhado{1} com sucesso.", cont, cont > 0 ? "s" : "");
		limparProcessosSelecionados();
	}

	public void movimentarLote() {
		int numeroTransicoesSucesso = 0;

		// verificar se o lançador de complementos foi devidamente preenchido
		// 1. verificar se o lançador de movimentos foi exibido na tela
		boolean lancadorDeMovimentosExibido = possuiAgrupamentos() && isAgrupamentoPossuiMovimentos(this.agrupamento);
		boolean transicaoValidada = true;

		List<Entry<Long, Transition>> entrySetList = ordenaProcessosGrid();

		// 2. se o lançador de movimentos foi exibido, é obrigatório colocar ao menos um
		// movimento.
		if ((lancadorDeMovimentosExibido) && (eventsLoteTree.getEventoBeanList().isEmpty())) {
			TramitacaoProcessualService tramitacaoProcessualService = ComponentUtil
					.getComponent(TramitacaoProcessualImpl.class);
			for (Map.Entry<Long, Transition> entry : entrySetList) {
				String transition = atividadesLoteService.getTransitionById(entry.getValue().getId(),
						atividadesLoteService.getTaskInstanceById(entry.getKey())).getName();
				if (!tramitacaoProcessualService.isTransicaoDispensaRequeridos(transition)) {
					ProcessoTrf processoTrf = processoListMap.get(entry.getKey());
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"É necessário lançar ao menos 1 movimento para movimentar o processo " + processoTrf,
							"Erro"));
					transicaoValidada = false;
					return;
				}
			}
		}

		if (transicaoValidada || eventsLoteTree.validarComplementos() == true) {
			// Cria registro do lote para fins de histórico
			HistoricoMovimentacaoLote historicoMovimentacao = atividadesLoteService
					.adicionaHistoricoMovimentacao(tipoAtividade.getLabel());

			// efetivamente transita o processo entre nós
			ProcessoTrf processoTrf;
			for (Map.Entry<Long, Transition> entry : entrySetList) {
				processoTrf = processoListMap.get(entry.getKey());
				try {
					atividadesLoteService.movimentarProcesso(entry.getKey(), entry.getValue().getId(), processoTrf,
							historicoMovimentacao);
					numeroTransicoesSucesso++;
				} catch (Throwable e) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Erro ao executar a transição para o processo " + processoTrf.getNumeroProcesso(), "Erro"));

					log.error("Erro ao executar a trans de id" + entry.getValue().getId(), e);
				}
			}
			String msg = String.format("%d processo(s) encaminhado(s) com sucesso", numeroTransicoesSucesso);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, msg, "Info"));
			log.debug(msg);

			limparProcessosSelecionados();
		}
	}

	public void finalizarAssinaturaLote() {
		StringBuilder mensagem = new StringBuilder();
		int count = 0, countNaoAssinadosMovimentados = 0;

		// Verifica se atividade em lote e do tipo Assinar inteiro teor em lote para nao
		// gerar incompatibilidade com as funcionalidades anteriores
		if (AtividadesLoteEnum.T == tipoAtividade) {

			HistoricoMovimentacaoLote history = atividadesLoteService
					.adicionaHistoricoMovimentacao(tipoAtividade.getLabel());

			for (AcordaoCompilacao acordaoCompilacao : acordaosCompilacoes) {
				if (acordaoCompilacao.getPermiteCompilacao()) {
					try {
						long taskInstanceId = acordaoCompilacao.getTaskInstance().getId();
						sessaoPautaProcessoTrfManager.compilarAcordaoEmLote(acordaoCompilacao, history,
								arquivosAssinados.get(taskInstanceId), documentosProcesso.get(taskInstanceId));
						facesMessages.add(Severity.INFO, "O acórdão do processo: {0} foi concluído com sucesso!",
								acordaoCompilacao.getSessaoPautaProcessoTrf().getProcessoTrf().getNumeroProcesso());
						;
						count++;
					} catch (Exception e) {
						facesMessages.add(Severity.ERROR,
								"Erro ao concluir o acórdão do processo: {0}, mensagem interna: {1}",
								acordaoCompilacao.getSessaoPautaProcessoTrf().getProcessoTrf().getNumeroProcesso(),
								e.getMessage());
					}
				}
			}
		} else {
			// ordena transições de saída
			List<Entry<Long, Transition>> transicoes = ordenaProcessosGrid();
			mensagem.append("Processos não  movimentados :  |");

			// efetivamente transita o processo entre nós
			Long taskId;
			Transition transit;
			ProcessoTrf proc;
			Set<ProcessoDocumento> docs;
			JbpmContext currentJbpmContext = ManagedJbpmContext.instance();
			DbPersistenceService dbPersistenceService = (DbPersistenceService) currentJbpmContext.getServices()
					.getPersistenceService();
			Session s = null;
			Util.beginTransaction();
			HistoricoMovimentacaoLote history = atividadesLoteService
					.adicionaHistoricoMovimentacao(tipoAtividade.getLabel());
			Util.commitTransction();

			for (Map.Entry<Long, Transition> entry : transicoes) {
				taskId = entry.getKey();
				transit = entry.getValue();
				proc = processoListMap.get(taskId);

				if (verificarTarefaElegivel(taskId)) {
					docs = documentosProcesso.get(taskId);
					try {
						Util.beginTransaction();
						s = dbPersistenceService.getSession();
						s.beginTransaction();
						atividadesLoteService.movimentar(taskId, proc, transit.getId(), arquivosAssinados.get(taskId),
								ordenarDocumentosPorHierarquia(docs), history);
						s.flush();
						s.getTransaction().commit();
						Util.commitTransction();
						count++;
					} catch (Throwable ex1) {
						try {
							countNaoAssinadosMovimentados++;
							mensagem.append(proc.getNumeroProcesso());
							mensagem.append(" | ");
							log.error("Erro ao assinar documento do processo {0}: {1}", proc.getNumeroProcesso(),
									ex1.getLocalizedMessage());
							JbpmUtil.clearWithoutClose(currentJbpmContext);
							s.getTransaction().rollback();
							Util.rollbackTransaction();
						} catch (Exception ex2) {
							ex2.printStackTrace();
						}
					} finally {
						Util.beginTransaction();
					}
				}
			}
		}

		facesMessages.add(Severity.INFO, "{0} processo(s) assinado(s)/movimentado(s) com sucesso.", count);

		if (countNaoAssinadosMovimentados > 0) {
			facesMessages.add(Severity.INFO, "{0} processo(s)  não movimentado(s).", countNaoAssinadosMovimentados);
			facesMessages.add(Severity.INFO, mensagem.toString());
		}

		facesMessages.add(Severity.INFO, "Esta janela será fechada automaticamente em 10 segundos.");
		limparProcessosSelecionados();
		log.debug("{0} processo(s) assinado(s) com sucesso", count);
	}

	/**
	 * Método retorna verdadeiro se a tarefa em questão foi definida como elegivel
	 * para a assinatura em lote
	 * 
	 * @param taskId
	 * @return true se tarefa é elegivel
	 */
	private boolean verificarTarefaElegivel(Long taskId) {
		boolean elegivel = false;
		if (processoListElegiveis != null) {
			for (Long taskIdElegivel : processoListElegiveis) {
				if (taskId.longValue() == taskIdElegivel.longValue()) {
					elegivel = true;
					break;
				}
			}
		}

		return elegivel;
	}

	public void finalizarMultiplos() {
		Map<Integer, String> signs = new HashMap<Integer, String>();
		Map<Integer, List<ProcessoDocumento>> mapaDocs = new HashMap<Integer, List<ProcessoDocumento>>();
		List<ProcessoDocumento> docs;
		List<ProcessoDocumento> aux;
		String doc;

		for (int i = 0; i < getProcessoListElegiveis().size(); i++) {

			docs = atividadesLoteService.getDocumentoProntoParaAssinatura(getProcessoListElegiveis().get(i));

			if (!docs.isEmpty()) {
				for (ProcessoDocumento documentoProntoParaAssinatura : docs) {
					try {
						aux = mapaDocs.get(documentoProntoParaAssinatura.getProcesso().getIdProcesso());
						if (aux == null) {
							aux = new ArrayList<ProcessoDocumento>();
							mapaDocs.put(documentoProntoParaAssinatura.getProcesso().getIdProcesso(), aux);
						}
						aux.add(documentoProntoParaAssinatura);
						for (ParAssinatura par : assinaturas) {
							doc = new String(SigningUtilities.base64Decode(par.getConteudo()));
							if (doc.equals(
									documentoProntoParaAssinatura.getProcessoDocumentoBin().getModeloDocumento())) {
								signs.put(documentoProntoParaAssinatura.getProcesso().getIdProcesso(),
										par.getAssinatura());
								break;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		int numeroDocumentosInseridos = 0;

		// ordena transições de saída
		List<Entry<Long, Transition>> entrySetList = ordenaProcessosGrid();

		// Cria registro do lote para fins de histórico
		HistoricoMovimentacaoLote history = atividadesLoteService
				.adicionaHistoricoMovimentacao(tipoAtividade.getLabel());

		// efetivamente transita o processo entre nós
		Long taskId;
		Transition transit;
		ProcessoTrf proc;
		String sig;
		List<ProcessoDocumento> docsProcesso;
		for (Map.Entry<Long, Transition> entry : entrySetList) {
			taskId = entry.getKey();
			transit = entry.getValue();
			proc = processoListMap.get(taskId);
			try {
				sig = signs.get(proc.getIdProcessoTrf());
				if (sig == null || sig.isEmpty()) {
					throw new Exception("Assinatura em branco.");
				}
				docsProcesso = mapaDocs.get(proc.getIdProcessoTrf());
				atividadesLoteService.assinarEMovimentarProcesso(taskId, proc, transit.getId(), sig, encodedCertChain,
						docsProcesso, history);
				numeroDocumentosInseridos++;
			} catch (Throwable e) {
				facesMessages.add(Severity.ERROR, "Erro ao assinar documento do processo {0}",
						proc.getNumeroProcesso());
				log.error("Erro ao assinar documento do processo {0}: {1}", proc.getNumeroProcesso(),
						e.getLocalizedMessage());
			}
		}
		facesMessages.add(Severity.INFO, "{0} processo(s) assinado(s) com sucesso", numeroDocumentosInseridos);
		log.debug("{0} processo(s) assinado(s) com sucesso", numeroDocumentosInseridos);
	}

	private List<Entry<Long, Transition>> ordenaProcessosGrid() {
		Set<Entry<Long, Transition>> entrySet = taskTransitionMap.entrySet();

		List<Entry<Long, Transition>> entrySetList = new ArrayList<Entry<Long, Transition>>();
		entrySetList.addAll(entrySet);

		Collections.sort(entrySetList, new Comparator<Entry<Long, Transition>>() {
			@Override
			public int compare(Entry<Long, Transition> a, Entry<Long, Transition> b) {
				return a.getKey().compareTo(b.getKey());
			}
		});
		return entrySetList;
	}

	public String getDownloadLinks() {
		Set<ProcessoDocumento> docs = new HashSet<ProcessoDocumento>();
		for (Set<ProcessoDocumento> aux : documentosProcesso.values()) {
			for (ProcessoDocumento processoDocumento : aux) {
				if (processoDocumento.getProcessoDocumentoBin().getModeloDocumento() == null) {
					EntityUtil.getEntityManager().refresh(processoDocumento.getProcessoDocumentoBin());
				}
				docs.add(processoDocumento);
			}
		}
		DocumentoJudicialService djs = ComponentUtil.getComponent(DocumentoJudicialService.class);
		return djs.getDownloadLinks(docs);
	}

	public List<Long> getProcessoListElegiveis() {
		return processoListElegiveis;
	}

	public void setProcessoListElegiveis(List<Long> processoListElegiveis) {
		this.processoListElegiveis = processoListElegiveis;
	}

	public List<Long> getProcessoListElegiveisSelecionados() {
		return processoListElegiveisSelecionados;
	}

	public void setProcessoListElegiveisSelecionados(List<Long> processoListElegiveisSelecionados) {
		this.processoListElegiveisSelecionados = processoListElegiveisSelecionados;
	}

	public List<Long> getProcessoListNaoElegiveis() {
		Set<Long> keySet = processoNaoElegiveisListMap.keySet();
		List<Long> processoListNaoElegiveis = new ArrayList<Long>();

		processoListNaoElegiveis.addAll(keySet);

		return processoListNaoElegiveis;
	}

	public Integer getAgrupamento() {
		return agrupamento;
	}

	public boolean possuiAgrupamentos() {
		return (agrupamento != null && agrupamento > 0);
	}

	private boolean isAgrupamentoPossuiMovimentos(Integer idAgrupamento) {
		if (agrupamentoPossuiMovimentos.get(idAgrupamento) == null) {
			List<Evento> eventosList = ComponentUtil.getComponent(EventoAgrupamentoManager.class)
					.recuperarEventos(idAgrupamento);
			agrupamentoPossuiMovimentos.put(idAgrupamento, CollectionUtilsPje.isNotEmpty(eventosList));
		}

		return agrupamentoPossuiMovimentos.get(idAgrupamento);
	}

	public Map<Long, List<String>> getProcessoNaoElegiveisListMap() {
		return processoNaoElegiveisListMap;
	}

	public void setProcessoNaoElegiveisListMap(Map<Long, List<String>> processoNaoElegiveisListMap) {
		this.processoNaoElegiveisListMap = processoNaoElegiveisListMap;
	}

	public List<TipoProcessoDocumento> getTipoDocumentoItems() {
		if (tipoDocumentoItems == null) {
			// tipoDocumentoItems = atividadesLoteService.getTipoDocumentoItems(null);
			tipoDocumentoItems.add(ComponentUtil.getTipoProcessoDocumentoManager()
					.findByDescricaoTipoDocumento(TipoProcessoDocumentoEnum.I.getLabel()));
			// tipoDocumentoItems = TipoProcessoDocumento
		}
		return tipoDocumentoItems;
	}

	public void atualizarModelosDisponiveis(TipoProcessoDocumento documento) {
		try {
			this.modelosDocumentosDisponiveis = atividadesLoteService
					.getModelosDisponiveisPorTipoProcessoDocumento(documento);
		} catch (Exception e) {
		}
	}

	public void atualizarModelosDisponiveis(TipoProcessoDocumento documento, boolean usarLocalizacao) throws Exception {
		this.modelosDocumentosDisponiveis = atividadesLoteService
				.getModelosDisponiveisPorTipoProcessoDocumento(documento, usarLocalizacao);
	}

	public List<ModeloDocumento> getModelosDocumentosDisponiveis() {
		return this.modelosDocumentosDisponiveis;
	}

	public void setModelosDocumentosDisponiveis(List<ModeloDocumento> modelosDocumentosDisponiveis) {
		this.modelosDocumentosDisponiveis = modelosDocumentosDisponiveis;
	}

	public String getEncodedCertChain() {
		return encodedCertChain;
	}

	public void setEncodedCertChain(String encodedCertChain) {
		this.encodedCertChain = encodedCertChain;
	}

	public String getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(String modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public class Selecionado {
		private Boolean selecionado = Boolean.FALSE;

		public Selecionado(Boolean selecionado) {
			this.selecionado = selecionado;
		}

		public Boolean getSelecionado() {
			return selecionado;
		}

		public void setSelecionado(Boolean selecionado) {
			this.selecionado = selecionado;
		}
	}

	public List<ProcessoDocumento> getDocumentosProcesso(Long idTask) {
		return ordenarDocumentosPorHierarquia(documentosProcesso.get(idTask));
	}

	/**
	 * Método que retorna os documentos que serão assinados na assinatura em lote
	 * para determinada
	 * 
	 * @param idTaskInstance
	 * @return List<ProcessoDocumento>
	 */
	public List<ProcessoDocumento> getMinutaParaAssinaturaEmLote(Long idTaskInstance) {
		List<ProcessoDocumento> processoDocumentoList = new ArrayList<ProcessoDocumento>();

		for (ProcessoDocumento pd : atividadesLoteService.getDocumentoProntoParaAssinatura(idTaskInstance)) {
			if (atividadesLoteService.liberaCertificacao(pd)) {
				processoDocumentoList.add(pd);
			}
		}
		return ordenarDocumentosPorHierarquia(processoDocumentoList);
	}

	public ProcessoDocumentoBinPessoaAssinatura verificaAssinatura(ProcessoDocumento processoDocumento) {
		return atividadesLoteService.verificaAssinatura(processoDocumento);
	}

	public void setTipoDocumentoAssinarLote(String tipos) {
		atividadesLoteService.setTipoDocumentoAssinarLote(tipos);
	}

	public Transition getTransicaoPadrao() {
		return transicaoPadrao;
	}

	public void setTransicaoPadrao(Transition transicaoPadrao) {
		if (transicoesPossiveis.contains(transicaoPadrao)) {
			this.transicaoPadrao = transicaoPadrao;
		} else {
			facesMessages.add(Severity.ERROR, "Não foi possível selecionar a transição {0}", transicaoPadrao);
		}
	}

	public void aplicarTransicaoTodos() {
		if (idTransicaoPadrao == null) {
			return;
		}

		List<Transition> trans;
		boolean found;
		for (Long l : processoListElegiveis) {
			trans = atividadesLoteService.getAvaliableTransitions(atividadesLoteService.getTaskInstanceById(l));
			found = false;
			for (Transition t : trans) {
				if (t.getId() == idTransicaoPadrao) {
					taskTransitionMap.put(l, t);
					found = true;
					break;
				}
			}
			if (!found) {
				Contexts.getConversationContext().set("naoMostrarMensagemFechamento", true);
				facesMessages.add(Severity.INFO,
						"Transição não disponível para o processo " + processoListMap.get(l).getNumeroProcesso() + ".");
			}
		}
	}

	public Set<Transition> getTransicoesPossiveis() {
		return transicoesPossiveis;
	}

	public void setTransicoesPossiveis(LinkedHashSet<Transition> transicoesPossiveis) {
		this.transicoesPossiveis = transicoesPossiveis;
	}

	public Long getIdTransicaoPadrao() {
		return idTransicaoPadrao;
	}

	public void setIdTransicaoPadrao(Long idTransicaoPadrao) {
		this.idTransicaoPadrao = idTransicaoPadrao;
	}

	@Observer("MOVIMENTAR_LOTE")
	public void movimentar(String idConversation, Long idHistorico, Map<Long, Map<String, Object>> data) {
		Context sctx = null;
		int cont = 0;
		int total = data.size();
		Date begin = new Date();
		if (Contexts.isSessionContextActive()) {
			sctx = Contexts.getSessionContext();
		}
		if (sctx != null) {
			sctx.set("pje:lote:" + idConversation + ":inicio", begin);
			sctx.set("pje:lote:" + idConversation + ":total", total);
		}
		for (Entry<Long, Map<String, Object>> e : data.entrySet()) {
			try {
				atividadesLoteService.movimentarProcesso(e.getKey(), (Long) e.getValue().get("idTransition"),
						(Integer) e.getValue().get("idProcesso"), idHistorico);
			} catch (Throwable t) {
				log.error("Erro ao realizar a movimentação do processo {0}: {1}", e.getValue().get("idProcesso"),
						t.getLocalizedMessage());
			}
			cont++;
			if (sctx != null) {
				sctx.set("pje:lote:" + idConversation + ":percentual", Math.ceil((cont * 100.0 / total)));
			}
		}
		long duration = (new Date().getTime() - ((Date) sctx.get("pje:lote:" + idConversation + ":inicio")).getTime());
		double mean = total * 1.0 / (duration / 1000d);
		log.debug("Realizadas {0} movimentações em {1} segundos, com média de {2} movimentações/segundo.", total,
				(duration / 1000d), mean);
		if (sctx != null) {
			sctx.remove("pje:lote:" + idConversation + ":inicio");
			sctx.remove("pje:lote:" + idConversation + ":percentual");
			sctx.remove("pje:lote:" + idConversation + ":total");
		}
	}

	public SessaoPautaProcessoTrfManager getSessaoPautaProcessoTrfManager() {
		return sessaoPautaProcessoTrfManager;
	}

	/**
	 * Realiza o download do documento para o usuário
	 * 
	 * @param processoDocumento documento a ser baixado
	 */
	public void downloadDocumento(ProcessoDocumento processoDocumento) {
		try {
			ProcessoDocumentoManager processoDocumentoManager = ComponentUtil
					.getComponent(ProcessoDocumentoManager.class);
			processoDocumentoManager.downloadDocumento(processoDocumento);
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, e.getMessage());
		}
	}

	/**
	 * Método retorna uma lista ordenada a partir da coleção de documentos de forma
	 * que o documento principal("pai") apareçam antes de seus documentos
	 * vinculados("filhos").
	 * 
	 * @param documentos Coleção de documentos a ser ordenada
	 * @return Lista de documentos ordenadas de forma hierarquica
	 */
	private List<ProcessoDocumento> ordenarDocumentosPorHierarquia(Collection<ProcessoDocumento> documentos) {
		List<ProcessoDocumento> documentosOrdenadosPorPrincipal = new ArrayList<ProcessoDocumento>(0);
		if (documentos != null) {
			documentosOrdenadosPorPrincipal.addAll(documentos);

			Collections.sort(documentosOrdenadosPorPrincipal, new Comparator<ProcessoDocumento>() {
				@Override
				public int compare(ProcessoDocumento doc1, ProcessoDocumento doc2) {
					Boolean doc1Principal = verificarDocumentoPrincipal(doc1);
					Boolean doc2Principal = verificarDocumentoPrincipal(doc2);
					if ((doc1Principal && doc2Principal)) {
						return 0;
					} else if ((doc1Principal && !doc2Principal) || (!doc1Principal && doc2Principal)) {
						return compararDocumentoPrincipalComSecundario(doc1Principal, doc2Principal);
					} else {
						return compararDocumentoSecundarioComSecundario(doc1, doc2);
					}
				}

				/**
				 * Método define ordem dos documentos vendo qual deles é o principal.
				 * 
				 * @param documento1Principal boolean indicando se documento1 é principal
				 * @param documento2Principal boolean indicando se documento2 é principal
				 * @return 1 se documento1Principal é principal e -1 se documento2Principal é
				 *         principal
				 */
				private int compararDocumentoPrincipalComSecundario(Boolean documento1Principal,
						Boolean documento2Principal) {
					if (documento1Principal) {
						return -1;
					} else {
						return 1;
					}
				}

				/**
				 * Método define a ordem dos documentos baseado na ordem definida pelo usuário.
				 * 
				 * @param documentoSecundario1 documento1
				 * @param documentoSecundario2 documento2
				 * @return 1 se documentoSecundario1 deve vir prineiro, -1 se
				 *         documentoSecundario2 deve vir primeiro e 0 se são iguais
				 */
				private int compararDocumentoSecundarioComSecundario(ProcessoDocumento documentoSecundario1,
						ProcessoDocumento documentoSecundario2) {
					int retorno = 0;

					if (documentoSecundario1.getNumeroOrdem() != null
							&& documentoSecundario2.getNumeroOrdem() != null) {
						if (documentoSecundario1.getNumeroOrdem().intValue() > documentoSecundario2.getNumeroOrdem()
								.intValue()) {
							retorno = 1;
						} else if (documentoSecundario1.getNumeroOrdem().intValue() < documentoSecundario2
								.getNumeroOrdem().intValue()) {
							retorno = -1;
						} else {
							retorno = 0;
						}
					}
					return retorno;
				}
			});

		}
		return documentosOrdenadosPorPrincipal;
	}

	/**
	 * Método retorna verdadeiro se documento é um documento principal
	 * 
	 * @param documento
	 * @return true se documento é um documento principal
	 */
	public Boolean verificarDocumentoPrincipal(ProcessoDocumento documento) {
		return (documento != null) && (documento.getDocumentoPrincipal() == null);
	}

	/**
	 * Método identifica o tipo de documento para ser apresentado no view
	 * 
	 * @param documento
	 * @return Tipo de documento para apresentação na view
	 */
	public String obterTipoDocumento(ProcessoDocumento documento) {
		if (documento != null && documento.getProcessoDocumentoBin() != null
				&& documento.getProcessoDocumentoBin().isBinario()) {
			return TIPO_DOCUMENTO_BINARIO;
		}
		return TIPO_DOCUMENTO_TEXTO;
	}

	/**
	 * Método responsavel por passar a constante TIPO_DOCUMENTO_TEXTO para view
	 * 
	 * @return
	 */
	public String getTipoDocumentoTexto() {
		return TIPO_DOCUMENTO_TEXTO;
	}

	/**
	 * Método responsavel por passar a constante TIPO_DOCUMENTO_BINARIO para view
	 * 
	 * @return
	 */
	public String getTipoDocumentoBinario() {
		return TIPO_DOCUMENTO_BINARIO;
	}

	public Boolean getIsUsandoMiniPac() {
		return isUsandoMiniPac;
	}

	/**
	 * Grava a variável {@link Variaveis#PJE_FLUXO_MINI_PAC_LIST_VO} com os dados
	 * indicados na tela de minuta em lote. Para cada processo que tenha um meio de
	 * comunicação definido na tela do usuário, os meios e polos indicados pelo
	 * usuários são processados para cada parte ativa e gravadas em contexto para
	 * posterior recuperação.
	 */
	public void sincronizarVariavelVOContexto() {
		if (!polosSelecionadosList.isEmpty() && !meiosSelecionadosList.isEmpty() && !processoListMap.isEmpty()) {
			TaskInstance taskInstance;
			for (Long idTaskInstance : processoListMap.keySet()) {
				carregarMiniPacList(processoListMap.get(idTaskInstance));
				if (!miniPacList.isEmpty()) {
					taskInstance = atividadesLoteService.getTaskInstanceById(idTaskInstance);
					taskInstance.getProcessInstance().getContextInstance()
							.setVariable(Variaveis.PJE_FLUXO_MINI_PAC_LIST_VO, miniPacList);
				}
			}
		}
	}

	/**
	 * Método auxiliar para carregar a lista de destinatários do processo indicado,
	 * que será gravada em contexto para posterior recuperação. É verificado se o
	 * processo indicado foi elegível para o miniPAC
	 * ({@link #atualizarMapaMeiosAplicados(ExpedicaoExpedienteEnum, Boolean)} Se o
	 * processo for elegível, para cada meio indicado pelo usuário na tela, as
	 * partes ativas dos polos também indicados pelo usuário serão adicionadas à
	 * lista.
	 * 
	 * @param processoTrf
	 */
	private void carregarMiniPacList(ProcessoTrf processoTrf) {
		MiniPacVO miniPacVO;
		miniPacList = new ArrayList<MiniPacVO>(0);
		MiniPacService miniPacService = ComponentUtil.getComponent(MiniPacService.class);
		for (ExpedicaoExpedienteEnum meio : miniPacTempMap.keySet()) {
			if (miniPacTempMap.get(meio).contains(processoTrf)) {
				for (ProcessoParteParticipacaoEnum polo : polosSelecionadosList) {
					for (ProcessoParte processoParte : processoTrf.getListaPartePrincipal(polo)) {
						if (processoParte.getIsAtivo()) {
							miniPacVO = getMiniPacVOFromLocalList(processoParte);
							if (miniPacVO == null) {
								miniPacVO = miniPacService.carregaMiniPacVO(processoParte, this.intimacaoPessoal,
										this.prazoGeral);
								miniPacList.add(miniPacVO);
							}
							miniPacVO.getMeios().add(meio);
						}
					}
				}
			}
		}
	}

	/**
	 * Método auxiliar para recuperar um destinatário ({@link MiniPacVO}
	 * correspondente à parte ({@link ProcessoParte} indicada. Utilizado para evitar
	 * repetidas chamadas ao banco, pois o objeto destinatário já pode ter sido
	 * construído antes.
	 * 
	 * @param processoParte
	 * @return {@link MiniPacVO} destinatário correspondente à parte indicada
	 */
	private MiniPacVO getMiniPacVOFromLocalList(ProcessoParte processoParte) {
		for (MiniPacVO vo : miniPacList) {
			if (vo.getIdProcessoParte() == processoParte.getIdProcessoParte()) {
				return vo;
			}
		}
		return null;
	}

	/**
	 * Registra se o polo indicado deve ou não (toggle) ser processado para cada
	 * processo elegível na atividade em lote.
	 * 
	 * Quando o usuário seleciona um polo para adição e já tenha selecionado algum
	 * meio de comunicação, o sistema verifica se todas as partes dos processos
	 * elegíveis podem ser notificadas pelo(s) meio(s) já indicado(s).
	 * 
	 * Como se trata de atividade em lote, só é possível notificar todas as partes
	 * do polo indicado. Caso uma das partes não possa ser notificada em um meio de
	 * comunicação específico, o processo todo fica inelegível para usar o miniPAC
	 * naquele meio.
	 * 
	 * Nessa verificação, é considerado o comando do novo CPC (Art. 246 § 1º) para
	 * indicar o meio eletrônico automaticamente caso a parte seja elegível para
	 * esse meio de comunicação e o administrador de fluxo tenha indicado que a
	 * tarefa usará o meio eletrônico.
	 * 
	 * @param polo a ser processado para todos os processos elegíveis
	 */
	public void togglePolo(ProcessoParteParticipacaoEnum polo) {
		Boolean isChecarMeioEletronicoPreferencial = Boolean.FALSE;
		if (!polosSelecionadosList.remove(polo)) {
			polosSelecionadosList.add(polo);
		}
		if (meiosIndicadosFluxo.contains(ExpedicaoExpedienteEnum.E)
				&& !meiosSelecionadosList.contains(ExpedicaoExpedienteEnum.E)) {
			meiosSelecionadosList.add(ExpedicaoExpedienteEnum.E);
			isChecarMeioEletronicoPreferencial = Boolean.TRUE;
		}
		if (polosSelecionadosList.isEmpty()) {
			miniPacTempMap.clear();
		} else {
			for (ExpedicaoExpedienteEnum meio : meiosSelecionadosList) {
				atualizarMapaMeiosAplicados(meio, isChecarMeioEletronicoPreferencial);
			}
		}
		if (isChecarMeioEletronicoPreferencial) {
			meiosSelecionadosList.remove(ExpedicaoExpedienteEnum.E);
		}
	}

	/**
	 * Registra se o meio de comunicação indicado será processado ou não (toggle)
	 * para todos os processos elegíveis na atividade em lote.
	 * 
	 * Quando o usuário seleciona um meio e já tenha selecinado um ou mais polos no
	 * frame do miniPAC, o sistema irá processar todos as partes ativas dos polos
	 * selecionados e verificar se podem ser notificadas no meio indicado.
	 * 
	 * @param meio a ser processado para todos os processos elegíveis na atividade
	 *             em lote
	 */
	public void toggleMeio(ExpedicaoExpedienteEnum meio) {
		if (meiosSelecionadosList.remove(meio)) {
			miniPacTempMap.remove(meio);
		} else {
			meiosSelecionadosList.add(meio);
			atualizarMapaMeiosAplicados(meio, Boolean.FALSE);
		}
	}

	/**
	 * Método auxiliar para efetivamente verificar se as partes dos processos
	 * elegíveis na atividade em lote podem ser notificadas pelo meio de comunicação
	 * indicado.
	 * 
	 * O que ocorre:
	 * <li>para cada processo elegível na atividade em lote, o método analisa todsa
	 * as pares dos polos indicados pelo usuário no frame do miniPAC
	 * <li>se a parte estiver ativa
	 * <ul>
	 * <li>verifica se o administrador de fluxo indicou que o meio eletrônico seria
	 * usado na tarefa
	 * <li>verifica se a parte deve ser notificada preferencialmente por meio
	 * eletrônico
	 * ({@link ProcessoParte#isNotificarPreferencialmenteMeioEletronico()}
	 * </ul>
	 * 
	 * Se ao final da verificação das partes todas foram elegíveis para serem
	 * notificadas pelo meio indicado, o processo é adicionado a um mapa temporário.
	 * Caso contrário, o processo é removido ou não é aidiconado ao mapa, pois como
	 * se trata de atividade em lote, só é possível notificar todas as partes de um
	 * polo.
	 * 
	 * @param meio                               a ser processado para os processos
	 *                                           elegíveis na atividade em lote.
	 * @param isChecarMeioEletronicoPreferencial indica se é para verificar o meio
	 *                                           eletrônico preferencial para as
	 *                                           partes dos processos elegíveis
	 */
	private void atualizarMapaMeiosAplicados(ExpedicaoExpedienteEnum meio, Boolean isChecarMeioEletronicoPreferencial) {
		Boolean isPodeInserirProcesso;
		miniPacTempMap.put(meio, new ArrayList<ProcessoTrf>(0));
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		for (ProcessoTrf processoTrf : processoListMap.values()) {
			isPodeInserirProcesso = Boolean.FALSE;
			for (ProcessoParteParticipacaoEnum polo : polosSelecionadosList) {
				processoTrf = EntityUtil.getEntityManager().merge(processoTrf);
				for (ProcessoParte processoParte : processoTrf.getListaPartePrincipal(polo)) {
					if (!processoParte.getIsAtivo()) {
						continue;
					}
					processoParte = EntityUtil.getEntityManager().merge(processoParte);
					if (atoComunicacaoService.isPodeInserirMeio(processoParte, meio, this.intimacaoPessoal)) {
						isPodeInserirProcesso = Boolean.TRUE;
					} else {
						isPodeInserirProcesso = Boolean.FALSE;
						break;
					}
				}
				if (!isPodeInserirProcesso) {
					break;
				}
			}
			if (isPodeInserirProcesso) {
				miniPacTempMap.get(meio).add(processoTrf);
			} else {
				miniPacTempMap.get(meio).remove(processoTrf);
			}
		}
	}

	public void setPrazoGeral(String prazo) {
		this.prazoGeral = prazo;
	}

	public String getPrazoGeral() {
		return this.prazoGeral;
	}

	public List<ExpedicaoExpedienteEnum> getMeiosIndicadosFluxo() {
		return this.meiosIndicadosFluxo;
	}

	public Boolean getIntimacaoPessoal() {
		return intimacaoPessoal;
	}

	/**
	 * Define se os atos de comunicação a serem criados pelo miniPAC serão pessoais
	 * ou não.
	 * 
	 * @param intimacaoPessoal
	 */
	public void setIntimacaoPessoal(Boolean intimacaoPessoal) {
		this.intimacaoPessoal = intimacaoPessoal;
		if (!polosSelecionadosList.isEmpty()) {
			for (ExpedicaoExpedienteEnum meio : meiosSelecionadosList) {
				atualizarMapaMeiosAplicados(meio, false);
			}
		}
	}

	public Map<ExpedicaoExpedienteEnum, List<ProcessoTrf>> getMeiosProcessoMap() {
		return miniPacTempMap;
	}

	/**
	 * Retorna true se houver ao menos um processo elegível na atividade em lote com
	 * algum meio de comunicação. Utilizado para exibir ou não botão na tela de
	 * minuta em lote para que o usuário possa remover da lista processos elegíveis
	 * aqueles sem meio de comunicação definido pelo frame do miniPAC.
	 */
	public boolean hasMeioAplicado() {
		boolean hasMeioAplicado = false;
		for (Map.Entry<ExpedicaoExpedienteEnum, List<ProcessoTrf>> listaPorMeio : miniPacTempMap.entrySet()) {
			if (listaPorMeio.getValue().size() > 0) {
				hasMeioAplicado = true;
				break;
			}
		}
		return hasMeioAplicado;
	}

	public List<ProcessoParteParticipacaoEnum> getPolosSelecionadosList() {
		return polosSelecionadosList;
	}

	public List<ExpedicaoExpedienteEnum> getMeiosSelecionadosList() {
		return meiosSelecionadosList;
	}

	/**
	 * Remove da lista de processos elegíveis um proceso específico. Pode ser
	 * utilizado para que o usuário possa, por exemplo, remover um único processo
	 * que não ficou elegível para utilizar um meio de comunicação pelo frame do
	 * miniPAC.
	 * 
	 * @param idTaskInstance id do task instance para remover o processo
	 *                       correspondente da lista de elegíveis na atividade em
	 *                       lote
	 */
	public void removerProcessoElegivel(Long idTaskInstance) {
		processoListElegiveis.remove(idTaskInstance);
		processosMap.remove(idTaskInstance);
		taskTransitionMap.remove(idTaskInstance);
	}

	/**
	 * Remove da lista de processos elegíveis na atividade em lote, aqueles que não
	 * possuem meio de comunicação definido pelo frame do miniPAC.
	 */
	public void removerProcessosSemMeioAplicado() {
		List<Long> idTaskInstanceListSemMeioAplicado = new ArrayList<Long>(0);
		Boolean existe;
		for (Long idTaskInstance : processoListElegiveis) {
			existe = Boolean.FALSE;
			for (List<ProcessoTrf> lista : miniPacTempMap.values()) {
				if (lista.contains(processoListMap.get(idTaskInstance))) {
					existe = Boolean.TRUE;
					break;
				}
			}
			if (!existe) {
				idTaskInstanceListSemMeioAplicado.add(idTaskInstance);
			}
		}
		for (Long idTaskInstance : idTaskInstanceListSemMeioAplicado) {
			removerProcessoElegivel(idTaskInstance);
		}
	}

	public TipoProcessoDocumento getTipoProcessoDocumentoSelecionado() {
		return this.tipoProcessoDocumentoSelecionado;
	}

	public void setTipoProcessoDocumentoSelecionado(TipoProcessoDocumento tipoProcessoDocumentoSelecionado) {
		this.tipoProcessoDocumentoSelecionado = tipoProcessoDocumentoSelecionado;
	}

	public void onSelectProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		Boolean isAlterandoAgrupadorMovimentos = true;
		Integer idAgrupadorIndicado = null;
		this.setTipoComunicacao(tipoProcessoDocumento);
		onChangeMeioComunicacao();
		this.setTipoProcessoDocumentoSelecionado(tipoProcessoDocumento);

		if (tipoProcessoDocumento != null && tipoProcessoDocumento.getAgrupamento() != null) {
			idAgrupadorIndicado = tipoProcessoDocumento.getAgrupamento().getIdAgrupamento();

			if (idAgrupadorIndicado.equals(this.agrupamento)) {
				isAlterandoAgrupadorMovimentos = false;
			}
		}
		if (Contexts.isPageContextActive() && isAlterandoAgrupadorMovimentos) {
			TaskInstance ti;
			for (Long taskId : processoListMap.keySet()) {
				ti = atividadesLoteService.getTaskInstanceById(taskId);
				LancadorMovimentosService.instance().apagarMovimentosTemporarios(ti.getProcessInstance());

				if (idAgrupadorIndicado != null && idAgrupadorIndicado > 0) {
					LancadorMovimentosService.instance().setAgrupamentoDeMovimentosTemporarios(ti.getProcessInstance(),
							idAgrupadorIndicado);
				}
			}
			this.setNovoAgrupamentoMovimentos(idAgrupadorIndicado);
		}
	}

	private void setNovoAgrupamentoMovimentos(Integer idAgrupadorIndicado) {
		eventsLoteTree.clearList();
		eventsLoteTree.clearTree();

		this.agrupamento = idAgrupadorIndicado;
		if (this.agrupamento != null && this.agrupamento > 0) {
			if (Contexts.isPageContextActive()) {
				eventsLoteTree.setRootsSelectedMap(new HashMap<Evento, List<Evento>>());
				eventsLoteTree.getRoots(this.agrupamento);
			}
		}
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {

		Long taskInstanceId = recuperarTaskInstanceIdDoArquivoAssinado(arquivoAssinadoHash);

		if (taskInstanceId == null) {
			throw new Exception("Erro não foi possível determinar de qual task instance o documento pertence!");
		}

		if (this.arquivosAssinados.get(taskInstanceId) == null) {
			this.arquivosAssinados.put(taskInstanceId, new ArrayList<ArquivoAssinadoHash>());
		}

		this.arquivosAssinados.get(taskInstanceId).add(arquivoAssinadoHash);
	}

	/**
	 * Metodo responsavel por recuperar a task instance id do arquivo assinado pelo
	 * id do documentos disponiveis para assinatura
	 * 
	 * @param arquivoAssinadoHash O arquiv0o assinado
	 * @return O id da task instance se encontrar e nulo caso nao encontre
	 */
	private Long recuperarTaskInstanceIdDoArquivoAssinado(ArquivoAssinadoHash arquivoAssinadoHash) {

		for (Long taskInstanceId : documentosProcesso.keySet()) {

			Set<ProcessoDocumento> taskDocumentos = documentosProcesso.get(taskInstanceId);

			for (ProcessoDocumento processoDocumento : taskDocumentos) {

				if (processoDocumento.getIdProcessoDocumento() == arquivoAssinadoHash.getIdEmInteger()) {
					return taskInstanceId;
				}
			}
		}

		return null;
	}

	@Override
	public String getActionName() {
		return NAME;
	}

	public List<ModeloDocumento> getListaModelosDocumentoPorTipoDocumento() {
		return listaModelosDocumentoPorTipoDocumento;
	}

	public void setListaModelosDocumentoPorTipoDocumento(List<ModeloDocumento> listaModelosDocumentoPorTipoDocumento) {
		this.listaModelosDocumentoPorTipoDocumento = listaModelosDocumentoPorTipoDocumento;
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}

	public void setProtocolarDocumentoBean(ProtocolarDocumentoBean protocolarDocumentoBean) {
		this.protocolarDocumentoBean = protocolarDocumentoBean;
	}

	public String getEstilosFormatacao() {
		return editorEstiloService.recuperarEstilosJSON();
	}

	public String getNomeTipoDocumentoPrincipal() {
		if (getProtocolarDocumentoBean() != null && getProtocolarDocumentoBean().getDocumentoPrincipal() != null
				&& getProtocolarDocumentoBean().getDocumentoPrincipal().getTipoProcessoDocumento() != null) {
			return getProtocolarDocumentoBean().getDocumentoPrincipal().getTipoProcessoDocumento()
					.getTipoProcessoDocumento();
		}
		return "";
	}

	public List<String> getTiposParteIntimacao() {
		return tiposParteIntimacao;
	}

	public void setTiposParteIntimacao(List<String> tiposParteIntimacao) {
		this.tiposParteIntimacao = tiposParteIntimacao;
	}

	public Integer getPrazoIntimacao() {
		return prazoIntimacao;
	}

	public void setPrazoIntimacao(Integer prazoIntimacao) {
		this.prazoIntimacao = prazoIntimacao;
	}

	public ExpedicaoExpedienteEnum getMeioIntimacao() {
		return meioIntimacao;
	}

	public void setMeioIntimacao(ExpedicaoExpedienteEnum meioIntimacao) {
		this.meioIntimacao = meioIntimacao;
	}

	public void onCheckTipoParte() {
		String selecionado = tiposParteIntimacao.get(tiposParteIntimacao.size() - 1);
		if (!Strings.isEmpty(selecionado)) {
			if (selecionado.equals(TODAS)) {
				tiposParteIntimacao.clear();
				String[] split = TIPOS_DE_POLO.split(",");
				tiposParteIntimacao.addAll(Arrays.asList(split));
			}
		}
		onChangeMeioIntimacao();
		selecionarTodos();
	}

	public void onChangeMeioIntimacao() {
		if (this.tipoComunicacao != null) {
			msgIntimacaoMap = new HashMap<Integer, String>();
			for (Long idProcesso : processoListElegiveis) {
				ProcessoTrf processo = processoListMap.get(idProcesso);
				verificaPartesAptas(processo);
				if (ultimoAtoProferido) {
					carregarUltimoAtoProferidoMap(getProcessoListMap().get(idProcesso));
				}

			}
			selecionarTodos();
		}
	}

	private List<ProcessoParte> verificaPartesAptas(ProcessoTrf processoTrf) {
		List<ProcessoParte> partesParaIntimcacao = new ArrayList<>();
		if (this.tipoComunicacao.getTipoProcessoDocumento().equals(TipoProcessoDocumentoEnum.C.getLabel())) {
			partesParaIntimcacao = getPartesAptasParaCitacao(processoTrf);
		}

		if (this.meioIntimacao.equals(ExpedicaoExpedienteEnum.P)
				&& this.tipoComunicacao.getTipoProcessoDocumento().equals(TipoProcessoDocumentoEnum.I.getLabel())) {
			partesParaIntimcacao = getPartesAptasParaIntimacaoDiario(processoTrf);
		}
		if (this.meioIntimacao.equals(ExpedicaoExpedienteEnum.E)
				&& this.tipoComunicacao.getTipoProcessoDocumento().equals(TipoProcessoDocumentoEnum.I.getLabel())) {
			partesParaIntimcacao = getPartesAptasParaIntimacao(processoTrf);
		}
		return partesParaIntimcacao;
	}

	public void selecionarTodos() {
		for (Long idProcesso : processoListElegiveis) {
			ProcessoTrf processo = processoListMap.get(idProcesso);
			if (processo != null) {
				if (msgIntimacaoMap.get(processo.getIdProcessoTrf()) != null && (msgIntimacaoMap
						.get(processo.getIdProcessoTrf()).equals(MSG_NENHUMA_PARTE_INTIMACAO)
						|| msgIntimacaoMap.get(processo.getIdProcessoTrf()).equals(MSG_NENHUMA_PARTE_CITACAO))) {
					processosSelecionadosCheck.put(idProcesso, false);
				} else {
					processosSelecionadosCheck.put(idProcesso, selecionarTodos);
				}
			}
		}
	}

	public void selecionarProcesso(Long idProcesso) {
		if (getProcessosSelecionadosCheck().get(idProcesso)) {
			getProcessosSelecionadosCheck().remove(idProcesso);
		} else {
			getProcessosSelecionadosCheck().put(idProcesso, true);
		}
	}

	public void filtrarProcesso() {
		for (Long p : processoListElegiveis) {
			if (processoListMap.get(p) != null) {

				if (msgIntimacaoMap.get(processoListMap.get(p).getIdProcessoTrf()) != null) {

					if (this.partePendente && msgIntimacaoMap.get(processoListMap.get(p).getIdProcessoTrf())
							.equals(MSG_PARTES_SEM_INTIMAR)) {

						processosSelecionadosCheck.put(p, true);
					}
					if ((!this.partePendente && msgIntimacaoMap.get(processoListMap.get(p).getIdProcessoTrf())
							.equals(MSG_PARTES_SEM_INTIMAR))) {
						processosSelecionadosCheck.put(p, false);
					}
				}
				if (this.podeIntimar && msgIntimacaoMap.get(processoListMap.get(p).getIdProcessoTrf()) == null) {

					processosSelecionadosCheck.put(p, true);
				}
				if (!this.podeIntimar && msgIntimacaoMap.get(processoListMap.get(p).getIdProcessoTrf()) == null) {
					processosSelecionadosCheck.put(p, false);
				}
			}
		}

	}

	public String getTiposDePolo() {
		return tiposPolosDisponiveisString;
	}

	public Boolean getUltimoAtoProferido() {
		return ultimoAtoProferido;
	}

	public void setUltimoAtoProferido(Boolean ultimoAtoProferido) {
		this.ultimoAtoProferido = ultimoAtoProferido;
	}

	public boolean isConteudoAlterado() {
		return conteudoAlterado;
	}

	public void setConteudoAlterado(boolean conteudoAlterado) {
		this.conteudoAlterado = conteudoAlterado;
	}

	public boolean isConteudoVazio() {
		return conteudoVazio;
	}

	public void setConteudoVazio(boolean conteudoVazio) {
		this.conteudoVazio = conteudoVazio;
	}

	public boolean isIntimacaoEmLoteFinalizada() {
		return intimacaoEmLoteFinalizada;
	}

	public void setIntimacaoEmLoteFinalizada(boolean intimacaoEmLoteFinalizada) {
		this.intimacaoEmLoteFinalizada = intimacaoEmLoteFinalizada;
	}

	public Map<Integer, ProcessoDocumento> getUltimoAtoProferidoMap() {
		return ultimoAtoProferidoMap;
	}

	public void setUltimoAtoProferidoMap(Map<Integer, ProcessoDocumento> ultimoAtoProferidoMap) {
		this.ultimoAtoProferidoMap = ultimoAtoProferidoMap;
	}

	public Map<Integer, String> getMsgIntimacaoMap() {
		return msgIntimacaoMap;
	}

	public void setMsgIntimacaoMap(Map<Integer, String> msgIntimacaoMap) {
		this.msgIntimacaoMap = msgIntimacaoMap;
	}

	public AtividadesLoteEnum getTipoAtividadeReq() {
		return tipoAtividadeReq;
	}

	public void setTipoAtividadeReq(AtividadesLoteEnum tipoAtividadeReq) {
		this.tipoAtividadeReq = tipoAtividadeReq;
	}

	public Boolean getSelecionarTodos() {
		return selecionarTodos;
	}

	public void setSelecionarTodos(Boolean selecionarTodos) {
		this.selecionarTodos = selecionarTodos;
	}
}