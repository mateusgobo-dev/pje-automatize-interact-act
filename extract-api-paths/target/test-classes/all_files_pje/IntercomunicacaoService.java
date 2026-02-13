package br.jus.cnj.pje.intercomunicacao.v222.servico;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.com.itx.exception.AplicationException;
import br.jus.cnj.intercomunicacao.v222.beans.*;
import br.jus.cnj.pje.nucleo.service.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.node.ProcessState;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.home.ProcessoPrioridadeProcessoHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.jbpm.actions.JbpmEventsHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.infox.pje.manager.PessoaProcuradoriaEntidadeManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.entidades.vo.PesquisaExpedientesVO;
import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;
import br.jus.cnj.pje.intercomunicacao.util.ComplementoClasseUtil;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.util.constant.XhtmlPath;
import br.jus.cnj.pje.intercomunicacao.v222.converter.DocumentoIdentificacaoParaTipoDocumentoIdentificacaoConverter;
import br.jus.cnj.pje.intercomunicacao.v222.converter.DocumentoProcessualParaProcessoDocumentoConverter;
import br.jus.cnj.pje.intercomunicacao.v222.converter.IntercomunicacaoEnderecoParaEnderecoConverter;
import br.jus.cnj.pje.intercomunicacao.v222.converter.ModalidadeVinculacaoProcessoParaTipoConexaoConverter;
import br.jus.cnj.pje.intercomunicacao.v222.converter.PessoaMNIParaPessoaPJEConverter;
import br.jus.cnj.pje.intercomunicacao.v222.converter.ProcessoDocumentoParaDocumentoConverter;
import br.jus.cnj.pje.intercomunicacao.v222.converter.ProcessoEventoParaMovimentacaoProcessualConverter;
import br.jus.cnj.pje.intercomunicacao.v222.converter.ProcessoParteExpedienteParaAvisoComunicacaoPendenteConverter;
import br.jus.cnj.pje.intercomunicacao.v222.converter.ProcessoParteExpedienteParaComunicacaoProcessual;
import br.jus.cnj.pje.intercomunicacao.v222.converter.ProcessoTrfParaCabecalhoProcessualConverter;
import br.jus.cnj.pje.intercomunicacao.v222.util.ConversorUtil;
import br.jus.cnj.pje.intercomunicacao.v222.util.IntercomunicacaoUtil;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIParametroUtil;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIUtil;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.identity.PjeIdentity;
import br.jus.cnj.pje.nucleo.manager.AssuntoTrfManager;
import br.jus.cnj.pje.nucleo.manager.CepManager;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ComplementoClasseManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.DownloadBinarioArquivoManager;
import br.jus.cnj.pje.nucleo.manager.EnderecoWsdlManager;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.JurisdicaoManager;
import br.jus.cnj.pje.nucleo.manager.ManifestacaoProcessualManager;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.PrioridadeProcessoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoAssuntoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoPeticaoNaoLidaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoInstanceManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteEnderecoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteHistoricoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteRepresentanteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoPesoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoPrioridadeProcessoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfConexaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.ProtocoloExternoMniManager;
import br.jus.cnj.pje.nucleo.manager.SituacaoProcessualManager;
import br.jus.cnj.pje.nucleo.manager.TipoParteManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TipoSituacaoProcessualManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.servicos.AutuacaoService;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.cnj.pje.servicos.MimeUtilChecker;
import br.jus.cnj.pje.servicos.NoDeDesvioService;
import br.jus.cnj.pje.servicos.PrevencaoService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.csjt.pje.business.pdf.XhtmlParaPdf;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.csjt.pje.business.service.PlantaoJudicialService;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.csjt.pje.view.action.ProcessoJTHome;
import br.jus.je.pje.manager.EleicaoManager;
import br.jus.pje.je.entidades.ComplementoProcessoJE;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.jt.entidades.AtividadeEconomica;
import br.jus.pje.jt.entidades.MunicipioIBGE;
import br.jus.pje.mni.entidades.DownloadBinario;
import br.jus.pje.mni.entidades.DownloadBinarioArquivo;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.ComplementoClasse;
import br.jus.pje.nucleo.entidades.ComplementoClasseProcessoTrf;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoInstance;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteHistorico;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoPrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.ProtocoloExternoMni;
import br.jus.pje.nucleo.entidades.RespostaExpediente;
import br.jus.pje.nucleo.entidades.SituacaoProcessual;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoSituacaoProcessual;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.ClasseJudicialInicialEnum;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoReferenciaEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.enums.ServicosPJeMNIEnum;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;
import br.jus.pje.nucleo.enums.TipoDocumentoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoExpedienteEnum;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Search;

@Name(IntercomunicacaoService.NAME)
@SuppressWarnings("java:S1192")
public class IntercomunicacaoService{
	public static final String NAME = "v222.intercomunicacaoService"; 

	// utilizado para buscar outros parametros MNI
	public static final String NOME_PARAMETRO_MNI_NO_DESTINO = "no_destino";
	public static final String NOME_PARAMETRO_MNI_DISTRIBUICAO = "distribuir";

	// utilizado para buscar entidade parametro pje
	private static final String NOME_PARAMETRO_PJE_UTILIZACAO_NO_DESVIO = "flagUtilizacaoNoDesvio";

	public enum PessoaQualificacaoEnum {
		ADVOGADO, ESCRITORIO_ADVOCACIA, MINISTERIO_PUBLICO, DEFENSORIA_PUBLICA, PROCURADORIA
	}

	// utilizado para carregar documentos
	public static final String HTML_MIME_TYPE = "text/html";
	public static final String P7S_MIME_TYPE = "application/pkcs7-signature";
	private static final String ASSINATURA_TESTE = "assinatura modo teste";
	
	// utilizado para validação
	private static final String NUMERO_PROCESSO_NULO = "00000000000000000000";

	// eventos lançados
	public static final String AFTER_EVENT_NAME = "br.jus.cnj.pje.intercomunicacao.v222.servico.AfterProcess";
	
	// Utilizado para verificar se a remessa foi efetuada com certificado
	private static final String PARAM_LOGOU_CERTIFICADO = "logouComCertificado";
	
	@In
	private DownloadBinarioArquivoManager downloadBinarioArquivoManager;

	@Logger
	private Log log;
	
	@In(create = true)
	private PrevencaoService prevencaoService;
	
	private ServicosPJeMNIEnum servicoAtual;

	private List<ProcessoParte> processoParteList = new ArrayList<ProcessoParte>();
	
	private List<TipoProcessoDocumento> tiposPrincipaisValidos = new ArrayList<>();
	
	private List<TipoProcessoDocumento> tiposAnexosValidos = new ArrayList<>();
	
	private Procuradoria orgaoRepresentacao;
	
	@In (required = false)
	@Out (required = false)
	private List<ProcessoDocumento> documentosRequisicao;
	
	@In
	private JurisdicaoManager jurisdicaoManager;

	@In 
	private ProcessoParteManager processoParteManager;
	
	//Constante que identifica
	private final String TODOS = "*";

	public static IntercomunicacaoService getInstance(){
		return ComponentUtil.getComponent(IntercomunicacaoService.class);
	}
	
	private Boolean ignorarAutenticacao = false;
	
private Boolean sinalizaProcessoMigrado = false;
  	
  	@In
	private  ParametroService parametroService;
  	
  	private static final String COMPLEMENTO_MOTIVO_DA_REMESSA = "motivo_da_remessa";
	private static final String COMPLEMENTO_DESTINO = "destino";
	private static final String DESCRICAO_REMESSA = "em razão de migração para outro sistema processual";
	private static final String DESCRICAO_DESTINO = "EPROC";
	private ProcessoTrf processoDCP;
  	
  	public void setIgnorarAutenticacao(Boolean ignorarAutenticacao) {
  		this.ignorarAutenticacao = ignorarAutenticacao;
  	}
  	
  	public Boolean getIgnoraAutenticacao() {
  		return ignorarAutenticacao;
  	}
  	
	/**
	 * Varivel global que identifica a requisio a partir de um PJE.
	 */
  	private boolean isRequisicaoDePJE = false;  
  	
  	private static final String CODIGO_ANDAMENTO_MIGRACAO = "pje:migrado";
	
	@In 
	private SituacaoProcessualManager situacaoProcessualManager;
	
	@In
	private TipoSituacaoProcessualManager tipoSituacaoProcessualManager;

  	/**
	 * Operação destinada a permitir a entrega de manifestação processual por
	 * órgão de representação processual ou por advogado. Essa operação também
	 * permite a entrega de petição inicial.
	 * 
	 * @param parametro
	 *            ManifestacaoProcessual
	 * @return returns número do processo.
	 */
	public String entregarManifestacaoProcessual(ManifestacaoProcessual parametro) throws Exception {
		setServicoAtual(ServicosPJeMNIEnum.EntregarManifestacaoProcessual);
		String numeroProcesso = null;
		String idOrgaoRepresentacao = MNIParametroUtil.obterValor(parametro, MNIParametro.getIdOrgaoRepresentacao());
		setIsRequisicaoDePJE(parametro);

		Boolean logouComCertificado = Boolean.parseBoolean(MNIParametroUtil.obterValor(parametro, MNIParametro.PARAM_LOGOU_COM_CERTIFICADO));
		Contexts.getSessionContext().set(PARAM_LOGOU_CERTIFICADO, logouComCertificado);
		
		if (isRespostaDeExpediente(parametro)) {
			validarAtributos(parametro, "numeroProcesso", "documento");
			responderExpediente(parametro);
			numeroProcesso = parametro.getNumeroProcesso().getValue();
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Validando...");
			}
			
			Parametro paramProtocolo = new Parametro();
			paramProtocolo.setNome(MNIParametro.PARAM_PROTOCOLO);
			
			if(idOrgaoRepresentacao != null){
				validarOrgaoRepresentacaoUsuario(idOrgaoRepresentacao);	
			}
			
			if (isNumeroProcessoValido(parametro.getNumeroProcesso())) {
	
				paramProtocolo.setValor("false");
				parametro.getParametros().add(paramProtocolo);
				
				
				String documento = atualizarProcesso(parametro);
				numeroProcesso = String.format("%s%s", parametro.getNumeroProcesso().getValue(), documento);
			} else {
	
				paramProtocolo.setValor("true");
				parametro.getParametros().add(paramProtocolo);
	
				numeroProcesso = protocolarProcesso(parametro);
			}
		}

