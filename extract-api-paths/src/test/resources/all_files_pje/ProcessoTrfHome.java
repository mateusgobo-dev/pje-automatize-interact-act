package br.com.infox.cliente.home;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import br.com.infox.pje.dao.ProcessoTrfDAO;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.persistence.FullTextHibernateSessionProxy;
import org.jboss.seam.security.Identity;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.util.Strings;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.SwimlaneInstance;

import br.com.infox.DAO.EntityList;
import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.RemessaProcesso;
import br.com.infox.cliente.bean.PreCadastroPessoaBean;
import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.cliente.component.ValidacaoAssinaturaProcessoDocumento;
import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.cliente.component.suggest.ProcessoDependenciaSuggestBean;
import br.com.infox.cliente.component.suggest.ProcessoOrigemSuggestBean;
import br.com.infox.cliente.component.suggest.ProcessoOriginarioSuggestBean;
import br.com.infox.cliente.component.tree.ClasseJudicialProcessoTreeHandler;
import br.com.infox.cliente.exception.ProcessoTrfHomeException;
import br.com.infox.cliente.util.MimetypeUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.editor.action.EditorAction;
import br.com.infox.ibpm.component.tree.EventoBean;
import br.com.infox.ibpm.component.tree.EventsTreeHandler;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.EventoHome;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.home.UsuarioHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.ibpm.jbpm.actions.RegistraEventoAction;
import br.com.infox.ibpm.jbpm.assignment.LocalizacaoAssignment;
import br.com.infox.pje.dao.ProcessoTrfDAO;
import br.com.infox.pje.list.PautaJulgamentoList;
import br.com.infox.pje.list.PessoaAdvogadoParteList;
import br.com.infox.pje.list.ProcessoComAudienciaNaoMarcadaList;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.pje.manager.SituacaoProcessoManager;
import br.com.infox.pje.webservice.consultaoutrasessao.EncryptionSecurity;
import br.com.infox.trf.eventos.DefinicaoEventos;
import br.com.infox.utils.Constantes;
import br.com.itx.component.SelectItemsQuery;
import br.com.itx.component.Util;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.constant.Grid;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.Cached;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.com.itx.util.HibernateUtil;
import br.com.jt.pje.manager.HabilitacaoAutosManager;
import br.com.jt.pje.manager.PautaSessaoManager;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.auxiliar.PontoExtensaoResposta;
import br.jus.cnj.pje.extensao.auxiliar.custas.CodigoRespostaGuiaRecolhimentoEnum;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.PjeRestClientException;
import br.jus.cnj.pje.nucleo.PrevencaoException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.AgrupamentoClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.AssuntoTrfManager;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ComplementoProcessoJEManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.DownloadBinarioArquivoManager;
import br.jus.cnj.pje.nucleo.manager.DownloadBinarioMNIManager;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.InformacaoCriminalRascunhoManager;
import br.jus.cnj.pje.nucleo.manager.JurisdicaoManager;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoAlertaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoAssuntoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoPeticaoNaoLidaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoHistoricoClasseManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteRepresentanteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoPushManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoRascunhoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoSegredoManager;
import br.jus.cnj.pje.nucleo.manager.RevisorProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoVisibilidadeSegredoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.TipoParteManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.DefinicaoCompetenciaService;
import br.jus.cnj.pje.nucleo.service.LogAcessoAutosDownloadsService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.ProcessoService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.servicos.AutuacaoService;
import br.jus.cnj.pje.servicos.DistribuicaoService;
import br.jus.cnj.pje.servicos.DistribuicaoService.TipoDistribuicao260;
import br.jus.cnj.pje.servicos.PrevencaoService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.PesquisaProcessoParadigmaAction;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.cnj.pje.view.ProgressoProtocoloWebSocket;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.webservice.client.criminal.ProcessoCriminalRestClient;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.csjt.pje.business.service.PlantaoJudicialService;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.csjt.pje.view.action.ProcessoJTHome;
import br.jus.je.pje.entity.vo.ProcessoDocumentoVO;
import br.jus.pje.je.entidades.ComplementoProcessoJE;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.jt.entidades.AudImportacao;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.ProcessoJT;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.enums.ClassificacaoTipoSituacaoPautaEnum;
import br.jus.pje.jt.enums.TipoSituacaoPautaJTEnum;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.beans.criminal.TipoProcessoEnum;
import br.jus.pje.nucleo.dto.InformacaoCriminalDTO;
import br.jus.pje.nucleo.dto.ParteDTO;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.Cargo;
import br.jus.pje.nucleo.entidades.ClasseAplicacao;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ClasseJudicialAgrupamento;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.CompetenciaAreaDireito;
import br.jus.pje.nucleo.entidades.ComplementoClasse;
import br.jus.pje.nucleo.entidades.ComplementoClasseProcessoTrf;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrfSemFiltro;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.HistoricoDistribuicao;
import br.jus.pje.nucleo.entidades.HistoricoMotivoAcessoTerceiro;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRascunho;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.JurisdicaoMunicipio;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.NaturezaClet;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCompetencia;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoAlerta;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoClet;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoHistoricoClasse;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoSegredo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.entidades.ProcessoTrfConsultaSemFiltros;
import br.jus.pje.nucleo.entidades.ProcessoTrfImpresso;
import br.jus.pje.nucleo.entidades.ProcessoTrfRedistribuicao;
import br.jus.pje.nucleo.entidades.RevisorProcessoTrf;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.SalaHorario;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.TipoAudiencia;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoVisibilidade;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.entidades.lancadormovimento.ComplementoSegmentado;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.ClasseJudicialInicialEnum;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoReferenciaEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.enums.RelatorRevisorEnum;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.nucleo.enums.SimNaoFacultativoEnum;
import br.jus.pje.nucleo.enums.StatusAudienciaEnum;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;
import br.jus.pje.nucleo.enums.TipoDistribuicaoEnum;
import br.jus.pje.nucleo.enums.TipoNaturezaCletEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;
import br.jus.pje.nucleo.enums.TipoRedistribuicaoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.webservice.bean.consumidor.Anexo;
import br.jus.pje.webservice.bean.consumidor.Fornecedor;
import br.jus.pje.webservice.bean.consumidor.Reclamacao;
import br.jus.pje.webservice.bean.consumidor.Registro;
import br.jus.pje.webservice.client.ConsumidorRestClient;

@Name("processoTrfHome")
@BypassInterceptors
public class ProcessoTrfHome extends AbstractProcessoTrfHome<ProcessoTrf>{

	private static final String QUEBRA_DE_LINHA = "<br/>\n";

	private static final long serialVersionUID = -5169713421398909913L;

	public static final String EVENT_PROCESSO_DISTRIBUIDO = "eventProcessoDistribuido";
	
	private static final String SESSOES_JULGAMENTO_SEPARACAO_RESULTADO_LISTA = "</br>";
	private static final String SESSOES_JULGAMENTO_TIPO_RETORNO_CONSULTA_APELIDO = "apelido";
	private static final String SESSOES_JULGAMENTO_TIPO_RETORNO_CONSULTA_DATA_SESSAO = "dataSessao";
	private static final String SESSOES_JULGAMENTO_TIPO_RETORNO_CONSULTA_SITUACAO_JULGAMENTO = "situacaoJulgamento";
	private static final String SESSOES_JULGAMENTO_TIPO_CONSULTA_SITUACAO_TODOS = "Todos";
	private static final String SESSOES_JULGAMENTO_VALOR_RESULTADO_NAO_CADASTRADO = "Não cadastrado";

	private static final String QUEBRA_LINHA_HTML = "<br />";
	
	private static final String PROCESSO_ASSUNTO_VIEW_GRID = "processoAssuntoViewGrid";
	private static final String PROCESSO_ASSUNTO_CAD_PROCESSO_GRID = "processoAssuntoCadProcessoGrid";
	private static final String PROCESSO_ASSUNTO_GRID = "processoAssuntoGrid";
	
	private static final LogProvider log = Logging.getLogProvider(ProcessoTrfHome.class);
	private static final int HORAEMMINUTOS = 60;
	private List<ComplementoClasseProcessoTrf> compClasseProcessoTrfList;
	private boolean localizarProcessoReferencia;
	private boolean usuarioPodeVisualizarDadosProcessoReferencia;
	private Boolean isTrue = false;
	private AssuntoTrf selectedRowAssuntoPrincipal;
	private String classeJudicialFiltro;
	private Boolean dependenciaProcesso;
	private String numeroProcesso;
	private ProcessoTrf processoTrfIncidente;
	private List<ProcessoParte> partesList = new ArrayList<>(0);
	private Pessoa pessoaLogada;
	private ClasseJudicial classeJudicialAnterior;
	private Boolean mensagem = Boolean.FALSE;
	private Boolean mensagemTutelaLiminar = Boolean.FALSE;
	private Boolean mensagemPeticoes = Boolean.FALSE;
	private Boolean mensagemJusticaGratuita = Boolean.FALSE;
	private Boolean mensagemSigilo = Boolean.FALSE;
	private Boolean habilitaDesabiliaRadioSegredoJustica = Boolean.FALSE;
	private Boolean avulsaAnexada = Boolean.FALSE;
	private Boolean exibirTodosAlertas = false;
	private PrioridadeProcesso ultimaPrioridade;
	private PrioridadeProcesso prioridadeRetirada;
	private int pagina;
	private int idConsulta;
	private String nrProcessoConsulta;
	private Date dataDistribuicao1;
	private Date dataDistribuicao2;
	private Boolean prontoPauta;
	private Boolean prontoJulgamento;
	private String processo2Grau;
	private EnderecoWsdl enderecoWsdl;
	private Boolean revisado;
	private TipoDistribuicaoEnum tipoDistribuicao;
	private String mensagemProtocolacao;
	private ProcessoTrf processoTrf;
	private Boolean atualiza = Boolean.FALSE;
	private Boolean mudaClasse = Boolean.TRUE;
	private Boolean retificacao = Boolean.FALSE;
	private boolean btGravar = false;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgadorCargo orgaoJulgadorCargo;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private Cargo cargo;
	private List<ProcessoTrf> listaProcesso = new ArrayList<>(0);
	private Boolean marcouListTudo = Boolean.FALSE;
	private String numeroProcessoDistriuir;
	private Sessao sessao;
	private OrgaoJulgador orgaoJulgadorRevisor;
	private transient RemessaProcesso remessaProcesso;
	private Boolean remessaRecente = Boolean.FALSE;
	private Boolean remetidoSegundoGrau = null;
	private Boolean caValido = Boolean.FALSE;
	private String ca;
	private Boolean permissaoSessao = Boolean.FALSE;
	private Long idTaskInstance;
	private Boolean cachePermiteVisualizarProcesso = Boolean.FALSE;
	private Boolean peticionar = Boolean.FALSE;
	
	private List<ProcessoTrf> listaPedidoSegredo = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listaPedidoSigilo = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listaPedidoLiminar = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listaPedidoJustica = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listaPeticaoAvulsa = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listaAnalisePrevencao = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listaMandadoDevolvido = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listaProcessoComAudienciaNaoDesignada = new ArrayList<>(0);
	
	private Map<Integer, Boolean> mapaPendenciaRecuperacao = new HashMap<>();

	private Boolean checkAll = Boolean.FALSE;
	private Boolean checkAllAnalisePrevencao = Boolean.FALSE;
	private Boolean checkAllSigilo = Boolean.FALSE;
	private Boolean checkAllSegredo = Boolean.FALSE;
	private Boolean checkAllLiminar = Boolean.FALSE;
	private Boolean checkAllPeticaoAvulsa = Boolean.FALSE;
	private Boolean checkAllMandadoDevolvido = Boolean.FALSE;
	private Boolean checkAllProcessoComAudienciaNaoDesignada = Boolean.FALSE;
	private boolean exibeBotaoGravar = false;

	private ProcessoDocumentoPeticaoNaoLidaManager processoDocumentoPeticaoNaoLidaManager;
	private List<ProcessoDocumentoPeticaoNaoLida> processoDocumentoPeticaoNaoLida;
	
	private String assuntoTrfCompletoList;
	private Estado estadoMunicipioFatoPrincipal;
	private Boolean ehClasseCriminal;
	private Boolean ehClasseInfracional;

	private HistoricoMotivoAcessoTerceiro historicoAcessoTerceiros = new HistoricoMotivoAcessoTerceiro();
	private String linkConsulta;
	private String hashOS;
	private String dsUsuarioAcessouOS;
	private String oabProcOS;
	private Competencia competenciaConflito = null;
	private List<Competencia> competenciasPossiveis = new ArrayList<>();

	private boolean possuiTarefaAberta;
	private Boolean possuiAlertaProcesso = null;
	private Boolean exibeAbaPermissoes = null;
	private boolean isFaixaIncopativelAudiencia = false;
	private boolean isTelaCaracteristicaProcesso = false;
	private Competencia compEscolhida;

	//variavel usado apenas para a justiça do trabalho
	private Boolean aptoPauta;

	private boolean atendimentoEmPlantao = false;
	private boolean cadastroProcessoClet = false;
	private ProcessoClet processoClet;
	private String tipoNaturezaClet = "L";
	
	private String numeroProcessoClet;
	private Boolean possuiIncidentesSemJulgamentoClet = false;
	private List<ClasseJudicial> classeJudicialListClet;
	
	private List<UsuarioLocalizacaoVisibilidade> magistradoVisibilidade;
	private Boolean existemDocumentosParaConsolidar;
	private Set<Integer> documentosParaConsolidar;
	private Boolean selecionarTodosDocumentos;
	
	private static final int INCOMPATIBILIDADE_ASSUNTO = 1;
	private static final int INCOMPATIBILIDADE_PREVENCAO_ART_260 = 2;
	private int incompatibilidades = 0;
	private String mensagemAlerta;
	
	private Boolean isInserirPartesVisualizacao = false;
	private ClasseJudicialManager classeJudicialManager;
	
	private String mensagemErroProcessoIncidente = StringUtils.EMPTY;
	private ProcessoTrf processoEncontrado;
	
	private Boolean exibeCertidao;

	private Estado estadoJurisdicao;
	private Municipio municipioJurisdicao;
	private Eleicao eleicao;
	
	private boolean cadastraProcessoConsumidorGovBr = Boolean.FALSE;
	private List<Fornecedor> fornecedores;
	private Fornecedor reclamado;
	private List<Reclamacao> reclamacoes = new ArrayList<>();
	private String documentoReclamante;
	private Double valorCausa;
	private Boolean concordaTermosUsoServico;
	private PessoaFisica reclamante;
	private String urlConsumidorGovBr;
	
	private Boolean acaoAmbiental;
	boolean falhaNaPrevencaoExterna = false;	
	
	public void limpaAptoPautaJulgamento(){
		getInstance().setSelecionadoJulgamento(false);
		getInstance().setSelecionadoPauta(false);
		super.persist();
	}

	// Textos de saéda oficiais
	private String[] textosProtocolacao = {"", "", "", ""};
	
	// éndices mapeados no array 'textosProtocolacao'
	private static final int INDEX_DISTRIBUICAO = 0;
	private static final int INDEX_AUDIENCIA = 1;
	private static final int INDEX_AVISO = 2;
	private static final int INDEX_ERRO = 3;

	// Textos de status da Distribuição do processo
	private static final String TEXTO_DISTRIBUICAO_AUTOMATICA = "Processo distribuído com o número %s \npara o órgão %s.";
	private static final String TEXTO_DISTRIBUICAO_AUTOMATICA_PLANTAO = "Processo protocolizado com o número %s \n e encaminhado ao plantão judiciário.";	
	private static final String TEXTO_DISTRIBUICAO_MANUAL = "O processo %s foi encaminhado para a distribuição.";//[PJEII-1378]
	private static final String TEXTO_PROCESSO_CADASTRADO = "O processo informado já está cadastrado!";
	private static final String GRID_ASSUNTO_CAD_PROCESSO =  "processoAssuntoCadProcessoGrid";
	// Textos de status da marcao da audincia
	private static final String TEXTO_AUDIENCIA_AGENDADA = "Audiência (%s) designada para o dia: %s.";
	private static final String TEXTO_AUDIENCIA_NAO_AGENDADA = "Audiência inicial do processo não agendada automaticamente.";

	// Textos de aviso
	protected static final String TEXTO_AVISO_CLT = "Fica V. Sa. ciente, também por seu(s) constituinte(s), de que "
		+ "\ndeverá comparecer para a audiência designada, sendo passível, "
		+ "\nno caso de ausência, da aplicação do art. 844 da CLT.";
	private static final String TEXTO_AVISO_VALOR_CAUSA = "Salvo exceções legais, o valor da causa informado "
			+ "\nnão é compatével com a classe processual escolhida.";

	private static final String TEXTO_AVISO_TIPO_INDEFINIDO = "Tipo de audiência inicial não definido para a classe processual escolhida.";	
	
	private ProcessoHistoricoClasseManager processoHistoricoClasseManager;
	
	private transient ProtocolarDocumentoBean protocolarDocumentoBean;

	public boolean getPossuiTarefaAberta(){
		return possuiTarefaAberta;
	}

	public boolean getLocalizarProcessoReferencia(){
		return this.localizarProcessoReferencia;
	}

	public boolean isUsuarioPodeVisualizarDadosProcessoReferencia() {
		return usuarioPodeVisualizarDadosProcessoReferencia;
	}

	public void setUsuarioPodeVisualizarDadosProcessoReferencia(boolean usuarioPodeVisualizarDadosProcessoReferencia) {
		this.usuarioPodeVisualizarDadosProcessoReferencia = usuarioPodeVisualizarDadosProcessoReferencia;
	}

	public void setLocalizarProcessoReferencia(boolean localizarProcessoReferencia){
		this.localizarProcessoReferencia = localizarProcessoReferencia;
	}

	public void setCompetenciaConflito(Competencia competenciaConflito){
		getInstance().setCompetencia(competenciaConflito);
		this.competenciaConflito = competenciaConflito;
	}

	public Competencia getCompetenciaConflito(){
		return competenciaConflito;
	}

	public boolean possuiParteInativaPoloAtivo(){
		return possuiParteInativa(ProcessoParteParticipacaoEnum.A);
	}

	public boolean possuiParteInativaPoloPassivo(){
		return possuiParteInativa(ProcessoParteParticipacaoEnum.P);
	}

	public boolean possuiParteInativa(ProcessoParteParticipacaoEnum inParticipacao){
		return ComponentUtil.getComponent(ProcessoParteManager.class).possuiParteInativa(getInstance(), inParticipacao);
	}

	public String getDsUsuarioAcessouOS(){
		return dsUsuarioAcessouOS;
	}

	public void setDsUsuarioAcessouOS(String dsUsuarioAcessouOS){
		this.dsUsuarioAcessouOS = dsUsuarioAcessouOS;
	}

	public String getOabProcOS(){
		return oabProcOS;
	}

	public void setOabProcOS(String oabProcOS){
		this.oabProcOS = oabProcOS;
	}

	public Cargo getCargo(){
		return cargo;
	}

	public void setCargo(Cargo cargo){
		this.cargo = cargo;
	}

	public OrgaoJulgadorCargo getOrgaoJulgadorCargo(){
		return orgaoJulgadorCargo;
	}

	public void setOrgaoJulgadorCargo(OrgaoJulgadorCargo orgaoJulgadorCargo){
		this.orgaoJulgadorCargo = orgaoJulgadorCargo;
	}

	public OrgaoJulgador getOrgaoJulgador(){
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador){
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado(){
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado){
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoEvento> getListaProcessoEvento(ProcessoTrf processoTrf){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoEvento o ");
		sb.append("where o.processo = :processo");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processo", processoTrf.getProcesso());
		return q.getResultList();
	}

	public ProcessoEvento getUltimoProcessoEvento(ProcessoTrf processoTrf){
		return ProcessoHome.instance().getUltimoProcessoEvento(processoTrf.getProcesso());
	}

	public Boolean verificaUltimoEventoConclusao(){
		return verificaUltimoEventoConclusao(getInstance());
	}

	public Boolean verificaUltimoEventoConclusao(ProcessoTrf processoTrf){
		return ProcessoHome.instance().verificaUltimoEvento(processoTrf.getProcesso(),
				ParametroUtil.instance().getEventoConclusao());
	}

	public Boolean verificaUltimoEventoDistribuicaoRedistribuicao(){
		return verificaUltimoEventoDistribuicaoRedistribuicao(instance);
	}

	public Boolean verificaUltimoEventoDistribuicaoRedistribuicao(ProcessoTrf processoTrf){
		Evento eventoTipoDistribuicao = ParametroUtil.instance().getEventoTipoDistribuicao();
		Evento eventoTipoRedistribuicao = ParametroUtil.instance().getEventoTipoRedistribuicao();

		return ProcessoHome.instance().verificaUltimoEvento(processoTrf.getProcesso(), eventoTipoDistribuicao)
			|| ProcessoHome.instance().verificaUltimoEvento(processoTrf.getProcesso(), eventoTipoRedistribuicao);
	}

	public Boolean verificaUltimoEventoDistribuicao(){
		Evento eventoTipoDistribuicao = ParametroUtil.instance().getEventoTipoDistribuicao();
		return ProcessoHome.instance().verificaUltimoEvento(instance.getProcesso(), eventoTipoDistribuicao);
	}

	public boolean possuiOrgaoJulgador(){
		if (!isManaged()){
			setId(ProcessoHome.instance().getId());
		}
		return getInstance().getOrgaoJulgador() != null;
	}

	public Boolean vericaEventoConclusao(){
		return vericaEventoConclusao(getInstance());
	}

	public Boolean vericaEventoConclusao(ProcessoTrf processoTrf){
		Evento eventoConclusao = ParametroUtil.instance().getEventoConclusao();
		return ProcessoHome.instance().verificaEvento(processoTrf.getProcesso(), eventoConclusao);
	}

	public Boolean vericaEventoDistribuicaoRedistribuicao(){
		Evento eventoTipoDistribuicao = ParametroUtil.instance().getEventoTipoDistribuicao();
		Evento eventoTipoRedistribuicao = ParametroUtil.instance().getEventoTipoRedistribuicao();
		return ProcessoHome.instance().verificaEvento(instance.getProcesso(), eventoTipoDistribuicao)
			|| ProcessoHome.instance().verificaEvento(instance.getProcesso(), eventoTipoRedistribuicao);
	}


	public void setarInstancia(){
		if (!isIdDefined()){
			Integer idProcesso = ProcessoHome.instance().getInstance().getIdProcesso();
			setId(idProcesso);
		}
	}

	public void setarInstanciaPainelProcurador(){
		Integer idProcesso = ProcessoHome.instance().getInstance().getIdProcesso();
		setId(idProcesso);
	}

	public Boolean getDependenciaProcesso(){
		if (getInstance().getProcesso().getNumeroProcessoOrigem() != null){
			dependenciaProcesso = true;
		}
		else{
			dependenciaProcesso = false;
		}
		return dependenciaProcesso;
	}

	public void setDependenciaProcesso(Boolean dependenciaProcesso){
		if (!dependenciaProcesso){
			Contexts.removeFromAllContexts("processoOrigemSuggest");
			getInstance().getProcesso().setNumeroProcessoOrigem(null);
		}
		this.dependenciaProcesso = dependenciaProcesso;
	}

	public String getClasseJudicialFiltro(){
		return classeJudicialFiltro;
	}

	public void setClasseJudicialFiltro(String classeJudicialFiltro){
		this.classeJudicialFiltro = classeJudicialFiltro;
	}

	private void iniciarComplementoClasse(){
		if (compClasseProcessoTrfList != null){
			int tam = compClasseProcessoTrfList.size();
			for (int i = 0; i < tam; i++){
				if (compClasseProcessoTrfList.get(i).getValorComplementoClasseProcessoTrf() == null){
					compClasseProcessoTrfList.get(i).setValorComplementoClasseProcessoTrf("");
				}
			}
		}
	}

	public Boolean mostrarComplementoClasse(){
		iniciarComplementoClasse();

		if (compClasseProcessoTrfList != null){
			int tam = compClasseProcessoTrfList.size();
			int som = 0;
			for (int i = 0; i < tam; i++){
				if (compClasseProcessoTrfList.get(i).getValorComplementoClasseProcessoTrf().equals("")){
					som++;
				}
			}

			return !(tam == som);
		}
		else{
			return false;
		}
	}

	public Boolean verificarSegredo(){
		Boolean classeJudicialSegredoJustica = verificarClasseSigilosa();
		if ((getInstance().getSegredoJustica() != null) && (getInstance().getSegredoJustica()) || classeJudicialSegredoJustica){
			return Boolean.TRUE;
		}
		else{
			return Boolean.FALSE;
		}
	}

	private Boolean verificarClasseSigilosa() {
		Boolean classeJudicialSegredoJustica = getInstance().getClasseJudicial() != null && 
				getInstance().getClasseJudicial().getSegredoJustica() != null && 
				getInstance().getClasseJudicial().getSegredoJustica();
		return classeJudicialSegredoJustica;
	}

	public String getNumeroProcessoOrigem(){
		if (getInstance().getProcesso() != null){
			return getInstance().getProcesso().getNumeroProcessoOrigem();
		}
		else
			return "";
	}

	public List<ProcessoParte> getListaAtivos(){
		return getInstance().getListaPartePoloObj(ProcessoParteParticipacaoEnum.A);
	}

	public List<ProcessoParte> getListaPassivos(){
		return getInstance().getListaPartePoloObj(ProcessoParteParticipacaoEnum.P);
	}

	public Boolean mostrarCaracteristicas(){
		Boolean ret = false;
		if (getInstance().getSegredoJustica() != null){
			ret = true;
		}
		return ret;
	}

	// Listas das Informações primárias do processo
	public List<ProcessoTrf> getProcessoList(){
		List<ProcessoTrf> processoLista = new ArrayList<ProcessoTrf>(0);
		processoLista.add(getInstance());
		return processoLista;
	}

	public int getPagina(){
		return pagina;
	}

	public void setPagina(int pagina){
		this.pagina = pagina;
	}

	public List<ProcessoTrf> getListaProcessoComAudienciaNaoDesignada() {
		return listaProcessoComAudienciaNaoDesignada;
	}
	

	public void setListaProcessoComAudienciaNaoDesignada(
			List<ProcessoTrf> listaProcessoComAudienciaNaoDesignada) {
		this.listaProcessoComAudienciaNaoDesignada = listaProcessoComAudienciaNaoDesignada;
	}

	public Boolean getCheckAllProcessoComAudienciaNaoDesignada() {
		return checkAllProcessoComAudienciaNaoDesignada;
	}

	public void setCheckAllProcessoComAudienciaNaoDesignada(
			Boolean checkAllProcessoComAudienciaNaoDesignada) {
		this.checkAllProcessoComAudienciaNaoDesignada = checkAllProcessoComAudienciaNaoDesignada;
	}

	public Boolean verificarInicial(){

		// Este if se é necessário para os registros antigos, que não possuem
		// classe Judicial (campo que é obrigatório atualmente)
		String classeJudicial = "";
		if (getInstance().getClasseJudicial() != null){
			classeJudicial = getInstance().getClasseJudicial().getClasseJudicial();
		}

		ClasseJudicialInicialEnum cj = ClasseJudicialInicialEnum.I;
		if (getInstance().getInicial() != null){
			cj = getInstance().getInicial();
		}

		if ((cj.equals(ClasseJudicialInicialEnum.D)) || (cj.equals(ClasseJudicialInicialEnum.R))
			|| (classeJudicial.equals("Inicial Incidental")) || (classeJudicial.equals("Inicial Recursal"))){
			return Boolean.TRUE;
		}
		else{
			return Boolean.FALSE;
		}
	}

	@Override
	public void newInstance(){
		processoTrfIncidente = null;
		partesList = new ArrayList<ProcessoParte>(0);
		Contexts.removeFromAllContexts("classeAplicacaoProcessoTrfSuggest");
		Contexts.removeFromAllContexts("processoOriginarioSuggest");
		super.newInstance();
		getInstance().setInicial(ClasseJudicialInicialEnum.I);
		compClasseProcessoTrfList = new ArrayList<ComplementoClasseProcessoTrf>();
		setClasseJudicialAnterior(null);
		String desProcReferencia = Util.getRequestParameter("desProcReferencia"); 
		if (desProcReferencia != null) {
			getInstance().setDesProcReferencia(desProcReferencia);
			setProcessoEncontrado(ProcessoJudicialManager.instance().pesquisarProcessoPJE(getInstance().getDesProcReferencia(), true));
		}
		getInstance().setNivelAcesso(0);
	}
	
	private void iniciarJurisdicao(){
		SelectItemsQuery jurisdicaoitens = (SelectItemsQuery) ComponentUtil.getComponent("jurisdicaoItems");
		if (jurisdicaoitens.getResultList().size() == 1) {
			getInstance().setJurisdicao((Jurisdicao) jurisdicaoitens.getResultList().get(0));
		}
	}

	public void setarClasseAplicacao(){
		iniciarJurisdicao();
		if (getInstance().getClasseJudicial() != null){
			String sql = "select o.aplicacaoClasse from OrgaoJulgador o " + "where o.jurisdicao = :jurisdicao and "
				+ "o.ativo = true";
			Query q = getEntityManager().createQuery(sql);
			q.setParameter("jurisdicao", getInstance().getJurisdicao());
			AplicacaoClasse ac = EntityUtil.getSingleResult(q);
			if (ParametroUtil.instance().isPrimeiroGrau() && ac == null){
				FacesMessages.instance().add(
						Severity.ERROR,
						"Não foi encontrada aplicação classe para jurisdição escolhida: "
							+ getInstance().getJurisdicao());
			}

			String sqlCA = "select o from ClasseAplicacao o " + "where o.aplicacaoClasse = :aplicacaoClasse "
				+ "and o.classeJudicial = :classeJudicial";
			Query qca = getEntityManager().createQuery(sqlCA);
			qca.setParameter("aplicacaoClasse", ac);
			qca.setParameter("classeJudicial", getInstance().getClasseJudicial());
			ClasseAplicacao ca = EntityUtil.getSingleResult(qca);
			if (ParametroUtil.instance().isPrimeiroGrau() && ca == null){
				FacesMessages.instance().add(
						Severity.ERROR,
						"Não foi encontrada Classe Aplicacao para aplicação Classe  " + ac + " e Classe Judicial "
							+ getInstance().getClasseJudicial());
			}

			ClasseAplicacaoHome.instance().setInstance(ca);
		}
	}
	
	public void existeProcessoPJE() {
		ProcessoTrf processo = ProcessoJudicialManager.instance().pesquisarProcessoPJE(getInstance().getDesProcReferencia(), true);
		setProcessoEncontrado(processo);
		if (processo != null) getInstance().setProcessoReferencia(processo);
	}

	/**
	 * Metodo que identifica o advogado logado ou o advogado do assistente logado e persiste como uma parte do processo, pois  obrigatório que este
	 * advogado conste no polo ativo
	 * 
	 * @param processo
	 * @return
	 */
	public void persistAdvogadoLogado() {
		TipoParte tipoParte = getTipoParteClasseJudicial();
		if (tipoParte != null) {
			List<PessoaAdvogado> listaAdv = advogadosLocalizacao();
			if(listaAdv.size() > 0) {
				PessoaAdvogado advogado = listaAdv.get(0);
				ProcessoParteHome.instance().getInstance().setInParticipacao(ProcessoParteParticipacaoEnum.A);
				ProcessoParteHome.instance().getInstance().setPessoa(advogado.getPessoa());
				ProcessoParteHome.instance().getInstance().setProcessoTrf(getInstance());
				ProcessoParteHome.instance().getInstance().setTipoParte(tipoParte);
				ProcessoParteHome.instance().persist();
			}
		}
	}

	public String persistProcessClient(Processo processo){
		getInstance().setIdProcessoTrf(processo.getIdProcesso());
		getInstance().setProcesso(processo);
		
		definirInstancia();

		validaJusticaEleitoralPrimeiroGrau();

		String persist = persist();
		if (persist != null){
			if(Authenticator.isAdvogado() || Authenticator.isAssistenteAdvogado()) {
				this.persistAdvogadoLogado();
			} else if (Authenticator.isJusPostulandi()) {
				ProcessoParteHome.instance().setCodInParticipacao(ProcessoParteParticipacaoEnum.A.name());
				ProcessoParteHome.instance().inserir(Authenticator.getPessoaLogada(), Boolean.TRUE);
			}
		}

		ProcessoParteHome.instance().verificarExigenciaFiscalDaLei(getInstance());

		return persist;
	}

	public String insert(){
		if (getInstance().getInicial() == null){
			getInstance().setInicial(ClasseJudicialInicialEnum.I);
		}

		ProcessoHome procHome = getProcessoHome();
		Processo processo = procHome.criarProcesso();
		return persistProcessClient(processo);
	}

	public void incluirProcesso() {
		iniciarClasseJudicial();
		Processo proc = ProcessoHome.instance().getInstance();
//		validaSeProcessoEproc(proc);
		if (numeroProcessoNormatizado(proc) && !verificarProcessoReferencia(getInstance().getDesProcReferencia()) && !validaSeProcessoEproc(proc)) {
			if (!protocolacaoValida() ) {
				FacesMessages.instance().clear();
				FacesMessages.instance().addFromResourceBundle(Severity.INFO, TEXTO_PROCESSO_CADASTRADO);
				return;
			}
			String msg = insert();
			validaRedirecionamentoConsumidorGov(msg);
		}
	}

	private void validaRedirecionamentoConsumidorGov(String msg) {
		if (msg != null) {
			boolean cadastraProcessoConsumidorGovBr = ParametroUtil.instance().getIntegracaoConsumidorGovBr() && 
				AssuntoTrfManager.instance().isAreaDiretoConsumo(this.instance.getIdAreaDireito());
			
			Redirect redirect = Redirect.instance();
			redirect.setViewId("/Processo/update.seam");
			redirect.setParameter("idProcesso", ProcessoHome.instance().getInstance().getIdProcesso());
			redirect.setParameter("tab", cadastraProcessoConsumidorGovBr ? "consumidorGovBr" : "assunto");
			redirect.setParameter("cadastraProcessoConsumidorGovBr", cadastraProcessoConsumidorGovBr);
			redirect.setParameter("documentoReclamante", Authenticator.isJusPostulandi() ? 
				Authenticator.getPessoaLogada().getDocumentoCpfCnpj() : null);
			
			Optional<String> cookie = PjeUtil.instance().getCookie(Constantes.CONCORDA_TERMOS_INTEGRACAO_PJE_CONSUMIDOR);
			if (cookie.isPresent()) {
				redirect.setParameter("concordaTermosUsoServico", BooleanUtils.toBooleanObject(cookie.get()));
			}
			
			redirect.setConversationPropagationEnabled(false);
			redirect.execute();
		}
	}
	
	/**
	 *  Metodo responsável por inicializar as variáveis de instância "jurisdicao" e "classeJudicial"
	 *  da entidade "ProcessoTrf" quando o valor do atributo "areasDireito" for alterado.
	 */
	public void inicializaVariaveisCadastroProcesso() {
		this.instance.setJurisdicao(null);
		this.instance.setClasseJudicial(null);
	}
	
	/**
	 * Metodo responsável por carregar previamente a lista de fornecedores.
	 */
	public void carregaFornecedores() {
		try {
			this.fornecedores = ComponentUtil.getComponent(ConsumidorRestClient.class).listaFornecedores();
		} catch (Exception e) {
			this.cadastraProcessoConsumidorGovBr = Boolean.FALSE;
			this.setTab("assunto");
			
			log.error(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, "Não foi possével acessar o sistema CONSUMIDOR.GOV.BR. Por favor, tente mais tarde.");
		}
	}
	
	/**
	 * Metodo responsável por retornar a lista de fornecedores.
	 * 
	 * @param suggest Filtro.
	 * @return Lista de fornencedores.
	 */
	public List<Fornecedor> filtraFornecedores(Object suggest) {
		List<Fornecedor> result = new ArrayList<>();
		
		if (this.fornecedores != null) {
			result = fornecedores.stream().filter(
				empresa -> StringUtils.containsIgnoreCase(empresa.getDescricao(), (String)suggest)).collect(Collectors.toList());
		}
		
		return result;
	}
	
	/**
	 * Metodo responsável por iniciar o procedimento de cadastro da reclamação no sistema CONSUMIDOR.GOV.BR 
	 */
	public void iniciaReclamacao() {
		if (BooleanUtils.isFalse(this.concordaTermosUsoServico)) {
			FacesMessages.instance().add(Severity.ERROR, "Vocé deve concordar com os termos de uso do serviéo.");
		} else if (this.reclamado == null || this.reclamado.getCodigo() == null) {
			FacesMessages.instance().add(Severity.ERROR, "Nenhum fornecedor foi selecionado.");
		} else {
			PjeUtil.instance().setCookie(
				Constantes.CONCORDA_TERMOS_INTEGRACAO_PJE_CONSUMIDOR, this.concordaTermosUsoServico.toString(), -1);
			
			this.reclamante = Authenticator.isJusPostulandi() ? (PessoaFisica)Authenticator.getPessoaLogada() : 
				(PessoaFisica)PreCadastroPessoaBean.instance().pesquisaPessoa(TipoPessoaEnum.F, this.documentoReclamante);
			
			if (this.reclamante.getDocumentoCpfCnpj() != null) {
				try {
					this.reclamacoes = ComponentUtil.getComponent(ConsumidorRestClient.class).listaReclamacoes(
						Long.parseLong(StringUtil.removeNaoNumericos(this.documentoReclamante)), this.reclamado.getCodigo());
					
				} catch (Exception e) {
					this.reclamacoes = new ArrayList<>();
					log.error(e.getLocalizedMessage());
				}
				
				if (this.reclamacoes.isEmpty()) {
					this.recuperaURL();
				}
			}
		}
	}
	
	/**
	 * Metodo responsável por recuperar a URL de acesso ao sistema CONSUMIDOR.GOV.BR
	 * 
	 * @return URL de acesso ao sistema CONSUMIDOR.GOV.BR
	 */
	public void recuperaURL() {
		try {
			StringBuilder url = new StringBuilder(ComponentUtil.getComponent(ConsumidorRestClient.class).recuperaURL())
				.append("&codigoEmpresa=").append(this.reclamado.getCodigo())
				.append("&cpfConsumidor=").append(StringUtil.removeNaoNumericos(this.reclamante.getDocumentoCpfCnpj()));
			
			if (StringUtils.isNotBlank(this.reclamante.getNome())) {
				url.append("&nomeConsumidor=").append(this.reclamante.getNome());
			}
			if (StringUtils.isNotBlank(this.reclamante.getEmail())) {
				url.append("&emailConsumidor=").append(this.reclamante.getEmail());
			}
			if (this.reclamante.getSexo() != null) {
				url.append("&sexoConsumidor=").append(this.reclamante.getSexo());
			}
			if (this.reclamante.getDataNascimento() != null) {
				url.append("&dataNascimentoConsumidor=").append(this.reclamante.getDataNascimentoFormatada());
			}
			if (!Authenticator.isAdvogado() && !this.getReclamante().getEnderecoList().isEmpty()) {
				url.append("&cepConsumidor=").append(this.getReclamante().getEnderecoList().get(0).getCep().getNumeroCep());
			}
			
			this.urlConsumidorGovBr = url.toString();
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
		}
	}
	
	/**
	 * Metodo responsável por direcionar o usuário para o cadastro de processo padrão.
	 */
	public void configuraCadastroProcessoPadrao() {
		this.cadastraProcessoConsumidorGovBr = false;
		this.setTab("assunto");
	}
	
	@Transactional
	public void cadastraProcesso(String protocoloConsumidorGovBr) {
		try {
			Reclamacao reclamacao = ComponentUtil.getComponent(ConsumidorRestClient.class)
					.detalhaReclamacao(Long.parseLong(protocoloConsumidorGovBr));
			
			this.instance.setValorCausa(this.valorCausa);

			// Assunto
			this.instance.getProcessoAssuntoList().add(ProcessoAssuntoManager.instance().criaNovo(
					ComponentUtil.getComponent(AssuntoTrfManager.class).findByCodigo(Constantes.CODIGO_ASSUNTO_CONSUMIDOR_GOV_BR), this.instance));

			// Reclamante
			ProcessoParte reclamante = ProcessoParteManager.instance().criaNovo(this.instance, ProcessoParteParticipacaoEnum.A, this.reclamante);
			this.instance.getProcessoParteList().add(reclamante);
			
			if (Authenticator.isAdvogado()) {
				// Representante do reclamado
				Optional<ProcessoParte> representante = this.instance.getProcessoParteList().stream().filter(
					processoParte -> processoParte.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())).findFirst();
				
				if (representante.isPresent()) {
					reclamante.getProcessoParteRepresentanteList().add(ProcessoParteRepresentanteManager.instance().criaNovo(
						reclamante, representante.get(), Authenticator.getPessoaLogada(), representante.get().getTipoParte()));
				}
			}
			
			// Reclamado
			this.instance.getProcessoParteList().add(ProcessoParteManager.instance().criaNovo(
				this.instance, ProcessoParteParticipacaoEnum.P, (PessoaJuridica)PreCadastroPessoaBean.instance().pesquisaPessoa(
					TipoPessoaEnum.J, InscricaoMFUtil.mascaraCnpj(
						InscricaoMFUtil.preencheComZerosAEsquerda(reclamacao.getReclamado().getDocumento().toString(), "00000000000000")))));
			
			// Documento
			if (this.instance.getProcesso().getProcessoDocumentoList().isEmpty()) {
				ProcessoDocumento documentoPrincipal = DocumentoJudicialService.instance().criaDocumentoPrincipalAssinado(
					this.instance, reclamacao.getTexto().replace("${valor}", StringUtil.formatarValorMoeda(this.valorCausa, Boolean.TRUE)));
				
				TipoProcessoDocumento tipo = ComponentUtil.getComponent(TipoProcessoDocumentoManager.class).findByCodigoDocumento(
					Constantes.COD_TIPO_DOCUMENTO_ANEXO_CONSUMIDOR_GOV_BR, Boolean.TRUE);
				
				for (Anexo anexo : reclamacao.getAnexos()) {
					DocumentoJudicialService.instance().criaDocumentoAnexoAssinado(documentoPrincipal, anexo.getNome(), tipo, 
						anexo.getTamanho().intValue(), anexo.getArquivoBase64(), MimetypeUtil.getMimetypePdf());
				}
				this.getEntityManager().flush();
			}
			
			Events.instance().raiseEvent(ProgressoProtocoloWebSocket.ATUALIZA_PROGRESSO_PROTOCOLO, 
				this.instance.getIdProcessoTrf(), "protocolo_estagio_1");

		    Contexts.getEventContext().set(Variaveis.PJE_FLUXO_VARIABLES_STARTSTATE, 
		    		Collections.singletonMap("pje:fluxo:protocolo:CONSUMIDOR.GOV.BR", reclamacao.getProtocolo()));

			this.protocolar();

			Events.instance().raiseEvent(ProgressoProtocoloWebSocket.ATUALIZA_PROGRESSO_PROTOCOLO, 
				this.instance.getIdProcessoTrf(), "protocolo_estagio_2");
			
			Events.instance().raiseAsynchronousEvent(ConsumidorRestClient.REGISTRA_RECLAMACAO, new Registro(
				this.instance.getNumeroProcesso(), reclamacao.getProtocolo(), reclamacao.getDataAbertura()));
			
			this.textosProtocolacao[INDEX_AVISO] = FacesUtil.getMessage("processoTrf.protocolo.consumidorGovBr", (Object) reclamacao.getProtocolo().toString());
			
			this.validado = Boolean.TRUE;
			
			FacesMessages.instance().clear();
		} catch (Exception e) {
			this.validado = Boolean.FALSE;
			
			this.instance.setProcessoStatus(ProcessoStatusEnum.E);
			
			this.instance.getProcessoAssuntoList().clear();
			
			for (ProcessoParte processoParte : this.instance.getProcessoParteList()) {
				EntityUtil.getEntityManager().remove(processoParte);
			}
			this.instance.getProcessoParteList().clear();
			
			for (ProcessoDocumento processoDocumento: this.instance.getProcesso().getProcessoDocumentoList()) {
				EntityUtil.getEntityManager().remove(processoDocumento);
			}
			this.instance.getProcesso().getProcessoDocumentoList().clear();
			
			log.error(e.getLocalizedMessage());
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
		}
	}
	
	private ProcessoSegredoManager getProcessoSegredoManager(){ 
		ProcessoSegredoManager processoSegredoManager = ComponentUtil.getComponent(ProcessoSegredoManager.NAME); 
		return processoSegredoManager; 
	} 
	
	/**
	 * Metodo responsável por verificar se o processo de referência informado pertence a esta instalação do Pje.
	 * 
	 * @param numeroProcessoReferencia Número do processo de referência.
	 * @return Verdadeiro se o processo de referência pertence a esta instalação do PJe. Falso, caso contrário.
	 */
	private boolean verificarProcessoReferencia(String numeroProcessoReferencia) {
		ProcessoTrfManager processoTrfManager = ComponentUtil.getComponent(ProcessoTrfManager.NAME);
		
		ProcessoTrf processoTrf = processoTrfManager.recuperarProcesso(
				getInstance().getDesProcReferencia(), ClasseJudicialInicialEnum.D, ClasseJudicialInicialEnum.I);
		if (processoTrf != null) {
			FacesMessages.instance().add(
					StatusMessage.Severity.ERROR, FacesUtil.getMessage("entity_messages", "classeJudicial.processoIncorreto"));	
			
			return true;
		}
		return false;
	}

	@Override
	public String persist(){
		getInstance().setSegredoJustica(getInstance().getClasseJudicial() != null && 
			getInstance().getClasseJudicial().getSegredoJustica() != null && getInstance().getClasseJudicial().getSegredoJustica());
		
		getInstance().setJusticaGratuita(false);
		getInstance().setTutelaLiminar(false);
		getInstance().setLocalizacaoInicial(Authenticator.getLocalizacaoFisicaAtual());
		getInstance().setEstruturaInicial(Authenticator.getLocalizacaoModeloAtual());
		String persist = super.persist();
		if (persist != null){
			ProcessoTrfImpresso processoTrfImpresso = new ProcessoTrfImpresso();
			processoTrfImpresso.setProcessoTrf(instance);
			processoTrfImpresso.setIdProcessoTrf(instance.getIdProcessoTrf());
			processoTrfImpresso.setImpresso(false);
			getEntityManager().persist(processoTrfImpresso);
		}
		EntityUtil.flush();
		if (persist != null){
			FacesMessages.instance().clear();
		}
		return persist;
	}

	public TipoParte getTipoParteClasseJudicial(){
		return getTipoParteClasseJudicial(null);
	}

	@SuppressWarnings("unchecked")
	public TipoParte getTipoParteClasseJudicial(ProcessoTrf processoTrf){
		TipoParte tipoParte = null; 
		String query = "SELECT tpc.tipoParte "
				+ " FROM ProcessoTrf p "
				+ " JOIN p.classeJudicial cj "
				+ " JOIN cj.tipoParteConfigClJudicial tpcj "
				+ " JOIN tpcj.tipoParteConfiguracao tpc "
				+ "where p = :processoTrf and tpc.tipoParte = :tipoParte and tpc.poloAtivo = true ";
		Query q = EntityUtil.getEntityManager().createQuery(query);
		q.setParameter("processoTrf", processoTrf == null ? getInstance() : processoTrf);
		q.setParameter("tipoParte", ParametroUtil.instance().getTipoParteAdvogado());
		List<TipoParte> list = q.getResultList();
		if (list.size() != 0){
			tipoParte = list.get(0);
		}
		return tipoParte;
	}

	private ProcessoHome getProcessoHome(){
		return ProcessoHome.instance();
	}

	public String persistIncidente(Processo processo){
		getInstance().setInicial(ClasseJudicialInicialEnum.D);
		getInstance().setIsIncidente(Boolean.TRUE);
		getInstance().setIdProcessoTrf(processo.getIdProcesso());
		getInstance().setProcesso(processo);
		
		getInstance().setValorCausa(instance.getValorCausaIncidente());
		String persist = persist();
		if (persist != null){
			ProcessoParteHome.instance().verificarExigenciaFiscalDaLei(getInstance());

			// Inserindo o advogado criador do Processo como uma parte ativa do
			// processo, do tipo advogado, caso exista.
			TipoParte tp = getTipoParteClasseJudicial();
			Pessoa pessoaLogada = (Pessoa) Contexts.getSessionContext().get("pessoaLogada");
			if (tp != null && Pessoa.instanceOf(pessoaLogada, PessoaAdvogado.class) && 
					Authenticator.getPapelAtual().equals(ParametroUtil.instance().getPapelAdvogado())){

				ProcessoParteHome.instance().getInstance().setInParticipacao(ProcessoParteParticipacaoEnum.A);
				ProcessoParteHome.instance().getInstance().setPessoa(pessoaLogada);
				ProcessoParteHome.instance().getInstance().setProcessoTrf(getInstance());
				ProcessoParteHome.instance().getInstance().setTipoParte(tp);
				getInstance().getProcessoParteList().add(ProcessoParteHome.instance().getInstance());
				ProcessoParteHome.instance().persist();
			}
			else{
				List<PessoaAdvogado> listaAdv = advogadosLocalizacao();
				tp = ParametroUtil.instance().getTipoParteAdvogado();
				if (listaAdv.size() == 1){
					ProcessoParteHome.instance().getInstance().setInParticipacao(ProcessoParteParticipacaoEnum.A);
					ProcessoParteHome.instance().getInstance().setPessoa(listaAdv.get(0).getPessoa());
					ProcessoParteHome.instance().getInstance().setProcessoTrf(getInstance());
					ProcessoParteHome.instance().getInstance().setTipoParte(tp);
					ProcessoParteHome.instance().persist();
				}
			}
			getEntityManager().flush();
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "ProcessoTrf_created");
		}
		return persist;
	}

	public List<PessoaAdvogado> advogadosLocalizacao(){
		EntityList<UsuarioLocalizacao> lista = ComponentUtil.getComponent(PessoaAdvogadoParteList.class);
		List<PessoaAdvogado> listaAdv = new ArrayList<PessoaAdvogado>(0);
		for (UsuarioLocalizacao ul : lista.getResultList()){
			PessoaAdvogado pa = EntityUtil.find(PessoaAdvogado.class, ul.getUsuario().getIdUsuario());
			if (pa != null){
				listaAdv.add(EntityUtil.find(PessoaAdvogado.class, ul.getUsuario().getIdUsuario()));
			}
		}
		return listaAdv;
	}

	@Override
	public void setId(Object id){
		boolean changed = (id != null && !id.equals(getId()));
		if (id instanceof String){
			String idS = (String) id;
			try{
				id = Integer.parseInt(idS);
			} catch (NumberFormatException e){
				log.error("Id inválido: setId(" + id + ")", e);
				id = 0;
			}
		}
		super.setId(id);
		if (id != null && getInstance().getProcesso() != null){
			getProcessoHome().setInstance(getInstance().getProcesso());
			// usado para sessao da justica do trabalho
			if(getInstance().getSelecionadoJulgamento()){
				aptoPauta = false;
			}else if(getInstance().getSelecionadoPauta()){
				aptoPauta = true;
			}
		}
		if (changed){
			setClasseJudicialAnterior(getInstance().getClasseJudicial());
		}
		if (changed || id == null){
			compClasseProcessoTrfList = null;
		}
		if(getInstance().getDesProcReferencia() != null){
			existeProcessoPJE();
		}
		if (getInstance().getProcessoOriginario() != null) {
			setProcessoEncontrado(getInstance().getProcessoOriginario());
		}
		if(getInstance().getOrgaoJulgador() != null) {
			setOrgaoJulgador(getInstance().getOrgaoJulgador());
		}
		if(getInstance().getCompetencia() != null) {
			setCompetenciaConflito(getInstance().getCompetencia());
		}
		if(id != null && changed && this.getMostraDadosEleicao() && this.getInstance().getComplementoJE() != null) {
			this.setEstadoJurisdicao(this.getInstance().getComplementoJE().getEstadoEleicao());
			this.setMunicipioJurisdicao(this.getInstance().getComplementoJE().getMunicipioEleicao());
			this.setEleicao(this.getInstance().getComplementoJE().getEleicao());
		}
	}

	private ProcessoOrigemSuggestBean getProcessoOrigemSuggest(){
		return getComponent("processoOrigemSuggest");
	}

	@Override
	protected boolean beforePersistOrUpdate(){
		/*[PJEII-2985] Magistrado não consegue assinar atas de audiência
		 * Adicionado bloco try/exception com rollback na captura de exceção, pois no caso de algum erro os dados eram perisitidos parcialmente. 
		 */
		try {
			if (instance.getProcessoReferencia() != null && instance.getClasseJudicial() != null
				&& !getInstance().getSegredoJustica()){
				instance.setSegredoJustica(instance.getClasseJudicial().getSegredoJustica());
			}
	
			alterarTipoParte();
			if(getInstance().getProcessoAssuntoList().size() > 0) {
				removerAssuntosMudancaCompetencia();
			}
			instance.getProcesso().setNumeroProcessoOrigem(getProcessoOrigemSuggest().getInstance());
			getProcessoOriginarioSuggest().getInstance();
			if (getInstance().getProcessoStatus() == null){
				instance.setProcessoStatus(ProcessoStatusEnum.E);
			}
	
			// Código referente ao complemento da Classe
			// =====================================================================
			if (compClasseProcessoTrfList != null){
	
				Integer listSize = compClasseProcessoTrfList.size();
				for (int i = 0; i < listSize; i++){
					if (compClasseProcessoTrfList.get(i).getValorComplementoClasseProcessoTrf() == null){
						compClasseProcessoTrfList.get(i).setValorComplementoClasseProcessoTrf("");
					}
				}
				getInstance().setComplementoClasseProcessoTrfList(compClasseProcessoTrfList);
				compClasseProcessoTrfList = null;
	
			}
			// =====================================================================
		}catch (Exception e) {
			log.error("Erro ao realizar ProcessoTrfHome.beforePersistOrUpdate(): " + e.getLocalizedMessage());
			try {
				if (Transaction.instance().isActive()) {
					Transaction.instance().setRollbackOnly();
				}
			} catch (Exception e1) {
				log.error("Erro ao tratar exceééo em ProcessoTrfHome.beforePersistOrUpdate(): " + e1.getLocalizedMessage());
			} 
		}
		return super.beforePersistOrUpdate();
	}

	public void updatePermissaoSegredoJustica(){
		super.update();
	}

	/**
	 * Pega a primeira petição inicial assinada de um processo, se Não encontrar nenhum registro devolve null.
	 * 
	 * @param processoTrf
	 */
	private ProcessoDocumento pegarPeticaoInicialProcesso(ProcessoTrf processoTrf) {
		try {
			// [PJEII-14572] Vanessa Schriver
			List<ProcessoDocumento> iniciais = DocumentoJudicialService.instance().getInicial(processoTrf);
			return iniciais != null && iniciais.size()>0? iniciais.get(0):null;
		} catch (PJeBusinessException e) {
			log.warn("Não foi possével recuperar petição inicial do processo " + processoTrf + ": " + e.getLocalizedMessage());
		}
		return null;
	}

	public String getUsuarioAssinouPetIni(ProcessoTrf processoTrf){
		ProcessoDocumento petIni = pegarPeticaoInicialProcesso(processoTrf);
		if (petIni != null){
			return petIni.getUsuarioInclusao().getNome();
		}
		return "";
	}

	public boolean existeProcessoSegredo(ProcessoTrf processoTrf, Pessoa pessoa){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from ProcessoVisibilidadeSegredo o ");
		sb.append("where o.processo.idProcesso = :idProcesso ");
		sb.append("and o.pessoa = :pessoa");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcesso", processoTrf.getIdProcessoTrf());
		q.setParameter("pessoa", pessoa);
		Long count = (Long) q.getSingleResult();
		return count != 0;
	}

	/**
	 * Define a visibilidade no que diz respeito a segredo de justiça para um determinado processo
	 */
	public void definirVisibilidadeSegredoJustica(ProcessoTrf processoTrf){

		ProcessoJudicialService processoJudicialService = ProcessoJudicialService.instance();

		if (processoTrf.getSegredoJustica()){
			
			try {
				//Adicionado o magistrado.
				processoJudicialService.acrescentaVisualizador(processoTrf, Authenticator.getPessoaLogada(), null);
				
				//Adicionado as partes conforme a NR 451
				int cont = processoJudicialService.habilitarVisibilidadePartesPoloAtivoFiscalLei(processoTrf);
				if(cont > 0){
					isInserirPartesVisualizacao = true;
				}
			}catch (PJeBusinessException e){
				FacesMessages.instance().add(Severity.ERROR, "Houve um erro ao tentar liberar o acesso és partes do processo.");
			}
			
		}else{
			try {
				processoJudicialService.removerTodosVisualizadores(processoTrf);
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, "Houve um erro ao tentar remover o acesso dos visualizadores do processo.");
			}
		}
		refreshGrid("caracteristicaProcessoGrid");
	}

	@Override
	public String update(){
		Processo proc = ProcessoHome.instance().getInstance();
		// Verifica se o número do processo inserido manualmente é válido.
		if (!numeroProcessoNormatizado(proc) || validaSeProcessoEproc(proc)) {
			return null;
		} else {
			if (!protocolacaoValida() ) {
				FacesMessages.instance().clear();
				FacesMessages.instance().addFromResourceBundle(Severity.INFO, TEXTO_PROCESSO_CADASTRADO);
				return null;
			}
		}
		iniciarClasseJudicial();

		verificarProcessoReferenciaExigidoPelaClasseJudicial();

		validaProcessoDependencia();
		
		if (getInstance().getProcesso() != null){
			getProcessoHome().setInstance(getInstance().getProcesso());
			getProcessoHome().update();
		}

		// Defini a visibilidade inicial de um processo so colocar ele como
		// segredo de justiça
		definirVisibilidadeSegredoJustica(getInstance());

		String update = super.update();

		for (ComplementoClasseProcessoTrf ccpt : getInstance().getComplementoClasseProcessoTrfList()){
			ccpt.setProcessoTrf(getInstance());
			if (verificarComplemento(ccpt)){
				getEntityManager().merge(ccpt);
			}
			else{
				getEntityManager().persist(ccpt);
			}
			getEntityManager().flush();
		}

		if (update != null){

			if ((getInstance().getViolacaoFaixaValoresCompetencia() != null || this.isFaixaIncopativelAudiencia())
				&& this.isTelaCaracteristicaProcesso()){
				FacesMessages.instance().clear();
				FacesMessages.instance().add("Registro alterado com sucesso.");
				FacesMessages
						.instance()
						.add("\nATENÇÃO: Salvo exceções legais, o valor da causa informado não é compatível com a classe processual escolhida. Ajuste a classe processual de acordo com o valor da causa ou confirme sua opção.");
				if (!getInstance().getClasseJudicial().getDesignacaoAudienciaErroValorCausa()
					&& tipoAudienciaPadrao() != null){
					FacesMessages.instance().add("A ausência de ajuste do valor da causa resultará na não designarão automática da audiência.");
				}
			}else if(isInserirPartesVisualizacao){
				FacesMessages.instance().clear();
				FacesMessages.instance().add("Registro alterado com sucesso.\n");
				FacesMessages.instance().add("Todas as partes do polo ativo e seus representantes foram cadastrados com permissão para visualizar este processo.");

				isInserirPartesVisualizacao = false;
			}else{
				FacesMessages.instance().clear();
				FacesMessages.instance().add("Registro alterado com sucesso.");
			}

			validaFaixaeVisualizadores();

			// Remover do contexto para atualizar a grid de Assuntos
			Contexts.removeFromAllContexts(GRID_ASSUNTO_CAD_PROCESSO);
			this.compClasseProcessoTrfList = null;
		}
		return update;
	}

	private void validaFaixaeVisualizadores() {
		if ((getInstance().getViolacaoFaixaValoresCompetencia() != null || this.isFaixaIncopativelAudiencia())
			&& this.isTelaCaracteristicaProcesso()){
			FacesMessages.instance().clear();
			FacesMessages.instance().add("Registro alterado com sucesso.");
			FacesMessages
					.instance()
					.add("\nATENÇÃO: Salvo exceções legais, o valor da causa informado não é compatível com a classe processual escolhida. Ajuste a classe processual de acordo com o valor da causa ou confirme sua opção.");
			if (!getInstance().getClasseJudicial().getDesignacaoAudienciaErroValorCausa()
				&& tipoAudienciaPadrao() != null){
				FacesMessages.instance().add("A ausência de ajuste do valor da causa resultará na não designação automática da audiência.");
			}
		}else if(isInserirPartesVisualizacao){
			FacesMessages.instance().clear();
			FacesMessages.instance().add("Registro alterado com sucesso.\n");
			FacesMessages.instance().add("Todas as partes do polo ativo e seus representantes foram cadastrados com permissão para visualizar este processo.");

			isInserirPartesVisualizacao = false;
		}else{
			FacesMessages.instance().clear();
			FacesMessages.instance().add("Registro alterado com sucesso.");
		}
	}

	private void validaProcessoDependencia() {
		if (ProcessoTrfHome.instance().getInstance().getProcessoDependencia() != null){
			ProcessoTrfConexaoHome conexao = new ProcessoTrfConexaoHome();
			conexao.getInstance().setTipoConexao(TipoConexaoEnum.DP);
			conexao.getInstance().setProcessoTrf(ProcessoTrfHome.instance().getInstance().getProcessoDependencia());
			conexao.getInstance().setProcessoTrfConexo(ProcessoTrfHome.instance().getInstance());
			conexao.persist();
		}
	}
	
	public void gravarSegredoSigilo(){ 
		try { 
			ProcessoSegredoManager processoSegredoManager = getProcessoSegredoManager(); 
			
			if (getInstance().getSegredoJustica()){ 
				if (getInstance().getObservacaoSegredo() != null){ 
					getInstance().setApreciadoSegredo(ProcessoTrfApreciadoEnum.A); 
				} 
	        
				ProcessoSegredo processoSegredo = montarProcessoSegredo(getInstance()); 
	        
				if(getInstance().getProcessoStatus().equals(ProcessoStatusEnum.E)){ 
					processoSegredoManager.removerProcessoSegredoPendente(getInstance()); 
				} else { 
					processoSegredoManager.removerProcessoSegredoPendenteUsuarioLogado(getInstance()); 
				} 
			
				processoSegredoManager.gravarProcessoSegredo(processoSegredo); 
			} else { 
		        processoSegredoManager.removerProcessoSegredoPendente(getInstance()); 
		        getInstance().setObservacaoSegredo(null); 
		        getInstance().setApreciadoSegredo(ProcessoTrfApreciadoEnum.N); 
		    } 
			getEntityManager().persist(getInstance());
			getProcessoHome().setInstance(getInstance().getProcesso());
			getProcessoHome().update();
			
		    FacesMessages.instance().clear(); 
		    FacesMessages.instance().add(FacesUtil.getMessage("ProcessoDocumento_updated")); 
		} catch (PJeBusinessException e) { 
			FacesMessages.instance().add(Severity.WARN, FacesUtil.getMessage("processoTrfCaracteristicas.segredoJusticaProcesso.error")); 
			log.error("Ocorreu erro ao gravar segredo/sigilo no processo " + getInstance() + ": " + e.getLocalizedMessage());
		} 
  } 
   
	private ProcessoSegredo montarProcessoSegredo(ProcessoTrf instance) { 
     
		UsuarioLogin usuarioLogin = EntityUtil.find(UsuarioLogin.class, Authenticator.getIdUsuarioLogado());
		
		ProcessoSegredo processoSegredo = new ProcessoSegredo(); 
		processoSegredo.setProcessoTrf(instance); 
		processoSegredo.setMotivo(instance.getObservacaoSegredo()); 
		processoSegredo.setUsuarioLogin(usuarioLogin); 
		processoSegredo.setApreciado(false); 
     
		return processoSegredo; 
	}	
	
	/**
	 * Metodo que apaga o processo de referencia no momento da retificarão caso
	 * a nova classe judicial escolhida tenha sido configurada para não exigir o
	 * processo de referencia.
	 * 
	 * @author Eduardo Medeiros Pereira - TSE
	 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-18801">PJEII-18801</a>
	 */
	private void verificarProcessoReferenciaExigidoPelaClasseJudicial() {
		if (!Boolean.TRUE.equals(this.getInstance().getClasseJudicial().getIncidental())) {
			ProcessoReferenciaEnum processoReferenciaEnum = this.getInstance().getClasseJudicial().getProcessoReferencia();
			if(processoReferenciaEnum == ProcessoReferenciaEnum.NE && this.getInstance().getDesProcReferencia() != null){
				this.getInstance().setDesProcReferencia(null);
			}
		}
	}

	public int getIncompatibilidades() {
		return incompatibilidades;
	}

	public String getMensagemAlerta() {
		return mensagemAlerta;
	}

	public void updateRetificacaoProcesso() throws PJeBusinessException {
		if (ProcessoStatusEnum.D != this.instance.getProcessoStatus()) {
			this.cadastraProcessoConsumidorGovBr = ParametroUtil.instance().getIntegracaoConsumidorGovBr() && 
					AssuntoTrfManager.instance().isAreaDiretoConsumo(this.instance.getIdAreaDireito());

			if (this.cadastraProcessoConsumidorGovBr) {
				this.carregaFornecedores();
			}
		}

		ClasseJudicial classeAntiga = this.getClasseJudicialAnterior();
		ClasseJudicial classeNova = this.instance.getClasseJudicial();
		
		if(getInstance().getProcessoStatus().equals(ProcessoStatusEnum.E) && (getInstance().getOrgaoJulgador() != null || getInstance().getCompetencia() != null)) {
			getInstance().setOrgaoJulgador(null);
			getInstance().setCompetencia(null);
			ComponentUtil.getProcessoTrfManager().update(getInstance());
		}

		validaJusticaEleitoralPrimeiroGrau();

		String updateRet = this.update();
		if (updateRet != null && instance.getProcessoStatus().equals(ProcessoStatusEnum.D)){
			boolean ehRetificacao = this.getRetificacao() != null && this.getRetificacao() == Boolean.TRUE;
			boolean modificouClasseJudicial = classeAntiga != null && !classeAntiga.equals(classeNova);
			
			if (ehRetificacao) {
				if (modificouClasseJudicial) {
					ProcessoParteHome.instance().setClasseJudicialAnterioExigeFiscalDaLei(
							classeAntiga != null ? classeAntiga.getExigeFiscalLei() : Boolean.FALSE);
					
					ProcessoParteHome.instance().verificarExigenciaFiscalDaLei(ProcessoTrfHome.instance().getInstance());
					
					//Registrando o histórico de alteração de classes
					processoHistoricoClasseManager  = ComponentUtil.getComponent("processoHistoricoClasseManager");
					ProcessoHistoricoClasse processoHistoricoClasse = new ProcessoHistoricoClasse();
					processoHistoricoClasse.setInversaoPolos(ProcessoParteHome.instance().getHouveInversaoPolo());
					processoHistoricoClasse.setClasseJudicialAnterior(classeAntiga);
					processoHistoricoClasse.setClasseJudicialAtual(classeNova);
					processoHistoricoClasse.setProcessoTrf(getInstance());

					try {
						processoHistoricoClasseManager.persistAndFlush(processoHistoricoClasse);
					} catch (Exception e) {
						log.warn("Erro ao tentar gerar histérico de alteração de classes. Detalhes: \"" + e.getMessage() +"\"");
					}
					
					setClasseJudicialAnterior(classeNova);
					
					this.instance.setExigeRevisor(SimNaoFacultativoEnum.S.equals(classeNova.getExigeRevisor()));
					
					try {
						if("JE".equals(ParametroUtil.instance().getTipoJustica())){
							retificarVinculacaoDependenciaEleitoral();
						}else{
							DistribuicaoService.instance().atualizarAcumuladoresProcessoRetificado(this.instance);
							getEntityManager().flush();
						}
						
						MovimentoAutomaticoService.preencherMovimento()
								.deCodigo(CodigoMovimentoNacional.CODIGO_MOVIMENTO_RETIFICACAO_CLASSE)
								.comComplementoDeNome("classe_anterior").preencherComTexto(classeAntiga.toString())
								.comComplementoDeNome("classe_nova").preencherComTexto(classeNova.toString())
								.associarAoProcesso(this.instance).lancarMovimento();
					
					} catch (Exception e) {
						log.error("Erro na retificação de processo. Não foi possével atualizar o acumulador do cargo judicial.");
						FacesMessages.instance().add(Severity.ERROR, "Erro na retificação de processo. Não foi possével atualizar o acumulador do cargo judicial.");
					}
				}
				if (this.instance.getProcesso().getNumeroProcessoTemp() != null && this.instance.getClasseJudicial().getPermiteNumeracaoManual()) {
					try {
						NumeroProcessoUtil.numerarProcesso(this.getInstance());
					} catch (PersistenceException cve) {
						log.error(cve.getCause());
						FacesMessages.instance().clear();
						FacesMessages.instance().add(Severity.ERROR,  "O número informado jé está cadastrado.");
					} catch (Exception e) {
						log.error(e.getCause());
						FacesMessages.instance().clear();
						FacesMessages.instance().add(Severity.ERROR,  e.getMessage());
					}
				}
			}
			
			//Limpando o cache do Hibernate, para forçar a realização da consulta novamente
			getEntityManager().clear();
			
			//Atualizando a grid com o resultado da consulta
			refreshGrid("consultaProcessoRetificacaoAutuacaoGrid");
		}			
	}

	private void retificarVinculacaoDependenciaEleitoral() throws PJeBusinessException{
		try {
			DistribuicaoService.instance().retificarVinculacaoDependenciaEleitoral(this.instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	private Boolean verificarComplemento(ComplementoClasseProcessoTrf ccpt){
		String query = "select count(o) from ComplementoClasseProcessoTrf o " + "where o.processoTrf = :processoTrf and "
			+ "o.complementoClasse = :complementoClasse";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("processoTrf", ccpt.getProcessoTrf());
		q.setParameter("complementoClasse", ccpt.getComplementoClasse());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	public Boolean verificaOrgaoJulgador(){
		OrgaoJulgador oj = Authenticator.getOrgaoJulgadorAtual();
		return instance.getOrgaoJulgador().equals(oj);
	}
	
	/**
	* @return Nome do juiz titular do órgão julgador do processo
	*/
	public String getNomeJuizOrgaoJulgador() {
		String nomeDoJuizOrgaoJulgador = "";
		this.orgaoJulgador = getInstance().getOrgaoJulgador();
		if (orgaoJulgador != null) {
			Localizacao localizacao = orgaoJulgador.getLocalizacao();
			OrgaoJulgadorCargo orgaoJulgadorCargo = obterOrgaoJulgadorCargoTitular(orgaoJulgador);
			UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor = obterLocalizacaoUsuario(orgaoJulgadorCargo);
			if (localizacao != null) {
					nomeDoJuizOrgaoJulgador = usuarioLocalizacaoMagistradoServidor.getUsuarioLocalizacao().getUsuario().getNome();					
				}
			}
		return nomeDoJuizOrgaoJulgador;
	}
	
	@SuppressWarnings("unchecked")
	private OrgaoJulgadorCargo obterOrgaoJulgadorCargoTitular(OrgaoJulgador orgaoJulgador) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorCargo o ");
		sb.append("where o.orgaoJulgador = :orgaoJulgador ");
		sb.append("and o.auxiliar = false ");
		sb.append("and o.ativo = true ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgador", orgaoJulgador);
		List<OrgaoJulgadorCargo> lista = new ArrayList<OrgaoJulgadorCargo>();
		lista.addAll(q.getResultList());
		if (lista.size() > 0) {
			return lista.get(0);
		} 
		else {
			/*
			 * Execução do Metodo obterOrgaoJulgadorCargoAuxiliar para o caso do órgão julgador Não possuir
			 * juiz titular entre os cargos judiciais.
			 */					
			return obterOrgaoJulgadorCargoAuxiliar(orgaoJulgador);
		}
	}
	
	/*
	 * Criaééo do Metodo obterOrgaoJulgadorCargoAuxiliar para o caso do órgão julgador Não possuir
	 * juiz titular entre os cargos judiciais.
	 */					
	@SuppressWarnings("unchecked")
	private OrgaoJulgadorCargo obterOrgaoJulgadorCargoAuxiliar(OrgaoJulgador orgaoJulgador) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorCargo o ");
		sb.append("where o.orgaoJulgador = :orgaoJulgador ");
		sb.append("and o.auxiliar = true ");
		sb.append("and o.ativo = true ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgador", orgaoJulgador);
		List<OrgaoJulgadorCargo> lista = new ArrayList<OrgaoJulgadorCargo>();
		lista.addAll(q.getResultList());
		if (lista.size() > 0) {
			return lista.get(0);
		} 
		else {
			return null;
		}
	}	
		
	@SuppressWarnings("unchecked")
	private UsuarioLocalizacaoMagistradoServidor obterLocalizacaoUsuario(OrgaoJulgadorCargo orgaoJulgadorCargo) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from UsuarioLocalizacaoMagistradoServidor o ");
		sb.append("where o.orgaoJulgadorCargo = :orgaoJulgadorCargo ");
		sb.append("and o.dtInicio <= current_timestamp ");
		sb.append("and (o.dtFinal is null or o.dtFinal >= current_timestamp) ");
		sb.append("order by o.dtInicio desc ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgadorCargo", orgaoJulgadorCargo);
		return EntityUtil.getSingleResult(q);
	}

	public void atualizarProcessoIncidente(){
		String update = StringUtils.EMPTY;
		if (isProcessoReferenciaValido()) {
			if (isOrgaoJulgadorColegiadoValido(ParametroUtil.instance().isPrimeiroGrau())) {

				
				if (!numeroProcessoNormatizado(ProcessoHome.instance().getInstance())) {
					//return null;
				}
				
				if (ProcessoTrfHome.instance().getInstance().getProcessoDependencia() != null){
					ProcessoTrfConexaoHome conexao = new ProcessoTrfConexaoHome();
					conexao.getInstance().setTipoConexao(TipoConexaoEnum.DP);
					conexao.getInstance().setProcessoTrf(ProcessoTrfHome.instance().getInstance().getProcessoDependencia());
					conexao.getInstance().setProcessoTrfConexo(ProcessoTrfHome.instance().getInstance());
					conexao.persist();
				}
				if (getInstance().getProcessoReferencia() != null){
					instance.setOrgaoJulgador(getInstance().getProcessoReferencia().getOrgaoJulgador());
					instance.setCargo(getInstance().getProcessoReferencia().getCargo());
					instance.setCompetencia(getInstance().getProcessoReferencia().getCompetencia());
					instance.setValorCausa(getInstance().getValorCausaIncidente());
					
				}
				if (getInstance().getProcesso() != null){
					getProcessoHome().setInstance(getInstance().getProcesso());
					getProcessoHome().update();
				}
				if ((getInstance().getSegredoJustica() && getInstance().getObservacaoSegredo() != null)){
					getInstance().setApreciadoSegredo(ProcessoTrfApreciadoEnum.A);
				}
				else{
					if (!getInstance().getSegredoJustica()){
						getInstance().setObservacaoSegredo(null);
						getInstance().setApreciadoSegredo(ProcessoTrfApreciadoEnum.N);
					}
				}
				if (ParametroUtil.instance().isJusticaEleitoralAndPrimeiroGrau()) {
					this.getInstance().setComplementoJE(ComplementoProcessoJEManager.instance().definirComplementoProcessoJe(
							this.getInstance(), this.getEleicao(), this.getMunicipioJurisdicao()));
				}
				update = super.update();
				if (update != null){
					FacesMessages.instance().clear();
					FacesMessages.instance().addFromResourceBundle(Severity.INFO, "ProcessoTrf_updated");
				}

				updateIncidente();
			}
		}
	}
	
	public String updateIncidente(){
		

		if (!numeroProcessoNormatizado(ProcessoHome.instance().getInstance())) {
			return null;
		} else {
			if (!protocolacaoValida()) {
				FacesMessages.instance().clear();
				FacesMessages.instance().addFromResourceBundle(Severity.INFO, TEXTO_PROCESSO_CADASTRADO);
				return null;
			}
		}

		validaProcessoDependencia();
		validaProcessoReferencia();
		
		if (getInstance().getProcesso() != null) {
			getProcessoHome().setInstance(getInstance().getProcesso());
			getProcessoHome().update();
		}
		validaSegredoJustica();
		
		validaJusticaEleitoralPrimeiroGrau();
		
		String update = super.update();
		if (update != null) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "ProcessoTrf_updated");
		}

		return update;
	}

	private void validaJusticaEleitoralPrimeiroGrau() {
		if (ParametroUtil.instance().isJusticaEleitoralAndPrimeiroGrau()) {
			this.getInstance().setComplementoJE(ComplementoProcessoJEManager.instance().definirComplementoProcessoJe(
					this.getInstance(), this.getEleicao(), this.getMunicipioJurisdicao()));
		}
	}

	private void validaSegredoJustica() {
		if ((getInstance().getSegredoJustica() && getInstance().getObservacaoSegredo() != null)){
			getInstance().setApreciadoSegredo(ProcessoTrfApreciadoEnum.A);
		}
		else{
			if (getInstance().getSegredoJustica().equals(false) ){
				getInstance().setObservacaoSegredo(null);
				getInstance().setApreciadoSegredo(ProcessoTrfApreciadoEnum.N);
			}
		}
	}

	private void validaProcessoReferencia() {
		if (getInstance().getProcessoReferencia() != null){
			instance.setOrgaoJulgador(getInstance().getProcessoReferencia().getOrgaoJulgador());
			instance.setCargo(getInstance().getProcessoReferencia().getCargo());
			instance.setCompetencia(getInstance().getProcessoReferencia().getCompetencia());
			instance.setValorCausa(getInstance().getValorCausaIncidente());
			instance.setCompetencia(this.competenciaConflito);
			setCompEscolhida(this.competenciaConflito);
		}
	}

	public void addProcessoAssunto(AssuntoTrf obj, String gridId){
		if (getInstance() != null){
			ProcessoTrf proc = getInstance();
			ProcessoAssunto processoAssunto = new ProcessoAssunto();
			
			// Evita que assunto complementar seja marcado como principal.
			if (proc.getProcessoAssuntoPrincipal() == null){
				if (obj.getComplementar()) {
					FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "assuntoTrf.adicionar.naocomplementar");
					return;
				} else {
					this.selectedRowAssuntoPrincipal = obj;
					processoAssunto.setAssuntoPrincipal(true);
				}
			} else{
				processoAssunto.setAssuntoPrincipal(false);
			}
			
			processoAssunto.setAssuntoTrf(obj);
			processoAssunto.setProcessoTrf(proc);

			proc.getProcessoAssuntoList().add(processoAssunto);
			getEntityManager().persist(proc);
			getEntityManager().flush();
			getEntityManager().refresh(proc);

			refreshGrid(gridId);
			refreshGrid(GRID_ASSUNTO_CAD_PROCESSO);
			refreshGrid("processoAssuntoGrid");
			refreshGrid("processoAssuntoViewGrid");
		}
	}
	
	public AssuntoTrf getSelectedRowAssuntoPrincipal(){
		if (selectedRowAssuntoPrincipal == null) {
			selectedRowAssuntoPrincipal = new AssuntoTrf();
		}
		return selectedRowAssuntoPrincipal;
	}

	public List<ComplementoClasseProcessoTrf> getComplementoClasseProcessoTrfList(){
		if (compClasseProcessoTrfList != null){
			return compClasseProcessoTrfList;
		}
		if (getInstance() == null){
			return null;
		}
		List<ComplementoClasseProcessoTrf> processoTrfList = null;
		List<ComplementoClasse> listComplementoClasse = getListComplementoClasse();

		if (listComplementoClasse.size() > 0){
			compClasseProcessoTrfList = new ArrayList<ComplementoClasseProcessoTrf>();
			List<ComplementoClasse> compClasseList = listComplementoClasse;
			processoTrfList = getInstance().getComplementoClasseProcessoTrfList();
			for (ComplementoClasse cc : compClasseList){
				boolean exists = false;
				for (ComplementoClasseProcessoTrf ccpt : processoTrfList){
					if (cc.equals(ccpt.getComplementoClasse())){
						exists = true;
						compClasseProcessoTrfList.add(ccpt);
					}
				}
				if (!exists){
					ComplementoClasseProcessoTrf ccp = new ComplementoClasseProcessoTrf();
					ccp.setProcessoTrf(getInstance());
					ccp.setComplementoClasse(cc);
					compClasseProcessoTrfList.add(ccp);
				}
			}
		}
		if (compClasseProcessoTrfList == null) {
			compClasseProcessoTrfList = Collections.emptyList();
		}
		return compClasseProcessoTrfList;
	}

	public boolean isRequired(Object o){
		boolean ret = false;
		if (o instanceof ComplementoClasseProcessoTrf){
			ComplementoClasseProcessoTrf ccpt = (ComplementoClasseProcessoTrf) o;
			ret = ccpt.getComplementoClasse().getObrigatorio();
		}
		return ret;
	}

	@Override
	public String remove(ProcessoTrf obj){
		for (ComplementoClasseProcessoTrf ccpt : obj.getComplementoClasseProcessoTrfList()){
			getEntityManager().remove(ccpt);
		}
		return super.remove(obj);
	}

	public Boolean visualizarBotao(){
		Boolean ret = Boolean.FALSE;
		if (getInstance().getProcesso() != null){
			if ((getInstance().getProcessoStatus().equals(ProcessoStatusEnum.V) && !Strings.isEmpty(getInstance()
					.getNumeroProcesso())) || getInstance().getProcessoStatus().equals(ProcessoStatusEnum.D)){
				ret = Boolean.TRUE;
			}
			
		}
		
		return ret;
	}

	@SuppressWarnings("unchecked")
	public boolean peticaoInicialAssinada(ProcessoDocumento pd){
		StringBuilder sb = new StringBuilder();
		sb.append("select o.pessoa from ProcessoDocumentoBinPessoaAssinatura o ");
		sb.append("where o.processoDocumentoBin = :bin");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("bin", pd.getProcessoDocumentoBin());

		List<Pessoa> lista = q.getResultList();
		if (lista != null && !lista.isEmpty()){
			for (Pessoa p : lista){
				if ((p.getTipoPessoa().equals(ParametroUtil.instance().getTipoAdvogado())) || Pessoa.instanceOf(p, PessoaProcurador.class) || Pessoa.instanceOf(p, PessoaAdvogado.class) || Pessoa.instanceOf(p, PessoaMagistrado.class) || Pessoa.instanceOf(p, PessoaServidor.class)){
					return true;
				}
			}
		}
		return false;
	}

	public void validarIncidente() {
		getInstance().setIsIncidente(true);
		
		if(getInstance().getProcessoReferencia() == null){
			setarProcessoReferenciaSemFiltroSigilo();
			atualizarDadosProcessoReferencia();
		}
		
		if (isProcessoReferenciaValido() && isOrgaoJulgadorColegiadoValido(ParametroUtil.instance().isPrimeiroGrau())) {
			validar();
		} else {
			FacesMessages.instance().clear();
			mensagemErroProcessoIncidente = Messages.instance().get("processoTrf.cadastrarIncidental.erro.protocolar");
			FacesMessages.instance().addFromResourceBundle(Severity.INFO,mensagemErroProcessoIncidente);
		}
	}

	/**
	 * Metodo responsável por setar o processo referência, sem os filtros de "sigilo".
     *
	 */
	private void setarProcessoReferenciaSemFiltroSigilo() {
		try {
			if (getInstance().getDesProcReferencia() != null) {
				List<ProcessoTrf> result = ProcessoJudicialManager.instance().findByNU(getInstance().getDesProcReferencia());
				if(result != null && result.size() > 0 ){
					getInstance().setProcessoReferencia(result.get(0));
				}else{
					getInstance().setProcessoReferencia(null);
				}
			}
		} catch (PJeBusinessException e) {
			log.warn("Não foi possével recuperar processo pela numeração única " + this.instance.getDesProcReferencia() + ": " + e.getLocalizedMessage());
		} 
	}
	
	/**
	 * Metodo responsável por atualizar os dados do processo de acordo com o processo referência.
	 * 
     *
	 */
	private void atualizarDadosProcessoReferencia(){
		if (getInstance().getProcessoReferencia() != null) {
			if(getInstance().getProcessoReferencia().getCompetencia() != null){
				getInstance().setCompetencia(getInstance().getProcessoReferencia().getCompetencia());
			}
			
			if(getInstance().getProcessoReferencia().getCargo() != null){
				getInstance().setCargo(getInstance().getProcessoReferencia().getCargo());
			}
			
			if(getInstance().getProcessoReferencia().getJurisdicao() != null){
				getInstance().setJurisdicao(getInstance().getProcessoReferencia().getJurisdicao());
			}
			
			if (getInstance().getProcessoReferencia().getOrgaoJulgador() != null) {
				getInstance().setOrgaoJulgador(getInstance().getProcessoReferencia().getOrgaoJulgador());
			}
		
			if (!ParametroUtil.instance().isPrimeiroGrau() && getInstance().getProcessoReferencia().getOrgaoJulgadorColegiado() != null){
				getInstance().setOrgaoJulgadorColegiado(getInstance().getProcessoReferencia().getOrgaoJulgadorColegiado());
			}

			if (ParametroUtil.instance().isJusticaEleitoralAndPrimeiroGrau() && getInstance().getProcessoReferencia().getComplementoJE() != null) {
				ComplementoProcessoJE complementoProcessoJE = getInstance().getProcessoReferencia().getComplementoJE();
				getInstance().setComplementoJE(ComplementoProcessoJEManager.instance().definirComplementoProcessoJe(
					this.getInstance(), complementoProcessoJE.getEleicao(), complementoProcessoJE.getMunicipioEleicao()));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<String> validarProcesso(ProcessoTrf processoTrf){
		List<String> erros = new ArrayList<String>();

		// Verifica se existe uma petição inicial, caso o processo seja inicial
		List<ProcessoDocumento> processoDocumentoList = processoTrf.getProcesso().getProcessoDocumentoList();

		if (processoTrf.getInicial().equals(ClasseJudicialInicialEnum.I)){
			Boolean peticaoInicial = Boolean.FALSE;
			// [PJEII-1243] Padronizado para recuperar o id do TipoProcessoDocumento configurado na tabela de parâmetros como petição inicial
			int idTipoProcessoDocumentoPeticaoInicial = this.getInstance().getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento();
			for (ProcessoDocumento procDoc : processoDocumentoList){
				if (procDoc.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == idTipoProcessoDocumentoPeticaoInicial
					&& peticaoInicialAssinada(procDoc)){
					peticaoInicial = Boolean.TRUE;
				}
			}

			if (!peticaoInicial){
				erros.add("O Processo é Inicial. Logo, é necessério uma petição Inicial anexada a ele e a petição deve estar assinada pelo advogado.");
			}
		}

		// Verifica se todos os documentos anexados estão assinados
		// digitalmente.
		Boolean faltaAssinatura = Boolean.FALSE;

		StringBuffer sb = new StringBuffer();
		sb.append("select pdb.ds_cert_chain, pdb.ds_signature from tb_processo_documento pd inner join ");
		sb.append("tb_processo_documento_bin pdb ");
		sb.append("on(pd.id_processo_documento_bin = pdb.id_processo_documento_bin) ");
		sb.append("where pd.id_processo = :idProcesso");

		Query q = getEntityManager().createNativeQuery(sb.toString());
		q.setParameter("idProcesso", processoTrf.getProcesso().getIdProcesso());
		List<Object[]> objectList = q.getResultList();

		for (Object[] obj : objectList){
			if (Strings.isEmpty((String) obj[0])){
				faltaAssinatura = Boolean.TRUE;
				break;
			}
		}
		if (faltaAssinatura){
			erros.add("Todos os documentos devem estar assinados digitalmente.");
		}

		// Verifica a quantidade de assuntos vinculados ao processo. Deve
		// existir pelo menos 1.
		if (processoTrf.getAssuntoTrfList().size() == 0){
			erros.add("Deve haver ao menos um assunto vinculado ao processo.");
		}

		// Verifica se existe pelo menos uma parte no polo ativo.
		Integer tamAtivo = processoTrf.getListaParteAtivo().size();
		if (tamAtivo == 0){
			erros.add("Deve haver ao menos uma pessoa compondo o polo ativo do processo.");
		}

		// Verifica se existe pelo menos uma parte no polo passivo.
		Integer tamPassivo = processoTrf.getListaPartePassivo().size();
		if (tamPassivo == 0){
			erros.add("Deve haver ao menos uma pessoa compondo o polo passivo do processo.");
		}

		Boolean partePrincipalAtivo = false;
		Boolean partePrincipalPassivo = false;

		for (ProcessoParte ppa : processoTrf.getListaParteAtivo()){
			if (ppa.getPartePrincipal()){
				partePrincipalAtivo = true;
			}
		}

		for (ProcessoParte ppp : processoTrf.getListaPartePassivo()){
			if (ppp.getPartePrincipal()){
				partePrincipalPassivo = true;
			}
		}

		if (!partePrincipalAtivo){
			erros.add("Deve haver ao menos uma pessoa compondo o polo ativo como parte principal.");
		}

		if (!partePrincipalPassivo){
			erros.add("Deve haver ao menos uma pessoa compondo o polo passivo como parte principal. ");
		}
				
		Integer tamVetor = processoTrf.getComplementoClasseProcessoTrfList().size();
		for (int i = 0; i < tamVetor; i++){
			if (processoTrf.getComplementoClasseProcessoTrfList().get(i).getComplementoClasse().getObrigatorio()
				&& processoTrf.getComplementoClasseProcessoTrfList().get(i).getValorComplementoClasseProcessoTrf() == null){
				erros.add("Existe um complemento obrigatório que Não foi preenchido.");
			}
		}
		return erros;
	}

	public void carregaAbaProcesso(){
		acoesAntesDeProtocolarProcesso();
		refreshGridDocumento();
	}

	public void acoesAntesDeProtocolarProcesso(){
		carregaCompetencias();
		if(!cadastroProcessoClet){
			confirmaMotivoSegredoJustica();
		}
		setAcaoAmbiental(null);
	}
	
	private Competencia getCompetenciaPreSelecionada() {
		Competencia competenciaPreSelecionada = null;
		if(getInstance().getCompetencia() != null){
			competenciaPreSelecionada = HibernateUtil.deproxy(getInstance().getCompetencia(),Competencia.class);
		}
		return competenciaPreSelecionada;
	}

	public void carregaCompetencias(){
		this.competenciaConflito = null;
		this.competenciasPossiveis = getCompetenciasProcesso();
		Competencia competenciaPreSelecionada = this.getCompetenciaPreSelecionada();
		Integer idCompetenciaPreSelecionada = null;
		if(competenciaPreSelecionada != null) {
			idCompetenciaPreSelecionada = competenciaPreSelecionada.getIdCompetencia();
		}
		if (this.competenciasPossiveis.size() == 1){
			this.competenciaConflito = competenciasPossiveis.get(0);
		}else if(this.competenciasPossiveis.size() > 1){
			// verifica se já havia sido selecionada uma competência para o processo atual e essa competência está na lista de possíveis - usado nos Não protocolados
			if(idCompetenciaPreSelecionada != null) {
				for (Competencia competenciaPossivel : this.competenciasPossiveis) {
					if(idCompetenciaPreSelecionada.equals(competenciaPossivel.getIdCompetencia())) {
						this.competenciaConflito = competenciaPreSelecionada;
						break;
					}
				}
			}
		}
		if(this.competenciaConflito == null || (idCompetenciaPreSelecionada != null && !idCompetenciaPreSelecionada.equals(this.competenciaConflito.getIdCompetencia()))) {
			if(!this.instance.getIsIncidente() && !redistribuicaoSelecionadaIncompetencia()) {
				this.instance.setOrgaoJulgador(null);
			}
		}
		if (!redistribuicaoSelecionadaIncompetencia() || this.competenciaConflito != null) {
			this.instance.setCompetencia(this.competenciaConflito);
		}
	}
	
	public boolean redistribuicaoSelecionadaIncompetencia() {
		ProcessoTrfRedistribuicaoHome processoTrfRedistribuicaoHome = ProcessoTrfRedistribuicaoHome.instance();
        return processoTrfRedistribuicaoHome != null && processoTrfRedistribuicaoHome.getInTipoRedistribuicao() != null && processoTrfRedistribuicaoHome.getInTipoRedistribuicao() == TipoRedistribuicaoEnum.R;
    }
	
	public void carregaCompetenciasRedistribuicao(){
		ProcessoTrfRedistribuicaoHome processoTrfRedistribuicaoHome = ProcessoTrfRedistribuicaoHome.instance();
		if(processoTrfRedistribuicaoHome.getInstance().getProcessoTrf() == null) {
			processoTrfRedistribuicaoHome.getInstance().setProcessoTrf(getInstance());
		}
		carregaCompetencias();
		
		setTab("tabRedistribuicoes");
		TipoRedistribuicaoEnum tipoRedist = processoTrfRedistribuicaoHome.getInTipoRedistribuicao();
		
		if(tipoRedist != null){
			switch (tipoRedist){
				case A:
					setTab("tabDistribuicaoAfastamentoRelator");
					break;
					
				case C:
					setTab("tabDistribuicaoAlteracaoCompetenciaOrgao");
					if((ParametroUtil.instance().isPrimeiroGrau())
							&& (processoTrfRedistribuicaoHome.getJurisdicaoRedistribuicao() == null)) {
						processoTrfRedistribuicaoHome.setJurisdicaoRedistribuicao(this.getInstance().getJurisdicao());
					}
					this.carregaTipoDistribuicao();
					break;
				
				case D:
					setTab("tabDistribuicaoDesaforamento");
					if((processoTrfRedistribuicaoHome.getJurisdicaoSorteio() == null)) {
						processoTrfRedistribuicaoHome.setJurisdicaoSorteio(this.getInstance().getJurisdicao());
					}
					break;
				
				case E:
					setTab("tabDistribuicaoReuniaoExecucoes");
					break;
				
				case I:
					if(processoTrfRedistribuicaoHome.getInstance().getCausaImpedimento() != null){
 						setTab("tabTipoRedistribuicao");
 					}else{
 						processoTrfRedistribuicaoHome.getInstance().setInTipoDistribuicao(null);
 						setTab("tabDistribuicaoImpedimento");
 					}
					this.carregaTipoDistribuicao();
					break;

				case Y:
					setTab("tabDistribuicaoImpedimentoOrgaoJulgadorColegiado");
					break;
				
				case J:
					setTab("tabDistribuicaoDetJudicial");
					this.carregaTipoDistribuicao();
					break;
				
				case K:
					setTab("tabDistribuicaoRazaoPosseRelator");
					break;
				
				case M:
					setTab("tabDistribuicaoErroMaterial");
					this.carregaTipoDistribuicao();
					break;
				
				case N:
					setTab("tabDistribuicaoImpedimentoRelator");
					break;
				
				case O:
					setTab("tabDistribuicaoSuspeicaoRelator");
					this.carregaTipoDistribuicao();
					break;
				
				case P:
					setTab("tabDistribuicaoPorPrevencao");
					break;
				
				case R:
					setTab("tabDistribuicaoIncompetencia");
					break;
				
				case S:
					if(processoTrfRedistribuicaoHome.getInstance().getCausaSuspeicao() != null){
 						setTab("tabTipoRedistribuicao");
 						this.carregaTipoDistribuicao();
 					}else{
 						processoTrfRedistribuicaoHome.getInstance().setInTipoDistribuicao(null);
 						setTab("tabDistribuicaoSuspeicao");
 					}
					break;
				
				case T:
					setTab("tabDistribuicaoAfastamentoTemporarioTitular");
					break;
				
				case U:
					setTab("tabDistribuicaoCriacaoUnidadeJudiciaria");
					this.carregaTipoDistribuicao();
					break;
				
				case X:
					setTab("tabDistribuicaoExtincaoUnidadeJudiciaria");
					this.carregaTipoDistribuicao();
					break;
				
				case W:
					setTab("tabDistribuicaoRecusaPrevencaoDependencia");
					this.carregaTipoDistribuicao();
					break;
					
				case Z:
					if(ParametroUtil.instance().isPrimeiroGrau() && 
							processoTrfRedistribuicaoHome.getJurisdicaoRedistribuicao() == null) {
						
						processoTrfRedistribuicaoHome.setJurisdicaoRedistribuicao(this.getInstance().getJurisdicao());
					}
					setTab("tabDistribuicaoSucessao");
					this.carregaTipoDistribuicao();
					break;
					
				default:
					
					break;
			}
		}
	}
	
	public void carregaTipoDistribuicao(){
		ProcessoTrfRedistribuicaoHome processoTrfRedistribuicaoHome = ProcessoTrfRedistribuicaoHome.instance();
		TipoDistribuicaoEnum tipoDistribuicao = null;
		if(processoTrfRedistribuicaoHome.getInstance() != null) {
			tipoDistribuicao = processoTrfRedistribuicaoHome.getInstance().getInTipoDistribuicao();
		}
		if(tipoDistribuicao != null){
			switch (tipoDistribuicao){
				case CE: 
					setTab("tabDistribuicaoCompetenciaExclusiva");
					break;
				case PP:
					setTab("tabDistribuicaoPrevencaoDependencia");
					break;
				case I:
				case PD:
					setTab("tabDistribuicaoPrevencaoDependencia");
					break;
				case EN:
					processoTrfRedistribuicaoHome.iniciarEncaminhamento(getInstance());
					setTab("tabDistribuicaoEncaminhamnento");
					break;
				case A:
				case Z:
				case S:
				default:
					if(processoTrfRedistribuicaoHome.permiteSelecionarJurisdicaoRedistribuicao()) {
						processoTrfRedistribuicaoHome.iniciarTipoComJurisdicao(getInstance());
					}
					setTab("tabDistribuicaoSorteio");
					break;
			}
		}
	}

	public void refreshGridDocumento(){
		refreshGrid(Grid.PROCESSO_TRF_DOCUMENTO_GRID);
		refreshGrid("processoTrfDocumentoAdvogadoGrid");
		ProcessoDocumentoBinPessoaAssinaturaHome pdbp = getComponent("processoDocumentoBinPessoaAssinaturaHome", false);
		if (pdbp != null) {
			pdbp.limpar();
		}
	}
	
	public void refreshGridDocumentoRetificacao(){
		refreshGrid("processoDocumentoGridTab");		
	}

	public void refreshGrids(){
		refreshGrid("processoAssuntoViewGrid");
		refreshGrid("processoPoloAtivoGrid");
		refreshGrid("processoPoloPassivoGrid");
		refreshGrid("processoAbaParteTerceiroGrid");
		refreshGrid("processoFiscalLeiGrid");
		refreshGrid(Grid.PROCESSO_TRF_DOCUMENTO_GRID);
	}

	@SuppressWarnings("unchecked")
	private List<ComplementoClasse> getListComplementoClasse(){
		String query = "select o from ComplementoClasse o " + "inner join o.classeAplicacao cat "
			+ "where cat.classeJudicial = :classeJudicial";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("classeJudicial", getInstance().getClasseJudicial());
		return q.getResultList();
	}

	public void setIsTrue(boolean isTrue){
		this.isTrue = isTrue;
	}

	public Boolean getIsTrue(){
		return isTrue;
	}

	@SuppressWarnings("unchecked")
	public String getPeticao(){
		String query = "select o.peticao from Peticao o " + "inner join o.peticaoClasseAplicacaoList pcat "
			+ "inner join pcat.classeAplicacao cat "
			+ "where cat.classeJudicial.classeJudicial = :classeJudicial and exists (select p from ProcessoTrf p "
			+ "where p.classeJudicial = cat.classeJudicial and p.idProcessoTrf = :idProcessoTrf)";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("classeJudicial", getInstance().getClasseJudicial().getClasseJudicial());
		q.setParameter("idProcessoTrf", getInstance().getIdProcessoTrf());
		List<String> list = q.getResultList();
		if (list.size() >= 1){
			if (list.size() > 1){
				FacesMessages.instance().add(Severity.WARN, "Existe mais de uma petição.");
			}
			return list.get(0);
		}
		else{
			FacesMessages.instance().add(Severity.ERROR, "Não foi encontrada a petição.");
			return null;
		}
	}

	public Integer getFirst(){
		List<ProcessoDocumento> processoDocList = getInstance().getProcesso().getProcessoDocumentoList();
		return processoDocList.size();
	}

	public List<ProcessoDocumento> getListProcessoDocumento(){
		return getInstance().getProcesso().getProcessoDocumentoList();
	}

	public static ProcessoTrfHome instance(){
		return ComponentUtil.getComponent("processoTrfHome");
	}

	/**
	 * Limpar a lista de Assuntos quando alterar a seção/subseção
	 */
	public void limparAssuntos(){
		getInstance().getProcessoAssuntoList().clear();
		getInstance().getAssuntoTrfList().clear();
	}

	public void filtrarClasseJudicial(){
		limparAssuntos();
		ClasseJudicialProcessoTreeHandler cjth = getComponent("classeJudicialProcessoTree");
		cjth.clearTree();
	}


	public List<ProcessoDocumento> getEntityProcDocList(Integer idProcessoTRF){
		ProcessoTrf pt = getEntityManager().find(ProcessoTrf.class, idProcessoTRF);
		return pt.getProcesso().getProcessoDocumentoList();
	}

	@SuppressWarnings("unchecked")
	public boolean possuiPeticao(){
		List<Integer> lista = (List<Integer>) Contexts.getSessionContext().get(ProcessoDocumentoHome.PETICAO_INSERIDA);
		if (lista == null || getInstance() == null || getInstance().getProcesso() == null){
			return false;
		}
		return lista.contains(getInstance().getProcesso().getIdProcesso());
	}

	public int tempoOrgaoJulgador(){
		Date tempoOJ = null;
		if (getInstance().getOrgaoJulgador() != null
			&& getInstance().getOrgaoJulgador().getNumeroTempoAudiencia() != null){
			tempoOJ = getInstance().getOrgaoJulgador().getNumeroTempoAudiencia();
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(tempoOJ);
			return (calendar.get(Calendar.HOUR) * HORAEMMINUTOS + calendar.get(Calendar.MINUTE));
		}

		return 0;
	}

	public String getNumeroProcesso(){
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso){
		this.numeroProcesso = numeroProcesso;
	}

	public ProcessoTrf getProcessoTrfIncidente(){
		return processoTrfIncidente;
	}

	public void setProcessoTrfIncidente(ProcessoTrf processoTrfIncidente){
		this.processoTrfIncidente = processoTrfIncidente;
	}

	/**
	 * Metodo responsável por criar um processo incidente na tela de Cadastro de
	 * Processo Incidente
	 */
	public void inserirProcessoIncidente(){
		if (isProcessoReferenciaValido()) {
			if (isOrgaoJulgadorColegiadoValido(ParametroUtil.instance().isPrimeiroGrau())) {
				definirInstancia();
				ProcessoHome procHome = getProcessoHome();
				if(numeroProcessoNormatizado(procHome.getInstance())){
					if (protocolacaoValida() ) {
							Processo processo = procHome.criarProcesso();
							persistIncidente(processo);
					} else {
						FacesMessages.instance().clear();
						FacesMessages.instance().addFromResourceBundle(Severity.INFO, TEXTO_PROCESSO_CADASTRADO);
					}
				}	
			}
		}
	}
	
	public void atualizaDadosProcessoIncidente() {
		PesquisaProcessoParadigmaAction pesquisaProcessoParadigma = ComponentUtil.getComponent(PesquisaProcessoParadigmaAction.class);
		instance.setIsIncidente(Boolean.TRUE);
		if(pesquisaProcessoParadigma.getProcessoValidado()) {
			this.setUsuarioPodeVisualizarDadosProcessoReferencia(pesquisaProcessoParadigma.isUsuarioPodeVisualizarDadosProcesso());
			instance.setProcessoOriginario(pesquisaProcessoParadigma.getOrigemSistema().equals(Constantes.ORIGEM_SISTEMA_PJE) ? pesquisaProcessoParadigma.getProcessoTrfParadigma() : null);
			instance.setDesProcReferencia(pesquisaProcessoParadigma.getProcessoParadigma());
			instance.setJurisdicao(pesquisaProcessoParadigma.getJurisdicaoParadigma());
			instance.setIdAreaDireito(pesquisaProcessoParadigma.getIdAreaDireitoParadigma());
			if(instance.getCompetencia() == null) {
				instance.setCompetencia(pesquisaProcessoParadigma.getCompetenciaParadigma());
			}
			instance.setOrgaoJulgador(pesquisaProcessoParadigma.getOrgaoJulgadorParadigma());
			instance.setOrgaoJulgadorColegiado(pesquisaProcessoParadigma.getOrgaoJulgadorColegiadoParadigma());
            instance.setOrgaoJulgadorCargo(pesquisaProcessoParadigma.getOrgaoJulgadorCargoParadigma());
			if(ParametroJtUtil.instance().justicaEleitoral()) {
				this.setMunicipioJurisdicao(pesquisaProcessoParadigma.getMunicipioJurisdicaoParadigma());
				this.setEleicao(pesquisaProcessoParadigma.getEleicaoParadigma());
				this.setEstadoJurisdicao(pesquisaProcessoParadigma.getEstadoJurisdicaoParadigma());
				if (ParametroUtil.instance().isPrimeiroGrau()) {
					instance.setComplementoJE(ComplementoProcessoJEManager.instance().definirComplementoProcessoJe(
							this.getInstance(), this.getEleicao(), this.getMunicipioJurisdicao()));
				}
			}
			limparAbaAssuntos();
			this.carregaCompetencias();
		} else {
			this.setUsuarioPodeVisualizarDadosProcessoReferencia(Boolean.FALSE);
			instance.setProcessoDependencia(null);
			instance.setDesProcReferencia(null);
			instance.setJurisdicao(null);
			instance.setCompetencia(null);
			instance.setOrgaoJulgador(null);
			instance.setOrgaoJulgadorColegiado(null);
			instance.setClasseJudicial(null);
			instance.setOrgaoJulgadorCargo(null);
			instance.setComplementoJE(null);

			this.competenciasPossiveis = null;
		}
	}
	
	public void limparAbaAssuntos() {
		try {
			if (getInstance().getIdProcessoTrf() != 0) {
				for (int i = 0; i < getInstance().getProcessoAssuntoList().size();) {
					ProcessoAssunto processoAssunto = getInstance().getProcessoAssuntoList().get(i);
					getInstance().getProcessoAssuntoList().remove(i);
					getEntityManager().remove(processoAssunto);
					getEntityManager().persist(getInstance());
					getEntityManager().flush();
					getEntityManager().refresh(getInstance());
					
					retificarVinculacaoDependenciaEleitoral();
				}
				limparAssuntos();
			}
			Contexts.removeFromAllContexts("processoAssuntoCadProcessoIncidenteGrid");
			refreshGrid(PROCESSO_ASSUNTO_VIEW_GRID);
			refreshGrid(PROCESSO_ASSUNTO_CAD_PROCESSO_GRID);
			refreshGrid(PROCESSO_ASSUNTO_GRID);
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Erro ao limpar Aba Assuntos");
		}
	}

	/**
	 * Metodo responsável por definir a instância de tramitação
	 * 
	 * @see {@link ProcessoTrf#setInstancia(Character)}
	 */
	private void definirInstancia() {
		getInstance().setInstancia(ParametroUtil.instance().getInstancia().toString().charAt(0));
	}
	
	/**
	 * Metodo responsável por validar o número do processo referência caso a
	 * {@link ClasseJudicial#getHabilitarMascaraProcessoReferencia()} esteja
	 * como <code>true</code>
	 * 
	 * @return <code>True</code>, se a classe judicial necessitar habilitar é
	 *         máscara ao processo referência e o mesmo esteja de acordo com a
	 *         numeração processual
	 */
	private boolean isProcessoReferenciaValido() {
		boolean retorno = true;
		if (instance.getClasseJudicial() == null ) {
			retorno = false;
		} else if (instance.getClasseJudicial().getHabilitarMascaraProcessoReferencia()) {
			if (!NumeroProcessoUtil.numeroProcessoValido(instance.getDesProcReferencia())) {
				mensagemErroProcessoIncidente = Messages.instance().get("processoTrf.cadastrarIncidental.erro.processoReferencia");
				FacesMessages.instance().clear();
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, mensagemErroProcessoIncidente);
				retorno = false;
			}
		}
		return retorno;
	}
	
	/**
	 * Metodo responsável por validar a {@link Jurisdicao},
	 * {@link OrgaoJulgador} e {@link OrgaoJulgadorColegiado} do cadastro de
	 * processo incidente
	 * 
	 * @param isPrimeiroGrau
	 *            booleano para identificar o grau do tribunal. Para tribunais
	 *            de primeiro grau a validação de {@link OrgaoJulgadorColegiado}
	 *            Não é necesséria
	 * @return <code>True</code>, se o cadastro estiver com {@link Jurisdicao},
	 *         {@link OrgaoJulgador} e {@link OrgaoJulgadorColegiado} (se
	 *         diferente de primeiro grau) preenchidos
	 */
	private boolean isOrgaoJulgadorColegiadoValido(boolean isPrimeiroGrau) {
		boolean retorno = false;
		boolean isJurisdicaoOrgaoJulgadorPreenchido = (instance.getJurisdicao() != null && instance.getOrgaoJulgador() != null);
		mensagemErroProcessoIncidente = StringUtils.EMPTY;
		if (isPrimeiroGrau) {
			retorno = isJurisdicaoOrgaoJulgadorPreenchido;
		} else {
			mensagemErroProcessoIncidente = " e \u00F3rg\u00E3o julgador colegiado";
			retorno = (isJurisdicaoOrgaoJulgadorPreenchido && instance.getOrgaoJulgadorColegiado() != null);
		}
		if (!retorno) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "processoTrf.cadastrarIncidental.erro.orgaoJulgador", mensagemErroProcessoIncidente);			
		}
		return retorno;
	}

	public List<ProcessoParte> getPartesList(){
		return partesList;
	}

	public void setPartesList(List<ProcessoParte> partesList){
		this.partesList = partesList;
	}

	public void inserirParte(ProcessoParte obj){
		if (obj.getCheckado()){
			partesList.add(obj);
		}
		else{
			partesList.remove(obj);
		}
	}

	/**
	 * Nao foi encontrada nenhuma chamada a este metodo no sistema Verificar pra que ele realmente serve... PESSOANEW
	 * 
	 * @param polo
	 */
	public void inserirProcessoParte(String polo){
		ProcessoParteParticipacaoEnum participacao = ProcessoParteParticipacaoEnum.P;
		if (polo.equals("Ativo")){
			participacao = ProcessoParteParticipacaoEnum.A;
		}
		for (ProcessoParte processoParte : partesList){
			ProcessoParte obj = new ProcessoParte();
			obj.setProcessoTrf(getInstance());
			obj.setInParticipacao(participacao);
			obj.setPessoa(processoParte.getPessoa());
			obj.setTipoParte(processoParte.getTipoParte());
			ProcessoParteHome.instance().persist(obj);
			processoParte.setCheckado(Boolean.FALSE);
		}

		refreshGrid("processoPoloAtivoGrid");
		refreshGrid("processoPoloPassivoGrid");
		refreshGrid("processoIncidentePoloAtivoGrid");
		refreshGrid("processoIncidentePoloPassivoGrid");
		FacesMessages.instance().clear();
		partesList = new ArrayList<ProcessoParte>(0);
	}

	public void inserirProcessoNoFluxo(int idProcesso, Fluxo fluxo) throws PJeBusinessException {
		inserirProcessoNoFluxo(idProcesso, fluxo, null);
	}

	public void inserirProcessoNoFluxo(int idProcesso, Fluxo fluxo, Map<String, Object> variaveis) throws PJeBusinessException {
		log.info(MessageFormat.format("Distribuindo processo id {0} para o fluxo {1}", idProcesso, fluxo.getFluxo()));
		setId(idProcesso);
		ProcessoHome.instance().setId(idProcesso);
		ProcessoHome.instance().adicionarFluxo(fluxo, variaveis);
		org.jboss.seam.bpm.TaskInstance.instance().setActorId(null);
		SwimlaneInstance swimlaneInstance = org.jboss.seam.bpm.TaskInstance.instance().getSwimlaneInstance();
		String actorsExpression = swimlaneInstance.getSwimlane().getPooledActorsExpression();
		LocalizacaoAssignment.instance().setPooledActors(actorsExpression);
	}

	public void executarDistribuicao(Fluxo fluxo, Date dataDistribuicao, Competencia competencia) throws Exception{
		//Se o movimento 26 (Distribuição) já tiver sido lançado para o processo, então se trata de uma redistribuição
		boolean isRedistribuicao = existeMovimentoLancadoPorIdEvento(ParametroUtil.instance().getEventoDistribuicao().getIdEvento());
		
		this.getInstance().setOrgaoJulgador(this.orgaoJulgador);
		
		//Executa prevenção
		try {
			verificarPrevencao();
		} catch (PontoExtensaoException e) {
			informarErroNaPrevencao(e);
			falhaNaPrevencaoExterna = true;
		} catch (PrevencaoException e) {
			informarErroNaPrevencao(e);
		}
		
		distribuirProcesso(getCompetenciaConflito(), getInstance());

		boolean je = ParametroJtUtil.instance().justicaEleitoral();

		String complemento;
		if (isRedistribuicao){
			//2: sorteio; 4: dependência
			complemento = instance.getDesProcReferencia() == null ? "2" : "4";
			
			// Código = 36 - Descrição = Redistribuído por #{tipo_de_distribuicao_redistribuicao} #{motivo_da_redistribuicao}
			// **************************************************************************************
			MovimentoAutomaticoService.preencherMovimento().deCodigo(ParametroUtil.getFromContext(CodigoMovimentoNacional.COD_MOVIMENTO_REDISTRIBUICAO, true))
									  .associarAoProcesso(getInstance().getProcesso())
									  .comProximoComplementoVazio().doTipoDominio().preencherComElementoDeCodigo(complemento) //sorteio
									  .lancarMovimento();
		}else{
			/*
			 * [PJEII-3651] Se for JE e ocorrer a dependência pela Distribuição por prevento (art. 260 CE).
			 * Uso do processo dependência conforme sugerido pelo Dr. Paulo Cristovão.
			 */
			if (je && instance.getProcessoDependencia() != null) {
				// Remove a dependência
				instance.setProcessoDependencia(null);
				// 3: prevento
				complemento = "3";
				TipoDistribuicao260 tipo = DistribuicaoService.instance().getTipoDistribuicao260();
			 	if (tipo != null)
			 	{
			 		if (tipo.equals(TipoDistribuicao260.PREVENCAO_ESTADUAL))
			 		{
			 			complemento = "6003";
			 		}
			 		else if (tipo.equals(TipoDistribuicao260.PREVENCAO_MUNICIPAL) )
			 		{
			 			complemento = "6004";
			 		}
			 	}
			}else{
				if(getInstance().getCompetencia().getIndicacaoOrgaoJulgadorObrigatoria()) {
					complemento = "1";
				} else {
					complemento = instance.getIsIncidente() == null || !instance.getIsIncidente() ? "2" : "4";
				}
				
				if (je) {
				 	TipoDistribuicao260 tipo = DistribuicaoService.instance().getTipoDistribuicao260();
				 	if (tipo != null)
				 	{
				 		if (tipo.equals(TipoDistribuicao260.SORTEIO_ESTADUAL)) 
				 		{
				 			complemento = "6001";
				 		}
				 		else if (tipo.equals(TipoDistribuicao260.SORTEIO_MUNICIPAL))
				 		{
				 			complemento = "6002";
				 		}
				 	}
				 }
			}
			// Código = 26 - Descrição = Distribuído por #{tipo_de_distribuicao_redistribuicao}
			// **************************************************************************************
			MovimentoAutomaticoService.preencherMovimento().deCodigo(CodigoMovimentoNacional.COD_MOVIMENTO_DISTRIBUICAO_DISTRIBUIDO)
			  						  .associarAoProcesso(getInstance().getProcesso())
			  						  .comProximoComplementoVazio().doTipoDominio().preencherComElementoDeCodigo(complemento)
			  						  .lancarMovimento();
		}
		
		numerarProcessoJudicial();
		
		Map<String, Object> variaveis = new HashMap<String, Object>();
		variaveis.put("orgaoJulgador", getInstance().getOrgaoJulgador().getIdOrgaoJulgador());
		if (getInstance().getProcessoStatus() == ProcessoStatusEnum.D){
		    if (!"true".equals(ParametroUtil.getFromContext("pje:audiencia:designacaoEmFluxo", false))){
		        agendarAudiencia();
		    }
		}
		ProcessoHome.instance().adicionarFluxo(fluxo, variaveis);
		org.jboss.seam.bpm.TaskInstance.instance().setActorId(null);
		SwimlaneInstance swimlaneInstance = org.jboss.seam.bpm.TaskInstance.instance().getSwimlaneInstance();
		String actorsExpression = swimlaneInstance.getSwimlane().getPooledActorsExpression();
		LocalizacaoAssignment.instance().setPooledActors(actorsExpression);
		
		if (falhaNaPrevencaoExterna) {
			ProcessInstance processInstance = org.jboss.seam.bpm.TaskInstance.instance().getProcessInstance();
			processInstance.getRootToken().getProcessInstance().getContextInstance().setVariable(Variaveis.HOUVE_FALHA_PREVENCAO_EXTERNA, Boolean.TRUE);
		}
		
	}

	private void lancarMovimento(boolean isRedistribuicao) {
		if (isRedistribuicao){
			lancarMovimentoDeRedistribuicao();
		}else{
			lancarMovimento();
		}
	}

	private void lancarMovimento() {
		String complemento;
		if (instance.getProcessoDependencia() != null && seJusticaEleitoralETipoIgualAEnum(
				DistribuicaoService.instance().getTipoDistribuicao260(), TipoDistribuicao260.PREVENCAO_ESTADUAL)) {
			instance.setProcessoDependencia(null);
			complemento = "6003";
		} else if (instance.getProcessoDependencia() != null && seJusticaEleitoralETipoIgualAEnum(
				DistribuicaoService.instance().getTipoDistribuicao260(), TipoDistribuicao260.PREVENCAO_MUNICIPAL)) {
			instance.setProcessoDependencia(null);
			complemento = "6004";
		} else if (ParametroJtUtil.instance().justicaEleitoral() && instance.getProcessoDependencia() != null) {
			instance.setProcessoDependencia(null);
			complemento = "3";
		} else if (seJusticaEleitoralETipoIgualAEnum(DistribuicaoService.instance().getTipoDistribuicao260(),
				TipoDistribuicao260.SORTEIO_ESTADUAL)) {
			complemento = "6001";
		} else if (seJusticaEleitoralETipoIgualAEnum(DistribuicaoService.instance().getTipoDistribuicao260(),
				TipoDistribuicao260.SORTEIO_MUNICIPAL)) {
			complemento = "6002";
		} else if (Boolean.TRUE.equals(getInstance().getCompetencia().getIndicacaoOrgaoJulgadorObrigatoria())) {
			complemento = "1";
		} else {
			complemento = instance.getIsIncidente() == null || !instance.getIsIncidente() ? "2" : "4";
		}

		MovimentoAutomaticoService.preencherMovimento().deCodigo(CodigoMovimentoNacional.COD_MOVIMENTO_DISTRIBUICAO_DISTRIBUIDO)
		  						  .associarAoProcesso(getInstance().getProcesso())
		  						  .comProximoComplementoVazio().doTipoDominio().preencherComElementoDeCodigo(complemento)
		  						  .lancarMovimento();
	}

	private boolean seJusticaEleitoralETipoIgualAEnum(TipoDistribuicao260 tipo, TipoDistribuicao260 tipoReferencia) {
		boolean je = ParametroJtUtil.instance().justicaEleitoral();
		return je && seIgualAEnum(tipo, tipoReferencia);
	}

	private boolean seIgualAEnum(TipoDistribuicao260 tipo, TipoDistribuicao260 tipoReferencia) {
		return tipo != null && tipo.equals(tipoReferencia);
	}

	private void lancarMovimentoDeRedistribuicao() {
		String complemento = instance.getDesProcReferencia() == null ? "2" : "4";
		MovimentoAutomaticoService.preencherMovimento().deCodigo(ParametroUtil.getFromContext(CodigoMovimentoNacional.COD_MOVIMENTO_REDISTRIBUICAO, true))
								  .associarAoProcesso(getInstance().getProcesso())
								  .comProximoComplementoVazio().doTipoDominio().preencherComElementoDeCodigo(complemento) //sorteio
								  .lancarMovimento();
	}
	
	private void distribuirProcesso(Competencia competenciaConflitoLocal, ProcessoTrf processo) throws Exception {
		if (competenciaConflitoLocal == null){
			DistribuicaoService.instance().distribuirProcesso(processo);
		} else {
			instance.setCompetencia(competenciaConflitoLocal);
			DistribuicaoService.instance().distribuirProcesso(processo, competenciaConflitoLocal);
		}
	}

	public void registraHistoricoDistribuicao(ProcessoTrf processoTrf, Fluxo fluxo){
		HistoricoDistribuicao historicoDistribuicao = new HistoricoDistribuicao();
		historicoDistribuicao.setProcessoTrf(processoTrf);
		historicoDistribuicao.setCargo(processoTrf.getCargo());
		historicoDistribuicao.setDataDistribuicao(processoTrf.getDataDistribuicao());
		historicoDistribuicao.setOrgaoJulgador(processoTrf.getOrgaoJulgador());
		historicoDistribuicao.setOrgaoJulgadorColegiado(processoTrf.getOrgaoJulgadorColegiado());
		historicoDistribuicao.setFluxo(fluxo);
		getEntityManager().persist(historicoDistribuicao);
	}

	public String dataAjuizamento(){
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
		String data = "";
		if (getInstance().getDataAutuacao() != null){
			data = formato.format(getInstance().getDataAutuacao());
		}
		return data;
	}

	@SuppressWarnings("unchecked")
	public List<AssuntoTrf> getListaAssunto(){
		String sql = "select o.assuntoTrf from ProcessoAssunto o " + "where o.processoTrf = :processoTrf";
		Query q = getEntityManager().createQuery(sql);
		q.setParameter("processoTrf", getInstance());
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public Competencia getCompetencia(ProcessoTrf processoTrf){

		List<AssuntoTrf> assuntoTrfList = new ArrayList<AssuntoTrf>(0);

		if (assuntoTrfList != null && assuntoTrfList.size() > 0){
			StringBuilder sb = new StringBuilder();
			sb.append("select o from Competencia o ");
			sb.append("inner join o.competenciaClasseAssuntoList compClAss ");
			sb.append("where compClAss.classeAplicacao.classeJudicial = :classeJudicial ");
			sb.append("and compClAss.assuntoTrf in (:assuntoTrf) ");
			sb.append("and compClAss.classeAplicacao.aplicacaoClasse = :aplicacao ");
			sb.append("and o.ativo = true ");
			Query query = getEntityManager().createQuery(sb.toString());
			query.setParameter("assuntoTrf", assuntoTrfList);
			query.setParameter("classeJudicial", processoTrf.getClasseJudicial());
			query.setParameter("aplicacao", ParametroUtil.instance().getAplicacaoSistema());
			for (Competencia comp : (List<Competencia>) query.getResultList()){
				for (Competencia competencia : (List<Competencia>) query.getResultList()){
					if (competencia.getCompetenciaPai() != null
						&& comp.getIdCompetencia() == competencia.getCompetenciaPai().getIdCompetencia()){
						return competencia;
					}
				}
				return comp;
			}
			return EntityUtil.getSingleResult(query);
		}
		else{
			return null;
		}
	}

	public List<Competencia> getCompetencias(ProcessoTrf processoTrf){
		if (competenciasPossiveis.size() == 0){
			competenciasPossiveis = getCompetenciasProcesso(processoTrf);
		}
		if (competenciasPossiveis.size() == 1){
			compEscolhida = competenciasPossiveis.get(0);
			competenciaConflito = compEscolhida;
		}
		if(competenciasPossiveis.size() > 1 && compEscolhida != null){
			competenciaConflito = compEscolhida;
		 }
		return competenciasPossiveis;
	}

	public void removerAssuntosMudancaCompetencia(){
		if (!getInstance().getClasseJudicial().equals(getClasseJudicialAnterior())){
			if (mudaClasse){
				excluiAssuntosDiversos();
			}

			setClasseJudicialAnterior(getInstance().getClasseJudicial());
			refreshGrid(GRID_ASSUNTO_CAD_PROCESSO);
			refreshGrid("processoAssuntoGrid");
		}
	}

	public Competencia getCompetencia(){
		return getCompetencia(getInstance());
	}

	public Pessoa getPessoaLogada(){
		return pessoaLogada;
	}

	public void setPessoaLogada(Pessoa pessoaLogada){
		this.pessoaLogada = pessoaLogada;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaLogada(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaLogada(PessoaFisicaEspecializada pessoa){
		setPessoaLogada(pessoa != null ? pessoa.getPessoa() : (Pessoa) null);
	}

	public Boolean isEmElaboracao(){
		if (isManaged()){
			return instance.getProcessoStatus().equals(ProcessoStatusEnum.E);
		}
		return false;
	}

	public void verificarMensagem(){

		if (getInstance().getApreciadoSegredo() == ProcessoTrfApreciadoEnum.A){
			setMensagem(Boolean.TRUE);
		}
		else{
			setMensagem(Boolean.FALSE);
		}

		if ((getInstance().getTutelaLiminar() == Boolean.TRUE)
			&& (getInstance().getApreciadoTutelaLiminar() == null || (getInstance().getApreciadoTutelaLiminar() == Boolean.FALSE))){
			setMensagemTutelaLiminar(Boolean.TRUE);
		}
		else{
			setMensagemTutelaLiminar(Boolean.FALSE);
		}

		if ((getInstance().getJusticaGratuita() == Boolean.TRUE)
			&& (getInstance().getApreciadoJusticaGratuita() == null || (getInstance().getApreciadoJusticaGratuita() == Boolean.FALSE))){
			setMensagemJusticaGratuita(Boolean.TRUE);
		}
		else{
			setMensagemJusticaGratuita(Boolean.FALSE);
		}

		if (getInstance().getApreciadoSigilo() == ProcessoTrfApreciadoEnum.A){
			setMensagemSigilo(Boolean.TRUE);
		}
		else{
			setMensagemSigilo(Boolean.FALSE);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from ProcessoDocumentoPeticaoNaoLida o where ");
		sb.append("o.retirado = false and o.processoDocumento.processo = :processo");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processo", getInstance().getProcesso());
		try {
			Long retorno = (Long) q.getSingleResult();
			setMensagemPeticoes(retorno > 0);
		} catch (NoResultException no) {
		}
	}

	public Boolean getMensagem(){
		return mensagem;
	}

	public void setMensagem(Boolean mensagem){
		this.mensagem = mensagem;
	}

	public void alterarApreciado(){
		setMensagem(Boolean.FALSE);
		getInstance().setApreciadoSegredo(ProcessoTrfApreciadoEnum.S);
		super.update();
		refreshGrid("painelUsuarioMagistradoGrid");
	}

	public void alterarApreciadoJusticaGratuita(){
		setMensagemJusticaGratuita(Boolean.FALSE);
		getInstance().setApreciadoJusticaGratuita(true);
		super.update();
		refreshGrid("pedidoJusticaGratuitaGrid");
	}

	public Boolean getMensagemTutelaLiminar(){
		return mensagemTutelaLiminar;
	}

	public void setMensagemTutelaLiminar(Boolean mensagemTutelaLiminar){
		this.mensagemTutelaLiminar = mensagemTutelaLiminar;
	}

	public void alterarApreciadoTutelaLiminar(){
		setMensagemTutelaLiminar(Boolean.FALSE);
		getInstance().setApreciadoTutelaLiminar(true);
		super.update();
		refreshGrid("tutelaLiminarGrid");
	}

	public boolean possuiClasse(int idClasse){
		ProcessoHome pHome = ProcessoHome.instance();
		int idProcesso = pHome.getInstance().getIdProcesso();
		ProcessoTrf processo = EntityUtil.getEntityManager().find(ProcessoTrf.class, idProcesso);
		return processo.getClasseJudicial().getIdClasseJudicial() == idClasse;
	}

	/**
	 * retorna o revisor de um processo em uma sessão
	 * 
	 * @return retorna o oj revisor
	 */
	public OrgaoJulgador getRevisor(){
		Criteria criteria = HibernateUtil.getSession().createCriteria(SessaoComposicaoOrdem.class);
		criteria.add(Restrictions.eq("orgaoJulgador", getInstance().getOrgaoJulgador()));
		criteria.add(Restrictions.eq("sessao", getSessao()));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		SessaoComposicaoOrdem sessaoComposicaoOrdem = (SessaoComposicaoOrdem) criteria.uniqueResult();
		OrgaoJulgador oj = null;
		if (sessaoComposicaoOrdem != null){
			oj = sessaoComposicaoOrdem.getOrgaoJulgador();
		}

		return oj;
	}

	public void setarApreciadoJusticaGratuita(ProcessoTrf processoTrf){
		processoTrf.setApreciadoJusticaGratuita(Boolean.TRUE);
		processoTrf.setPessoaApreciouJusticaGratuita(((PessoaFisica) Authenticator.getPessoaLogada()).getPessoaMagistrado());
		getEntityManager().merge(processoTrf);
		getEntityManager().flush();
	}

	public boolean isEmPedidoSegredoJustica(ProcessoTrf processoTrf){
		if (processoTrf == null){
			throw new IllegalArgumentException("O processo está nulo");
		}
		return ProcessoTrfApreciadoEnum.A == processoTrf.getApreciadoSegredo();
	}

	public boolean isEmPedidoSegredoJustica(){
		return isEmPedidoSegredoJustica(getInstance());
	}

	public boolean isEmPedidoUrgencia(ProcessoTrf processoTrf){
		if (processoTrf == null){
			throw new IllegalArgumentException("O processo está nulo");
		}
		Boolean tutelaLiminar = processoTrf.getTutelaLiminar() == null ? false : processoTrf.getTutelaLiminar();
		Boolean apreciadoTutelaLiminar = processoTrf.getApreciadoTutelaLiminar() == null ? false : processoTrf
				.getApreciadoTutelaLiminar();
		return (tutelaLiminar && !apreciadoTutelaLiminar);
	}

	public boolean isEmPedidoUrgencia(){
		return isEmPedidoUrgencia(getInstance());
	}

	public void setarProcessoOriginario(ProcessoTrf processo){
		getInstance().setProcessoOriginario(processo);
		this.instance.setProcessoReferencia(processo);
		this.setProcessoEncontrado(processo);
		if(processo != null){
			this.instance.setDesProcReferencia(processo.getNumeroProcesso());
			this.instance.setJurisdicao(processo.getJurisdicao());
			this.instance.setOrgaoJulgador(processo.getOrgaoJulgador());
			this.instance.setOrgaoJulgadorColegiado(processo.getOrgaoJulgadorColegiado());
		}
	}

	public void setarProcessoDependencia(ProcessoTrf processo){
		getInstance().setProcessoDependencia(processo);
		getProcessoDependenciaSuggest().setInstance(getInstance().getProcessoDependencia());
	}

	private ProcessoOriginarioSuggestBean getProcessoOriginarioSuggest(){
		return getComponent("processoOriginarioSuggest");
	}

	private ProcessoDependenciaSuggestBean getProcessoDependenciaSuggest(){
		return getComponent("processoDependenciaSuggest");
	}

	public String protocolarProcesso(){
		getInstance().setProcessoStatus(ProcessoStatusEnum.V);
		String update = update();
		newInstance();
		return update;
	}

	public Boolean getMensagemSigilo(){
		return mensagemSigilo;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoEvento> getEventosProcessoMagistrado(){
		List<Evento> eventos = EventoHome.instance().getEventoLeafMagistrado();
		EntityManager em = getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from ProcessoEvento o ").append("where o.processo = :processo ")
				.append("and o.evento in (:evento)");
		Query query = em.createQuery(sql.toString());
		query.setParameter("processo", getInstance().getProcesso());
		query.setParameter("evento", Util.isEmpty(eventos)?null:eventos);
		List<ProcessoEvento> result = query.getResultList();
		return result;
	}

	public void setMensagemSigilo(Boolean mensagemSigilo){
		this.mensagemSigilo = mensagemSigilo;
	}

	public void alterarApreciadoSigilo(){
		setMensagemSigilo(Boolean.FALSE);
		getInstance().setApreciadoSigilo(ProcessoTrfApreciadoEnum.S);
		super.update();
		refreshGrid("documentoSigiloGrid");
	}

	@SuppressWarnings("unchecked")
	private List<UsuarioLocalizacaoVisibilidade> getVisibilidadeLoc(UsuarioLocalizacao ul){
		String sql = "select o from UsuarioLocalizacaoVisibilidade o "
			+ "where o.usuarioLocalizacaoMagistradoServidor.usuarioLocalizacao.idUsuarioLocalizacao = :ul";
		Query q = getEntityManager().createQuery(sql);
		q.setParameter("ul", ul.getIdUsuarioLocalizacao());
		return q.getResultList();
	}

	public boolean exibeAbaPermissoes(){
		UsuarioLocalizacao usuarioLocalizacaoAtual = Authenticator.getUsuarioLocalizacaoAtual();
		PessoaMagistrado pessoaMagistrado = EntityUtil.find(PessoaMagistrado.class, usuarioLocalizacaoAtual
				.getUsuario().getIdUsuario());
		
		if (magistradoVisibilidade == null) {
			magistradoVisibilidade = getVisibilidadeLoc(usuarioLocalizacaoAtual);
		}
		
		if (pessoaMagistrado != null){
			if (getInstance().getCargo() != null && getInstance().getCargo().getCargo() != null){
				List<UsuarioLocalizacaoVisibilidade> magistradoVisibilidade = getVisibilidadeLoc(usuarioLocalizacaoAtual);
				if (magistradoVisibilidade.size() > 0){
					for (UsuarioLocalizacaoVisibilidade ulv : magistradoVisibilidade) {
						if (ulv.getOrgaoJulgadorCargo() == null) {
							if (ulv.getUsuarioLocalizacaoMagistradoServidor() != null) {
								if(ulv.getUsuarioLocalizacaoMagistradoServidor().getOrgaoJulgador() != null &&
										ulv.getUsuarioLocalizacaoMagistradoServidor().getOrgaoJulgador()
										.equals(getInstance().getOrgaoJulgador())) {
									return true;
								} else if (ulv.getUsuarioLocalizacaoMagistradoServidor().getOrgaoJulgadorColegiado() != null &&	
										ulv.getUsuarioLocalizacaoMagistradoServidor().getOrgaoJulgadorColegiado()
										.equals(getInstance().getOrgaoJulgadorColegiado())) {
									return true;
								}
							}
						}
						else if ((ulv.getOrgaoJulgadorCargo() != null)
							&& ulv.getOrgaoJulgadorCargo().getCargo().equals(getInstance().getCargo())){
							return true;
						}
					}
					EntityManager em = getEntityManager();
					StringBuilder sqlPes = new StringBuilder();
					sqlPes.append(" select count(o) from UsuarioLocalizacao o, OrgaoJulgador oj where ");
					sqlPes.append(" o.papel.identificador LIKE 'magistrado' ");
					sqlPes.append(" and oj.localizacao = o.localizacaoFisica ");
					sqlPes.append(" and oj = :orgaoJulgador ");
					sqlPes.append(" and o != :usuarioLocalizacaoAtual ");
					Query query = em.createQuery(sqlPes.toString());
					query.setParameter("orgaoJulgador", getInstance().getOrgaoJulgador());
					query.setParameter("usuarioLocalizacaoAtual", usuarioLocalizacaoAtual);
					
					Long count;
					try {
						count = (Long) query.getSingleResult();
					} catch (NoResultException e) {
						count = 0L;
					}
					
					return (count.compareTo(0L) == 0);
				}
				else{
					return false;
				}
			}
			else{
				return true;
			}
		}
		return false;
	}

	/*
	 * variével para habilitar/dasabilitar o radio segredoJustica do componente caracteristicaProcessoForm
	 */
	public Boolean getHabilitaDesabiliaRadioSegredoJustica(){
		Boolean classeJudicialSegredoJustica = verificarClasseSigilosa();
		return habilitaDesabiliaRadioSegredoJustica ||classeJudicialSegredoJustica; 
	}

	public void setHabilitaDesabiliaRadioSegredoJustica(Boolean habilitaDesabiliaRadioSegredoJustica){
		this.habilitaDesabiliaRadioSegredoJustica = habilitaDesabiliaRadioSegredoJustica;
	}

	/*
	 * Metodo que verifica de o processo jé esta sendo visualizado por outro usuário.
	 */
	public String verificaProcessoConsultado(){
		if (getInstance().getNumeroProcesso() != null){
			String query = "select o from Processo o where o.numeroProcesso = :nrProcesso";
			Query q = getEntityManager().createQuery(query);
			q.setParameter("nrProcesso", getInstance().getNumeroProcesso());
			Processo processo = EntityUtil.getSingleResult(q);
			if (processo.getActorId() != null){
				return !processo.getActorId().equals(Authenticator.getUsuarioLogado().getLogin()) ? "Este Processo esta sendo visualizado pelo usuário: "
					+ processo.getActorId()
						: null;
			}
		}
		return null;
	}

	public Boolean getMensagemPeticoes(){
		return mensagemPeticoes;
	}

	public void setMensagemPeticoes(Boolean mensagemPeticoes){
		this.mensagemPeticoes = mensagemPeticoes;
	}

	public void alterarRetirado(){
		setMensagemPeticoes(Boolean.FALSE);
		if (processoDocumentoPeticaoNaoLidaManager == null) {
			processoDocumentoPeticaoNaoLidaManager = ComponentUtil.getComponent(ProcessoDocumentoPeticaoNaoLidaManager.class);
		}
		List<ProcessoDocumentoPeticaoNaoLida> list = processoDocumentoPeticaoNaoLidaManager
				.obterProcessoDocumentoPeticaoNaoLida(getInstance());
		ProcessoDocumentoPeticaoNaoLidaHome lidaHome = ProcessoDocumentoPeticaoNaoLidaHome.instance();
		for (int i = 0; i < list.size(); i++){
			list.get(i).setRetirado(Boolean.TRUE);
			lidaHome.setInstance(list.get(i));
			lidaHome.update();
		}
		listaProcessoPeticaoNaoLida();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO,FacesUtil.getMessage("entity_messages", "peticao.retirada.painel"));
	}
	
	/**
	 * Listagem das petições Não lidas baseado no processo 
	 * da instancia atual <b>getInstance()</b>
	 * 
	 */
	public void listaProcessoPeticaoNaoLida(){
		processoDocumentoPeticaoNaoLidaManager = ComponentUtil.getComponent(ProcessoDocumentoPeticaoNaoLidaManager.class);
		processoDocumentoPeticaoNaoLida = processoDocumentoPeticaoNaoLidaManager
				.obterProcessoDocumentoPeticaoNaoLida(getInstance());
	}
	
	
	/**
	 * Resgata a mensagem de declaração, dependendo de documento petição.  
	 * 
	 * @param ProcessoDocumentoPeticaoNaoLida
	 * @return uma <b>String</b> com a mensagem
	 */
	public String getProcessoDeclaracao(ProcessoDocumentoPeticaoNaoLida pd) {
		if (pd.getHabilitacaoAutos() != null && pd.getHabilitacaoAutos().getTipoDeclaracao() != null) {
			return pd.getHabilitacaoAutos().getTipoDeclaracao().getLabel();
		}
		else {
			return FacesUtil.getMessage("entity_messages", "processoTrf.mensagemDeclaracaoInstrumentoMandato");
		}
	}

	/** 
	 * Este é um getter que retorna a lista de processoDocumentoPeticaoNaoLida
	 * 
	 * @return uma lista de {@link #processoDocumentoPeticaoNaoLida}.
	 */
	public List<ProcessoDocumentoPeticaoNaoLida> getProcessoDocumentoPeticaoNaoLida() {
		if(processoDocumentoPeticaoNaoLida == null){
			processoDocumentoPeticaoNaoLida = new ArrayList<ProcessoDocumentoPeticaoNaoLida>();
		}
		return processoDocumentoPeticaoNaoLida;
	}

	/** 
	 * Este é um setter que define a lista de processoDocumentoPeticaoNaoLida
	 * 
	 */
	public void setProcessoDocumentoPeticaoNaoLida(
			List<ProcessoDocumentoPeticaoNaoLida> processoDocumentoPeticaoNaoLida) {
		this.processoDocumentoPeticaoNaoLida = processoDocumentoPeticaoNaoLida;
	}

	/*
	 * Metodo para verificar se um processo possui tarefa aberta.
	 */
	public boolean verificarProcessoTarefaAberta(ProcessoTrf processoTrf){
		EntityManager em = getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select count(o) from SituacaoProcesso o ");
		sql.append("where idProcesso = :nrProcesso");
		Query query = em.createQuery(sql.toString());

		query.setParameter("nrProcesso", processoTrf.getIdProcessoTrf());

		Long retorno = 0L;
		try {
			retorno = (Long) query.getSingleResult();
		} catch (NoResultException no) {
			possuiTarefaAberta = Boolean.FALSE;
		}
		possuiTarefaAberta = retorno > 0;
		return possuiTarefaAberta;
	}

	/*
	 * Metodo para verificar se um processo possui documentos nao lidos.
	 */
	public boolean verificarProcessoDoc(ProcessoTrf processoTrf){
		EntityManager em = getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select count(o) from ProcessoDocumentoTrf o ")
				.append("where (lower(to_ascii(o.processoDocumento.papel.identificador)) like lower(to_ascii('advogado'))")
				.append("or lower(to_ascii(o.processoDocumento.papel.identificador)) like lower(to_ascii('procurador')))")
				.append("and o.processoTrf.idProcessoTrf = :nrProcesso");
		Query query = em.createQuery(sql.toString());
		query.setParameter("nrProcesso", processoTrf.getIdProcessoTrf());
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
		
	}

	public Boolean getAvulsaAnexada(){
		return avulsaAnexada;
	}

	public void setAvulsaAnexada(Boolean avulsaAnexada){
		this.avulsaAnexada = avulsaAnexada;
	}

	public String getPrioridades(){
		try{
			String lista = this.instance.getPrioridadeProcessoList().toString();
			lista = lista.replace("[", "('");
			lista = lista.replace("]", "')");
			lista = lista.replace(", ", "','");
			return lista;
		} catch (Exception e){
			return "(0)";
		}
	}

	public PrioridadeProcesso getUltimaPrioridade(){
		return ultimaPrioridade;
	}

	public void setUltimaPrioridade(PrioridadeProcesso ultimaPrioridade){
		this.ultimaPrioridade = ultimaPrioridade;
	}

	public PrioridadeProcesso getPrioridadeRetirada(){
		return prioridadeRetirada;
	}

	public void setPrioridadeRetirada(PrioridadeProcesso prioridadeRetirada){
		this.prioridadeRetirada = prioridadeRetirada;
	}

	private void verificaConsulta(){
		if (idConsulta == 0 || nrProcessoConsulta == null){
			return;
		}
		setProcessoTrfIdProcessoTrf(idConsulta);
	}
	
	public void verificarConsultaPublica(int idProcesso, String numeroProcesso) {
		this.idConsulta = idProcesso;
		this.nrProcessoConsulta = numeroProcesso;
		this.verificaConsulta();
	}

	public int getIdConsulta(){
		return idConsulta;
	}

	public void setIdConsulta(int idConsulta){
		this.idConsulta = idConsulta;
		verificaConsulta();
	}

	public String getNrProcessoConsulta(){
		return nrProcessoConsulta;
	}

	public void setNrProcessoConsulta(String nrProcessoConsulta){
		setNrProcessoConsulta(nrProcessoConsulta, true);
	}
	
	public void setNrProcessoConsulta(String nrProcessoConsulta, Boolean verificar){
		this.nrProcessoConsulta = nrProcessoConsulta;
		if (verificar) {
			verificaConsulta();
		}
	}

	public Date getDataDistribuicao1(){
		return dataDistribuicao1;
	}

	public void setDataDistribuicao1(Date dataDistribuicao1){
		this.dataDistribuicao1 = dataDistribuicao1;
	}

	public Date getDataDistribuicao2(){
		return dataDistribuicao2;
	}

	public void setDataDistribuicao2(Date dataDistribuicao2){
		this.dataDistribuicao2 = dataDistribuicao2;
	}

	public Boolean getExibirTodosAlertas(){
		return exibirTodosAlertas;
	}

	public void setExibirTodosAlertas(Boolean exibirTodosAlertas){
		this.exibirTodosAlertas = exibirTodosAlertas;
	}

	public void setProntoPauta(Boolean prontoPauta){
		this.prontoPauta = prontoPauta;
	}

	public String getProcessoPartePoloAtivoSemAdvogadoStr(){
		return getProcessoParteSemAdvogadoStr(ProcessoParteParticipacaoEnum.A,false);
	}

	public String getProcessoPartePoloPassivoSemAdvogadoStr(){
		return getProcessoParteSemAdvogadoStr(ProcessoParteParticipacaoEnum.P,false);
	}

	public String getProcessoParteSemAdvogadoStr(){
		return getProcessoParteSemAdvogadoStr(ProcessoParteParticipacaoEnum.T,false);
	}

	public String getProcessoPartePoloAtivoSemAdvogadoStrValidaSigilo(){
		return getProcessoParteSemAdvogadoStr(ProcessoParteParticipacaoEnum.A, true);
	}

	public String getProcessoPartePoloPassivoSemAdvogadoStrValidaSigilo(){
		return getProcessoParteSemAdvogadoStr(ProcessoParteParticipacaoEnum.P,true);
	}

	public String getProcessoParteSemAdvogadoStrValidaSigilo(){
		return getProcessoParteSemAdvogadoStr(ProcessoParteParticipacaoEnum.T,true);
	}

	
	private String getProcessoParteSemAdvogadoStr(ProcessoParteParticipacaoEnum inParticipacao, Boolean sigilo){
		List<ProcessoParte> processoParteSemAdvogadoList = null;
		StringBuilder sb = new StringBuilder();
		Set<TipoParte> tipoParteList = new HashSet<TipoParte>(0);

		switch (inParticipacao){
		case A:
			processoParteSemAdvogadoList = instance.getProcessoPartePoloAtivoSemAdvogadoList();
			break;
		case P:
			processoParteSemAdvogadoList = instance.getProcessoPartePoloPassivoSemAdvogadoList();
			break;
		default:
			processoParteSemAdvogadoList = instance.getProcessoParteSemAdvogadoList();
			break;

		}

		for (ProcessoParte processoParte : processoParteSemAdvogadoList){
			TipoParte tipoParte = processoParte.getTipoParte();
			if (!tipoParteList.contains(tipoParte)){
				String partesPorTipo = getProcessoParteStr(processoParteSemAdvogadoList, tipoParte, sigilo);
				sb.append(partesPorTipo + QUEBRA_DE_LINHA);
				tipoParteList.add(tipoParte);
			}
		}
		if(tipoParteList.size() == 1 &&  sb.lastIndexOf(QUEBRA_DE_LINHA) >= 0) {
			sb.delete(sb.lastIndexOf(QUEBRA_DE_LINHA), sb.length());
		}
		return sb.toString();
	}

	public String getProcessoOutrosParticipantesSemAdvogadoStr(){
		return getProcessoOutrosParticipantesSemAdvogadoStr(false);
		
	}
	public String getProcessoOutrosParticipantesSemAdvogadoStr(Boolean sigilo){
		List<ProcessoParte> processoParteSemAdvogadoList = null;
		StringBuilder sb = new StringBuilder();
		Set<TipoParte> tipoParteList = new HashSet<TipoParte>(0);
		processoParteSemAdvogadoList = instance.getProcessoParteSemAdvogadoList();
		for (ProcessoParte processoParte : processoParteSemAdvogadoList) {
			if (processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.T)) {
				List<ProcessoParteRepresentante> listaRepresentantes = processoParte.getProcessoParteRepresentanteListAtivos();
				if (listaRepresentantes != null && listaRepresentantes.size() > 0) {
					TipoParte tipoParte = processoParte.getTipoParte();
					if (!tipoParteList.contains(tipoParte)){
						String partesPorTipo = getProcessoParteStr(processoParteSemAdvogadoList, tipoParte,sigilo);
						sb.append(partesPorTipo + QUEBRA_DE_LINHA);
						tipoParteList.add(tipoParte);
					}
				}
			}
		}
		return sb.toString();
	}
	
	public String getProcessoParteRepresentantePoloAtivoStr(){
		return getProcessoParteRepresentanteStr(ProcessoParteParticipacaoEnum.A);
	}

	public String getProcessoParteRepresentantePoloPassivoStr(){
		return getProcessoParteRepresentanteStr(ProcessoParteParticipacaoEnum.P);
	}

	public String getProcessoParteRepresentanteOutrosParticipantesStr(){
		return getProcessoParteRepresentanteStr(ProcessoParteParticipacaoEnum.T);
	}
	
	private String getProcessoParteRepresentanteStr(ProcessoParteParticipacaoEnum inParticipacao) {
		StringBuilder result = new StringBuilder();
		List<ProcessoParte> processoParteList = null;
		
		switch (inParticipacao) {
			case A:
				processoParteList = instance.getProcessoPartePoloAtivoSemAdvogadoList();
				break;
			case P:
				processoParteList = instance.getProcessoPartePoloPassivoSemAdvogadoList();
				break;
			case T:
				processoParteList = instance.getProcessoOutrosParticipantesSemAdvogadoList();
				break;
			default:
				processoParteList = instance.getProcessoParteSemAdvogadoList();
		}
		for (ProcessoParte pp : processoParteList) {
			List<ProcessoParteRepresentante> pprListAtivos = pp.getProcessoParteRepresentanteListAtivos();
			for (ProcessoParteRepresentante ppr : pprListAtivos) {
				ProcessoParte processoParte = ppr.getParteRepresentante();
				result.append(processoParte.getTipoParte().getTipoParte() + " do(a) " + pp.getTipoParte().getTipoParte() + ": ");
				PessoaFisica pessoa = (PessoaFisica)processoParte.getPessoa();
				PessoaAdvogado pessoaAdvogado = pessoa.getPessoaAdvogado();
				result.append(pessoa.getNome().toUpperCase());
				if (pessoaAdvogado != null) {
					if (StringUtils.isNotBlank(pessoaAdvogado.getOabFormatado())) {
						result.append(" - " + pessoaAdvogado.getOabFormatado());
					}
				}
				result.append(QUEBRA_DE_LINHA);
			}
		}
		return result.toString();
	}
	
	private SessaoPautaProcessoTrf getPautaSessaoAbertaFutura() {
		ProcessoTrf processo = this.getInstance();
		SessaoPautaProcessoTrfManager pautaManager = getComponent("sessaoPautaProcessoTrfManager");
		List<SessaoPautaProcessoTrf> pautas = pautaManager.getSessoesJulgamentoPautados(processo, TipoSituacaoPautaEnum.AJ);
		SessaoPautaProcessoTrf pautaSessao = null;
		for (SessaoPautaProcessoTrf pauta : pautas) {
			if (pauta.getDataExclusaoProcessoTrf() == null
				&& pauta.getSessao() != null
				&& pauta.getSessao().getDataSessao() != null
				&& pauta.getSessao().getDataSessao().after(new Date())
			) {
				if (pautaSessao == null
					|| pautaSessao.getSessao().getDataSessao().after(pauta.getSessao().getDataSessao())
				) {
					pautaSessao = pauta;
				}
			}
		}
		return pautaSessao;
	}
	
	public String getDataHoraSessaoJulgamentoStr() {
		SessaoPautaProcessoTrf pautaSessao = getPautaSessaoAbertaFutura();
		if (pautaSessao == null) {
			return "A pauta da sessão Não foi encontrada.";
		}
		Sessao sessao = pautaSessao.getSessao();
		Date data = sessao.getDataSessao();
		SalaHorario salaHorario = sessao.getOrgaoJulgadorColegiadoSalaHorario();
		Date hora = salaHorario.getHoraInicial();
		if (hora == null) {
			return "O horério Não está cadastrado.";
		}
		SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
		String dataHoraSessaoJulgamentoStr = formatoData.format(data) + " " + formatoHora.format(hora);
		return dataHoraSessaoJulgamentoStr;
	}

	public String getLocalSessaoJulgamentoStr() {
		SessaoPautaProcessoTrf pautaSessao = getPautaSessaoAbertaFutura();
		if (pautaSessao == null) {
			return "A pauta da sessão Não foi encontrada.";
		}
		Sessao sessao = pautaSessao.getSessao();
		SalaHorario salaHorario = sessao.getOrgaoJulgadorColegiadoSalaHorario();
		Sala sala = salaHorario.getSala();
		String localSessaoJulgamentoStr = sala.getSala();
		return localSessaoJulgamentoStr;
	}

	private String getProcessoParteStr(List<ProcessoParte> processoParteList, TipoParte tipoParte, Boolean sigilo){
		StringBuilder sb = new StringBuilder();

		for (ProcessoParte processoParte : processoParteList){
			if (processoParte.getTipoParte().getTipoParte().equals(tipoParte.getTipoParte()) && !(processoParte.getIsBaixado())){
				if (sb.length() == 0){
					sb.append(tipoParte.toString().toUpperCase() + ": ");
				}
				else{
					sb.append(", ");
				}

				sb.append(processoParte.getNomeParte().toUpperCase());
			}
		}
		return sb.toString();
	}

	public String getProcessoPartePoloAtivoDetalhadoStr(){
		return getProcessoParteDetalhadoStr(ProcessoParteParticipacaoEnum.A);
	}

	public String getProcessoPartePoloPassivoDetalhadoStr(){
		return getProcessoParteDetalhadoStr(ProcessoParteParticipacaoEnum.P);
	}

	public String getProcessoPartePassivoAtivoDetalhadoStr(){
		return getProcessoParteDetalhadoStr(ProcessoParteParticipacaoEnum.P);
	}

	public String getProcessoParteDetalhadoStr(ProcessoParteParticipacaoEnum inParticipacao) {

		List<ProcessoParte> processoParteList = null;

		switch (inParticipacao) {
			case A:
				processoParteList = instance.getProcessoPartePoloAtivoSemAdvogadoList();
				break;
			case P:
				processoParteList = instance.getProcessoPartePoloPassivoSemAdvogadoList();
				break;
			default:
				processoParteList = instance.getProcessoParteSemAdvogadoList();
		}

		return formatarNomeDosAdvogados(processoParteList);
	}

	private String formatarNomeDosAdvogados(List<ProcessoParte> processoParteList){
		StringBuilder nomeAdvogado = new StringBuilder();
		StringBuilder resultado = new StringBuilder();

		Set<String> nomes = new HashSet<>();

		for (ProcessoParte processoParte : processoParteList) {

			String nomesAdvogado =  concatenarNomeAdvogados(processoParte);

			if (!nomesAdvogado.isEmpty()) {
				if (nomesAdvogado.contains(",")) {
					nomeAdvogado.append("Advogados ");
				} else {
					nomeAdvogado.append("Advogado ");
				}
				nomeAdvogado.append("do(a) " + processoParte.getTipoParte().getTipoParte() + ": ")
						.append(nomesAdvogado)
						.append(QUEBRA_LINHA_HTML);

				nomes.add(nomeAdvogado.toString());

				nomeAdvogado = new StringBuilder();
			}
		}

		for (String nome : nomes) {
			resultado.append(nome);
		}

		return resultado.toString();

	}

	private  String concatenarNomeAdvogados(ProcessoParte processoParte){
		int contador = 0;
		StringBuilder nomesAdvogado = new StringBuilder();

		List<ProcessoParteRepresentante> pprListAtivos = processoParte.getProcessoParteRepresentanteListAtivos();

		ordenaListaProcessoParteRepresentante(pprListAtivos);

		for (ProcessoParteRepresentante ppr : pprListAtivos) {
			PessoaAdvogado pessoaAdvogado = ((PessoaFisica) ppr.getParteRepresentante().getPessoa()).getPessoaAdvogado();
			if (pessoaAdvogado != null) {
				nomesAdvogado.append(pessoaAdvogado.getNome().toUpperCase());
				if (StringUtils.isNotBlank(pessoaAdvogado.getOabFormatado())) {
					nomesAdvogado.append(" - " + pessoaAdvogado.getOabFormatado());
				}
				if(++contador < processoParte.getProcessoParteRepresentanteListAtivos().size()){
					nomesAdvogado.append(", ");
				}
			}
		}
		return nomesAdvogado.toString();
	}

	private void ordenaListaProcessoParteRepresentante(List<ProcessoParteRepresentante> pprListAtivos) {
		Collections.sort(pprListAtivos, (processoParteRepresentante1, processoParteRepresentante2) -> {

			PessoaAdvogado pessoaAdvogado1 = ((PessoaFisica) processoParteRepresentante1.getParteRepresentante().getPessoa()).getPessoaAdvogado();
			PessoaAdvogado pessoaAdvogado2 = ((PessoaFisica) processoParteRepresentante2.getParteRepresentante().getPessoa()).getPessoaAdvogado();

			if (pessoaAdvogado1 == null) {
				return -1;
			}else if (pessoaAdvogado2 == null) {
				return 1;
			}else {
				return pessoaAdvogado1.getNome().toUpperCase().compareTo(pessoaAdvogado2.getNome().toUpperCase());
			}
		});
	}

	public String getProcessoParteEnderecoPoloAtivoStr(){
		return getProcessoParteEnderecoStr(ProcessoParteParticipacaoEnum.A);
	}

	public String getProcessoParteEnderecoPoloPassivoStr(){
		return getProcessoParteEnderecoStr(ProcessoParteParticipacaoEnum.P);
	}

	public String getProcessoParteEnderecoPoloAtivoExpedienteStr(){
		return getProcessoParteEnderecoStr(ProcessoParteParticipacaoEnum.A, true);
	}

	public String getProcessoParteEnderecoPoloPassivoExpedienteStr(){
		return getProcessoParteEnderecoStr(ProcessoParteParticipacaoEnum.P, true);
	}

	public String getProcessoParteEnderecoStr(ProcessoParteParticipacaoEnum inParticipacao){
		return getProcessoParteEnderecoStr(inParticipacao, false);
	}

	public String getProcessoParteEnderecoStr(ProcessoParteParticipacaoEnum inParticipacao, boolean isExpediente){
		StringBuilder sb = new StringBuilder();
		List<ProcessoParte> processoParteList = new ArrayList<ProcessoParte>(0);

		switch (inParticipacao){
		case A:
			processoParteList = instance.getProcessoPartePoloAtivoSemAdvogadoList();
			break;
		case P:
			processoParteList = instance.getProcessoPartePoloPassivoSemAdvogadoList();
			break;
		default:
			processoParteList = instance.getProcessoParteSemAdvogadoList();
		}

		for (ProcessoParte pp : processoParteList){
			if(!pp.getIsBaixado()){
				sb.append("Nome: " + pp.getPessoa().getNome());
				sb.append(QUEBRA_LINHA_HTML);
				sb.append(Strings.isEmpty(pp.getProcessoParteEnderecoStr()) ? "Endereço: desconhecido" : pp
						.getProcessoParteEnderecoStr());
				sb.append(QUEBRA_LINHA_HTML);
			}
		}
		
		return sb.toString();
	}
	

	public String getProcessoParteEnderecoCompletoStr(ProcessoParte processoParte){
		StringBuilder sb = new StringBuilder();
		List<ProcessoParte> processoParteList = new ArrayList<ProcessoParte>(0);

	
		processoParteList = instance.getProcessoParteSemAdvogadoList();
		
		for (ProcessoParte pp : processoParteList){
				   if (pp.equals(processoParte)) {
					sb.append(QUEBRA_LINHA_HTML);
					sb.append(Strings.isEmpty(pp.getProcessoParteEnderecoStr()) ? "Endereço: desconhecido" : pp
							.getProcessoParteEnderecoStr());
					sb.append(QUEBRA_LINHA_HTML);
				   }
		}
		
		return sb.toString();
	}
	
	
	public String getAdvogadoEnderecoPoloAtivoStr(){
		return getAdvogadoEnderecoStr(ProcessoParteParticipacaoEnum.A);
	}

	public String getAdvogadoEnderecoPoloPassivoStr(){
		return getAdvogadoEnderecoStr(ProcessoParteParticipacaoEnum.P);
	}

	public String getAdvogadoEnderecoStr(ProcessoParteParticipacaoEnum inParticipacao){
		StringBuilder sb = new StringBuilder();
		List<ProcessoParte> advogadosList = null;

		switch (inParticipacao){
		case A:
			advogadosList = instance.getListaAdvogadosPoloAtivo();
			break;
		case P:
			advogadosList = instance.getListaAdvogadosPoloPassivo();
			break;
		default:
			advogadosList = instance.getListaAdvogados(ProcessoParteParticipacaoEnum.T);
		}

		for (ProcessoParte pp : advogadosList){
			sb.append((((PessoaFisica) pp.getPessoa()).getPessoaAdvogado()).getPessoaStr() + "\n");
			sb.append(Strings.isEmpty(pp.getProcessoParteEnderecoStr()) ? "Endereéo: desconhecido" : pp
					.getProcessoParteEnderecoStr());
			sb.append("\n\n");
		}
		return sb.toString();
	}

	public String getProcessoAudienciaListStr(){
		StringBuilder sb = new StringBuilder();
		for (ProcessoAudiencia processoAudiencia : instance.getProcessoAudienciaList()){
			if (processoAudiencia.getStatusAudiencia().equals(StatusAudienciaEnum.M)){
				sb.append(processoAudiencia.getProcessoAudienciaStr() + "\n");
			}
		}
		return sb.toString();
	}

	public Boolean isConcluso(){
		String query = "SELECT COUNT(o.idProcessoEvento) FROM ProcessoEvento AS o WHERE o.processo = :processo AND " +
				"(o.evento = :evento OR o.evento.eventoSuperior = :evento)";
		javax.persistence.Query q = EntityUtil.getEntityManager().createQuery(query);
		q.setParameter("processo", instance.getProcesso());
		q.setParameter("evento", ParametroUtil.instance().getEventoConclusao());
		Number cont = (Number) q.getSingleResult();
		return cont.intValue() > 0;
	}

	public Boolean getProntoPauta(){
		if (prontoPauta == null){
			OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();

			prontoPauta = instance.getClasseJudicial().getPauta() && instance.getOrgaoJulgador() != null
				&& instance.getOrgaoJulgador().getInstancia().equals("2") && isConcluso()
				&& orgaoJulgadorAtual != null && orgaoJulgadorAtual.equals(instance.getOrgaoJulgador());
		}
		return prontoPauta;
	}

	public boolean selecionadoPauta(){
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, getInstance().getIdProcessoTrf());
		ClasseJudicial cj = getInstance().getClasseJudicial();
		OrgaoJulgadorColegiado ojc = processoTrf.getOrgaoJulgadorColegiado();
		boolean prontoRevisao = getInstance().getProntoRevisao() != null ? getInstance().getProntoRevisao() : false;
		boolean pautaSemRevisor = cj.getPauta() && !BooleanUtils.isTrue(processoTrf.getExigeRevisor());
		boolean pautaComRevisor = cj.getPauta() && BooleanUtils.isTrue(processoTrf.getExigeRevisor());
		boolean revisadoAntecipacao = getInstance().getRevisado() || ojc.getPautaAntecRevisao();
		boolean ojRelator = getInstance().getOrgaoJulgadorColegiado().equals(Authenticator.getOrgaoJulgadorColegiadoAtual());
		boolean relator = RelatorRevisorEnum.REL.equals(ojc.getRelatorRevisor()) && ojRelator;
		boolean revisor = RelatorRevisorEnum.REV.equals(ojc.getRelatorRevisor()) && !ojRelator;

		return (pautaSemRevisor && ojRelator)
			|| (pautaComRevisor && prontoRevisao && ((relator && revisadoAntecipacao) || (revisor && getInstance()
					.getRevisado())));
	}

	public boolean selecionadoJulgamento(){
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, getInstance().getIdProcessoTrf());
		ClasseJudicial cj = getInstance().getClasseJudicial();
		OrgaoJulgadorColegiado ojc = processoTrf.getOrgaoJulgadorColegiado();
		boolean prontoRevisao = getInstance().getProntoRevisao() != null ? getInstance().getProntoRevisao() : false;
		boolean semPautaRevisao = !cj.getPauta() && (processoTrf.getExigeRevisor() != null && !processoTrf.getExigeRevisor());
		boolean revisaoSemPauta = !cj.getPauta() && processoTrf.getExigeRevisor() != null && processoTrf.getExigeRevisor();
		boolean revisadoAntecipacao = getInstance().getRevisado() || ojc.getPautaAntecRevisao();
		boolean ojRelator = getInstance().getOrgaoJulgadorColegiado().equals(Authenticator.getOrgaoJulgadorColegiadoAtual());
		boolean relator = RelatorRevisorEnum.REL.equals(ojc.getRelatorRevisor()) && ojRelator;
		boolean revisor = RelatorRevisorEnum.REV.equals(ojc.getRelatorRevisor()) && !ojRelator;

		return (semPautaRevisao && ojRelator)
			|| (revisaoSemPauta && prontoRevisao && ((relator && revisadoAntecipacao) || (revisor && getInstance()
					.getRevisado())));
	}

	public void gravarPauta(){
		instance.setPessoaMarcouPauta(instance.getSelecionadoPauta() ? Authenticator.getPessoaLogada() : null);		
		if (ProcessoTrfHome.instance().getInstance().getRevisado() != null
			&& !ProcessoTrfHome.instance().getInstance().getRevisado()){
			if (ProcessoTrfHome.instance().getInstance().getSessaoSugerida() != null
				&& !ProcessoTrfHome.instance().getInstance().getOrgaoJulgadorColegiado().getRelatorRevisor()
						.equals(RelatorRevisorEnum.REL)){
				ProcessoTrfHome.instance().getInstance().setSessaoSugerida(null);
				ProcessoTrfHome.instance().setExibeBotaoGravar(false);
			}
		}
		super.update();
	}

	public void gravarProntoRevisao(){
		super.update();
	}

	public Boolean getProntoJulgamento(){
		if (prontoJulgamento == null){
			OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
			prontoJulgamento = !instance.getClasseJudicial().getPauta() && instance.getOrgaoJulgador() != null
				&& instance.getOrgaoJulgador().getInstancia().equals("2") && isConcluso()
				&& orgaoJulgadorAtual != null && orgaoJulgadorAtual.equals(instance.getOrgaoJulgador());
		}
		return prontoJulgamento;
	}

	public void gravarJulgamento(){
		instance.setPessoaMarcouJulgamento(instance.getSelecionadoJulgamento() ? Authenticator.getPessoaLogada() : null);
		if (!instance.getSelecionadoJulgamento()){
			setSessao(null);
		}
		super.update();
	}

	public Boolean getRevisado(){
		if (revisado == null){
			setRevisado(true);
		}
		return revisado;
	}

	public void setRevisado(Boolean revisado){
		this.revisado = revisado;
	}

	public void gravarRevisado(){
		if (getRevisado()){
			instance.setOrgaoJulgadorRevisor(Authenticator.getOrgaoJulgadorAtual());
			instance.setPessoaMarcouRevisado(Authenticator.getPessoaLogada());
			if (super.update() != null){
				FacesMessages.instance().clear();
				if (instance.getRevisado()){
					FacesMessages.instance().add(StatusMessage.Severity.INFO, "Processo revisado.");
				}
				else{
					FacesMessages.instance().add(Severity.INFO, "Processo retirado da reviséo.");
				}
			}
			else{
				FacesMessages.instance().add(Severity.ERROR, "Ocorreu um erro ao tentar revisar o processo.");
			}
		}
	}

	public boolean desabilitaSelecionaPauta(){
		if (getInstance().getExigeRevisor() == null
			|| RevisorProcessoTrfHome.instance().getRevisorProcessoTrf(getInstance()) == null
			|| getInstance().getOrgaoJulgadorColegiado().getRelatorRevisor() == null
			|| getInstance().getOrgaoJulgadorColegiado().getPautaAntecRevisao() == null){
			return false;
		}
		if (BooleanUtils.isTrue(getInstance().getExigeRevisor())
			&& RevisorProcessoTrfHome.instance().getRevisorProcessoTrf(getInstance()) != null
			&& getInstance().getOrgaoJulgadorColegiado().getRelatorRevisor().getLabel().equals("REL")
			&& getInstance().getOrgaoJulgadorColegiado().getPautaAntecRevisao()){
			return true;
		}
		return false;
	}

	public void enviarProcesso() throws SQLException{
		if (remessaProcesso == null){
			remessaProcesso = new RemessaProcesso();
		}
		instance().setInstance(
				getEntityManager().find(ProcessoTrf.class, ProcessoHome.instance().getInstance().getIdProcesso()));
		if (!remessaProcesso.verificaConexao()){
			FacesMessages.instance().add(Severity.ERROR, "A conexão se encontra inativa.");
		}
		else{
			try{
				remessaProcesso.migrarDados(instance().getInstance().getIdProcessoTrf());
				segueParaRemetidosOsAutos();
				remessaRecente = Boolean.TRUE;
				FacesMessages.instance().add(Severity.INFO, "Processo enviado com sucesso.");
			} catch (Exception e){
				log.error("Erro ao tentar enviar o processo. " + e.getMessage());
				FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar enviar o processo. " + e.getMessage());
			}
		}
	}

	private void segueParaRemetidosOsAutos(){
		TaskInstanceHome.instance().end(ParametroUtil.instance().getNomeTarefaRemessa2Grau());
	}

	public String getProcesso2Grau(){
		return processo2Grau;
	}

	public void setProcesso2Grau(String processo2Grau){
		this.processo2Grau = processo2Grau;
	}

	public EnderecoWsdl getEnderecoWsdl(){
		return enderecoWsdl;
	}

	public void setEnderecoWsdl(EnderecoWsdl enderecoWsdl){
		this.enderecoWsdl = enderecoWsdl;
	}

	public Usuario getRelator(ProcessoTrf processo){
		return ProcessoJudicialManager.instance().getRelator(processo);
	}

	@Cached(scope = ScopeType.EVENT)
	public String getNomeRelator(){
		Usuario relator = getRelator(getInstance());
		return relator == null ? null : relator.toString();
	}
	
	@Cached(scope = ScopeType.EVENT)
	public String getNomeRevisor() {
		if (getInstance().getOrgaoJulgadorRevisor() != null) {			
			Usuario revisor = ProcessoTrfHome.instance().getRelator(getInstance().getOrgaoJulgadorRevisor(), null);
			return revisor == null ? "" : revisor.getNome();
		} else {
			return "";
		}
	}

	@Deprecated
	public Usuario getRelator(OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado){
		StringBuilder sb = new StringBuilder();
		sb.append("select o.usuarioLocalizacao.usuario from UsuarioLocalizacaoMagistradoServidor o  ");
		sb.append("where o.orgaoJulgadorCargo.recebeDistribuicao = true ");
		sb.append("and o.orgaoJulgadorCargo.orgaoJulgador = :orgaoJulgador ");
		sb.append("and o.magistradoTitular = true ");
		if (orgaoJulgadorColegiado != null){
			sb.append("and o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ");
		}
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("orgaoJulgador", orgaoJulgador);
		if (orgaoJulgadorColegiado != null){
			query.setParameter("orgaoJulgadorColegiado", orgaoJulgadorColegiado);
		}
		return (Usuario) EntityUtil.getSingleResult(query);
	}
	
	@Deprecated
	public String getNomeRelator(ProcessoTrf p){
		if(p.getPessoaRelator() != null){
			return p.getPessoaRelator().getNome();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select o.usuarioLocalizacao.usuario.nome from UsuarioLocalizacaoMagistradoServidor o  ");
		sb.append("where o.orgaoJulgadorCargo.recebeDistribuicao = true ");
		sb.append("and o.orgaoJulgadorCargo.orgaoJulgador = :orgaoJulgador ");
		sb.append("and o.magistradoTitular = true ");
		if (p.getOrgaoJulgadorColegiado() != null){
			sb.append("and o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ");
		}
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("orgaoJulgador", p.getOrgaoJulgador());
		if (p.getOrgaoJulgadorColegiado() != null){
			query.setParameter("orgaoJulgadorColegiado", p.getOrgaoJulgadorColegiado());
		}
		return (String) EntityUtil.getSingleResult(query);
	}

	public String getRevisor(ProcessoTrf obj){
		Criteria criteria = HibernateUtil.getSession().createCriteria(SessaoComposicaoOrdem.class);
		criteria.add(Restrictions.eq("orgaoJulgador.idOrgaoJulgador", obj.getOrgaoJulgador().getIdOrgaoJulgador()));
		criteria.add(Restrictions.eq("sessao.idSessao", SessaoHome.instance().getInstance().getIdSessao()));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		SessaoComposicaoOrdem sessaoComposicaoOrdem = (SessaoComposicaoOrdem)criteria.uniqueResult();
		if (sessaoComposicaoOrdem != null) {
			if(sessaoComposicaoOrdem.getOrgaoJulgadorRevisor() == null){
				return null;
			}
			return sessaoComposicaoOrdem.getOrgaoJulgadorRevisor().toString();
		} else {
			return null;
		}
	}

	public Boolean isRevisor(ProcessoTrf processoTrf){
		if (Authenticator.getOrgaoJulgadorAtual() != null){
			StringBuilder sb = new StringBuilder();
			sb.append("select count(o) from RevisorProcessoTrf o where ");
			sb.append("o.orgaoJulgadorRevisor = :orgaoJulgador and ");
			sb.append("o.processoTrf = :processoTrf and ");
			sb.append("o.dataFinal is null  ");
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("orgaoJulgador", Authenticator.getOrgaoJulgadorAtual());
			q.setParameter("processoTrf", processoTrf);
			try {
				Long retorno = (Long) q.getSingleResult();
				return retorno > 0;
			} catch (NoResultException no) {
				return Boolean.FALSE;
			}
		}
		return Boolean.FALSE;
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaosJulgadoresVinculadosColegiado(){
		String sql = "select o from OrgaoJulgador o "
			+ "where o in (select ojcoj.orgaoJulgador from OrgaoJulgadorColegiadoOrgaoJulgador ojcoj where ojcoj.orgaoJulgadorColegiado = :orgaoJulgadorColegiado)";
		Query q = getEntityManager().createQuery(sql);
		q.setParameter("orgaoJulgadorColegiado", processoTrf.getOrgaoJulgadorColegiado());
		return q.getResultList();
	}

	public List<TipoDistribuicaoEnum> getTipoDistribuicaoEnum(){
		List<TipoDistribuicaoEnum> distribuicaoList = new ArrayList<TipoDistribuicaoEnum>(0);
		for (TipoDistribuicaoEnum tipoDistribuicao : TipoDistribuicaoEnum.values()){
			if (!tipoDistribuicao.equals(TipoDistribuicaoEnum.A) && !tipoDistribuicao.equals(TipoDistribuicaoEnum.I)){
				distribuicaoList.add(tipoDistribuicao);
			}
		}
		return distribuicaoList;
	}
	
	public List<TipoDistribuicaoEnum> getTipoDistribuicaoEnumSorteio(){
		List<TipoDistribuicaoEnum> distribuicaoList = new ArrayList<TipoDistribuicaoEnum>(0);
		distribuicaoList.add(TipoDistribuicaoEnum.S);
		return distribuicaoList;
	}

	public void distribuirProcessante(){
		try{
			Long taskId = BusinessProcess.instance().getTaskId();
			protocolar(true);
			org.jbpm.taskmgmt.exe.TaskInstance ti = (org.jbpm.taskmgmt.exe.TaskInstance) ManagedJbpmContext.instance()
					.getSession().get(org.jbpm.taskmgmt.exe.TaskInstance.class, taskId);
			if(ti.isOpen()) {
				ti.end("Término");
			}
			setAtualiza(true);
			this.textosProtocolacao[INDEX_DISTRIBUICAO] += String.format("\n"
				+ TEXTO_DISTRIBUICAO_AUTOMATICA, getInstance()
					.getNumeroProcesso(), getInstance().getOrgaoJulgador());
		} catch (Exception e){
			try{
				Transaction.instance().setRollbackOnly();
			} catch (Exception e1){
				log.error("Erro ao realizar rollback: " + e1.getMessage());
			}
			FacesMessages.instance().add(Severity.ERROR,
					"Erro ao realizar a Distribuição: \n{0}", e.getMessage());
		}
	}

	public void redistribuir(){
		ProcessoTrfRedistribuicao redistribuicao = ProcessoTrfRedistribuicaoHome.instance().getInstance();
		redistribuicao.setDataRedistribuicao(new Date());
		getEntityManager().merge(redistribuicao);
		getEntityManager().flush();
	}

	public List<Cargo> getCargoDistribuicaoList(){
		List<Cargo> cargoList = new ArrayList<Cargo>();
		if (ProcessoTrfHome.instance().getOrgaoJulgador() != null){
			String hql = "select o.cargo from OrgaoJulgadorCargo o where o.orgaoJulgador = :oj";
			Cargo cargo = EntityUtil.getSingleResult(getEntityManager().createQuery(hql).setParameter("oj",
					ProcessoTrfHome.instance().getOrgaoJulgador()));
			cargoList.add(cargo);
		}
		return cargoList;
	}

	public void distribuirSREEO() throws Exception{
		Fluxo fluxoSREEO = ParametroUtil.instance().getFluxoSREEO();
		OrgaoJulgador orgaoJulgadorSREEO = ParametroUtil.instance().getOrgaoJulgadorSREEO();
		getInstance().setOrgaoJulgador(orgaoJulgadorSREEO);
		getInstance().setOrgaoJulgadorColegiado(ParametroUtil.instance().getOrgaoJulgadorColegiadoSRREO());
		String hql = "select o.cargo from OrgaoJulgadorCargo o where o.orgaoJulgador = :oj";
		Cargo cargo = EntityUtil.getSingleResult(getEntityManager().createQuery(hql).setParameter("oj",
				orgaoJulgadorSREEO));
		getInstance().setCargo(cargo);
		getInstance().setDataDistribuicao(new Date());
		getInstance().getProcesso().setFluxo(fluxoSREEO);
		registraHistoricoDistribuicao(getInstance(), fluxoSREEO);
		getEntityManager().merge(getInstance().getProcesso());
		getEntityManager().merge(getInstance());
		getEntityManager().flush();
		TaskInstance.instance().end("Término");
		inserirProcessoNoFluxo(getInstance().getIdProcessoTrf(), fluxoSREEO);
	}

	public void distribuirProcessanteSemSorteio() throws Exception{
		TaskInstance.instance().end("Término");
		inserirProcessoNoFluxo(getInstance().getIdProcessoTrf(), ParametroUtil.instance().getFluxoProcessante());
	}

	public void redistribuirPorPrevencao(){
		redistribuir(DefinicaoEventos.DISTRIBUICAO_PREVENCAO, false, TipoConexaoEnum.PR);
	}

	public void redistribuirPorDependencia(){
		redistribuir(DefinicaoEventos.DISTRIBUICAO_DEPENDENCIA, true, TipoConexaoEnum.DP);
	}

	public void distribuirPorPrevencao(){
		distribuir(DefinicaoEventos.DISTRIBUICAO_PREVENCAO, false, TipoConexaoEnum.PR);
	}

	public void distribuirPorDependencia(){
		distribuir(DefinicaoEventos.DISTRIBUICAO_DEPENDENCIA, true, TipoConexaoEnum.DP);
	}

	public void distribuirIncidental2Grau(){
		getInstance().setIsIncidente(true);
		getInstance().setDesProcReferencia(numeroProcessoDistriuir);
		getInstance().setOrgaoJulgadorColegiado(orgaoJulgadorColegiado);
		if (orgaoJulgador == null){
			Random random = new Random();
			SelectItemsQuery s = getComponent("orgaoJulgadorItems");
			int aleatorio = random.nextInt(s.getResultList().size());
			orgaoJulgador = (OrgaoJulgador) s.getResultList().get(aleatorio);
			getInstance().setOrgaoJulgador(orgaoJulgador);
		}
		else{
			getInstance().setOrgaoJulgador(orgaoJulgador);
		}
		distribuirProcessante();
	}

	/**
	 * Executa a redistribuição do processo
	 * 
	 * @param nomeEvento é o nome do evento que  lançado
	 * @param isDependencia - indica se a Distribuição é por dependência
	 * @param tipoConexao é o tipo de conexão que o processo irá receber
	 */
	@SuppressWarnings("unchecked")
	private void redistribuir(String nomeEvento, boolean isDependencia, TipoConexaoEnum tipoConexao){
		ProcessoTrf trf = getProcessoDistribuicao(numeroProcessoDistriuir);
		if (trf == null){
			if (orgaoJulgador == null){
				Random random = new Random();
				SelectItemsQuery s = getComponent("orgaoJulgadorItems");
				List<OrgaoJulgador> resultList = s.getResultList();
				if (resultList.size() == 0){
					FacesMessages.instance().add(Severity.ERROR, "Não existem órgão Julgadores disponéveis");
					return;
				}
				int aleatorio = random.nextInt(resultList.size());
				orgaoJulgador = resultList.get(aleatorio);
			}

			if (!ParametroUtil.instance().isPrimeiroGrau()){
				SelectItemsQuery s = getComponent("cargoDistribuicaoItems");
				cargo = (Cargo) s.getResultList().get(0);
			}

		}
		else{
			orgaoJulgador = trf.getOrgaoJulgador();
			orgaoJulgadorColegiado = trf.getOrgaoJulgadorColegiado();
			cargo = trf.getCargo();
		}
		Date dataDistribuicao = new Date();
		try{
			RegistraEventoAction.instance().registraPorNome(nomeEvento, dataDistribuicao);
		} catch (Exception e){
			FacesMessages.instance().add(Severity.ERROR, "Erro ao distribuir por prevenção: " + e.getMessage(), e);
			return;
		}
		TaskInstance.instance().end("Término");
		getInstance().setOrgaoJulgador(orgaoJulgador);
		getInstance().setOrgaoJulgadorColegiado(orgaoJulgadorColegiado);
		getInstance().setCargo(cargo);
		getInstance().setProcessoStatus(ProcessoStatusEnum.D);
		getInstance().setDataDistribuicao(dataDistribuicao);
		if (isDependencia){
			if (verificarProcessoNaBase(numeroProcessoDistriuir) != null){
				processoTrf = verificarProcessoNaBase(numeroProcessoDistriuir);
				getInstance().setJurisdicao(processoTrf.getJurisdicao());
			}
		}
		Fluxo fluxoProcessante = ParametroUtil.instance().getFluxoProcessante();
		registraHistoricoDistribuicao(getInstance(), fluxoProcessante);
		getEntityManager().merge(getInstance());
		getEntityManager().flush();

		Map<String, Object> variaveis = new HashMap<String, Object>();
		variaveis.put("orgaoJulgador", orgaoJulgador.getIdOrgaoJulgador());
		variaveis.put("titularidade", cargo.getSigla());

		try{
			inserirProcessoNoFluxo(getInstance().getIdProcessoTrf(), fluxoProcessante, variaveis);
		} catch (Exception e){
			String msgErro = "Erro ao inciar o fluxo: " + e.getLocalizedMessage();
			FacesMessages.instance().add(Severity.ERROR, msgErro, e);
			throw new AplicationException(AplicationException.createMessage(msgErro,
					isDependencia ? "distribuirPorDependencia()" : "distribuirPorPrevencao()",
					ProcessoTrfHome.class.getName(), "PJE"));
		}

		inserirProcessoConexao(tipoConexao);
		Events.instance().raiseEvent(EVENT_PROCESSO_DISTRIBUIDO, getInstance());
		setAtualiza(true);
	}

	/**
	 * Executa a Distribuição do processo
	 * 
	 * @param nomeEvento é o nome do evento que  lançado
	 * @param isDependencia - indica se a Distribuição é por dependência
	 * @param tipoConexao é o tipo de conexão que o processo iré receber
	 */
	@SuppressWarnings("unchecked")
	private void distribuir(String nomeEvento, boolean isDependencia, TipoConexaoEnum tipoConexao){
		ProcessoTrf trf = getProcessoDistribuicao(numeroProcessoDistriuir);
		if (trf == null){
			if (orgaoJulgador == null){
				Random random = new Random();
				SelectItemsQuery s = getComponent("orgaoJulgadorItems");
				List<OrgaoJulgador> resultList = s.getResultList();
				if (resultList.size() == 0){
					FacesMessages.instance().add(Severity.ERROR, "Não existem órgão Julgadores disponéveis");
					return;
				}
				int aleatorio = random.nextInt(resultList.size());
				orgaoJulgador = resultList.get(aleatorio);
			}

			if (!ParametroUtil.instance().isPrimeiroGrau()){
				SelectItemsQuery s = getComponent("cargoDistribuicaoItems");
				cargo = (Cargo) s.getResultList().get(0);
			}

		}
		else{
			orgaoJulgador = trf.getOrgaoJulgador();
			orgaoJulgadorColegiado = trf.getOrgaoJulgadorColegiado();
			cargo = trf.getCargo();
		}

		boolean isRedistribuicao = instance.getOrgaoJulgador() != null;

		Date dataDistribuicao = new Date();

		try{
			String codElementoComplemento = isDependencia ? "4" : "3";
			
			if (!isRedistribuicao){

				// Código = 26 - Descrição = Distribuído por #{tipo_de_distribuicao_redistribuicao}
				// **************************************************************************************
				MovimentoAutomaticoService.preencherMovimento().deCodigo(CodigoMovimentoNacional.COD_MOVIMENTO_DISTRIBUICAO_DISTRIBUIDO)
				  						  .associarAoProcesso(getInstance().getProcesso())
				  						  .comProximoComplementoVazio().doTipoDominio().preencherComElementoDeCodigo(codElementoComplemento)
				  						  .lancarMovimento();
			}else{
				
				// Código = 36 - Descrição = Redistribuído por #{tipo_de_distribuicao_redistribuicao} #{motivo_da_redistribuicao}
				// **************************************************************************************
				MovimentoAutomaticoService.preencherMovimento().deCodigo(CodigoMovimentoNacional.COD_MOVIMENTO_REDISTRIBUICAO)
										  .associarAoProcesso(getInstance().getProcesso())
										  .comProximoComplementoVazio().doTipoDominio().preencherComElementoDeCodigo(codElementoComplemento)
										  .lancarMovimento();
			}
			
			this.textosProtocolacao[INDEX_DISTRIBUICAO] += String.format("\n"
					+ TEXTO_DISTRIBUICAO_AUTOMATICA, getInstance()
						.getNumeroProcesso(), orgaoJulgador);

		} catch (Exception e){
			FacesMessages.instance().add(Severity.ERROR, "Erro ao distribuir por prevenção: " + e.getMessage(), e);
			return;
		}

		TaskInstance.instance().end("Término");

		int x = 0;
		List<OrgaoJulgadorCargo> ojcList = orgaoJulgador.getOrgaoJulgadorCargoList();
		if (ojcList.size() > 1){
			Random random = new Random();
			x = random.nextInt(ojcList.size() - 1);
		}

		getInstance().setOrgaoJulgador(orgaoJulgador);
		getInstance().setOrgaoJulgadorColegiado(orgaoJulgadorColegiado);
		getInstance().setOrgaoJulgadorCargo(ojcList.get(x));
		getInstance().setCargo(cargo);
		getInstance().setProcessoStatus(ProcessoStatusEnum.D);
		getInstance().setDataDistribuicao(dataDistribuicao);
		if (isDependencia){
			if (verificarProcessoNaBase(numeroProcessoDistriuir) != null){
				processoTrf = verificarProcessoNaBase(numeroProcessoDistriuir);
				getInstance().setJurisdicao(processoTrf.getJurisdicao());
			}
		}
		Fluxo fluxoProcessante = getInstance().getClasseJudicial().getFluxo();
		registraHistoricoDistribuicao(getInstance(), fluxoProcessante);
		getEntityManager().merge(getInstance());
		getEntityManager().flush();

		Map<String, Object> variaveis = new HashMap<String, Object>();
		variaveis.put("orgaoJulgador", orgaoJulgador.getIdOrgaoJulgador());
		variaveis.put("titularidade", cargo.getSigla());

		try{
			inserirProcessoNoFluxo(getInstance().getIdProcessoTrf(), fluxoProcessante, variaveis);
		} catch (Exception e){
			String msgErro = "Erro ao inciar o fluxo: " + e.getLocalizedMessage();
			FacesMessages.instance().add(Severity.ERROR, msgErro, e);
			throw new AplicationException(AplicationException.createMessage(msgErro,
					isDependencia ? "distribuirPorDependencia()" : "distribuirPorPrevencao()",
					ProcessoTrfHome.class.getName(), "PJE"));
		}

		inserirProcessoConexao(tipoConexao);
		Events.instance().raiseEvent(EVENT_PROCESSO_DISTRIBUIDO, getInstance());
		setAtualiza(true);
	}

	private ProcessoTrf verificarProcessoNaBase(String processo){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o ");
		sb.append("where o.processo.numeroProcesso = :numeroProcesso ");
		Query q = getEntityManager().createQuery(sb.toString()).setParameter("numeroProcesso", NumeroProcessoUtil.mascaraNumeroProcesso(processo));
		return EntityUtil.getSingleResult(q);
	}

	private void inserirProcessoConexao(TipoConexaoEnum tipo){
		ProcessoTrfConexao conexao = new ProcessoTrfConexao();
		ProcessoTrf processoBase = verificarProcessoNaBase(numeroProcessoDistriuir);
		if (processoBase != null){
			conexao.setProcessoTrfConexo(processoBase);
			conexao.setOrgaoJulgador(processoBase.getOrgaoJulgador().getOrgaoJulgador());
			conexao.setSessaoJudiciaria(ParametroUtil.getFromContext(Parametros.NOME_SECAO_JUDICIARIA, true));
			conexao.setClasseJudicial(processoBase.getClasseJudicialStr());
			conexao.setLinkSessaoJudiciaria(ParametroUtil.getFromContext("dslinkprevencao", true)
				+ processoBase.getIdProcessoTrf());

		}
		else{
			conexao.setNumeroProcesso(numeroProcessoDistriuir);
		}
		conexao.setProcessoTrf(getInstance());
		conexao.setTipoConexao(tipo);
		conexao.setAtivo(true);
		getEntityManager().persist(conexao);
		EntityUtil.flush();
	}

	private ProcessoTrf getProcessoDistribuicao(String numeroProcesso){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o ");
		sb.append("where o.processo.numeroProcesso = :numeroProcesso ");
		sb.append("and o.processoStatus = 'D'");
		Query q = getEntityManager().createQuery(sb.toString()).setParameter("numeroProcesso", numeroProcesso);
		return EntityUtil.getSingleResult(q);
	}

	@SuppressWarnings("unchecked")
	/**
	 * Metodo que retorna o processo do Fluxo SREEO para o Fluxo de Origem
	 */
	public void distribuirDoSREEOParaFluxoOrigem() throws Exception{
		String hql = "select o from HistoricoDistribuicao o where o.processoTrf = :processoTrf order by o.dataDistribuicao desc";

		// Para retornar o processo onde ele estava antes é preciso pegar o
		// penultimo registro no historico de Distribuição
		Query query = getEntityManager().createQuery(hql).setParameter("processoTrf", getInstance());
		List<HistoricoDistribuicao> list = query.setMaxResults(2).getResultList();
		if (list.size() > 1){
			HistoricoDistribuicao hd = list.get(1);
			getInstance().setOrgaoJulgador(hd.getOrgaoJulgador());
			getInstance().setOrgaoJulgadorColegiado(hd.getOrgaoJulgadorColegiado());
			getInstance().setCargo(hd.getCargo());
			Fluxo fluxo = hd.getFluxo();
			getInstance().getProcesso().setFluxo(fluxo);
			Date dataDistribuicao = new Date();
			getInstance().setDataDistribuicao(dataDistribuicao);
			registraHistoricoDistribuicao(getInstance(), fluxo);
			getEntityManager().merge(getInstance().getProcesso());
			getEntityManager().merge(getInstance());
			getEntityManager().flush();
			TaskInstance.instance().end("Término");
			inserirProcessoNoFluxo(getInstance().getIdProcessoTrf(), fluxo);
		}
		else{
			throw new Exception("Sé foi encontrado um histérico de Distribuição");
		}

	}

	public Competencia getCompEscolhida(){
		return compEscolhida;
	}

	public void setCompEscolhida(Competencia compEscolhida){
		this.compEscolhida = compEscolhida;
	}

	public TipoDistribuicaoEnum getTipoDistribuicao(){
		return tipoDistribuicao;
	}

	public void setTipoDistribuicao(TipoDistribuicaoEnum tipoDistribuicao){
		this.tipoDistribuicao = tipoDistribuicao;
	}

	public ProcessoTrf getProcessoTrf(){
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf){
		this.processoTrf = processoTrf;
	}

	public Boolean getAtualiza(){
		return atualiza;
	}

	public void setAtualiza(Boolean atualiza){
		this.atualiza = atualiza;
	}

	public void setProntoJulgamento(Boolean prontoJulgamento){
		this.prontoJulgamento = prontoJulgamento;
	}

	/**
	 * Metodo que testa se o processo possui algum evento do agrupamento, mas levando em consideração um evento excludente que vai cancelar o evento
	 * testado caso o evento excludenet tenha sido lançado depois.
	 * 
	 * @param processoTrf
	 * @param agrupamento
	 * @param agrupamentoExcludente
	 * @return
	 */
	public boolean possuiEventoTestandoExcludente(Processo processo, String agrupamento, String agrupamentoExcludente){
		ProcessoHome pHome = ProcessoHome.instance();
		ProcessoEvento ultimoEvento = pHome.getUltimoProcessoEvento(processo, agrupamento);
		if (ultimoEvento != null){
			ProcessoEvento ultimoEventoExcludente = pHome.getUltimoProcessoEvento(processo, agrupamentoExcludente);
			Date dtEvento = ultimoEvento.getDataAtualizacao();
			return ultimoEventoExcludente == null || dtEvento.after(ultimoEventoExcludente.getDataAtualizacao());
		}
		else{
			return false;
		}
	}

	public boolean possuiEventoTestandoExcludente(String agrupamento, String agrupamentoExcludente){
		Processo processo = null;
		if (isManaged()){
			processo = getInstance().getProcesso();
		}
		else{
			processo = ProcessoHome.instance().getInstance();
		}
		return possuiEventoTestandoExcludente(processo, agrupamento, agrupamentoExcludente);
	}

	public List<ProcessoTrf> getListaProcesso(){
		return listaProcesso;
	}

	public void setListaProcesso(List<ProcessoTrf> listaProcesso){
		this.listaProcesso = listaProcesso;
	}

	public void criarLista(ProcessoTrf obj){
		if (obj.getCheck()){
			listaProcesso.add(obj);
		}
		else{
			listaProcesso.remove(obj);
		}
	}

	public void limparLista(){
		for (ProcessoTrf processoTrf : getListaProcesso()){
			processoTrf.setCheck(Boolean.FALSE);
		}
		getListaProcesso().clear();
	}

	public void incluirProcessos(){
		Sessao sessao = SessaoHome.instance().getInstance();
		if (sessao.getDataRealizacaoSessao() != null) {
			FacesMessages.instance().add(Severity.ERROR, "A sessão foi realizada. Não é possével efetuar a inclusão de processos.");
			return;
		}
		List<String> listMsg = new ArrayList<>();
		boolean exibirMsgSucesso = false;
		SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.NAME);
		for (ProcessoTrf processoTrf : listaProcesso) {

			if(ComponentUtil.getComponent(SessaoPautaProcessoTrfHome.class).verificaProcessoRelacao(processoTrf,sessao,true)){
				FacesMessages.instance().add(Severity.ERROR,String.format("O processo %s jé está incluso na Relação de Julgamento.",processoTrf.getProcesso().getNumeroProcesso()));
			}else{
				if ((BooleanUtils.isTrue(processoTrf.getExigeRevisor()) ||
						SimNaoFacultativoEnum.S.equals(processoTrf.getClasseJudicial().getExigeRevisor())) &&
						processoTrf.getOrgaoJulgadorRevisor() == null) {

					listMsg.add(String.format(" - %s: %s", processoTrf.getProcesso().getNumeroProcesso(),
							Messages.instance().get("sessaoPautaProcessoTrf.erro.processoSemRevisor")));

				} else if ((BooleanUtils.isTrue(processoTrf.getExigeRevisor()) && processoTrf.getRevisado()) ||
						!BooleanUtils.isTrue(processoTrf.getExigeRevisor())) {

					atualizaSessaoProcessoDocumentos(processoTrf, sessao);
					try {
						sessaoPautaProcessoTrfManager.pautarProcesso(sessao, processoTrf);
						exibirMsgSucesso = true;
					}
					catch (Exception e) {
						FacesMessages.instance().add(Severity.ERROR, "Não foi possével incluir o processo, mensagem interna: " + e.getMessage());
					}
				} else {
					listMsg.add(" - " + processoTrf.getProcesso().getNumeroProcesso());
				}
			}

		}
		limparLista();
		setMarcouListTudo(Boolean.FALSE);
			if (listMsg.size() > 0){
				FacesMessages.instance().add(Severity.ERROR,
						"Os seguintes processos Não foram revisados. Os processos Não foram incluédos:");
				for (String string : listMsg){
					FacesMessages.instance().add(Severity.ERROR, string);
				}
			}else{
				if(exibirMsgSucesso){
					FacesMessages.instance().add(Severity.INFO,
							"Processo(s) incluédo(s) em pauta com sucesso.");
				}

			}
	}

	@SuppressWarnings("unchecked")
	public void atualizaSessaoProcessoDocumentos(ProcessoTrf processoTrf2, Sessao sess) {
		String hql = "SELECT spd FROM SessaoProcessoDocumento AS spd " +
				"		WHERE spd.sessao IS NULL " +
				"			AND spd.processoDocumento IN (" +
				"				SELECT pd FROM ProcessoDocumento AS pd WHERE pd.processo = :processo AND pd.ativo = true" +
				"			)";
		List<SessaoProcessoDocumento> docs = EntityUtil.getEntityManager().createQuery(hql)
				.setParameter("processo", processoTrf2.getProcesso()).getResultList();
		for(SessaoProcessoDocumento spd: docs){
			spd.setSessao(sess);
		}
	}

	public void marcarTudo(){
		if (getMarcouListTudo()){
			listaProcesso = new ArrayList<ProcessoTrf>();
			List<ConsultaProcessoTrfSemFiltro> listaConsulta = new ArrayList<ConsultaProcessoTrfSemFiltro>(0);
			PautaJulgamentoList lista = ComponentUtil.getComponent(PautaJulgamentoList.NAME);
			lista.setMaxResults(null);
			listaConsulta = lista.getResultList();
			for(ConsultaProcessoTrfSemFiltro c : listaConsulta){
				listaProcesso.add(c.getProcessoTrf());
			}
			setMarcouListTudo(Boolean.TRUE);
			for (ProcessoTrf processoTrf : listaProcesso){
				processoTrf.setCheck(Boolean.TRUE);
			}
		}
		else{
			setMarcouListTudo(Boolean.FALSE);
			for (ProcessoTrf processoTrf : listaProcesso){
				processoTrf.setCheck(Boolean.FALSE);
			}
			limparLista();
		}
	}

	public String getNumeroProcessoDistriuir(){
		return numeroProcessoDistriuir;
	}

	public void setNumeroProcessoDistriuir(String numeroProcessoDistriuir){
		this.numeroProcessoDistriuir = numeroProcessoDistriuir;
	}

	public Boolean getMarcouListTudo(){
		return marcouListTudo;
	}

	public void setMarcouListTudo(Boolean marcouListTudo){
		this.marcouListTudo = marcouListTudo;
	}

	public boolean beforeCheckVisibilidadeProcesso(){
		FacesContext context = FacesContext.getCurrentInstance();
		SecurityTokenControler stc = ComponentUtil.getComponent(SecurityTokenControler.NAME);
		boolean isPodeVisualizar = false;

		if(Boolean.FALSE.equals(caValido) && stc.verificaChaveAcesso() != null){
			ca = context.getExternalContext().getRequestParameterMap().get("ca");
			caValido = Boolean.TRUE;
			setId(stc.verificaChaveAcesso());
		}
		
		if(Boolean.TRUE.equals(caValido) && stc.verificaChaveAcesso(ca).equals(recuperarParamProcesso())) {
			ProcessoTrfDAO procTrfDAO = ComponentUtil.getComponent(ProcessoTrfDAO.class);
			ProcessoTrf procTrfAcessado = procTrfDAO.find(ProcessoTrf.class, recuperarParamProcesso());
	
			if(Boolean.FALSE.equals(procTrfAcessado.getSegredoJustica()) || this.fazParteDoProcesso(procTrfAcessado) || Boolean.TRUE.equals(permiteAbrirTarefa())) {
				ProcessoTrf proc = null;
				try {
					proc = ProcessoJudicialManager.instance().recuperarProcesso(
						procTrfAcessado.getIdProcessoTrf(),
						(Identity) Component.getInstance("org.jboss.seam.security.identity"),
						Authenticator.getPessoaLogada(), Authenticator.getUsuarioLocalizacaoAtual()
					);
					isPodeVisualizar = (proc != null);
	
					if(isPodeVisualizar) {
						LogAcessoAutosDownloadsService logAutos = ComponentUtil.getComponent(LogAcessoAutosDownloadsService.class);
						logAutos.logarAcessoAosAutos(proc);	
					}
	
				} catch (PJeBusinessException e) {
					log.error("Erro ao recuperar o processo (beforeCheckVisibilidadeProcesso). " + e.getMessage());
				} 
			}
			Contexts.getSessionContext().set(Authenticator.TEM_VISIBILIDADE, isPodeVisualizar);
		}

		return isPodeVisualizar;
	}

	private boolean fazParteDoProcesso(ProcessoTrf procTrfAcessado) {
		ProcessoVisibilidadeSegredoManager pvsManager = ComponentUtil.getComponent(ProcessoVisibilidadeSegredoManager.class);
		boolean fazParteDoProcesso = false;
		
		fazParteDoProcesso = Authenticator.isPapelOficialJustica() || Authenticator.isPapelOficialJusticaDistribuidor();
		fazParteDoProcesso = fazParteDoProcesso || procTrfAcessado.getProcessoParteList().stream().anyMatch(p -> p.getIsAtivo() && p.getIdPessoa().equals(Authenticator.getIdUsuarioLogado()));
		fazParteDoProcesso = fazParteDoProcesso || 
			procTrfAcessado
				.getProcessoParteList()
				.stream().anyMatch(p -> p.getIsAtivo() && (
					(p.getProcuradoria() != null && Authenticator.getProcuradoriaAtualUsuarioLogado() != null) && 
					p.getProcuradoria().getIdProcuradoria() == Authenticator.getProcuradoriaAtualUsuarioLogado().getIdProcuradoria())
				);
		fazParteDoProcesso = fazParteDoProcesso || pvsManager.visivel(procTrfAcessado, Authenticator.getPessoaLogada(), Authenticator.getProcuradoriaAtualUsuarioLogado());
		return fazParteDoProcesso;
	}
	
	private Integer recuperarParamProcesso(){
		FacesContext context = FacesContext.getCurrentInstance();
		
		Integer retorno = 0;
		
		String id = context.getExternalContext().getRequestParameterMap().get("id");
		
		if(id == null){
			id = context.getExternalContext().getRequestParameterMap().get("idProcesso");
			if(id == null){
				id = context.getExternalContext().getRequestParameterMap().get("idProcessoTrf");
			}
		}
		
		if(id != null){
			try{
				retorno = Integer.parseInt(id);
			} catch(NumberFormatException nfe){
				log.warn("Não foi possével recuperar parâmetro processo pelo id " + id + ": " + nfe.getLocalizedMessage());
			}
		} else {
			retorno = getProcessoTrfIdProcessoTrf();
		}
		
		return retorno;
	}
	
	public Boolean permiteAbrirTarefa(){
		if(cachePermiteVisualizarProcesso){
			return Boolean.TRUE;
		} else {
			SituacaoProcessoManager spm = ComponentUtil.getComponent(SituacaoProcessoManager.NAME);
			List<SituacaoProcesso> listaTarefas = null;			
			List<Integer> idsLocalizacoesFilhasAtuaisList = Authenticator.getIdsLocalizacoesFilhasAtuaisList();
			Integer idOrgaoJulgadorColegiadoAtual = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
			boolean isServidorExclusivoColegiado = Authenticator.isServidorExclusivoColegiado();
			boolean isVisualizaSigiloso = Authenticator.isVisualizaSigiloso();

			if (idTaskInstance == null){
				listaTarefas = spm.getByProcessoSemFiltros(getProcessoTrfIdProcessoTrf());
				for(SituacaoProcesso tarefa : listaTarefas) {
					if(spm.getByIdTaskInstanceLocalizacoes(tarefa.getIdTaskInstance(), idsLocalizacoesFilhasAtuaisList, 
						idOrgaoJulgadorColegiadoAtual,isServidorExclusivoColegiado, isVisualizaSigiloso) != null){
						cachePermiteVisualizarProcesso = Boolean.TRUE;
						return cachePermiteVisualizarProcesso;
					}
				}
			} else {
				if(spm.getByIdTaskInstanceLocalizacoes(idTaskInstance, idsLocalizacoesFilhasAtuaisList, 
					idOrgaoJulgadorColegiadoAtual,isServidorExclusivoColegiado, isVisualizaSigiloso) != null){
					cachePermiteVisualizarProcesso = Boolean.TRUE;
					return cachePermiteVisualizarProcesso;
				}
			}
		}
		return Boolean.FALSE;
	}	

	public boolean checkVisibilidadeProcesso(Processo processo){
		ProcessoHome.instance().setId(processo.getIdProcesso());
		return checkVisibilidadeProcesso();
	}
	
	public boolean checkVisibilidadeProcesso(){
		return checkVisibilidadeProcesso(false,null);
	}

	/**
	 * Metodo exclusivo da funcionalidade Peticionar, localizada em >Processo>Outras opções>Peticionar
	 * que verifica se o usuário teré acesso ao processo
	 * 
	 * @return
	 * 	true se o usuário terá acesso ao processo
	 */
	public boolean checkVisibilidadeProcessoPeticionar(){
		peticionar = true;
		return checkVisibilidadeProcesso(false,null);
	}
	
	public boolean checkVisibilidadeProcesso(boolean desabilitarFiltros, Integer idSessao){
		if(!getInstance().getSegredoJustica()){
			return true;
		}
		Integer id = getInstance().getIdProcessoTrf();
		ProcessoTrf proc = null;
		boolean ret = false;
		try {
			if(getInstance().getSegredoJustica()){
				ProcessoParteExpedienteManager ppem = (ProcessoParteExpedienteManager) Component.getInstance(ProcessoParteExpedienteManager.class);
				ret = ppem.temExpediente(Authenticator.getUsuarioLogado(),id);
			}
			if(ret){
				return ret;
			}else{
				if(idSessao == null) {
					proc = ProcessoJudicialManager.instance().recuperarProcesso(id, 
							(Identity) Component.getInstance("org.jboss.seam.security.identity"), 
							Authenticator.getPessoaLogada(), Authenticator.getUsuarioLocalizacaoAtual());
				} else {
					if(Authenticator.isPapelPermissaoSecretarioSessao()){
						permissaoSessao = Boolean.TRUE;
						return true;
					} else {
						proc = ProcessoJudicialManager.instance().recuperaProcessoPautaSessaoPessoa(idSessao, getInstance());
						if(proc != null){
							permissaoSessao = Boolean.TRUE;
						}
					}
				}
			}
		} catch(Throwable t) {
			// swallow
		}
		if(ret || proc != null || (proc == null && peticionar)){
			return true;
		}else{
			return checkVisibilidadeProcessoPorFiltro(desabilitarFiltros);
		}
	}

	private boolean checkVisibilidadeProcessoPorFiltro(boolean desabilitarFiltros) {
		Integer id = null;
		FullTextHibernateSessionProxy session = (FullTextHibernateSessionProxy) HibernateUtil.getSession();
		Set<String> enabledFilters = new HashSet<String>(HibernateUtil.getEnabledFilters(session).keySet());
		String numeroProcesso = null;
		if (!isManaged()){
			id = ProcessoHome.instance().getInstance().getIdProcesso();
			numeroProcesso = ProcessoHome.instance().getInstance().getNumeroProcesso();
		}
		else{
			id = getInstance().getIdProcessoTrf();
			numeroProcesso = getInstance().getNumeroProcesso();
		}

		if(!desabilitarFiltros){
			ControleFiltros.instance().iniciarFiltro();
		}
		else{
			//desativar os filtros habilitados
			if(enabledFilters != null && !enabledFilters.isEmpty()){
				for(String filterName : enabledFilters){
					ControleFiltros.instance().desabilitarFiltro(filterName);
				}
			}
		}
		
		Query query = getEntityManager().createQuery(
				"select o.idProcessoTrf from ProcessoTrf o where o.idProcessoTrf = :id");
		query.setParameter("id", id);
		Object result = EntityUtil.getSingleResult(query);
		boolean check = result != null;
		if (!check && !permissaoSessao){
			FacesMessages.instance().add(Severity.ERROR, "Sem permissão para acessar o processo: " + numeroProcesso);
		}
		
		return check;
	}

	public boolean checkVisibilidadeProcessoFluxo(){
		Integer id = null;
		String numeroProcesso = null;
		if (!isManaged()){
			id = ProcessoHome.instance().getInstance().getIdProcesso();
			numeroProcesso = ProcessoHome.instance().getInstance().getNumeroProcesso();
		}
		else{
			id = getInstance().getIdProcessoTrf();
			numeroProcesso = getInstance().getNumeroProcesso();
		}

		boolean check = checkVisibilidadeProcessoFluxo(id);
		if (!check){
			FacesMessages.instance().add(Severity.ERROR, "Sem permissão para acessar o processo: " + numeroProcesso);
		}
		return check;
	}

	public boolean checkVisibilidadeProcessoFluxo(Integer idProcesso){
		if (idProcesso == null){
			return true;
		}
		ControleFiltros.instance().iniciarFiltro();
		Query query = getEntityManager().createQuery(
				"select o.idProcesso from SituacaoProcesso o where o.idProcesso = :id");
		query.setParameter("id", idProcesso);
		Object result = EntityUtil.getSingleResult(query);
		return result != null;
	}

	@SuppressWarnings("unchecked")
	public String verificarPrevencaoRetornaProcesso(ProcessoTrfConexao ptc){
		String ret = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrfConexao o where o.processoTrf = :processoTrf ");
		if (ptc.getProcessoTrfConexo() != null){
			sb.append("and o.processoTrfConexo = :processoConexo ");
		}
		if (ptc.getNumeroProcesso() != null){
			sb.append("and o.numeroProcesso = :conexaoNumeroProcesso ");
		}

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoTrf", ptc.getProcessoTrf());
		if (ptc.getProcessoTrfConexo() != null){
			q.setParameter("processoConexo", ptc.getProcessoTrfConexo());
		}
		if (ptc.getNumeroProcesso() != null){
			q.setParameter("conexaoNumeroProcesso", ptc.getNumeroProcesso());
		}
		q.setMaxResults(1);
		List<ProcessoTrfConexao> result = q.getResultList();
		if (result != null && !result.isEmpty()){
			ProcessoTrfConexao processo = result.get(0);
			if (processo.getProcessoTrfConexo() != null){
				ret = processo.getProcessoTrfConexo().getProcesso().getNumeroProcesso();
			}
			else{
				ret = processo.getNumeroProcesso();
			}
		}
		return ret;
	}

	public void limparModalConfirmarProcessoReferencia(){
		this.localizarProcessoReferencia = false;
		setarProcessoOriginario(null);
	}

	public Integer getJurisdicao(){
		if (getInstance().getJurisdicao() != null){
			return getInstance().getJurisdicao().getIdJurisdicao();
		}
		else{
			return 0;
		}
	}

	private boolean verificarAssunto() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from CompetenciaClasseAssunto o where ");
		sb.append("o.classeAplicacao.classeJudicial.idClasseJudicial = :idClasseJudicial and ");
		sb.append("o.assuntoTrf.idAssuntoTrf = :idAssunto and ");
		sb.append("o.dataInicio <= current_date and (o.dataFim is null or o.dataFim >= current_date)");		
		
		for (AssuntoTrf assuntoTrf : getInstance().getAssuntoTrfList()) {
			Query query = getEntityManager().createQuery(sb.toString());
			query.setParameter("idClasseJudicial", getInstance().getClasseJudicial().getIdClasseJudicial());
			query.setParameter("idAssunto", assuntoTrf.getIdAssuntoTrf());
		
			if (query.getResultList().size() == 0) {					
				return true;
			}
		}
		return false;
				}
	
	/**
	 * Verifica se houve mudança nos dados do processo (classe/assunto) para a retirada da cadeia 260.
	 * 
	 * @return
	 */
	private boolean verificarPrevencaoArt260() {
		try {
			return DistribuicaoService.instance().verificarPrevencaoArt260(getInstance());
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(
					StatusMessage.Severity.ERROR, "Erro ao aplicar consulta da prevenção Art. 206 CE.");
			return false;
			}
		}

	/**
	 * Verifica incompatibilidades na alteração de classe Judicial.
	 * As seguintes incompatibilidades serão analizadas:
	 * <ul>
	 * 		<li>Assuntos Não existentes na nova classe</li>
	 * 		<li>prevenção do art. 260 do C.E.</li>	
	 * </ul>
			 */
	public void verificarIncompatibilidades() {
		this.mudaClasse = false;
		
		if (!getInstance().getClasseJudicial().equals(this.classeJudicialAnterior)) {
			this.mudaClasse = true;

			if (verificarAssunto()) {
				this.incompatibilidades |= INCOMPATIBILIDADE_ASSUNTO;
			}

			if (ParametroJtUtil.instance().justicaEleitoral()) {
				if (verificarPrevencaoArt260()) {
					this.incompatibilidades |= INCOMPATIBILIDADE_PREVENCAO_ART_260;
				}
			}
		}
	}

	/**
	 * Prepara a mensagem de alerta que será exibida ao usuário caso haja incompatibilidades na alteração de classe Judicial.
	 */
	public void prepararMensagemAlerta() {
		if (this.incompatibilidades == 0) {
			this.mensagemAlerta = StringUtils.EMPTY;  // Nenhuma imcompatibilidade.
			return;
				}
		
		String bundle = "entity_messages";
		StringBuilder sb = new StringBuilder();
		if (incompatibilidades == 3) { // Adiciona uma pré mensagem. O número 3 indica a presença de mais de uma incompatibilidade.
			sb.append(FacesUtil.getMessage(
					bundle, "pje.alteracaoClasseJudicial.preMesagem"));
			}
		if ((incompatibilidades & INCOMPATIBILIDADE_ASSUNTO) == INCOMPATIBILIDADE_ASSUNTO) {
			sb.append(FacesUtil.getMessage(
					bundle, "pje.alteracaoClasseJudicial.incompatibilidade.assunto"));
		}
		if ((incompatibilidades & INCOMPATIBILIDADE_PREVENCAO_ART_260) == INCOMPATIBILIDADE_PREVENCAO_ART_260) {
			sb.append(FacesUtil.getMessage(
					bundle, "pje.alteracaoClasseJudicial.incompatibilidade.prevencaoArt260"));
	}

		this.mensagemAlerta = sb.append(FacesUtil.getMessage(
				bundle, "pje.alteracaoClasseJudicial.pergunta")).toString();
						}

	/**
	 * Modifica a taxonomia (TipoParte) das partes principais (polo ativo e passivo) do processo quando da mudança de classe judicial.
	 * 
	 */
	private void alterarTipoParte() {
		ClasseJudicial classeJudicial = getInstance().getClasseJudicial();
		if (getClasseJudicialAnterior() != null && !getInstance().getClasseJudicial().equals(getClasseJudicialAnterior())){
			for (ProcessoParte processoParte : getInstance().getProcessoParteList()){
				if (processoParte.getPartePrincipal() && 
						!processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.T)) {
					
					if (processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.A)){  // Polo ativo
						processoParte.setTipoParte(ComponentUtil.getComponent(TipoParteManager.class).tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.A));
					} else {  // Polo passivo
						processoParte.setTipoParte(ComponentUtil.getComponent(TipoParteManager.class).tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.P));
					}
					getEntityManager().merge(processoParte);
						EntityUtil.flush();
					}
				}
			}
		}

	public Boolean getMudaClasse(){
		return mudaClasse;
	}

	public void setMudaClasse(Boolean mudaClasse){
		this.mudaClasse = mudaClasse;
	}

	public ClasseJudicial getClasseJudicialAnterior(){
		return classeJudicialAnterior;
	}

	public void setClasseJudicialAnterior(ClasseJudicial classeJudicialAnterior){
		this.classeJudicialAnterior = classeJudicialAnterior;
	}

	public Boolean getRetificacao(){
		return retificacao;
	}

	public void setRetificacao(Boolean retificacao){
		this.retificacao = retificacao;
	}

	public List<Date> getDatasSessaoJulgamento(){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from CompetenciaClasseAssunto o where ");
		sb.append("o.classeAplicacao.classeJudicial.idClasseJudicial = :classeJudicial and ");
		sb.append("o.assuntoTrf.idAssuntoTrf = :assunto and ");
		sb.append("o.dataInicio <= current_date and ");
		sb.append("(o.dataFim is null or o.dataFim >= current_date)");
		Query q = getEntityManager().createQuery(sb.toString()).setParameter("classeJudicial",
				getInstance().getClasseJudicial().getIdClasseJudicial());
		if (q.getResultList().size() == 0){
			mudaClasse = Boolean.TRUE;
		}
		return null;
	}

	public Boolean testaDocAssinado(){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from SessaoProcessoDocumento o where ");
		sb.append("o.processoDocumento.tipoProcessoDocumento = :relatorio and ");
		sb.append("o.processoDocumento.processo.idProcesso = :processo and ");
		sb.append("o.sessao is null and ");
		sb.append("o.tipoInclusao = 'A' and ");
		sb.append("o.processoDocumento.ativo = true and ");
		sb.append("exists ( ");
		sb.append("select pdbpa from ProcessoDocumentoBinPessoaAssinatura pdbpa ");
		sb.append("where pdbpa.processoDocumentoBin = o.processoDocumento.processoDocumentoBin) ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("relatorio", ParametroUtil.instance().getTipoProcessoDocumentoRelatorio());
		q.setParameter("processo", getInstance().getProcesso().getIdProcesso());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	public boolean renderizaComboSessaoSugerida(){
		boolean prontoRevisao = getInstance().getProntoRevisao() == null ? false : getInstance().getProntoRevisao();
		return (getInstance().getExigeRevisor() != null && getInstance().getSelecionadoPauta() != null && getInstance()
				.getOrgaoJulgador() != null)
			&& (getInstance().getExigeRevisor() && prontoRevisao)
			|| (getInstance().getExigeRevisor() != null && !getInstance().getExigeRevisor() && (getInstance().getSelecionadoJulgamento() || getInstance()
					.getSelecionadoPauta()))
			&& getInstance().getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual());
	}

	/**
	 * Metodo que retorna uma lista das sessões que estão em andamento a partir de um órgão julgador e um órgão julgador colegiado
	 * 
	 * @param OJC
	 * @param OJ
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Sessao> getSessaoJulgamento(OrgaoJulgadorColegiado ojc, OrgaoJulgador oj){
		StringBuilder sb = new StringBuilder();
		sb.append("select o.sessao from SessaoComposicaoOrdem o ");
		sb.append("where o.sessao.dataAberturaSessao = null ");
		sb.append("and o.sessao.dataSessao >= current_date ");
		sb.append("and o.sessao.orgaoJulgadorColegiado = :OJC ");
		sb.append("and o.orgaoJulgador = :OJ ");
		sb.append("and o.sessao.dataRealizacaoSessao is null ");
		sb.append("and (o.sessao.dataFechamentoPauta >= current_date or o.sessao.dataFechamentoPauta is null) ");
		sb.append("order by o.sessao.dataSessao ");

		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("OJC", ojc);
		query.setParameter("OJ", oj);
		List<Sessao> sessaoTemp = null;
		sessaoTemp = query.getResultList();
		return sessaoTemp;
	}

	/**
	 * Metodo que retorna uma lista das sessões que estão em andamento a partir de um órgão julgador e um órgão julgador colegiado
	 * 
	 * @param OJC
	 * @param OJ
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Sessao> getSessoesEmAndamento(OrgaoJulgadorColegiado ojc, OrgaoJulgador oj){
		StringBuilder sb = new StringBuilder();
		sb.append("select o.sessao from SessaoComposicaoOrdem o ");
		sb.append("where o.sessao.dataAberturaSessao = null ");
		sb.append("and o.sessao.orgaoJulgadorColegiado = :OJC ");
		sb.append("and o.orgaoJulgador = :OJ ");
		sb.append("order by o.sessao.dataSessao");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("OJC", ojc);
		q.setParameter("OJ", oj);
		List<Sessao> sessaoTemp = q.getResultList();
		return sessaoTemp;
	}

	/**
	 * Metodo que junta a data de uma sessao com o horario
	 * 
	 * @param sessao
	 * @return
	 */
	public String getDataHoraSessao(Sessao sessao){
		if (sessao != null){
			return montaDataSessaoStr(sessao);
		}
		else{
			return "Não existe sessão";
		}
	}	
	
	public String getDataHoraSessao(SessaoJT sessaoJT){
		if (sessaoJT != null){
			return montaDataSessaoStr(sessaoJT);
		}
		else{
			return "Não existe sessão";
		}
	}

	@SuppressWarnings("deprecation")
	private String montaDataSessaoStr(Sessao sessao){
		String ret = "";
		if (sessao.getDataSessao() != null && sessao.getOrgaoJulgadorColegiadoSalaHorario().getHoraInicial() != null
			&& sessao.getOrgaoJulgadorColegiadoSalaHorario().getHoraInicial() != null){
			Calendar c = new GregorianCalendar();
			c.setTime(sessao.getDataSessao());
			c.set(Calendar.HOUR_OF_DAY, sessao.getOrgaoJulgadorColegiadoSalaHorario().getHoraInicial().getHours());
			c.set(Calendar.MINUTE, sessao.getOrgaoJulgadorColegiadoSalaHorario().getHoraInicial().getMinutes());
			ret = DateUtil.getDataFormatada(c.getTime(), "dd/MM/yyyy HH:mm");
		}
		return ret;
	}
	
	@SuppressWarnings("deprecation")
	private String montaDataSessaoStr(SessaoJT sessaoJT){
		String ret = "";
		if (sessaoJT.getDataSessao() != null && sessaoJT.getSalaHorario().getHoraInicial() != null
			&& sessaoJT.getSalaHorario().getHoraInicial() != null){
			Calendar c = new GregorianCalendar();
			c.setTime(sessaoJT.getDataSessao());
			c.set(Calendar.HOUR_OF_DAY, sessaoJT.getSalaHorario().getHoraInicial().getHours());
			c.set(Calendar.MINUTE, sessaoJT.getSalaHorario().getHoraInicial().getMinutes());
			ret = DateUtil.getDataFormatada(c.getTime(), "dd/MM/yyyy HH:mm");
		}
		return ret;
	}

	public String getDataHoraSessaoSelecione(Sessao sessao){
		if (sessao != null){
			return montaDataSessaoStr(sessao);
		}
		else{
			return "Selecione";
		}
	}

	public void setRevisorSessao(){
		RevisorProcessoTrf rev = RevisorProcessoTrfHome.instance().getRevisorProcessoTrf(getInstance());
		if (rev != null){
			orgaoJulgadorRevisor = rev.getOrgaoJulgadorRevisor();
		}
	}

	public void gravarDataSugestaoSessao(){
		// grava novo revisor
		if (null != RevisorProcessoTrfHome.instance().getOrgaoJulgadorRevisor()){
			RevisorProcessoTrfHome.instance().gravarOrgaoRevisor(instance, instance.getSessaoSugerida());
		}
		// grava sessão surgerida
		getEntityManager().merge(instance);
		getEntityManager().flush();
		setExibeBotaoGravar(false);
	}

	public Boolean validaPessoaLogadaPresidente(){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from SessaoComposicaoOrdem o where ");
		sb.append("o.presidente = true ");
		sb.append("and o.orgaoJulgador = :OJ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("OJ", Authenticator.getOrgaoJulgadorAtual());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	/**
	 * Metodo que verifica se o Revisor é setado como "true" e o RelatorRevisor está setado como "REL". Se as duas
	 * condiéées estiverem ok, o Metodo verifica se o tipo do documento é igual a "Relatério" e se está assinado, retornando true se sim ou false se
	 * Não.
	 */
	public Boolean getVerificaProntoRevisao(){
		if (BooleanUtils.isTrue(getInstance().getExigeRevisor())){
			StringBuilder sb = new StringBuilder();
			sb.append("select count(o) from SessaoProcessoDocumento o ");
			sb.append("where o.tipoInclusao = 'A' ");
			sb.append("and o.sessao is null ");
			sb.append("and o.processoDocumento.tipoProcessoDocumento = :tpd ");
			sb.append("and o.processoDocumento.processoDocumentoBin.signature is not null ");
			sb.append("and o.processoDocumento.processo = :processo ");

			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("tpd", ParametroUtil.instance().getTipoProcessoDocumentoRelatorio());
			q.setParameter("processo", getInstance().getProcesso());
			try {
				Long retorno = (Long) q.getSingleResult();
				return retorno > 0;
			} catch (NoResultException no) {
				return Boolean.FALSE;
			}
		}
		return false;
	}

	/**
	 * Metodo que retorna o revisor por string em caso de combo desabilitada (nonselectionLabel) nos detalhes do processo
	 * 
	 * @return
	 */
	public String getRevisorStr(){
		RevisorProcessoTrf rpTrfTemp = getRevisorProcessoTrf(getInstance());
		if (rpTrfTemp != null && rpTrfTemp.getOrgaoJulgadorRevisor() != null){
			return rpTrfTemp.getOrgaoJulgadorRevisor().toString();
		}
		return "Selecione";
	}

	/**
	 * Pega o revisor do processoTrf
	 * 
	 * @param procTrf
	 * @return
	 */
	private RevisorProcessoTrf getRevisorProcessoTrf(ProcessoTrf procTrf){
		StringBuilder sb = new StringBuilder();
		sb.append("select rptrf from RevisorProcessoTrf rptrf ");
		sb.append("where rptrf.idRevisorProcessoTrf = (select MAX(maxRptrf.idRevisorProcessoTrf) from RevisorProcessoTrf maxRptrf ");
		sb.append("where maxRptrf.dataFinal is null and ");
		sb.append("maxRptrf.processoTrf = :processoTrf) ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoTrf", procTrf);
		q.setMaxResults(1);
		RevisorProcessoTrf rpTrfTemp = null;
		try {
			rpTrfTemp = (RevisorProcessoTrf) q.getSingleResult();
		} catch (NoResultException no) {
		}
		return rpTrfTemp;
	}

	public void desbloquearProcesso(Processo processo){
		processo.setActorId(null);
		getEntityManager().merge(processo);
		EntityUtil.flush();
	}

	public Sessao getSessao(){
		return sessao;
	}

	public void setSessao(Sessao sessao){
		this.sessao = sessao;
	}

	public OrgaoJulgador getOrgaoJulgadorRevisor(){
		return orgaoJulgadorRevisor;
	}

	public void setOrgaoJulgadorRevisor(OrgaoJulgador orgaoJulgadorRevisor){
		this.orgaoJulgadorRevisor = orgaoJulgadorRevisor;
	}

	public Boolean getMensagemJusticaGratuita(){
		return mensagemJusticaGratuita;
	}

	public void setMensagemJusticaGratuita(Boolean mensagemJusticaGratuita){
		this.mensagemJusticaGratuita = mensagemJusticaGratuita;
	}

	public void refreshGridsPainel(){
		refreshGrid("painelUsuarioMagistradoGrid");
		refreshGrid("documentoSigiloGrid");
		refreshGrid("pedidoJusticaGratuitaGrid");
		refreshGrid("peticoesAvulsasGrid");
		refreshGrid("tutelaLiminarGrid");
		refreshGrid("analisePrevencaoGrid");
		refreshGrid("processoDocumentoNaoLidoGrid");
		refreshGrid("processoDocumentoTrfPeritoGrid");
		refreshGrid("processoDocumentoTrfProcuradorGrid");
	}

	/**
	 * Metodo que verifica se o processo tem alguma prioridade
	 * 
	 * @param processoTrf
	 * @return true ou false
	 */
	public boolean isPrioritario(ProcessoTrf processoTrf){
		if (processoTrf == null){
			return false;
		}
		return isPrioritario(processoTrf.getIdProcessoTrf());
	}

	public boolean isPrioritario(ProcessoTrfConsultaSemFiltros processoTrf){
		if (processoTrf == null){
			return false;
		}
		return isPrioritario(processoTrf.getIdProcessoTrf());
	}

	private boolean isPrioritario(int idProcessoTrf){
		String hql = "select count(o) from ProcessoPrioridadeProcesso o "
			+ "where o.processoTrf.idProcessoTrf = :idProcessoTrf";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("idProcessoTrf", idProcessoTrf);
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	/**
	 * @param processoTrf
	 * @return Nome da primeira parte do processo do polo ativo que Não séo advogados e nem procuradores.
	 */
	public String getNomeProcessoPartePoloAtivo(ProcessoTrf processoTrf){
		return getNomeProcessoParte(getProcessoPartePoloAtivoList(processoTrf));
	}
	
	/**
	 * @param documento
	 * @return
	 */
	public String obterPoloAtivoPeloProcessoDocumento(ProcessoDocumento documento) {
		
		StringBuilder sb =  new StringBuilder();
		sb.append("SELECT ptrf FROM ProcessoTrf ptrf  ");
		sb.append("WHERE ptrf.idProcessoTrf =:idProcesso ");
		
		Query query = this.getEntityManager().createQuery(sb.toString());
		query.setParameter("idProcesso", documento.getProcesso().getIdProcesso());
		
		ProcessoTrf processoTrf = (ProcessoTrf) query.getSingleResult();
		
		return getNomeProcessoParte(getProcessoPartePoloAtivoList(processoTrf));
	}

	/**
	 * @param processoTrf
	 * @return Nome da primeira parte do processo do polo passivo que Não são advogados e nem procuradores.
	 */
	public String getNomeProcessoPartePoloPassivo(ProcessoTrf processoTrf){
		return getNomeProcessoParte(getProcessoPartePoloPassivoList(processoTrf));
	}

	public String obterPoloPassivoPeloProcessoDocumento(ProcessoDocumento documento) {
		StringBuilder sb =  new StringBuilder();
		sb.append("SELECT ptrf FROM ProcessoTrf ptrf ");
		sb.append("WHERE ptrf.idProcessoTrf =:idProcesso ");
		
		Query query = this.getEntityManager().createQuery(sb.toString());
		query.setParameter("idProcesso", documento.getProcesso().getIdProcesso());
		
		ProcessoTrf processoTrf = (ProcessoTrf) query.getSingleResult();
		
		return getNomeProcessoParte(getProcessoPartePoloPassivoList(processoTrf));
	}
	/**
	 * @param processoTrf
	 * @return Nome da primeira parte do processo que Não séo advogados e nem procuradores.
	 */
	private String getNomeProcessoParte(List<ProcessoParte> processoParteList){
		StringBuilder nomeProcessoParte = new StringBuilder();
		int size = processoParteList.size();
		if (size == 0){
			return null;
		}
		else if (size == 1){
			return processoParteList.get(0).getNomeParte();
		}
		else{
			for (ProcessoParte processoParte : processoParteList){
				if (!processoParte.getTipoParte().getTipoParte().matches("ADVOGADO|PROCURADOR")){
					nomeProcessoParte.append(processoParte.getNomeParte());
					break;
				}
			}
			if (nomeProcessoParte.length() < 1){
				nomeProcessoParte.append(processoParteList.get(0).getNomeParte());
			}
			nomeProcessoParte.append(" e outros");
		}
		return nomeProcessoParte.toString();
	}

	/**
	 * @param processoTrf
	 * @return Lista de partes do processo do polo ativo.
	 */
	public List<ProcessoParte> getProcessoPartePoloAtivoList(ProcessoTrf processoTrf){
		return getProcessoParteList(processoTrf, ProcessoParteParticipacaoEnum.A);
	}

	/**
	 * @param processoTrf
	 * @return Lista de partes do processo do polo passivo.
	 */
	public List<ProcessoParte> getProcessoPartePoloPassivoList(ProcessoTrf processoTrf){
		return getProcessoParteList(processoTrf, ProcessoParteParticipacaoEnum.P);
	}

	/**
	 * Metodo responsável por recuperar todas as partes de um dos polos do processo.
	 * 
	 * @param processoTrf Processo judicial.
	 * @param processoParteParticipacaoEnum Polo.
	 * @return Lista partes de um dos polos do processo.
	 */
	public List<ProcessoParte> getProcessoParteList(ProcessoTrf processoTrf, ProcessoParteParticipacaoEnum processoParteParticipacaoEnum) {
		List<ProcessoParte> processoParteList = new ArrayList<ProcessoParte>(0);
		if (processoTrf == null || processoTrf.getProcessoParteList().size() == 0) {
			return processoParteList;
		} else{
			for (ProcessoParte processoParte : processoTrf.getProcessoParteList()) {
				if (processoParte.getInParticipacao().equals(processoParteParticipacaoEnum)) {
					processoParteList.add(processoParte);
				}
			}
			return processoParteList;
		}
	}

	public String getPolos(ProcessoTrf processoTrf){
		return getPoloAtivo(processoTrf) + " X " + getPoloPassivo(processoTrf);
	}

	public String getPoloAtivo(ProcessoTrf processoTrf){
		return getParte(processoTrf.getListaParteAtivo());
	}

	public String getPoloPassivo(ProcessoTrf processoTrf){
		return getParte(processoTrf.getListaPartePassivo());
	}

	public String getParte(List<ProcessoParte> partes){
		if (partes.size() == 1){
			StringBuilder retorno = new StringBuilder(partes.get(0).getPessoaNomeAlternativo() == null ?  
					partes.get(0).getNomeParte() :  
					partes.get(0).getPessoaNomeAlternativo().getPessoaNomeAlternativo());
			
			if (partes.get(0).getPessoa().getDocumentoCpfCnpj() != null) {
				retorno.append(" (");
				retorno.append(partes.get(0).getPessoa().getDocumentoCpfCnpj());
				retorno.append(")");
			}
			return retorno.toString();
		}

		List<ProcessoParte> listParte = new ArrayList<ProcessoParte>();
		for (ProcessoParte parte : partes){
			if (!parte.getTipoParte().getTipoParte().matches(("ADVOGADO|PROCURADOR"))){
				listParte.add(parte);
			}
		}

		String nome = "";
		String documento = "";
		String sufixo = "";
		if (!listParte.isEmpty()){
			ProcessoParte parte = listParte.get(0); 
			nome = parte.getNomeParte();
			documento = StringUtils.isNotBlank(parte.getPessoa().getDocumentoCpfCnpj()) ? 
					" (" + parte.getPessoa().getDocumentoCpfCnpj()  + ")" : StringUtils.EMPTY;
			
			int tam = listParte.size();
			if (tam == 2){
				sufixo = " e outro";
			}
			else if (tam > 2){
				sufixo = " e outros";
			}
		}
		if ("".equals(documento)) {
			return nome + sufixo;
		} else {
			return nome + documento + sufixo ;
		}
	}

	public void setBtGravar(boolean btGravar){
		this.btGravar = btGravar;
	}

	public boolean getBtGravar(){
		return btGravar;
	}

	/**
	 * Remove os registros selecionados da listagem do agrupador.
	 */
	public void alterarListaProcessosComAudienciaNaoDesignadas() {
		if (listaProcessoComAudienciaNaoDesignada.size() > 0){
			for (ProcessoTrf p : listaProcessoComAudienciaNaoDesignada){
				p.setDeveMarcarAudiencia(Boolean.FALSE);
				EntityUtil.getEntityManager().merge(p);
			}
			EntityUtil.getEntityManager().flush();
		}
		limparListaToogles(listaProcessoComAudienciaNaoDesignada);
		listaProcessoComAudienciaNaoDesignada.clear();
		setCheckAllProcessoComAudienciaNaoDesignada(Boolean.FALSE);
		refreshGrid("processoAudienciaNaoMarcadaGrid");
		ProcessoComAudienciaNaoMarcadaList p = getComponent(ProcessoComAudienciaNaoMarcadaList.NAME, false);
		p.refreshResultadoTotal();
	}
	
	
	
	/**
	 * Remove os registros selecionados da listagem do agrupador.
	 */
	public void alterarListaDevolvidos() {
		if (listaMandadoDevolvido.size() > 0){
			for (ProcessoTrf p : listaMandadoDevolvido){
				p.setMandadoDevolvido(Boolean.FALSE);
				EntityUtil.getEntityManager().merge(p);
			}
			EntityUtil.getEntityManager().flush();
		}
		limparListaToogles(listaMandadoDevolvido);
		listaMandadoDevolvido.clear();
		setCheckAllMandadoDevolvido(Boolean.FALSE);
		refreshGrid("painelMandadosDevolvidosOJGrid");
	}
	

	@SuppressWarnings("unchecked")
	public void alterarRetiradoPeticaoNaoLida(Processo processo){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoDocumentoPeticaoNaoLida o ");
		sb.append("where o.retirado = false and o.processoDocumento.processo = :processo");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processo", processo);

		List<ProcessoDocumentoPeticaoNaoLida> lista = q.getResultList();
		if (lista != null && !lista.isEmpty()){
			for (ProcessoDocumentoPeticaoNaoLida processoDocumentoPeticaoNaoLida : lista){
				processoDocumentoPeticaoNaoLida.setRetirado(true);
				getEntityManager().merge(processoDocumentoPeticaoNaoLida);
				EntityUtil.flush();
			}
		}
	}

	public void alterarListaPeticaoAvulsa(){
		if (listaPeticaoAvulsa.size() > 0){
			for (ProcessoTrf p : listaPeticaoAvulsa){
				alterarRetiradoPeticaoNaoLida(p.getProcesso());
			}
		}
		limparListaToogles(listaPeticaoAvulsa);
		listaPeticaoAvulsa.clear();
		setCheckAllPeticaoAvulsa(Boolean.FALSE);
	}

	public void alterarListaApreciado(){
		if (listaPedidoSegredo.size() > 0){
			for (ProcessoTrf p : listaPedidoSegredo){
				p.setApreciadoSegredo(ProcessoTrfApreciadoEnum.S);
				EntityUtil.getEntityManager().merge(p);
			}
			EntityUtil.getEntityManager().flush();
		}
		limparListaToogles(listaPedidoSegredo);
		listaPedidoSegredo.clear();
		setCheckAll(Boolean.FALSE);
		refreshGrid("painelUsuarioMagistradoGrid");
	}

	public void alterarListaApreciadoTutelaLiminar(){
		if (listaPedidoLiminar.size() > 0){
			for (ProcessoTrf p : listaPedidoLiminar){
				p.setApreciadoTutelaLiminar(Boolean.TRUE);
				EntityUtil.getEntityManager().merge(p);
			}
			EntityUtil.getEntityManager().flush();
		}
		limparListaToogles(listaPedidoLiminar);
		listaPedidoLiminar.clear();
		setCheckAll(Boolean.FALSE);
		refreshGrid("tutelaLiminarGrid");
	}

	public void alterarListaApreciadoJusticaGratuita(){
		if (listaPedidoJustica.size() > 0){
			for (ProcessoTrf processo : listaPedidoJustica){
				processo.setApreciadoJusticaGratuita(Boolean.TRUE);
				EntityUtil.getEntityManager().merge(processo);
			}
			EntityUtil.getEntityManager().flush();
		}
		limparListaToogles(listaPedidoJustica);
		listaPedidoJustica.clear();
		setCheckAll(Boolean.FALSE);
		refreshGrid("pedidoJusticaGratuitaGrid");
	}

	public void alterarListaApreciadoSigilo(){
		if (listaPedidoSigilo.size() > 0){
			for (ProcessoTrf p : listaPedidoSigilo){
				p.setApreciadoSigilo(ProcessoTrfApreciadoEnum.S);
				EntityUtil.getEntityManager().merge(p);
			}
			EntityUtil.getEntityManager().flush();
		}
		limparListaToogles(listaPedidoSigilo);
		listaPedidoSigilo.clear();
		setCheckAllSigilo(Boolean.FALSE);
		refreshGrid("documentoSigiloGrid");
	}

	public void limparListaToogles(List<ProcessoTrf> lista){
		for (ProcessoTrf processoTrf : lista){
			processoTrf.setCheck(false);
		}
		refreshGrid("painelUsuarioMagistradoGrid");
		refreshGrid("documentoSigiloGrid");
		refreshGrid("tutelaLiminarGrid");
		refreshGrid("pedidoJusticaGratuitaGrid");
		refreshGrid("peticoesAvulsasGrid");
		refreshGrid("analisePrevencaoGrid");
		refreshGrid("processoPeticaoNaoLidaGrid");
		refreshGrid("processoAnalisePrevencaoGrid");
	}

	public Boolean getCheckAll(){
		return checkAll;
	}

	public void setCheckAll(Boolean checkAll){
		this.checkAll = checkAll;
	}

	public Boolean getCheckAllSigilo(){
		return checkAllSigilo;
	}

	public void setCheckAllSigilo(Boolean checkAllSigilo){
		this.checkAllSigilo = checkAllSigilo;
	}

	public Boolean getCheckAllSegredo(){
		return checkAllSegredo;
	}

	public void setCheckAllSegredo(Boolean checkAllSegredo){
		this.checkAllSegredo = checkAllSegredo;
	}

	public Boolean getCheckAllLiminar(){
		return checkAllLiminar;
	}

	public void setCheckAllLiminar(Boolean checkAllLiminar){
		this.checkAllLiminar = checkAllLiminar;
	}

	public void confirmaMotivoSegredoJustica(){
		if (getInstance().getSegredoJustica() != null 
				&& getInstance().getSegredoJustica() 
				&& Strings.isEmpty(getInstance().getObservacaoSegredo()) 
				&& !verificarClasseSigilosa()){
			getInstance().setSegredoJustica(false);
		}
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> checkAll(String grid, List<ProcessoTrf> lista, Boolean checkAll){
		GridQuery gq = ComponentUtil.getComponent(grid);
		if (checkAll){
			lista.clear();
			lista.addAll(gq.getFullList());
			for (ProcessoTrf p : lista){
				p.setCheck(Boolean.TRUE);
			}
		}
		else{
			for (ProcessoTrf p : lista){
				p.setCheck(Boolean.FALSE);
			}
			lista.clear();
		}
		return lista;
	}

	public void criarListaPedido(ProcessoTrf obj, String grid){
		if (grid.equals("painelUsuarioMagistradoGrid")){
			trataInsercaoRemocaoLista(obj, grid, listaPedidoSegredo, checkAllSegredo); 
		}
		else if (grid.equals("documentoSigiloGrid")){
			trataInsercaoRemocaoLista(obj, grid, listaPedidoSigilo, checkAllSigilo);
		}
		else if (grid.equals("tutelaLiminarGrid")){
			trataInsercaoRemocaoLista(obj, grid, listaPedidoLiminar, checkAllLiminar);
		}
		else if (grid.equals("pedidoJusticaGratuitaGrid")){
			trataInsercaoRemocaoLista(obj, grid, listaPedidoJustica, checkAll);
		}
		else if (grid.equals("peticoesAvulsasGrid") || grid.equals("processoPeticaoNaoLidaGrid")){
			trataInsercaoRemocaoLista(obj, grid, listaPeticaoAvulsa, checkAllPeticaoAvulsa);
		}
		else if (grid.equals("analisePrevencaoGrid") || grid.equals("processoAnalisePrevencaoGrid")){
			trataInsercaoRemocaoLista(obj, grid, listaAnalisePrevencao, checkAllAnalisePrevencao);
		}
		else if (grid.equals("painelMandadosDevolvidosOJGrid")) {
			trataInsercaoRemocaoLista(obj, grid,listaMandadoDevolvido, checkAllMandadoDevolvido);
		}
		else if (grid.equals("processoAudienciaNaoMarcadaGrid")) {
			trataInsercaoRemocaoLista(obj, grid,listaProcessoComAudienciaNaoDesignada, checkAllProcessoComAudienciaNaoDesignada);
		}
	}

	/**
	 * @param obj
	 * @param grid
	 */
	private void trataInsercaoRemocaoLista(ProcessoTrf obj, String grid, List<ProcessoTrf> lista, Boolean checkAll) {
		if (obj == null){
			checkAll(grid, lista, checkAll);
		}else{
			addRemove(obj, lista);
		}
	}

	public List<ProcessoTrf> addRemove(ProcessoTrf obj, List<ProcessoTrf> lista){
		if (lista.contains(obj)){
			lista.remove(obj);
		}
		else{
			lista.add(obj);
		}
		return lista;
	}

	public List<ProcessoTrf> getListaPedidoSegredo(){
		return listaPedidoSegredo;
	}

	public void setListaPedidoSegredo(List<ProcessoTrf> listaPedidoSegredo){
		this.listaPedidoSegredo = listaPedidoSegredo;
	}

	public List<ProcessoTrf> getListaPedidoSigilo(){
		return listaPedidoSigilo;
	}

	public void setListaPedidoSigilo(List<ProcessoTrf> listaPedidoSigilo){
		this.listaPedidoSigilo = listaPedidoSigilo;
	}

	public List<ProcessoTrf> getListaPedidoLiminar(){
		return listaPedidoLiminar;
	}

	public void setListaPedidoLiminar(List<ProcessoTrf> listaPedidoLiminar){
		this.listaPedidoLiminar = listaPedidoLiminar;
	}

	public List<ProcessoTrf> getListaPedidoJustica(){
		return listaPedidoJustica;
	}

	public void setListaPedidoJustica(List<ProcessoTrf> listaPedidoJustica){
		this.listaPedidoJustica = listaPedidoJustica;
	}

	public List<ProcessoTrf> getListaPeticaoAvulsa(){
		return listaPeticaoAvulsa;
	}

	public void setListaPeticaoAvulsa(List<ProcessoTrf> listaPeticaoAvulsa){
		this.listaPeticaoAvulsa = listaPeticaoAvulsa;
	}

	public boolean isRemetidoSegundoGrau(){
		if (this.remetidoSegundoGrau != null){
			return this.remetidoSegundoGrau;
		}

		if (getId() == null){
			setId(ProcessoHome.instance().getId());
		}
		if (getId() != null && ParametroUtil.instance().isPrimeiroGrau() && verificaRemetido2Grau(instance)){
			this.remetidoSegundoGrau = true;
		}
		else{
			this.remetidoSegundoGrau = false;
		}

		return this.remetidoSegundoGrau;

	}

	public boolean isRemessaRecente(){
		return remessaRecente.booleanValue();
	}

	public boolean mostrarCertidaoPeticaoInicial(ProcessoDocumento pd){
		boolean ret = false;
		String sql = "select min(o.dataAssinatura) from ProcessoDocumentoBinPessoaAssinatura o "
			+ "where o.processoDocumentoBin.idProcessoDocumentoBin = :idpd";
		Query query = getEntityManager().createQuery(sql);
		query.setParameter("idpd", pd.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
		Date menorDataAssinatura = (Date) query.getSingleResult();
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, pd.getProcesso().getIdProcesso());
		if (menorDataAssinatura != null
			&& this.getInstance().getClasseJudicial().getTipoProcessoDocumentoInicial() == pd.getTipoProcessoDocumento()
			&& processoTrf.getDataAutuacao() != null
			&& (menorDataAssinatura.before(processoTrf.getDataAutuacao()))
			&& (processoTrf.getProcessoStatus().equals(ProcessoStatusEnum.V) || processoTrf.getProcessoStatus()
					.equals(ProcessoStatusEnum.D))){
			ret = true;
		}
		return ret;
	}

	public void setAssuntoTrfCompletoList(String assuntoTrfCompletoList){
		this.assuntoTrfCompletoList = assuntoTrfCompletoList;
	}

	public String getAssuntoTrfCompletoList(){
		return assuntoTrfCompletoList;
	}

	/**
	 * Verifica se o processo se encontra em pauta de julgamento
	 * 
	 * @param procTrf ProcessoTrf esperado para efetuar verificação
	 * @return Retorna "True" se o processo encontra se em alguma pauta de julgamento e "false" caso contrério
	 */
	public boolean verificaColocaoPauta(ProcessoTrf procTrf){
		boolean ret = false;
		ProcessoEvento ultimoEventoInclusao = pegarUltimoProcessoEventoPorEventoProcessual(ParametroUtil.instance()
				.getEventoInclusaoPauta(), procTrf.getProcesso());
		ProcessoEvento ultimoEventoRetirada = pegarUltimoProcessoEventoPorEventoProcessual(ParametroUtil.instance()
				.getEventoRetiradoPauta(), procTrf.getProcesso());
		// verifica se já houve evento de inclusão em pauta daquele processo
		if (null != ultimoEventoInclusao){
			// verifica se já houve evento de retirada em pauta daquele processo
			if (null != ultimoEventoRetirada){
				// checa se a data da ultima inclusão do processo é mais atual
				// que a data da éltima retirada de pauta do processo
				if (ultimoEventoInclusao.getDataAtualizacao().after(ultimoEventoRetirada.getDataAtualizacao())){
					ret = true;
				}
			}
			else{
				ret = true;
			}
		}
		return ret;
	}

	/**
	 * Verifica se o processo se encontra com status de remetido para o 2 grau
	 * 
	 * @param procTrf ProcessoTrf esperado para efetuar verificação
	 * @return Retorna "true" se o processo encontra se remetido para o segundo grau e "false" caso contrério
	 */
	public boolean verificaRemetido2Grau(ProcessoTrf procTrf){
		boolean processoRemetidoOutraInstancia = false;
		ProcessoEvento ultimoEventoEnvio = pegarUltimoProcessoEventoPorEventoProcessual(ParametroUtil.instance()
				.getEventoRemetidoTrf(), procTrf.getProcesso());
		ProcessoEvento ultimoEventoRecebimento = pegarUltimoProcessoEventoPorEventoProcessual(ParametroUtil.instance()
				.getEventoRecebimento1Grau(), procTrf.getProcesso());
		// verifica se já houve evento de envio para o segundo grau daquele processo
		if (null != ultimoEventoEnvio){
			// verifica se já houve evento de recebimento no primeiro grau daquele processo
			if (null != ultimoEventoRecebimento){
				// checa se a data do último envio do processo é mais atual que a data do último recebimento do processo
				if (ultimoEventoEnvio.getDataAtualizacao().after(ultimoEventoRecebimento.getDataAtualizacao())){
					processoRemetidoOutraInstancia = true;
				}
			} else {
				processoRemetidoOutraInstancia = true;
			}
		}
		
		if (!processoRemetidoOutraInstancia) processoRemetidoOutraInstancia = procTrf.getInOutraInstancia();
		
		return processoRemetidoOutraInstancia;
	}

	/**
	 * Pega o último evento processual para um determinado processo de um tipo específico de evento processual.
	 * 
	 * @param ep Evento processual esperado para Execução do filtro.
	 * @param proc Processo esperado para busca.
	 * @return Retorna o ultimo ProcessoEvento do tipo de Evento especificado, para o processo especificado
	 */
	public ProcessoEvento pegarUltimoProcessoEventoPorEventoProcessual(Evento e, Processo proc){
		StringBuilder sb = new StringBuilder();
		sb.append("select pe from ProcessoEvento pe ");
		sb.append("where pe.dataAtualizacao in (select MAX(pedata.dataAtualizacao) from ProcessoEvento pedata ");
		sb.append("   where pedata.evento = :evento ");
		sb.append("   and pedata.processo = :processo) ");
		sb.append("and pe.processo = :processo ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("evento", e);
		q.setParameter("processo", proc);
		if (q.getResultList().size() > 0){
			return (ProcessoEvento) q.getResultList().get(0);
		}
		return null;
	}

	public boolean numeroProcessoExistente(Processo processo){
		return !Strings.isEmpty(processo.getNumeroProcesso());
	}

	public RemessaProcesso getRemessaProcesso(){
		try{
			if (this.remessaProcesso == null){
				this.remessaProcesso = new RemessaProcesso();
			}
		} catch (Exception e){
			log.error("Erro na inicializaééo da classe de remessa. " + e.getMessage());
			FacesMessages.instance()
					.add(Severity.ERROR, "Erro na inicializaééo da classe de remessa." + e.getMessage());
		}
		return this.remessaProcesso;
	}

	public void setRemessaProcesso(RemessaProcesso remessaProcesso){
		this.remessaProcesso = remessaProcesso;
	}

	public Boolean getRemessaRecente(){
		return remessaRecente;
	}

	public void setRemessaRecente(Boolean remessaRecente){
		this.remessaRecente = remessaRecente;
	}

	/*
	 * Metodo com a regra que define quando deverá ser mostrado os campos Oj e Ojc no documento Certidão
	 */
	public boolean mostrarOjOjcDocCertidao(){
		if (ParametroUtil.instance().isPrimeiroGrau()){
			return false;
		}
		if (instance.getProcesso().getFluxo() == null){
			return false;
		}
		if (instance.getProcesso() != null && instance.getProcesso().getFluxo() != null &&  instance.getProcesso()
				.getFluxo().getIdFluxo() == ParametroUtil.instance().getFluxoDistribuicao().getIdFluxo()){
			return false;
		}
		return true;
	}

	public void gravarMotivoAcessoProcessoTerceiro(){
		try{
			if (getInstance() != null){
				historicoAcessoTerceiros.setProcessoTrf(getInstance());
				historicoAcessoTerceiros.setDsUsuarioAcessou(Authenticator.getUsuarioLogado().getNome());
				historicoAcessoTerceiros.setNrOabProcuradoria(ConsultaProcessoHome.instance().getOabPessoaAdvogado(
						Authenticator.getUsuarioLogado()));
				historicoAcessoTerceiros.setUsuario(Authenticator.getUsuarioLogado());
				historicoAcessoTerceiros.setIP(LogUtil.getIpRequest(100));
				historicoAcessoTerceiros.setDtMotivoAcesso(new Date());
				getEntityManager().persist(historicoAcessoTerceiros);
				getEntityManager().flush();
				historicoAcessoTerceiros = new HistoricoMotivoAcessoTerceiro();
				ProcessoHome phome = getProcessoHome();
				phome.setId(getInstance().getProcesso().getIdProcesso());
				setHashOS(EncryptionSecurity.encrypt(String.valueOf(getInstance().getProcesso().getIdProcesso())));
			}
		} catch (Exception e){
			log.error("Erro ao gravar o motivo de acesso a processo de terceiro: " + e.getMessage());
		}

	}
	
	public void gravarMotivoAcessoProcessoTerceiroJT(ProcessoTrf processo){
		try{
			if (getInstance() != null 
					&& Authenticator.isUsuarioExterno()) { // grava historico somente para usuario externo. [#PJEI-3504]
				historicoAcessoTerceiros = new HistoricoMotivoAcessoTerceiro();
				historicoAcessoTerceiros.setProcessoTrf(processo);
				historicoAcessoTerceiros.setDsUsuarioAcessou(Authenticator.getUsuarioLogado().getNome());
				historicoAcessoTerceiros.setNrOabProcuradoria(ConsultaProcessoHome.instance().getOabPessoaAdvogado(
						Authenticator.getUsuarioLogado()));
				historicoAcessoTerceiros.setUsuario(Authenticator.getUsuarioLogado());
				historicoAcessoTerceiros.setIP(LogUtil.getIpRequest(100));
				historicoAcessoTerceiros.setDtMotivoAcesso(new Date());
				getEntityManager().persist(historicoAcessoTerceiros);
				getEntityManager().flush();
				ProcessoHome phome = getProcessoHome();
				phome.setId(processo.getProcesso().getIdProcesso());
				setHashOS(EncryptionSecurity.encrypt(String.valueOf(processo.getProcesso().getIdProcesso())));
			}
		} catch (Exception e){
			log.error("Erro ao gravar o motivo de acesso a processo de terceiro: " + e.getMessage());
		}

	}

	public void gravarMotivoAcessoProcessoOutrasSecoes(){
		try{
			if (getInstance() != null){
				historicoAcessoTerceiros.setProcessoTrf(getInstance());
				historicoAcessoTerceiros.setDsUsuarioAcessou(getDsUsuarioAcessouOS());
				historicoAcessoTerceiros.setNrOabProcuradoria(getOabProcOS());
				historicoAcessoTerceiros.setDtMotivoAcesso(new Date());
				getEntityManager().persist(historicoAcessoTerceiros);
				getEntityManager().flush();
				historicoAcessoTerceiros = new HistoricoMotivoAcessoTerceiro();
				ProcessoHome phome = getProcessoHome();
				phome.setId(getInstance().getProcesso().getIdProcesso());
				setHashOS(EncryptionSecurity.encrypt(String.valueOf(getInstance().getProcesso().getIdProcesso())));
			}
		} catch (Exception e){
			log.error("Erro ao gravar o motivo de acesso a processo de terceiro: " + e.getLocalizedMessage());
		}
	}

	public void setIdOS(String link, String hash, String oabProc, String dsUsuarioAcessou){
		setLinkConsulta(link);
		setHashOS(hash);
		setOabProcOS(oabProc);
		setDsUsuarioAcessouOS(dsUsuarioAcessou);
	}

	public void setarInstanciaConsultaOS() throws InvalidKeyException, BadPaddingException, NoSuchPaddingException,
			IllegalBlockSizeException, NoSuchAlgorithmException, InvalidAlgorithmParameterException{
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		String hash = null;
		if (request.getParameter("hash") != null){
			hash = request.getParameter("hash");
			hash = decript(hash.replace(' ', '+'));
			Integer idProcesso = Integer.parseInt(hash);
			setId(idProcesso);
		}
		setOabProcOS(StringUtil.convertUtf8ToIso88591(request.getParameter("oabProc")));
		setDsUsuarioAcessouOS(StringUtil.convertUtf8ToIso88591(request.getParameter("dsUsuarioAcessou")));
	}

	private String decript(String key) throws InvalidKeyException, BadPaddingException, NoSuchPaddingException,
			IllegalBlockSizeException, NoSuchAlgorithmException, InvalidAlgorithmParameterException{
		key = EncryptionSecurity.decrypt(key);
		return key;
	}

	public void setHistoricoAcessoTerceiros(HistoricoMotivoAcessoTerceiro historicoAcessoTerceiros){
		this.historicoAcessoTerceiros = historicoAcessoTerceiros;
	}

	public HistoricoMotivoAcessoTerceiro getHistoricoAcessoTerceiros(){
		return historicoAcessoTerceiros;
	}

	public boolean getExibeBotaoGravar(){
		return exibeBotaoGravar;
	}

	public void setExibeBotaoGravar(boolean exibeBotaoGravar){
		this.exibeBotaoGravar = exibeBotaoGravar;
	}

	public void setLinkConsulta(String linkConsulta){
		this.linkConsulta = linkConsulta;
	}

	public String getLinkConsulta(){
		return linkConsulta;
	}

	public void setHashOS(String hashOS){
		this.hashOS = hashOS;
	}

	public String getHashOS(){
		return hashOS;
	}

	public Long resultCountGrid(String grid){
		refreshGrid(grid);
		GridQuery gridQuery = ComponentUtil.getComponent(grid);
		return gridQuery.getResultCount();
	}

	@SuppressWarnings("unchecked")
	public List<Jurisdicao> itemsJurisdicao(){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Jurisdicao o ");
		sb.append("where o.ativo = true ");
		if (ParametroUtil.instance().isPrimeiroGrau()){
			sb.append("and o.numeroOrigem != 0 ");
		}
		Query q = getEntityManager().createQuery(sb.toString());
		return q.getResultList();
	}

	public String getCargoProcesso(){
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, ProcessoHome.instance().getId());
		if (processoTrf.getCargo() != null){
			return processoTrf.getCargo().getSigla();
		}
		return "";
	}

	public String getClasseProcesso(){
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, ProcessoHome.instance().getId());
		if (processoTrf.getClasseJudicialStr() != null){
			return processoTrf.getClasseJudicialStr();
		}
		return "";
	}

	public boolean getRecursal(){
		return ParametroUtil.instance().isPrimeiroGrau();
	}

	public Boolean getCheckAllPeticaoAvulsa(){
		return checkAllPeticaoAvulsa;
	}

	public void setCheckAllPeticaoAvulsa(Boolean checkAllPeticaoAvulsa){
		this.checkAllPeticaoAvulsa = checkAllPeticaoAvulsa;
	}

	@SuppressWarnings("unchecked")
	public void alterarListaAnalisePrevencao(){
		if (listaAnalisePrevencao.size() > 0){
			for (ProcessoTrf p : listaAnalisePrevencao){
				StringBuilder sb = new StringBuilder();
				sb.append("select o from ProcessoTrfConexao o ");
				sb.append("where o.processoTrf = :processoTrf ");
				sb.append("and o.prevencao = 'PE' ");
				Query q = getEntityManager().createQuery(sb.toString());
				q.setParameter("processoTrf", p);
				List<ProcessoTrfConexao> lista = q.getResultList();
				if (lista != null && !lista.isEmpty()){
					for (ProcessoTrfConexao conexo : lista){
						conexo.setPrevencao(PrevencaoEnum.RE);
						getEntityManager().merge(conexo);
						EntityUtil.flush();
					}
				}
			}
		}
		limparListaToogles(listaAnalisePrevencao);
		listaAnalisePrevencao.clear();
		setCheckAllAnalisePrevencao(Boolean.FALSE);
	}

	public void setListaAnalisePrevencao(List<ProcessoTrf> listaAnalisePrevencao){
		this.listaAnalisePrevencao = listaAnalisePrevencao;
	}

	public List<ProcessoTrf> getListaAnalisePrevencao(){
		return listaAnalisePrevencao;
	}

	public void setCheckAllAnalisePrevencao(Boolean checkAllAnalisePrevencao){
		this.checkAllAnalisePrevencao = checkAllAnalisePrevencao;
	}

	public Boolean getCheckAllAnalisePrevencao(){
		return checkAllAnalisePrevencao;
	}

	public Boolean possuiRegistros(String nomeGrid){
		GridQuery grid = ComponentUtil.getComponent(nomeGrid);
		return grid.getResultCount() > 0;
	}

	public Integer getListParteTerceiroSize(){
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, getInstance().getIdProcessoTrf());
		return processoTrf.getListaParteTerceiro().size();

	}

	/**
	 * Metodo que pega a primeira prioridade da lista. Se tiver mais de uma, ele concatena com " e outras." e se tiver somente uma, ele retonará
	 * prioridade.
	 * 
	 * @return prioridade
	 */
	public String retornaPrioridade(){
		int qtdPrioridade = getInstance().getPrioridadeProcessoList().size();
		String prioridade = getInstance().getPrioridadeProcessoList().get(0).toString();
		if (qtdPrioridade > 1){
			prioridade = prioridade + " e outras.";
		}
		return prioridade;
	}

	public void confirmarProcessoReferencia(){
		if (getInstance().getProcessoReferencia() != null){
			getInstance().setDesProcReferencia(getInstance().getProcessoReferencia().getNumeroProcesso());
		}
	}
	
	private void inserirNoPush(){
		List<ProcessoParte> lista = new ArrayList<ProcessoParte>();
		lista.addAll(getInstance().getListaAdvogadosPoloAtivo());
		lista.addAll(getInstance().getListaAdvogadosPoloPassivo());
		ProcessoPushManager.instance().inserirNoPush(lista);
	}

	private void protocolar(boolean forcarDistribuicao) throws PJeBusinessException {
		this.resetTextosProtocolacao();
		FacesMessages.instance().clear();
		autuarProcesso();
		ProcessoTrf processoJudicial = getInstance();
		
		if (isAtendimentoEmPlantao() && !PlantaoJudicialService.instance().verificarPlantao()) {
			setAtendimentoEmPlantao(false);
		}
		
		if (isAtendimentoEmPlantao()) {
			PlantaoJudicialService.instance().registraSeDeveIrParaPlantao();
		}
		
		ProcessoTrfManager processoManager = ComponentUtil.getProcessoTrfManager();
		processoManager.aplicarNivelSigilo(getInstance());
				
		if(cadastroProcessoClet){
			inserirProcessoNoFluxo(getInstance().getIdProcessoTrf(), ParametroUtil.instance().getTarefaAnaliseLiquidacao().getFluxo());
			getInstance().setProcessoStatus(ProcessoStatusEnum.D);
		}else if (!forcarDistribuicao && ParametroUtil.instance().getDistribuicaoManual()){
			try{
				if (ParametroUtil.instance().getFluxoDistribuicao() == null){
					throw new PJeBusinessException("Não há fluxo de Distribuição definido. Consulte o administrador do sistema.");
				}
				inserirProcessoNoFluxo(getInstance().getIdProcessoTrf(), ParametroUtil.instance()
						.getFluxoDistribuicao());

				// Código = 981 - Descrição = Recebido pela Distribuição (#{objeto recebido}) #{motivo do recebimento}
				// **************************************************************************************
				MovimentoAutomaticoService.preencherMovimento().deCodigo(CodigoMovimentoNacional.COD_MOVIMENTO_DISTRIBUICAO_RECEBIDO)
					.associarAoProcesso(getInstance().getProcesso()).lancarMovimento();
				
			} catch (PJeBusinessException e){
				this.textosProtocolacao[INDEX_ERRO] = String.format("Erro ao receber processo para Distribuição: \n%s", e.getMessage());
				throw new PJeBusinessException(String.format("Erro ao receber processo para Distribuição: \n%s", e.getMessage()));
			}
		}else{
			if (!distribuirProcesso()){
				return;
			}
		}
		if(isClasseCriminalOuInfracional()){
			criarProcessoCriminal(processoJudicial);
		}

		// [PJEII-4083] Processo não estava sendo sinalizado para o agrupador "Processos com pedido de sigilo nos documentos não apreciado" 
		// quando o documento era marcado como sigiloso 
		definirSigiloEDataJuntadaProcesso(processoJudicial);
		
		ProcessoHome.instance().update();
		setIsTrue(Boolean.TRUE);
		super.update();
		
		inserirNoPush();
		
		if (processoJudicial.getSegredoJustica()) {
			ProcessoJudicialService.instance().habilitarVisibilidadePartesPoloAtivoFiscalLei(processoJudicial);
		}
	}
	
	
	private void definirSigiloEDataJuntadaProcesso(ProcessoTrf processoJudicial) {
		if (isRecursoTurmaRecursal()) 
			return;
				
			Integer idMinuta = (Integer) org.jboss.seam.bpm.ProcessInstance.instance().getContextInstance().getVariable(Variaveis.MINUTA_EM_ELABORACAO);
		
		for (ProcessoDocumento procDoc : processoJudicial.getProcesso().getProcessoDocumentoList()) {
			if (procDoc.getDocumentoSigiloso()) {
				processoJudicial.setApreciadoSigilo(ProcessoTrfApreciadoEnum.A);
			}
				
			Integer procDocId = procDoc.getIdProcessoDocumento();
			if (!procDocId.equals(idMinuta)){
				procDoc.setDataJuntada(processoJudicial.getDataDistribuicao());
			}
		}		
	}
		
	private void criarProcessoCriminal(ProcessoTrf processoTrf) throws PJeBusinessException {
		ProcessoRascunhoManager processoRascunhoManager = ComponentUtil.getComponent(ProcessoRascunhoManager.class);
		InformacaoCriminalRascunhoManager icManager = ComponentUtil.getComponent(InformacaoCriminalRascunhoManager.class);

		ProcessoCriminalDTO processoCriminalDTO = processoRascunhoManager.recuperarRascunhoProcessoCriminal(processoTrf);
		
		try{
			if(processoCriminalDTO == null){
				throw new Exception("As informaéées da aba \"Local do fato principal\" e \"Procedimento de origem\" séo obrigatórias!");
			} else {
				ProcessoCriminalRestClient processoCriminalRestClient = ComponentUtil.getComponent(ProcessoCriminalRestClient.class);
			
				if (getEhClasseCriminal().equals(Boolean.TRUE)) {
					processoCriminalDTO.setTipoProcesso(TipoProcessoEnum.CRI);
				} else if (getEhClasseInfracional().equals(Boolean.TRUE)) {
					processoCriminalDTO.setTipoProcesso(TipoProcessoEnum.INF);
				}
				processoCriminalDTO.setNrProcesso(processoTrf.getProcesso().getNumeroProcesso());
				processoCriminalDTO.setPjeOrigem(ConfiguracaoIntegracaoCloud.getAppName());

				verificarProcessoCriminalDTO(processoCriminalDTO);
				
				processoCriminalDTO = processoCriminalRestClient.createResource(processoCriminalDTO);
				
				List<InformacaoCriminalRascunho> icRascunhos = icManager.findAllByIdProcessoTrf(processoTrf.getIdProcessoTrf());
				
				List<InformacaoCriminalDTO> listaInfo = new ArrayList<InformacaoCriminalDTO>();
				
				for (InformacaoCriminalRascunho ic : icRascunhos) {
					ic = icManager.refresh(ic);
					InformacaoCriminalDTO info = new InformacaoCriminalDTO();
					info.setConteudo(ic.getInformacaoCriminal());
					info.setParte(new ParteDTO(ic.getProcessoParte().getId(), 
												ic.getProcessoParte().getIdPessoa(), 
												null,
												ic.getProcessoParte().getSituacao()));
					listaInfo.add(info);
				}
				
				if(!CollectionUtilsPje.isEmpty(listaInfo)){
					processoCriminalRestClient.inserirInformacoesCriminaisAoProcessoCriminal(processoCriminalDTO, listaInfo);
				}
			}
		}catch (PjeRestClientException e) {
			log.error(e);
			String mensagem = e.obterMensagemErroDetail();
			throw new PJeRuntimeException(mensagem, e);
		}catch(Exception e){
			String msg = "Erro ao distribuir processo criminal: "+e.getMessage();
			e.printStackTrace();
			this.textosProtocolacao[INDEX_ERRO] = msg;
			throw new PJeBusinessException(msg);
		}
	}

	public void verificarProcessoCriminalDTO(ProcessoCriminalDTO processoCriminalDTO ) throws ProcessoTrfHomeException {
		if(processoCriminalDTO.getDsLocalFato() == null || CollectionUtilsPje.isEmpty(processoCriminalDTO.getProcessoProcedimentoOrigemList())){
			throw new ProcessoTrfHomeException("As informações da aba \"Local do fato principal\" e \"Procedimento de origem\" são obrigatórias!");
		}
		if (processoCriminalDTO.getDtLocalFato() != null && processoCriminalDTO.getDtLocalFato().after(new Date())) {
			throw new ProcessoTrfHomeException("A data do fato não pode ser posterior a data do dia da distribuição!");
		}
	}
	public void protocolar() throws PJeBusinessException {
		// Operação para atender a verificação se uma classe judicial exige a inclusão de um fiscal da lei.
		ProcessoParteHome.instance().verificarExigenciaFiscalDaLei(getInstance());
		
		// Verifica se as informacoes eleitorais estao preenchidas
		if(ParametroJtUtil.instance().justicaEleitoral()) {
			ComplementoProcessoJE complementoProcessoJE = this.getInstance().getComplementoJE();
			if((complementoProcessoJE == null) || (complementoProcessoJE.getEstadoEleicao() == null) || (complementoProcessoJE.getMunicipioEleicao() == null)) {
				this.textosProtocolacao[INDEX_ERRO] = "Os dados eleitorais séo obrigatórios";
				throw new PJeBusinessException("Os dados eleitorais séo obrigatórios");
			}
			else if (complementoProcessoJE.getEleicao() == null) {
				/*
				 * Uma vez que o ano da eleição Não foi informado, seré verificado o enquadramento no art. 260 do Código Eleitoral.
				 * Caso enquadre, a protocolaééo Não seré realizada.
				 */
				if (DistribuicaoService.instance().verificarEnquadramentoPEO(this.getInstance())) {
					String msg = "O ano da eleição deve ser informado, pois este processo "
							+ "poderá ser enquadrado na prevenção do art. 260 do Código Eleitoral.";
					this.textosProtocolacao[INDEX_ERRO] = msg;
					throw new PJeBusinessException(msg);
				}
			}
		}
		
		protocolar(!ParametroUtil.instance().getDistribuicaoManual() && !cadastroProcessoClet);
		if (ParametroUtil.instance().getDistribuicaoManual()){
			this.textosProtocolacao[INDEX_DISTRIBUICAO] += String.format("\n" + TEXTO_DISTRIBUICAO_MANUAL,
					getInstance().getNumeroProcesso());
		}
		else{
			if(isAtendimentoEmPlantao()) {
				this.textosProtocolacao[INDEX_DISTRIBUICAO] += String.format("\n" + TEXTO_DISTRIBUICAO_AUTOMATICA_PLANTAO,
					getInstance().getNumeroProcesso());
			} else {
				this.textosProtocolacao[INDEX_DISTRIBUICAO] += String.format("\n" + TEXTO_DISTRIBUICAO_AUTOMATICA,
						getInstance().getNumeroProcesso(), getInstance().getOrgaoJulgador());
			}
		}

		if (getInstance().getClasseJudicial().getDesignarAudienciaEmFluxo()){
		    if (ProjetoUtil.isNotVazio(getInstance().getProcessoAudienciaList())){
		        this.textosProtocolacao[INDEX_AUDIENCIA] = String.format(
		                TEXTO_AUDIENCIA_AGENDADA, 
		                getInstance().getProcessoAudienciaList().get(0).getTipoAudiencia(), 
		                getInstance().getProcessoAudienciaList().get(0).getDtInicioFormatada());
		    }
		}
	}
	
	public TipoAudiencia tipoAudienciaPadrao() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.tipoAudiencia from OjClasseTipoAudiencia o ");
		sb.append("where o.classeJudicial.idClasseJudicial = #{processoTrfHome.instance.classeJudicial.idClasseJudicial} and ");
		sb.append("o.orgaoJulgador.idOrgaoJulgador = #{processoTrfHome.instance.orgaoJulgador.idOrgaoJulgador} ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		return EntityUtil.getSingleResult(q);
	}

	private void agendarAudiencia() {
		TipoAudiencia tipoAudienciaAutomatica = tipoAudienciaPadrao();
		if (tipoAudienciaAutomatica == null) {
			tipoAudienciaAutomatica = getInstance().getClasseJudicial().getTipoPrimeiraAudiencia();
		}
		if (tipoAudienciaAutomatica != null) {
			try{
				ProcessoAudiencia audienciaAutomatica = ProcessoAudienciaHome.instance().marcarAudienciaAutomatica(
						getInstance().getOrgaoJulgador(), tipoAudienciaAutomatica);

				if (audienciaAutomatica != null) {
					this.textosProtocolacao[INDEX_AUDIENCIA] = String.format(TEXTO_AUDIENCIA_AGENDADA,
							tipoAudienciaAutomatica, audienciaAutomatica.getDtInicioFormatada());
					
				} else {
					this.textosProtocolacao[INDEX_AUDIENCIA] = TEXTO_AUDIENCIA_NAO_AGENDADA;
				}
			} catch (Exception e){
				this.textosProtocolacao[INDEX_AUDIENCIA] = "Não foi possével marcar a audiência automética.";
			}
		}
		if (isViolacaoFaixaValores()){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, this.textosProtocolacao[INDEX_ERRO]);
		}
	}

	public boolean getValidado() {
		return validado;
	}
	
	private boolean validado = false;
	
	public boolean validar(){
		validado = false;		
		try{
			Processo processo = ProcessoHome.instance().getInstance();
			if((processo.getNumeroProcessoTemp() != null && numeroProcessoJaCadastrado(processo))){
					this.textosProtocolacao[INDEX_ERRO] = "número do processo de origem jé cadastrado!";
					validado = false;
			}else if (validaSeProcessoEproc(processo)) {
				validado = false;
			} else {
				PontoExtensaoResposta pontoExtensaoResposta = CustasJudiciaisHome.instance().salvarDadosCustasProtocolar();
				if (CodigoRespostaGuiaRecolhimentoEnum.INVALIDO.getLabel().equals(pontoExtensaoResposta.getCodigo())) {
					throw new PJeBusinessException(pontoExtensaoResposta.getMensagem());
				}
				getEntityManager().lock(getInstance(), LockModeType.PESSIMISTIC_WRITE);
				getEntityManager().refresh(getInstance());
				protocolar();
				consolidarDocumentoIdentificacao();
				validado = true;
			}
		} catch (Exception ex){
			try{
				log.error("Erro ao protocolar processo com ID " + getInstance().getIdProcessoTrf() + ": " + ex.getLocalizedMessage());
				Util.rollbackTransaction();
				setInstance(EntityUtil.getEntityManager().find(ProcessoTrf.class,getInstance().getIdProcessoTrf()));
				compClasseProcessoTrfList = null;
				protocolarDocumentoBean = null;
				this.atendimentoEmPlantao = false;
			} catch (Exception e1){
				log.error("Erro ao realizar rollback: " + e1.getLocalizedMessage());
			}
		}
		if(validado) {
			Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_ATUALIZAR_CAIXAS_PROCURADORES, getInstance());
			
			FacesMessages.instance().clear();
		    try {
		       org.jboss.seam.transaction.UserTransaction ut = Transaction.instance();
		       if (ut != null && ut.isActive()) {
					getEntityManager().flush();
		       }
		       getEntityManager().clear();
		       Conversation.instance().end();
		    } catch (Exception e) {}

		    Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_ATUALIZAR_GUIA_RECOLHIMENTO_POS_PROTOCOLAR,
					getInstance(), CustasJudiciaisHome.instance().getNumeroGuia());
		}

		FacesMessages.instance().add(Severity.INFO, getMensagemProtocolacao(false));
		return validado;
	}
	
	/**
	 * Metodo responsável por atualizar a propriedade "temporario" da entidade {@link PessoaDocumentoIdentificacao} para falso.
	 * Desta forma, evita-se que o usuário externo que incluiu o documento de identificação o manipule.  
	 */
	private void consolidarDocumentoIdentificacao() {
		PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager = ComponentUtil.getComponent("pessoaDocumentoIdentificacaoManager");
		
		List<PessoaDocumentoIdentificacao> documentosIdentificacao = 
				pessoaDocumentoIdentificacaoManager.recuperarDocumentosTemporarios(getInstance().getProcessoParteList());
		
		for (PessoaDocumentoIdentificacao documentoIdentificacao : documentosIdentificacao) {
			documentoIdentificacao.setTemporario(Boolean.FALSE);
			getEntityManager().persist(documentoIdentificacao);
		}
	}
	
	public boolean validarClet() {
		if (possuiIncidentesSemJulgamentoClet != null && possuiIncidentesSemJulgamentoClet) {
			String erro = "Sé podem ser cadastrados, via CLET, os processos que Não tenham incidentes processuais sem julgamento!";
			this.textosProtocolacao[INDEX_ERRO] = erro;
			return false;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from Processo o ");
		sb.append("where o.numeroProcesso = :numeroProcesso ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("numeroProcesso", NumeroProcessoUtil.mascaraNumeroProcesso(getNumeroProcessoClet()));
		long result = (Long) q.getSingleResult();
		if (result > 0) {
			String erro = "Processo jé existente ou inválido!";
			this.textosProtocolacao[INDEX_ERRO] = erro;
			return false;
		}

		if(validar()){
			getInstance().setDataAutuacao(getProcessoClet().getDataAjuizamento());
			getEntityManager().merge(getInstance());
			getEntityManager().flush();
			return true;
		}
		return false;
	}
	
	public String getMensagemCertidaoProtocolacao(){
		TipoAudiencia tipoAudienciaAutomatica = tipoAudienciaPadrao();
		ProcessoAudiencia audiencia = ProcessoJudicialManager.instance().getProximaAudienciaDesignada(getInstance());
		String mensagem = null;
		if (audiencia != null){
			mensagem = String.format(TEXTO_AUDIENCIA_AGENDADA + "\n" + TEXTO_AVISO_CLT.replace("\n", ""),
					((tipoAudienciaAutomatica == null)?audiencia.getTipoAudiencia():tipoAudienciaAutomatica), audiencia.getDtInicioFormatada());
		}
		else if ((new Character('D')).equals(this.instance.getViolacaoFaixaValoresCompetencia())){
			mensagem = TEXTO_AUDIENCIA_NAO_AGENDADA + "\nAtenção: " + TEXTO_AVISO_VALOR_CAUSA.replace("\n", "");
		}
		else{
			mensagem = TEXTO_AUDIENCIA_NAO_AGENDADA;
		}
		return mensagem;
	}
	
	public String getTextoResultadoAudienciaCertidao(Integer idProcessoDocumento){

		if (isPeticaoInicial(idProcessoDocumento)){
			if (this.instance.isNumerado()){
				ProcessoAudiencia inicial = getPrimeiraAudienciaInicial();

				if (inicial != null){
					return String.format("\n" + TEXTO_AUDIENCIA_AGENDADA + "\n" + TEXTO_AVISO_CLT.replace("\n", ""),
							inicial.getTipoAudiencia(), inicial.getDtInicioFormatada());
				}
				else{
					if ((new Character('D')).equals(this.instance.getViolacaoFaixaValoresCompetencia())){
						return TEXTO_AUDIENCIA_NAO_AGENDADA + "\nAtenção: " + TEXTO_AVISO_VALOR_CAUSA.replace("\n", "");
					}
					if (tipoAudienciaPadrao() == null){
						return TEXTO_AUDIENCIA_NAO_AGENDADA + "\nMotivo: "
							+ TEXTO_AVISO_TIPO_INDEFINIDO.replace("\n", "");
					}
					return TEXTO_AUDIENCIA_NAO_AGENDADA;
				}
			}
			else{
				return "O processo ainda Não foi protocolado.";
			}
		}
		return "";
	}
	
	@SuppressWarnings("unchecked")
	public ProcessoAudiencia getPrimeiraAudienciaInicial(){
		String query = "select distinct o from ProcessoAudiencia o where "
			+ "o.processoTrf.idProcessoTrf = :idProcessoTrf and " + "o.statusAudiencia <> :cancelada and "
			+ "o.statusAudiencia <> :redesignada and " + "o.statusAudiencia <> :naoRealizada and "
			+ "o.statusAudiencia <> :convertida and " + "o.tipoAudiencia.tipoAudiencia = 'Inicial' "
			+ "order by o.dtInicio asc";

		Query q = getEntityManager().createQuery(query);
		q.setParameter("idProcessoTrf", instance.getIdProcessoTrf());
		q.setParameter("cancelada", StatusAudienciaEnum.C);
		q.setParameter("redesignada", StatusAudienciaEnum.R);
		q.setParameter("naoRealizada", StatusAudienciaEnum.N);
		q.setParameter("convertida", StatusAudienciaEnum.D);
		q.setMaxResults(1);
		List<ProcessoAudiencia> result = q.getResultList();
		if (result != null && result.size() > 0){
			return result.get(0);
		}

		return null;
	}
	
	private boolean isPeticaoInicial(Integer idProcessoDocumento){
		return idProcessoDocumento == this.getInstance().getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento();
	}
	
	private void verificarPrevencao() throws PontoExtensaoException, PrevencaoException {
		ProcessoTrf processoTrf = getInstance();
		if (Boolean.TRUE.equals(processoTrf.getIsIncidente()) && processoTrf.getProcessoReferencia() != null) {
			processoTrf.setProcessoOriginario(processoTrf.getProcessoReferencia());
		}
		PrevencaoService prevencaoService = PrevencaoService.instance();
		prevencaoService.verificarPrevencao(processoTrf);
	}

	private void autuarProcesso() throws PJeBusinessException{
		try{
			AutuacaoService.instance().autuarProcesso(getInstance());
		} catch (Exception e){
			refreshGridDocumento();
			this.textosProtocolacao[INDEX_ERRO] = String.format(
				"Erro ao autuar processo: \n%s", e.getMessage() != null ? e.getMessage() : StringUtils.EMPTY);
			
			throw new PJeBusinessException(this.textosProtocolacao[INDEX_ERRO]);
		}
	}

	private boolean numerarProcesso(ProcessoTrf processoJudicial) throws Exception {
		try {
			if(!cadastroProcessoClet){
				AutuacaoService.instance().numerarProcesso(getInstance());
			}else{
				String numeroProcessoAux = getNumeroProcessoClet();
				numeroProcessoAux = numeroProcessoAux.replace("-", "");
				numeroProcessoAux = numeroProcessoAux.replace(".", "");
				
				String numeroSequencial = numeroProcessoAux.substring(0, 7);
				numeroProcessoAux = numeroProcessoAux.substring(7);
				
				String digitoVerificador = numeroProcessoAux.substring(0,2);
				numeroProcessoAux = numeroProcessoAux.substring(2);
				
				String ano = numeroProcessoAux.substring(0,4);
				numeroProcessoAux = numeroProcessoAux.substring(4);
				
				String numeroOrgaoJustica = numeroProcessoAux.substring(0,3);
				numeroProcessoAux = numeroProcessoAux.substring(3);
				
				String numeroOrigem = numeroProcessoAux;
				
				processoJudicial.setAno(Integer.parseInt(ano));
				processoJudicial.setNumeroSequencia(Integer.parseInt(numeroSequencial));
				processoJudicial.setNumeroOrgaoJustica(Integer.parseInt(numeroOrgaoJustica));
				processoJudicial.setNumeroOrigem(Integer.parseInt(numeroOrigem));
				processoJudicial.setNumeroDigitoVerificador(Integer.parseInt(digitoVerificador));
				getEntityManager().merge(processoJudicial);
				processoJudicial.getProcesso().setNumeroProcesso(getNumeroProcessoClet());
				getEntityManager().merge(processoJudicial.getProcesso());
				getEntityManager().flush();
			}
		} 
		catch (PJeBusinessException e){
			String errorMessage = FacesUtil.getMessage("entity_messages", e.getMessage(), e.getParams());
			this.textosProtocolacao[INDEX_ERRO] = String.format("Erro ao autuar processo: \n%s", errorMessage);
			throw new Exception(this.textosProtocolacao[INDEX_ERRO]);
		}
		return true;
	}

	private boolean distribuirProcesso() throws PJeBusinessException {
		Competencia c = getCompetenciaConflito();
		if (c != null && !getEntityManager().contains(c)) {
			setCompetenciaConflito(getEntityManager().find(Competencia.class, c.getIdCompetencia()));
		  	c = getCompetenciaConflito();
		}
		if (c == null){
			if (competenciasPossiveis != null && competenciasPossiveis.size() > 1){
				this.textosProtocolacao[INDEX_ERRO] = String
						.format("Favor selecionar uma das competências possíveis para viabilizar a Distribuição do processo.");
				throw new PJeBusinessException(this.textosProtocolacao[INDEX_ERRO]);
			}
			else{
				this.textosProtocolacao[INDEX_ERRO] = String
						.format("Não há competência possével nesta jurisdição para o processo tal como ele está cadastrado.");
				throw new PJeBusinessException(this.textosProtocolacao[INDEX_ERRO]);
			}
		}
		
		try{
			ClasseJudicial classeJudicial = getInstance().getClasseJudicial();

			// Caso seja permitido ao usuário escolher uma classe judicial sem que exista
			// um fluxo específico associado a esta classe judicial o processo deve seguir o
			// fluxo padrao cadastrado na tabela de parametros.
			Fluxo fluxo = classeJudicial.getFluxo();

			if (null == fluxo) {
				log.error("Nao foi possivel obter o fluxo associado a classe judicial ("
						+ classeJudicial.getCodClasseJudicial() + " - " + classeJudicial.getClasseJudicialSigla()
						+ "). Usando o fluxo padrao.");
				fluxo = ParametroUtil.instance().getFluxoPadrao();
			}
			executarDistribuicao(fluxo, getInstance().getDataAutuacao(), c);
		} catch (Exception e){
			log.error("Erro ao distribuir incidental: " + e);
			this.textosProtocolacao[INDEX_ERRO] = String.format("Erro ao distribuir processo: \n%s", e.getMessage());
			throw new PJeBusinessException(this.textosProtocolacao[INDEX_ERRO], e);
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean existeConflitoCompetenciaAssunto(List<AssuntoTrf> assuntos, AssuntoTrf assunto){
		if (assuntos.size() == 0){
			return false;
		}

		String query = "select distinct o.competencia from CompetenciaClasseAssunto o "
			+ "where o.classeAplicacao.classeJudicial = :classeJudicial " + "and o.assuntoTrf in (:assuntos)";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("classeJudicial", getInstance().getClasseJudicial());
		q.setParameter("assuntos", assuntos);
		List<Competencia> list = q.getResultList();

		query = "select distinct o.competencia from CompetenciaClasseAssunto o "
			+ "where o.classeAplicacao.classeJudicial = :classeJudicial " + "and o.assuntoTrf = :assunto";
		Query q1 = getEntityManager().createQuery(query);
		q1.setParameter("classeJudicial", getInstance().getClasseJudicial());
		q1.setParameter("assunto", assunto);
		List<Competencia> list1 = q1.getResultList();

		for (Competencia c : list1){
			if (!list.contains(c)){
				return true;
			}
		}
		return false;
	}

	public List<Competencia> getCompetenciasProcesso(){
		return getCompetenciasProcesso(getInstance());
	}
	
	public List<Competencia> getCompetenciasProcesso(ProcessoTrf processoTrf){
		try{
			Jurisdicao jurisdicao = getInstance().getJurisdicao();
			
			if (ProcessoTrfRedistribuicaoHome.instance().getJurisdicaoRedistribuicao() != null) {
				jurisdicao = ProcessoTrfRedistribuicaoHome.instance().getJurisdicaoRedistribuicao();
			}
			
			return AutuacaoService.instance().recuperaCompetenciasPossiveis(getInstance(), jurisdicao);
		} catch (IllegalArgumentException e){
			FacesMessages.instance().add(Severity.ERROR, String.format(e.getMessage()));
			return null;
		}
	}

	public List<ClasseJudicial> getClasseItens(ClasseJudicialInicialEnum classeJudicialInicialEnum){
		List<ClasseJudicial> result = new ArrayList<>();
		
		if (this.instance.getJurisdicao() != null){
			result = getListClasseJudicial(classeJudicialInicialEnum);
		}
		
		return result;
	}
	
	public List<ClasseJudicial> getListClasseJudicial(ClasseJudicialInicialEnum classeJudicialInicialEnum) {
		List<ClasseJudicial> result = new ArrayList<>();
		
		if (classeJudicialInicialEnum != null && isAplicacaoColegiada()) {
			switch (classeJudicialInicialEnum) {
				case R:
					result = classesJudiciaisRetificacaoAutos();
					break;
				case D:
					result = classesJudiciaisIncidentais();
					break;
				case I:
					result = classesJudiciaisIniciais();
					break;
			}
		} else {
			result = this.getClassesJudiciaisNovoProcessoPrimeiroGrau(ParametroUtil.instance().isJusPostulandi());
		}
		
		return result;
	}
	
	public List<Estado> getEstadosPorJurisdicaoCompetenciaAtiva() {
		List<Estado> result = ComponentUtil.getComponent(EstadoManager.class).recuperarPorJurisdicaoCompetenciaAtiva();
		if (result.size() == 1) {
			this.estadoJurisdicao = result.get(0);
		}
		return result;
	}
	
	public List<Municipio> getMunicipiosPorEstadoComJurisdicaoCompetenciaAtiva() {
		List<Municipio> result = new ArrayList<>();
		if (this.estadoJurisdicao != null) {
			result = ComponentUtil.getComponent(MunicipioManager.class)
				.recuperarPorEstadoComJurisdicaoCompetenciaAtiva(this.estadoJurisdicao.getIdEstado());
			
			if (result.size() == 1) {
				this.municipioJurisdicao = result.get(0);
			}
		}
		return result;
	}
	
	public List<Municipio> getMunicipiosJurisdicao() {
		List<Municipio> result = new ArrayList<>();
		if (this.instance != null && this.instance.getJurisdicao() != null) {
			result = ComponentUtil.getComponent(MunicipioManager.class)
				.recuperarPorJurisdicao(this.instance.getJurisdicao().getIdJurisdicao());
			
			if (result.size() == 1) {
				this.municipioJurisdicao = result.get(0);
			}
		}
		return result;
	}
	
	public List<CompetenciaAreaDireito> getAreasDireito() {
		return ProcessoJudicialManager.instance().recuperarAreasDireito(this.municipioJurisdicao);
	}

	/**
	 * Verifica se a instância da aplicação é segundo ou terceiro grau.
	 * @return verdadeiro se a instância da aplicação for segundo ou terceiro grau.
	 */
	private boolean isAplicacaoColegiada() {
		return !ParametroUtil.instance().isPrimeiroGrau();
	}

	public List<Jurisdicao> getJurisdicoes() {
		List<Jurisdicao> result = new ArrayList<>();
		if (this.instance.getIdAreaDireito() != null) {
			result = JurisdicaoManager.instance().recuperarJurisdicoes(this.instance.getIdAreaDireito(), this.municipioJurisdicao);
			
			if (result.size() == 1) {
				this.instance.setJurisdicao(result.get(0));
			}
		} else if (this.instance.getJurisdicao() != null) {
			// Para o caso de processo Não protocolado o qual foi cadastrado antes da inclusão do campo "Matéria".
			result = JurisdicaoManager.instance().recuperarJurisdicoes();
		}
		return result;
	}

	// Uso do complemento 10966 mudança de CLasse Processual de #{classe judicial} para #{nova classe processual}
	// Esse Metodo é chamado por uma EL em banco, favor Não alterar
	public List<ClasseJudicial> getListClasseJudicial(){
		return classesJudiciaisRetificacaoAutos();
	}

	private List<ClasseJudicial> getClassesJudiciaisNovoProcessoPrimeiroGrau(Boolean jusPostulandi) {
		return this.getClasseJudicialManager().recuperarClassesJudiciaisNovoProcessoPrimeiroGrau(
				this.instance.getIdAreaDireito(), jusPostulandi, getJurisdicao(), getClasseJudicialFiltro());
	}

	/**
	 * Recupera as classes judiciais iniciais.
	 * @return List<ClasseJudicial>
	 */
	public List<ClasseJudicial> classesJudiciaisIncidentais(){
		return this.getClasseJudicialManager().recuperarClassesJudiciaisIncidentais(getJurisdicao());
	}

	@SuppressWarnings("unchecked")
	public List<ClasseJudicial> getOrgaoJulgadorColegiadoItens(){
		String sql = "select o from OrgaoJulgadorColegiado o " + "where o.ativo = true "
			+ "and o in (select oj from OrgaoJulgadorColegiado oj "
			+ "          inner join oj.orgaoJulgadorColegiadoCompetenciaList orgComp "
			+ "          where orgComp.competencia.idCompetencia = :idCompetenciaEscolhida "
			+ "          and oj.ativo = true " + "          and current_date >= orgComp.dataInicio "
			+ "          and (orgComp.dataFim >= current_date or orgComp.dataFim is null) ) "
			+ "and o.jurisdicao.idJurisdicao = :idJurisdicao " + "order by o.orgaoJulgadorColegiado";

		Query query = getEntityManager().createQuery(sql);
		query.setParameter("idCompetenciaEscolhida", getCompEscolhida() == null ? getCompetencia(getInstance())
				.getIdCompetencia() : getCompEscolhida().getIdCompetencia());
		query.setParameter("idJurisdicao", getInstance().getJurisdicao().getIdJurisdicao());
		return query.getResultList();
	}

	@Observer({EventsTreeHandler.GRAVAR_ALERTA_EVENT})
	public void gravarAlertas(){
		// recuperando eventos com o check de ICR marcado
		List<EventoBean> eventos = EventsTreeHandler.instance().getEventosCriminaisSelected();
		ProcessoAlertaManager procAlertaManager = ComponentUtil.getComponent(ProcessoAlertaManager.class);
		try{
			for (EventoBean evento : eventos){
				
				String textoAlerta = "O evento '" + evento.getIdEvento() + "', do processo né "
						+ ProcessoTrfHome.instance().getInstance().getNumeroProcesso() + ", possui uma pendência de ICR";
				
				procAlertaManager.incluirAlertaAtivo(instance, textoAlerta, CriticidadeAlertaEnum.A);
			}
			procAlertaManager.flush();
		}catch (PJeBusinessException e){
			throw new PJeRuntimeException("Erro ao tentar incluir alerta.", e);
		}
	}

	/**
	 * Recupera as classes judiciais retificação de autos.
	 * @return List<ClasseJudicial>
	 */
	public List<ClasseJudicial> classesJudiciaisRetificacaoAutos() {
		return this.getClasseJudicialManager().recuperarClassesJudiciaisRetificacaoAutos(getJurisdicao());
	}

	/**
	 * Recupera as classes judiciais iniciais.
	 * @return List<ClasseJudicial>
	 */
	public List<ClasseJudicial> classesJudiciaisIniciais() {
		return this.getClasseJudicialManager().recuperarClassesJudiciaisIniciais(getJurisdicao());
	}

	/**
	 * Verifica se o município selecionado na aba 'Local do fato principal' está entre os municípios da jurisdição escolhida na aba 'Dados iniciais'
	 */
	public void verificarConflitoTerritorialFatoPrincipal(){
		setEstadoMunicipioFatoPrincipal(getInstance().getMunicipioFatoPrincipal().getEstado());
		for (JurisdicaoMunicipio jm : getInstance().getJurisdicao().getMunicipioList()){
			if (jm.getMunicipio().equals(getInstance().getMunicipioFatoPrincipal()))
				return;
		}
		FacesMessages
				.instance()
				.add(Severity.ERROR,
						String.format("Atenção: O local do fato principal informado (#{processoTrfHome.instance.municipioFatoPrincipal}) Não está na jurisdição escolhida (#{processoTrfHome.instance.jurisdicao})."));
	}

	public void setEstadoMunicipioFatoPrincipal(Estado estadoMunicipioFatoPrincipal){
		this.estadoMunicipioFatoPrincipal = estadoMunicipioFatoPrincipal;
	}

	public Estado getEstadoMunicipioFatoPrincipal(){
		return estadoMunicipioFatoPrincipal;
	}

	public Boolean getEhClasseCriminal(){
		if (ehClasseCriminal == null)
			this.iniciarClasseJudicial();
		return ehClasseCriminal;
	}

	public void setEhClasseCriminal(Boolean ehClasseCriminal){
		this.ehClasseCriminal = ehClasseCriminal;
	}

	public void setSelectedRowAssuntoPrincipal(ProcessoAssunto processoAssunto){
		if (processoAssunto.getAssuntoTrf().getComplementar() == true) {
			FacesMessages.instance().add(Severity.INFO, "Assunto complementar Não pode ser  marcado como principal!");
		} else {
			this.selectedRowAssuntoPrincipal = processoAssunto.getAssuntoTrf();
			for (ProcessoAssunto assunto : getInstance().getProcessoAssuntoList()) {
				if (assunto.getIdProcessoAssunto() == processoAssunto.getIdProcessoAssunto()) {
					assunto.setAssuntoPrincipal(true);
				} else {
					assunto.setAssuntoPrincipal(false);
				}
 			}
			getEntityManager().flush();
 		}
	}

	/**
	 * Remove o assunto da lista de assuntos do processo.
	 * 
	 * @param obj
	 * @param gridId
	 * @throws PJeBusinessException 
	 */
	public void removeProcessoAssunto(ProcessoAssunto obj, String gridId) throws PJeBusinessException{
		if (getInstance() != null){

			if (getInstance().getProcessoAssuntoList().size() >= 1){
				for (ProcessoAssunto pa : getInstance().getProcessoAssuntoList()){
					if (pa.equals(obj) && pa.getAssuntoPrincipal()){
						FacesMessages.instance().add(StatusMessage.Severity.ERROR,
								"Não é possével remover o assunto principal.");
						return;
					}
				}
			}

			getInstance().getProcessoAssuntoList().remove(obj);
			getEntityManager().remove(obj);
			getEntityManager().persist(getInstance());
			getEntityManager().flush();
			getEntityManager().refresh(getInstance());
			
			/*
		 	 * Verifica modificação feita na lista de assunto e atualiza a cadeia de prevenção 260
		 	 */
			retificarVinculacaoDependenciaEleitoral();

			refreshGrid(gridId);
			refreshGrid("processoAssuntoViewGrid");
			refreshGrid(GRID_ASSUNTO_CAD_PROCESSO);
			refreshGrid("processoAssuntoGrid");
		}
	}

	public void iniciarClasseJudicial(){
		ClasseJudicial classeJudicial = getInstance().getClasseJudicial();
		this.ehClasseCriminal = false;
		this.ehClasseInfracional = false;
		if (classeJudicial != null && classeJudicial.getIdClasseJudicial() != 0){
			this.ehClasseCriminal = this.getClasseJudicialManager().isClasseCriminal(classeJudicial);
			this.ehClasseInfracional = this.getClasseJudicialManager().isClasseInfracional(classeJudicial);
		}
	}
	
	public boolean validaSeProcessoEproc(Processo processo){
		if(processo.getNumeroProcessoTemp() != null) {
			boolean numeroProcTemp =  ProcessoService.instance().getNumProc(processo.getNumeroProcessoTemp());
			if(numeroProcTemp){	
					FacesMessages.instance().clear();
					FacesMessages.instance().add(StatusMessage.Severity.ERROR, "O Processo com o número informado ("+processo.getNumeroProcessoTemp()+") já se encontra cadastrado no EPROC !");
					return true;
			}
		}
		return false;
	}

	/**
	 * @return Retorna uma lista de Strings no formato "&lt;nome&gt;&lt;ws&gt;CPF:&lt;ws&gt;&lt;numero_cpf&gt;" onde &lt;ws&gt; \E9 o espa\E7o em
	 *         branco<br/>
	 *         Este m\E9todo n\E3o pode ficar em ProcessoTrf, por este ser entidade.
	 */
	private List<String> obterListaNomesCpfAutor(){
		List<String> cpfAutores = new ArrayList<String>();
		String cpfCorrente = null;
		for (ProcessoParte pp : this.getInstance().getListaParteAtivo()){
			String cpfAutor = new String();
			cpfAutor = pp.getNomeParte();
			cpfAutor = cpfAutor.concat(" CPF: ");
			cpfCorrente = pp.getPessoa().getDocumentoCpfCnpj();
			cpfAutor = cpfAutor.concat(cpfCorrente == null ? "Não informado" : cpfCorrente);

			cpfAutores.add(cpfAutor);
		}
		return cpfAutores;
	}

	/**
	 * @return retorna um \FAnico string contendo uma lista de nomes-parte e cpf, separados por v\EDrgula.
	 */
	public String getNomeCpfAutorList(){
		String lista = new String();
		List<String> cpfAutores = this.obterListaNomesCpfAutor();
		int tamLista = cpfAutores.size();
		for (int i = 0; i < tamLista; ++i){
			lista = lista.concat(cpfAutores.get(i));
			if (i < (tamLista - 1)) // n\E3o adicionar a \FAltima virgula
			{
				lista = lista.concat(", ");
			}
		}
		return lista;
	}

	public String getTipoAudiencia() {
		try{
			return ProcessoJudicialManager.instance().getProximaAudienciaDesignada(getInstance()).getTipoAudiencia().getTipoAudiencia();
		} catch (Exception ex){
			log.info("Nao ha proxima audiencia para o processo " + getInstance().getNumeroProcesso(), ex);
		}
		return null;
	}

	public String getSalaAudiencia() {
		try{
			return ProcessoJudicialManager.instance().getProximaAudienciaDesignada(getInstance()).getSalaAudiencia().getSala();
		} catch (Exception ex){
			log.info("Nao ha proxima audiencia para o processo " + getInstance().getNumeroProcesso(), ex);
		}
		return null;
	}

	public String getEnderecoSalaAudiencia(){
		try{
			return ProcessoJudicialManager.instance().getProximaAudienciaDesignada(getInstance()).getSalaAudiencia()
					.getOrgaoJulgador().getLocalizacao().getEnderecoCompleto().getEnderecoCompleto();
		} catch (Exception ex){
			log.info("Nao ha proxima audiencia para o processo " + getInstance().getNumeroProcesso(), ex);
		}
		return null;
	}

	/**
	 * @return Data e hora da sala da proxima audiencia do processo no formato dd/MM/yyyy HH:mm:ss.
	 */
	public String getDataAudiencia(){
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

		try{
			StringBuilder sb = new StringBuilder();
			for (ProcessoAudiencia processoAudiencia : instance.getProcessoAudienciaList()){
				if (processoAudiencia.getStatusAudiencia().equals(StatusAudienciaEnum.M)){
					return sdf.format(processoAudiencia.getDtInicio());
				}
			}
			return sb.toString();
		} catch (Exception ex){
			log.info("Não há proxima audiência para o processo " + getInstance().getNumeroProcesso(), ex);
		}

		return null;
	}

	public String validarCaracteristicasDoProcesso(){
		setTelaCaracteristicaProcesso(true);
		ProcessoTrf proc = getInstance();
		ClasseJudicial classe = proc.getClasseJudicial();
		if (classe != null && classe.getControlaValorCausa()){
			Double minimo = classe.getPisoValorCausa();
			Double maximo = classe.getTetoValorCausa();
			Double vc = proc.getValorCausa();
			if (vc == null || vc.compareTo(0.0) == 0){
				FacesMessages
						.instance()
						.add(Severity.ERROR, "Para a classe {0}, é necessério indicar o valor da causa.", classe.getClasseJudicial());
			}
			else if (maximo == null && vc.compareTo(minimo) < 0){
				FacesMessages
						.instance()
						.add(Severity.ERROR, "O valor da causa indicado é menor que o limite ménimo ({0}) estipulado para a classe [{1}]. ",
								minimo.toString(), classe.getClasseJudicial());
				proc.setViolacaoFaixaValoresCompetencia('D');
				if (!classe.getDesignacaoAudienciaErroValorCausa()){
					setFaixaIncopativelAudiencia(true);
				}
			}
			else if (vc.compareTo(minimo) < 0 || (maximo != null && vc.compareTo(maximo) > 0)){
				FacesMessages
						.instance()
						.add(Severity.ERROR, "O valor da causa indicado é incompatével com os limites ({0} - {1}) estipulados para a classe [{2}]. ",
								minimo.toString(), maximo.toString(), classe.getClasseJudicial());
				proc.setViolacaoFaixaValoresCompetencia('D');

				if (!classe.getDesignacaoAudienciaErroValorCausa()){
					setFaixaIncopativelAudiencia(true);
				}
			}
			else{
				setFaixaIncopativelAudiencia(false);
				proc.setViolacaoFaixaValoresCompetencia(null);
			}
		}
		return this.update();
	}
	
	public void gravarCaracteristicasClet(){
		if(getProcessoClet().getNaturezaClet() == null || getProcessoClet().getNaturezaClet().getIdNaturezaClet() == null || getProcessoClet().getNaturezaClet().getIdNaturezaClet().equals(0)){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Deve ser informada uma natureza vélida");
			return;
		}
		if(!getProcessoClet().getDataAjuizamento().before(DateUtil.getDataSemHora(new Date()))){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "A data do ajuizamento deve ser menor que a data atual");
			return;
		}
		if(!getInstance().getDtTransitadoJulgado().after(getProcessoClet().getDataAjuizamento())){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "A data do trénsito em julgado deve ser maior que a data do ajuizamento");
			return;
		}
		if(getInstance().getDtTransitadoJulgado().after(DateUtil.getDataSemHora(new Date()))){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "A data do trénsito em julgado deve ser menor ou igual a data atual");
			return;
		}
		if(getProcessoClet().getDataInicioClet().before(getInstance().getDtTransitadoJulgado())){
			StringBuilder msgErro = new StringBuilder();
			msgErro.append("A data de inécio da ");
			if(getProcessoClet().getNaturezaClet().getTipoNatureza().equals(TipoNaturezaCletEnum.L)){
				msgErro.append("liquidaééo");
			}else{
				msgErro.append("Execução");
			}
			msgErro.append(" deve ser maior ou igual a data do trénsito em julgado");
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, msgErro.toString());
			return;
		}
		getEntityManager().merge(getInstance());
		getEntityManager().merge(getProcessoClet());
		getEntityManager().flush();
		if(getProcessoClet().getNaturezaClet().getTipoNatureza().equals(TipoNaturezaCletEnum.L)){
			refreshGrid("caracteristicaProcessoLiquidacaoGrid");
		}else{
			refreshGrid("caracteristicaProcessoExecucaoGrid");
		}
		FacesMessages.instance().clear();
		FacesMessages.instance().add("Registro alterado com sucesso");
	}

	public String updateClearMsg(){
		setTelaCaracteristicaProcesso(false);
		FacesMessages.instance().clear();
		return "ok";
	}

	public void setFaixaIncopativelAudiencia(boolean isFaixaIncopativelAudiencia){
		this.isFaixaIncopativelAudiencia = isFaixaIncopativelAudiencia;
	}

	public boolean isFaixaIncopativelAudiencia(){
		return isFaixaIncopativelAudiencia;
	}

	public void setTelaCaracteristicaProcesso(boolean isTelaCaracteristicaProcesso){
		this.isTelaCaracteristicaProcesso = isTelaCaracteristicaProcesso;
	}

	public boolean isTelaCaracteristicaProcesso(){
		return isTelaCaracteristicaProcesso;
	}

	public boolean isViolacaoFaixaValores(ProcessoTrf processoTrf) {
		if (processoTrf == null) {
			throw new IllegalArgumentException("O processo está nulo");
		}
		return processoTrf.getViolacaoFaixaValoresCompetencia() != null && processoTrf.getViolacaoFaixaValoresCompetencia() == 'D';
	}

	public boolean isViolacaoFaixaValores(){
		return isViolacaoFaixaValores(getInstance());
	}

	public boolean getPossuiAlertaProcesso(){

		if (possuiAlertaProcesso == null) {
			possuiAlertaProcesso = getAlertasProcesso().size() > 0;
		}

		return possuiAlertaProcesso;
	}
	
	public boolean possuiAlertaAtivoProcesso(ProcessoTrf processoTrf){
		setInstance(processoTrf);

		return getAlertasProcesso().size() > 0;
	}

	public Boolean getExibeAbaPermissoes(){

		if (exibeAbaPermissoes == null)
			exibeAbaPermissoes = exibeAbaPermissoes();

		return exibeAbaPermissoes;
	}

	public boolean hasMensagens(){
		return mensagemTutelaLiminar || mensagem || mensagemSigilo || mensagemPeticoes;
	}
	
	/**
	 * Metodo que retorna o Código HTML de uma tabela contendo todos os documentos do processo referenciado pelo atributo <code>this.instance</code>.
	 * As colunas séo: 'Título', 'Tipo' e 'Chave de acesso**'.
	 * 
	 * @return <code>String</code> contendo o Código HTML da tabela.
	 * 
	 */
	public String getTabelaHashDocumentos(){

		StringBuilder header = new StringBuilder();
		header.append("<p style='font-family: Arial, Verdana, Sans-serif; font-size: 8pt;'>Documentos associados ao processo</p>");
		header.append("<p style='font-family: Arial, Verdana, Sans-serif; font-size: 8pt;'>");
		header.append("<table style='border: 1px solid black; border-collapse:collapse;'>");
		header.append("<tr>");
		header.append("<th style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>Título</th>");
		header.append("<th style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>Tipo</th>");
		header.append("<th style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>Chave de acesso**</th>");
		header.append("</tr>");

		StringBuilder body = new StringBuilder();

		for (ProcessoDocumento doc : getListaOrdenadaDocumentos(this.instance.getProcesso().getProcessoDocumentoList())){
			//Devem ser listados apenas os documentos com assinatura validada, e que Não sejam sigilosos.
			if (doc.getProcessoDocumentoBin() != null && doc.getDataJuntada() != null && !doc.getDocumentoSigiloso()) {
				body.append("<tr>");
				body.append("<td style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>");
				body.append(doc.getProcessoDocumento());
				body.append("</td>");
				body.append("<td style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>");
				body.append(doc.getTipoProcessoDocumento().getTipoProcessoDocumento());
				body.append("</td>");
				body.append("<td style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>");
				body.append(ValidacaoAssinaturaProcessoDocumento.instance().getCodigoValidacaoDocumento(
						doc.getProcessoDocumentoBin()));
				body.append("</td>");
				body.append("</tr>");
		  	}
		}
		
		StringBuilder trailler = new StringBuilder();
		trailler.append("</table>");
		trailler.append("</p>");
		
		StringBuilder sb = new StringBuilder();
		sb.append(header.toString());
		sb.append(body.toString());
		sb.append(trailler.toString());

		return sb.toString();
	}
	

	/**
	 * Metodo que retorna o Código HTML de uma tabela contendo todos os documentos e Id do processo referenciado pelo atributo <code>this.instance</code>.
	 * As colunas séo: 'Título', 'Tipo' e 'Chave de acesso**'.
	 * issue PJEII-18383
	 * 
	 * @return <code>String</code> contendo o Código HTML da tabela.
	 * 
	 */
	public String getTabelaHashDocumentosComId(){

		StringBuilder header = new StringBuilder();
		header.append("<p style='font-family: Arial, Verdana, Sans-serif; font-size: 8pt;'>Documentos associados ao processo</p>");
		header.append("<p style='font-family: Arial, Verdana, Sans-serif; font-size: 8pt;'>");
		header.append("<table style='border: 1px solid black; border-collapse:collapse;'>");
		header.append("<tr>");
		header.append("<th style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>ID</th>");
		header.append("<th style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>Título</th>");
		header.append("<th style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>Tipo</th>");
		header.append("<th style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>Chave de acesso**</th>");
		header.append("</tr>");

		StringBuilder body = new StringBuilder();

		for (ProcessoDocumento doc : getListaOrdenadaDocumentos(this.instance.getProcesso().getProcessoDocumentoList())){
			//Devem ser listados apenas os documentos com assinatura validada, e que Não sejam sigilosos.
			if (doc.getProcessoDocumentoBin() != null && doc.getDataJuntada() != null && !doc.getDocumentoSigiloso() && doc.getUsuarioExclusao() == null ) {
		  		body.append("<tr>");
		  		body.append("<td style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>");
				body.append(ValidacaoAssinaturaProcessoDocumento.instance().getIdProcessoDocumento(doc.getProcessoDocumentoBin()));
				body.append("</td>");
				body.append("<td style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>");
				body.append(doc.getProcessoDocumento());
				body.append("</td>");
				body.append("<td style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>");
				body.append(doc.getTipoProcessoDocumento().getTipoProcessoDocumento());
				body.append("</td>");
				body.append("<td style='border: 1px solid black; padding-left: 5px; padding-right: 5px;'>");
				body.append(ValidacaoAssinaturaProcessoDocumento.instance().getCodigoValidacaoDocumento(doc.getProcessoDocumentoBin()));
				body.append("</td>");
				body.append("</tr>");
		  	}
		}
		
		StringBuilder trailler = new StringBuilder();
		trailler.append("</table>");
		trailler.append("</p>");
		
		StringBuilder sb = new StringBuilder();
		sb.append(header.toString());
		sb.append(body.toString());
		sb.append(trailler.toString());

		return sb.toString();
	}
	
	/**
	 * Metodo que retorna o nome e cpf da parte ativa numa string concatenada com o sinal ';'. Caso Não possua CPF irá retornar apenas o nome da parte.
	 * issue PJEII-18383
	 * 
	 * @return String 
	 * 
	 */
	public String getParteAtivoCPF(){
		String tmp = "";
			for (ProcessoParte doc : this.instance.getListaParteAtivo()){
			//Devem ser listados apenas os documentos com assinatura validada, e que Não sejam sigilosos.
			if (doc.getParteSigilosa() == false && doc.getIsBaixado() == false)  {
				if (doc.getPessoa().getDocumentoCpfCnpj() != null) {
					tmp = tmp +  doc.getNomeParte() +  "(" + InscricaoMFUtil.acrescentaMascaraMF( doc.getPessoa().getDocumentoCpfCnpj() )  + "); "; 
				} else {
			  		tmp = tmp +  doc.getNomeParte() + "; "; 
			  	}
			}	
			
		}
		return tmp.toString();
	}
	
	/**
	 * Metodo que retorna o nome e cpf da parte passiva numa string concatenada com o sinal ';'. Caso Não possua CPF iré retornar apenas o nome da parte.
	 * issue PJEII-18383
	 * 
	 * @return String 
	 * 
	 */
	public String getPartePassivoCPF(){

		String tmp = "";
		for (ProcessoParte doc : this.instance.getListaPartePassivo()){
		//Devem ser listados apenas os documentos com assinatura validada, e que Não sejam sigilosos.
		if (doc.getParteSigilosa() == false && doc.getIsBaixado() == false)  {
			if (doc.getPessoa().getDocumentoCpfCnpj() != null) {
				tmp = tmp +  doc.getPessoa().getNomeParte() +  "(" + InscricaoMFUtil.acrescentaMascaraMF( doc.getPessoa().getDocumentoCpfCnpj() )  + "); "; 
		  	} else {
		  		tmp = tmp +  doc.getPessoa().getNomeParte() + "; "; 
		  	}
		}	
	}
	return tmp.toString();
	}
	
	

	public ProcessoTrf carregarProcesso(AudImportacao ai){
		ProcessoTrf processoTrf = null;

		if (ai.isNovoProcesso()){
			processoTrf = getEntityManager().find(ProcessoTrf.class, ai.getIdProcesso());
		}
		else{
			String sql = "select p from ProcessoTrf p where p.processo.numeroProcesso = :num";
			long numOrgao = (ai.getOrgaoJustica() * 100) + (ai.getRegional());
			String numeroProcesso = NumeroProcessoUtil.formatNumeroProcesso(ai.getNumProcesso(), ai.getDvProcesso(),
					ai.getAnoProcesso(), numOrgao, 1);

			Query query = getEntityManager().createQuery(sql);
			query.setParameter("num", numeroProcesso);

			processoTrf = (ProcessoTrf) query.getSingleResult();
			ai.setProcessoTrf(processoTrf);
			ai.setIdProcesso(processoTrf.getIdProcessoTrf());
		}

		return processoTrf;
	}

	/**
	 * @return Entidade ProcessoJT associada
	 */
	public ProcessoJT getProcessoJT(){
		ProcessoJTHome processoJTHome = new ProcessoJTHome();
		processoJTHome.wire();
		return processoJTHome.getInstance();
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoAlerta> getAlertasProcesso(){
		StringBuilder query = new StringBuilder("from ProcessoAlerta pa where pa.processoTrf = :processo and pa.ativo = true and pa.alerta.ativo = true");
		
		OrgaoJulgador oj = Authenticator.getOrgaoJulgadorAtual();		
		OrgaoJulgadorColegiado ojc = Authenticator.getOrgaoJulgadorColegiadoAtual();
		
		if(oj != null){
			query.append("	AND pa.alerta.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ");
		}
		if(ojc != null){
			query.append(" AND pa.alerta.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = :idOrgaoJulgadorColegiado ");
		}

		Query q = getEntityManager().createQuery(query.toString());

		q.setParameter("processo", getInstance());
		
		if(oj != null){
			q.setParameter("idOrgaoJulgador", oj.getIdOrgaoJulgador());
		}
		if(ojc != null){
			q.setParameter("idOrgaoJulgadorColegiado", ojc.getIdOrgaoJulgadorColegiado());
		}
		
		List<ProcessoAlerta> processoAlertaList = q.getResultList();
		
		if(processoAlertaList.size() == 0){
			return processoAlertaList;
		}
		
		Collections.sort(processoAlertaList, new Comparator<ProcessoAlerta>(){

			@Override
			public int compare(ProcessoAlerta o1, ProcessoAlerta o2){

				// na ordem inversa
				/*
				 * Mudarão de comparação realizada para ordenar por data desc by thiago.vieira
				 */
				if (o1.getAlerta().getDataAlerta().after(o2.getAlerta().getDataAlerta())){
					return -1;
				}
				else if (o1.getAlerta().getDataAlerta().before(o2.getAlerta().getDataAlerta())){
					return 1;
				}
				else{
					return 0;
				}
			}
		});

		return processoAlertaList;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoAlerta> recuperaAlertasProcesso(){
		Query query;
		if(exibirTodosAlertas){
			query = getEntityManager().createQuery("from ProcessoAlerta pa where pa.processoTrf = ? ");
		} else{
			query = getEntityManager().createQuery("from ProcessoAlerta pa where pa.processoTrf = ? and pa.ativo = true");
		}
		query.setParameter(1, getInstance());

		List<ProcessoAlerta> processoAlertaList = query.getResultList();

		if(processoAlertaList.size() == 0){
			return processoAlertaList;
		}
		
		Collections.sort(processoAlertaList, new Comparator<ProcessoAlerta>(){

			@Override
			public int compare(ProcessoAlerta o1, ProcessoAlerta o2){

				// na ordem inversa
				/*
				 * Mudarão de comparação realizada para ordenar por data desc by thiago.vieira
				 */
				if (o1.getAlerta().getDataAlerta().after(o2.getAlerta().getDataAlerta())){
					return -1;
				}
				else if (o1.getAlerta().getDataAlerta().before(o2.getAlerta().getDataAlerta())){
					return 1;
				}
				else{
					return 0;
				}
			}
		});

		return processoAlertaList;
	}

	public void desativarAlerta(ProcessoAlerta processoAlerta){
		processoAlerta.setAtivo(false);
		getEntityManager().persist(processoAlerta);
		getEntityManager().flush();
	}

	/**
	 * @return retorna a data da Distribuição do processo no formato dd/MM/yyyy HH:mm:ss.
	 */
	public String getDataDistribuicao(){
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String data = null;
		try{
			data = sdf.format(this.instance.getDataDistribuicao());
		} catch (Exception e){
			log.error("Erro ao formatar data de Distribuição em ProcessoTrfHome.getDataDistribuicao(): " + e.getLocalizedMessage());
		}
		return data;
	}

	public boolean possuiCompetencia(int idCompetencia){
		ProcessoHome pHome = ProcessoHome.instance();
		int idProcesso = pHome.getInstance().getIdProcesso();
		ProcessoTrf processo = EntityUtil.getEntityManager().find(ProcessoTrf.class, idProcesso);
		return possuiCompetencia(idCompetencia, processo);
	}

	public boolean possuiCompetencia(int idCompetencia, ProcessoTrf processo){
		if (processo == null){
			return false;
		}
		for (OrgaoJulgadorCompetencia ojc : processo.getOrgaoJulgador().getOrgaoJulgadorCompetenciaList()){
			if (ojc.getDataFim() == null || (ojc.getDataFim() != null && !ojc.getDataFim().before(new Date()))){
				if (ojc.getCompetencia().getIdCompetencia() == idCompetencia){
					return true;
				}
			}
		}
		return false;
	}

	//Retorna mensagens de audiência de protocolaééo
	public String getMensagemAudienciaProtocolacao(){
		return this.textosProtocolacao[INDEX_AUDIENCIA];
	}
	
	//Retorna mensagens de AVISO de protocolaééo
	public String getMensagemAvisoProtocolacao(){
		return this.textosProtocolacao[INDEX_AVISO];
	}
	
	//Retorna mensagens de ERRO de protocolaééo
	public String getMensagemErroProtocolacao(){
		return this.textosProtocolacao[INDEX_ERRO];
	}

	public String getMensagemProtocolacao(boolean escapeHtml) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotEmpty(this.textosProtocolacao[INDEX_DISTRIBUICAO])){
			sb.append(this.textosProtocolacao[INDEX_DISTRIBUICAO]);
			sb.append(QUEBRA_LINHA_HTML);
		}
		if (StringUtils.isNotEmpty(this.textosProtocolacao[INDEX_AUDIENCIA]) && ParametroUtil.instance().isPrimeiroGrau()){
			sb.append(this.textosProtocolacao[INDEX_AUDIENCIA]);
			sb.append(QUEBRA_LINHA_HTML);
		}
		if (StringUtils.isNotEmpty(this.textosProtocolacao[INDEX_AVISO])) {
			sb.append(this.textosProtocolacao[INDEX_AVISO]);
			sb.append(QUEBRA_LINHA_HTML);
		}
		if (StringUtils.isNotEmpty(this.textosProtocolacao[INDEX_ERRO])) {
			sb.append(escapeHtml ? StringEscapeUtils.escapeHtml(this.textosProtocolacao[INDEX_ERRO])
					: this.textosProtocolacao[INDEX_ERRO]);
		}
		if (StringUtils.isBlank(sb.toString()) && isProcessoNovo()) {
			sb.append(escapeHtml
					? StringEscapeUtils
							.escapeHtml(Messages.instance().get("processoTrf.cadastrarIncidental.erro.assunto"))
					: Messages.instance().get("processoTrf.cadastrarIncidental.erro.assunto"));
		}

		return sb.toString();
	}

	public String getMensagemProtocolacao() {
		return getMensagemProtocolacao(true);
	}

	/**
	 * Metodo responsável por identificar se o processo tem número e assunto,
	 * com o intuito de distinguir se o usuário faz uma redistribuição ou
	 * protolocaééo
	 * 
	 * @return <code>Boolean</code>, <code>true</code> se o processo Não tiver
	 *         um assunto nem um número associado.
	 */
	private boolean isProcessoNovo() {
		return (StringUtils.isEmpty(instance.getNumeroProcesso()) && instance.getProcessoAssuntoList().size() == 0);
	}

	public boolean getHouveErroProtocolacao() {
		return !"".equals(this.textosProtocolacao[INDEX_ERRO]);
	}
	
	private void resetTextosProtocolacao(){
		for (int i = 0; i < this.textosProtocolacao.length; i++){
			this.textosProtocolacao[i] = "";
		}
	}

	/**
	 * Metodo que recupera no processo corrente o movimento mais recente que seja igual ao movimento informado e verifica se o movimento possui os
	 * complementos indicados nos parâmetros de entrada.
	 * 
	 * @param parametros - formado por "idEvento;movimentoComplementos[];valorMovimentoComplementos[];logicaEOU". Exemplo: '51;17|18;Valor1|Valor2;e'
	 *            idEvento - Código do movimento conforme tabela do CNJ movimentoComplementos (conjunto de valores separados por "|") - lista de tipos
	 *            de complemento (Metodo sé funciona para completos do tipo "doménio") valorMovimentoComplementos (conjunto de valores separados por
	 *            "|") - lista dos respectivos valores para tipos de complemento logicaEOU - logica para verificação da existência dos complementos.
	 *            Todos ("E") ou algum dos complementos ("OU"). Se Não vier nada nessa parte da string (ex:'51;17|18;Valor1|Valor2;'. OBS: o último
	 *            ";" Não poderá ser omitido. A string formada tem de ter exatamente trés ";"), a légica usada por padréo seré a do "E".
	 */
	@SuppressWarnings("unchecked")
	public boolean existeMovimentoCompletoTipoDominio(String parametros){
		String[] partesParametros = parametros.replaceAll(" ", "").split(";", -2);
		int idEvento = Integer.valueOf(partesParametros[0]);
		String[] movimentoComplementos = partesParametros[1].split("\\|", -2);
		String[] valorMovimentoComplementos = partesParametros[2].split("\\|", -2);

		String logicaEOU = partesParametros[3];

		if (movimentoComplementos.length != valorMovimentoComplementos.length){
			return false;
		}

		String query = "SELECT o FROM ProcessoEvento o " +
				"	WHERE o.processo = :processo " +
				"		AND o.evento.idEvento = :idEvento " +
				"		AND o.processoEventoExcludente is NULL " +
				"	ORDER BY o.dataAtualizacao DESC";

		Query q = getEntityManager().createQuery(query);
		q.setParameter("processo", this.instance.getProcesso());
		q.setParameter("idEvento", idEvento);

		List<ProcessoEvento> movimentos = q.getResultList();

		if (movimentos.size() <= 0)
			return false;

		String query2 = "SELECT o FROM ComplementoSegmentado o " +
				"	WHERE o.movimentoProcesso.idProcessoEvento = :idProcessoEvento";

		q = getEntityManager().createQuery(query2);
		q.setParameter("idProcessoEvento", movimentos.get(0).getIdProcessoEvento());
		List<ComplementoSegmentado> complementos = q.getResultList();

		if (logicaEOU != null && !logicaEOU.equals("") && logicaEOU.equalsIgnoreCase("ou")){
			for (int j = 0; j < movimentoComplementos.length; j++){
				int complemento = Integer.valueOf(movimentoComplementos[j]);
				String valorComplemento = valorMovimentoComplementos[j];

				for (int i = 0; i < complementos.size(); i++){
					ComplementoSegmentado complementoSegmentado = complementos.get(i);
					if( complementoSegmentado.getTipoComplemento().getCodigo().equalsIgnoreCase(String.valueOf(complemento)) 
							&& complementoSegmentado.getTexto().equalsIgnoreCase(valorComplemento))
					{
						return true;
					}
				}
			}
			return false;
		}
		else{
			for (int j = 0; j < movimentoComplementos.length; j++){
				
				
				
				
				int complemento = Integer.valueOf(movimentoComplementos[j]);
				String valorComplemento = valorMovimentoComplementos[j];
				for (int i = 0; i < complementos.size(); i++){
					ComplementoSegmentado complementoSegmentado = complementos.get(i);
					if (complementoSegmentado.getTipoComplemento().getCodigo().equalsIgnoreCase(String.valueOf(complemento)) &&
						complementoSegmentado.getTexto().equalsIgnoreCase(valorComplemento)){
						break;
					}
					if (i == complementos.size() - 1){
						return false;
					}
				}
			}

			return true;
		}
	}
	
	/**
	 * Recupera o Código do movimento mais recente que tenha sido lançado no processo 
	 * corrente.
	 * 
	 * @param codigosMovimentos os Códigos dos movimentos a serem verificados
	 * @return o Código do movimento mais recente, se existir na movimentação processual, ou -1
	 */
	@SuppressWarnings("unchecked")
	public String obterMovimentoMaisRecente(String... codigosMovimentos) {
		Processo processo = getInstance().getProcesso();
		List<String> codMovimentos = Arrays.asList(codigosMovimentos);
		
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoEvento o ");
		sb.append("where ");
		sb.append("o.processo = :processo ");
		sb.append("and o.evento.idEvento in ");
		sb.append("				(select e.idEvento from Evento e ");
		sb.append("				 where e.codEvento in (:codMovimentos)) ");
		sb.append("and o.processoEventoExcludente is null ");
		sb.append("order by o.dataAtualizacao desc ");
		
		Query query = EntityUtil.createQuery(sb.toString());
		query.setParameter("processo", processo);
		query.setParameter("codMovimentos", codMovimentos);
		
		List<ProcessoEvento> movimentosProcessuais = query.getResultList();
		
		if ((movimentosProcessuais != null)
				&& (movimentosProcessuais.size() > 0)) {
			Evento evento = movimentosProcessuais.get(0).getEvento();
			if(evento != null) {
				return evento.getCodEvento();
			} else {
				return "-1";
			}
		} else {
			return "-1";
		}
	}

	@SuppressWarnings("unchecked")
	public boolean existeMovimentoLancadoPorIdEvento(int idEvento){
		String query = "SELECT o FROM ProcessoEvento o " +
				"	WHERE o.processo = :processo " +
				"		AND o.evento.idEvento = :idEvento " +
				"		AND o.processoEventoExcludente is NULL " +
				"	ORDER BY o.dataAtualizacao DESC";

		Query q = getEntityManager().createQuery(query);
		q.setParameter("processo", this.instance.getProcesso());
		q.setParameter("idEvento", idEvento);

		List<ProcessoEvento> movimentos = q.getResultList();

		if (movimentos.size() > 0)
			return true;
		else
			return false;
	}
	
	public boolean possuiOrgaoJulgadorColegiado(){
		if (instance == null){
			Processo processo = ProcessoHome.instance().getInstance();
			log.info("Buscando ProcessoTrf de id: " + processo.getIdJbpm());
			ProcessoTrf processoTrf2 = getEntityManager().find(ProcessoTrf.class, processo.getIdProcesso());
			return processoTrf2.getOrgaoJulgadorColegiado() != null;
		}
		return instance.getOrgaoJulgadorColegiado() != null;
	}

	public List<Competencia> getCompetenciasPossiveis() {
		PesquisaProcessoParadigmaAction pesquisaProcessoParadigma = ComponentUtil.getComponent(PesquisaProcessoParadigmaAction.class);
		if (pesquisaProcessoParadigma.getOrigemSistema() != null && pesquisaProcessoParadigma.getOrigemSistema().equals(Constantes.ORIGEM_SISTEMA_DCP)) {
			return competenciasPossiveis.stream()
					.filter(competencia -> competencia.getIdCompetencia() == 2)
					.collect(Collectors.toList());
		}
		return competenciasPossiveis;
	}
	
	public List<Competencia> getCompetenciasPorJurisdicao(){
		Jurisdicao jurisdicao = null;
		if(ProcessoTrfRedistribuicaoHome.instance().getJurisdicaoRedistribuicao() != null) {
			jurisdicao = ProcessoTrfRedistribuicaoHome.instance().getJurisdicaoRedistribuicao();
			DefinicaoCompetenciaService definicaoCompetenciaService = 
					ComponentUtil.getComponent("definicaoCompetenciaService");
			return definicaoCompetenciaService.getCompetencias(getInstance(), jurisdicao);
		} else {
			return null;
		}
	}
	
	/**
	 * Metodo utilizado no segundo grau para processos sem jurisdicao.
	 */
	public void verificaJurisdicao() {
		setId(ProcessoHome.instance().getId());
		if(getInstance().getJurisdicao() == null){
			getInstance().setJurisdicao(ParametroUtil.instance().getJurisdicao());
			getEntityManager().merge(getInstance());
			getEntityManager().flush();
		}
	}
	
	/**
	 * metodo utilizado na alteração da classe judicial do processo.
	 * as informações do processo sao alteradas de acorco com as necessidades da classe judicial.
	 * ao ser alterada a classe judicial do processo, a lista de assuntos selecionados do mesmo deverá ser limpa(inclusive no banco de dados).
	 */
	public void atualizarClasseJudicial() {
		atualizaTipoParte();
		limparListaAssuntosAssociadosSelecionados();
		if (getInstance().getClasseJudicial() != null
				&& Boolean.TRUE.equals(getInstance().getClasseJudicial().getHabilitarMascaraProcessoReferencia())) {
			getInstance().setDesProcReferencia(NumeroProcessoUtil.mascaraNumeroProcesso(getInstance().getDesProcReferencia()));
		}
		isProcessoReferenciaValido();
	}
	
	/**
	 * Metodo para atualizar o tipo da parte ao alterar a classe judicial do processo
	 */
	public void atualizaTipoParte() {	
		limparNumeroProcessoTemp();
		ClasseJudicial classeJudicial = getInstance().getClasseJudicial();
		TipoParteManager tipoParteManager = ComponentUtil.getComponent(TipoParteManager.class);
		for (ProcessoParte processoParte : getInstance().getListaPartePrincipalAtivo()) {
			processoParte.setTipoParte(tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.A));
		}
		for (ProcessoParte processoParte : getInstance().getListaPartePrincipalPassivo()) {
			processoParte.setTipoParte(tipoParteManager.tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.P));
			//processoParte.setTipoParte(getInstance().getClasseJudicial().getPoloPassivo());
		}
		if(getInstance().getIdProcessoTrf() != 0){
			ProcessoParteHome.instance().setClasseJudicialAnterioExigeFiscalDaLei(
					this.classeJudicialAnterior != null ? this.classeJudicialAnterior.getExigeFiscalLei() : Boolean.FALSE);

			ProcessoParteHome.instance().verificarExigenciaFiscalDaLei(getInstance());

			refreshGrid("processoAbaParteTerceiroGrid");
		}
		setClasseJudicialAnterior(getInstance().getClasseJudicial());
	}
	
	/**
	 * metodo responsavel por resetar a lista de assuntos já selecionados no processo.
	 * a lista sera limpa caso exista e na persistencia do objeto ProcessoTrf será apagada do banco de dados.
	 */
	private void limparListaAssuntosAssociadosSelecionados() {	
		if(this.getDefinedInstance() != null && this.getDefinedInstance().getProcessoAssuntoList() != null && !this.getDefinedInstance().getProcessoAssuntoList().isEmpty()) {
			this.getDefinedInstance().getProcessoAssuntoList().clear();
		}
	}

	/**
	 * Realiza operações de preparaééo para a entrada na aba 'Partes'.
	 */
	public void loadAbaPartes(){
		super.setTab("tabPartes");
		refreshGridPartes();
	}
	
	/**
	 * Realiza 'refresh' em todas as grids relacionadas a partes:
	 * 'processoAbaPartePoloAtivoGrid'
	 * 'processoAbaPartePoloPassivoGrid'
	 * 'processoPoloAtivoGrid'
	 * 'processoPoloPassivoGrid'
	 * 'processoIncidentePoloAtivoGrid'
	 * 'processoIncidentePoloPassivoGrid'
	 */
	private void refreshGridPartes(){
		refreshGrid("processoAbaPartePoloAtivoGrid");
		refreshGrid("processoAbaPartePoloPassivoGrid");
		refreshGrid("processoPoloAtivoGrid");
		refreshGrid("processoPoloPassivoGrid");
		refreshGrid("processoIncidentePoloAtivoGrid");
		refreshGrid("processoIncidentePoloPassivoGrid");
	}
	
	public Boolean getAptoPauta() {
		return aptoPauta;
	}

	public void setAptoPauta(Boolean aptoPauta) {
		this.aptoPauta = aptoPauta;
	}	
	
	public void marcarAptoPautaJulgamento(){
		if(aptoPauta){
			getInstance().setSelecionadoPauta(true);
			getInstance().setSelecionadoJulgamento(false);
			gravarPauta();
		}else{
			getInstance().setSelecionadoJulgamento(true);
			getInstance().setSelecionadoPauta(false);
			gravarJulgamento();
		}
	}
	
	public void limparMarcacaoApto(){
		setAptoPauta(null);
	}
	
	public boolean isProcessoExecucaoLiquidacao(Integer idProcessoTrf){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from ProcessoClet o ");
		sb.append("where o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcessoTrf", idProcessoTrf);
		long result = (Long)q.getSingleResult();
		return result > 0;
	}
	
	public boolean isProcessoExecucaoLiquidacao(){
		return isProcessoExecucaoLiquidacao(getInstance().getIdProcessoTrf());
	}
	
	public boolean isProcessoExecucao(){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from ProcessoClet o ");
		sb.append("where o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("and o.naturezaClet.tipoNatureza = 'E' ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcessoTrf", getInstance().getIdProcessoTrf());
		long result = (Long)q.getSingleResult();
		return result > 0;
	}
	
	public boolean isProcessoLiquidacao(){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from ProcessoClet o ");
		sb.append("where o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("and o.naturezaClet.tipoNatureza = 'L' ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcessoTrf", getInstance().getIdProcessoTrf());
		long result = (Long)q.getSingleResult();
		return result > 0;
	}
	
	public void incluirClet() {
		if (possuiIncidentesSemJulgamentoClet != null && possuiIncidentesSemJulgamentoClet) {
			FacesMessages.instance().add(Severity.ERROR, "Só podem ser cadastrados, via CLET, os processos que Não tenham incidentes processuais sem julgamento!");
			return;
		}

		iniciarClasseJudicial();

		String msg = insert();

		if (msg != null){
			
			Processo processo = ProcessoHome.instance().getInstance();

			Redirect redirect = Redirect.instance();
			redirect.setViewId("/Processo/update.seam");
			int idProcessoTrf = processo.getIdProcesso();
			redirect.setParameter("idProcesso", idProcessoTrf);
			redirect.setParameter("cid", Conversation.instance().getId());
			Logging.getLogProvider(ProcessoTrfHome.class).info("Redirecionando para update.xhtml com id: " + idProcessoTrf);
			redirect.setParameter("tab", "assunto");
			redirect.setParameter("cadastroProcessoClet", true);
			
			setProcessoClet(new ProcessoClet());
			getProcessoClet().setNaturezaClet(new NaturezaClet());
			getProcessoClet().getNaturezaClet().setTipoNatureza(TipoNaturezaCletEnum.L);
			getProcessoClet().setProcessoTrf(ProcessoTrfHome.instance().getInstance());
			
			redirect.execute();
			
		}
	}
	
	public void gravarClet() {
		if (possuiIncidentesSemJulgamentoClet != null && possuiIncidentesSemJulgamentoClet) {
			FacesMessages.instance().add(Severity.ERROR, "Só podem ser cadastrados, via CLET, os processos que Não tenham incidentes processuais sem julgamento!");
			return;
		}
		if (Authenticator.getOrgaoJulgadorAtual() == null || (getInstance().getOrgaoJulgador().getIdOrgaoJulgador() != Authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador())) {
			FacesMessages.instance().add(Severity.ERROR, "Os processos só poderão ser cadastrados na mesma Vara em que o mesmo tramita no legado!");
			return;
		}
	}

	public void carregaClasseJudicialClet() {
		try{
			
			String numeroProcesso = String.valueOf(numeroProcessoClet);
			numeroProcesso = numeroProcesso.replace("-", "");
			numeroProcesso = numeroProcesso.replace(".", "");
			
			if (numeroProcesso.length() == 20) {
				
				numeroProcesso = NumeroProcessoUtil.mascaraNumeroProcesso(numeroProcesso);
				StringBuilder sb = new StringBuilder();
				sb.append("select count(o) from Processo o ");
				sb.append("where o.numeroProcesso = :numeroProcesso ");
				Query q = getEntityManager().createQuery(sb.toString());
				q.setParameter("numeroProcesso", numeroProcesso);
				long result = (Long) q.getSingleResult();
				if(result > 0){
					FacesMessages.instance().clear();
					FacesMessages.instance().add(Severity.ERROR, "Processo já existente ou inválido!");
					return;
				}
				
				if (Authenticator.getOrgaoJulgadorAtual() != null) {
					getInstance().setJurisdicao(Authenticator.getOrgaoJulgadorAtual().getJurisdicao());
					getInstance().setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
					classeJudicialListClet = getClassesJudiciaisNovoProcessoPrimeiroGrau(null);
				}
				
				setNumeroProcessoClet(numeroProcesso);
				
			} else {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, "Favor, informar o processo de acordo com a numeração única!");
				return;
			}
			
		}catch(Exception e){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Erro ao validar o número do processo!");
			return;
		}
	}
	
	/*
	 * [PJEII-4914] Para a lista de documentos informada, faz ordenaééo pela data de juntada.
     */
    private void ordenarListDocsDataJuntada(List<ProcessoDocumento> listProcDoc){
    	Collections.sort(listProcDoc, new Comparator<ProcessoDocumento>() {
            @Override
			public int compare(ProcessoDocumento p1, ProcessoDocumento p2) {
                int result;
                if (p1.getDataJuntada() == null || p2.getDataJuntada() == null) {
                    if (p1.getDataJuntada() == null) {
                        result = -1;
                    } else if (p2.getDataJuntada() == null) {
                        result = 1;
                    } else {
                        result = 0;
                    }
                    
                } else {
                    result = p1.getDataJuntada().compareTo(p2.getDataJuntada());
                }
                return (result * -1);
            }
        });        
    }
    
    /**
     * [PJEII-4914] Recupera o id do documento da ultima decisão registrada no processo.
     * Operação utilizada para criação de variével utilizada nos modelos de documento.
     * @return
     */
	public String getIdUltimoRecurso() {
    	List<ProcessoDocumento> listProcDoc = getListProcessoDocumento();
    	if(listProcDoc != null){
    		for (ProcessoDocumento processoDocumento : listProcDoc) {
    			if ("6000050".equals(processoDocumento.getTipoProcessoDocumento().getCodigoDocumento()) /* agravo regimental  */
    					|| "49".equals(processoDocumento.getTipoProcessoDocumento().getCodigoDocumento()) /* embargo de declaração */ ) {
    				return Integer.toString(processoDocumento.getIdProcessoDocumento());
    			}
			}
    	}
    	return "";
    }
    
	/**
	 * [PJEII-4914] Retorna um VO encapsulando as informaéées da Ultima Deciséo.
	 *  Operação utilizada para criaééo de variéveis em modelos de documento.
	 * @return
	 */
    public ProcessoDocumentoVO getUltimaDecisao() {

    	final List<ProcessoDocumento> listProcDoc = getListProcessoDocumento();
        if (listProcDoc != null) {
            ordenarListDocsDataJuntada(listProcDoc);    
            for (ProcessoDocumento pd : listProcDoc) {
                if (pd.getTipoProcessoDocumento() != null)  {
                    if ("6000019".equals(pd.getTipoProcessoDocumento().getCodigoDocumento()) /* DESPACHO */
                            || "6000030".equals(pd.getTipoProcessoDocumento().getCodigoDocumento()) /* ACéRDéO */ 
                            || "6000020".equals(pd.getTipoProcessoDocumento().getCodigoDocumento()) /* DECISéO */ ) {
                        return new ProcessoDocumentoVO(pd.getIdProcessoDocumento(), pd.getTipoProcessoDocumento(), pd.getDataJuntada());
                    }                        
                }
            }            
        }

        return new ProcessoDocumentoVO();
    }
	
	/**
	 * 
	 * @return Se foi marcado para plantéo pelo advogado/procurador
	 */
	public boolean isAtendimentoEmPlantao() {
		return atendimentoEmPlantao;
	}
	
	/**
	 * Seta se foi marcado para plantéo pelo advogado/procurador
	 * @param atendimentoEmPlantao
	 */
	public void setAtendimentoEmPlantao(boolean atendimentoEmPlantao) {
		this.atendimentoEmPlantao = atendimentoEmPlantao;
	}
	
	/**
	 * PJEII-3573 - Verifica se o usuário tem permissão para ver a aba
	 * Sigilo/Segredo
	 * 
	 * @param podeExibir
	 * @return verdadeiro se pode exibir ou falso se Não pode exibir
	 */
	public boolean podeExibirAbaSigiloSegredo(boolean podeExibir) {
		return podeExibir;
	}

	
	public List<ProcessoTrf> getListaMandadoDevolvido(){
		return listaMandadoDevolvido;
	}

	
	public void setListaMandadoDevolvido(List<ProcessoTrf> listaMandadoDevolvido){
		this.listaMandadoDevolvido = listaMandadoDevolvido;
	}

	
	public Boolean getCheckAllMandadoDevolvido(){
		return checkAllMandadoDevolvido;
	}

	
	public void setCheckAllMandadoDevolvido(Boolean checkAllMandadoDevolvido){
		this.checkAllMandadoDevolvido = checkAllMandadoDevolvido;
	}

	public boolean isCadastroProcessoClet() {
		return cadastroProcessoClet;
	}

	public void setCadastroProcessoClet(boolean cadastroProcessoClet) {
		this.cadastroProcessoClet = cadastroProcessoClet;
	}

	public ProcessoClet getProcessoClet() {
		return processoClet;
	}

	public void setProcessoClet(ProcessoClet processoClet) {
		this.processoClet = processoClet;
	}

	public String getTipoNaturezaClet() {
		return tipoNaturezaClet;
	}

	public void setTipoNaturezaClet(String tipoNaturezaClet) {
		this.tipoNaturezaClet = tipoNaturezaClet;
	}
	
	public boolean isSituacaoPauta(String codigoSituacaoFromTipoSituacaoPautaJTEnum){
		ProcessoTrf processo = this.getInstance();
		boolean retorno = false;
		if(processo != null){
			PautaSessao pautaSessao = recuperarPautaSessao(processo);
			if(pautaSessao!= null && pautaSessao.getTipoSituacaoPauta() != null && pautaSessao.getTipoSituacaoPauta() != null  && pautaSessao.getTipoSituacaoPauta().getClassificacao() != null  && pautaSessao.getTipoSituacaoPauta().getCodigoTipoSituacaoPauta().equalsIgnoreCase(codigoSituacaoFromTipoSituacaoPautaJTEnum)){
				retorno = true;
			}
		}
		
		return retorno;
	}

	public boolean isAdiado() {
		return isSituacaoPauta(TipoSituacaoPautaJTEnum.AD.name());
	}

	public boolean isJulgado(){
		ProcessoTrf processo = this.getInstance();
		boolean retorno = false;
		if(processo != null){
			PautaSessao pautaSessao = recuperarPautaSessao(processo);
			if(pautaSessao != null && pautaSessao.getTipoSituacaoPauta() != null && pautaSessao.getTipoSituacaoPauta().getClassificacao() != null && pautaSessao.getTipoSituacaoPauta().getClassificacao().equals(ClassificacaoTipoSituacaoPautaEnum.J) ){
				retorno = true;
			}
		}
		
		return retorno;
	}
	
	public PautaSessao recuperarPautaSessao(ProcessoTrf processo){
		PautaSessaoManager pautaManager = getComponent("pautaSessaoManager");
		return pautaManager.getUltimaPautaByProcesso(processo);
	}
	
	/**
	 * [PJEII-4718] [PJEII-4763] Retorna os pelos, como a função getPolos(ProcessoTrf), mas sem os documentos das partes.
	 * @param processoTrf
	 * @return <nome parte polo ativo> [e outros] X <nome parte polo passivo> [e outros] 
	 */
	public String getNomeProcessoPartePolos(ProcessoTrf processoTrf) {
		return getNomeProcessoPartePoloAtivo(processoTrf) + " X " + getNomeProcessoPartePoloPassivo(processoTrf);
	}
	public String getNumeroProcessoClet() {
		return numeroProcessoClet;
	}

	public void setNumeroProcessoClet(String numeroProcessoClet) {
		this.numeroProcessoClet = numeroProcessoClet;
	}

	public Boolean getPossuiIncidentesSemJulgamentoClet() {
		return possuiIncidentesSemJulgamentoClet;
	}

	public void setPossuiIncidentesSemJulgamentoClet(Boolean possuiIncidentesSemJulgamentoClet) {
		this.possuiIncidentesSemJulgamentoClet = possuiIncidentesSemJulgamentoClet;
	}

	public List<ClasseJudicial> getClasseJudicialListClet() {
		return classeJudicialListClet;
	}

	public void setClasseJudicialListClet(List<ClasseJudicial> classeJudicialListClet) {
		this.classeJudicialListClet = classeJudicialListClet;
	}
	
	/**
	 * Metodo para fornecer conteédo para substituição de variével nos modelos de tópicos de documentos estruturados
	 */
	public String getNomeJuizDocumento() {
		return EditorAction.instance().getPessoaMagistrado().getNome();
	}
	
	/**
	 * Metodo para fornecer conteédo para substituição de variével nos modelos de tópicos de documentos estruturados
	 */
	public String getGeneroJuizDocumento() {
		return EditorAction.instance().getPessoaMagistrado().getSexo().toString();
	}

	public Boolean getExistemDocumentosParaConsolidar() {
		if(existemDocumentosParaConsolidar == null && instance != null) {
			DownloadBinarioMNIManager downloadBinarioMNIManager = (DownloadBinarioMNIManager) Component.getInstance("downloadBinarioMNIManager");
			existemDocumentosParaConsolidar = downloadBinarioMNIManager.haAgendamentos(instance);
			if(existemDocumentosParaConsolidar) {
				documentosParaConsolidar = downloadBinarioMNIManager.recuperaIdentificadoresConteudos(instance);
			}
		}else{
			existemDocumentosParaConsolidar = false;
		}
		return existemDocumentosParaConsolidar;
	}

	public void setExistemDocumentosParaConsolidar(Boolean existemDocumentosParaConsolidar) {
		this.existemDocumentosParaConsolidar = existemDocumentosParaConsolidar;
	}

	public Set<Integer> getDocumentosParaConsolidar() {
		return documentosParaConsolidar;
	}

	public void setDocumentosParaConsolidar(Set<Integer> documentosParaConsolidar) {
		this.documentosParaConsolidar = documentosParaConsolidar;
	}
	
	public Boolean getSelecionarTodosDocumentos() {
		return selecionarTodosDocumentos;
	}

	public void setSelecionarTodosDocumentos(Boolean selecionarTodosDocumentos) {
		this.selecionarTodosDocumentos = selecionarTodosDocumentos;
	}
	
	@SuppressWarnings("unchecked")
	public void selecionarTodosDocumentos() {
		if(selecionarTodosDocumentos == null) {
			return;
		}
		
		GridQuery grid = ComponentUtil.getComponent(Grid.PROCESSO_TRF_DOCUMENTO_GRID);
		List<ProcessoDocumento> documentos = grid.getFullList();
		List<Object> selecionados = grid.getSelectedRowsList();
		
		for(Object documento : documentos) {
			int idProcessoDocumentoBin = ((ProcessoDocumento) documento).getProcessoDocumentoBin().getIdProcessoDocumentoBin();
			
			if(!documentosParaConsolidar.contains(idProcessoDocumentoBin)) {
				continue;
			}
			
			if(selecionarTodosDocumentos) {
				if(!selecionados.contains(documento)) {
					grid.addRemoveRowList(documento);
				}
			} else {
				if(selecionados.contains(documento)) {
					grid.addRemoveRowList(documento);
				}
			}
		}
	}
	
	public boolean exibeColunaDeDownloadDeDocumentos() {
		Boolean resultado = Boolean.FALSE;
		
		FacesContext context = FacesContext.getCurrentInstance();
		//O contexto é validado porque as requisições via webservices Não possuem FacesContext.
		if (context != null && context.getViewRoot() != null) {
			String viewId = context.getViewRoot().getViewId();
			boolean isVisualizacaoDoProcessoCompleto = viewId.contains("detalheProcessoVisualizacao");
			
			resultado = isVisualizacaoDoProcessoCompleto && getExistemDocumentosParaConsolidar();
		}
		return resultado; 
	}
	
	/**
	 * Valida a autuação do processo. 
	 * A validação exige que:
	 * <li>o processo tenha uma {@link Jurisdicao} vinculada</li>
	 * <li>o processo tenha uma petição inicial ({@link ParametroUtil#getIdTipoProcessoDocumentoPeticaoInicial()} assinada</li>
	 * <li>todos os documentos do processo estejam assinados</li>
	 * <li>o processo tenha uma classe judicial vinculada</li>
	 * <li>que os complementos de classe judicial obrigatórios estejam definidos ({@link #getComplementoClasseProcessoTrfList()})</li>
	 * <li>o processo tenha pelo menos um assunto</li>
	 * <li>o processo tenha um assunto marcado como assunto principal</li>
	 * <li>os assuntos criminais que exigem assunto antecedente estejam com esses assuntos definidos</li>
	 * <li>exista pelo menos uma pessoa compondo o polo passivo quando se tratar de classe que exige polo passivo ({@link ClasseJudicial#getReclamaPoloPassivo()}</li>
	 * <li>exista ao menos uma autoridade compondo o polo passivo quando se tratar de classe que exige autoridade ({@link ClasseJudicial#getExigeAutoridade()})</li>
	 * <li>o processo tenha valor da causa, quando o sistema estiver configurado para a justiça do Trabalho ({@link ParametroUtil#getTipoJustica()})</li>
	 * 
	 * @throws Exception caso uma das regras acima Não tenha sido respeitada.
	 */
	public void validarAutuacao() throws Exception{
		validarAutuacao(getInstance());
	}
	
	/**
	 * Valida a autuação do processo. 
	 * A validação exige que:
	 * <li>o processo tenha uma {@link Jurisdicao} vinculada</li>
	 * <li>o processo tenha uma petição inicial ({@link ParametroUtil#getIdTipoProcessoDocumentoPeticaoInicial()} assinada</li>
	 * <li>todos os documentos do processo estejam assinados</li>
	 * <li>o processo tenha uma classe judicial vinculada</li>
	 * <li>que os complementos de classe judicial obrigatórios estejam definidos ({@link #getComplementoClasseProcessoTrfList()})</li>
	 * <li>o processo tenha pelo menos um assunto</li>
	 * <li>o processo tenha um assunto marcado como assunto principal</li>
	 * <li>os assuntos criminais que exigem assunto antecedente estejam com esses assuntos definidos</li>
	 * <li>exista pelo menos uma pessoa compondo o polo passivo quando se tratar de classe que exige polo passivo ({@link ClasseJudicial#getReclamaPoloPassivo()}</li>
	 * <li>exista ao menos uma autoridade compondo o polo passivo quando se tratar de classe que exige autoridade ({@link ClasseJudicial#getExigeAutoridade()})</li>
	 * <li>o processo tenha valor da causa, quando o sistema estiver configurado para a justiça do Trabalho ({@link ParametroUtil#getTipoJustica()})</li>
	 * <li>o processo, quando marcado como sigiloso, tem de ter selecionado o motivo do sigilo.
	 * 
	 * @param proc o processo a ser validado
	 * @throws Exception caso uma das regras acima Não tenha sido respeitada.
	 */
	public void validarAutuacao(ProcessoTrf proc) throws Exception{
		if (proc.getProcessoStatus() == ProcessoStatusEnum.V) {
			return;
		}
		StringBuilder erros = new StringBuilder();
		
		if (!isCadastroProcessoClet()) {
			List<ProcessoDocumento> processoDocumentoList = proc.getProcesso().getProcessoDocumentoList();
			int idTipoProcessoDocumentoPeticaoInicial = proc.getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento();
			if (proc.getInicial().equals(ClasseJudicialInicialEnum.I) && !proc.getProcessoStatus().equals(ProcessoStatusEnum.D)) {
				Boolean peticaoInicial = Boolean.FALSE;
				Boolean temKml = Boolean.FALSE;
				for (ProcessoDocumento procDoc : processoDocumentoList) {
					ProcessoDocumentoBin pBin = procDoc.getProcessoDocumentoBin();
					
					if(Boolean.TRUE.equals(getAcaoAmbiental(proc)) && pBin.getExtensao() != null && pBin.getExtensao().equals(MimetypeUtil.MIME_TYPE_KML)) {
						temKml = true;
					}
					
					
					if (procDoc.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == idTipoProcessoDocumentoPeticaoInicial && pBin != null &&
							((pBin.isBinario() && pBin.getNumeroDocumentoStorage()!=null && pBin.getNomeArquivo()!=null) ||
							 (pBin.getModeloDocumento() != null && !pBin.getModeloDocumento().trim().isEmpty()))
						) {
						peticaoInicial = Boolean.TRUE;
					}
				}
				if (!peticaoInicial) {
					erros.append("Não há petição inicial anexada ao processo. \n");
				}
				
				if (Boolean.TRUE.equals(getAcaoAmbiental()) && !temKml) {
					erros.append("Não há documento do tipo KML anexado ao processo. \n");
				}
			}
		}

		// Verifica se todos os documentos anexados estão assinados
		// digitalmente.
		Boolean faltaAssinatura = Boolean.FALSE;
		
		//Verifica se todos os documentos estão assinados somente se o processo ainda Não
		//foi Distribuído. Em casos de redistribuição, essa validação pode trazer problemas...
		if(!proc.getProcessoStatus().equals(ProcessoStatusEnum.D)) {
			for (ProcessoDocumento procDocList : proc.getProcesso().getProcessoDocumentoList()) {
				boolean temAssinatura = verificaDocumentoAssinado(procDocList);
				if (!temAssinatura && procDocList.getAtivo().equals(Boolean.TRUE) && procDocList.getUsuarioInclusao() != null) {
					PessoaHome pHome = ComponentUtil.getComponent("pessoaHome");
					ProcessoTrfRedistribuicao processoTrfRedistribuicao = ProcessoTrfRedistribuicaoHome.instance().getInstance();
	
					/*
					 * Trata os casos de verificação de documentos assinados na Distribuição,
					 * isto é, em casos onde o entity gerenciado em
					 * ProcessoTrfRedistribuicaoHome Não tenha um tipo de
					 * redistribuição associado.
					 */
					if (processoTrfRedistribuicao.getInTipoRedistribuicao() == null) {
						faltaAssinatura = Boolean.TRUE;
					}else if (procDocList.getUsuarioExclusao() == null && !pHome.isUsuarioExterno(procDocList.getUsuarioInclusao())) {
						/*
						 * Realiza o tratamento dos casos de envolvendo assinatura e
						 * excluséo de documentos na redistribuição de processos, isto
						 * é, em casos onde o entity gerenciado em
						 * ProcessoTrfRedistribuicaoHome tenha um tipo de redistribuição
						 * associado. Os casos onde deve ser acusado erro de falta de
						 * assinatura séo quando o documento Não foi excluédo e quando o
						 * usuário que o incluiu for um usuário interno.
						 */
						faltaAssinatura = Boolean.TRUE;
					}
				}
			}
			if (!verificarClasseSigilosa() && getInstance().getSegredoJustica() && getInstance().getObservacaoSegredo() == null) {
				erros.append("O Motivo do segredo de justiça deve ser preenchido. \n");
			}
		}

		if (faltaAssinatura) {
			erros.append("Todos os documentos devem estar assinados digitalmente. \n");
		}

		if (proc.getClasseJudicial() == null) {
			erros.append("A classe judicial Não foi definida.\n");
		} else if (proc.getClasseJudicial().getExigeAutoridade() && proc.getAutoridadesPoloPassivo().size() == 0) {
			erros.append("A classe judicial exige ao menos uma autoridade no polo passivo. \n");
		}

		// Verifica a quantidade de assuntos vinculados ao processo. Deve existir pelo menos 1 assunto principal.
		if (proc.getProcessoAssuntoList().isEmpty()) {
 			erros.append("Deve haver ao menos um assunto vinculado ao processo. \n");
		} else if(proc.getProcessoAssuntoPrincipal() == null){
			erros.append("Deve haver ao menos um assunto principal vinculado ao processo. \n");
		} else if (BooleanUtils.isTrue(proc.getProcessoAssuntoPrincipal().getAssuntoTrf().getComplementar())) {
			erros.append("Assuntos complementares exigem a presença de pelo menos um assunto principal. \n");
 		}

		/*
		 * Caso a classe pertença ao agrupamento criminal (CRI), verifica a
		 * existência de assuntos que exigem informar crimes antecedentes e se
		 * esses crimes foram informados
		 */
		boolean criminal = false;
		if (proc.getClasseJudicial() != null) {
			for (ClasseJudicialAgrupamento cja : proc.getClasseJudicial().getAgrupamentos()) {
				if (cja.getAgrupamento().getCodAgrupamento().equalsIgnoreCase("CRI")) {
					criminal = true;
					break;
				}
			}

			if (criminal) {
				for (ProcessoAssunto aux : proc.getProcessoAssuntoList()) {
					if (aux.getAssuntoTrf().getExigeAssuntoAntecedente()
							&& aux.getProcessoAssuntoAntecedenteList().isEmpty()) {
						erros.append("Informe o(s) assunto(s) antecedente(s) para o assunto [" + aux.getAssuntoTrf().getAssuntoTrf() + "].\n");
					}
				}

				if (proc.getProcessoAssuntoPrincipal() == null) {
					erros.append("Informe o assunto principal.\n");
				}
			}
		}

		// Verifica se existe pelo menos uma parte no polo ativo.
		Integer tamAtivo = proc.getProcessoPartePoloAtivoSemAdvogadoList().size();
		if (tamAtivo == 0) {
			erros.append("Deve haver ao menos uma parte no polo ativo vinculada ao processo. \n");
		}
		
		// Verifica se existe pelo menos uma parte no polo passivo.
		Integer tamPassivo = proc.getProcessoPartePoloPassivoSemAdvogadoList().size();
		if (tamPassivo == 0 && proc.getClasseJudicial().getReclamaPoloPassivo()) {
			erros.append("Deve haver ao menos uma parte no polo passivo vinculada ao processo. \n");
		}

		Integer tamVetor = this.getComplementoClasseProcessoTrfList().size();
		for (int i = 0; i < tamVetor; i++) {
			if (this.getComplementoClasseProcessoTrfList().get(i).getComplementoClasse().getObrigatorio()) {
				if (this.getComplementoClasseProcessoTrfList().get(i).getValorComplementoClasseProcessoTrf() == null) {
					erros.append("Existe um complemento obrigatório que Não foi preenchido. \n");
				}
			}
		}

		// Verifica se existe jurisdição associada ao processo
		if (proc.getJurisdicao() == null) {
			erros.append("Deve haver uma jurisdição vinculada ao processo. \n");
		}

		// Verifica valor da causa
		if (Boolean.TRUE.equals(proc.getClasseJudicial().getControlaValorCausa()) && proc.getValorCausa() == null) {
			erros.append("Deve ser informado o valor da causa. \n");
		}
		
		if(getInstance().getJurisdicao() == null && proc.getJurisdicao() != null) {
			getInstance().setJurisdicao(proc.getJurisdicao());
		}
		
		if(getInstance().getClasseJudicial() == null) {
			getInstance().setClasseJudicial(proc.getClasseJudicial());
		}
		
		if(getInstance().getAssuntoTrfList() == null || getInstance().getAssuntoTrfList().isEmpty()) {
			getInstance().setAssuntoTrfList(proc.getAssuntoTrfList());
		}
		
		Competencia competenciaValidacao = getInstance().getCompetencia() != null ? getInstance().getCompetencia() : proc.getCompetencia();
		if(competenciaValidacao == null){
			carregaCompetencias();
			if(getCompetenciasPossiveis() != null) {
				if(getCompetenciaConflito() == null) {
					erros.append("Deve haver somente uma competência. \n");
				}else {
					competenciaValidacao = getCompetenciaConflito();
				}
			}else{
				erros.append("Não há competências possíveis. \n");
			}
		}
		if(competenciaValidacao != null){
			if(competenciaValidacao.getIndicacaoOrgaoJulgadorObrigatoria()){
				if(proc.getOrgaoJulgador() == null) {
					erros.append("é obrigatória a indicação de um órgão julgador para a Distribuição. \n");
				}
			}else if(!proc.getProcessoStatus().equals(ProcessoStatusEnum.D) && proc.getOrgaoJulgador() != null && !ParametroUtil.instance().getDistribuicaoManual() && !proc.isIncidental()) {
				erros.append("A competência do processo Não permite a indicação de um órgão julgador para a Distribuição. \n");
			}
		}
		
		if (erros.length() > 0) {
			throw new Exception(erros.toString());
		}
	}
	
	private boolean verificaDocumentoAssinado(ProcessoDocumento processoDocumento){
		ProcessoDocumentoBinPessoaAssinaturaManager pdbpam = ComponentUtil.getComponent("processoDocumentoBinPessoaAssinaturaManager");
		
		return pdbpam.isDocumentoAssinado(processoDocumento);
	}
	
	
	
	
	
	/**
	 * Valida o processo para Distribuição.
	 * Nessa validação, assegura-se que:
	 * <li>o processo está validado para autuação ({@link #validarProcessoParaAutuacao()}</li>
	 * <li>a classe processual do processo judicial tem um fluxo vinculado; e</li>
	 * <li>o fluxo vinculado é classe esteja ativo, publicado na data da validação e marcado como publicado</li>
	 * 
	 * @throws Exception caso algum dos critérios de validação Não estejam preenchidos.
	 */
	private void validarDistribuicao(ProcessoTrf proc) throws Exception {
		// Não há necessidade de validar o processo caso ele jé tenha sido
		// Distribuído
		if (proc.getProcessoStatus() == ProcessoStatusEnum.D) {
			return;
		}

		// Repete a validação de processo para autuação
		validarAutuacao(proc);

		StringBuilder erros = new StringBuilder();

		Fluxo fluxo = proc.getClasseJudicial().getFluxo();
		Date hoje = new Date();
		if (fluxo == null || 
		    !fluxo.getAtivo() ||
			(fluxo.getDataFimPublicacao() != null && fluxo.getDataFimPublicacao().compareTo(hoje) < 0) ||
			fluxo.getDataInicioPublicacao().compareTo(hoje) > 0) {
			erros.append(String.format("Não há fluxo processual definido para a classe %s. \n", proc.getClasseJudicial()));
		}
		if (fluxo != null && !fluxo.getPublicado()) {
			erros.append(String.format("O fluxo processual vinculado é classe %s Não está publicado. \n", proc.getClasseJudicial()));
		}
		if (erros.length() > 0) {
			throw new Exception(erros.toString());
		}
	}
	
	public void validarRedistribuicao(TipoRedistribuicaoEnum tipoRedistribuicao) throws Exception{
		validarRedistribuicao(getInstance(), tipoRedistribuicao);
	}

	/**
	 * Valida o processo para redistribuição. 
	 * Internamente, essa validação sé acontece quando o tipo de redistribuição indicado 
	 * é o "por determinaééo judicial" ({@link TipoRedistribuicaoEnum#J} e se limita a 
	 * repetir a validação de que trata {@link #validarProcessoParaDistibuicao()}.
	 * 
	 * @param tipoRedistribuicao o tipo de redistribuição que provocou a revalidação.
	 * @throws Exception caso haja algum erro na validação
	 */
	public void validarRedistribuicao(ProcessoTrf proc, TipoRedistribuicaoEnum tipoRedistribuicao) throws Exception {
		switch (tipoRedistribuicao) {
		case J:
			// Repete a validação de processo para Distribuição
			validarDistribuicao(proc);
			break;
		default:
			break;
		}
	}
	
	public ProtocolarDocumentoBean getProtocolarDocumentoBean(){
		if(protocolarDocumentoBean == null && this.instance != null && this.instance.getIdProcessoTrf() > 0){
			protocolarDocumentoBean = new ProtocolarDocumentoBean(this.instance.getIdProcessoTrf(), ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL, ProtocolarDocumentoBean.TipoAcaoProtocoloEnum.NOVO_PROCESSO);
		}
		return protocolarDocumentoBean;
	}

	public boolean pendeRecuperacao(Integer idProcessoDocumentoBin){
		Boolean ret = mapaPendenciaRecuperacao.get(idProcessoDocumentoBin);
		if(ret == null){
			DownloadBinarioArquivoManager dbam = (DownloadBinarioArquivoManager) Component.getInstance("downloadBinarioArquivoManager");
			ret = dbam.pendeRecuperacaoPorIdentificadorBinarioLocal(idProcessoDocumentoBin);
			mapaPendenciaRecuperacao.put(idProcessoDocumentoBin, ret);
		}
	  	return ret;  
	}  
	
	public boolean numeroProcessoJaExiste(String numeroProcesso){
		String query = "select count(id_processo_trf) from tb_cabecalho_processo tcp where tcp.nr_processo = :numeroProcesso";
		Query q = getEntityManager().createNativeQuery(query);
		q.setParameter("numeroProcesso", numeroProcesso);	
		q.setMaxResults(1);
		Number cont = (Number) q.getSingleResult();
		return cont.intValue() > 0;
	}
	
	public boolean numeroProcessoNormatizado(Processo processo){
		if(processo.getNumeroProcessoTemp() != null) {
			String numeroProcTemp = NumeroProcessoUtil.retiraMascaraNumeroProcesso(processo.getNumeroProcessoTemp());
			if(!numeroProcTemp.trim().equals("")){	
				if(!NumeroProcessoUtil.numeroProcessoValido(processo)){
					FacesMessages.instance().clear();
					FacesMessages.instance().add(StatusMessage.Severity.ERROR, "número do processo de origem é inválido!");
					return false;
				}
			} else {
				ProcessoHome.instance().getInstance().setNumeroProcessoTemp(null);
			}
		}
		return true;
	}
	
	public boolean numeroProcessoJaCadastrado(Processo processo){
		if(numeroProcessoJaExiste(processo.getNumeroProcessoTemp())) {
			return true;
		}
		return false;
	}
	
	public boolean protocolacaoValida(){
		Processo processo = ProcessoHome.instance().getInstance();
		if(processo.getNumeroProcessoTemp() != null){
			if(!numeroProcessoJaCadastrado(processo)){
				return true;
			} else {
				ProcessoHome.instance().getInstance().setNumeroProcessoTemp(null);
				return false;
			}
		}
		return true;
	}
	
	public void limparNumeroProcessoTemp() {
		ProcessoHome.instance().getInstance().setNumeroProcessoTemp(null);
	}
	
	public String getPartePorPolo(List<ProcessoParte> listaDePartes){
		return getParte(listaDePartes);
	}
	
	public String getTituloDoAdvogado(ProcessoParte parte){
		String titulo = "";
		Pessoa pessoa = parte.getPessoa();
		TipoParte tipo = parte.getTipoParte();

		if (pessoa.getInTipoPessoa() == TipoPessoaEnum.F){
			try{
				SexoEnum sexo = ((PessoaFisica) pessoa).getSexo();
				if (tipo.getTipoParte().equals("ADVOGADO")){
					titulo = sexo == SexoEnum.F ? "Advogada" : "Advogado";
				} else if (tipo.getTipoParte().equals("PROCURADOR")){
					titulo = sexo == SexoEnum.F ? "Procuradora" : "Procurador";
				}
			} catch (ClassCastException ex) {
				titulo = tipo.getTipoParte().equals("ADVOGADO") ? "Advogado(a)" : "Procurador(a)";
			}
		}
		return titulo;
	}
	
	public String getPrioridadesFormatadas(){
		try{
			String lista = this.instance.getPrioridadeProcessoList().toString();
			lista = lista.replace("[", "");
			lista = lista.replace("]", "");
			return lista;
		} catch (Exception e){
			return "(0)";
		}
	}
	
	/**
	 * Metodo invocado ao clicar na aba 'Incluir petições e documentos'.
	 * 
	 * @param protocolarDocumentoBean
	 */
	public void carregaAbaIncluirPeticoesEhDocumentos(ProtocolarDocumentoBean protocolarDocumentoBean) {
		if(protocolarDocumentoBean != null){
			this.protocolarDocumentoBean = protocolarDocumentoBean;
		}
		getProtocolarDocumentoBean().sincronizarProcessoDocumentoComProcessoTrf(true);
		setAcaoAmbiental(null);
		FacesMessages.instance().clear();
	}
		
	/**
	 * Este Metodo é responsável por iniciar 
	 * a numeração do processo judicial
	 * @throws Exception
	 */
	private void numerarProcessoJudicial() throws Exception{
		ProcessoTrf processoJudicial = getInstance();
			
		if(Strings.isEmpty(processoJudicial.getNumeroProcesso())) {
			numerarProcesso(processoJudicial);
		}
	}
	
	private void excluiAssuntosDiversos() {
        List<AssuntoTrf> assuntos = retornaAssuntosDiversos();
        if (assuntos.size() > 0) {
            Query q = EntityUtil.getEntityManager().createQuery(
            		"delete from ProcessoAssunto o where o.assuntoTrf in (:assuntos) and o.processoTrf = :processoTrf");
            q.setParameter("assuntos", assuntos);
            q.setParameter("processoTrf", getInstance());
            q.executeUpdate();
        }
	}

	private List<AssuntoTrf> retornaAssuntosDiversos() {
		Integer idNovaJurisdicao = getInstance().getJurisdicao().getIdJurisdicao();
		Integer idNovaClasse = getInstance().getClasseJudicial().getIdClasseJudicial();
		List<AssuntoTrf> assuntosAtuais = getInstance().getAssuntoTrfList();
		List<AssuntoTrf> assuntosDiversos = new ArrayList<AssuntoTrf>(0);
		if (idNovaJurisdicao != null && idNovaClasse != null
				&& assuntosAtuais != null && assuntosAtuais.size() > 0) {
			List<AssuntoTrf> novosAssuntos = buscaAssuntosDaCompetencia();
			for (AssuntoTrf assuntoTrf : assuntosAtuais) {
				if (!novosAssuntos.contains(assuntoTrf)) {
					assuntosDiversos.add(assuntoTrf);
				}
			}
		}
		return assuntosDiversos;
	}

	@SuppressWarnings("unchecked")
	private List<AssuntoTrf> buscaAssuntosDaCompetencia() {
		Integer idJurisdicao = getInstance().getJurisdicao().getIdJurisdicao();
		Integer idClasseJudicial = getInstance().getClasseJudicial().getIdClasseJudicial();
		String q = "select distinct o from AssuntoTrf o "
				+ "inner join o.competenciaClasseAssuntoList cca "
				+ "inner join cca.competencia co "
				+ "inner join co.orgaoJulgadorCompetenciaList ojc "
				+ "where ojc.orgaoJulgador.jurisdicao.idJurisdicao = :idJurisdicao "
				+ "and cca.classeAplicacao.classeJudicial.idClasseJudicial = :idClasseJudicial "
				+ "and ojc.dataInicio <= :dataAtual "
				+ "and (ojc.dataFim >= :dataAtual or ojc.dataFim is null) "
				+ "and cca.dataInicio <= :dataAtual "
				+ "and (cca.dataFim >= :dataAtual or cca.dataFim is null) "
				+ "and co.ativo = true";
		Query query = EntityUtil.getEntityManager().createQuery(q);
		query.setParameter("idJurisdicao", idJurisdicao);
		query.setParameter("idClasseJudicial", idClasseJudicial);
		query.setParameter("dataAtual", new Date());
		return query.getResultList();
	}
	
	/**
	 * Retorna uma Lista Ordenada do Tipo ProcessoDocumento de acordo com os criterios do 
	 * ProcessoDocumentoComparator
	 */
	public List<ProcessoDocumento> getListaOrdenadaDocumentos(List<ProcessoDocumento> lista) {
		Collections.sort(lista, new ProcessoDocumentoComparator());
		return lista;
	}
	
	/**
	 * Metodo responsável por controlar a exibição do órgão Julgador Revisor nos
	 * detalhes do processo.
	 */
	public boolean getExibeRevisor() {
		return Boolean.TRUE == getInstance().getExigeRevisor();
	}
	
	/**
	 * Retorna 'true' se o processo estiver relacionado a turmas recursais;
	 * 
	 * @return isProcessoTurmaRecursal
	 */
	private boolean isRecursoTurmaRecursal(){
		return getInstance().getInicial() == ClasseJudicialInicialEnum.R;
	}

	/**
	 * Retorna um texto com uma lista das sessões de julgamento do processo ordenados por apelido da sessão.
	 * @param situacaoJulgamento Junção das descrições (label) dos Enuns:TipoSituacaoPautaEnum e AdiadoVistaEnum e a palavra "Todos"  
	 * @param processo
	 * @param tipoInformacaoRetorno Tipo de informaééo que seré impressa na lista a ser retornada.
	 * @return Texto com lista das sessões com apenas o texto extraédo do Metodo selecionado para apresentaééo
	 */	
	private String obterTextoListaSessaoJulgamentoPorDetalhe(
			ProcessoTrf processo, String situacaoJulgamento,
			String tipoInformacaoRetorno) throws PJeBusinessException {

		String retorno = StringUtils.EMPTY;

		List<SessaoPautaProcessoTrf> historicoSessoes = new ArrayList<SessaoPautaProcessoTrf>();

		if (StringUtils.isNotBlank(situacaoJulgamento)) {
			if (StringUtils.isNotBlank(tipoInformacaoRetorno)) {
				retorno = retornarTratamentoSituacaoJulgamento(
						situacaoJulgamento, historicoSessoes,processo);
			}
			else{
				throw new PJeBusinessException(
						"processoTrfHomeSessoesJulgamento.tipoRetornoNaoNuloEmBranco");				
			}
		} else {
			throw new PJeBusinessException(
					"processoTrfHomeSessoesJulgamento.situacaoJulgamentoNaoNuloEmBranco");
		}

		if (StringUtils.isEmpty(retorno)) {
			retorno = carregarDetalhesSituacaoJulgamentosListadosParaApresentacao(
					historicoSessoes, tipoInformacaoRetorno);
		}

		return retorno;
	}
	
	/**
	 * Retorna um texto com valor StringUtils.EMPTY caso a lista historicoSessoes seja carregada por uma das situações ou o texto processoTrfHomeSessoesJulgamento.parametroNaoPrevisto
	 * @param situacaoJulgamento Junção das descrições (label) dos Enuns:TipoSituacaoPautaEnum e AdiadoVistaEnum e a palavra "Todos"  
	 * @param historicoSessoes Lista a ser carregada com as sessoes de acordo com o parémentro situacaoJulgamento.
	 * @param processo
	 * @return Texto com menssagem ou StringUtils.EMPTY quando a historicoSessoes for carregada
	 */	
	private String retornarTratamentoSituacaoJulgamento(
			String situacaoJulgamento,
			List<SessaoPautaProcessoTrf> historicoSessoes, ProcessoTrf processo) {
		String retorno = StringUtils.EMPTY;
		SessaoPautaProcessoTrfManager manager = ComponentUtil
				.getComponent("sessaoPautaProcessoTrfManager");

		if (situacaoJulgamento
				.equals(SESSOES_JULGAMENTO_TIPO_CONSULTA_SITUACAO_TODOS)) {
			historicoSessoes.addAll(manager
					.getSessoesJulgamentoPautados(processo));
		} else if (TipoSituacaoPautaEnum.isComtem(situacaoJulgamento)) {
			historicoSessoes.addAll(manager.getSessoesJulgamentoPautados(
					processo,
					TipoSituacaoPautaEnum.getEnum(situacaoJulgamento)));
		} else if (AdiadoVistaEnum.isComtem(situacaoJulgamento)) {
			historicoSessoes.addAll(manager.getSessoesNaoJulgadasPautadas(
					processo, AdiadoVistaEnum.getEnum(situacaoJulgamento)));
		} else {
			retorno = FacesUtil.getMessage("entity_messages", "processoTrfHomeSessoesJulgamento.parametroNaoPrevisto", situacaoJulgamento);
		}
		return retorno;
	}


	/**
	 * Retorna um texto com uma lista das sessões de julgamento do processo da lista informada.
	 * @param sessoesJulgamento Uma lista de sessoes de julgamento  
	 * @param tipoInformacaoRetorno Tipo de informaééo que seré impressa na lista a ser retornada.
	 * @return Texto com lista das sessões com apenas o texto extraédo do Metodo selecionado para apresentaééo
	 */
	private String carregarDetalhesSituacaoJulgamentosListadosParaApresentacao(
			List<SessaoPautaProcessoTrf> sessoesJulgamento,
			String tipoInformacaoRetorno) throws PJeBusinessException {
		String retorno = StringUtils.EMPTY;

		if (sessoesJulgamento != null && !sessoesJulgamento.isEmpty()) {

			if (StringUtils.isNotBlank(tipoInformacaoRetorno)) {
				StringBuilder sb = new StringBuilder();

				for (SessaoPautaProcessoTrf sessaoProcesso : sessoesJulgamento) {
					sb.append(obterDetalhesJulgamento(tipoInformacaoRetorno,
							sessaoProcesso));
					sb.append(SESSOES_JULGAMENTO_SEPARACAO_RESULTADO_LISTA);
				}
				retorno = sb.toString();

			} else {
				throw new PJeBusinessException("processoTrfHomeSessoesJulgamento.tipoRetornoNaoNuloEmBranco");
			}
		}
		return retorno;
	}
	
	
	/**
	 * Retorna um texto com a Descrição do apelido de uma sessão de julgamento
	 * @param sessaoProcesso sessão de Julgamento em questão  
	 * @return Texto texto com a Descrição do apelido da sessão para apresentaééo
	 */	
	private String obterApelidoSessao(SessaoPautaProcessoTrf sessaoProcesso) {
		StringBuilder sb = new StringBuilder();
		
		if (sessaoProcesso.getSessao().getApelido() != null){					
			sb.append(sessaoProcesso.getSessao().getApelido());					
		} else {
			sb.append(SESSOES_JULGAMENTO_VALOR_RESULTADO_NAO_CADASTRADO);
		}
		return sb.toString();
	}

	/**
	 * Retorna um texto com a Descrição do situaééo de uma sessão de julgamento
	 * @param sessaoProcesso sessão de Julgamento em questão  
	 * @return Texto texto com a Descrição da situaééo do julgamento para apresentaééo
	 */	
	private String obterSituacaoJulgamento(SessaoPautaProcessoTrf sessaoProcesso) {
		String situacaoParaRetorno = null;
		
		if (sessaoProcesso.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.NJ) && (sessaoProcesso.getAdiadoVista() != null)) {				
			situacaoParaRetorno = sessaoProcesso.getAdiadoVista().name();
		} else {
			situacaoParaRetorno = sessaoProcesso.getSituacaoJulgamento().name();
		}
		return situacaoParaRetorno;
	}

	/**
	 * Retorna um texto com a Descrição de acordo com tipo de retorno selecionado (tipoInformacaoRetorno) para a sessão de julgamento
	 * @param sessaoProcesso sessão de Julgamento em questão  
	 * @param tipoInformacaoRetorno Tipo de informaééo que seré retornada da sessão de julgamento
	 * @return Texto texto com a Descrição da informaééo selecionada da sessão de julgamento para apresentaééo
	 */		
	private String obterDetalhesJulgamento(String tipoInformacaoRetorno, SessaoPautaProcessoTrf sessaoProcesso) throws PJeBusinessException {
		StringBuilder detalhesJulgamentoSb = new StringBuilder();
		
		if (tipoInformacaoRetorno.equals(SESSOES_JULGAMENTO_TIPO_RETORNO_CONSULTA_DATA_SESSAO)) {
			detalhesJulgamentoSb.append(DateUtil.dateToString(sessaoProcesso.getSessao().getDataSessao()));
			
		} else if (tipoInformacaoRetorno.equals(SESSOES_JULGAMENTO_TIPO_RETORNO_CONSULTA_APELIDO)) {
			detalhesJulgamentoSb.append(obterApelidoSessao(sessaoProcesso));
			
		} else if (tipoInformacaoRetorno.equals(SESSOES_JULGAMENTO_TIPO_RETORNO_CONSULTA_SITUACAO_JULGAMENTO)) {
			detalhesJulgamentoSb.append(obterSituacaoJulgamento(sessaoProcesso));
			
		} else {
			throw new PJeBusinessException("processoTrfHomeSessoesJulgamento.tipoRetornoNaoPrevisto");
		}
		
		return detalhesJulgamentoSb.toString();
	}	

	
	/**
	 * Retorna um texto com uma lista das sessões de julgamento do processo por apelido ordenada por apelido.
	 * @param situacaoJulgamento Junção das descrições (label) dos Enuns:TipoSituacaoPautaEnum e AdiadoVistaEnum e a palavra "Todos"  
	 * @param processoTrf
	 * @return Texto com lista das sessões com apenas o texto dos apelidos das sessões para apresentaééo
	 */	
	
	public String sessaoJulgamentoApelido(String situacaoJulgamento,ProcessoTrf processoTrf) throws PJeBusinessException{	

		try {
			return obterTextoListaSessaoJulgamentoPorDetalhe(processoTrf, situacaoJulgamento, SESSOES_JULGAMENTO_TIPO_RETORNO_CONSULTA_APELIDO);
		} catch (PJeBusinessException e) {
			return FacesUtil.getMessage("entity_messages", "processoTrfHomeSessoesJulgamento.erroGeralGerando", situacaoJulgamento);			
		}			
	}
	
	/**
	 * Retorna um texto com uma lista das sessões de julgamento do processo por apelido ordenada por apelido.
	 * @param situacaoJulgamento Junção das descrições (label) dos Enuns:TipoSituacaoPautaEnum e AdiadoVistaEnum e a palavra "Todos"  
	 * @param processoTrf
	 * @return Texto com lista das sessões com apenas as datas das sessões para apresentaééo
	 */	
	public String sessaoJulgamentoDataSessao(String situacaoJulgamento,ProcessoTrf processoTrf){
		try {
			return obterTextoListaSessaoJulgamentoPorDetalhe(processoTrf, situacaoJulgamento, SESSOES_JULGAMENTO_TIPO_RETORNO_CONSULTA_DATA_SESSAO);
		} catch (PJeBusinessException e) {
			return FacesUtil.getMessage("entity_messages", "processoTrfHomeSessoesJulgamento.erroGeralGerando");			
		}	
	}
	
	/**
	 * Retorna um texto com uma lista das sessões de julgamento do processo por apelido ordenada por apelido.
	 * @param situacaoJulgamento Junção das descrições (label) dos Enuns:TipoSituacaoPautaEnum e AdiadoVistaEnum e a palavra "Todos"  
	 * @param processoTrf
	 * @return Texto com lista das sessões com apenas as situações das sessões para apresentaééo
	 */	
	public String sessaoJulgamentoSituacao(String situacaoJulgamento,ProcessoTrf processoTrf){
		
		try {
			return obterTextoListaSessaoJulgamentoPorDetalhe(processoTrf, situacaoJulgamento, SESSOES_JULGAMENTO_TIPO_RETORNO_CONSULTA_SITUACAO_JULGAMENTO );
		} catch (PJeBusinessException e) {
			return FacesUtil.getMessage("entity_messages", "processoTrfHomeSessoesJulgamento.erroGeralGerando");			
		}	
	}	
	
	public Boolean getCaValido() {
		return caValido;
	}
	
	public void setCaValido(Boolean caValido) {
		this.caValido = caValido;
	}
	
	public String getCa() {
		return ca;
	}
	
	public void setCa(String ca) {
		this.ca = ca;
	}
	
	public Long getIdTaskInstance() {
		return idTaskInstance;
	}
	
	public void setIdTaskInstance(Long idTaskInstance) {
		this.idTaskInstance = idTaskInstance;
	}
	
	
	public List<ProcessoDocumento> consultarDocumentoJuntadoEhCiente(ProcessoTrf processo, Date dataReferencia) {
		return consultarDocumentoJuntadoEhCiente(processo, dataReferencia, null);
	}
	
	/**
	 * Consulta os documentos de um processo juntados a partir da data informada e que Não estejam 
	 * pendentes de ciência.
	 * A consulta usa o componente processoTrfDocumentoGrid (agrupador de documentos) para garantir
	 * que iré funcionar da mesma forma que o agrupador de documentos.
	 * 
	 * @param processo Processo
	 * @param dataReferencia Data de juntada
	 * @return documentos juntados sem pendência de ciência.
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> consultarDocumentoJuntadoEhCiente(
			ProcessoTrf processo, Date dataReferencia, Integer idDocumentoPrincipal) {
		List<ProcessoDocumento> resultado = new ArrayList<ProcessoDocumento>();
		
		if (processo != null) {
			if (getInstance().getIdProcessoTrf() == 0) {
				setInstance(processo);
			}
			
			if (getProcessoHome().getInstance().getIdProcesso() == 0) {
				getProcessoHome().setInstance(processo.getProcesso());
			}
			
			Expressions expression = Expressions.instance();
			GridQuery grid = ComponentUtil.getComponent(Grid.PROCESSO_TRF_DOCUMENTO_GRID, true);
			grid.setMaxResults(null);
			
			Contexts.getConversationContext().set("dataJuntada", dataReferencia);
			Contexts.getConversationContext().set("usuarioLogado", Authenticator.getUsuarioLogado());
			Contexts.getConversationContext().set("usuarioHome", UsuarioHome.instance());
			Contexts.getConversationContext().set("idDocumentoPrincipal", idDocumentoPrincipal);
			if (Authenticator.getPapelAtual() != null) {
				Contexts.getConversationContext().set("identificadorPapelAtual", Authenticator.getPapelAtual().getIdentificador());
			}
			
			List<ValueExpression<Object>> restrictions = grid.getRestrictions();
			restrictions.add(expression.createValueExpression("#{true} = true and o.dataJuntada is not null"));
			restrictions.add(expression.createValueExpression("o.dataJuntada >= #{dataJuntada}"));
			restrictions.add(expression.createValueExpression("o.ativo = #{true}"));
			if(idDocumentoPrincipal != null){
				restrictions.add(expression.createValueExpression("documentoPrincipal.idProcessoDocumento = #{idDocumentoPrincipal} "));
			}
			resultado = grid.getResultList();
			Contexts.removeFromAllContexts(Grid.PROCESSO_TRF_DOCUMENTO_GRID);
		}
		return resultado;
	}

	/**
	 * Metodo responsável por limpar o campo de Processo referência na tela de
	 * Cadastro de processo
	 */
	public void limparProcessoReferencia() {
		this.instance.setDesProcReferencia(null);
	}

	public ClasseJudicialManager getClasseJudicialManager() {
		if(classeJudicialManager == null) {
			classeJudicialManager = (ClasseJudicialManager) Component.getInstance(ClasseJudicialManager.NAME);
		}
		return classeJudicialManager;
	}
	
	/**
	 * Metodo responsável por limpar os dados do Cadastro de processo incidental
	 */
	public void limparDadosCadastroProcessoIncidental() {
		setarProcessoOriginario(null);
		this.instance.setClasseJudicial(null);
		this.instance.setJurisdicao(null);
		this.instance.setDesProcReferencia(null);
		this.instance.setProcessoOriginario(null);
		this.instance.setOrgaoJulgadorColegiado(null);
		this.instance.setOrgaoJulgador(null);
		this.instance.setProcessoReferencia(null);
	}
	
	/**
	 * Retorna a jurisdicao baseado no orgao julgador passado por parametro
	 * @param	orgaoJulgador
	 * @return	retorna uma jurisdicao se existir um orgaoJulgador. Caso contrario, retorna uma string vazia.
	 */
	public String obterJurisdicao(OrgaoJulgador orgaoJulgador){
		String retorno = StringUtils.EMPTY;
		if (orgaoJulgador != null) {
			retorno = this.instance.getJurisdicao().getJurisdicao();
		}
		return retorno;
	}

	public ProcessoTrf getProcessoEncontrado() {
		return processoEncontrado;
	}

	public void setProcessoEncontrado(ProcessoTrf processoEncontrado) {
		this.processoEncontrado = processoEncontrado;
	}
	
	/**
	 * Obter o label a ser apresentado no campo referente é jurisdição.
	 */
	public String obterLabelJurisdicaoOuSecao(){
		return FacesUtil.getMessage("processoTrf.jurisdicao");
	}

	public Boolean getExibeCertidao() {
		return exibeCertidao;
	}

	public void setExibeCertidao(Boolean exibeCertidao) {
		this.exibeCertidao = exibeCertidao;
	}

	public boolean getCadastraProcessoConsumidorGovBr() {
		return cadastraProcessoConsumidorGovBr;
	}
	
	public void setCadastraProcessoConsumidorGovBr(boolean cadastraProcessoConsumidorGovBr) {
		this.cadastraProcessoConsumidorGovBr = cadastraProcessoConsumidorGovBr;
	}

	public Fornecedor getReclamado() {
		return reclamado;
	}

	public void setReclamado(Fornecedor reclamado) {
		this.reclamado = reclamado;
	}

	public List<Reclamacao> getReclamacoes() {
		return reclamacoes;
	}
	
	public String getDocumentoReclamante() {
		return documentoReclamante;
	}

	public void setDocumentoReclamante(String documentoReclamante) {
		this.documentoReclamante = documentoReclamante;
	}
	
	public Double getValorCausa() {
		return valorCausa;
	}

	public void setValorCausa(Double valorCausa) {
		this.valorCausa = valorCausa;
	}

	public Boolean getConcordaTermosUsoServico() {
		return concordaTermosUsoServico;
	}

	public void setConcordaTermosUsoServico(Boolean concordaTermosUsoServico) {
		this.concordaTermosUsoServico = concordaTermosUsoServico;
	}

	public PessoaFisica getReclamante() {
		return reclamante;
	}

	public String getUrlConsumidorGovBr() {
		return urlConsumidorGovBr;
	}

	/**
	 * Metodo para controlar a exibição da coluna "Certidão", na Relação de documentos de um processo.
	 * @return	verdadeiro se a propriedade exibeCertidao for nula ou tiver o valor true.
	 */
	public Boolean exibirColunaCertidao() {
		return BooleanUtils.isTrue(this.exibeCertidao);
	}
	
	public TipoParte getTipoParteAtivo(){	
		return ComponentUtil.getComponent(TipoParteManager.class)
			.tipoPartePorClasseJudicial(this.instance.getClasseJudicial(), ProcessoParteParticipacaoEnum.A);
	}
	
	public TipoParte getTipoPartePassivo(){	
		return ComponentUtil.getComponent(TipoParteManager.class)
			.tipoPartePorClasseJudicial(this.instance.getClasseJudicial(), ProcessoParteParticipacaoEnum.P);
	}
	
	public TipoParte getTipoParteTerceiros(){	
		return ComponentUtil.getComponent(TipoParteManager.class)
			.tipoPartePorClasseJudicial(this.instance.getClasseJudicial(), ProcessoParteParticipacaoEnum.T);
	}
	
	private Boolean ExisteMenor= Boolean.FALSE;
	private Boolean ExisteDFP= Boolean.FALSE;
	private Boolean ExisteMP= Boolean.FALSE;
	private Boolean ExisteHabilitacao = Boolean.FALSE;
	
	private ProcessoTrf processoCaixa;
	
	private List<Integer> cacheIdProcesso = new ArrayList<Integer>();
	
	
	public Boolean populaIcones(ProcessoTrf processoTrf) {

		if(cacheIdProcesso.contains(processoTrf.getIdProcessoTrf())){
			return true;
		}
		else {
			cacheIdProcesso.add(processoTrf.getIdProcessoTrf());
		}
		
		setExisteMenor(false);
		setExisteDFP(false);
		setExisteMP(false);
		setExisteHabilitacao(false);
		
		
	
		
		ProcessoTrfManager processoTrfManager = ComponentUtil.getComponent(ProcessoTrfManager.NAME);
		this.setProcessoCaixa(processoTrfManager.getProcessoTrfByProcesso(processoTrf));
		
		//Recupera as MP ou DFP e Verifica se tem menor como parte
		ArrayList<ProcessoParte> processoParteList = new ArrayList<ProcessoParte>(0);
		
	
		processoParteList.addAll(getProcessoCaixa().getListaParteAtivo());
		processoParteList.addAll(getProcessoCaixa().getListaPartePassivo());
		processoParteList.addAll(getProcessoCaixa().getListaParteTerceiro());
	
		
		try {
			
			
			HabilitacaoAutosManager habilitacaoAutosManager = ComponentUtil.getComponent("habilitacaoAutosManager");
			List<ProcessoParte> habilitacaoAutosList = habilitacaoAutosManager.getProcessoHabilitacaoPendente(processoTrf);
			
			if(habilitacaoAutosList.size()>0) {
				setExisteHabilitacao(true);
			}
			
			for (ProcessoParte processoParte : processoParteList) {
				if (!processoParte.getTipoParte().equals(tryParseInt(ParametroUtil.getParametro(Parametros.TIPOPARTEADVOGADO)))) {
					try {
						if (processoParte.getPessoa() instanceof PessoaFisica) {
							PessoaFisica pessoa = (PessoaFisica)processoParte.getPessoa();
							if (pessoa != null){
							  if (pessoa.isMenor() ) setExisteMenor(true);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if (processoParte.getProcuradoria()!=null) {
						if (processoParte.getProcuradoria().getTipo() == TipoProcuradoriaEnum.D) setExisteDFP(true);
						if (processoParte.getProcuradoria().getIdProcuradoria() ==  tryParseInt(ParametroUtil.getParametro(Parametros.ID_MINISTERIO_PUBLICO))) setExisteMP(true);
					}
				}
			}
		
		 

		
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private Integer tryParseInt(String value) { 
		 try {  
		     return Integer.parseInt(value);  
		  } catch(NumberFormatException nfe) {  
		      // Log exception.
		      return 0;
		  }  
	}
	
	/**
	 * Recupera texto com as partes do processo
	 * @param processoParteParticipacaoEnum
	 * @throws PJeBusinessException 
	 */
	public String recuperarParte(ProcessoParteParticipacaoEnum... participacoes) throws PJeBusinessException {
		StringBuilder retorno = new StringBuilder("");
		ProcessoTrf processo = this.getProcessoTrf();
		if(processo == null) {
			org.jbpm.graph.exe.ProcessInstance instancia = org.jboss.seam.bpm.ProcessInstance.instance(); 
			if( instancia != null && instancia.getContextInstance() != null ) {
				processo = ComponentUtil.getTramitacaoProcessualService().recuperaProcesso();
			} 
			if( processo == null ) {
				try{
					processo = ComponentUtil.getListProcessoCompletoBetaAction().getProcessoSelecionado();
				} catch(Exception e) {
					throw new PJeBusinessException(e.getLocalizedMessage());
				}
			}
		}
		if( processo != null ) {
			retorno.append("<table>");
			for(ProcessoParteParticipacaoEnum participacao: participacoes){
				for(ProcessoParte polo: processo.getListaPartePoloObj(false, participacao)){
					if(polo.getPartePrincipal()){
						retorno = retorno.append("<tr><td colspan=2>" + polo.getPoloTipoParteStr() + "</td><td>:");
						retorno = retorno.append(polo.getNomeParte() + "</td></tr>");
						for(ProcessoParteRepresentante representante: polo.getProcessoParteRepresentanteList()){
							if(representante.getParteRepresentante().getInSituacao().equals(ProcessoParteSituacaoEnum.A)) {					
								retorno.append("<tr><td></td><td>" + representante.getParteRepresentante().getTipoParte().getTipoParte() + "</td><td>:" + representante.getRepresentante().getNome() + "</td></tr>");
							}
						}
					}
				}
			
			}
			retorno.append("</table>");
		}
		return retorno.toString();
	}

	public Boolean getExisteMenor() {
		return ExisteMenor;
	}

	public void setExisteMenor(Boolean existeMenor) {
		ExisteMenor = existeMenor;
	}

	public Boolean getExisteDFP() {
		return ExisteDFP;
	}

	public void setExisteDFP(Boolean existeDFP) {
		ExisteDFP = existeDFP;
	}

	public Boolean getExisteMP() {
		return ExisteMP;
	}

	public void setExisteMP(Boolean existeMP) {
		ExisteMP = existeMP;
	}

	public Boolean getExisteHabilitacao() {
		return ExisteHabilitacao;
	}

	public void setExisteHabilitacao(Boolean existeHabilitacao) {
		ExisteHabilitacao = existeHabilitacao;
	}

	public ProcessoTrf getProcessoCaixa() {
		return processoCaixa;
	}

	public void setProcessoCaixa(ProcessoTrf processoCaixa) {
		this.processoCaixa = processoCaixa;
	}
	
	public Estado getEstadoJurisdicao() {
		return estadoJurisdicao;
	}

	public void setEstadoJurisdicao(Estado estadoJurisdicao) {
		this.estadoJurisdicao = estadoJurisdicao;
	}
	
	public Municipio getMunicipioJurisdicao() {
		return municipioJurisdicao;
	}

	public void setMunicipioJurisdicao(Municipio municipioJurisdicao) {
		this.municipioJurisdicao = municipioJurisdicao;
	}

	public Eleicao getEleicao() {
		return eleicao;
	}

	public Boolean getEhClasseInfracional() {
		return ehClasseInfracional;
	}

	public void setEhClasseInfracional(Boolean ehClasseInfracional) {
		this.ehClasseInfracional = ehClasseInfracional;
	}
	
	public boolean isClasseCriminalOuInfracional() {
		return getEhClasseCriminal() || getEhClasseInfracional();
	}
	
	public void setEleicao(Eleicao eleicao) {
		this.eleicao = eleicao;
	}
	
	public boolean getMostraDadosEleicao() {
		return ComponentUtil.getComponent(ParametroUtil.class).isJusticaEleitoralAndPrimeiroGrau();
	}

	public boolean getMostraAbaEleicao() {
		return ParametroJtUtil.instance().justicaEleitoral() && !ParametroUtil.instance().isPrimeiroGrau();
	}

	public Boolean getAcaoAmbiental() {
		if(acaoAmbiental == null) {
			acaoAmbiental = isProcessoAmbiental();
		}
		return acaoAmbiental;
	}
	
	public Boolean getAcaoAmbiental(ProcessoTrf processoTrf) {
		if(acaoAmbiental == null) {
			acaoAmbiental = isProcessoAmbiental(processoTrf);
		}
		return acaoAmbiental;
	}

	private Boolean isProcessoAmbiental() {
		return isProcessoAmbiental(getInstance());
	}

	public void setAcaoAmbiental(Boolean acaoAmbiental) {
		this.acaoAmbiental = acaoAmbiental;
	}
	
	private Boolean isProcessoAmbiental(ProcessoTrf processoTrf) {
		String codAgrupamentoAmbiental = Constantes.COD_AGRUPAMENTO_AMBIENTAL;
		TramitacaoProcessualService tps = (TramitacaoProcessualService)Component.getInstance("tramitacaoProcessualService");
		AgrupamentoClasseJudicialManager acjm = (AgrupamentoClasseJudicialManager) Component.getInstance("agrupamentoClasseJudicialManager");
		return acjm.pertence(processoTrf.getClasseJudicial(), codAgrupamentoAmbiental) || 
				tps.temAssuntoDoGrupo(processoTrf.getIdProcessoTrf(),codAgrupamentoAmbiental);
	}
	
	public Boolean temArquivoKml() {
		List<ProcessoDocumento> processoDocumentoList = getInstance().getProcesso().getProcessoDocumentoList();
		Boolean temKml = false;
		for (ProcessoDocumento procDoc : processoDocumentoList) {
			ProcessoDocumentoBin pBin = procDoc.getProcessoDocumentoBin();
			
			if(Boolean.TRUE.equals(getAcaoAmbiental()) && pBin.getExtensao() != null && pBin.getExtensao().equals(MimetypeUtil.MIME_TYPE_KML)) {
				temKml = true;
				break;
			}
			
		}
		return temKml;
	}

	public boolean classeAlterada() {
		ClasseJudicial classeAntiga = this.getClasseJudicialAnterior();
		ClasseJudicial classeNova = this.instance.getClasseJudicial();
		if (classeAntiga != null && classeNova != null && !classeAntiga.equals(classeNova)) {
			return true;
		}
		return false;
	}
	/**
	 * @return True se a classe for de Execução fiscal.
	 */
	public Boolean isClasseExecucaoFiscal(){
		ClasseJudicial classeJudicial = getInstance().getClasseJudicial();
		return isClasseExecucaoFiscal(classeJudicial);
	}
	
	/**
	 * @param classeJudicial ClasseJudicial
	 * @return True se a classe for de execução fiscal.
	 */
	public Boolean isClasseExecucaoFiscal(ClasseJudicial classeJudicial){
		return this.getClasseJudicialManager().isClasseExecucaoFiscal(classeJudicial);
	}
	
	/**
	 * Retorna true se o processo estiver arquivado.
	 * 
	 * @param processo
	 * @return Booleano
	 */
	public Boolean isArquivado(ProcessoTrf processo) {
		Boolean resultado = Boolean.FALSE;
		
		Evento eventoArquivamentoDefinitivo = ParametroUtil.instance().getEventoArquivamentoDefinitivoProcessual();
		Evento eventoBaixaDefinitiva = ParametroUtil.instance().getEventoBaixaDefinitivaProcessual();
		String arquivamentoDefinitivo = (eventoArquivamentoDefinitivo != null ? eventoArquivamentoDefinitivo.getCodEvento() : null);
		String baixaDefinitiva = (eventoBaixaDefinitiva != null ? eventoBaixaDefinitiva.getCodEvento() : null);
		
		ProcessoEventoManager processoEventoManager = ComponentUtil.getComponent(ProcessoEventoManager.class);
		ProcessoEvento processoEvento = processoEventoManager.recuperaUltimaMovimentacao(processo);
		if (processoEvento != null && processoEvento.getEvento() != null) {
			String evento = processoEvento.getEvento().getCodEvento();
			resultado = (StringUtils.equals(evento, arquivamentoDefinitivo) || StringUtils.equals(evento, baixaDefinitiva));
		}
		return resultado;
		
	}

	public List<ClasseJudicial> recuperarClassesJudiciaisIncidentais(int idJurisdicao, int idCompetencia, int idClasseJudicial, boolean somenteIncidental) {
		PesquisaProcessoParadigmaAction pesquisaProcessoParadigma = ComponentUtil.getComponent(PesquisaProcessoParadigmaAction.class);

		if (pesquisaProcessoParadigma.getOrigemSistema() != null && pesquisaProcessoParadigma.getOrigemSistema().equals(Constantes.ORIGEM_SISTEMA_DCP)) {
			String codClasseJudicial = ParametroUtil.getParametro("codClasseJudicialIncidentaisDcp");

			Set<String> codigos = Arrays.stream(codClasseJudicial.split(","))
					.map(String::trim)
					.collect(Collectors.toSet());

			List<ClasseJudicial> classeJudicials = this.getClasseJudicialManager().recuperarClassesJudiciaisIncidentais(idJurisdicao, idCompetencia, idClasseJudicial, somenteIncidental);

			List<ClasseJudicial> filteredClasseJudicials = classeJudicials.stream().filter(classeJudicial ->
					classeJudicial.getCodClasseJudicial() != null &&
							codigos.contains(classeJudicial.getCodClasseJudicial().trim())
			).collect(Collectors.toList());

			if (filteredClasseJudicials != null || !filteredClasseJudicials.isEmpty()) {
				return filteredClasseJudicials;
			}
		}
		return this.getClasseJudicialManager().recuperarClassesJudiciaisIncidentais(idJurisdicao, idCompetencia, idClasseJudicial, somenteIncidental);
	}


	private void informarErroNaPrevencao(Exception e) {
		log.error("Erro ao verificar prevencao", e);
		StringBuilder sb = new StringBuilder();
		sb.append("Erro ao verificar preveno: ");
		sb.append(System.lineSeparator());
		sb.append(e.getMessage());
		this.mensagemProtocolacao = sb.toString();
		FacesMessages.instance().add(Severity.ERROR, this.mensagemProtocolacao);
	}
	
}