		return numeroProcesso;
	}

	/**
	 * Operação destinada a permitir a consulta a um processo judicial. Não é
	 * necessário que o consultante seja representante de qualquer das partes
	 * componentes do processo. A implementação deverá assegurar que o processo
	 * somente seja retornado se o nível de sigilo interno permitir a consulta
	 * pelo requerente. A identificação do requerente somente será necessária
	 * quando inexistente a autenticação e autorização pelo certificado cliente
	 * do serviço. A operação retornará objeto do tipo
	 * 'tipoConsultarProcessoResposta', que contém, além dos dados básicos, um
	 * objeto do tipo 'tipoProcessoJudicial', definido pelo esquema
	 * http://www.cnj.jus.br/intercomunicacao-2.2, caso o processo exista e
	 * possa ser acessado pelo consultante.
	 * 
	 * 
	 * @param parametro RequisicaoConsultaProcesso.
	 * @return ProcessoJudicial com os dados do processo.
	 */
	public ProcessoJudicial consultarProcesso(RequisicaoConsultaProcesso parametro) throws Exception {
		setServicoAtual(ServicosPJeMNIEnum.ConsultarProcesso);
		Boolean deveCarregarPartesDeTodasSituacoes = Boolean.FALSE;
		
		validarAtributos(parametro, "idConsultante", "numeroProcesso");
		if (!NumeroProcessoUtil.numeroProcessoValido(parametro.getNumeroProcesso().getValue())) {
			throw new NegocioException(String.format("Número do processo inválido: %s ", parametro.getNumeroProcesso().getValue()));
		}
		ProcessoTrf processoTrf = obterProcessoTrf(parametro.getNumeroProcesso(), !ignorarAutenticacao);
		// Logar acesso aos autos
		LogAcessoAutosDownloadsService logAutos = ComponentUtil.getComponent(LogAcessoAutosDownloadsService.class);
		logAutos.logarAcessoProcessoMNI(processoTrf);

		Boolean incluirCabecalho = (parametro.isSetIncluirCabecalho() ? parametro.isIncluirCabecalho() : Boolean.TRUE);

		ProcessoJudicial resultado = new ProcessoJudicial();
		
		//Se true o metadata do arquivo será retornado.
		Boolean carregarMetadata = BooleanUtils.toBoolean(parametro.isIncluirDocumentos());

		if (carregarMetadata || parametro.getDocumento().contains(TODOS)) {
			String processosRestricao = ParametroUtil.getParametro("tjrj:download:restricao:processo");

			if (processosRestricao != null && processosRestricao.contains(processoTrf.getNumeroProcesso())) {
				String horaRestricaoInicio = ParametroUtil.getParametro("tjrj:download:restricao:horario:inicio");
				String horaRestricaoFim = ParametroUtil.getParametro("tjrj:download:restricao:horario:fim");
				Integer horaAtual = DateUtil.obterHora(DateService.instance().getDataHoraAtual());
				if (horaRestricaoInicio != null && horaRestricaoFim != null
						&& horaAtual >= Integer.parseInt(horaRestricaoInicio)
						&& horaAtual <= Integer.parseInt(horaRestricaoFim)) {

					throw new NegocioException(String.format("Não é permitida a consulta de documentos deste processo das %02d:00 até %02d:59 horas", Integer.parseInt(horaRestricaoInicio), Integer.parseInt(horaRestricaoFim)));
				}
			}
			processosRestricao = null;
		}		

		if (incluirCabecalho) {
			resultado.setDadosBasicos(converterParaCabecalhoProcessual(processoTrf, deveCarregarPartesDeTodasSituacoes));
		}
		resultado.getMovimento().addAll(consultarMovimentacaoProcessual(processoTrf, parametro));
		resultado.getDocumento().addAll(consultarDocumentoProcessual(processoTrf, parametro));
		
		return resultado;
	}
	
	/**
	 * Operação destinada a permitir que o consultante verifique a existência de
	 * avisos de comunicação processual pendentes junto ao tribunal fornecedor
	 * do serviço. Essa consulta poderá ser específica em relação a uma parte
	 * representada ou, ainda, genérica, relativa aos processos em que o
	 * consultante opera como órgão de representação processual (MP, defensoria
	 * pública, advocacia pública, escritório de advocacia e advogado).
	 * 
	 * @param parametro
	 *            RequisicaoConsultaAvisosPendentes
	 * @return lista de avisos de comunicação processual.
	 * @throws Exception
	 */
	public List<ProcessoParteExpediente> consultarAvisosPendentes(RequisicaoConsultaAvisosPendentes parametro) throws Exception {
		setServicoAtual(ServicosPJeMNIEnum.ConsultarAvisosPendentes);
		validarAtributos(parametro, "idConsultante", "senhaConsultante");
		
		List<ProcessoParteExpediente> expedientes = null;
		Date data = null;
		
		if(parametro.isSetDataReferencia() && !parametro.getDataReferencia().getValue().equals("")){
		  data = ConversorUtil.converterParaDate(parametro.getDataReferencia());
		  if (data == null){
			  throw new NegocioException("Parâmetro dataReferencia inválido, verifique se a máscara segue o padrão: "+MNIParametro.PARAM_FORMATO_DATA_HORA+"."); 
		  }
		}
		 
		
		//Todos os expedientes que o usuário 'consultante' tem acesso.
		//Caso seja passado o idRepresentado serão recuperados somente os expedientes desse usuário.
		//Somente serão recuperados os expedientes onde o idConsultante possua acesso de ciência.
		expedientes = consultarExpedientes(
				data, 
				parametro.getIdConsultante(), 
				parametro.getIdRepresentado(), 
				null, 
				null,
				Boolean.FALSE,
				TipoSituacaoExpedienteEnum.PENDENTES_CIENCIA);
		
		return expedientes;
	}

	/**
	 * Converte uma coleção de ProcessoParteExpediente para outra coleção de AvisoComunicacaoPendente.
	 * 
	 * @param expedientes coleção de ProcessoParteExpediente
	 * @return coleção de AvisoComunicacaoPendente
	 */
	public List<AvisoComunicacaoPendente> converterParaAvisoComunicacaoPendente(List<ProcessoParteExpediente> expedientes) {

		ProcessoParteExpedienteParaAvisoComunicacaoPendenteConverter converter = new ProcessoParteExpedienteParaAvisoComunicacaoPendenteConverter();
		return converter.converterColecao(expedientes);
	}

	/**
	 * Operação destinada a permitir a consulta a teor específico de comunicação
	 * processual pendente.
	 * 
	 * @param parametro
	 *            TipoConsultarTeorComunicacao
	 * @return lista de comunicação processual.
	 * @throws Exception
	 */
	public List<ComunicacaoProcessual> consultarTeorComunicacao(RequisicaoConsultarTeorComunicacao parametro) throws Exception {
		setServicoAtual(ServicosPJeMNIEnum.ConsultarTeorComunicacao);
		List<ProcessoParteExpediente> expedientes = new ArrayList<ProcessoParteExpediente>(0);
		
		if (StringUtil.isNotEmpty(parametro.getIdentificadorAviso().getValue())) {
			Identificador aviso = parametro.getIdentificadorAviso();
			Integer idAviso = Integer.parseInt(aviso.getValue());
			if(log.isDebugEnabled()){
				log.debug("consultando expediente " +  aviso.getValue());
			}

			ProcessoParteExpediente processoParteExpediente = ComponentUtil.getComponent(ProcessoParteExpedienteManager.class).findById(idAviso);
			if(processoParteExpediente == null){
				throw new IntercomunicacaoException(String.format("Aviso %s não encontrado", idAviso));
			}
			expedientes.add(processoParteExpediente);
		} else if (StringUtil.isNotEmpty(parametro.getNumeroProcesso().getValue())) {
			if (log.isDebugEnabled()) {
				log.debug("consultando expedientes do processo " + parametro.getNumeroProcesso().getValue()); 
		}
			expedientes.addAll(consultarExpedientes(null, parametro.getIdConsultante(), null, parametro.getNumeroProcesso().getValue(), null, null, TipoSituacaoExpedienteEnum.PENDENTES_CIENCIA_RESPOSTA));
		} else {
			throw new NegocioException("O número do processo ou o identificador da comunicação devem ser informados!");
		}
		
		return converterParaComunicacaoProcessual(expedientes);
	}
	
	/**
	 * Gera o PDF de recibo de entrega de manifestação processual.
	 * 
	 * @param numeroProcesso
	 *            Número do processo.
	 * @return bytes do PDF gerado.
	 * @throws PJeBusinessException
	 */
	public byte[] obterReciboEntregaManifestacaoProcessual(NumeroUnico numeroProcesso) throws PJeBusinessException {
		byte[] resultado = null;
		ProcessoTrf processo = obterProcessoTrf(numeroProcesso, false);
		
		if (getDocumentosRequisicao() != null) {
			processo.getProcesso().setProcessoDocumentoList(getDocumentosRequisicao());
		}
		
		ProcessoTrfHome home = ComponentUtil.getComponent(ProcessoTrfHome.class, true);
		home.setInstance(processo);
		home.setPessoaLogada(Authenticator.getPessoaLogada());
		resultado = XhtmlParaPdf.converterParaBytes(XhtmlPath.getPdfReciboEntregaManifestacaoProcessual());
		
		EntityUtil.getEntityManager().clear();
		return resultado;
	}

	private void salvarManifestacao(ManifestacaoProcessual tipoManifestacaoProcessual, ProcessoTrf processoTrf) throws Exception {
		try {
			br.jus.pje.nucleo.entidades.ManifestacaoProcessual entityManifestacaoProcessual = new br.jus.pje.nucleo.entidades.ManifestacaoProcessual();
			
			String instanciaOrigem = MNIParametroUtil.obterValor(tipoManifestacaoProcessual,MNIParametro.PARAM_INSTANCIA_PROCESSO_ORIGEM);
			if(Objects.isNull(instanciaOrigem)) {
				entityManifestacaoProcessual.setCodigoOrigem(ParametroUtil.instance().getInstancia());
				entityManifestacaoProcessual.setCodigoAplicacaoOrigem(ParametroUtil.instance().getInstancia());
			} else {
				entityManifestacaoProcessual.setCodigoOrigem(instanciaOrigem);
				entityManifestacaoProcessual.setCodigoAplicacaoOrigem(instanciaOrigem);
			}
			entityManifestacaoProcessual.setDataRecebimento(new Date());
			entityManifestacaoProcessual.setProcessoTrf(processoTrf);

			ComponentUtil.getComponent(ManifestacaoProcessualManager.class).persist(entityManifestacaoProcessual);

			EntityUtil.flush();
		} catch (Exception e) {
			throw new IntercomunicacaoException("Erro ao Serializar a Manifestação Processual", e);
		}
	}

	private String atualizarProcesso(ManifestacaoProcessual tipoManifestacaoProcessual) throws Exception {
		// carregar o processo
		ProcessoTrf processoTrf = obterProcessoTrf(tipoManifestacaoProcessual.getNumeroProcesso(), false);
		ProcessoDocumento documentoPrincipal = null;
		ProcessoTrfHome.instance().setInstance(processoTrf);
		
		boolean identificadorValidado = validarIdentificadorSistemaExterno(tipoManifestacaoProcessual);
		
		validarTipoDocumento(tipoManifestacaoProcessual.getDocumento(), false, true, Integer.valueOf(processoTrf.getClasseJudicial().getCodClasseJudicial()));
		validarProcessoRemetido(processoTrf);
		validarProcessoMigracaoBloqueada(processoTrf);
		validarProcessoPeticaoBloqueada(processoTrf);
		validarAtendimentoPlantao(tipoManifestacaoProcessual);
		validarSinalizacaoMigracao(processoTrf, tipoManifestacaoProcessual, Authenticator.getPessoaLogada());
		
		prevencaoService.realizaPrevencaoDeProcessoBuscandoOutrosProcessosQueFazemReferenciaAhEle(processoTrf);				
		
		//PJEII-18990
		//Requisição feita por pessoa não associada ao processo.
		boolean isPessoaNaoAssociada = !isParteAssociada(Authenticator.getPessoaLogada(), processoTrf); 
		String numeroProcesso1G = getNumeroProcesso1Grau(tipoManifestacaoProcessual);
		List<ProcessoDocumento> documentos = carregarColecaoDocumentoProcessual(
				tipoManifestacaoProcessual.getDocumento(), 
				processoTrf.getProcesso(), 
				null, 
				numeroProcesso1G);
		
		if (isAtendimentoPlantao(tipoManifestacaoProcessual)) {
			PlantaoJudicialService.instance().registraSeDeveIrParaPlantao();
		}

		// Armazena os documentos da requisição para apresentá-los no recibo.
		setDocumentosRequisicao(documentos);
		
		//PJEII-22205 Adição da condição !isRemessa
		boolean isRemessa = MNIParametroUtil.obterValor(tipoManifestacaoProcessual, MNIParametro.PARAM_URL_ORIGEM_ENVIO) != null;
		if(isPessoaNaoAssociada && !isRemessa){
			ProcessoDocumentoPeticaoNaoLidaManager processoDocumentoPeticaoNaoLidaManager = ComponentUtil.getComponent(ProcessoDocumentoPeticaoNaoLidaManager.class);
			for(ProcessoDocumento processoDocumento : documentos){
				ProcessoDocumentoPeticaoNaoLida peticaoAvulsa = new ProcessoDocumentoPeticaoNaoLida();
				peticaoAvulsa.setProcessoDocumento(processoDocumento);
				peticaoAvulsa.setRetificado(false);
				peticaoAvulsa.setRetirado(false);
				processoDocumentoPeticaoNaoLidaManager.persist(peticaoAvulsa);
			}
		}
		
		if (isRespostaDeExpedientePostados(tipoManifestacaoProcessual)) {
			Map<String, Object> mapasIdsProcessoParteExpedientesPostadosRetornados = new HashMap<String, Object>();
			Parametro idsProcessoParteExpediente = MNIParametroUtil.obter(tipoManifestacaoProcessual, MNIParametro.getIdRetornoProcessoParteExpedientePostado());
			mapasIdsProcessoParteExpedientesPostadosRetornados.put(Variaveis.PJE_ID_PPE_RETORNO_POSTAGEM, idsProcessoParteExpediente.getValor());
			documentoPrincipal = iniciarFluxoDocumentoPrincipal(processoTrf, documentos, mapasIdsProcessoParteExpedientesPostadosRetornados, true);
		} else {
			documentoPrincipal = iniciarFluxoDocumentoPrincipal(processoTrf, documentos);
		}

		// TODO atualizar os movimentos
		if(identificadorValidado) {
			salvarIdentificadorExterno(tipoManifestacaoProcessual, processoTrf.getProcesso());
		}		
		salvarManifestacao(tipoManifestacaoProcessual, processoTrf);
		Events.instance().raiseEvent(AFTER_EVENT_NAME, tipoManifestacaoProcessual, processoTrf, documentoPrincipal);
		EntityUtil.flush();
		/*
		 * Início - PJEII-8136 - Registro de prioridade processual é perdido ao
		 * ser remetido ou baixado processo. [CSJT] - Thiago Oliveira -
		 * 04/06/2013
		*/
		for (Parametro param : tipoManifestacaoProcessual.getParametros()) {
			if (param.getNome().equals(MNIParametro.PARAM_PRIORIDADE_PROCESSUAL)) {
				this.deletaEAtualizaProcessoPrioridadeProcesso(processoTrf, param);
			}
		}
		/*
		* Fim - PJEII-8136
		*/
		if (isRequisicaoDePJE && 
			(documentoPrincipal == null ||
				ParametroUtil.instance().getTipoProcessoDocumentoComunicacaoEntreInstancias() == null ||
				!ParametroUtil.instance().getTipoProcessoDocumentoComunicacaoEntreInstancias().equals(documentoPrincipal.getTipoProcessoDocumento()))) {
			ComponentUtil.getComponent(ProcessoTrfManager.class).atualizaStatusRecebidoOutraInstancia(processoTrf.getIdProcessoTrf());
		}
		
		ProcessoDocumento principal = (documentos.isEmpty() == false? documentos.get(0): null);
		
		if(getSinalizaProcessoMigrado()) {
			sinalizaProcessoMigrado(processoTrf);
		}
		
		return principal != null && principal.getIdProcessoDocumento() > 0 ? Integer.toString(principal.getIdProcessoDocumento()) : "";
	} 

	private String protocolarProcesso(ManifestacaoProcessual tipoManifestacaoProcessual) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Carregando Jurisdição...");
		}
		validarProcessoDCP(tipoManifestacaoProcessual);
		
		boolean identificadorValidado = validarIdentificadorSistemaExterno(tipoManifestacaoProcessual);		
		
		validarManifestacaoProcessual(tipoManifestacaoProcessual);
		validarExistenciaPeticaoInicial(tipoManifestacaoProcessual.getDocumento(), tipoManifestacaoProcessual.getDadosBasicos().getClasseProcessual());
		validarTipoDocumento(tipoManifestacaoProcessual.getDocumento(), true, true, tipoManifestacaoProcessual.getDadosBasicos().getClasseProcessual());
		if (!this.isRemessaEntreInstancias()) {
			validarTamanhoEhMimetypeDocumento(tipoManifestacaoProcessual.getDocumento());
		}
		validarTipoPessoaPreenchido(tipoManifestacaoProcessual);
		validarDocumentoIdentificacaoPreenchido(tipoManifestacaoProcessual);
		validarDocumentoIdentificacao(tipoManifestacaoProcessual);
		validarExistenciaDocumentoIdentificacaoDosPolos(tipoManifestacaoProcessual);
		validarDocumentoIdentificacaoPrincipalDosPolos(tipoManifestacaoProcessual);
		verificarManifestacaoAdvogado(tipoManifestacaoProcessual);
		verificarManifestacaoJusPostulandi(tipoManifestacaoProcessual);
		verificarRemessa(tipoManifestacaoProcessual);
		verificarEnderecoWsdlOrigem(tipoManifestacaoProcessual);
		
		if(!tipoManifestacaoProcessual.getDadosBasicos().isSetCodigoLocalidade()){
			throw new NegocioException("Deve-se informar o código identificador da "
						+ "localidade a que pertence ou deve pertencer o processo.");
		}

		// Carrega a jurisdição
		Jurisdicao jurisdicao = carregarJurisdicao(tipoManifestacaoProcessual.getDadosBasicos().getCodigoLocalidade());

		if (log.isDebugEnabled()) {
			log.debug("Carregando classe...");
		}
		// Carrega a classe judicial
		ProcessoTrfHome processoTrfHome = ProcessoTrfHome.instance();
		processoTrfHome.getInstance().setJurisdicao(jurisdicao);
		String cdClasseJudicial = String.valueOf(tipoManifestacaoProcessual.getDadosBasicos().getClasseProcessual());
		processoTrfHome.setClasseJudicialFiltro(cdClasseJudicial);
		ClasseJudicial classeJudicial = ComponentUtil.getComponent(ClasseJudicialManager.class).findByCodigo(cdClasseJudicial);
				
		if (classeJudicial == null) {
			throw new PJeBusinessException("A classe judicial escolhida está inativa na jurisdição/localidade escolhida e não pode ser utilizada para protocolo.");
		}
		
		if (classeJudicial.getFluxo() == null) {
			throw new PJeBusinessException("A classe judicial escolhida não pertence a nenhum rito processual na jurisdição/localidade escolhida e não pode ser utilizada para protocolo.");
		}
		
		if (!isCorrelacaoClasseCompetenciaJurisdicaoValida(jurisdicao.getIdJurisdicao(), classeJudicial)) {
			throw new PJeBusinessException("A classe judicial escolhida não pertence a nenhuma competência na jurisdição/localidade escolhida e não pode ser utilizada para protocolo.");
		}
		
		if(!ignorarAutenticacao && ParametroUtil.instance().isJusPostulandi() && !classeJudicial.getJusPostulandi()){
			throw new NegocioException("A classe judicial escolhida não permite Jus Postulandi como manifestante "
					+ "sem os dados do representante processual.");
		}

		Boolean isAtendimentoPlantao = Boolean.parseBoolean(MNIParametroUtil.obterValor(tipoManifestacaoProcessual, MNIParametro.PARAM_ATENDIMENTO_PLANTAO));

		if (isAtendimentoPlantao && !isAtendimentoPlantaoPermitido()) {
			throw new NegocioException("Solicitação de atendimento no plantão judiciário fora do horário permitido. ");
		}
		
		validarVinculacaoReferencia(tipoManifestacaoProcessual, classeJudicial);
		validarExistenciaParte(tipoManifestacaoProcessual, classeJudicial);
		validarEnteOuAutoridade(tipoManifestacaoProcessual, classeJudicial);
		validarClasseExigeNumeracaoPropria(tipoManifestacaoProcessual, classeJudicial);
		validarClasseExigeDocIdentificacao(tipoManifestacaoProcessual, classeJudicial);
		
		if (log.isDebugEnabled()) {
			log.debug("Carregando assuntos...");
		}
		// Carrega os assuntos do processo
		List<AssuntoTrf> assuntos = carregarAssuntosJudiciais(jurisdicao.getIdJurisdicao(), classeJudicial.getIdClasseJudicial(), tipoManifestacaoProcessual.getDadosBasicos().getAssunto());

		// Cria o processo bpm
		if (log.isDebugEnabled()) {
			log.debug("Criando Processo...");
		}

		Processo processo = criarProcesso(tipoManifestacaoProcessual, classeJudicial.getExigeNumeracaoPropria());

		if(identificadorValidado) {
			salvarIdentificadorExterno(tipoManifestacaoProcessual, processo);
		}
		
		// Cria o processo judicial
		ProcessoTrf processoJudicial = criarProcessoJudicial(tipoManifestacaoProcessual, jurisdicao, classeJudicial, assuntos, processo, classeJudicial.getExigeNumeracaoPropria());
		atualizarProcessoDcp(processoJudicial, tipoManifestacaoProcessual);
		processoTrfHome.setAtendimentoEmPlantao(isAtendimentoPlantao);
		
		/*
         * Inicio - PJEII-6019 - Tratar os dados eleitorais na
         * remessa/recebimento
         */
        if (ParametroJtUtil.instance().justicaEleitoral()) {
                ComplementoProcessoJE complementoJE = new ComplementoProcessoJE();
                Estado estado;
                Municipio municipio;
                String anoEleicao=null;
                String codigoTipoEleicao=null;
                
                for (Parametro param : tipoManifestacaoProcessual
                                .getParametros()) {
                        if (param.getNome().equals(MNIParametro.PARAM_JE_ESTADO)) {                        	
                                estado = ComponentUtil.getComponent(EstadoManager.class).findBySigla(param.getValor());
                                complementoJE.setEstadoEleicao(estado);
                        }
                        if (param.getNome().equals(MNIParametro.PARAM_JE_MUNICIPIO)) {
                                String codigoIBGE = param.getValor();
                                municipio = ComponentUtil.getComponent(MunicipioManager.class).getMunicipioByCodigoIBGE(codigoIBGE);
                                complementoJE.setMunicipioEleicao(municipio);
                        }
                        if (param.getNome().equals(MNIParametro.PARAM_JE_ANO_ELEICAO)) {
                                anoEleicao = param.getValor();
                        }

                        if (param.getNome().equals(MNIParametro.PARAM_JE_TIPO_ELEICAO)) {
                                codigoTipoEleicao = param.getValor();
                        }

                }

                if (anoEleicao != null && codigoTipoEleicao != null) {
                        Eleicao eleicao = ComponentUtil.getComponent(EleicaoManager.class).obtemOuCriaEleicao(
                                        new Integer(anoEleicao), new Integer(codigoTipoEleicao));
                        complementoJE.setEleicao(eleicao);
                }

                complementoJE.setProcessoTrf(processoJudicial);
                processoJudicial.setComplementoJE(complementoJE);
                ComponentUtil.getComponent(ProcessoJudicialManager.class).merge(processoJudicial);
        }
        /*
         * Fim - PJEII-6019
         */

		ProcessoJTHome processoJTHome = ComponentUtil.getComponent(ProcessoJTHome.class);
		if (new ParametroJtUtil().justicaTrabalho() && processoJTHome != null) {
			AtividadeEconomica atividade = null;
			MunicipioIBGE municipio = null;

			for (Parametro param : tipoManifestacaoProcessual.getParametros()) {
				if (param.getNome().equals(MNIParametro.PARAM_ATIVIDADE_ECONOMICA)) {
					atividade = EntityUtil.getEntityManager().getReference(AtividadeEconomica.class, Integer.parseInt(param.getValor()));
					processoJTHome.getInstance().setAtividadeEconomica(atividade);
				}

				if (param.getNome().equals(MNIParametro.PARAM_MUNICIPIO_IBGE)) {
					municipio = EntityUtil.getEntityManager().getReference(MunicipioIBGE.class, Integer.parseInt(param.getValor()));
					processoJTHome.getInstance().setMunicipioIBGE(municipio);
				}
				
				/*
				 * Início - PJEII-8136 - Registro de prioridade processual é
				 * perdido ao ser remetido ou baixado processo. [CSJT] -
				 * Thiago Oliveira - 04/06/2013
				*/
				if (param.getNome().equals(MNIParametro.PARAM_PRIORIDADE_PROCESSUAL)) {
					this.deletaEAtualizaProcessoPrioridadeProcesso(processoJudicial, param);
				}
				/*
				 * Fim - PJEII-8136
				 */
			}

			processoTrfHome.setInstance(processoJudicial);
			if (log.isDebugEnabled()) {
				log.debug("Persist ProcessoTRF...");
			}
			processoJTHome.persist();
		}

		salvarExtensao(processoJudicial, tipoManifestacaoProcessual);
		if (isDistribuir(tipoManifestacaoProcessual)) {
			if (log.isDebugEnabled()) {
				log.debug("Carregando Competências...");
			}

			Integer comp = tipoManifestacaoProcessual.getDadosBasicos().isSetCompetencia() ? tipoManifestacaoProcessual.getDadosBasicos().getCompetencia() : null; 

			Competencia competencia = carregarCompetencia(jurisdicao, classeJudicial, assuntos, comp);

			ProcessoHome processoHome = ProcessoHome.instance();
			processoHome.setInstance(processo);
			processoTrfHome.setInstance(processoJudicial);
			processoTrfHome.setCompetenciaConflito(competencia);
			configurarSigiloProcesso(processoJudicial, tipoManifestacaoProcessual.getDadosBasicos());
			processoTrfHome.setOrgaoJulgador(obterOrgaoJulgador(tipoManifestacaoProcessual));
			processoTrfHome.protocolar();
		} else {
			String numeroProcesso = tipoManifestacaoProcessual.getDadosBasicos().getNumero().getValue();
			Date dataAutuacao = ConversorUtil.converterParaDate(tipoManifestacaoProcessual.getDataEnvio(), true);
			OrgaoJulgador orgaoJulgador = ComponentUtil.getComponent(OrgaoJulgadorManager.class).findById(Integer.parseInt(tipoManifestacaoProcessual.getDadosBasicos().getOrgaoJulgador().getCodigoOrgao()));
			persistSemDistribuir(processoJudicial, numeroProcesso, dataAutuacao, orgaoJulgador, getNoDestino(tipoManifestacaoProcessual));
			prevencaoService.realizaPrevencaoDeProcessoBuscandoOutrosProcessosQueFazemReferenciaAhEle(processoJudicial);
		}
		salvarManifestacao(tipoManifestacaoProcessual, processoTrfHome.getInstance());
		
		if (!log.isDebugEnabled()) {
			log.debug("Processos Vinculados");
		}
		atualizarProcessoDcp(processoJudicial, tipoManifestacaoProcessual);

		Events.instance().raiseEvent(AFTER_EVENT_NAME, tipoManifestacaoProcessual, processoJudicial, null);
		Events.instance().raiseEvent(Eventos.EVENTO_ATUALIZAR_CAIXAS_PROCURADORES, processoTrfHome.getInstance());
		String numeroProcesso = (processoTrfHome.getInstance().getNumeroProcesso() != null ? processoTrfHome.getInstance().getNumeroProcesso() : processoJudicial.getNumeroProcesso());
		return NumeroProcessoUtil.retiraMascaraNumeroProcesso(numeroProcesso);
	}
	
	/**
	 * Verifica se a classe judicial do processo exige documento de identificacao para polo ativo, passivo ou terceiros via MNI
	 * @param manifestacaoProcessual
	 * @param classeJudicial
	 */
	private void validarClasseExigeDocIdentificacao(ManifestacaoProcessual manifestacaoProcessual,
			ClasseJudicial classeJudicial) {
		if(classeJudicial.getExigeDocumentoIdentificacaoMNI() && !Authenticator.instance().isPermiteCadastrarParteSemDocumento()) {
			CabecalhoProcessual cabecalhoProcessual = manifestacaoProcessual.getDadosBasicos();
	 		List<PoloProcessual> polos = cabecalhoProcessual.getPolo();
	 		Predicate filtroDocumentoIdentificacaoPreenchido = novoFiltroDocumentoIdentificacaoPreenchido();
	 		if (Authenticator.isUsuarioExterno()){
	 	 		for (PoloProcessual polo : polos) {
	 	 			boolean isPoloAtivoOrPassivoOrTerceiro = (polo.getPolo() == ModalidadePoloProcessual.AT || 
	 	 					polo.getPolo() == ModalidadePoloProcessual.PA || polo.getPolo() == ModalidadePoloProcessual.TC);
	 	 			if (isPoloAtivoOrPassivoOrTerceiro){
	 	 	 			for (Parte parte : polo.getParte()) {
	 	 	 				br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoa = parte.getPessoa(); 	 	 				
	 	 	 				boolean existeDocumentoIdentificacaoPreenchido = CollectionUtils.exists(pessoa.getDocumento(), filtroDocumentoIdentificacaoPreenchido);
	 	 	 				if (isNulo(pessoa) || !existeDocumentoIdentificacaoPreenchido) {
	 	 	 	 				if(pessoa.getTipoPessoa() != TipoQualificacaoPessoa.AUTORIDADE){
	 	 	 	 					String mensagem = "Documento de identificação não informado para parte '%s.'";
	 	 	 	 					throw new NegocioException(String.format(mensagem, pessoa.getNome())); 	 	 	 					
	 	 	 	 				}
	 	 	 				}
	 	 	 			} 	 				
	 	 			}
	 	 		}
	 			
	 		}
		}
	}
	
	/**
	 * Atualiza o nome dos criadores dos arquivos vindo de uma instancia inferior ou superior
	 * Só é usado quando a requisição é feita inter-PJes
	 * @param isRequisicaoDePJE
	 * @param processoDocumento
	 * @param documentoProcessual
	 * @throws PJeBusinessException 
	 */
	private void atualizaCriadorArquivoRemessaRetorno(boolean isRequisicaoDePJE, ProcessoDocumento processoDocumento, DocumentoProcessual documentoProcessual) throws PJeBusinessException{
		if (isRequisicaoDePJE) {
			String criadorArquivo = MNIParametroUtil.obterValor(documentoProcessual, MNIParametro.PARAM_CRIADOR_ARQUIVO);
	  		if(criadorArquivo != null){
	  			processoDocumento.setNomeUsuarioInclusao(criadorArquivo);  
	  			processoDocumento.setNomeUsuarioAlteracao(criadorArquivo);
			  	tratarPessoa(processoDocumento, documentoProcessual);  
	  		}else {
	  			log.error("O documento de origem (PD) {0} não teve o criador cadastrado. Criador estava nulo", documentoProcessual.getIdDocumento());
	  		}
		}
	}

	/**
	 * Valida se o número do processo de referência foi passado na entrega de manifestação caso
	 * a classe o exija.
	 * 
	 * @param manifestacao ManifestacaoProcessual
	 * @param classeJudicial ClasseJudicial
	 */
	private void validarVinculacaoReferencia(ManifestacaoProcessual manifestacao, ClasseJudicial classeJudicial) {
		
		if(classeJudicial.getProcessoReferencia() == ProcessoReferenciaEnum.OB){
			String numeroOriginario = obterNumeroOriginario(manifestacao);
			
			if(StringUtils.isBlank(numeroOriginario)){
				throw new NegocioException("A classe judicial escolhida exige número do processo referência/originário");
			}
		}
	}

	private String getNoDestino(ManifestacaoProcessual manifestacaoProcessual) {
		for (Parametro parametro : manifestacaoProcessual.getParametros()) {
			if (parametro.getNome().equals(NOME_PARAMETRO_MNI_NO_DESTINO)) {
				return parametro.getValor();
			}
		}
		return null;

	}

	private void persistSemDistribuir(ProcessoTrf processoJudicial, String numeroProcesso, Date dataEnvio, OrgaoJulgador orgaoJulgador, String noDestino) throws Exception {
		// setar o numero do processo (NNNNNNN-DD.AAAA.J.TR.OOOO)

		if (log.isDebugEnabled()) {
			log.debug("Persist sem Distribuir...");
		}
		String numeroFormatado = numeroProcesso;
		processoJudicial.getProcesso().setNumeroProcesso(numeroFormatado);
		processoJudicial.setNumeroSequencia(Integer.parseInt(numeroFormatado.substring(0, 7)));
		processoJudicial.setNumeroDigitoVerificador(Integer.parseInt(numeroFormatado.substring(8, 10)));
		processoJudicial.setAno(Integer.parseInt(numeroFormatado.substring(11, 15)));
		processoJudicial.setNumeroOrgaoJustica(Integer.parseInt(numeroFormatado.substring(16, 20).replace(".", "")));
		processoJudicial.setNumeroOrigem(Integer.parseInt(numeroFormatado.substring(21)));

		// processoStatus
		processoJudicial.setProcessoStatus(ProcessoStatusEnum.D);

		// data autuacao
		processoJudicial.setDataAutuacao(dataEnvio);

		// orgao julgador
		processoJudicial.setOrgaoJulgador(orgaoJulgador);

		// apreciadoEmSegredo
		processoJudicial.setApreciadoSegredo(processoJudicial.getSegredoJustica() ? ProcessoTrfApreciadoEnum.S : ProcessoTrfApreciadoEnum.N);

		// dataDistribuicao
		// orientar os tribunais que estajam fazendo migração a informar data de
		// distribuicao no campo dataEnvio
		processoJudicial.setDataDistribuicao(dataEnvio);

		processoJudicial.setApreciadoSigilo(processoJudicial.getSegredoJustica() ? ProcessoTrfApreciadoEnum.S : ProcessoTrfApreciadoEnum.N);

		processoJudicial.setSelecionadoPauta(false);

		processoJudicial.setRevisado(false);

		processoJudicial.setCargo(null);

		if (orgaoJulgador.getOrgaoJulgadorCargoList().size() == 0) {
			throw new NegocioException(String.format("Não há cargos configurados para o órgão julgador %s.", orgaoJulgador.getOrgaoJulgador()));
		}

		processoJudicial.setOrgaoJulgadorCargo(orgaoJulgador.getOrgaoJulgadorCargoList().get(0));

		processoJudicial.setInOutraInstancia(false);

		processoJudicial.setInBloqueiaPeticao(false);

		processoJudicial.setSelecionadoJulgamento(false);

		processoJudicial.setInstancia(null);

		processoJudicial.setApreciadoJusticaGratuita(processoJudicial.getJusticaGratuita());

		Double pesoProcessual = processoJudicial.calcularPesoProcessual(ComponentUtil.getComponent(ProcessoPesoParteManager.class).buscarPesosPartes());
		processoJudicial.setValorPesoProcessual(pesoProcessual);

		// Por orientação do Dr. Paulo Cristóvão, levando-se em consideração que
		// o sistema cliente é incapaz de fornecer o valor do peso da
		// distribuição,
		// este atributo deve ser igualado ao peso do processo
		processoJudicial.setValorPesoDistribuicao(pesoProcessual);

		EntityUtil.getEntityManager().persist(processoJudicial);

		Boolean instalacaoUtilizaNoDesvio = instalacaoUtilizaNoDesvio();

		Fluxo fluxo = processoJudicial.getClasseJudicial().getFluxo();
		
		if(fluxo != null){
			ProcessoTrfHome.instance().inserirProcessoNoFluxo(processoJudicial.getIdProcessoTrf(), fluxo);
			ProcessDefinition processDefinition = new ProcessDefinition(fluxo.getFluxo());

			if(BusinessProcess.instance().hasCurrentTask()){
				processDefinition = TaskInstance.instance().getProcessInstance().getProcessDefinition();
			}
		
		if (instalacaoUtilizaNoDesvio) {
				transitar(processoJudicial.getProcesso(), NoDeDesvioService.getNomeNoDesvio(processDefinition));
		}

		if (noDestino != null && !noDestino.isEmpty()) {
				String[] nosDestino = noDestino.split("/");
				try{
					transitar(processoJudicial.getProcesso(), nosDestino);
				} catch (Exception e) {
					throw new IntercomunicacaoException("Erro ao transitar (fluxo principal: " + fluxo.getFluxo() + ", nós: " + noDestino + ") verifique a configuração do Fluxo", e);
				}
			}
		} else {
			throw new NegocioException("Classe judicial sem Fluxo associado. Verifique o cadastro da Classe");
			}
		}

	private Boolean instalacaoUtilizaNoDesvio() {
		String valorParametro = ParametroUtil.getParametro(NOME_PARAMETRO_PJE_UTILIZACAO_NO_DESVIO);
		Boolean instalacaoUtilizaNoDesvio = new Boolean(valorParametro != null ? valorParametro : "false");
		return instalacaoUtilizaNoDesvio;
	}

	private void transitar(Processo processo, String... nos) throws IntercomunicacaoException{
		if(nos != null){
			for(String no : nos){

				JbpmEventsHandler.instance().iniciarTask(processo);
				BusinessProcess businessProcess = BusinessProcess.instance();
				ProcessDefinition processDefinition = TaskInstance.instance().getProcessInstance().getProcessDefinition();
				Node node = processDefinition.getNode(no);
				if(node == null){
					throw new NegocioException("Nó " + no + " não está presente no Fluxo " + processDefinition.getName());
				}
				businessProcess.endTask(no);
				businessProcess.clearDirty();
				businessProcess.setProcessId(null);
				// ProcessState indica transição para subfluxo, tentar então
				// transitar para o nó de desvio do subfluxo
				if(node instanceof ProcessState && instalacaoUtilizaNoDesvio()){
					JbpmEventsHandler.instance().iniciarTask(processo);
					processDefinition = TaskInstance.instance().getProcessInstance().getProcessDefinition();
					transitar(processo, NoDeDesvioService.getNomeNoDesvio(processDefinition));
				}

			}
		}
	}

	private boolean isDistribuir(ManifestacaoProcessual manifestacaoProcessual) {
		for (Parametro parametro : manifestacaoProcessual.getParametros()) {
			if (parametro != null && parametro.getNome() != null) {
				if (parametro.getNome().equalsIgnoreCase(NOME_PARAMETRO_MNI_DISTRIBUICAO)) {
					if (parametro.getValor().equals("0")) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Registra ciência e responde um expediente.
	 * 
	 * @param parametro Manifestação com o documento, o número do processo e a lista de expedientes 
	 * que serão respondidos.
	 * 
	 * @throws Exception
	 */
	private void responderExpediente(ManifestacaoProcessual parametro) throws Exception {
		String numeroProcesso1G = getNumeroProcesso1Grau(parametro);
		List<Parametro> parametros = parametro.getParametros();
		Parametro idsProcessoParteExpediente = MNIParametroUtil.obter(parametros, MNIParametro.getIdsProcessoParteExpediente());
		
		ProcessoParteExpediente[] expedientes = obterArrayProcessoParteExpediente(idsProcessoParteExpediente);
		
		if (ArrayUtils.isNotEmpty(expedientes)) {
			
			ProcessoTrf processoTrf = obterProcessoTrf(parametro.getNumeroProcesso(), false);
			validarProcessoRemetido(processoTrf);
			validarProcessoMigracaoBloqueada(processoTrf);
			validarProcessoPeticaoBloqueada(processoTrf);
			
			DocumentoProcessual resposta = parametro.getDocumento().get(0);
			
			if(parametro.getDocumento().size() > 1){
				resposta.getDocumentoVinculado().addAll(parametro.getDocumento().subList(1, parametro.getDocumento().size()));
			}
			
			ProcessoDocumento processoDocumento = carregarDocumentoProcessual(resposta, 
					processoTrf.getProcesso(), null, numeroProcesso1G);
			processoDocumento.setProcessoTrf(processoTrf);
			
			RespostaExpediente resp = new RespostaExpediente();
			resp.setProcessoDocumento(processoDocumento);
			
			ComponentUtil.getComponent(AtoComunicacaoService.class).registraResposta(resp, true, expedientes);
			
        	ProcessoEvento movimentoProcesso = ProtocolarDocumentoBean.lancarMovimentacaoProcessual(processoDocumento, Boolean.FALSE);
        	
        	if (movimentoProcesso == null){
        		throw new NegocioException("Não foi possível lançar o movimento de resposta do expediente");
        	}
			
        	List<ProcessoDocumento> documentosRequisicao = new ArrayList<ProcessoDocumento>();
        	documentosRequisicao.add(processoDocumento);
        	documentosRequisicao.addAll(processoDocumento.getDocumentosVinculados());
        	setDocumentosRequisicao(documentosRequisicao);
        	
			Events.instance().raiseEvent(Eventos.INICIAR_FLUXO_PETICAO_INCIDENTAL, resp.getProcessoDocumento().getIdProcessoDocumento());
		}
	}

	/**
	 * Retorna true se a manifestação processual for do tipo de resposta de expediente.
	 * 
	 * @param parametro Manifestação com parâmetro 'mni:idsProcessoParteExpediente'.
	 * @return booleano
	 */
	private boolean isRespostaDeExpediente(ManifestacaoProcessual parametro) {
		
		List<Parametro> parametros = parametro.getParametros();
		return MNIParametroUtil.hasParametro(parametros, MNIParametro.getIdsProcessoParteExpediente());
	}
	
	public void setIsRequisicaoDePJE(ManifestacaoProcessual parametro) {	
		String isPJE = MNIParametroUtil.obterValor(parametro, MNIParametro.isPJE());
		boolean resultado = (StringUtils.isNotBlank(isPJE) ? Boolean.parseBoolean(isPJE) : false);
	
		this.isRequisicaoDePJE = resultado;
	}

	/**
	 * Retorna true se a manifestao processual requisitada a partir de um PJE.
	 * 
	 * @param parametro Manifestao com parmetro 'mni:isPJE'.
	 * @return booleano
	 */
	public boolean getIsRequisicaoDePJE() {
		
		 return this.isRequisicaoDePJE;
	}		

	/**
	 * Retorna o número do processo no 1 grau. Usado na remessa.
	 * 
	 * @param parametro
	 * @return String
	 */
	public String getNumeroProcesso1Grau(ManifestacaoProcessual parametro) {
		return MNIParametroUtil.obterValor(parametro, MNIParametro.PARAM_NUM_PROC_1_GRAU);
	}
	
	/**
	 * Retorna true se a pessoa é parte do processo
	 * 
	 * @param pessoa Pessoa
	 * @param processo ProcessoTrf
	 * @return booleano
	 */
	private boolean isParteAssociada(Pessoa pessoa, ProcessoTrf processo) {
		ProcessoParte processoParte = processoParteManager.findProcessoParte(processo, null, pessoa);
		
		return processoParte != null;
	}

	/**
	 * Valida se os atributos obrigatórios da manifestação processual foram
	 * atribuídos.
	 * 
	 * @param manifestacaoProcessual
	 * @throws Exception
	 */
	public void validarManifestacaoProcessual(ManifestacaoProcessual manifestacaoProcessual) throws Exception {
		if (manifestacaoProcessual == null) {
			throw new NegocioException("Manifestação processual não informada");
		}

		if (manifestacaoProcessual.getIdManifestante() == null || manifestacaoProcessual.getIdManifestante().trim().length() == 0) {
			throw new NegocioException("Identificador do manifestante não informado.");
		}

		if (manifestacaoProcessual.getDadosBasicos() == null) {
			throw new NegocioException("Dados básicos ou número do processo para peticionamento intermediário não informados.");
		}

		if (manifestacaoProcessual.getDadosBasicos().getClasseProcessual() == 0) {
			throw new NegocioException("Classe processual não informada.");
		}

		if (manifestacaoProcessual.getDadosBasicos().getAssunto() == null || manifestacaoProcessual.getDadosBasicos().getAssunto().size() == 0) {
			throw new NegocioException("Assuntos processuais não informados.");
		}

		if (manifestacaoProcessual.getDadosBasicos().getPolo() == null || manifestacaoProcessual.getDadosBasicos().getPolo().size() == 0) {
			throw new NegocioException("Polos processuais não informados.");
		}

		if (manifestacaoProcessual.getDocumento().size() == 0) {
			throw new NegocioException("Documentação não informada.");
		}
	}

	/**
	 * Valida se os atributos obrigatórios do documento foram preenchidos.
	 * 
	 * @param documento DocumentoProcessual
	 * @param conteudo Array de bytes
	 * @param isRequisicaoDePJE True se a requisição foi feita a partir de um PJE. (caso de remessa)
	 * @throws Exception
	 */
	private void validarDocumentoProcessual(DocumentoProcessual documento, byte[] conteudo) throws Exception {
		if (documento == null) {
			throw new NegocioException("Documento processual não informado.");
		}
		
		if (StringUtils.isBlank(documento.getTipoDocumento())) {
			throw new NegocioException("Tipo do documento não informado.");
		}
		
		if (isRequisicaoDePJE == false && conteudo == null) {
			throw new NegocioException("O documento não foi anexado.");
		}
	}

	private Jurisdicao carregarJurisdicao(String numeroOrigemJurisdicao) throws Exception {
		Jurisdicao jurisdicao = jurisdicaoManager.obterPorNumeroOrigem(numeroOrigemJurisdicao);
		
		if (jurisdicao == null) {
			throw new NegocioException("Jurisdição não encontrada");
		} 
		
		if (jurisdicao.getAtivo() == false){
			throw new NegocioException(String.format("Jurisdição \"%s\" inativa", jurisdicao.getJurisdicao()));
		}
		
		if (Authenticator.isUsuarioExterno() && !jurisdicao.getIsJuridicaoExterna() ) {
			throw new NegocioException("Jurisdição não permitida para usuários externos.");
		}
		
	
		return jurisdicao;
	}

	private List<AssuntoTrf> carregarAssuntosJudiciais(Integer idJurisdicao, Integer idClasseJudicial, List<AssuntoProcessual> assuntos) throws Exception {
		List<String> codigoAssuntoList = new ArrayList<String>();
		for (AssuntoProcessual assuntoProcessual : assuntos) {
			AssuntoLocal assuntoLocal = assuntoProcessual.getAssuntoLocal();
			Integer codigoNacional = assuntoProcessual.getCodigoNacional();
			Integer codigoLocal = (assuntoLocal != null ? assuntoLocal.getCodigoAssunto() : null);
			Integer codigo = (codigoNacional != null ? codigoNacional : codigoLocal);
			
			codigoAssuntoList.add(String.valueOf(codigo));
		}
		List<AssuntoTrf> assuntoTrfList = ComponentUtil.getComponent(AssuntoTrfManager.class).carregarAssuntosJudiciais(idJurisdicao, idClasseJudicial, codigoAssuntoList);

		// Verifica se todos os códigos de assuntos informados existem na base
		for (String codigoAssunto : codigoAssuntoList) {
			boolean encontrado = false;
			for (AssuntoTrf assuntoTrf : assuntoTrfList) {
				if (assuntoTrf.getCodAssuntoTrf().equals(String.valueOf(codigoAssunto))) {
					encontrado = true;
					break;
				}
			}
			if (!encontrado) {
				throw new NegocioException(String.format("Assunto de código %d inválido", codigoAssunto));
			}
		}

		return assuntoTrfList;
	}

	private List<ProcessoAssunto> carregarAssuntosProcessuais(List<AssuntoTrf> assuntos, Integer codigoAssuntoPrincipal, ProcessoTrf processoJudicial) throws Exception {
		List<ProcessoAssunto> processoAssuntoList = new ArrayList<ProcessoAssunto>();
		ProcessoAssuntoManager processoAssuntoManager = ComponentUtil.getComponent(ProcessoAssuntoManager.class);
		for (AssuntoTrf assunto : assuntos) {
			ProcessoAssunto processoAssunto = new ProcessoAssunto();
			processoAssunto.setProcessoTrf(processoJudicial);
			processoAssunto.setAssuntoTrf(assunto);
			processoAssunto.setAssuntoPrincipal(assunto.getCodAssuntoTrf().equals(String.valueOf(codigoAssuntoPrincipal)));
			processoAssuntoManager.persist(processoAssunto);
			processoAssuntoList.add(processoAssunto);
		}
		return processoAssuntoList;
	}

	/**
	 * Verifica se a competência solicitada é permitida e para a jurisdição, classe e assuntos passados por parâmetro.
	 * 
	 * @param jurisdicao
	 * @param classeJudicial
	 * @param assuntos
	 * @param idCompetencia
	 * @return Competencia
	 */
    private Competencia carregarCompetencia(Jurisdicao jurisdicao, ClasseJudicial classeJudicial, List<AssuntoTrf> assuntos, Integer idCompetencia) throws Exception {
        Competencia resultado = null;
        
        ProcessoTrf processo = new ProcessoTrf();
        processo.setJurisdicao(jurisdicao);
        processo.setClasseJudicial(classeJudicial);
        processo.setAssuntoTrfList(assuntos);
        
        List<Competencia> competencias = AutuacaoService.instance().recuperaCompetenciasPossiveis(processo);
        if (CollectionUtilsPje.isNotEmpty(competencias)) {
            if(idCompetencia == null) {
                if(competencias.size() == 1) {
                    resultado = competencias.get(0);
                }
            }else {
                for (Competencia competencia : competencias) {
                    if (competencia.getIdCompetencia() == idCompetencia.intValue()) {
                        resultado = competencia;
                    }
                }
            }
        }
        
        if (resultado == null) {
            String mensagem = "A Competência '%s' não é permitida.";
            throw new PJeBusinessException(String.format(mensagem, idCompetencia));
        }
        return resultado;
    }
    
	private Processo criarProcesso(ManifestacaoProcessual manifestacaoProcessual, boolean numeracaoPropria) throws Exception {
		String numeroProcesso1G = getNumeroProcesso1Grau(manifestacaoProcessual);
		Processo processo = new Processo();
		processo.setUsuarioCadastroProcesso(obterUsuarioLogin());
		processo.setDataInicio(new Date());

		ComponentUtil.getComponent(ProcessoManager.class).persist(processo);

		processo.setProcessoDocumentoList(carregarColecaoDocumentoProcessual(
				manifestacaoProcessual.getDocumento(), processo, null, numeroProcesso1G));
		if (log.isDebugEnabled()) {
			log.debug("Flush Documentos...");
		}
		EntityUtil.flush();
		return processo;
	}
	
	/**
	 * Cria um usuário no sistema caso os dados estejam preenchidos
	 * Caso não consiga seta o usuário sistema, mantendo o nome do usuário inclusão enviado na remessa
	 * o erro será logado para posterior análise do documento com problema
	 * @param pd
	 * @param documento
	 * @throws PJeBusinessException
	 */
	private void tratarPessoa(ProcessoDocumento pd, DocumentoProcessual documento) throws PJeBusinessException {
		String nomePessoa = pd.getNomeUsuarioInclusao();
		String tipoDocumentoIdentificacaoCriadorArquivo = MNIParametroUtil.obterValor(documento, MNIParametro.PARAM_TIPO_DOCUMENTO_IDENTIFICACAO_CRIADOR_ARQUIVO);
		String documentoIdentificacaoCriadorArquivo = MNIParametroUtil.obterValor(documento, MNIParametro.PARAM_DOCUMENTO_IDENTIFICACAO_CRIADOR_ARQUIVO);
		
		if (nomePessoa != null && !ASSINATURA_TESTE.equals(nomePessoa)) {
			// PJEII-19607 - Identificar o criador do documento pelo documento de identificação da pessoa
			if (documentoIdentificacaoCriadorArquivo != null && tipoDocumentoIdentificacaoCriadorArquivo != null) {
				List<Pessoa> pessoas = ComponentUtil.getComponent(PessoaService.class).findByNomeAndDocumentoIdentificacao(nomePessoa, tipoDocumentoIdentificacaoCriadorArquivo, documentoIdentificacaoCriadorArquivo);
				
				if (pessoas != null && !pessoas.isEmpty()){
					if (pessoas.size() > 1){
						throw new PJeBusinessException("pje.interComunicacaoService.error.usuarioNaoUnico", null, nomePessoa);
					}
					
					pd.setUsuarioInclusao(pessoas.get(0));
				} else {
					setarUsuarioSistema(pd);
				}				
			}
			else {
				List<Usuario> usuarios = ComponentUtil.getComponent(UsuarioManager.class).findByNome(nomePessoa);
				
				if(usuarios != null && !usuarios.isEmpty()){
					if(usuarios.size() > 1){
						throw new PJeBusinessException("pje.interComunicacaoService.error.usuarioNaoUnico", null, nomePessoa);
					}
					
					pd.setUsuarioInclusao(usuarios.get(0));
				}else{
					setarUsuarioSistema(pd);
				}
			}
		} else {
			setarUsuarioSistema(pd); 
		}		
	}
	
	/**
	 * Seta o usuário sistema no processoDocumento mantendo o nome do usuário que fez a inclusão quando não houver
	 * documento para identificar o usuário e fazer a criação na instância de destino
	 * @param pd
	 */
	private void setarUsuarioSistema(ProcessoDocumento pd) {
		Usuario usuario = obterUsuarioLogin();
		pd.setUsuarioInclusao(usuario);  
		pd.setNomeUsuarioInclusao(usuario.getNome());  
	}  
	
	private void inserirDocumentoDownloadPosterior(final br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual tipoDocumento, final Integer idArquivoBin, String numeroProcesso1G) {

		if(numeroProcesso1G != null){
			DownloadBinario db = null;
			EntityManager entityManager = EntityUtil.getEntityManager();
			Query query = entityManager.createQuery("from DownloadBinario where numeroProcesso = :np");
			query.setParameter("np", numeroProcesso1G);
	
			try {
				db = (DownloadBinario) query.getSingleResult();
			} catch (Exception e) {
				db = null;
			}
	
			if (db == null) {
				db = new DownloadBinario();
				db.setDataInsercao(new Date());
				db.setNumeroProcesso(numeroProcesso1G);
				entityManager.persist(db);
				entityManager.flush();
			}
	
			if (tipoDocumento != null && tipoDocumento.getOutroParametro() != null && tipoDocumento.getOutroParametro().size() > 0) {
				
				String idArquivoOrigem = MNIParametroUtil.obterValor(tipoDocumento, MNIParametro.PARAM_ID_ARQUIVO_ORIGEM);
				
				if (idArquivoOrigem != null) {
			
					DownloadBinarioArquivo dba = downloadBinarioArquivoManager.recuperaPorIdentificadorOriginario(idArquivoOrigem);
					if (dba == null){
						dba = new DownloadBinarioArquivo();
					dba.setDownloadBinario(db);
					dba.setIdArquivoOrigem(idArquivoOrigem);
					dba.setIdProcessoDocumentoBin(idArquivoBin);
			
					entityManager.persist(dba);
					
				}
				
			}
		}

		}
	}

	private ProcessoTrf criarProcessoJudicial(ManifestacaoProcessual manifestacaoProcessual, Jurisdicao jurisdicao, 
			ClasseJudicial classeJudicial, List<AssuntoTrf> assuntos, Processo processo, boolean exigeNumeracaoPropria) throws Exception {
		
		ProcessoTrf processoTrf = new ProcessoTrf();
		if (log.isDebugEnabled()) {
			log.debug("Criando Processo TRF...");
		}
		
		String numeroOriginario = obterNumeroOriginario(manifestacaoProcessual);
		setDesProcessoTrfReferencia(classeJudicial, processoTrf, numeroOriginario);

		Date dataAtual = new Date();
		CabecalhoProcessual cabecalhoProcessual = manifestacaoProcessual.getDadosBasicos();
		
		processoTrf.setIdProcessoTrf(processo.getIdProcesso());
		processoTrf.setProcessoStatus(ProcessoStatusEnum.V);
		processoTrf.setJurisdicao(jurisdicao);
		processoTrf.setInstancia(ComponentUtil.getParametroService().valueOf("aplicacaoSistema").charAt(0));
		processoTrf.setClasseJudicial(classeJudicial);
		processoTrf.setProcesso(processo);
		processoTrf.setValorCausa(manifestacaoProcessual.getDadosBasicos().getValorCausa());
		
		Boolean isIncidental = Boolean.parseBoolean(MNIParametroUtil.obterValor(manifestacaoProcessual, MNIParametro.isProcessoIncidental()));
		if (isIncidental) {
			configurarProcessoIncidental(processoTrf, manifestacaoProcessual);
		}
		
		/**
		 * um parâmetro foi adicionado para receber os processos do PJe.
		 */
		Boolean isTutelaLiminar = Boolean.parseBoolean(MNIParametroUtil.obterValor(manifestacaoProcessual, MNIParametro.PARAM_LIMINAR_ANTECIPA_TUTELA));
		processoTrf.setTutelaLiminar(isTutelaLiminar);
		processoTrf.setJusticaGratuita(isJusticaGratuita(manifestacaoProcessual.getDadosBasicos().getPolo()));
		processoTrf.setInicial(obterClasseJudicialInicial());
		processoTrf.setDataAutuacao(dataAtual);
		processoTrf.setDataDistribuicao(dataAtual);
		
		NumeroUnico numeroUnico = cabecalhoProcessual.getNumero();
		String numeroUnicoStr = isNulo(numeroUnico) == false ? StringUtil.removeNaoNumericos(numeroUnico.getValue()) : null;
		String numeroProcessoOrigemFormatado = NumeroProcessoUtil.mascaraNumeroProcesso(numeroUnicoStr);
		
		String numeroProcesso = numeroProcessoOrigemFormatado;
		if (StringUtils.isNotBlank(numeroProcessoOrigemFormatado)) {
			if (verificarExigenciaNovaNumeracao(exigeNumeracaoPropria, numeroProcessoOrigemFormatado)) {
				numeroProcesso = obterNumeroProcesso(numeroProcessoOrigemFormatado, manifestacaoProcessual, jurisdicao, classeJudicial.getIncidental()).toString();
			}
			
			if(NumeroProcessoUtil.numeroProcessoValido(numeroProcesso)){
				processoTrf.setNumeroSequencia(Integer.parseInt(numeroProcesso.substring(0, 7)));
				processoTrf.setNumeroDigitoVerificador(Integer.parseInt(numeroProcesso.substring(8, 10)));
				processoTrf.setAno(Integer.parseInt(numeroProcesso.substring(11, 15)));
				processoTrf.setNumeroOrgaoJustica(Integer.parseInt(numeroProcesso.substring(16, 20).replace(".", "")));
				processoTrf.setNumeroOrigem(Integer.parseInt(numeroProcesso.substring(21)));
				
				processoTrf.getProcesso().setNumeroProcesso(numeroProcesso);				
			}else{
				throw new IntercomunicacaoException("Número de processo inválido: " + numeroProcesso+".");
			}
		
		} // Caso a variável 'numeroProcessoOrigemFormatado' esteja vazia, o número do processo será gerado no momento do protocolo.

		ComponentUtil.getComponent(ProcessoJudicialManager.class).persist(processoTrf);

		List<AssuntoProcessual> colecaoAssuntoProcessual = manifestacaoProcessual.getDadosBasicos().getAssunto();
		Integer codigoAssuntoPrincipal = 0;
		for (AssuntoProcessual assunto : colecaoAssuntoProcessual) {
			if (assunto.isPrincipal()) {
				codigoAssuntoPrincipal = assunto.getCodigoNacional();
				break;
			}
		}
		
		if (codigoAssuntoPrincipal == 0 && colecaoAssuntoProcessual.isEmpty() == false) {
			codigoAssuntoPrincipal = colecaoAssuntoProcessual.get(0).getCodigoNacional();
		}
		
		String cpfCnpjUsuario = MNIParametroUtil.obterValor(manifestacaoProcessual, MNIParametro.PARAM_CPF_CNPJ_USUARIO);
		if(cpfCnpjUsuario == null || cpfCnpjUsuario.trim().isEmpty()){
			cpfCnpjUsuario = manifestacaoProcessual.getIdManifestante();//TODO pode nao funcionar se usar usuario cujo login nao seja igual ao CPF ou CNPJ
		}
		
		Contexts.getSessionContext().set(MNIParametro.PARAM_CPF_CNPJ_USUARIO, cpfCnpjUsuario);
		processoTrf.setProcessoAssuntoList(carregarAssuntosProcessuais(assuntos, codigoAssuntoPrincipal, processoTrf));
		processoTrf.setProcessoParteList(carregarPartesProcessuais(manifestacaoProcessual, processoTrf));
		processoTrf.setProcessoTrfConexaoList(carregarProcessosVinculados(manifestacaoProcessual.getDadosBasicos().getProcessoVinculado(), processoTrf));
		processoTrf.setComplementoClasseProcessoTrfList(carregarComplementoClasseProcessoTrf(manifestacaoProcessual, processoTrf, classeJudicial));
		processoTrf.setIdAreaDireito(ComponentUtil.getComponent(AssuntoTrfManager.class).obtemAreaDireito(processoTrf.getProcessoAssuntoPrincipal().getAssuntoTrf()));
		salvarPrioridadeProcesso(manifestacaoProcessual.getDadosBasicos(), processoTrf);
		return processoTrf;
	}

	private void setDesProcessoTrfReferencia(ClasseJudicial classeJudicial, ProcessoTrf processoTrf,
			String numeroOriginario) {
		if (!StringUtils.isBlank(numeroOriginario)) {
			processoTrf.setDesProcReferencia(tratarMascaraNumeroProcessoReferencia(numeroOriginario, classeJudicial));
		}
	}
	
	/**
	 * Método responsável por verificar a exigência de uma nova numeração para o processo. 
	 * 
	 * @param exigeNumeracaoPropria Indica se o processo exige (ou não) numeração própria.
	 * @param numeroProcessoOrigem Número do processo da instância de origem.
	 * @return 
	 * <ul>
	 *    <li>
	 *       Verdadeiro se:
	 *       <ul>
	 *          <li>A indicação de numeração própria é verdadeira</li>
	 *          <li>A indicação de numeração própria é falsa  e o número do órgão de justiça da instância 
	 *          	de destino é diferente do número do órgão de justiça que consta no número do processo.</li>
	 *       </ul>
	 *    </li>
	 *    <li>Falso, caso nenhuma das condições acima seja satisfeita.</li>
	 * </ul>
	 */
	private boolean verificarExigenciaNovaNumeracao(boolean exigeNumeracaoPropria, String numeroProcessoOrigem) {
		boolean resultado = true;
		if (!exigeNumeracaoPropria) {
			try {
				int numeroOrgaoJusticaDestino = Integer.parseInt(ComponentUtil.getComponent(ParametroService.class).valueOf("numeroOrgaoJustica").substring(0, 1));
				int numeroOrgaoJusticaOrigem = Integer.parseInt(numeroProcessoOrigem.substring(16, 17));
				
				resultado = numeroOrgaoJusticaDestino != numeroOrgaoJusticaOrigem;
			} catch (NumberFormatException e) {
				throw new IntercomunicacaoException(e.getLocalizedMessage());
			}
		}
		return resultado;
	}
	
	/**
	 * Método responsável por obter o número do processo de acordo com os dados do processo na instância de origem.
	 * 
	 * @param numeroProcessoOrigem Número do processo na instância de origem.
	 * @param manifestacaoProcessual Dados da manifestação processual.
	 * @param jurisdicao Jurisdição de destino.
	 * @return O número do processo de acordo com os dados do processo na instância de origem.
	 */
	private NumeroProcesso obterNumeroProcesso(String numeroProcessoOrigem, ManifestacaoProcessual manifestacaoProcessual, Jurisdicao jurisdicao, boolean isIncidental) {
		int numeroOrgaoJusticaDestino;
		try {
			numeroOrgaoJusticaDestino = Integer.parseInt(ComponentUtil.getComponent(ParametroService.class).valueOf("numeroOrgaoJustica"));
		} catch (NumberFormatException e) {
			throw new IntercomunicacaoException(e.getLocalizedMessage());
		}

		int numeroOrgaoJusticaOrigem = Integer.parseInt(numeroProcessoOrigem.substring(16, 17));
		
		int numeroJurisdicao = NumeroProcessoUtil.obterNumeroJurisdicao(jurisdicao.getNumeroOrigem(), numeroOrgaoJusticaOrigem, 
				MNIParametroUtil.obterValor(manifestacaoProcessual, MNIParametro.PARAM_INSTANCIA_PROCESSO_ORIGEM), isIncidental);
		
		return NumeroProcessoUtil.gerarNumeroProcesso(numeroOrgaoJusticaDestino, numeroJurisdicao);
	}

	private List<ProcessoParte> carregarPartesProcessuais(ManifestacaoProcessual manifestacao, ProcessoTrf processoJudicial) throws Exception {
		List<PoloProcessual> polos = manifestacao.getDadosBasicos().getPolo();
		
		if (polos == null || polos.size() == 0) {
			throw new NegocioException("Processo sem polos definidos");
		}
		
		for (PoloProcessual polo : polos) {
			if (polo.getParte() == null || polo.getParte().size() == 0) {
				throw new NegocioException("Não existem partes definidas para o polo");
			}

			for (br.jus.cnj.intercomunicacao.v222.beans.Parte parte : polo.getParte()) {
				ProcessoParte processoParte;
				ProcessoParteSituacaoEnum parteSituacao = ProcessoParteSituacaoEnum.A;
				boolean sigiloso = false;
				boolean partePrincipal = true;
				
				if (log.isDebugEnabled()) {
					log.debug("Carregando Parte... " + parte.getPessoa().getNome() + " polo " + polo.getPolo());
				}
				
				if (isRequisicaoDePJE){
					parteSituacao = carregaParteSituacao(polo, parte, manifestacao);
					sigiloso = carregaParteSigilo(polo, parte, manifestacao);
				}
				
				processoParte = carregarParteProcessual(polo, parte, processoJudicial, partePrincipal, null, manifestacao, parteSituacao, sigiloso);
				
				processoParteList.add(processoParte);
			}
		}
		return processoParteList;
	}
	
	private ProcessoParteSituacaoEnum carregaParteSituacao(PoloProcessual polo, br.jus.cnj.intercomunicacao.v222.beans.Parte parte, ManifestacaoProcessual manifestacao){
		ProcessoParteSituacaoEnum situacaoAtual = ProcessoParteSituacaoEnum.A;

		String indiceParteSituacao = MNIParametroUtil.obterValor(manifestacao, MNIParametro.getIndiceParteSituacao());
		if (StringUtils.isNotBlank(indiceParteSituacao)) {
			indiceParteSituacao = indiceParteSituacao.replaceAll("[\\s \\[ \\]]", "");
			
			for(String poloParteSituacao : indiceParteSituacao.split(",")){
				if(poloParteSituacao.split(":")[0].equals(polo.getPolo().toString())){
					if(poloParteSituacao.split(":")[1].equals(""+polo.getParte().indexOf(parte))){
						situacaoAtual = ProcessoParteSituacaoEnum.valueOf(poloParteSituacao.split(":")[2]);
						break;
					}
				}
			}	
		}
		
		return situacaoAtual;
	}

	private ProcessoParteSituacaoEnum carregaParteRepresentanteSituacao(PoloProcessual polo, 
																		br.jus.cnj.intercomunicacao.v222.beans.Parte parteRepresentada, 
																		int indexRepresentante,
																		ManifestacaoProcessual manifestacao, 
																		List<br.jus.cnj.intercomunicacao.v222.beans.RepresentanteProcessual> representantesProcessuais){

		ProcessoParteSituacaoEnum situacaoAtual = ProcessoParteSituacaoEnum.A;

		String indiceParteRepresentanteSituacao = MNIParametroUtil.obterValor(manifestacao, MNIParametro.getIndiceParteRepresentanteSituacao());
		if (StringUtils.isNotBlank(indiceParteRepresentanteSituacao)) {
			indiceParteRepresentanteSituacao = indiceParteRepresentanteSituacao.replaceAll("[\\s \\[ \\]]", "");
			
			for(String poloParteRepresentanteSituacao : indiceParteRepresentanteSituacao.split(",")){
				if(poloParteRepresentanteSituacao.split(":")[0].equals(polo.getPolo().toString())){
					if(poloParteRepresentanteSituacao.split(":")[1].equals(""+polo.getParte().indexOf(parteRepresentada))){
						if(poloParteRepresentanteSituacao.split(":")[2].equals(""+indexRepresentante)){
							situacaoAtual = ProcessoParteSituacaoEnum.valueOf(poloParteRepresentanteSituacao.split(":")[3]);
							break;
						}
					}
				}
			}
		}
		return situacaoAtual;
	}
	
	private boolean carregaParteSigilo(PoloProcessual polo, br.jus.cnj.intercomunicacao.v222.beans.Parte parte, ManifestacaoProcessual manifestacao){
		String indiceParteSigilo = MNIParametroUtil.obterValor(manifestacao, MNIParametro.getIndiceParteSigilo());
		Boolean sigiloso = false;
		
		if(StringUtils.isNotBlank(indiceParteSigilo)){
			indiceParteSigilo = indiceParteSigilo.replaceAll("[\\s \\[ \\]]", "");
			for(String poloParteSituacao : indiceParteSigilo.split(",")){
				if(poloParteSituacao.split(":")[0].equals(polo.getPolo().toString())){
					if(poloParteSituacao.split(":")[1].equals(""+polo.getParte().indexOf(parte))){
						sigiloso = Boolean.valueOf(poloParteSituacao.split(":")[2]);
						break;
					}
				}
			}
		}
		return sigiloso;
	}

	private boolean carregaParteRepresentanteSigilo(PoloProcessual polo, 
													br.jus.cnj.intercomunicacao.v222.beans.Parte parteRepresentada, 
													int indexRepresentante,
													ManifestacaoProcessual manifestacao, 
													List<br.jus.cnj.intercomunicacao.v222.beans.RepresentanteProcessual> representantesProcessuais){

		String indiceParteRepresentanteSigilo = MNIParametroUtil.obterValor(manifestacao, MNIParametro.getIndiceParteRepresentanteSigilo());
		boolean sigiloso = false;
		
		if(StringUtils.isNotBlank(indiceParteRepresentanteSigilo)){
			indiceParteRepresentanteSigilo = indiceParteRepresentanteSigilo.replaceAll("[\\s \\[ \\]]", "");
			for(String poloParteRepresentanteSituacao : indiceParteRepresentanteSigilo.split(",")){
				if(poloParteRepresentanteSituacao.split(":")[0].equals(polo.getPolo().toString())){
					if(poloParteRepresentanteSituacao.split(":")[1].equals(""+polo.getParte().indexOf(parteRepresentada))){
						if(poloParteRepresentanteSituacao.split(":")[2].equals(""+indexRepresentante)){
							sigiloso = Boolean.valueOf(poloParteRepresentanteSituacao.split(":")[3]);
							break;
						}
					}
				}
			}
		}
		return sigiloso;
	}

	@SuppressWarnings("unchecked")
	private boolean isJusticaGratuita(List<br.jus.cnj.intercomunicacao.v222.beans.PoloProcessual> polos) {
		Predicate filtro = novoFiltroPoloProcessualPelaModalidade(ModalidadePoloProcessual.AT);
		Collection<br.jus.cnj.intercomunicacao.v222.beans.PoloProcessual> ativos = CollectionUtils.select(polos, filtro);
		
		for (br.jus.cnj.intercomunicacao.v222.beans.PoloProcessual polo : ativos) {
			for (br.jus.cnj.intercomunicacao.v222.beans.Parte parte : polo.getParte()) {
				if (parte.isSetAssistenciaJudiciaria() && parte.isAssistenciaJudiciaria() == true) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Este método verifica se o endereço da parte é desconhecido
	 * @param list
	 * @return {@link Boolean}
	 */
	private Boolean isEnderecoDesconhecido(ModalidadePoloProcessual polo, 
			br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoa){

		boolean enderecoDesconhecido = false;
		
		List<br.jus.cnj.intercomunicacao.v222.beans.Endereco> list = pessoa.getEndereco();
		
		if(list == null || list.size() == 0){
			
			enderecoDesconhecido = true;
		}
		
		/*
		 * A entidade endereco do PJe não aceita CEP nulo
		 * portanto caso o CEP informado não exista o endereço
		 * também será considerado inexistente. Esta abordagem
		 * corrige erro reportado ao entregar manisfestação 
		 * processual contendo CEP que não existe na base do PJe
		 */
		for(br.jus.cnj.intercomunicacao.v222.beans.Endereco endereco : list){
			String cep = endereco.getCep();
			if(cep != null && buscarCep(cep) == null){
				enderecoDesconhecido = true;
			}
		}

		return enderecoDesconhecido;
	}
	
	private ProcessoParte carregarParteProcessual(PoloProcessual polo, br.jus.cnj.intercomunicacao.v222.beans.Parte parte, ProcessoTrf processoJudicial, boolean partePrincipal, PessoaQualificacaoEnum qualificacaoPessoal, ManifestacaoProcessual manifestacao, ProcessoParteSituacaoEnum parteSituacao, boolean parteSigilosa) throws Exception {
		String enderecoOrigemEnvio = MNIParametroUtil.obterValor(manifestacao, MNIParametro.PARAM_URL_ORIGEM_ENVIO);

		if (parte.getPessoa() == null) {
			throw new NegocioException(String.format("Não existe pessoa vinculada à parte no polo %s", polo));
		} else if (parte.getPessoa().getNome() == null || parte.getPessoa().getNome().trim().isEmpty()) {
			throw new NegocioException("Não foi especificado nome para a parte");
		}
		TipoParte tipoParte = carregarTipoParte(polo.getPolo(), processoJudicial.getClasseJudicial(), qualificacaoPessoal);
		if (tipoParte == null) {
			throw new NegocioException(String.format("Não foi possível especificar o tipo de participação da pessoa %s no polo %s", parte.getPessoa().getNome(), polo.getPolo()));
		}

		Pessoa pessoa = ComponentUtil.getComponent(PessoaMNIParaPessoaPJEConverter.class).converter(polo, parte.getPessoa(), qualificacaoPessoal);
		boolean enderecoDesconhecido = isEnderecoDesconhecido(polo.getPolo(), parte.getPessoa());
		
		ProcessoParte processoParte;
		ProcessoParteHistorico processoParteHistorico;
		List<ProcessoParte> listaProcessoParte = IntercomunicacaoUtil.existeParte(processoJudicial, pessoa, carregarPolo(polo.getPolo()), tipoParte, parteSituacao);
		processoParteHistorico = new ProcessoParteHistorico();
		
		if(listaProcessoParte.size() > 0){
			processoParte = listaProcessoParte.get(0);
			
			processoParte.setTipoParte(tipoParte);
			processoParte.setPartePrincipal(partePrincipal);
			processoParte.setIsEnderecoDesconhecido(enderecoDesconhecido);
			processoParte.setInSituacao(parteSituacao);
			processoParte.setParteSigilosa(parteSigilosa);
			
		} else{
			processoParte = new ProcessoParte(); 
			processoParte.setProcessoTrf(processoJudicial);
			processoParte.setInParticipacao(carregarPolo(polo.getPolo()));
			processoParte.setPessoa(pessoa);
			processoParte.setTipoParte(tipoParte);
			processoParte.setPartePrincipal(partePrincipal);
			processoParte.setIsEnderecoDesconhecido(enderecoDesconhecido);
			processoParte.setInSituacao(parteSituacao);
			processoParte.setParteSigilosa(parteSigilosa);
			
			ComponentUtil.getComponent(ProcessoParteManager.class).persist(processoParte);	
		}
		
		
		processoParteHistorico.setDataHistorico(new Date());
		processoParteHistorico.setProcessoParte(processoParte);
		processoParteHistorico.setInSituacao(parteSituacao);
		processoParteHistorico.setJustificativa("Histórico de situação criado via sistema. WebService PJe.");
		processoParteHistorico.setUsuarioLogin(obterUsuarioLogin());
		ComponentUtil.getComponent(ProcessoParteHistoricoManager.class).persist(processoParteHistorico);

		processoParte.getProcessoParteRepresentanteList().clear();
		processoParte.getProcessoParteRepresentanteList().addAll(carregarRepresentantesProcessuais(polo, parte, processoParte, manifestacao));

		if(!isRequisicaoDePJE && !StringUtils.isNotBlank(enderecoOrigemEnvio)){			
			if(enderecoDesconhecido && polo.getPolo() == ModalidadePoloProcessual.AT){
				throw new NegocioException(String.format("A informação de endereço das partes e seus representantes no polo ativo é obrigatória. "
						+ "Indique o endereço para a pessoa %s", pessoa.getNome()));
			}
		}
		
		if (!enderecoDesconhecido) {
			processoParte.getProcessoParteEnderecoList().clear();
			processoParte.getProcessoParteEnderecoList().addAll(carregarEnderecosParte(processoParte, parte.getPessoa()));
		}
		EntityUtil.flush();
		return processoParte;
	}	
			

	private List<ProcessoParteEndereco> carregarEnderecosParte(ProcessoParte processoParte, br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoaMNI) throws Exception {
		List<ProcessoParteEndereco> processoParteEnderecoList = new ArrayList<ProcessoParteEndereco>();
		
		List<br.jus.cnj.intercomunicacao.v222.beans.Endereco> enderecosMNI = pessoaMNI.getEndereco();
		if (ProjetoUtil.getTamanho(enderecosMNI) > 1) {
			throw new NegocioException("Não é possível cadastrar mais de um endereço para o polo '"+ pessoaMNI.getNome() +"'");
		}
		IntercomunicacaoEnderecoParaEnderecoConverter converter = new IntercomunicacaoEnderecoParaEnderecoConverter();
		List<Endereco> enderecosPJE = converter.converterColecao(enderecosMNI, processoParte.getPessoa());
		processoParteEnderecoList = ComponentUtil.getComponent(ProcessoParteEnderecoManager.class).salvar(processoParte, enderecosPJE);
		
		return processoParteEnderecoList;
	}

	private ProcessoParteParticipacaoEnum carregarPolo(ModalidadePoloProcessual tipoPolo) {
		// luis sergio PJEII-18635
				if (tipoPolo == null) {
					throw new NegocioException("Não foi definido um tipo de polo.");
				}
				// luis sergio
		switch (tipoPolo) {
		case AT:
			return ProcessoParteParticipacaoEnum.A;

		case PA:
			return ProcessoParteParticipacaoEnum.P;

		default:
			return ProcessoParteParticipacaoEnum.T;
		}
	}

	private TipoParte carregarTipoParte(ModalidadePoloProcessual tipoPolo, ClasseJudicial classeJudicial, PessoaQualificacaoEnum qualificacaoPessoal) throws Exception {
		TipoParte tipoParte = null;
		// luis sergio PJEII-18635
		if (tipoPolo == null) {
			throw new NegocioException("Não foi definido um tipo de polo.");
		}
		// luis sergio

		if (qualificacaoPessoal != null) {
            switch (qualificacaoPessoal) {
            case ADVOGADO:
                tipoParte = ParametroUtil.instance().getTipoParteAdvogado();
                if (tipoParte == null) {
                    throw new NegocioException("O parâmetro de sistema 'idTipoParteAdvogado' não corresponde ao identificador do tipo de parte advogado.");
                }
                break;

            case MINISTERIO_PUBLICO:
            case DEFENSORIA_PUBLICA:
            case PROCURADORIA:
                tipoParte = ParametroUtil.instance().getTipoParteRepresentante();
                if (tipoParte == null) {
                    throw new NegocioException("O parâmetro de sistema 'idTipoParteRepresentante' não corresponde ao identificador do tipo de parte representante.");
                }
                break;

            case ESCRITORIO_ADVOCACIA:
                break;
            }
		} else {
			if (tipoPolo == ModalidadePoloProcessual.AT) {
				tipoParte = ComponentUtil.getComponent(TipoParteManager.class).tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.A);
			} else if (tipoPolo == ModalidadePoloProcessual.PA) {
				tipoParte = ComponentUtil.getComponent(TipoParteManager.class).tipoPartePorClasseJudicial(classeJudicial, ProcessoParteParticipacaoEnum.P);
			} else {
				String nomeParticipacao = null;
				switch (tipoPolo) {
				case TJ:
					nomeParticipacao = "TESTEMUNHA";
					break;
				case AD:
					nomeParticipacao = "ASSISTENTE";
					break;
				case VI:
					nomeParticipacao = "VÍTIMA";
					break;
				// importacao ecnj
				case TC:
					nomeParticipacao = "TERCEIRO INTERESSADO";
					break;
					//luis sergio PJEII-18635
				case FL:
					nomeParticipacao = "FISCAL DA LEI";
					break;
				default:
					break;
				}

				List<TipoParte> list = ComponentUtil.getComponent(TipoParteManager.class).findByNomeParticipacao(nomeParticipacao);
				if (list.size() > 0) {
					tipoParte = list.get(0);
				} else {
					throw new NegocioException(String.format("Tipo de participação '%s' não localizado", nomeParticipacao));
				}
			}
		}
		return tipoParte;
	}
	
	private List<ProcessoTrfConexao> carregarProcessosVinculados(List<br.jus.cnj.intercomunicacao.v222.beans.VinculacaoProcessual> processosVinculados, ProcessoTrf processoJudicial) throws Exception {
		List<ProcessoTrfConexao> processoTrfConexaoList = new ArrayList<ProcessoTrfConexao>();
		ProcessoTrfConexaoManager processoTrfConexaoManager = ComponentUtil.getComponent(ProcessoTrfConexaoManager.class);
		for (br.jus.cnj.intercomunicacao.v222.beans.VinculacaoProcessual tipoVinculacaoProcessual : processosVinculados) {
			if (isNulo(tipoVinculacaoProcessual.getNumeroProcesso())) {
				throw new NegocioException("É obrigatório informar o número do processo vinculado");
			}

			String numeroProcessoVinculado = NumeroProcessoUtil.mascaraNumeroProcesso(tipoVinculacaoProcessual.getNumeroProcesso().getValue());
			ProcessoTrf processoJudicialVinculado = verificarProcessoNaBase(numeroProcessoVinculado);
			ProcessoTrfConexao processoConexao = new ProcessoTrfConexao();
			processoConexao.setNumeroProcesso(numeroProcessoVinculado);
			processoConexao.setProcessoTrf(processoJudicial);
			if (processoJudicialVinculado != null) {
				processoConexao.setProcessoTrfConexo(processoJudicialVinculado);
				OrgaoJulgador orgaoJulgador = processoJudicialVinculado.getOrgaoJulgador();
				processoConexao.setOrgaoJulgador(orgaoJulgador.getOrgaoJulgador());
			}
			processoConexao.setTipoConexao(carregarTipoConexao(tipoVinculacaoProcessual.getVinculo()));
			if (processoConexao.getTipoConexao().equals(TipoConexaoEnum.PR)) {
				processoConexao.setPrevencao(PrevencaoEnum.PE);
			}
			processoTrfConexaoManager.persist(processoConexao);
			processoTrfConexaoList.add(processoConexao);
		}
		return processoTrfConexaoList;
	}

	private ProcessoTrf verificarProcessoNaBase(String numeroProcesso) throws Exception {
		ProcessoTrf processoTrf = null;
		try {
			List<ProcessoTrf> processoTrfList = ComponentUtil.getComponent(ProcessoJudicialManager.class).findByNU(numeroProcesso);
			if (processoTrfList != null && !processoTrfList.isEmpty()) {
				processoTrf = processoTrfList.get(0);
			}
		} catch (PJeBusinessException e) {
			//Se ocorrer erro é porque o processo não foi encontrado.
		}

		return processoTrf;
	}

	private TipoConexaoEnum carregarTipoConexao(ModalidadeVinculacaoProcesso modalidadeVinculacaoProcesso) {
		/*
		 * Início - PJEII-18635 -Critica Modalidade Vinculada - Luis Sergio B. Machado 
		 * se for nulo será lançada exceção
		 */
		if(modalidadeVinculacaoProcesso==null){
			throw new NegocioException("Processo Vinculado: Infome a modalidade vinculação do processo");
		}
		return  ComponentUtil.getComponent(ModalidadeVinculacaoProcessoParaTipoConexaoConverter.class).converter(modalidadeVinculacaoProcesso);
	}

		private List<ProcessoParteRepresentante> carregarRepresentantesProcessuais(PoloProcessual polo,
																			   br.jus.cnj.intercomunicacao.v222.beans.Parte parte,
																			   ProcessoParte processoParte, 
																			   ManifestacaoProcessual manifestacao) throws Exception {
			
		PessoaProcuradoriaEntidadeManager pessoaProcuradoriaEntidadeManager = ComponentUtil.getComponent(PessoaProcuradoriaEntidadeManager.NAME);
		List<br.jus.cnj.intercomunicacao.v222.beans.RepresentanteProcessual> representantesProcessuais = parte.getAdvogado();
		List<ProcessoParteRepresentante> representanteProcessualList = new ArrayList<ProcessoParteRepresentante>();
		int indexParteRepresentante = 0;
		Procuradoria orgaoRepresentacaoParte = pessoaProcuradoriaEntidadeManager.getProcuradoriaPadraoPessoa(processoParte.getPessoa());
	 
		if(orgaoRepresentacaoParte != null){
			processoParte.setProcuradoria(orgaoRepresentacaoParte);
		}
		
		for (br.jus.cnj.intercomunicacao.v222.beans.RepresentanteProcessual representanteProcessual : representantesProcessuais) {
			br.jus.cnj.intercomunicacao.v222.beans.Parte tipoParte = converterParaTipoParte(representanteProcessual);
			
			if(tipoParte != null){
				PessoaQualificacaoEnum qualificacaoPessoal = carregarQualificacaoPessoal(representanteProcessual.getTipoRepresentante());
				ProcessoParte parteRepresentante;
				ProcessoParteSituacaoEnum parteSituacao = ProcessoParteSituacaoEnum.A;
				boolean sigiloso = carregaParteRepresentanteSigilo(polo, parte, indexParteRepresentante++, manifestacao, representantesProcessuais);

				if (isRequisicaoDePJE){
					parteSituacao = carregaParteRepresentanteSituacao(polo, parte, indexParteRepresentante++, manifestacao, representantesProcessuais);
				}
				
				parteRepresentante = carregarParteProcessual(polo, tipoParte, processoParte.getProcessoTrf(), false, qualificacaoPessoal, manifestacao, parteSituacao, sigiloso);
				
				if(parteRepresentante.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())) {
					processoParteList.add(parteRepresentante); //inclui o representante na lista de partes
					complementaCadastroAdvogado(representanteProcessual, parteRepresentante);
	
					ProcessoParteRepresentante processoParteRepresentante = new ProcessoParteRepresentante();
					processoParteRepresentante.setProcessoParte(processoParte);
					processoParteRepresentante.setParteRepresentante(parteRepresentante);
					processoParteRepresentante.setRepresentante(parteRepresentante.getPessoa());
					processoParteRepresentante.setInSituacao(ProcessoParteSituacaoEnum.A);
					processoParteRepresentante.setTipoRepresentante(ParametroUtil.instance().getTipoParteAdvogado());
					ComponentUtil.getComponent(ProcessoParteRepresentanteManager.class).persist(processoParteRepresentante);
					representanteProcessualList.add(processoParteRepresentante);
					
				}else{ //CASOS PROCURADORIA, DEFENSORIA, MINISTÉRIO PÚBLICO...
					if(qualificacaoPessoal == PessoaQualificacaoEnum.DEFENSORIA_PUBLICA 
							|| qualificacaoPessoal == PessoaQualificacaoEnum.PROCURADORIA
							|| qualificacaoPessoal == PessoaQualificacaoEnum.MINISTERIO_PUBLICO){
						
						Procuradoria orgaoRepresentacao = retornarOrgaoRepresentacao(qualificacaoPessoal, parteRepresentante.getPessoa()); 

						if(validarOrgaoRepresentacao(orgaoRepresentacao, processoParte, qualificacaoPessoal)){
							processoParte.setProcuradoria(orgaoRepresentacao);
						}
						 
						/**TODO
						 * PessoaQualificacaoEnum.ESCRITORIO_ADVOCACIA;
						 */
					}  
				}
			}
		}
		validarInsercaoProcuradoria(processoParte);
		
		return representanteProcessualList;
	}

	private br.jus.cnj.intercomunicacao.v222.beans.Parte converterParaTipoParte(br.jus.cnj.intercomunicacao.v222.beans.RepresentanteProcessual tipoRepresentanteProcessual) throws Exception {
		if(tipoRepresentanteProcessual != null){
			br.jus.cnj.intercomunicacao.v222.beans.Parte tipoParte = new br.jus.cnj.intercomunicacao.v222.beans.Parte();
			tipoParte.setPessoa(converterParaTipoPessoa(tipoRepresentanteProcessual));
			return tipoParte;
		}
		return null;
	}

	public br.jus.cnj.intercomunicacao.v222.beans.Pessoa converterParaTipoPessoa(br.jus.cnj.intercomunicacao.v222.beans.RepresentanteProcessual tipoRepresentanteProcessual) throws Exception {
		
		if (tipoRepresentanteProcessual.getNome() == null || tipoRepresentanteProcessual.getNome().trim().length() == 0) {
			throw new NegocioException("Não foi definido um nome para o representante processual");
		}

		if (tipoRepresentanteProcessual.getTipoRepresentante() == null) {
			throw new NegocioException(String.format("Não foi definido tipo para o representante processual %s", tipoRepresentanteProcessual.getNome()));
		}

		TipoQualificacaoPessoa tipoQualificacaoPessoa = tipoRepresentanteProcessual.getTipoRepresentante().equals(ModalidadeRepresentanteProcessual.A) ? TipoQualificacaoPessoa.FISICA
				: TipoQualificacaoPessoa.JURIDICA;

		br.jus.cnj.intercomunicacao.v222.beans.Pessoa tipoPessoa = new br.jus.cnj.intercomunicacao.v222.beans.Pessoa();
		tipoPessoa.setNome(tipoRepresentanteProcessual.getNome());
		tipoPessoa.setTipoPessoa(tipoQualificacaoPessoa);
		br.jus.cnj.intercomunicacao.v222.beans.DocumentoIdentificacao tipoDocumentoIdentificao;

		// documentos de identificacao
		if (tipoRepresentanteProcessual.getNumeroDocumentoPrincipal() != null) {
			// TODO Verificar se é um CPF ou CNPJ
			// TODO Mascarar o CPF/CNPJ antes de setá-lo
			tipoDocumentoIdentificao = montarTipoDocumentoIdentificacao(ModalidadeDocumentoIdentificador.CMF, tipoRepresentanteProcessual.getNumeroDocumentoPrincipal(), tipoPessoa.getNome(),
					"Secretaria da Receita Federal");
			tipoPessoa.getDocumento().add(tipoDocumentoIdentificao);
			
			tipoPessoa.setNumeroDocumentoPrincipal(
					MNIUtil.novoCadastroIdentificador(tipoRepresentanteProcessual.getNumeroDocumentoPrincipal()));
			
		}
		if (tipoRepresentanteProcessual.getInscricao() != null) {
			// TODO Mascarar o nr da OAB antes de setá-lo
			tipoDocumentoIdentificao = montarTipoDocumentoIdentificacao(ModalidadeDocumentoIdentificador.OAB, tipoRepresentanteProcessual.getInscricao().getValue(), tipoPessoa.getNome(),
					"Ordem dos Advogados do Brasil");
			tipoPessoa.getDocumento().add(tipoDocumentoIdentificao);
		}

		// endereços
		for (br.jus.cnj.intercomunicacao.v222.beans.Endereco tipoEndereco : tipoRepresentanteProcessual.getEndereco()) {
			tipoPessoa.getEndereco().add(tipoEndereco);
		}
		return tipoPessoa;
	}

	private br.jus.cnj.intercomunicacao.v222.beans.DocumentoIdentificacao montarTipoDocumentoIdentificacao(ModalidadeDocumentoIdentificador modalidadeDocumento, String numero, String nome,
			String orgaoEmissor) {
		br.jus.cnj.intercomunicacao.v222.beans.DocumentoIdentificacao tipoDocumentoIdentificao = new br.jus.cnj.intercomunicacao.v222.beans.DocumentoIdentificacao();
		tipoDocumentoIdentificao.setCodigoDocumento(numero);
		tipoDocumentoIdentificao.setTipoDocumento(modalidadeDocumento);
		tipoDocumentoIdentificao.setEmissorDocumento(orgaoEmissor);
		tipoDocumentoIdentificao.setNome(nome);
		return tipoDocumentoIdentificao;
	}

	private PessoaQualificacaoEnum carregarQualificacaoPessoal(ModalidadeRepresentanteProcessual tipoRepresentante) {
		PessoaQualificacaoEnum qualificacaoPessoal = null;
		// luis sergio PJEII-18635
		if (tipoRepresentante == null) {
			throw new NegocioException("Não foi definido um tipo de representante.");
		}
		// luis sergio
		switch (tipoRepresentante) {
		case A:
			qualificacaoPessoal = PessoaQualificacaoEnum.ADVOGADO;
			break;

		case E:
			qualificacaoPessoal = PessoaQualificacaoEnum.ESCRITORIO_ADVOCACIA;
			break;

		case M:
			qualificacaoPessoal = PessoaQualificacaoEnum.MINISTERIO_PUBLICO;
			break;

		case D:
			qualificacaoPessoal = PessoaQualificacaoEnum.DEFENSORIA_PUBLICA;
			break;

		case P:
			qualificacaoPessoal = PessoaQualificacaoEnum.PROCURADORIA;
			break;
		}
		return qualificacaoPessoal;
	}

	private Cep buscarCep(String numeroCep) {
		/*
		 * Início - PJEII-18270 - Luis Sergio B. Machado 15/09/2014
		 * se formatação estiver errada ou não for encontrado será lançada exceção
		 */
	    if(numeroCep == null || numeroCep.replaceAll("\\D", "").length() != 8){
			throw new NegocioException("Informe o CEP com 8 posições.");
		}
	    
	    Cep cep = ComponentUtil.getComponent(CepManager.class).findByCep(numeroCep);
	    
		if(cep == null) {
			throw new NegocioException("CEP "+ numeroCep +" não encontrado.");
		}
		/*
		 * Fim PJEII-18270 
		 */
		return cep;
	}
	
	/*
	 * Início - PJEII-8136 - Registro de prioridade processual é perdido ao ser
	 * remetido ou baixado processo. [CSJT] - Thiago Oliveira - 04/06/2013
	 */
	private void deletaEAtualizaProcessoPrioridadeProcesso(ProcessoTrf processoTrf, Parametro param) {
		
		// Apaga todos os ProcessoPrioridadeProcesso
		ComponentUtil.getComponent(ProcessoPrioridadeProcessoManager.class).removeTodosProcessoPrioridadeProcessoPorProcesso(processoTrf);

		String prioridades = param.getValor();
		
		if (StringUtils.isNotBlank(prioridades)) {
			String listaPrioridades[] = prioridades.split(";");
			ProcessoPrioridadeProcessoHome processoPrioridadeProcessoHome = ComponentUtil.getComponent(ProcessoPrioridadeProcessoHome.class);
			
			// Insere os novos ProcessoPrioridadeProcesso
			for (String prioridade : listaPrioridades) {
				PrioridadeProcesso prioridadeProcesso = EntityUtil.getEntityManager().getReference(PrioridadeProcesso.class, Integer.parseInt(prioridade));
				processoPrioridadeProcessoHome.getInstance().setProcessoTrf(processoTrf);
				processoPrioridadeProcessoHome.getInstance().setPrioridadeProcesso(prioridadeProcesso);
				processoPrioridadeProcessoHome.criaProcessoPrioridadeProcessoEntidade();
			}
		}
	}
	/*
	 * Fim - PJEII-8136
	 */
	
	/**
	 * Valida todos os atributos de um objeto, caso haja algum atributo com
	 * valor nulo será lançada uma exceção do tipo IntercomunicacaoException. Se
	 * for passado o parâmetro arrayAtributo para o método, somente os atributos
	 * relacionados no array serão validados. <br/>
	 * <br/>
	 * Exemplo: <dd>validarAtributos(objeto) // valida todos os atributos. <dd>
	 * validarAtributos(objeto, "nome", "endereco", "telefone"); // valida nome,
	 * endereço e telefone.
	 * 
	 * @param objeto
	 * @param arrayAtributo
	 * @throws Exception
	 *             exceção de parâmetro não informado.
	 */
	private void validarAtributos(Object objeto, String... arrayAtributo) throws NegocioException {
		
		if (objeto == null) {
			throw novoNegocioExceptionParametroNaoInformado(null);
		} else {
			try {
				if (ArrayUtils.isEmpty(arrayAtributo)) {
					Field[] arrayField = objeto.getClass().getDeclaredFields();
					for (Field atributo : arrayField) {
						Object valor = FieldUtils.readField(atributo, objeto, true);
						if (isNulo(valor)) {
							throw novoNegocioExceptionParametroNaoInformado(atributo.getName());
						}
					}
				} else {
					for (String atributo : arrayAtributo) {
						Object valor = PropertyUtils.getProperty(objeto, atributo);
						if (isNulo(valor)) {
							throw novoNegocioExceptionParametroNaoInformado(atributo);
						}
					}
				}
			} catch (InvocationTargetException e) {
				log.error(e);
			} catch (NoSuchMethodException e) {
				log.error(e);
			} catch (IllegalAccessException e) {
				log.error(e);
			}
		}
	}
	
	/**
	 * Valida o documento com o hash. O hash deverá ser gerada com o algoritmo SHA-256 ou MD5 e 
	 * posteriormente convertido em uma string hexadecimal. Se o documento não possuir um hash, será
	 * gerado um hash para atribuir ao campo hash.
	 * 
	 * Valida considerando as variações conteúdoUTF8-hashUTF8, conteudoUTF8-hashLatin1, 
	 * conteudoLatin1-hashUTF8, conteudoLatin1-hashLatin1. 
	 *  
	 * @param documento
	 * @param conteudo
	 * @return Hash MD5
	 */
	private void validarConteudoComHash(DocumentoProcessual documento, byte[] conteudo) {
		String encodeUTF8 = "UTF-8";
		String encodeLatin1 = "ISO-8859-1";		
		String hash = documento.getHash();		
		if (StringUtils.isNotBlank(hash) && conteudo != null) {
			Set<String> hashes = new HashSet<String>();
			try {
				if (StringUtils.length(hash) == 64) { 
					//SHA-256
					hashes.add(Crypto.encodeSHA256(conteudo).toLowerCase());
					hashes.add(Crypto.encodeSHA256(new String(conteudo,encodeUTF8).getBytes(encodeUTF8)).toLowerCase());
					hashes.add(Crypto.encodeSHA256(new String(conteudo,encodeUTF8).getBytes(encodeLatin1)).toLowerCase());
					hashes.add(Crypto.encodeSHA256(new String(conteudo,encodeLatin1).getBytes(encodeUTF8)).toLowerCase());
					hashes.add(Crypto.encodeSHA256(new String(conteudo,encodeLatin1).getBytes(encodeLatin1)).toLowerCase());
				} else if (StringUtils.length(hash) == 32) { 
					//MD5
					hashes.add(Crypto.encodeMD5(conteudo).toLowerCase());
					hashes.add(Crypto.encodeMD5(new String(conteudo,encodeUTF8).getBytes(encodeUTF8)).toLowerCase());
					hashes.add(Crypto.encodeMD5(new String(conteudo,encodeUTF8).getBytes(encodeLatin1)).toLowerCase());
					hashes.add(Crypto.encodeMD5(new String(conteudo,encodeLatin1).getBytes(encodeUTF8)).toLowerCase());
					hashes.add(Crypto.encodeMD5(new String(conteudo,encodeLatin1).getBytes(encodeLatin1)).toLowerCase());
				}				
			} catch (Exception ex) {
				if (!hashes.contains(hash.toLowerCase())) {
					String mensagem = "Erro inesperado validando hash de documento: '%s'.";
					throw new IntercomunicacaoException(String.format(mensagem, hash),ex);
				}				
			}
			if (!hashes.contains(hash.toLowerCase())) {
				String mensagem = "O hash '%s' não corresponde ao hash gerado do documento, verifique "
						+ "se o hash passado é uma String hexadecimal SHA-256 (64 bytes) ou MD5 (32"
						+ " bytes).";
				throw new IntercomunicacaoException(String.format(mensagem, hash));
			}			
		} else {
			if (conteudo != null) {
				hash = Crypto.encodeSHA256(conteudo);
			}
			documento.setHash(hash);
		}
	}
	
	/**
	 * Valida a existência de um documento do tipo 'petição inicial' na lista de documentos.
	 * 
	 * @param documentos Documentos que serão validados.
	 */
	private void validarExistenciaPeticaoInicial(
			List<DocumentoProcessual> documentos, Integer codClasseProcessual) {
		
		ClasseJudicial classeJudicial = ComponentUtil.getComponent(ClasseJudicialManager.class).findByCodigo(String.valueOf(codClasseProcessual));
		if (classeJudicial == null) {
			throw new NegocioException("Classe não encontrada: " + codClasseProcessual);
		}
		
		if (!this.isRemessaEntreInstancias()) {
			TipoProcessoDocumento peticao = classeJudicial.getTipoProcessoDocumentoInicial();
			
			boolean temPeticao = false;
			for (int indice = 0; indice < documentos.size() && temPeticao == false; indice++) {
				DocumentoProcessual documento = documentos.get(indice);
				temPeticao = StringUtils.equals(peticao.getCodigoDocumento(), documento.getTipoDocumento());
			}
			
			if (temPeticao == false) {
				throw new NegocioException(String.format(
						"Erro ao autuar processo: Não há documento inicial anexado ao processo. Favor anexar documento do tipo: %s (%s)",
						peticao.getTipoProcessoDocumento(), peticao.getCodigoDocumento()));

			}
		}
	}
	
	/**
	 * Retorna uma exceção de parâmetro não informado.
	 * 
	 * @param nomeAtributo
	 * @return exceção de parâmetro não informado.
	 */
	private NegocioException novoNegocioExceptionParametroNaoInformado(String nomeAtributo) {
		String mensagem = getMensagemParametroNaoInformado(nomeAtributo);
		return new NegocioException(mensagem); 
	}
	
	/**
	 * Retorna a mensagem de atributo não informado.
	 * 
	 * @param nomeAtributo
	 * @return mensagem de atributo não informado.
	 */
	private String getMensagemParametroNaoInformado(String nomeAtributo) {
		return String.format("Parâmetro %s não informado!", nomeAtributo);
	}
	
	/**
	 * Retorna true se o objeto for nulo, o método é usado para validar os
	 * atributos de um objeto do tipo Requisicao*.
	 * 
	 * @param objeto
	 * @return true se o objeto for nulo.
	 */
	@SuppressWarnings("rawtypes")
	private boolean isNulo(Object objeto) {
		return (objeto == null || 
				((objeto instanceof String) && StringUtils.isBlank((String) objeto) || 
				((objeto instanceof DataHora) && StringUtils.isBlank(((DataHora) objeto).getValue()))) || 
				((objeto instanceof NumeroUnico) && StringUtils.isBlank(((NumeroUnico) objeto).getValue())) || 
				((objeto instanceof Identificador) && StringUtils.isBlank(((Identificador) objeto).getValue())) || 
				((objeto instanceof List) && ProjetoUtil.isVazio((List) objeto)) ||
				((objeto instanceof Parametro) && StringUtils.isBlank(((Parametro) objeto).getValor())) ||
				((objeto instanceof br.jus.cnj.intercomunicacao.v222.beans.OrgaoJulgador) && StringUtils.isBlank(((br.jus.cnj.intercomunicacao.v222.beans.OrgaoJulgador) objeto).getCodigoOrgao())) ||
				((objeto instanceof CadastroIdentificador) && StringUtils.isBlank(((CadastroIdentificador) objeto).getValue())) ||
				((objeto instanceof DocumentoIdentificacao) && !MNIUtil.isDocumentoIdentificacaoPreenchido((DocumentoIdentificacao) objeto))
				);
	}
	
	/**
	 * Consulta a pessoa pelo CPF ou CNPJ informado.
	 * 
	 * @param cpfCnpj
	 *            CPF ou CNPJ.
	 * @return pessoa
	 * @throws PJeBusinessException
	 */
	private Pessoa obterPessoaPeloLogin(String login) throws PJeBusinessException {
		return ComponentUtil.getComponent(PessoaManager.class).findByLogin(login);
	}
	
	/**
	 * Consulta os expedientes com base nos parâmetros passados por parâmetro.
	 * 
	 * @param dataCriacao
	 *            Data de criação que é usada como critério 'a partir de'.
	 * @param idConsultante
	 *            Login do usuário.
	 * @param idRepresentado
	 * 			  Login do usuário representado.
	 * @param numeroProcesso
	 *            Número do processo.
	 * @param idExpediente
	 *            Id do aviso pendente.
	 * @return Lista de expedientes.
	 * 
	 * @throws Exception
	 */
	private List<ProcessoParteExpediente> consultarExpedientes(Date dataCriacao, String idConsultante, String idRepresentado, String numeroProcesso, Integer idExpediente, Boolean isFechado, TipoSituacaoExpedienteEnum tipoSituacaoExpediente) throws Exception {
		List<ProcessoParteExpediente> expedientes = new ArrayList<ProcessoParteExpediente>();
		Search search = new Search(ProcessoParteExpediente.class);
		PesquisaExpedientesVO pesquisaExpedientesVO = new PesquisaExpedientesVO();
		pesquisaExpedientesVO.setTipoSituacaoExpediente(tipoSituacaoExpediente);
		
		if (dataCriacao != null) {
			pesquisaExpedientesVO.setDataCriacaoExpedienteInicial(dataCriacao);
		}

		if (idRepresentado != null && !idRepresentado.isEmpty()) {
			Pessoa representado = obterPessoaPeloLogin(idRepresentado);
			if (representado != null){
				pesquisaExpedientesVO.setPessoaParteRepresentado(representado.getIdPessoa());
			}else{
				throw new NegocioException("Parte representada não localizada pelo identificador: "+idRepresentado+".");
			}
		}

		if (numeroProcesso != null && !numeroProcesso.isEmpty()) {
			String numeroProcessoFormatado = NumeroProcessoUtil.mascaraNumeroProcesso(numeroProcesso);
			Processo processo = ComponentUtil.getComponent(ProcessoManager.class).findByNumeroProcesso(numeroProcessoFormatado);
			if(processo != null){
				pesquisaExpedientesVO.setIdProcessoTrf(processo.getIdProcesso());
			}else{
				throw new NegocioException("Número do processo não localizada pelo identificador: "+numeroProcesso+".");
			}	
		}
		
		if (idExpediente != null) {
			pesquisaExpedientesVO.setIdProcessoParteExpediente(idExpediente);
		}

		
		if (search.getCriterias().isEmpty()){
			search = null;
		}
		ProcessoParteExpedienteManager processoParteExpedienteManager = ComponentUtil.getComponent(ProcessoParteExpedienteManager.class);
		expedientes.addAll(processoParteExpedienteManager.getExpedientes(pesquisaExpedientesVO, search));
		
		return expedientes;
	}

	/**
	 * Retorna o ProcessoTrf do número passado por parâmetro, se o processo não
	 * for encontrado será lançado uma exceção.
	 * 
	 * @param numeroProcesso
	 *          Número do processo que será localizado.
	 * @param filtroDePerfil 
	 * 			Booleano que indica se serão usados os filtros de perfil na consulta de processo.
	 * @return ProcessoTrf
	 * @throws PJeBusinessException
	 */
	private ProcessoTrf obterProcessoTrf(NumeroUnico numeroProcesso, boolean filtroDePerfil) throws PJeBusinessException {
		ProcessoTrf resultado = null;
		
		if (!isNulo(numeroProcesso)) {
			String numero = StringUtil.removeNaoNumericos(numeroProcesso.getValue());
			List<ProcessoTrf> processos = null;
			if (filtroDePerfil) {
				processos = ComponentUtil.getComponent(ProcessoJudicialManager.class).findByNUComFiltroDesabilitado(numero);
			} else {
				processos = ComponentUtil.getComponent(ProcessoJudicialManager.class).findByNU(numero);
			}

			if (processos != null && processos.size() == 1) {
				resultado = processos.get(0);
			} 
			if(resultado == null) {
				String mensagem = String.format("Processo de número %s não encontrado!", numero);
				throw new NegocioException(mensagem);
			}
		}
		return resultado;
	}
	
	/**
	 * Converte ProcessoTrf para CabecalhoProcessual.
	 * 
	 * @param processo
	 *            ProcessoTrf
	 * @return CabecalhoProcessual.
	 */
	private CabecalhoProcessual converterParaCabecalhoProcessual(ProcessoTrf processo, Boolean deveCarregarPartesDeTodasSituacoes) {
		return new ProcessoTrfParaCabecalhoProcessualConverter(deveCarregarPartesDeTodasSituacoes).converter(processo);
	}
	
	/**
	 * Converte uma coleção de ProcessoEvento em uma coleção de
	 * MobimentacaoProcessual.
	 * 
	 * @param colecao
	 *            coleção de ProcessoEvento.
	 * @return coleção convertida.
	 */
	private List<MovimentacaoProcessual> converterParaMovimentacaoProcessual(List<ProcessoEvento> colecao) {
		return ComponentUtil.getComponent(ProcessoEventoParaMovimentacaoProcessualConverter.class).converterColecao(colecao);
	}
	
	private DocumentoProcessual converterParaDocumentoProcessual(ProcessoDocumento documento,List<String> listaIdCarregarBinario) {
		DocumentoProcessual documentoMNI = ComponentUtil.getComponent(ProcessoDocumentoParaDocumentoConverter.class)
				.converter(documento, listaIdCarregarBinario);
		try {
			IntercomunicacaoUtil.adicionarOutrosParametros(documentoMNI, documento);
		} catch (Exception e) {
			String mensagem = String.format(
					"Não foi possível adicionar outros parâmetros ao documento processual. Erro: %s", e.getMessage());
			throw new NegocioException(mensagem);
		}
		return documentoMNI;
	}
	
	/**
  	 * @param numero
  	 * @return true se o número do processo for válido.
  	 */
  	private boolean isNumeroProcessoValido(NumeroUnico numero) {
  		return (!isNulo(numero) && !numero.getValue().replaceAll("[-/.]", "").equals(NUMERO_PROCESSO_NULO));
  	}

	/**
	 * Retorna novo filtro de PoloProcessual pela modalidade (ativo, passivo etc).
	 * 
	 * @param modalidade
	 *            Tipo do polo (Ex: CMF, OAB etc)
	 * @return filtro de polo processual.
	 */
	private Predicate novoFiltroPoloProcessualPelaModalidade(final ModalidadePoloProcessual modalidade) {
		Predicate resultado = null;
		
		if (modalidade != null) {
			resultado = new Predicate() {
				
				@Override
				public boolean evaluate(Object object) {
					PoloProcessual polo = (PoloProcessual) object;
					return (polo.getPolo() == modalidade);
				}
			};
		}
		return resultado;
	}

	/**
	 * Retorna novo filtro de PoloProcessual pela qualificação (física, jurídica, autoridade 
	 * e órgão de representação).
	 * 
	 * @param qualificacao
	 *            Tipo do polo (Ex: FISICA, JURÍDICA, AUTORIDADE e ORGÃO DE REPRESENTAÇÃO etc)
	 * @return filtro de polo processual.
	 */
	private Predicate novoFiltroPoloProcessualPelaQualificacao(final TipoQualificacaoPessoa qualificacao) {
		Predicate resultado = null;
		
		if (qualificacao != null) {
			resultado = new Predicate() {
				
				@Override
				public boolean evaluate(Object object) {
					Boolean resultado = Boolean.FALSE;

					PoloProcessual polo = (PoloProcessual) object;
					if (!isNulo(polo) && !polo.getParte().isEmpty()) {
						List<Parte> partes = polo.getParte();
						for (int indice = 0; indice < partes.size() && resultado == Boolean.FALSE; indice++) {
							Parte parte = partes.get(indice);
							br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoa = parte.getPessoa();
							resultado = (!isNulo(pessoa) && (pessoa.getTipoPessoa() == qualificacao));
						}
					}
					return resultado;
				}
			};
		}
		return resultado;
	}
	
	/**
	 * Retorna novo filtro de DocumentoIdentificacao preenchido..
	 * 
	 * @return filtro de DocumentoIdentificacao.
	 */
	private Predicate novoFiltroDocumentoIdentificacaoPreenchido() {
		
		return new Predicate() {
			
			@Override
			public boolean evaluate(Object object) {
				DocumentoIdentificacao identificacao = (DocumentoIdentificacao) object;
				return (isNulo(identificacao) == Boolean.FALSE);
			}
		};
	}

	/**
	 * Retorna novo filtro de DocumentoIdentificacao.
	 * 
	 * @param identificacao DocumentoIdentificacao.
	 * @return filtro de DocumentoIdentificacao.
	 */
	private Predicate novoFiltroDocumentoIdentificacao(final CadastroIdentificador identificacao) {
		
		return new Predicate() {
			
			@Override
			public boolean evaluate(Object object) {
				DocumentoIdentificacao id = (DocumentoIdentificacao) object;
				String codigoDocumento = StringUtils.trim(id.getCodigoDocumento());
				String identificacaoValue = (identificacao != null ? StringUtils.trim(identificacao.getValue()) : null);

				String codigoDocumentoSemFormatacao = StringUtil.removeNaoAlphaNumericos(codigoDocumento);
				String identificacaoValueSemFormatacao = StringUtil.removeNaoAlphaNumericos(identificacaoValue);
				return (
						StringUtils.equalsIgnoreCase(codigoDocumento, identificacaoValue) ||
						StringUtils.equalsIgnoreCase(codigoDocumentoSemFormatacao, identificacaoValueSemFormatacao));
			}
		};
	}

	/**
	 * Retorna um array contendo os expedientes que podem ser respondidos pelo usuário logado.
	 * 
	 * @param parametroIdsProcessoParteExpediente Parametro com a lista de expedientes.
	 * @return array de expedientes.
	 * @throws NumberFormatException
	 * @throws PJeBusinessException
	 */
	private ProcessoParteExpediente[] obterArrayProcessoParteExpediente(
			Parametro parametroIdsProcessoParteExpediente) throws NumberFormatException, PJeBusinessException {
		
		ProcessoParteExpediente[] resultado = null; 
		
		if (!isNulo(parametroIdsProcessoParteExpediente)) {
			Pessoa pessoaLogada = Authenticator.getPessoaLogada();
			String idsPPE = parametroIdsProcessoParteExpediente.getValor();
			String[] arrayIdsPPE = idsPPE.split(";");
			resultado = new ProcessoParteExpediente[arrayIdsPPE.length];
			List<ProcessoParteExpediente> listaPPE = new ArrayList<ProcessoParteExpediente>();
			AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
			ProcessoParteExpedienteManager processoParteExpedienteManager = ComponentUtil.getComponent(ProcessoParteExpedienteManager.class);
			for (int indice = 0; indice < arrayIdsPPE.length; indice++) {
				String id = arrayIdsPPE[indice];
				ProcessoParteExpediente ppe = processoParteExpedienteManager.findById(new Integer(id));
				if (atoComunicacaoService.isAptoParaResponder(ppe, pessoaLogada)) {
					listaPPE.add(ppe);
				} else {
					throw new NegocioException(String.format("O expediente %s não pode ser respondido. "
							+ "Favor verificar as condições: se o expediente está fechado ou "
							+ "se o usuário não tem permissão para responder o expediente.", id));
				}
			}
			listaPPE.toArray(resultado);
		}
		
		return resultado;
	}
	
	/**
	 * @return Usuário logado.
	 */
	private Usuario obterUsuarioLogin() {
		Usuario usuarioLogado = Authenticator.getUsuarioLogado();
		if (usuarioLogado == null) {
			usuarioLogado = ParametroUtil.instance().getUsuarioSistema();
		}
		return usuarioLogado;
	}
	
	/**
	 * Converte uma coleção de DocumentoProcessual em uma coleção de ProcessoDocumento e insere 
	 * na base de dados.
	 * 
	 * @param documentosProcessuais
	 * @param processo
	 * @param processoDocumentoPai
	 * @param isRequisicaoDePJE true se a requisição foi de um PJe (remessa)
	 * @param numeroProcesso1G Número do processo no 1G (usado nos casos de remessa)
	 * @return coleção de ProcessoDocumento.
	 * @throws Exception
	 */
	private List<ProcessoDocumento> carregarColecaoDocumentoProcessual(List<DocumentoProcessual> documentosProcessuais, Processo processo, ProcessoDocumento processoDocumentoPai, String numeroProcesso1G) throws Exception {
		List<ProcessoDocumento> resultado = new ArrayList<ProcessoDocumento>();
		
		for (int indice = 0; indice < documentosProcessuais.size(); indice++) {
			DocumentoProcessual documentoProcessual = documentosProcessuais.get(indice);
			if (processoDocumentoPai != null) {
				List<Parametro> parametros = documentoProcessual.getOutroParametro();
				if (!MNIParametroUtil.hasParametro(parametros, MNIParametro.PARAM_NUMERO_ORDEM)) {
					Parametro numeroOrdem = new Parametro();
					numeroOrdem.setNome(MNIParametro.PARAM_NUMERO_ORDEM);
					numeroOrdem.setValor(String.valueOf((indice + 1)));
					parametros.add(numeroOrdem);
				}
			}
			ProcessoDocumento pd = carregarDocumentoProcessual(documentoProcessual, processo, processoDocumentoPai,
					numeroProcesso1G);
			if (pd != null) {
				resultado.add(pd);
			}
		}
		
		return resultado;
	}

	/**
	 * Converte um DocumentoProcessual para um ProcessoDocumento e insere na base de dados.
	 * 
	 * @param documentoProcessual
	 * @param processo
	 * @param processoDocumentoPai
	 * @param isRequisicaoDePJE
	 * @param numeroProcesso1G Número do processo no 1G (usado nos casos de remessa)
	 * @return ProcessoDocumento criado.
	 * @throws Exception
	 */
	private ProcessoDocumento carregarDocumentoProcessual(DocumentoProcessual documentoProcessual, 
			Processo processo, ProcessoDocumento processoDocumentoPai,	String numeroProcesso1G) throws Exception {
		ProcessoDocumento processoDocumento = null;
		if (!isRequisicaoDePJE && !isNulo(documentoProcessual.getIdDocumento())) { 
		    documentoProcessual.setIdDocumento(null);
		}
		if (!ComponentUtil.getComponent(ProcessoDocumentoManager.class).existeDocumentoPorIdentificadorInstanciaOrigem(processo.getIdProcesso(), documentoProcessual.getIdDocumento())) {
			byte[] bytes = ProjetoUtil.converterParaBytes(documentoProcessual.getConteudo());
			validarDocumentoProcessual(documentoProcessual, bytes);
			validarConteudoComHash(documentoProcessual, bytes);
			
			processoDocumento = ComponentUtil.getComponent(DocumentoProcessualParaProcessoDocumentoConverter.class).converter(
					documentoProcessual, processo, processoDocumentoPai, false, isRequisicaoDePJE);
			Integer idPd = 0;
			processoDocumento.setIdProcessoDocumento(idPd);
			// Atualizando as pessoas que criaram os documentos  
			// Só deve ser feito quando o protocolo for de uma remessa
			atualizaCriadorArquivoRemessaRetorno(isRequisicaoDePJE, processoDocumento, documentoProcessual);
			validarProcessoDocumentoSemAssinatura(processoDocumento);
			
			if(!isRequisicaoDePJE){
				processoDocumento.setPapel(Authenticator.getPapelAtual());
				processoDocumento.setLocalizacao(Authenticator.getLocalizacaoAtual());
				
			}
			
			ProcessoDocumentoBin processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
			List<ProcessoDocumentoBinPessoaAssinatura> signatarios = processoDocumentoBin.getSignatarios();
			
			processoDocumentoBin = ComponentUtil.getComponent(ProcessoDocumentoBinManager.class).persist(processoDocumentoBin);
			if (processoDocumentoBin.getSize() == 0) {
				int id = processoDocumentoBin.getIdProcessoDocumentoBin();
				inserirDocumentoDownloadPosterior(documentoProcessual, id, numeroProcesso1G);
			} else {
				if(processoDocumentoBin.getBinario()){
					String numeroStorage = null;
					if (ParametroUtil.instance().isBaseBinariaUnificada() && !isNulo(processoDocumentoBin.getNumeroDocumentoStorage())) {
						numeroStorage = processoDocumentoBin.getNumeroDocumentoStorage();
					} else {
						numeroStorage = ComponentUtil.getComponent(DocumentoBinManager.class).persist(
								processoDocumentoBin.getProcessoDocumento(), 
								processoDocumentoBin.getExtensao());
					}
					processoDocumentoBin.setNumeroDocumentoStorage(numeroStorage);
				} else {
					String modeloDocumento = processoDocumentoBin.getModeloDocumento();
					String numeroStorage = processoDocumentoBin.getNumeroDocumentoStorage();
					if (!isNulo(modeloDocumento)) {
						processoDocumentoBin.setModeloDocumento(modeloDocumento);
					} else {
						if (!ParametroUtil.instance().isBaseBinariaUnificada() && !isNulo(numeroStorage)) {
							byte[] bytesStorage = ComponentUtil.getComponent(DocumentoBinManager.class).getData(numeroStorage);
							processoDocumentoBin.setModeloDocumento(new String(bytesStorage));
						}
					}
					processoDocumentoBin.setNumeroDocumentoStorage(numeroStorage);
				}
				//ComponentUtil.getComponent(ProcessoDocumentoBinManager.class).merge(processoDocumentoBin);
			}
			ProcessoDocumentoBinPessoaAssinaturaManager processoDocumentoBinPessoaAssinaturaManager = ComponentUtil.getComponent(ProcessoDocumentoBinPessoaAssinaturaManager.class);
			for (ProcessoDocumentoBinPessoaAssinatura signatario : signatarios) {
				processoDocumentoBinPessoaAssinaturaManager.persist(signatario);
			}
			
			processoDocumento.setProcessoDocumentoBin(processoDocumentoBin);
			ComponentUtil.getComponent(ProcessoDocumentoManager.class).persist(processoDocumento);
			
			List<ProcessoDocumento> colecaoPd = carregarColecaoDocumentoProcessual(documentoProcessual.getDocumentoVinculado(), processo, 
					processoDocumento, numeroProcesso1G);
			
			processoDocumento.getDocumentosVinculados().addAll(colecaoPd);
		}
		
		return processoDocumento;
	}
	
	/**
	 * Consulta as prioridades passadas no cabeçalho processual, as prioridades serão consultadas
	 * pelo ID ou Descrição.
	 * 
	 * @param cabecalhoProcessual CabecalhoProcessual
	 * @param processoTrf ProcessoTrf
	 * @return coleção de prioridades.
	 * @throws PJeBusinessException 
	 */
	private void salvarPrioridadeProcesso(
			CabecalhoProcessual cabecalhoProcessual, ProcessoTrf processoTrf) throws PJeBusinessException {
		
		List<String> prioridades = cabecalhoProcessual.getPrioridade();
		if (ProjetoUtil.isNotVazio(prioridades)) {
			//A conversão de List para Set é para remover os valores repetidos.
			Set<String> prioridadesSet = new LinkedHashSet<String>(prioridades);
			ProcessoPrioridadeProcessoManager processoPrioridadeProcessoManager = ComponentUtil.getComponent(ProcessoPrioridadeProcessoManager.class);
			PrioridadeProcessoManager prioridadeProcessoManager = ComponentUtil.getComponent(PrioridadeProcessoManager.class);
			for (String prioridade : prioridadesSet) {
				PrioridadeProcesso pp = prioridadeProcessoManager.obterPeloIDouDescricao(prioridade);
				if (pp == null) {
					String mensagem = String.format("A prioridade \"%s\" não existe e não pode ser utilizada.", prioridade);
					throw new NegocioException(mensagem);
				} else {
					ProcessoPrioridadeProcesso ppp = new ProcessoPrioridadeProcesso();
					ppp.setPrioridadeProcesso(pp);
					ppp.setProcessoTrf(processoTrf);
					processoPrioridadeProcessoManager.persist(ppp);
				}
			}
		}
	}

	/**
	 * Valida se existe mais de um registro da parte. A validação será feita
	 * somente nos casos de acesso externo, ou seja, a validação não será executada no caso de 
	 * remessa.
	 * 
	 * @param tipoManifestacaoProcessual
	 */
	/**
	 * Valida as regras quando o manifestante for Jus Postulandi.
	 * @param manifestacao
	 */
	private void verificarManifestacaoJusPostulandi(ManifestacaoProcessual manifestacao) {
		
		if(!ignorarAutenticacao && Authenticator.isJusPostulandi()){
			List<PoloProcessual> listaPoloAtivo = MNIUtil.obterColecaoPoloAtivo(manifestacao);
			Parte parteAtivo = MNIUtil.obterParte(listaPoloAtivo);
			
			//Manifestante Jus Postulandi
			Pessoa manifestante = Authenticator.getPessoaLogada();
			
			if (listaPoloAtivo.size() != 1 || 
				MNIUtil.isIguais(parteAtivo, manifestante) == false ||
				MNIUtil.isPossuiRepresentante(parteAtivo)) {
				throw new NegocioException(String.format("O jus postulandi \"%s\" deve ser o único integrante do polo ativo", 
						manifestante.getNome()));
			}
		}
	}

	/**
	 * Lança a exceção NegocioException no caso de não haver parte nos polos ativo e/ou passivo, caso a 
	 * classe processual exija.
	 * 
	 * @param manifestacaoProcessual
	 * @param classeJudicial
	 */
	private void validarExistenciaParte(ManifestacaoProcessual manifestacaoProcessual, 
			ClasseJudicial classeJudicial) {
		
		CabecalhoProcessual cabecalhoProcessual = manifestacaoProcessual.getDadosBasicos();
		List<PoloProcessual> listaPolo = cabecalhoProcessual.getPolo();
		String mensagem = "Erro ao autuar processo: " 
						+ "Deve haver ao menos uma parte no polo %s vinculada ao processo.";
		
		
		Predicate filtro = novoFiltroPoloProcessualPelaModalidade(ModalidadePoloProcessual.AT);
		boolean isExistePolo = CollectionUtils.exists(listaPolo, filtro);
			
		if (!isExistePolo) {
			throw new NegocioException(String.format(mensagem, "ativo"));
		}
		
		if (classeJudicial.getReclamaPoloPassivo()) {
			filtro = novoFiltroPoloProcessualPelaModalidade(ModalidadePoloProcessual.PA);
			isExistePolo = CollectionUtils.exists(listaPolo, filtro);
			
			if (!isExistePolo) {
				throw new NegocioException(String.format(mensagem, "passivo"));
			}
		}
		
		/*[PJEII-23317][PJEII-21127]
		 * Verifica Fiscal da Lei
		 * O Fiscal da Lei é inserido automaticamente no protocolo do processo, seguindo as configurações 
		 * da instalação do PJe.
		 * 
		 * ProcessoTrfHome.protocolar();
		 *  ...
		 *  ProcessoParteHome.instance().verificarExigenciaFiscalDaLei(getInstance());
		 *  ...
		 */
		filtro = novoFiltroPoloProcessualPelaModalidade(ModalidadePoloProcessual.FL);
		isExistePolo = CollectionUtils.exists(listaPolo, filtro);
		
		if (isExistePolo) {
			throw new NegocioException(String.format("A parte no polo 'Fiscal da Lei' é inserida automaticamente pelo sistema, por favor, remova-a da entrega de manifestação.", "Fiscal da Lei"));
		}

	}

	/**
	 * Valida as regras quando o manifestante for Advogado.
	 * @param manifestacao
	 */
	private void verificarManifestacaoAdvogado(ManifestacaoProcessual manifestacao) {
		
		if(!ignorarAutenticacao && 
				Authenticator.isUsuarioExterno() && 
				Identity.instance().hasRole("advogado") 
				){
			
			List<PoloProcessual> listaPoloAtivo = MNIUtil.obterColecaoPoloAtivo(manifestacao);
			boolean usuarioRepresentantePoloAtivo = false;
			
			for(PoloProcessual poloAtivo : listaPoloAtivo){
				for(Parte parte : poloAtivo.getParte()){
					if(MNIUtil.isPossuiRepresentante(parte)){
						for(RepresentanteProcessual representante : parte.getAdvogado()){
							if(InscricaoMFUtil.retiraMascara(representante.getNumeroDocumentoPrincipal()).
									equals(InscricaoMFUtil.retiraMascara(manifestacao.getIdManifestante()))){
								usuarioRepresentantePoloAtivo = true;
								break;
							}
						}
					}
				}
			}
			
			if(!usuarioRepresentantePoloAtivo){
				throw new NegocioException(String.format("O advogado \"%s\", usuário manifestante, "
						+ "deve ser informado como representante de ao menos uma parte do polo ativo.", 
						Authenticator.getUsuarioLogado().getNome()));
			}
			
			List<PoloProcessual> listaPoloPassivo = MNIUtil.obterColecaoPoloPassivo(manifestacao);

			for(PoloProcessual poloPassivo : listaPoloPassivo){
				for(Parte parte : poloPassivo.getParte()){
					if(MNIUtil.isPossuiRepresentante(parte)){
						throw new NegocioException("Usuário advogado não pode cadastrar advogados no polo passivo");	
					}
				}
			}
			
			
			
			
		}
	}

	/**
	 * Valida os campos se os campos complementares obrigatórios da classe estão presentes na 
	 * Manifestação Processual.
	 * 
	 * @param manifestacao ManifestacaoProcessual
	 * @param processo ProcessoTrf
	 * @param classe ClasseJudicial
	 */
	private List<ComplementoClasseProcessoTrf> carregarComplementoClasseProcessoTrf(ManifestacaoProcessual manifestacao, ProcessoTrf processo, ClasseJudicial classe) {
		List<ComplementoClasseProcessoTrf> resultado = new ArrayList<ComplementoClasseProcessoTrf>();
		
		if (manifestacao != null && classe != null) {
			List<ComplementoClasse> complementos = ComponentUtil.getComponent(ComplementoClasseManager.class).getListComplementoClasse(classe);
			
			if (!ProjetoUtil.isVazio(complementos)) {
				for (ComplementoClasse complemento : complementos) {
					String nome = complemento.getComplementoClasse();
					String valor = MNIParametroUtil.obterValor(manifestacao, nome);
					
					ComplementoClasseProcessoTrf novo = new ComplementoClasseProcessoTrf();
					novo.setProcessoTrf(processo);
					novo.setComplementoClasse(complemento);
					novo.setValorComplementoClasseProcessoTrf(valor);

					ComplementoClasseUtil.validar(novo);
					resultado.add(novo);
				}
			}
		}
		
		return resultado;
	}
	
	/**
	 * Valida se o documento possui assinatura.
	 * 
	 * @param processoDocumento
	 */
	private void validarProcessoDocumentoSemAssinatura(ProcessoDocumento documento) {
		
		if (documento != null && 
			documento.getProcessoDocumentoBin() != null && 
			documento.getProcessoDocumentoBin().getSignatarios().isEmpty()) {
			TipoProcessoDocumento tipo = documento.getTipoProcessoDocumento();
			String mensagem = "Documento '%s-%s' sem assinatura, não é possível juntar documento sem assinatura.";
			throw new NegocioException(String.format(
					mensagem,
					tipo.getCodigoDocumento(),
					documento.toString()));
		}
	}
	
	/**
	 * Valida as partes ENTE ou AUTORIDADE, conforme regra RN357.
	 * Regra implementada: 
	 * <pre>
	 * SE 'exige ente ou autoridade' E 'não tem autoridade' ENTÃO
	 * 	 erro "Erro ao protocolar processo: a classe judicial escolhida 'Exige ente ou autoridade' no polo ativo ou passivo."
	 * FIM SE
	 * 
	 * SE 'usuário externo' ENTÃO
	 *  SE 'tem autoridade' E NÂO ('permite autoridade' OU 'exige autoridade') ENTÃO
	 * 		erro "Erro ao protocolar processo: a classe judicial escolhida NÃO 'Permite ente ou autoridade' no polo ativo ou passivo."
	 *  FIM SE
	 * FIM SE
	 * </pre>
	 * @param manifestacao
	 * @param classe
	 */
	private void validarEnteOuAutoridade(ManifestacaoProcessual manifestacao, ClasseJudicial classe) {
		List<PoloProcessual> autoridades = consultarPoloProcessual(
				manifestacao, 
				null, 
				TipoQualificacaoPessoa.AUTORIDADE);

		Boolean exigeAutoridade = classe.getExigeAutoridade();
		Boolean permiteAutoridade = classe.getPermiteAutoridade();
		
		if (exigeAutoridade && autoridades.isEmpty()) {
			throw new NegocioException("Erro ao protocolar processo: a classe judicial escolhida 'Exige ente ou autoridade' no polo ativo ou passivo.");
		}
			
		if (Authenticator.isUsuarioExterno()) {
			if(!autoridades.isEmpty() && !(permiteAutoridade || exigeAutoridade)){
				throw new NegocioException("Erro ao protocolar processo: a classe judicial escolhida NÃO 'Permite ente ou autoridade' no polo ativo ou passivo.");
			}
		}
	}
	
	/**
	 * Consulta os polos do processo que satisfaçam os critérios da consulta.
	 * @param manifestacao Manifestação processual.
	 * @param modalidade Modalidade (ATIVO, PASSIVO Etc...)
	 * @param qualificacao (FÍSICA, JURÍDICA, AUTORIDADE Etc...)
	 * @return  Lista de polos.
	 */
	@SuppressWarnings("unchecked")
	private List<PoloProcessual> consultarPoloProcessual(ManifestacaoProcessual manifestacao, 
			ModalidadePoloProcessual modalidade, TipoQualificacaoPessoa qualificacao) {
		Collection<PoloProcessual> resultado = new ArrayList<PoloProcessual>();
		if (!isNulo(manifestacao) && !isNulo(manifestacao.getDadosBasicos())) {
			Collection<PoloProcessual> polos = manifestacao.getDadosBasicos().getPolo();
			
			Predicate filtroModalidade = novoFiltroPoloProcessualPelaModalidade(modalidade);
			Predicate filtroQualificacao = novoFiltroPoloProcessualPelaQualificacao(qualificacao);
			if (filtroModalidade != null) {
				polos = CollectionUtils.select(polos, filtroModalidade);
			}
			
			if (filtroQualificacao != null) {
				polos = CollectionUtils.select(polos, filtroQualificacao);
			}
			resultado.addAll(polos);
		}
		
		return (List<PoloProcessual>) resultado;
	}

	/**
	 * Valida o tipo dos documentos enviados para a entrega de manifestação.
	 * O método é recursivo para que possa percorrer os documentos principais e anexos.
	 *
	 * @param documentos
	 * @param inicial True se for petição inicial ou false se for entrega avulsa.
	 * @param principal True se for um documento principal e false se for um anexo.
	 */
	private void validarTipoDocumento(List<DocumentoProcessual> documentos, boolean inicial, boolean principal, Integer codClasseProcessual) throws PJeBusinessException {
		
		if (!isRequisicaoDePJE && ProjetoUtil.isNotVazio(documentos)) {

			List<TipoProcessoDocumento> tiposPrincipaisValidos = this.consultarTipoProcessoDocumentoPrincipaisValidos(inicial, codClasseProcessual);
			List<TipoProcessoDocumento> tiposAnexosValidos = this.consultarTipoProcessoDocumentoAnexosValidos(inicial, codClasseProcessual);
			
			for (DocumentoProcessual documento : documentos) {
				String codigo = documento.getTipoDocumento();
				TipoProcessoDocumento tipo = obterTipoProcessoDocumento(codigo);
				
				if(this.isRemessaEntreInstancias()) {
					if(!(tiposPrincipaisValidos.contains(tipo) || tiposAnexosValidos.contains(tipo))) {
						throw novoNegocioExceptionTipoDocumentoInvalido(codigo);
					}
				}else {
					if(principal && !tiposPrincipaisValidos.contains(tipo)) {
						throw novoNegocioExceptionTipoDocumentoInvalido(codigo);
					}else if(!principal && !tiposAnexosValidos.contains(tipo)) {
						throw novoNegocioExceptionTipoDocumentoInvalido(codigo);
					}
				}
				
				validarTipoDocumento(documento.getDocumentoVinculado(), inicial, false, codClasseProcessual);
			}
		}
	}

	/**
	 * Consulta os tipos de documento permitidos para o usuário em uma determinada situação e 
	 * natureza do documento.
	 * <br/>Situações:
	 * <dd>Peticionamento inicial</dd>
	 * <dd>Peticionamento avulso</dd>
	 * <br/>Natureza do documento:
	 * <dd>Documento principal</dd>
	 * <dd>Anexo</dd>
	 * @param inicial True se for peticionamento inicial e false se for avulso.
	 * @param principal True se o documento for principal e false se for anexo. 
	 * @return
	 */
	private List<TipoProcessoDocumento> consultarTipoProcessoDocumentoValidos(boolean inicial, boolean principal, Integer codClasseProcessual) {
		List<TipoProcessoDocumento> resultado = new ArrayList<TipoProcessoDocumento>();
		
		ProtocolarDocumentoBean protocolarDocumentoBean = new ProtocolarDocumentoBean(null);
		
		ClasseJudicial classeJudicial = ComponentUtil.getComponent(ClasseJudicialManager.class).findByCodigo(String.valueOf(codClasseProcessual), false);
		
		//Documento principal
		if (principal) {
			if(this.isRemessaEntreInstancias()) {
				resultado.addAll(protocolarDocumentoBean.getTiposDocumentosTexto(false));
			} else {
				resultado.addAll(protocolarDocumentoBean.getTiposDocumentosTexto(true));
			}
		} else { //Documento anexo
			resultado.addAll(protocolarDocumentoBean.getTiposDocumentos(TipoDocumentoEnum.T, TipoDocumentoEnum.D, TipoDocumentoEnum.P));
		}
		
		//Entrega de manifestação avulsa.
		//Remover a petição inicial
		if (inicial == false) {
			resultado.remove(classeJudicial.getTipoProcessoDocumentoInicial());
		}
		
		return resultado;
	}
	
	private List<TipoProcessoDocumento> consultarTipoProcessoDocumentoPrincipaisValidos(boolean inicial, Integer codClasseProcessual) {
		if(CollectionUtilsPje.isEmpty(this.tiposPrincipaisValidos)) {
			this.tiposPrincipaisValidos = this.consultarTipoProcessoDocumentoValidos(inicial, Boolean.TRUE, codClasseProcessual);
		}
		return this.tiposPrincipaisValidos;
	}

	private List<TipoProcessoDocumento> consultarTipoProcessoDocumentoAnexosValidos(boolean inicial, Integer codClasseProcessual) {
		if(CollectionUtilsPje.isEmpty(this.tiposAnexosValidos)) {
			this.tiposAnexosValidos = this.consultarTipoProcessoDocumentoValidos(inicial, Boolean.FALSE, codClasseProcessual);
		}
		return this.tiposAnexosValidos;
	}
	/**
	 * Lança a exceção NegocioException para tipo de documento inválido.
	 * 
	 * @param codigo Código do documento.
	 * @return NegocioException para tipo de documento inválido.
	 */
	private NegocioException novoNegocioExceptionTipoDocumentoInvalido(String codigo) {
		String mensagem = "Tipo de documento inválido para esta manifestação: %s";
		return new NegocioException(String.format(mensagem , codigo));
	}

	/**
	 * Consulta as movimentações processuais do ProcessoTrf. As movimentações serão retornadas
	 * somente se o parâmetro 'requisicaoConsultaProcesso.isMovimentos' for true.
	 * 
	 * @param processoTrf ProcessoTrf.
	 * @param requisicaoConsultaProcesso RequisicaoConsultaProcesso.
	 * @return Lista de movimentação processual.
	 */
	private List<MovimentacaoProcessual> consultarMovimentacaoProcessual(
			ProcessoTrf processoTrf, 
			RequisicaoConsultaProcesso requisicaoConsultaProcesso) {
		List<MovimentacaoProcessual> resultado = new ArrayList<MovimentacaoProcessual>();
		
		boolean carregarMovimentos = BooleanUtils.toBoolean(requisicaoConsultaProcesso.isMovimentos());
		if (carregarMovimentos) {
			Date dataReferencia = ConversorUtil.converterParaDate(requisicaoConsultaProcesso.getDataReferencia());
			List<ProcessoEvento> eventos = ComponentUtil.getComponent(ProcessoEventoManager.class).recuperaMovimentos(processoTrf, dataReferencia, false);
			resultado.addAll(converterParaMovimentacaoProcessual(eventos));
		}
		
		return resultado;
	}

	/**
	 * Consulta os documentos do ProcessoTrf. Os documentos serão recuperados somente os metadatas 
	 * ou o metadata com o arquivo binário.
	 * 
	 * @param processoTrf ProcessoTrf.
	 * @param requisicaoConsultaProcesso RequisicaoConsultaProcesso.
	 * @return Lista dos documentos do processo.
	 */
	private List<DocumentoProcessual> consultarDocumentoProcessual(
			ProcessoTrf processoTrf, 
			RequisicaoConsultaProcesso requisicaoConsultaProcesso) {
		
		List<DocumentoProcessual> resultado = new ArrayList<DocumentoProcessual>();
		
		//Se true o metadata do arquivo será retornado.
		Boolean carregarMetadata = BooleanUtils.toBoolean(requisicaoConsultaProcesso.isIncluirDocumentos());

		//ID's dos documentos que terão o binário consultado, ou '*' para retornar o binário de todos os documentos.
		List<String> listaIdDocumento = requisicaoConsultaProcesso.getDocumento();

		//Se existe documento binário que será retornado.
		Boolean isExisteBinarioParaCarregar = !ProjetoUtil.isVazio(listaIdDocumento);

		validarLimiteQuantidadeDocumentosBinariosConsultadosMNIPermitido(listaIdDocumento);
					
		//Faz a consulta de documentos somente se for para recuperar os metadatas ou algum arquivo binário.
		if (carregarMetadata || isExisteBinarioParaCarregar) {
			Date dataReferencia = ConversorUtil.converterParaDate(requisicaoConsultaProcesso.getDataReferencia());
			
			PjeIdentity identity = (PjeIdentity) PjeIdentity.instance();
			ProcessoTrfHome home = ProcessoTrfHome.instance();
			List<ProcessoDocumento> documentos = home.consultarDocumentoJuntadoEhCiente(processoTrf, dataReferencia);

			if (listaIdDocumento.contains(TODOS)) {
				validarLimiteQuantidadeDocumentosBinariosConsultadosMNIPermitido(documentos);
			}
			LogAcessoAutosDownloadsService logAutos = ComponentUtil.getComponent(LogAcessoAutosDownloadsService.class);
			for (ProcessoDocumento documento: documentos) {
				//remove os vinculados não consultados, pois se não estiverem na lista 'documentos' é porque o usuário 
				//não tem permissão para visualizá-los.
				removerDocumentosVinculadosNaoConsultados(documento, documentos);
				// Faz o log de acesso aos documentos
				logAutos.logarDownloadMNI(documento);
				
				if(!documento.getDocumentoSigiloso() || identity.isLogouComCertificado()){
					String id = String.valueOf(documento.getIdProcessoDocumento());
					//Se true o binário do documento será retornado.
					Boolean carregarBinario = listaIdDocumento.contains(id) || listaIdDocumento.contains(TODOS);
					
					//Serão recuperados os documentos do processo se for informado para carregar o metadata ou o binário.
					if (carregarMetadata || carregarBinario) {
						removerDocumentosVinculadosNaoRequisitados(documento, carregarMetadata, listaIdDocumento);
						DocumentoProcessual documentoProcessual = converterParaDocumentoProcessual(documento, listaIdDocumento);
						if (!isDocumentoJaIncluido(resultado, documentoProcessual)) {
							resultado.add(documentoProcessual);
						}
					}
				}
			}
		}
		return resultado;
	}

	/**
 	 * Lança a exceção NegocioException no caso de partes que não possuem documento de identificação.
 	 * 
 	 * @param manifestacaoProcessual
 	 */
 	private void validarExistenciaDocumentoIdentificacaoDosPolos(ManifestacaoProcessual manifestacaoProcessual) {
 		CabecalhoProcessual cabecalhoProcessual = manifestacaoProcessual.getDadosBasicos();
 		List<PoloProcessual> polos = cabecalhoProcessual.getPolo();
 		Predicate filtroDocumentoIdentificacaoPreenchido = novoFiltroDocumentoIdentificacaoPreenchido();
 		if (Authenticator.isUsuarioExterno()){
 	 		for (PoloProcessual polo : polos) {
 	 			boolean isPoloAtivo = (polo.getPolo() == ModalidadePoloProcessual.AT);
 	 			if (isPoloAtivo){
 	 	 			for (Parte parte : polo.getParte()) {
 	 	 				br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoa = parte.getPessoa(); 	 	 				
 	 	 				boolean existeDocumentoIdentificacaoPreenchido = CollectionUtils.exists(pessoa.getDocumento(), filtroDocumentoIdentificacaoPreenchido);
 	 	 				if (isNulo(pessoa) || !existeDocumentoIdentificacaoPreenchido) {
 	 	 	 				if(pessoa.getTipoPessoa() != TipoQualificacaoPessoa.AUTORIDADE){
 	 	 	 					String mensagem = "Documento de identificação não informado para parte '%s.'";
 	 	 	 					throw new NegocioException(String.format(mensagem, pessoa.getNome())); 	 	 	 					
 	 	 	 				}
 	 	 				}
 	 	 			} 	 				
 	 			}
 	 		}
 			
 		}
 	}

	/**
	 * Valida o tamanho e o mimetype dos documentos enviados para a entrega de manifestação.
	 * O método é recursivo para que possa percorrer os documentos principais e anexos.
	 *
	 * @param documentos Lista de documentos.
	 */
	private void validarTamanhoEhMimetypeDocumento(List<DocumentoProcessual> documentos) throws PJeBusinessException {
		
		
		if (!isRequisicaoDePJE && ProjetoUtil.isNotVazio(documentos)) {

			for (DocumentoProcessual documento : documentos) {
				int tamanho = ProjetoUtil.getTamanho(documento.getConteudo());
				String mime = MNIUtil.obterMimeType(documento);
				String nome = (StringUtils.isNotBlank(documento.getDescricao()) ? 
						documento.getDescricao() : 
						obterTipoProcessoDocumento(documento.getTipoDocumento()).getTipoProcessoDocumento());
				
				MimeUtilChecker checker = ComponentUtil.getComponent(MimeUtilChecker.class);
				checker.checkAllowed(nome, mime, (long) tamanho);
				
				validarTamanhoEhMimetypeDocumento(documento.getDocumentoVinculado());
			}
		}
	}
	
	/**
	 * Retorna o tipo do documento.
	 * 
	 * @param codigoTipoDocumento
	 * @return tipo do documento.
	 */
	protected TipoProcessoDocumento obterTipoProcessoDocumento(
			String codigoTipoDocumento) {
		try {
			return ComponentUtil.getComponent(TipoProcessoDocumentoManager.class).findByCodigoDocumento(codigoTipoDocumento, Boolean.TRUE);
		} catch (PJeBusinessException e) {
			String instancia = ParametroUtil.instance().getInstancia();
			String mensagem = "O tipo de documento '%s', não foi localizado na instância '%s', erro: %s";
			throw new IntercomunicacaoException(String.format(mensagem,
					codigoTipoDocumento, instancia, e.getLocalizedMessage()));
		}
	}

	/**
	 * Valida se foi passado o número do processo caso a classe exigir numeração própria.
	 * 
	 * @param manifestacao
	 * @param classe
	 * @throws PJeBusinessException 
	 */
	private void validarClasseExigeNumeracaoPropria(ManifestacaoProcessual manifestacao, ClasseJudicial classe) throws PJeBusinessException {
		Boolean exigeNumeracaoPropria = classe.getExigeNumeracaoPropria();
		CabecalhoProcessual cabecalhoProcessual = manifestacao.getDadosBasicos();
		NumeroUnico numeroUnico = cabecalhoProcessual.getNumero();
		String numeroUnicoString = (isNulo(numeroUnico) == false ? StringUtil.removeNaoNumericos(numeroUnico.getValue()) : null);
		String numeroUnicoStringMascarado = NumeroProcessoUtil.mascaraNumeroProcesso(numeroUnicoString);
		
		if (BooleanUtils.isFalse(exigeNumeracaoPropria)) {
			if (!isNulo(numeroUnico) && isNumeroProcessoValido(numeroUnico) == false) { //Se o número do processo for inválido
				throw new NegocioException(String.format("Número de processo %s inválido", numeroUnico.getValue()));
			} else if (ComponentUtil.getComponent(ProcessoManager.class).findByNumeroProcesso(numeroUnicoStringMascarado) != null) { //Se o processo já existir
				throw new NegocioException(String.format("O processo de nº %s já existe, " + "caso deseje atualizar este processo, repita a operação informando o parâmetro "
						+ "\"numeroProcesso\" no \"tipoManifestacaoProcessual\"", numeroUnicoStringMascarado));
			}
		} else {
			manifestacao.getDadosBasicos().setNumero(null);
		}
	}

	/**
	 * Lança a exceção NegocioException no caso do documento de identificação de uma parte não conferir
	 * com um documento da sua lista de documentos.
	 * 
	 * @param manifestacaoProcessual
	 */
	private void validarDocumentoIdentificacaoPrincipalDosPolos(ManifestacaoProcessual manifestacaoProcessual) {
		CabecalhoProcessual cabecalhoProcessual = manifestacaoProcessual.getDadosBasicos();
		List<PoloProcessual> polos = cabecalhoProcessual.getPolo();
		
		for (PoloProcessual polo : polos) {
			
			for (Parte parte : polo.getParte()) {
				
				br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoa = parte.getPessoa();
				CadastroIdentificador documentoPrincipal = pessoa.getNumeroDocumentoPrincipal();
				Predicate filtro = novoFiltroDocumentoIdentificacao(documentoPrincipal);
				
				if(parte.getPessoa().getTipoPessoa() == null ||
					 	(parte.getPessoa().getTipoPessoa() != null &&
					 	!parte.getPessoa().getTipoPessoa().equals(TipoQualificacaoPessoa.AUTORIDADE)) &&
						documentoPrincipal != null && !documentoPrincipal.getValue().isEmpty()){
					
					if(pessoa.getDocumento() == null ||
							(pessoa.getDocumento() != null && pessoa.getDocumento().size() > 0 &&
							!CollectionUtils.exists(pessoa.getDocumento(), filtro))){
						
						String mensagem = "Documento de identificação da parte '%s' não confere com o código do documento "
								+ "principal. É preciso preencher o atributo 'pessoa.numeroDocumentoPrincipal' com um valor "
								+ "existente na lista de atributos de 'pessoa.documento'.";
						
						throw new NegocioException(String.format(mensagem, pessoa.getNome()));
					}
				}
			}
		}
	}

	/**
	 * Lança a exceção NegocioException no caso de tipoPessoa não preenchido adequadamente. 
	 * O campo tipoPessoa é de preenchimento obrigatório.
	 * 
	 * @param manifestacaoProcessual
	 */
	private void validarTipoPessoaPreenchido(ManifestacaoProcessual manifestacaoProcessual) {
		CabecalhoProcessual cabecalhoProcessual = manifestacaoProcessual.getDadosBasicos();
		List<PoloProcessual> polos = cabecalhoProcessual.getPolo();
		
		for (PoloProcessual polo : polos) {
			for (Parte parte : polo.getParte()) {
				br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoa = parte.getPessoa();
				validarAtributos(pessoa, "tipoPessoa");
				if(parte.getPessoa().getPessoaVinculada() != null){
					validarAtributos(parte.getPessoa().getPessoaVinculada(), "tipoPessoa");
				}
			}
		}
	}

	/**
	 * Lança a exceção NegocioException no caso de documentos de identificação não preenchidos adequadamento. 
	 * Os campos codigoDocumento, emissorDocumento e tipoDocumento são de preenchimento obrigatório.
	 * 
	 * @param manifestacaoProcessual
	 */
	private void validarDocumentoIdentificacaoPreenchido(ManifestacaoProcessual manifestacaoProcessual) {
		CabecalhoProcessual cabecalhoProcessual = manifestacaoProcessual.getDadosBasicos();
		List<PoloProcessual> polos = cabecalhoProcessual.getPolo();
		TipoDocumentoIdentificacao tipoDocumentoIdentificacao;
		DocumentoIdentificacaoParaTipoDocumentoIdentificacaoConverter documentoIdentificacaoParaTipoDocumentoIdentificacaoConverter = ComponentUtil.getComponent(DocumentoIdentificacaoParaTipoDocumentoIdentificacaoConverter.class);
		for (PoloProcessual polo : polos) {			
			for (Parte parte : polo.getParte()) {				
				br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoa = parte.getPessoa();				
				for (DocumentoIdentificacao identificacao : pessoa.getDocumento()) {
					tipoDocumentoIdentificacao = documentoIdentificacaoParaTipoDocumentoIdentificacaoConverter.converter(identificacao, pessoa.getTipoPessoa());
					if(tipoDocumentoIdentificacao != null && tipoDocumentoIdentificacao.isOrgaoExpedidorObrigatorio() && !isRequisicaoDePJE){
					  validarAtributos(identificacao, "codigoDocumento", "emissorDocumento", "tipoDocumento");
					}else{
					  validarAtributos(identificacao, "codigoDocumento", "tipoDocumento");
					}					
				}
			}
		}
	}

	/**
	 * Lança a exceção NegocioException no caso de documentos de identificação não inválidos. 
	 * Os campos codigoDocumento, emissorDocumento e tipoDocumento são de preenchimento obrigatório.
	 * 
	 * @param manifestacaoProcessual
	 */
	private void validarDocumentoIdentificacao(ManifestacaoProcessual manifestacaoProcessual) {

		if(!isRequisicaoDePJE){
			
			CabecalhoProcessual cabecalhoProcessual = manifestacaoProcessual.getDadosBasicos();
			List<PoloProcessual> polos = cabecalhoProcessual.getPolo();
	
			for (PoloProcessual polo : polos) {
				
				for (Parte parte : polo.getParte()) {
					
					br.jus.cnj.intercomunicacao.v222.beans.Pessoa pessoa = parte.getPessoa();
					
					for (DocumentoIdentificacao identificacao : pessoa.getDocumento()) {
						boolean valido = true;
						switch(identificacao.getTipoDocumento()){
						case CMF:
							if(pessoa.getTipoPessoa() == TipoQualificacaoPessoa.FISICA){
								valido = InscricaoMFUtil.verificaCPF(identificacao.getCodigoDocumento());
							}
							else if (pessoa.getTipoPessoa() == TipoQualificacaoPessoa.JURIDICA){
								valido = InscricaoMFUtil.verificaCNPJ(identificacao.getCodigoDocumento());
							}
						//TODO implementar outros validadores
						case CC:
							break;
						case CEI:
							break;
						case CI:
							break;
						case CN:
							break;
						case CNH:
							break;
						case CP:
							break;
						case CT:
							break;
						case IF:
							break;
						case NIT:
							break;
						case OAB:
							break;
						case PAS:
							break;
						case PIS_PASEP:
							break;
						case RGE:
							break;
						case RIC:
							break;
						case RJC:
							break;
						case TE:
							break;
						default:
							break;
						}
						
						if(!valido){
							throw new NegocioException(String.format("Documento de identificação inválido para a pessoa: %s (tipo de pessoa: %s, tipo de documento: %s, código: %s)", 
									pessoa.getNome(), pessoa.getTipoPessoa(), identificacao.getTipoDocumento(), identificacao.getCodigoDocumento()));
						}
					}
				}
			}
		}
	}

	private void complementaCadastroAdvogado(RepresentanteProcessual representanteProcessual, ProcessoParte parteRepresentante) throws PJeBusinessException{
		CadastroOAB documentoOab = representanteProcessual.getInscricao();
		
		if (documentoOab != null) {
			String oab = documentoOab.getValue();
			
			// Regex com o padrão AA9999999A
			String regex = "^(\\D{2})?(\\d{1,7})?(\\D{1})?$";
			final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
			final Matcher matcher = pattern.matcher(oab);
			
			// Se o padrão do número OAB for condizente com a respectiva regra os dados do advogado serão atualizados.
			if (matcher.find()) {
				String ufOab = matcher.group(1);
				String numeroOab = StringUtil.retiraZerosEsquerda(matcher.group(2));
				String letraOab = matcher.group(3);
				
				Estado estado = ComponentUtil.getComponent(EstadoManager.class).findBySigla(ufOab);
				Pessoa representante = parteRepresentante.getPessoa();
				
				PessoaAdvogado advogado = null;
				if (representante instanceof PessoaFisica) {
					advogado = ((PessoaFisica) representante).getPessoaAdvogado();
				} else {
					advogado = EntityUtil.getEntityManager().getReference(PessoaAdvogado.class, representante.getIdPessoa());
				}
				
				if (advogado != null){
					PessoaAdvogadoManager advogadoManager = ComponentUtil.getComponent(PessoaAdvogadoManager.class);
					advogadoManager.complementaCadastro(advogado,  estado, numeroOab, letraOab);
				}else{
				 throw new IntercomunicacaoException(String.format("Representante processual (Advogado) não localizado no destino."));
				}
			}
		}
	}
	
	private Procuradoria retornarOrgaoRepresentacao(PessoaQualificacaoEnum qualificacaoPessoal, Pessoa pessoa){
		Procuradoria procuradoria = null;
		
		if (pessoa != null) {
			String cnpj = pessoa.getDocumentoCpfCnpj();
			String nome = pessoa.getNome();
			try{
				PessoaJuridica pessoJuridica = ComponentUtil.getComponent(PessoaJuridicaManager.class).findByCNPJ(cnpj);
				Procuradoria orgaoRepresentacao = ComponentUtil.getComponent(ProcuradoriaManager.class).findByPessoaJuridica(pessoJuridica);
				return orgaoRepresentacao;	
			}catch(Exception e){
				String mensagem = "O CNPJ (%s - %s) do representante processual não corresponde a um Órgão de Representação (%s) válido no sistema. Erro: %s";
				throw new NegocioException(String.format(mensagem, cnpj, nome, qualificacaoPessoal, e.getMessage()));
			}
		}

		return procuradoria;
	}

	private Procuradoria retornarOrgaoRepresentacaoParte(ProcessoParte processoParte) throws PJeBusinessException{
		List<PessoaProcuradoriaEntidade> listaPessoaProcuradoriaEntidade = ComponentUtil.getComponent(PessoaProcuradoriaEntidadeManager.class).getListaPessoaProcuradoriaEntidade(processoParte.getPessoa());

		if(orgaoRepresentacao != null){			
			for(PessoaProcuradoriaEntidade pessoaProcuradoriaEntidade : listaPessoaProcuradoriaEntidade){
				if(pessoaProcuradoriaEntidade.getProcuradoria().equals(orgaoRepresentacao)){
					return orgaoRepresentacao;
				}
			}
		}

		if(!listaPessoaProcuradoriaEntidade.isEmpty() && listaPessoaProcuradoriaEntidade.size() == 1){
			return listaPessoaProcuradoriaEntidade.get(0).getProcuradoria();
		}else{
			return null;	
		}
	}	
	
	private boolean validarOrgaoRepresentacao(Procuradoria orgaoRepresentacao, ProcessoParte processoParte, PessoaQualificacaoEnum qualificacaoPessoal) throws PJeBusinessException{
		Boolean resultado = Boolean.FALSE;
		
		if (orgaoRepresentacao != null && processoParte != null) {
			String nomeParte = ""+processoParte.getNomeParte()+"/"+processoParte.getPessoa().getDocumentoCpfCnpj();
			String cnpj = ""+orgaoRepresentacao.getPessoaJuridica().getDocumentoCpfCnpj();
			
			if (qualificacaoPessoal == PessoaQualificacaoEnum.DEFENSORIA_PUBLICA) {
				if (ComponentUtil.getComponent(PessoaProcuradoriaEntidadeManager.class).getListaPessoaProcuradoriaEntidade(processoParte.getPessoa()).size() > 0) {
					throw new NegocioException("A parte "+nomeParte+" não pode ser representada por uma Defensoria, pois já é representada por uma Procuradoria.");				
				}
				
				if (processoParte.getPessoa().getTipoPessoa() != ParametroUtil.instance().getTipoPessoaFisica() &&
					processoParte.getPessoa().getTipoPessoa() != ParametroUtil.instance().getTipoPessoaJuridica() && 
					!processoParte.getPessoa().getTipoPessoa().isChildrenOf(ParametroUtil.instance().getTipoPessoaJuridica())) {
					throw new NegocioException("A defensoria informada no CNPJ "+cnpj+" só pode representar pessoas físicas ou jurídicas.");
				}
							
				resultado = true;
			}
			
			if (qualificacaoPessoal == PessoaQualificacaoEnum.PROCURADORIA ||
					qualificacaoPessoal == PessoaQualificacaoEnum.MINISTERIO_PUBLICO) {
				List<Integer> idsPessoasRepresentadas = ComponentUtil.getComponent(PessoaProcuradoriaEntidadeManager.class).getIdsPessoasRepresentadasPorProcuradoria(orgaoRepresentacao.getIdProcuradoria());
				
				if (idsPessoasRepresentadas.contains(processoParte.getPessoa().getIdPessoa())) {
					resultado = true;
				} else {
					if (isRequisicaoDePJE) {
						PessoaProcuradoriaEntidade ppe = new PessoaProcuradoriaEntidade();
						ppe.setPessoa(processoParte.getPessoa());
						ppe.setProcuradoria(orgaoRepresentacao);
						ComponentUtil.getComponent(PessoaProcuradoriaEntidadeManager.class).persistAndFlush(ppe);
						resultado = true;
					} else {
						throw new NegocioException("O Órgão de Representação ("+qualificacaoPessoal+") informado no CNPJ "+cnpj+" não pode ser registrado como representante da parte "+nomeParte+" no sistema.");
					}
				}
			}
		}
		
		return resultado;
	}
	
	private void validarOrgaoRepresentacaoUsuario(String idOrgaoRepresentacao) throws PJeBusinessException{
		if(Authenticator.isProcurador()){
			PessoaProcurador procurador = ComponentUtil.getComponent(PessoaProcuradorManager.class).findById(Authenticator.getPessoaLogada().getIdPessoa());
			PessoaJuridica pessoaJuridica = ComponentUtil.getComponent(PessoaJuridicaManager.class).findByCNPJ(idOrgaoRepresentacao);
			List<Procuradoria> procuradoriasRepresentantes = null;
			
			if(procurador == null){
				throw new NegocioException("O usuário/senha informado não corresponde a um Procurador registrado no sistema.");				
			}
			if(pessoaJuridica == null){
				throw new NegocioException("Identificador ("+idOrgaoRepresentacao+") do Órgão de Representação (Procuradoria/Defensoria) não localizado.");				
			}
			
			procuradoriasRepresentantes = ComponentUtil.getComponent(ProcuradoriaManager.class).getlistProcuradorias(pessoaJuridica);

			if(CollectionUtilsPje.isEmpty(procuradoriasRepresentantes)){
				throw new NegocioException("CNPJ ("+idOrgaoRepresentacao+") do Órgão de Representação não localizado no sistema.");				
			}
			
			Procuradoria procuradoriaProcuradorOrgao = ComponentUtil.getComponent(PessoaProcuradoriaManager.class).recuperaProcuradoriaDoProcurador(
					(PessoaFisica) Authenticator.getPessoaLogada(), procuradoriasRepresentantes);
			
			
			if(procuradoriaProcuradorOrgao != null){
				orgaoRepresentacao = procuradoriaProcuradorOrgao;
			}else{
			  throw new NegocioException("Este usuário não está vinculado ao órgão de representação informado.");
			}
		}else{
			throw new NegocioException("O usuário/senha informado não possui papel de Procurador/Defensor.");
		}
		
	}
	
	private void validarInsercaoProcuradoria(ProcessoParte processoParte){
		List<PessoaProcuradoriaEntidade> procuradorias = ComponentUtil.getComponent(PessoaProcuradoriaEntidadeManager.class).getListaPessoaProcuradoriaEntidade(processoParte.getPessoa());
		
		if(procuradorias.size() > 0 && processoParte.getProcuradoria() == null){
			String relacaoProcuradorias = "";

			for(PessoaProcuradoriaEntidade pessoaProcuradoriaEntidade : procuradorias)
				relacaoProcuradorias += "\t\t\t\t"+pessoaProcuradoriaEntidade.getProcuradoria()+"\n";
				
			throw new NegocioException("Informe a procuradoria representante da parte "+processoParte.getPessoa()+"/"+processoParte.getPessoa().getDocumentoCpfCnpj()+", pois há mais de uma opção possível: \n"+relacaoProcuradorias);
		}								
	}
	
	/**
	 * Inicia o fluxo do documento principal.
	 * 
	 * @param processoTrf
	 * @param documentos
	 * @return Documento principal que foi entregue e disparou fluxo de petição incidental
	 */
	private ProcessoDocumento iniciarFluxoDocumentoPrincipal(ProcessoTrf processoTrf, List<ProcessoDocumento> documentos) throws PJeBusinessException {
		return iniciarFluxoDocumentoPrincipal(processoTrf, documentos, null, true);
	}
	
	private ProcessoDocumento iniciarFluxoDocumentoPrincipal(ProcessoTrf processoTrf, List<ProcessoDocumento> documentos, Map<String, Object> mapaVariaveisParaFluxo, boolean validarSeExisteFluxoIniciadoDoc) throws PJeBusinessException {
		ProcessoDocumento principal = (documentos.isEmpty() == false? documentos.get(0): null);
		
		if (principal != null) {
			//incluir a movimentacao
			ProtocolarDocumentoBean.lancarMovimentacaoProcessual(principal, Identity.instance().hasRole(Papeis.INTERNO));
			
			// se o documento possuir tipo com fluxo configurado.
			if (ComponentUtil.getComponent(ProcessoDocumentoManager.class).isDocumentoComFluxoConfigurado(principal)) {
				//configuração do sistema que indica inicialização de fluxos incidentais.
				Boolean isPodeIniciarFluxoPeticaoIncidental = ParametroUtil.instance().isSempreDispararFluxoIncidental();
				//true se existir fluxo criado para o processo e tipo de documento do documento principal.
				Boolean isExisteFluxoIniciadoParaDocumento = ComponentUtil.getComponent(ProcessoJudicialService.class).isExisteFluxoIniciadoParaDocumento(processoTrf, principal);
				
				if (isPodeIniciarFluxoPeticaoIncidental) {
					
					if (!isExisteFluxoIniciadoParaDocumento) {
						if(mapaVariaveisParaFluxo != null) {
							Events.instance().raiseEvent(Eventos.INICIAR_FLUXO_PETICAO_INCIDENTAL_COM_VARIAVEIS_EXTRAS, principal.getIdProcessoDocumento(),mapaVariaveisParaFluxo);	
						}else {
							Events.instance().raiseEvent(Eventos.INICIAR_FLUXO_PETICAO_INCIDENTAL, principal.getIdProcessoDocumento());	
						}
					} else {
						String mensagem = "Não foi possível iniciar fluxo para documento '%s' em razão de já existir um fluxo no processo '%s' para o tipo de documento '%s'.";
						throw new PJeBusinessException(String.format(mensagem, 
								principal.getIdProcessoDocumento(), 
								processoTrf.getNumeroProcesso(), 
								principal.getTipoProcessoDocumento().getTipoProcessoDocumento()));
					}
				}
			}
		}
		
		return principal;
	}

	public ServicosPJeMNIEnum getServicoAtual() {
		return servicoAtual;
	}

	public void setServicoAtual(ServicosPJeMNIEnum servicoAtual) {
		this.servicoAtual = servicoAtual;
	}
	
	private ClasseJudicialInicialEnum obterClasseJudicialInicial() {
		if (isRemessaEntreInstancias()) {
			return ClasseJudicialInicialEnum.R;
		} else {
			return ClasseJudicialInicialEnum.I;
		}
	}

	private boolean isRemessaEntreInstancias() {
		return isRequisicaoDePJE || Authenticator.isUsuarioInterno();
	}

	/**
	 * @return documentos presentes na requisição.
	 */
	public List<ProcessoDocumento> getDocumentosRequisicao() {
		return documentosRequisicao;
	}

	/**
	 * @param documentos Atribui documentos que estão presentes na requisição.
	 */
	public void setDocumentosRequisicao(List<ProcessoDocumento> documentos) {
		this.documentosRequisicao = documentos;
	}

	/**
	 * Verifica se mensagem possui a numeração do processo da origem
	 * quando houver o parâmetro  urlOrigemEnvio
	 * 
	 * @param tipoManifestacaoProcessual
	 */
	private void verificarRemessa(ManifestacaoProcessual manifestacao) {
		String enderecoOrigemEnvio = MNIParametroUtil.obterValor(manifestacao, MNIParametro.PARAM_URL_ORIGEM_ENVIO);
		
		if(enderecoOrigemEnvio != null && !enderecoOrigemEnvio.isEmpty()){
			if (!manifestacao.getDadosBasicos().isSetNumero() || manifestacao.getDadosBasicos().getNumero().getValue().isEmpty()) {
				throw new NegocioException("Número do processo de origem não informado nos Dados Básicos!");
			}
		}
	}

	/**
	 * Verifica se o endereço WSDL da origem está 
	 * cadastrado no PJE destino.
	 * 
	 * @param tipoManifestacaoProcessual
	 */
	private void verificarEnderecoWsdlOrigem(ManifestacaoProcessual manifestacao) {
		String enderecoOrigemEnvio = MNIParametroUtil.obterValor(manifestacao, MNIParametro.PARAM_URL_ORIGEM_ENVIO);

		if(isRequisicaoDePJE || StringUtils.isNotBlank(enderecoOrigemEnvio)){
			String enderecoOrigemConsulta = MNIParametroUtil.obterValor(manifestacao, MNIParametro.PARAM_URL_ORIGEM_CONSULTA);

			EnderecoWsdl endereco = new EnderecoWsdl();

			endereco.setWsdlConsulta(enderecoOrigemConsulta);
			endereco.setWsdlIntercomunicacao(enderecoOrigemEnvio);

			if(ComponentUtil.getComponent(EnderecoWsdlManager.class).getDAO().obterPeloWsdl(endereco) == null){
				throw new NegocioException("O endereço de origem da remessa não consta na lista de endereços reconhecidos na instância de destino.");
			}
		}
	}	
	
	private void validarProcessoRemetido(ProcessoTrf processoTrf) throws PJeBusinessException {
		if (!isRemessaEntreInstancias() && ComponentUtil.getComponent(ProcessoTrfManager.class).isProcessoRemetidoBloqueado(processoTrf)) {
			throw new NegocioException("Não é possível juntar documentos. O processo está em outra instância no momento.");
		}
	}

	private void validarProcessoPeticaoBloqueada(ProcessoTrf processoTrf) throws PJeBusinessException {
		if (!isRemessaEntreInstancias() && processoTrf.getInBloqueiaPeticao()) {
			throw new NegocioException("Processo bloqueado para peticionamento.");
		}
	}
	
	/**
	 * Recupera se existem classes judiciais retificação de autos baseados nos parâmetros  idJurisdicao e idClasseJudicial.
	 * @param idJurisdicao Id da jurisdição.
	 * @return Boolean existemClassesJudiciaisRetificacaoAutos
	 */
	private boolean isCorrelacaoClasseCompetenciaJurisdicaoValida(int idJurisdicao, ClasseJudicial classeJudicial) {
		return ComponentUtil.getComponent(ClasseJudicialManager.class).isExistemClassesJudiciaisRetificacaoAutos(idJurisdicao, classeJudicial.getIdClasseJudicial());
	}

	/**
	 * @return True se o serviço chamado foi o "Consultar avisos pendentes".
	 */
	public Boolean isServicoConsultarAvisosPendentes() {
		return ServicosPJeMNIEnum.ConsultarAvisosPendentes.equals(getServicoAtual());
	}
	
	/**
	 * @return True se o serviço chamado foi o "Consultar processo".
	 */
	public Boolean isServicoConsultarProcesso() {
		return ServicosPJeMNIEnum.ConsultarProcesso.equals(getServicoAtual());
	}
	
	/**
	 * @return True se o serviço chamado foi o "Consultar teor de comunicação".
	 */
	public Boolean isServicoConsultarTeorComunicacao() {
		return ServicosPJeMNIEnum.ConsultarTeorComunicacao.equals(getServicoAtual());
	}
	
	/**
	 * @return True se o serviço chamado foi o "Entregar manifestação processual".
	 */
	public Boolean isServicoEntregarManifestacaoProcessual() {
		return ServicosPJeMNIEnum.EntregarManifestacaoProcessual.equals(getServicoAtual());
	}
	
 	/**
 	 * Retorna true se o documento já estiver presente na lista como documento principal ou vinculado.
 	 * 
 	 * @param documentos Lista de documentos.
 	 * @param documentoReferencia Documento que será validado.
 	 * @return booleano
 	 */
 	private boolean isDocumentoJaIncluido(List<DocumentoProcessual> documentos, DocumentoProcessual documentoReferencia) {
 		Boolean resultado = Boolean.FALSE;
		
 		if (!ProjetoUtil.isVazio(documentos) && !isNulo(documentoReferencia)) {
 			for (int indice = 0; indice < documentos.size() && !resultado; indice++) {
 				DocumentoProcessual documento = documentos.get(indice);
 				resultado = StringUtils.equals(documento.getIdDocumento(), documentoReferencia.getIdDocumento());
 				
 				List<DocumentoProcessual> vinculados = documento.getDocumentoVinculado();
 				
 				for (int indice2 = 0; indice2 < vinculados.size() && !resultado; indice2++) {
 					DocumentoProcessual vinculado = vinculados.get(indice2);
 					resultado = StringUtils.equals(vinculado.getIdDocumento(), documentoReferencia.getIdDocumento());
				}
 			}
 		}
		return resultado;
	}

	/**
 	 * Remove os documentos vinculados não requisitados.
 	 * 
 	 * @param documento Documento principal.
 	 * @param carregarMetadata True se for para carregar o metadata do documento vinculado.
 	 * @param listaIdCarregarBinario ID's dos documentos que 
 	 */
 	private void removerDocumentosVinculadosNaoRequisitados(ProcessoDocumento documento, Boolean carregarMetadata, List<String> listaIdCarregarBinario) {
		if (!isNulo(documento) && ProjetoUtil.getTamanho(documento.getDocumentosVinculados()) > 0) {
			Set<ProcessoDocumento> vinculados = documento.getDocumentosVinculados();
			for (Iterator<ProcessoDocumento> iterator = vinculados.iterator(); iterator.hasNext();) {
				ProcessoDocumento vinculado = iterator.next();
				String id = String.valueOf(vinculado.getIdProcessoDocumento());
				Boolean carregarBinario = listaIdCarregarBinario.contains(id) || listaIdCarregarBinario.contains(TODOS);
				if (!(carregarBinario || carregarMetadata)) {
					iterator.remove();
				}
			}
		}
	}
 	
 	/**
 	 * Remove os documentos vinculados que não estão presentes na lista 'documentos', pois se o vinculado
 	 * não está presente na lista 'documentos' é porque o usuário não possui permissão para visualizá-lo.
 	 * 
 	 * @param documento
 	 * @param documentos
 	 */
 	private void removerDocumentosVinculadosNaoConsultados(ProcessoDocumento documento, List<ProcessoDocumento> documentos) {
		if (!isNulo(documento) && ProjetoUtil.getTamanho(documento.getDocumentosVinculados()) > 0) {
			Set<ProcessoDocumento> vinculados = documento.getDocumentosVinculados();
			for (Iterator<ProcessoDocumento> iterator = vinculados.iterator(); iterator.hasNext();) {
				ProcessoDocumento vinculado = iterator.next();
				if (!documentos.contains(vinculado)) {
					iterator.remove();
				}
			}
		}
	}
 	
 	/**
	 * Verifica se a quantidade de documentos binários solicitados é permitida pelo sistema.
	 * A quantidade de documentos binários solicitados é definida nos parâmetros do sistema.
	 * 
	 * @param documentos List de Id do documento ou List de ProcessoDocumento.
	 */
	private void validarLimiteQuantidadeDocumentosBinariosConsultadosMNIPermitido(List documentos) {
		if (ProjetoUtil.isNotVazio(documentos)) {
			Integer quantidadePermitida = ParametroUtil.instance().getLimiteQuantidadeDocumentosBinariosConsultadosMNI();
			
			if(quantidadePermitida != null && quantidadePermitida > 0 && (documentos.size() > quantidadePermitida)){
				throw new NegocioException(String.format("Não é permitido consultar mais de [%d] documentos binários.",	quantidadePermitida));
			} 
		}
	}
	
	public boolean isAtendimentoPlantaoPermitido() {
		return PlantaoJudicialService.instance().verificarPlantao();
	}

	/***
	 * Verificar se é permitido protocolar ao plantão judicial.
	 * @param manifestacaoProcessual
	 */
	private void validarAtendimentoPlantao(ManifestacaoProcessual manifestacaoProcessual) {
		boolean isAtendimentoPlantao = isAtendimentoPlantao(manifestacaoProcessual);
		
		if (isAtendimentoPlantao && !isAtendimentoPlantaoPermitido()) {
			throw new NegocioException("Solicitação de atendimento no plantão judiciário fora do horário permitido. ");
		}
	}
	
	/**
	 * Verifica se foi passado o parâmetro de plantão judicial na manifestação processual.
	 * @param manifestacaoProcessual
	 * @return isAtendimentoPlantao (boolean)
	 */
	private boolean isAtendimentoPlantao(ManifestacaoProcessual manifestacaoProcessual) {
		if (manifestacaoProcessual == null) {
			return false;
		}

		return Boolean.parseBoolean(
				MNIParametroUtil.obterValor(manifestacaoProcessual, MNIParametro.PARAM_ATENDIMENTO_PLANTAO));
	}

	/**
	 * Retorna o OJ da manifestação processual.
	 * 
	 * @param manifestacaoProcessual
	 * @return OrgaoJulgador
	 * @throws NumberFormatException
	 * @throws PJeBusinessException
	 */
	private OrgaoJulgador obterOrgaoJulgador(ManifestacaoProcessual manifestacaoProcessual) throws NumberFormatException, PJeBusinessException {
		OrgaoJulgador resultado = null;
		br.jus.cnj.intercomunicacao.v222.beans.OrgaoJulgador oj = manifestacaoProcessual.getDadosBasicos().getOrgaoJulgador();
		if (!isNulo(oj) && StringUtils.isNumeric(oj.getCodigoOrgao())) {
			OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getOrgaoJulgadorManager();
			resultado = orgaoJulgadorManager.findById(Integer.parseInt(oj.getCodigoOrgao()));
		}
		return resultado;
	}
	
	/**
	 * @param manifestacao ManifestacaoProcessual
	 * @param processo ProcessoTrf
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void salvarExtensao(ProcessoTrf processo, ManifestacaoProcessual manifestacao) {
		if (manifestacao != null && ProjetoUtil.isNotVazio(manifestacao.getDocumento())) {
			DocumentoProcessual documento = manifestacao.getDocumento().get(0);
			Object any = documento.getAny();
		}
	}
	
	private void configurarSigiloProcesso(ProcessoTrf processoTrf, CabecalhoProcessual cabecalhoProcessual) {
		processoTrf.setSegredoJustica(isSegredoJustica(cabecalhoProcessual));
		ComponentUtil.getComponent(ProcessoTrfManager.class).aplicarNivelSigilo(processoTrf);
		if (isRemessaEntreInstancias() && (processoTrf.getNivelAcesso() < cabecalhoProcessual.getNivelSigilo())) {
			processoTrf.setNivelAcesso(cabecalhoProcessual.getNivelSigilo());
		}
	}

	private boolean isSegredoJustica(CabecalhoProcessual cabecalhoProcessual) {
		return cabecalhoProcessual.isSetNivelSigilo() ? cabecalhoProcessual.getNivelSigilo() > 0 : false;
	}

	/**
	 * Converte a lista de ProcessoParteExpediente para ComunicacaoProcessual.
	 * 
	 * @param expedientes
	 * @return
	 * @throws PJeBusinessException
	 */
	protected List<ComunicacaoProcessual> converterParaComunicacaoProcessual(List<ProcessoParteExpediente> expedientes) throws PJeBusinessException {
		List<ComunicacaoProcessual> comunicacoes = new ArrayList<>();
		
		ProcessoParteExpedienteParaComunicacaoProcessual conversor = new ProcessoParteExpedienteParaComunicacaoProcessual();
		Pessoa destinatario = Authenticator.getPessoaLogada();
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		ProcessoParteExpedienteManager processoParteExpedienteManager = ComponentUtil.getComponent(ProcessoParteExpedienteManager.class);
		for(ProcessoParteExpediente exp : expedientes){
			ProcessoParteExpediente expediente = processoParteExpedienteManager.findById(exp.getIdProcessoParteExpediente());
			boolean aptoParaCiencia = atoComunicacaoService.aptoParaCiencia(expediente, destinatario);
			boolean aptoParaVisualizacao = atoComunicacaoService.aptoParaVisualizar(expediente, destinatario);
			if(aptoParaCiencia || aptoParaVisualizacao){
				if(!expediente.getFechado() && aptoParaCiencia){
					atoComunicacaoService.registraCienciaPessoal(new Date(), false, Arrays.asList(expediente));	
				}
				comunicacoes.add(conversor.converter(expediente));
			}
		}
		
		return comunicacoes;
	}
	
	private boolean isRespostaDeExpedientePostados(ManifestacaoProcessual parametro) {
		List<Parametro> parametros = parametro.getParametros();
		return MNIParametroUtil.hasParametro(parametros, MNIParametro.getIdRetornoProcessoParteExpedientePostado());
	}

	public void salvarIdentificadorExterno(ManifestacaoProcessual tipoManifestacaoProcessual, Processo processo) throws PJeBusinessException {
		String identificadorSistemaExterno = MNIParametroUtil.obterValor(tipoManifestacaoProcessual, MNIParametro.PARAM_IDENTIFICADOR_EXTERNO);
		Procuradoria procuradoria = Authenticator.getProcuradoriaAtualUsuarioLogado();
		if(processo == null) {
			throw new NegocioException("Ocorreu um erro ao salvar o Identificador Externo do processo, favor tentar novamente.");
		}
		
		ProtocoloExternoMni protocoloExterno = new ProtocoloExternoMni();
		protocoloExterno.setNumeroIdentificadorSistemaExterno(identificadorSistemaExterno);
		protocoloExterno.setIdProcesso(processo.getIdProcesso());
		protocoloExterno.setProcuradoria(procuradoria);
		if (procuradoria == null) {
			protocoloExterno.setUsuario(Authenticator.getUsuarioLogado());
		}
		ProtocoloExternoMniManager managerProtocoSistemaExterno = ComponentUtil.getComponent(ProtocoloExternoMniManager.class);
		managerProtocoSistemaExterno.persist(protocoloExterno);
		EntityUtil.flush();
	}
	
	private boolean validarIdentificadorSistemaExterno(ManifestacaoProcessual tipoManifestacaoProcessual) {
		String identificadorSistemaExterno = MNIParametroUtil.obterValor(tipoManifestacaoProcessual, MNIParametro.PARAM_IDENTIFICADOR_EXTERNO);
		Procuradoria procuradoria = Authenticator.getProcuradoriaAtualUsuarioLogado();
		
		if (!StringUtil.isEmpty(identificadorSistemaExterno)) {
			ProtocoloExternoMniManager managerProtocoSistemaExterno = ComponentUtil
					.getComponent(ProtocoloExternoMniManager.class);

			ProtocoloExternoMni protocoloExterno = null;

			if (procuradoria != null) {
				protocoloExterno = managerProtocoSistemaExterno
						.buscarPorIdentificadorSistemaExternoEhProcuradoria(identificadorSistemaExterno, procuradoria);
			} else {
				protocoloExterno = managerProtocoSistemaExterno
						.buscarPorIdentificadorSistemaExternoEhUsuario(identificadorSistemaExterno, Authenticator.getUsuarioLogado());
			}

			if (protocoloExterno != null) {
			    Processo processo = consultarProcessoPorId(protocoloExterno.getIdProcesso());
			    String numeroProcesso = (processo != null) ? processo.getNumeroProcesso() : "Processo não encontrado";
			    
			    throw new NegocioException(String.format(
			        "Já existe um processo vinculado ao identificador externo. Número do processo: %s. Identificador Externo: %s.",
			        numeroProcesso, identificadorSistemaExterno));
			}			
			return true;
		} else {
			return false;
		}
	}

	private Processo consultarProcessoPorId(Integer idProcesso) {
		if (idProcesso == null) {
			return null;
		}
		try {
			return ComponentUtil.getComponent(ProcessoManager.class).findById(idProcesso);
		} catch (PJeBusinessException e) {
			throw new PJeRuntimeException(e);
		}
	}

	private void configurarProcessoIncidental(ProcessoTrf processoTrf, ManifestacaoProcessual manifestacaoProcessual) throws PJeBusinessException {
		String numeroProcesso = obterNumeroOriginario(manifestacaoProcessual);
		
		if(StringUtils.isBlank(numeroProcesso)){
			throw new NegocioException("O número do processo originário/referência deve ser informado para processos incidentais.");
		}
		
		processoTrf.setIsIncidente(true);
		processoTrf.setDesProcReferencia(tratarMascaraNumeroProcessoReferencia(numeroProcesso, processoTrf.getClasseJudicial()));
		
		ProcessoTrf processoReferencia = null;
		try {
			if(NumeroProcessoUtil.numeroProcessoValido(numeroProcesso)){
				NumeroUnico numeroUnico = new NumeroUnico();
				numeroUnico.setValue(StringUtil.removeNaoNumericos(numeroProcesso));
				processoReferencia = obterProcessoTrf(numeroUnico, false);
			}
		} catch (NegocioException e) {
			log.info(e.getMensagem());
		}

		if (processoReferencia != null) {
			validarOrgaoJulgadorProcessoIncidental(processoTrf.getJurisdicao(), processoTrf.getClasseJudicial(), processoReferencia.getOrgaoJulgador());
			processoTrf.setProcessoReferencia(processoReferencia);
			processoTrf.setProcessoOriginario(processoReferencia);
		} else if (manifestacaoProcessual.getDadosBasicos().getOrgaoJulgador() != null) {
			OrgaoJulgador orgaoJulgador = ComponentUtil.getComponent(OrgaoJulgadorManager.class).findById(
					Integer.parseInt(manifestacaoProcessual.getDadosBasicos().getOrgaoJulgador().getCodigoOrgao()));
			processoTrf.setOrgaoJulgador(orgaoJulgador);
			ProcessoTrfHome.instance().setOrgaoJulgador(orgaoJulgador);
			validarOrgaoJulgadorProcessoIncidental(processoTrf.getJurisdicao(), processoTrf.getClasseJudicial(), orgaoJulgador);
		} else {
			String mensagem = String.format("Processo incidental de número %s não encontrado ou órgão julgador não informado para processos externos ao Pje.", numeroProcesso);
			throw new NegocioException(mensagem);
		}
	}

	private void validarOrgaoJulgadorProcessoIncidental(Jurisdicao jurisdicao, ClasseJudicial classeJudicial,
			OrgaoJulgador orgaoJulgador) {
		if(orgaoJulgador == null){
			throw new NegocioException("É necessário o envio do órgão julgador. Órgão julgador não encontrado!");
		}
		
		boolean isClasseJudicialIncidentalValida = ComponentUtil.getComponent(ClasseJudicialManager.class).isClasseJudicialIncidentalValida(jurisdicao, orgaoJulgador, classeJudicial);
		if(!isClasseJudicialIncidentalValida){
			throw new NegocioException("Correlação entre a classe e o órgão julgador do processo incidental não está válida."); 
		}
	}
	
	private String obterNumeroOriginario(ManifestacaoProcessual manifestacaoProcessual) {
		String numeroProcesso1Grau = MNIParametroUtil.obterValor(manifestacaoProcessual,
				MNIParametro.PARAM_NUM_PROC_1_GRAU);
		String numeroUnicoProcessoOriginario = MNIParametroUtil.obterValor(manifestacaoProcessual,
				MNIParametro.getNumeroUnicoProcessoOriginario());
		if (!StringUtils.isBlank(numeroProcesso1Grau)) {
			return numeroProcesso1Grau;
		}
		if (!StringUtils.isBlank(numeroUnicoProcessoOriginario)) {
			return numeroUnicoProcessoOriginario;
		}
		return null;
	}
	
	private String tratarMascaraNumeroProcessoReferencia(String numeroProcessoReferencia, ClasseJudicial classeJudicial) {
		if (classeJudicial.getHabilitarMascaraProcessoReferencia()) {
			if(NumeroProcessoUtil.numeroProcessoValido(numeroProcessoReferencia)){
				return NumeroProcessoUtil.mascaraNumeroProcesso(NumeroProcessoUtil.retiraMascaraNumeroProcesso(numeroProcessoReferencia));
			}else{

				throw new NegocioException("Número do processo originário ou de referência não está no formato de numeração única do CNJ.");
			}
		} else {
			return numeroProcessoReferencia;
		}
	}
	
	public Boolean getSinalizaProcessoMigrado() {
		return sinalizaProcessoMigrado;
	}

	public void setSinalizaProcessoMigrado(Boolean sinalizaProcessoMigrado) {
		this.sinalizaProcessoMigrado = sinalizaProcessoMigrado;
	}
	
	private void validarProcessoMigracaoBloqueada(ProcessoTrf processoTrf) throws PJeBusinessException {
		if (processoTrf.getInBloqueioMigracao()) {
			throw new NegocioException("Processo bloqueado devido migração.");
		}
	}
	
	private void validarSinalizacaoMigracao(ProcessoTrf processoTrf, ManifestacaoProcessual tipoManifestacaoProcessual,
			Pessoa usuarioMigracao) {
 
		try {
 
			if (Boolean.parseBoolean(parametroService.findByName(Parametros.MIGRACAO_PROCESSO_HABILITA_SINALIZACAO_PROCESSO_MIGRADO).getValorVariavel()) && contemCertidaoMigracao(tipoManifestacaoProcessual)) {
 
				if (validaUsuarioMigracao(usuarioMigracao)) {
					throw new NegocioException(
							"Processo não pode ser migrado, pois usuário não está habilitado para migração.");
				}
 
				setSinalizaProcessoMigrado(Boolean.TRUE);
			}
 
		} catch (NegocioException e) {
			throw e;
		}
	}
	
	private void sinalizaProcessoMigrado(ProcessoTrf processoTrf) throws Exception, PJeBusinessException {
		
		marcarProcessoBoqueado(processoTrf);
		gerarSituacaoMigrado(processoTrf);
		gerarNotificacao(processoTrf);
		movimentarFluxoMigracao(processoTrf);		
		lancarMovimento(processoTrf, CodigoMovimentoNacional.CODIGO_MOVIMENTO_COMUNICACAO_REMESSA);		
		ProcessoJudicialService.instance().incluirNovoFluxo(processoTrf, parametroService.findByName(Parametros.MIGRACAO_PROCESSO_FLUXO_DESTINO).getValorVariavel());
		
	}
	
	public Boolean validaUsuarioMigracao(Pessoa usuarioMigracao) {
		
		Integer idUsuario = Integer.valueOf(parametroService.findByName(Parametros.MIGRACAO_PROCESSO_USUARIO_SINALIZACAO_MNI).getValorVariavel());
		
		return !idUsuario.equals(usuarioMigracao.getEntityIdObject());
	}
	
	public Boolean contemCertidaoMigracao(ManifestacaoProcessual tipoManifestacaoProcessual) {
		
		String valorParametro = parametroService.findByName(Parametros.MIGRACAO_PROCESSO_CD_CERTIDAO_MIGRACAO).getValorVariavel();
		
		return  tipoManifestacaoProcessual.getDocumento().stream()
                .anyMatch(tipo -> tipo.getTipoDocumento().equals(valorParametro));
	}
	
	private void marcarProcessoBoqueado(ProcessoTrf processoTrf) throws Exception  {

		processoTrf.setInBloqueiaPeticao(Boolean.TRUE);
		processoTrf.setInBloqueioMigracao(Boolean.TRUE);
		EntityUtil.getEntityManager().merge(processoTrf);
		EntityUtil.flush();

	}
	
	private void gerarNotificacao(ProcessoTrf processoTrf) throws PJeBusinessException, Exception {		
		
		ProcessoJudicialService.instance().incluirNovoFluxo(processoTrf, parametroService.findByName(Parametros.MIGRACAO_PROCESSO_FLUXO_NOTIFICAO).getValorVariavel());
	}
	
	private void movimentarFluxoMigracao(ProcessoTrf processoTrf) throws PJeBusinessException, Exception {

		// finaliza tarefas abertas
		ProcessoInstanceManager processoInstanceManager = (ProcessoInstanceManager) Component.getInstance(ProcessoInstanceManager.class);
		List<ProcessoInstance> instances = processoInstanceManager.recuperaAtivas(processoTrf);

		if (!instances.isEmpty()) {
			instances.stream().forEach(obj -> {
				ComponentUtil.getComponent(FluxoService.class).finalizaFluxoManualmente(obj.getIdProcessoInstance());
			});
		}
	}
	
	private void lancarMovimento(ProcessoTrf processoTrf, String cdEvento) {

		MovimentoAutomaticoService.preencherMovimento().deCodigo(123).associarAoProcesso(processoTrf)
		.comComplementoDeCodigo(18)
		.doTipoDominio()
		.preencherComElementoDeCodigo(380)
		.comComplementoDeCodigo(7)
		.doTipoDinamico()
		.preencherComObjeto(DESCRICAO_DESTINO).lancarMovimento();
	}
	
	private void gerarSituacaoMigrado(ProcessoTrf processoTrf) throws PJeBusinessException {
		
		finalizarSituacoesAnteriores(processoTrf);
		
		TipoSituacaoProcessual tipoSituacaoProcessual = tipoSituacaoProcessualManager.findByCodigo(CODIGO_ANDAMENTO_MIGRACAO);
		
		if(tipoSituacaoProcessual == null){
			throw new IllegalArgumentException("Não há tipo de situação com o código " + CODIGO_ANDAMENTO_MIGRACAO);
		}
		
		SituacaoProcessual s = new SituacaoProcessual();
		s = situacaoProcessualManager.criarSituacao(processoTrf, tipoSituacaoProcessual);		
		situacaoProcessualManager.persist(s);
	}

	private void finalizarSituacoesAnteriores(ProcessoTrf processoTrf) throws PJeBusinessException {

		List<SituacaoProcessual> situacoes = situacaoProcessualManager.recuperaSituacoes(processoTrf);

		if (situacoes == null || situacoes.isEmpty()) {
			return;
		}

		situacoes.stream().filter(situacao -> situacao.getDataFinal() == null).forEach(situacao -> {
			situacao.setDateFinal(new Date());
			try {
				situacaoProcessualManager.merge(situacao);
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		});

		situacaoProcessualManager.flush();
	}

	private void atualizarProcessoDcp(ProcessoTrf processoPje, br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual manifestacaoProcessual) throws Exception {

		if (processoPje == null || manifestacaoProcessual == null || manifestacaoProcessual.getDadosBasicos() == null || manifestacaoProcessual.getDadosBasicos().getProcessoVinculado() == null) {
			return;
		}

		if (manifestacaoProcessual.getDadosBasicos().getProcessoVinculado().isEmpty()) {
			return;
		}

		ProcessoTrf processoDCP = geraProcessoDCP(manifestacaoProcessual);

		if (processoDCP == null) {
			return;
		}

		atualizarDadosProcessoDCP(processoPje, processoDCP);

		ProcessoAssuntoManager processoAssuntoManager = ComponentUtil.getComponent(ProcessoAssuntoManager.class);

		for (ProcessoAssunto assunto : processoPje.getProcessoAssuntoList()) {
			assunto.setProcessoTrf(processoPje);
			processoAssuntoManager.persist(assunto);
		}

		EntityUtil.getEntityManager().merge(processoPje);
		EntityUtil.flush();
	}

	private ProcessoTrf geraProcessoDCP(br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual manifestacaoProcessual) throws PJeBusinessException {
		if (this.processoDCP != null) {
			return this.processoDCP;
		}
		if (manifestacaoProcessual == null || manifestacaoProcessual.getDadosBasicos() == null || manifestacaoProcessual.getDadosBasicos().getProcessoVinculado() == null) {
			return null;
		}
		List<VinculacaoProcessual> processoVinculado = manifestacaoProcessual.getDadosBasicos().getProcessoVinculado();

		for (VinculacaoProcessual vinc : processoVinculado) {
			try {
				String numeroProcesso = NumeroProcessoUtil.mascaraNumeroProcesso(vinc.getNumeroProcesso().getValue());
				ProcessoTrf processoDCP = ProcessoService.instance().buscaDCP(numeroProcesso);

				if (processoDCP != null && processoDCP.getCompetencia().getIdCompetencia() == 2) {
					br.jus.cnj.intercomunicacao.v222.beans.OrgaoJulgador oj = new br.jus.cnj.intercomunicacao.v222.beans.OrgaoJulgador();
					oj.setCodigoOrgao(processoDCP.getOrgaoJulgador().getIdOrgaoJulgador() + "");
					manifestacaoProcessual.getDadosBasicos().setOrgaoJulgador(oj);
					this.processoDCP = processoDCP;
					return processoDCP;
				} else {
					return null;
				}
			} catch (AplicationException e) {
				throw new PJeBusinessException(e.getMessage());
			} catch (Exception e) {
				log.error("Error ao buscar DCP processo: " + e.getMessage());
			}
		}
		return null;
	}

	private void validarProcessoDCP(br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual manifestacaoProcessual) throws PJeBusinessException {
		if (manifestacaoProcessual == null || manifestacaoProcessual.getDadosBasicos() == null || manifestacaoProcessual.getDadosBasicos().getProcessoVinculado() == null) {
			return;
		}
		ProcessoTrf processoTrf = geraProcessoDCP(manifestacaoProcessual);
		if (processoTrf == null || processoTrf.getClasseJudicial() == null) {
			return;
		}

		String codClasseJudicial = ParametroUtil.getParametro("codClasseJudicialIncidentaisDcp");

		Set<String> codigos = Arrays.stream(codClasseJudicial.split(","))
				.map(String::trim)
				.collect(Collectors.toSet());

		String classeProcessual = String.valueOf(manifestacaoProcessual.getDadosBasicos().getClasseProcessual());
		if (!codigos.contains(classeProcessual)) {
			throw new NegocioException("O processo Inforamdo foi encontrado no DCP, e necessario infomar classe judicial correta: " + codClasseJudicial);
		}
	}

	private void atualizarDadosProcessoDCP(ProcessoTrf target, ProcessoTrf source) {
		if (source == null) {
			return;
		}
		target.setIsIncidente(true);
		target.setDesProcReferencia(source.getDesProcReferencia());

		if (source.getOrgaoJulgador() != null) {
			target.setOrgaoJulgador(source.getOrgaoJulgador());
		}

		if (source.getOrgaoJulgadorCargo() != null) {
			target.setOrgaoJulgadorCargo(source.getOrgaoJulgadorCargo());
		}

		if (source.getCompetencia() != null) {
			target.setCompetencia(source.getCompetencia());
			target.getCompetencia().setIndicacaoOrgaoJulgadorObrigatoria(false);
		}

		if (source.getJurisdicao() != null) {
			target.setJurisdicao(source.getJurisdicao());
		}

		if (source.getIdAreaDireito() != null) {
			target.setIdAreaDireito(source.getIdAreaDireito());
		}
	}
}
