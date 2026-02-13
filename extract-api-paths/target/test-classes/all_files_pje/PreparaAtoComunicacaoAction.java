package br.jus.cnj.pje.view.fluxo;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import br.com.itx.util.EntityUtil;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.compass.core.util.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Strings;
import org.jbpm.JbpmException;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.richfaces.component.UITree;
import org.richfaces.component.state.TreeState;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.actions.anexarDocumentos.AnexarDocumentos;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.component.FileHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.entidades.vo.DestinatarioComunicacao;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.EnderecoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoVisibilidadeSegredoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteCaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.CepService;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.cnj.pje.nucleo.service.EnderecoService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualImpl;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.DocumentoJudicialDataModel;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoVisibilidadeSegredo;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.PJeEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.TipoCalculoMeioComunicacaoEnum;
import br.jus.pje.nucleo.enums.TipoNomePessoaEnum;
import br.jus.pje.nucleo.enums.TipoOrigemAcaoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Objeto de trabalho de nó de fluxo destinado à produção de atos de comunicação.
 * 
 * @author Paulo Cristovão Filho
 * 
 */
@Name("preparaAtoComunicacaoAction")
public class PreparaAtoComunicacaoAction extends BaseAction<ProcessoExpediente> implements Serializable,ArquivoAssinadoUploader{

	private static final long serialVersionUID = 3829476863431908814L;

	/**
	 * Mapa responsável em manter um cache da lista de ExpedicaoExpedienteEnum para a chave pessoa/pessoal/procuradoria.
	 * O cache evita chamadas desnecessárias à base de dados e validação no Domicílio Bancário de uma pessoa.
	 */
	@In (required = false, scope = ScopeType.CONVERSATION)
	@Out (required = false, scope = ScopeType.CONVERSATION)
	private Map<String, List<ExpedicaoExpedienteEnum>> mapaCachePessoaMeioComunicacao = new HashMap<>();
	

	/**
	 * Enum utilitário destinado a permitir a identificação do tipo de documento utilizado como instrumento da comunicação.
	 * 
	 * @author Paulo Cristovão Filho
	 * 
	 */
	public enum InstrumentoComunicacao implements PJeEnum {

		DP("Documento do processo"),
		DN("Documento novo");

		private String label;

		InstrumentoComunicacao(String label){
			this.label = label;
		}

		/**
		 * @return the label
		 */
		@Override
		public String getLabel(){
			return label;
		}

	}
	
	public class ParAssinatura implements Serializable{
		
		private static final long serialVersionUID = 1L;
		private String conteudo;
		private String assinatura;
		private Boolean binario = false;
		private String md5Documento;
		
		public String getConteudo() {
			return conteudo;
		}
		public void setConteudo(String conteudo) {
			this.conteudo = conteudo;
		}
		public String getAssinatura() {
			return assinatura;
		}
		public void setAssinatura(String assinatura) {
			this.assinatura = assinatura;
		}
		
		public Boolean isBinario() {
			return binario;
		}
		public void setBinario(Boolean binario) {
			this.binario = binario;
		}
		
		public String getMd5Documento() {
			return md5Documento;
		}
		public void setMd5Documento(String md5Documento) {
			this.md5Documento = md5Documento;
		}
		
	}
	
	public class ParAnexo implements Serializable{
		
		private static final long serialVersionUID = 1L;
		private ProcessoDocumento pdPdf;
		private byte[] conteudo;
		public ProcessoDocumento getPdPdf() {
			return pdPdf;
		}
		public void setPdPdf(ProcessoDocumento pdPdf) {
			this.pdPdf = pdPdf;
		}
		public byte[] getConteudo() {
			return conteudo;
		}
		
		public void setConteudo(byte[] conteudo) {
			this.conteudo = conteudo;
		}
	}

	public static final String VAR_MAPASEXP = "prepararAto:mapaExpedientes";
	public static final String VAR_MAPAINSTR = "prepararAto:mapaInstrumento";
	public static final String VAR_MAPAINTPESS = "prepararAto:mapaIntimacaoPessoal";
	public static final String VAR_MAPAAGRUP = "prepararAto:mapaAgrupamentos";
	public static final String VAR_MEIOSCOMUNICACOES = "prepararAto:meiosComunicacoes";
	public static final String VAR_MAPA_FILTROS_MEIOSCOMUNICACOES = "prepararAto:mapaFiltrosMeiosComunicacoes";
	public static final String VAR_UM_EXPED_POR_ENDERECO = "prepararAto:mapaUmExpedientePorEndereco";
	public static final String VAR_MAPA_PROCURADORIAS = "prepararAto:mapaProcuradorias";
	public static final String VAR_MAPA_PROCURADORIA_SELECIONADA = "prepararAto:mapaProcuradoriaSelecionada";
	
	private int numeroDocumentosVinculados = 0;
	
	@Logger
	private Log logger;

	
	public TaskInstanceHome getTaskInstanceHome(){
		return TaskInstanceHome.instance();
	}

	private Boolean documentoEditavel = true;
	private Boolean incluirComoEnderecoProcessual = false;
	private Boolean modalRepresentacaoAberta = false;
	private boolean exibirPartesInativas = false;
	private DocumentoJudicialDataModel dataModel;
	private List<DocumentoJudicialDataModel> dataModelVinculaveis = new ArrayList<DocumentoJudicialDataModel>();
	private Endereco enderecoNovo;
	private Integer passo;
	private ModeloDocumento modelo;
	private Map<DestinatarioComunicacao, DestinatarioComunicacao> mapaAgrupadoAgrupador;
	private Map<DestinatarioComunicacao, String> destinatarioAgrupadoIdAgrupador;
	private DestinatarioComunicacao destinatarioSelecionado;
	private ProcessoExpediente processoExpediente;
	private ProcessoTrf processoJudicial;
	private String encodedCertChain;
	private String transicaoSaida;
	
	private List<TipoProcessoDocumento> tiposDocumentosDisponiveis;
	private Map<ProcessoDocumento, Boolean> documentosSelecionados;
	private Map<DestinatarioComunicacao, Set<Entry<DestinatarioComunicacao,ProcessoExpediente>>> agrupamentos;
	private Map<Pessoa, Set<Endereco>> mapaEnderecos;
	private Map<Pessoa, List<Integer>> cacheEnderecosPossiveis = new HashMap<>(0);
	private Map<Pessoa, Map<Integer, String>> cacheEnderecosPossiveisDescricao = new HashMap<>(0);
	private Map<Endereco, Boolean> enderecosSelecionados;
	private Map<DestinatarioComunicacao, ProcessoExpediente> destinatarios;
	private Map<Pessoa, Boolean> mapaIntimacaoPessoal;
	private Map<Pessoa, List<ProcessoParteRepresentante>> mapaAdvogados;
	private Map<DestinatarioComunicacao, InstrumentoComunicacao> mapaInstrumento;
	private Map<Pessoa, List<Procuradoria>> mapaProcuradorias;
	private Map<Pessoa, Procuradoria> mapaProcuradoriaSelecionada;
	private ArrayList<ParAssinatura> assinaturas;
	private Map<Pessoa, Boolean> mapaUmEnderecoPorExpediente = new HashMap<Pessoa, Boolean>(0);	
	
	private AnexarDocumentos anexarDocumentos;
	private String vinculacaoOutroDocumento;
	private List<ParAnexo> anexosPdf = new ArrayList<ParAnexo>();
	
	// variáveis para controle do modal de seleção de procuradoria
	private List<Procuradoria> listaComboProcuradorias;
	private Pessoa pessoaProcuradoriaSelecionada;
	private Procuradoria procuradoriaSelecionada;

	@In( value="partesTree",required=false, create=true)
	@Out(value="partesTree",required=false)
	private UITree richTree;
	
	private String mensagemIndisponibilidade;
	private String mensagemPossivelAtrasoEnvioDomicilio;

	private String cepFiltro;
	private String enderecoCompletoFiltro;
	private Map<Integer, String> enderecoIdDescricao;

	private static final String DESCRICAO_PARTE = "(Parte)";
	private static final String DESCRICAO_ADVOGADO = "(Advogado)";
	private static final String DESCRICAO_ORGAO_DE_REPRESENTACAO = "(Órgão de representação)";

	/**
	 * Inicializa o componente Seam, recuperando, se existente, as variáveis "prepararAto:minutaAtoComunicacao" (instância de processo) e
	 * "frameDefaultLeavingTransition" (instância de tarefa), a identificação dos tipos de documentos disponíveis para a tarefa, conforme
	 *  {@link DocumentoJudicialService#getTiposDisponiveis()}.
	 * 
	 * Também são inicializados os dados de anterior execução do nó, obtidos a partir das variáveis "prepararAto:mapaIntimacaoPessoal" (instância de
	 * tarefa) e "prepararAto:mapaIntimacaoPessoal" (instância de tarefa)
	 * 
	 * 
	 */
	@Create
	public void init() {
		try{
			runInit();
		} catch (Exception e){ // erro ao recuperar variáveis, ex: serialVersionUID foi mudado

			limpaAtosComunicacaoConsolidados();

			try{
				// tentar rodar novamente
				runInit();
			} catch (JbpmException e2){
				// subir excecao se não conseguiu se recuperar
				throw e2;
			}
		}
	}
	
	public void runInit(){
		processoJudicial = loadProcessoJudicial();
		documentosSelecionados = new HashMap<ProcessoDocumento, Boolean>();
		enderecosSelecionados = new HashMap<Endereco, Boolean>();
		mapaAdvogados = new HashMap<Pessoa, List<ProcessoParteRepresentante>>();
		// Recarrega procuradorias já carregadas em execução anterior do PAC
		mapaProcuradorias = recuperaDadosProcuradoria();
		mapaProcuradoriaSelecionada = recuperaDadosProcuradoriaSelecionada();
		destinatarios = recuperaDestinatarios();
		mapaIntimacaoPessoal = recuperaDadosIntimacoesPessoais();
		mapaEnderecos = recuperaEnderecos();
		mapaInstrumento = recuperaInstrumentos();
		mapaUmEnderecoPorExpediente = recuperaUmExpedientePorEndereco();
		passo = 0; //Escolha dos destinatários
		carregarMapaAgrupamentos();
		transicaoSaida = (String)TaskInstanceUtil.instance().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		DocumentoJudicialService documentoJudicialService = (DocumentoJudicialService) Component.getInstance(DocumentoJudicialService.class);
		dataModel = new DocumentoJudicialDataModel();
		dataModel.setProcessoJudicial(this.processoJudicial);
		dataModel.setDocumentoJudicialService(documentoJudicialService);
		dataModel.setOrdemDecrescente(true);
		dataModel.setMostrarPdf(true);
		dataModel.setIncluirComAssinaturaInvalidada(false);
		dataModel.setSoDocumentosJuntados(true);
		dataModel.setTipoOrigemAcao(TipoOrigemAcaoEnum.I);
		
		DocumentoJudicialDataModel docJudicialDataModel = new DocumentoJudicialDataModel();
		docJudicialDataModel.setProcessoJudicial(this.processoJudicial);
		docJudicialDataModel.setDocumentoJudicialService(documentoJudicialService);
		docJudicialDataModel.setOrdemDecrescente(true);
		docJudicialDataModel.setMostrarPdf(true);
		docJudicialDataModel.setIncluirComAssinaturaInvalidada(false);
		docJudicialDataModel.setSoDocumentosJuntados(true);
		
		getDataModelVinculaveis().add(docJudicialDataModel);
		
		// obtém a lista de representantes para cada parte
		for (ProcessoParte processoParte : processoJudicial.getProcessoParteList()) {
					
			adicionarAdvogadosAoMapa(processoParte.getPessoa(),
					processoParte.getProcessoParteRepresentanteList());
			
			adicionarProcuradoriaAoMapa(processoParte.getPessoa(), processoParte.getProcuradoria());
			
			/*
			 * Carrega o mapa de pessoas para as quais serão enviadas intimações
			 * pessoais, que são as que não possuem representantes.
			 * 
			 * Se a parte não foi adicionada ao mapa de intimação pessoal ou se
			 * foi adicionada como sendo passível de intimação pessoal, faz as
			 * verificações necessárias.
			 */
			if ((!mapaIntimacaoPessoal.containsKey(processoParte.getPessoa()))
					|| (mapaIntimacaoPessoal.get(processoParte.getPessoa()))) {
				
				if (!pessoaPossuiAdvogadoNoMapa(processoParte.getPessoa())	
						&& !pessoaPossuiProcuradoriaNoMapa(processoParte.getPessoa())) {
					
					mapaIntimacaoPessoal.put(processoParte.getPessoa(), Boolean.TRUE);
					
				} else {
					mapaIntimacaoPessoal.put(processoParte.getPessoa(), Boolean.FALSE);
					
				}
			}
		}
		
		anexarDocumentos = AnexarDocumentos.instance();
		anexarDocumentos.newInstance();
	}
	
	/**
	 * Método responsável por verificar se algum advogado já foi
	 * adicionado ao mapa para a pessoa especificada no parâmetro
	 */
	private boolean pessoaPossuiAdvogadoNoMapa(Pessoa pessoa) {
		if (pessoa != null && mapaAdvogados != null) {
			List<ProcessoParteRepresentante> advogados = mapaAdvogados.get(pessoa);
			if (advogados != null && !advogados.isEmpty()) {
				return true;
			}			
		}
		return false;
	}
	
	/**
	 * Método responsável por verificar se alguma procuradoria já foi
	 * adicionada ao mapa para a pessoa especificada no parâmetro
	 */
	private boolean pessoaPossuiProcuradoriaNoMapa(Pessoa pessoa) {
		if (pessoa != null && mapaProcuradorias != null) {
			 List<Procuradoria> procuradorias = mapaProcuradorias.get(pessoa);
			if (procuradorias != null && !procuradorias.isEmpty()) {
				return true;
			}			
		}
		return false;
	}
	
	
	/**
	 * [PJEII-19914] Recupera o mapa de procuradorias dos destinatarios selecionados em execução anterior da tarefa.
	 * 
	 * @return Mapa contendo as pessoas selecionadas anteriomente e as suas respectivas lista de procuradorias.
	 */
	private Map<Pessoa, List<Procuradoria>> recuperaDadosProcuradoria(){
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();

		if (taskInstance != null){
			@SuppressWarnings("unchecked")
			Map<Pessoa, List<Procuradoria>> aux = (HashMap<Pessoa, List<Procuradoria>>) taskInstance.getVariable(VAR_MAPA_PROCURADORIAS + taskInstance.getId());
			if (aux != null){

				Map<Pessoa, List<Procuradoria>> ret = new HashMap<Pessoa, List<Procuradoria>>(aux.size());
				PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
				for (Pessoa p : aux.keySet()){
					try{
						Pessoa m = pessoaService.findById(p.getIdUsuario());
						ret.put(m, aux.get(p));
					} catch (PJeDAOException e){
						logger.error("Erro ao tentar recuperar os dados relativos à pessoa [{0}].", p.getNome());
					} catch (PJeBusinessException e) {
						logger.error("Erro ao tentar recuperar os dados relativos à pessoa [{0}].", p.getNome());
					}
				}
				return ret;
			}
		}
		return new HashMap<Pessoa, List<Procuradoria>>(0);
	}
	
	/**
	 * Recupera um {@link Map} com as informações da pessoa para quem se está enviando o expediente 
	 * e da procuradoria que a representa selecionadas em execução anterior da tarefa.
	 * 
	 * @return {@link Map} com as informações da pessoa para quem se está enviando o expediente 
	 * e da procuradoria que a representa selecionadas em execução anterior da tarefa.
	 */
	private Map<Pessoa, Procuradoria> recuperaDadosProcuradoriaSelecionada(){
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();

		if (taskInstance != null){
			@SuppressWarnings("unchecked")
			Map<Pessoa, Procuradoria> aux = (HashMap<Pessoa, Procuradoria>) taskInstance.getVariable(VAR_MAPA_PROCURADORIA_SELECIONADA + taskInstance.getId());
			if (aux != null){

				Map<Pessoa, Procuradoria> ret = new HashMap<Pessoa, Procuradoria>(aux.size());
				PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
				for (Pessoa p : aux.keySet()){
					try{
						Pessoa m = pessoaService.findById(p.getIdUsuario());
						ret.put(m, aux.get(p));
					} catch (PJeDAOException e){
						logger.error("Erro ao tentar recuperar os dados relativos à pessoa [{0}].", p.getNome());
					} catch (PJeBusinessException e) {
						logger.error("Erro ao tentar recuperar os dados relativos à pessoa [{0}].", p.getNome());
					}
				}
				return ret;
			}
		}
		return new HashMap<Pessoa, Procuradoria>(0);
	}
	
	/**
	 * Recupera mapa de destinatario x expedientes, se houver, decorrente de execução anterior do nó de fluxo.
	 * 
	 * @return Mapa contendo os destinatários e seus respectivos expedientes, já gerenciados pelo EntityManager caso sejam entidades já gravadas.
	 * 
	 */
	@SuppressWarnings("unchecked")
	private Map<DestinatarioComunicacao, ProcessoExpediente> recuperaDestinatarios(){
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();
		if (taskInstance != null){
			Object o = taskInstance.getVariable(VAR_MAPASEXP + taskInstance.getId());
			if (o != null){
				HashMap<DestinatarioComunicacao, ProcessoExpediente> auxDest;
				try {
					auxDest = (HashMap<DestinatarioComunicacao, ProcessoExpediente>) o;
				}
				catch(Exception e) {
					taskInstance.deleteVariable(VAR_MAPASEXP);
					return new HashMap<DestinatarioComunicacao, ProcessoExpediente>(0);
				}
				
				HashMap<DestinatarioComunicacao, ProcessoExpediente> ret = new HashMap<DestinatarioComunicacao, ProcessoExpediente>(auxDest.size());
				PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
				for (DestinatarioComunicacao d : auxDest.keySet()){
					try {
						d.setPessoa(pessoaService.findById(d.getPessoa().getIdUsuario()));
				 	} catch (PJeBusinessException e) {
				 		logger.error("Erro ao tentar recuperar a dados relativos à pessoa [{0}].", d.getPessoa().getNome());
				 	}
				 	ProcessoExpediente pe = auxDest.get(d);

					// Necessario pois no caso de variaveis antigas o valor retornado era nulo
					for (ProcessoParteExpediente ppe : pe.getProcessoParteExpedienteList()) {
						ppe.setCancelado(ppe.getCancelado() == null ? Boolean.FALSE : ppe.getCancelado());
						ppe.setEnviadoCancelamento(
								ppe.getEnviadoCancelamento() == null ? Boolean.FALSE : ppe.getEnviadoCancelamento());
					}

				 	ret.put(d, pe);
				 }
				 return ret;
			}
	 	}
	 	return new HashMap<DestinatarioComunicacao, ProcessoExpediente>(0);
	}
		
	/**
	 * Recupera o mapa indicativo de situações de intimações pessoais de algum destinatário.
	 * 
	 * @return Mapa contendo as pessoas intimadas e booleano indicando se suas intimações são pessoais.
	 */
	private Map<Pessoa, Boolean> recuperaDadosIntimacoesPessoais(){
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();

		if (taskInstance != null){
			@SuppressWarnings("unchecked")
			Map<Pessoa, Boolean> aux = (HashMap<Pessoa, Boolean>) taskInstance.getVariable(VAR_MAPAINTPESS + taskInstance.getId());
			if (aux != null){

				Map<Pessoa, Boolean> ret = new HashMap<Pessoa, Boolean>(aux.size());
				PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
				for (Pessoa p : aux.keySet()){
					try{
						Pessoa m = pessoaService.findById(p.getIdUsuario());
						ret.put(m, aux.get(p));
					} catch (PJeDAOException e){
						logger.error("Erro ao tentar recuperar a dados relativos à pessoa [{0}].", p.getNome());
					} catch (PJeBusinessException e) {
						logger.error("Erro ao tentar recuperar a dados relativos à pessoa [{0}].", p.getNome());
					}
				}
				return ret;
			}
		}
		return new HashMap<Pessoa, Boolean>(0);
	}
	
	/**
	 * Verifica se o CNPJ da Procuradoria foi cadastrada no Domicílio Eletrônico Nacional
	 * 
	 * @param pessoa Pessoa.
	 * @return boolean.
	 */
	public boolean recuperarProcuradoriaHabilitada(Pessoa pessoa) {
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();
		Map<Pessoa, Procuradoria> aux = (HashMap<Pessoa, Procuradoria>) taskInstance.getVariable(VAR_MAPA_PROCURADORIA_SELECIONADA + taskInstance.getId());
		ProcuradoriaManager procuradoriaManager = ComponentUtil.getComponent(ProcuradoriaManager.class);
		Procuradoria procuradoriaAux = null;
		String cnpj = null;
		
	    try {
	    	if(aux != null) {
				if (EntityUtil.getEntityManager().contains(aux.get(pessoa))) {
					cnpj = (null != aux.get(pessoa).getPessoaJuridica() ? aux.get(pessoa).getPessoaJuridica().getNumeroCNPJ() : null);
				} else if (aux.get(pessoa) != null) {
					procuradoriaAux = procuradoriaManager.findById(aux.get(pessoa).getIdProcuradoria());
				    cnpj = (null != procuradoriaAux.getPessoaJuridica() ? procuradoriaAux.getPessoaJuridica().getNumeroCNPJ() : null);
				}
	    	}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	   
	    return habilitaDomicilio(cnpj);
	}
	
	/**
	 * Informa se há destinatário selecionado na tela de escolha de destinatários.
	 * 
	 * @return verdadeiro se há algum destinatário selecionado.
	 */
	public boolean haDestinatarios(){
		if (destinatarios != null){
			if(destinatarios.size() == 0){
				return false;
			}
		} else if (destinatarios == null){
			return false;
		}
		return true;
	}
	
	/**
	 * Recupera todos os expedientes destinados à assinatura
	 * 
	 * @return os expedientes destinados à assinatura.
	 */
	private List<ProcessoExpediente> obterExpedientes() {
		List<ProcessoExpediente> expedientes = new ArrayList<ProcessoExpediente>();
		for (Entry<DestinatarioComunicacao, ProcessoExpediente> entry : this.destinatarios.entrySet()) {
			/*
			 * Pega somente os expedientes que precisam ser assinados.
			 * Se for um expediente por endereço, ignora os expedientes com endereço nulo. 
			 * Se for um expediente para todos os endereços, ignora os com endereço
			 */
			if (isDestinatarioUtilizado(entry.getKey())){ continue; }

			// O usuário pode ter agrupado destinatários, que nesse caso utilizam o mesmo ProcessoExpediente.  
			if (!isDestinatarioAgrupado(entry.getKey())) {
				expedientes.add(entry.getValue());
			}
		}
		return expedientes;		
	}

	/**
	 * Finaliza os múltiplos expedientes pendentes, acrescentando as assinaturas necessárias. Se bem sucedido, será invocada a transição padrão
	 * existente definida na variável "frameDefaultLeavingTransition", ao tempo que as variáveis de fluxo serão apagadas.
	 */
	public void finalizarMultiplos(){
		List<ProcessoExpediente> expedientes = obterExpedientes();

		List<String> signs = new ArrayList<String>(expedientes.size());
		for(int i = 0; i < expedientes.size(); i++){
			try {
				for (ParAssinatura par : assinaturas) {
					if(par.isBinario()) {
						if(par.getMd5Documento().equals(expedientes.get(i).getProcessoDocumento().getProcessoDocumentoBin().getMd5Documento())) {
							signs.add(par.assinatura);
							break;
						}
					}
					else {
						String doc = new String(SigningUtilities.base64Decode(par.getConteudo()));
						if (doc.equals(expedientes.get(i).getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento())) {
							signs.add(par.assinatura);
							break;
						}
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try{
			TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();
			AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
			List<ProcessoExpediente> expedientesGerados = atoComunicacaoService.finalizarAtosComunicacao(
				expedientes.toArray(new ProcessoExpediente[]{}), signs.toArray(new String[]{}), 
				encodedCertChain, processoJudicial, taskInstance.getId(), anexosPdf);
			ProcessoParteExpedienteCaixaAdvogadoProcuradorManager processoParteExpedienteCaixaAdvogadoProcuradorManager = ComponentUtil.getComponent(ProcessoParteExpedienteCaixaAdvogadoProcuradorManager.class);
			processoParteExpedienteCaixaAdvogadoProcuradorManager.verificarVinculoCaixa(expedientesGerados);
			
			verificarVisibilidadeDoc(expedientesGerados);
			
			StringBuilder sb = new StringBuilder();
			for (ProcessoExpediente processoExpediente : expedientesGerados){
				sb.append(processoExpediente.getIdProcessoExpediente() + ",");
			}
			sb.deleteCharAt(sb.length() - 1);

			if (StringUtil.isSet(transicaoSaida) && getTaskInstanceHome() != null){
				limpaAtosComunicacaoConsolidados();
				taskInstance.getProcessInstance().getContextInstance().setVariable(ComunicacaoProcessualAction.VARIAVEL_EXPEDIENTE, sb.toString());
				getTaskInstanceHome().end(transicaoSaida);

			} else{
				facesMessages.add(Severity.WARN, "ATENÇÃO: Não foi definida uma transição de saída para este nó.");
			}
		} catch (PJeBusinessException e){
			facesMessages.addFromResourceBundle(Severity.ERROR, e.getCode(), e.getParams());
			logger.error("Erro de negócio ao finalizar os documentos. {0}", e);
		} catch (Exception ex){
			ex.printStackTrace();
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar gravar. {0}", ex.getLocalizedMessage());
		}
	}

	/**
	 * Ao agrupar documentos sigilosos ao processo é necessário liberar a visibilidade daquele documento
	 * para ser acessado pelos Advogados e Procuradores.
	 * @param expedientesGerados
	 */
	private void verificarVisibilidadeDoc(List<ProcessoExpediente> expedientesGerados) {
		List<ProcessoDocumentoVisibilidadeSegredo> visibilidadesAgrupados = new ArrayList<ProcessoDocumentoVisibilidadeSegredo>();
		List<ProcessoDocumentoVisibilidadeSegredo> visibilidadesAgrupadosAdvs = new ArrayList<ProcessoDocumentoVisibilidadeSegredo>();

		for(ProcessoExpediente procExp : expedientesGerados){
			for(ProcessoParteExpediente procParExp : procExp.getProcessoParteExpedienteList()){
				if(procParExp != null){
					acionarVisibilidades(visibilidadesAgrupados, visibilidadesAgrupadosAdvs, procParExp);
				}
			}
		}
		
		gravarVisibilidade(visibilidadesAgrupados);
	}

	private void acionarVisibilidades(List<ProcessoDocumentoVisibilidadeSegredo> visibilidadesAgrupados, 
					List<ProcessoDocumentoVisibilidadeSegredo> visibilidadesAgrupadosAdvs, ProcessoParteExpediente procParExp) {
		
		if(isDocumentoSigiloso(procParExp.getProcessoDocumento())){
				ProcessoDocumentoVisibilidadeSegredo visibilidade = getVisibilidade(procParExp.getProcessoDocumento(),procParExp.getPessoaParte());
				visibilidadesAgrupados.add(visibilidade);
				
				if(!mapaIntimacaoPessoal.get(procParExp.getPessoaParte())){
					visibilidadesAgrupadosAdvs.addAll(getVisibilidadesAdvs(procParExp));
					visibilidadesAgrupados.addAll(visibilidadesAgrupadosAdvs);
				}
			}
	}

	private boolean isDocumentoSigiloso(ProcessoDocumento procDoc) {
		return procDoc.getDocumentoSigiloso();
	}

	private List<ProcessoDocumentoVisibilidadeSegredo> getVisibilidadesAdvs(ProcessoParteExpediente procParExp) {
		List<ProcessoParteRepresentante> advogados = mapaAdvogados.get(procParExp.getPessoaParte());
		List<ProcessoDocumentoVisibilidadeSegredo> visibilidadesDocsAdv = new ArrayList<ProcessoDocumentoVisibilidadeSegredo>();
		ProcessoDocumentoVisibilidadeSegredo visibilidadeAdv = new ProcessoDocumentoVisibilidadeSegredo();
		
		if(advogados != null){
			for(ProcessoParteRepresentante adv : advogados){
				if(isDocumentoSigiloso(procParExp.getProcessoDocumento())){
					visibilidadeAdv = getVisibilidade(procParExp.getProcessoDocumento(), adv.getRepresentante());
					visibilidadesDocsAdv.add(visibilidadeAdv);
				}
			}
		}
		
		return visibilidadesDocsAdv;
	}
	
	private ProcessoDocumentoVisibilidadeSegredo getVisibilidade(ProcessoDocumento procDoc, Pessoa pessoa) {
		ProcessoDocumentoVisibilidadeSegredo visibilidade = new ProcessoDocumentoVisibilidadeSegredo();
		visibilidade.setProcessoDocumento(procDoc);
		visibilidade.setPessoa(pessoa);
		return visibilidade;
	}

	private void gravarVisibilidade(List<ProcessoDocumentoVisibilidadeSegredo> visibilidadesAgrupadas) {
		if(visibilidadesAgrupadas != null && !visibilidadesAgrupadas.isEmpty()){
			ProcessoDocumentoVisibilidadeSegredoManager usuManager = ComponentUtil.getComponent(ProcessoDocumentoVisibilidadeSegredoManager.class);
			try {
				for(ProcessoDocumentoVisibilidadeSegredo docVisibSegredo: visibilidadesAgrupadas){
					usuManager.persistAndFlush(docVisibSegredo);
				}
			} catch (PJeBusinessException e) {
				logger.error("Erro ao setar visibilidade do documento agrupado. {0}", e.getMessage());
			}
		}
	}

	/**
	 * Recupera os endereços vinculados a uma determinada pessoa.
	 * 
	 * @return mapa contendo os endereços escolhidos para a realização de intimações físicas.
	 * 
	 */
	private Map<Pessoa, Set<Endereco>> recuperaEnderecos(){
		Map<Pessoa, Set<Endereco>> ret = new HashMap<Pessoa, Set<Endereco>>();
		if (destinatarios.size() > 0){
			EnderecoService enderecoService = ComponentUtil.getComponent(EnderecoService.class);
			AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
			for (DestinatarioComunicacao d : destinatarios.keySet()){
				if (d.getEndereco() == null) {
					ProcessoParteExpediente ppe = atoComunicacaoService.getAtoPessoal(destinatarios.get(d) , d.getPessoa(), mapaProcuradoriaSelecionada.get(d.getPessoa()));
					List<Endereco> auxList = ppe.getEnderecos();
					if (auxList.size() > 0){
						Set<Endereco> set = new HashSet<Endereco>(auxList.size());
						for (Endereco end : auxList){
							// Em alguns casos os endereços estão sendo serializados nulos dentro da própria lista
							if (end == null){
								continue;
							}
							Endereco aux = enderecoService.getEndereco(end.getIdEndereco());
							if (aux != null){
								set.add(aux);
							}
						}
						ret.put(d.getPessoa(), set);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Recupera os tipos de instrumento de comunicação escolhidos em execução anterior do nó.
	 * 
	 * @return mapa contendo a vinculação entre uma pessoa e o instrumento de comunicação escolhido na execução anterior.
	 */
	@SuppressWarnings("unchecked")
	private Map<DestinatarioComunicacao, InstrumentoComunicacao> recuperaInstrumentos(){
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();

		if (taskInstance != null){
			Map<DestinatarioComunicacao, InstrumentoComunicacao> ret = (HashMap<DestinatarioComunicacao, InstrumentoComunicacao>) taskInstance
					.getVariable(VAR_MAPAINSTR + taskInstance.getId());
			if (ret != null){
				return ret;
			}
		}
		return new HashMap<DestinatarioComunicacao, PreparaAtoComunicacaoAction.InstrumentoComunicacao>();
	}

	/**
	 * Carrega os agrupamentos escolhidos em execução anterior do nó.
	 */
	@SuppressWarnings("unchecked")
	private void carregarMapaAgrupamentos(){
		//Agrupamentos de destinatários, tendo como chave o destinatário agrupador,
		//e como valor uma coleção com os pares { destinatário agrupado, ProcessoExpediente original }.
		this.agrupamentos = new HashMap<DestinatarioComunicacao, Set<Entry<DestinatarioComunicacao,ProcessoExpediente>>>();
		this.mapaAgrupadoAgrupador = new HashMap<DestinatarioComunicacao, DestinatarioComunicacao>();
		this.destinatarioAgrupadoIdAgrupador = new HashMap<DestinatarioComunicacao, String>();

		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();

		if (taskInstance != null){
			//Recupera o mapa contendo a vinculação entre uma pessoa e as pessoas agrupadas a ela,
			//escolhidos na execução anterior.
			Map<DestinatarioComunicacao, Set<Entry<DestinatarioComunicacao,ProcessoExpediente>>> ret = 
					(Map<DestinatarioComunicacao, Set<Entry<DestinatarioComunicacao,ProcessoExpediente>>>) taskInstance.getVariable(VAR_MAPAAGRUP + taskInstance.getId());

			if (ret != null){
				this.agrupamentos = ret;
				//A partir do mapa recuperado, preenche a visão dos agrupamentos pelos agrupados.
				for (Entry<DestinatarioComunicacao, Set<Entry<DestinatarioComunicacao,ProcessoExpediente>>> agrupamento : this.agrupamentos.entrySet()) {
					DestinatarioComunicacao agrupador = agrupamento.getKey();
					Set<Entry<DestinatarioComunicacao,ProcessoExpediente>> agrupados = agrupamento.getValue();
					if (agrupados != null) {
						for (Entry<DestinatarioComunicacao,ProcessoExpediente> entry : agrupados) {
							DestinatarioComunicacao agrupado = entry.getKey();
							this.mapaAgrupadoAgrupador.put(agrupado, agrupador);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Variável, que exibe os destinatarios do polo ativo
	 */
	public String getDestinatariosEnderecoPoloAtivoExpedienteStr(){
		return getDestinatariosEnderecoStr(ProcessoParteParticipacaoEnum.A);
	}

	/**
	 * Variável, que exibe as destinatarios do polo passivo
	 */
	public String getDestinatariosEnderecoPoloPassivoExpedienteStr(){
		return getDestinatariosEnderecoStr(ProcessoParteParticipacaoEnum.P);
	}
	
	/**
	 * Variável, que exibe os destinatarios terceiros
	 */	
	public String getDestinatariosEnderecoTerceirosExpedienteStr(){
		return getDestinatariosEnderecoStr(ProcessoParteParticipacaoEnum.T);
	}	 
	
	public String getDestinatariosEnderecoStr(ProcessoParteParticipacaoEnum inParticipacao) {
		StringBuilder sb = new StringBuilder();
		List<ProcessoParte> processoParteList = new ArrayList<ProcessoParte>(0);
		
		switch (inParticipacao){
			case A:
				processoParteList = processoJudicial.getProcessoPartePoloAtivoSemAdvogadoList();
				break;
			case P:
				processoParteList = processoJudicial.getProcessoPartePoloPassivoSemAdvogadoList();
				break;
			default:
				processoParteList = processoJudicial.getProcessoParteSemAdvogadoList();
		}
		
		for (DestinatarioComunicacao destinatarioComunicacao : this.getDestinatarios().keySet()) { 							
			for(ProcessoParte pp: processoParteList){
				if(destinatarioComunicacao.getPessoa().equals(pp.getPessoa())){
					sb.append(pp.getNomeParte());
					sb.append("<br />");
					if(recuperaEnderecos().containsKey(pp.getPessoa())){
						Set<Endereco> enderecosPessoa = recuperaEnderecos().get(pp.getPessoa());
						for(Endereco endereco : enderecosPessoa) {
							sb.append(endereco.getEnderecoCompleto());
							sb.append("<br />");
						}
					}
					break;
				}						
			}
		}
		
		return sb.toString();
	}	

	/**
	 * Obtém os modelos disponíveis para essa atividade, conforme {@link DocumentoJudicialService#getModelosDisponiveis()}
	 * 
	 * @return os modelos de documentos disponibilizados para essa tarefa.
	 */
	public List<ModeloDocumento> getModelosDisponiveis(){
		try{
			TipoProcessoDocumento tipoProcessoDocumento = getDestinatarios().get(getDestinatarioSelecionado()).getProcessoDocumento().getTipoProcessoDocumento();
			DocumentoJudicialService documentoJudicialService = ComponentUtil.getComponent(DocumentoJudicialService.class);
			return documentoJudicialService.getModelosDisponiveisPorTipoDocumento(tipoProcessoDocumento);
		} catch (Exception e){
			facesMessages.add(Severity.ERROR,
					"Houve um erro de banco de dados ao tentar obter os modelos de documentos disponíveis.");
		}
		return null;
	}

	/**
	 * Permite a realização de consulta dinâmica por pessoas no sistema, sejam elas físicas, jurídicas ou autoridades.
	 * 
	 * @param valor sequência de caracteres destinados à pesquisa. Quando se tratar de CPF (11 dígitos) ou CNPJ (8 ou 14 dígitos), a pesquisa será
	 *            feita por esses documentos.
	 * 
	 * @return a lista de pessoas que podem ser identificadas pelo valor dado.
	 */
	public List<Pessoa> pesquisaPessoas(Object valor){
		String txt = (String) valor;
		String strippedTxt = InscricaoMFUtil.retiraMascara(txt);
		List<Pessoa> ret = new ArrayList<Pessoa>();
		if (strippedTxt.matches("\\d*")
			&& (strippedTxt.length() == 8 || strippedTxt.length() == 11 || strippedTxt.length() == 14)){
			try{
				PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
				ret.add(pessoaService.findByInscricaoMF(txt));
			} catch (PJeBusinessException e){
				facesMessages.add(Severity.ERROR,
						"Houve um erro ao buscar a pessoa pelo número de inscrição no Ministério da Fazenda: {0}",
						e.getLocalizedMessage());
				// Como o action do suggestionBox só é executado na fase JSF RENDER_RESPONSE,
				// devemos executar este método utilitário para repassar o FacesMessage adiante.
				FacesUtil.refreshFacesMessages();
			}
		}else if (txt.length() >= 3){
			try {
				PessoaManager pessoaManager = (PessoaManager) Component.getInstance("pessoaManager");
				String textoPesquisa = txt.replaceAll("\\s", "%");
				Search search = new Search(Pessoa.class);
				search.setDistinct(true);
				search.setMax(15);
				
				Criteria nome = Criteria.contains("nomesPessoa.nome", textoPesquisa);
				search.addCriteria(nome);

				Criteria tipoNomeCriteria = Criteria.in("nomesPessoa.tipo", new TipoNomePessoaEnum[] {TipoNomePessoaEnum.C, TipoNomePessoaEnum.D, TipoNomePessoaEnum.A } );
				search.addCriteria(tipoNomeCriteria);
				
				List<Pessoa> pessoas = pessoaManager.list(search);
				ret.addAll(pessoas);
			} catch (NoSuchFieldException e) {
				facesMessages.add(Severity.ERROR, "Houve um erro ao tentar localizar os possíveis destinatários.");
			}
		}
		return ret;
	}

	/**
	 * Permite pesquisar a tabela de CEP.
	 * 
	 * @param valor String contendo todos os dígitos do CEP a ser pesquisado.
	 * @return O CEP que contém os dígitos dados.
	 */
	public Cep pesquisaCep(Object valor){
		String txt = (String) valor;
		CepService cepService = ComponentUtil.getComponent(CepService.class);
		return cepService.findByCodigo(txt);
	}

	/**
	 * 1- Acrescenta à lista de destinatários uma determinada pessoa.
	 * 2- Verifica o comportamento do campo "pessoal".
	 * 3- Associa os representantes à parte.
	 * 
	 * @param pessoa {@link Pessoa} a ser acrescentada como destinatário.
	 * 
	 * @param procuradoriaSelecionada A procuradoria que foi selecionada no momento do 
	 * protocolamento do processo, caso exista. Será a procuradoria padrão, 
	 * caso a pessoa possua mais de uma representação e ela ainda esteja ativa.
	 */
	private void acrescentaDestinatario(DestinatarioComunicacao destinatario, Procuradoria procuradoriaSelecionada) {
		Pessoa pessoa = destinatario.getPessoa();
		if (pessoa != null && !this.destinatarios.containsKey(destinatario)){
			AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
			ProcessoExpediente processoExpediente = atoComunicacaoService.getAtoComunicacao();
			destinatarios.put(destinatario, processoExpediente);
			mapaUmEnderecoPorExpediente.put(pessoa, false);
			
			List<Procuradoria> procuradoriaList = new ArrayList<Procuradoria>();
		
			ProcessoParte processoParte = atoComunicacaoService.getProcessoParte(pessoa, this.processoJudicial);
			if (processoParte != null) { // É parte do processo.
				
				if (procuradoriaSelecionada != null && procuradoriaSelecionada.getAtivo()) {
					procuradoriaList.add(procuradoriaSelecionada);
				}
			} else {
				PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
				procuradoriaList = pessoaService.obtemOrgaosRepresentantes(pessoa);
			}

			adicionarProcuradoriasAoMapa(pessoa, procuradoriaList);
			definirProcuradoriaSelecionada(pessoa, procuradoriaSelecionada);
			
			mapaIntimacaoPessoal.put(pessoa, Boolean.FALSE);
			
			if (!this.mapaAgrupadoAgrupador.containsKey(destinatario)) {
				this.mapaAgrupadoAgrupador.put(destinatario, null);
			}
		}
	}
	
	/**
	 * Método responsável por adicicionar uma lista de advogados ao mapa de
	 * relacionamento entre pessoa e seus advogados
	 */
	private void adicionarAdvogadosAoMapa(Pessoa pessoa, List<ProcessoParteRepresentante> advogados) {
		List<ProcessoParteRepresentante> advogadosAtivos = new ArrayList<ProcessoParteRepresentante>();
		if (pessoa != null && advogados != null) {
			for (ProcessoParteRepresentante advogado : advogados) {
				if (advogado.getInSituacao() == ProcessoParteSituacaoEnum.A) {
					advogadosAtivos.add(advogado);
				}
			}			
		}
		
		List<ProcessoParteRepresentante> listaDoMapa = mapaAdvogados.get(pessoa);
		if (listaDoMapa != null && !listaDoMapa.isEmpty()) {
			listaDoMapa.addAll(advogadosAtivos);
			Set<ProcessoParteRepresentante> setAdvogados = new HashSet<ProcessoParteRepresentante>(listaDoMapa);
			mapaAdvogados.remove(pessoa);
			mapaAdvogados.put(pessoa, new ArrayList<ProcessoParteRepresentante>(setAdvogados));
			
		} else {
			mapaAdvogados.put(pessoa, advogadosAtivos);
		}
	}
	
	/**
	 * Método responsável por remover do mapa de procuradorias as procuradorias
	 * setadas para uma determinada pessoa
	 */
	private void removerProcuradoriasDoMapa(Pessoa pessoa) {
		if (pessoa != null && mapaProcuradorias != null) {
			mapaProcuradorias.remove(pessoa);
		}
	}
	
	/**
	 * Método responsável por adicionar uma determinada procuradoria a lista de
	 * mapa de uma pessoa
	 * 
	 * @see #adicionarProcuradoriasAoMapa(Pessoa, List)
	 */
	private void adicionarProcuradoriaAoMapa(Pessoa pessoa, Procuradoria procuradoria) {
		if (pessoa != null && procuradoria != null) {
			List<Procuradoria> list = new ArrayList<Procuradoria>();
			list.add(procuradoria);
			adicionarProcuradoriasAoMapa(pessoa, list);
		}
	}
	
	/**
	 * Método responsável por adicionar uma lista de procuradias no mapa para
	 * uma pessoa. É feita uma validação se as procuradorias na lista estão
	 * ativas.
	 * 
	 * @see #verificarHabilitacaoModalSelProcuradoria(Pessoa)
	 */
	private void adicionarProcuradoriasAoMapa(Pessoa pessoa, List<Procuradoria> procuradorias) {
		List<Procuradoria> listaProcuradoriasAtivas = new ArrayList<Procuradoria>();
		if (pessoa != null && procuradorias != null) {
			for (Procuradoria procuradoria : procuradorias) {
				if (procuradoria.getAtivo()) {
					listaProcuradoriasAtivas.add(procuradoria);
				}
			}			
		}
		
		List<Procuradoria> listaDoMapa = mapaProcuradorias.get(pessoa);
		if (listaDoMapa != null && !listaDoMapa.isEmpty()) {
			listaDoMapa.addAll(listaProcuradoriasAtivas);
			Set<Procuradoria> setListaProcuradoria = new HashSet<Procuradoria>(listaDoMapa);
			mapaProcuradorias.remove(pessoa);
			mapaProcuradorias.put(pessoa, new ArrayList<Procuradoria>(setListaProcuradoria));
		} else if (!listaProcuradoriasAtivas.isEmpty()) {
			mapaProcuradorias.put(pessoa, listaProcuradoriasAtivas);
		}
		
		verificarHabilitacaoModalSelProcuradoria(pessoa);
	}
	
	/**
	 * Realiza a definição da procuradoria selecionada para emitir o expedinte.
	 * 
	 * @param pessoa Pessoa Destinatária
	 * 
	 * @param procuradoria Procuradoria que representa a pessoa. Caso seja nulo irá 
	 * definir através do mapa de procuradorias carregado se possuir apenas uma procuradoria no mapa.
	 * Caso a procuradoria seja nula, irá apagar o mapa de procuradoria selecionada para a pessoa.
	 */
	private void definirProcuradoriaSelecionada(Pessoa pessoa, Procuradoria procuradoria) {		
		if (pessoa != null) {			
			if (procuradoria != null && procuradoria.getAtivo()) {
				mapaProcuradoriaSelecionada.put(pessoa, procuradoria);	
			} else if (mapaProcuradorias.get(pessoa) != null && mapaProcuradorias.get(pessoa).size() == 1) {
				mapaProcuradoriaSelecionada.put(pessoa, mapaProcuradorias.get(pessoa).get(0));
			} else {
				mapaProcuradoriaSelecionada.remove(pessoa);
			}
		} 
		procuradoriaSelecionada = null;
	}
	
	/**
	 * Verifica a necessidade de habilitação do modal para seleção da
	 * procuradoria. Caso a pessoa possua mais de uma procuradoria habilitada
	 * então o atributo modalRepresentacao será setado para true
	 */
	private void verificarHabilitacaoModalSelProcuradoria(Pessoa pessoa){
		List<Procuradoria> list = mapaProcuradorias.get(pessoa);
		if (list != null && list.size() > 1) {
			setModalRepresentacaoAberta(true);
		}
	}
	
	/**
	 * Identifica, entre os destinatários selecionados, aqueles que podem servir como agrupadores para uma determinada pessoa. 
	 * Isso permite que um mesmo ato de comunicação sirva como base para os diferentes destinatários. 
	 * Somente é possível agrupar em um mesmo expediente uma determinada pessoa se o meio de intimação (correios, mandados etc.) 
	 * e o tipo de documento produzido (intimação, citação etc.) forem idênticos.
	 * 
	 * @param destinatario O destinatário da comunicação.
	 * @return Lista de possíveis agrupadores. A lista será vazia se a pessoa já é um agrupador.
	 */
	public List<SelectItem> possiveisAgrupadores(DestinatarioComunicacao destinatario){
		List<SelectItem> ret = new ArrayList<SelectItem>();
		if (isDestinatarioAgrupador(destinatario)){
			return ret;
		}
		
		ProcessoExpediente pePdg = this.destinatarios.get(destinatario);
		for (DestinatarioComunicacao d : this.destinatarios.keySet()){
			if (!d.equals(destinatario)){
				if (!isDestinatarioAgrupado(d) && 
						((this.mapaUmEnderecoPorExpediente.get(d.getPessoa()) && d.getEndereco() != null) || 
								(!this.mapaUmEnderecoPorExpediente.get(d.getPessoa()) && d.getEndereco() == null))){
					
					ProcessoExpediente pe = this.destinatarios.get(d);
					if (pe.getMeioExpedicaoExpediente() == pePdg.getMeioExpedicaoExpediente()
						&& pe.getTipoProcessoDocumento() == pePdg.getTipoProcessoDocumento()){
						if (pe.getProcessoDocumento() == null || pePdg.getProcessoDocumento() == null || 
							pe.getProcessoDocumento().getTipoProcessoDocumento() == pePdg.getProcessoDocumento().getTipoProcessoDocumento()){
							ret.add(new SelectItem(Integer.toString(d.hashCode()), d.getId() + " - " + d.getPessoa()));
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Identifica os destinatários cujo meio de intimação é físico (correios, mandados ou cartas precatórias).
	 * 
	 * @return a lista de destinatários cujo meio de intimação é físico.
	 */
	public List<DestinatarioComunicacao> getDestinatariosFisicos(){
		List<DestinatarioComunicacao> ret = new ArrayList<DestinatarioComunicacao>();
		if (this.mapaEnderecos == null){
			this.mapaEnderecos = new HashMap<Pessoa, Set<Endereco>>();
		}
		for (DestinatarioComunicacao d : destinatarios.keySet()){
			ProcessoExpediente pe = destinatarios.get(d);
			ExpedicaoExpedienteEnum meio = pe.getMeioExpedicaoExpediente();
			if (meio == ExpedicaoExpedienteEnum.C || meio == ExpedicaoExpedienteEnum.L
				|| meio == ExpedicaoExpedienteEnum.M || meio == ExpedicaoExpedienteEnum.G){
				if (this.mapaEnderecos.get(d.getPessoa()) == null){
					this.mapaEnderecos.put(d.getPessoa(), new HashSet<Endereco>());
				}
				if (d.getEndereco() == null) {
					ret.add(d);
 				}
			}
		}
		return ret;
	}

	/**
	 * Acrescenta, como destinatários, todos os componentes do polo ativo.
	 */
	public void acrescentaPoloAtivo(){
		acrescentaPolo(ProcessoParteParticipacaoEnum.A);
	}

	/**
	 * Acrescenta, como destinatários, todos os componentes do polo passivo.
	 */
	public void acrescentaPoloPassivo(){
		acrescentaPolo(ProcessoParteParticipacaoEnum.P);
	}

	/**
	 * Acrescenta, como destinatários, todos os terceiro.
	 */
	public void acrescentaPoloTerceiros(){
		acrescentaPolo(ProcessoParteParticipacaoEnum.T);
	}

	/**
	 * Acrescenta, como destinatários, todos os participantes do processo.
	 */
	public void acrescentaParticipantes() {
		List<ProcessoParte> partes = null;
		if (ParametroJtUtil.instance().justicaTrabalho()) {
			partes = processoJudicial.getListaPartePoloObj(ProcessoParteParticipacaoEnum.A,
					ProcessoParteParticipacaoEnum.P, ProcessoParteParticipacaoEnum.T);
		} else {
			partes = processoJudicial.getListaPartePrincipal(ProcessoParteParticipacaoEnum.A,
					ProcessoParteParticipacaoEnum.P, ProcessoParteParticipacaoEnum.T);
		}
		for (ProcessoParte parte : partes){
			setDestinatarioSelecionadoPessoa(parte.getPessoa(), parte.getProcuradoria());
		}
	}
	
	 /**
	  * Identifica se a {@link Pessoa} é parte do processo judicial.
	  *
	  * @param pessoa {@link Pessoa} a ser identificada como parte no processo judicial.
	  * @return Verdadeiro caso a {@link Pessoa} seja parte do processo. Falso, caso contrário.
	  */
	public boolean isParte(Pessoa pessoa){
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		return atoComunicacaoService.getProcessoParte(pessoa, this.processoJudicial) != null;
	}

	/**
	 * Acrescenta os participantes de um dado polo processual como destinatários das intimações.
	 * 
	 * @param polo {@link ProcessoParteParticipacaoEnum} desejado
	 */
	private void acrescentaPolo(ProcessoParteParticipacaoEnum polo){
		List<ProcessoParte> partesPolo = processoJudicial.getListaPartePrincipal(polo);
		for (ProcessoParte parte : partesPolo){
			setDestinatarioSelecionadoPessoa(parte.getPessoa(), parte.getProcuradoria());
		}
	}

	/**
	 * Permite agrupar, em um só ato de comunicação, duas ou mais pessoas.
	 * 
	 * @param agrupado A pessoas a ser agrupada.
	 * @return Verdadeiro se foi possível realizar o agrupamento. Falso, caso contrário.
	 */
	public boolean atualizarAgrupamentos(DestinatarioComunicacao agrupado) {
		if (agrupado == null) {
			return false;
		}

		DestinatarioComunicacao agrupador = null;
		String idAgrupador = this.destinatarioAgrupadoIdAgrupador.get(agrupado);
		if (org.apache.commons.lang.StringUtils.isNotBlank(idAgrupador)) {
			for (DestinatarioComunicacao destinatario : this.destinatarios.keySet()) {
				if (destinatario.hashCode() == Integer.parseInt(idAgrupador)) {
					agrupador = destinatario;
					break;
				}
			}
		}
		
		if (agrupado.equals(agrupador)) {
			//O destinatário não pode ser agrupado com ele mesmo.
			this.mapaAgrupadoAgrupador.put(agrupado, null);
			return false;
		}
		
		boolean ehAgrupado = agrupador != null;
		
		DestinatarioComunicacao agrupadorAnterior = obterAgrupadorPorDestinatario(agrupado);
		boolean eraAgrupado = agrupadorAnterior != null;
		boolean eraAgrupadoOutroAgrupador = eraAgrupado && agrupadorAnterior != null && !agrupadorAnterior.equals(agrupador);
		
		if (ehAgrupado && eraAgrupado && !eraAgrupadoOutroAgrupador) {
			//Nada mudou: A pessoa já estava agrupada ao mesmo agrupador.
			return false;
		} else if (!ehAgrupado && !eraAgrupado) {
			//Nada mudou: A pessoa não está nem estava agrupada a ninguém.
			return false;
		} else {
			if (ehAgrupado) {
				if (eraAgrupadoOutroAgrupador) {
					desagruparDestinatario(agrupadorAnterior, agrupado);
				}
				return agruparDestinatario(agrupador, agrupado);
			} else if (eraAgrupado) {
				desagruparDestinatario(agrupadorAnterior, agrupado);
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Agrupa, em um só ato de comunicação, duas pessoas.
	 * 
	 * @param agrupador A pessoa a quem se quer agrupar.
	 * @param agrupado A pessoa a ser agrupada.
	 * @return Verdadeiro se foi possível realizar o agrupamento. Falso, caso contrário.
	 */
	private boolean agruparDestinatario(DestinatarioComunicacao agrupador, DestinatarioComunicacao agrupado) {
		if (agrupador == null) {
			throw new IllegalArgumentException("O parâmetro 'agrupador' não pode ser nulo.");
		}
		if (agrupado == null) {
			throw new IllegalArgumentException("O parâmetro 'agrupado' não pode ser nulo.");
		}
		if (agrupador.equals(agrupado)) {
			throw new IllegalArgumentException("Os parâmetros 'agrupador' e 'agrupado' não podem ser iguais.");
		}

		ProcessoExpediente peAgrupador = this.destinatarios.get(agrupador);
	 	
		if (peAgrupador == null){
			facesMessages.add(Severity.ERROR, "Não é possível agrupar destinatários com pessoa não escolhida como tal [{0}].", agrupador);
			return false;
		}

		ProcessoExpediente peAgrupado = this.destinatarios.get(agrupado);
		if (peAgrupado == null){
			facesMessages.add(Severity.ERROR, "Não é possível agrupar destinatário [{0}].", agrupado);
			return false;
		}
		
		/*
		 * Atenção: Não utilizar equals() na comparação entre ProcessoExpedientes, pois a implementação atual
		 * sempre considera iguais dois ProcessoExpedientes quando ambos idProcessoExpediente forem 0.  
		 */
		if (peAgrupador == peAgrupado) {
			// Este método não pode ser chamado quando os destinatários 'agrupado' e 'agrupador' já estiverem agrupados.
			throw new IllegalArgumentException("O agrupamento já existe.");
		}

		// Adiciona no agrupador o ProcessoParteExpediente do agrupado.
		for (ProcessoParteExpediente ppe : peAgrupado.getProcessoParteExpedienteList()) {
			if (agrupado.getPessoa().equals(ppe.getPessoaParte())) {
				peAgrupador.getProcessoParteExpedienteList().add(ppe);
				break;
			}
		}
		
		// Troca o ProcessoExpediente do agrupado.
		this.destinatarios.put(agrupado, peAgrupador);
		
		this.mapaAgrupadoAgrupador.put(agrupado, agrupador);
		
		// Atualiza os agrupamentos.
		return adicionarAgrupamento(agrupador, agrupado, peAgrupado);
	}
	
	/**
	 * Desagrupa duas pessoas do ato de comunicação.
	 * 
	 * @param agrupador a pessoa agrupadora.
	 * @param agrupado a pessoa a ser desagrupada.
	 * @return true, se foi possível remover o agrupamento, false, se não foi possível.
	 */
	private boolean desagruparDestinatario(DestinatarioComunicacao agrupador, DestinatarioComunicacao agrupado) {
		if (agrupador == null) {
			throw new IllegalArgumentException("O parâmetro 'agrupador' não pode ser nulo.");
		}
		if (agrupado == null) {
			throw new IllegalArgumentException("O parâmetro 'agrupado' não pode ser nulo.");
		}
		if (agrupador.equals(agrupado)) {
			throw new IllegalArgumentException("Os parâmetros 'agrupador' e 'agrupado' não podem ser iguais.");
		}

		//Reutiliza o ProcessoExpediente original do agrupado.
		Entry<DestinatarioComunicacao, Entry<DestinatarioComunicacao,ProcessoExpediente>> agrupamentoAnterior = obterAgrupamentoPorDestinatario(agrupado);
		if (agrupamentoAnterior == null) {
			//Os dados do agrupamento não foram encontrados para serem removidos.
			return false;
		}
	 	
		ProcessoExpediente peAgrupado = agrupamentoAnterior.getValue().getValue();
		this.destinatarios.put(agrupado, peAgrupado);
		
		//Remove do agrupador anterior o ProcessoParteExpediente do agrupado. 
		ProcessoExpediente peAgrupadorAnterior = this.destinatarios.get(agrupador);
		for (int ii = 0; ii < peAgrupadorAnterior.getProcessoParteExpedienteList().size(); ii++) {
			ProcessoParteExpediente ppe = peAgrupadorAnterior.getProcessoParteExpedienteList().get(ii);
			if (agrupado.getPessoa().equals(ppe.getPessoaParte())) {
				//Atenção: Como os objetos ProcessoParteExpediente da coleção getProcessoParteExpedienteList()
				//ainda não têm valor Id (idProcessoParteExpediente), o método ProcessoParteExpediente.equals() 
				//retorna true mesmo para objetos diferentes. Por isso, é necessário remover pelo índice, ao 
				//invés de remover pelo objeto.
				peAgrupadorAnterior.getProcessoParteExpedienteList().remove(ii);
				break;
			}
		}
		
		this.mapaAgrupadoAgrupador.put(agrupado, null);
		
		//Atualiza os agrupamentos.
		return removerAgrupamento(agrupador, agrupado);
	}
	
	/**
	 * Retorna o destinatário ao qual o destinatário selecionado está agrupado, se houver.
	 * 
	 * @return pessoa agrupadora. 
	 */
	private DestinatarioComunicacao obterAgrupadorPorDestinatario(DestinatarioComunicacao destinatario) {
		Entry<DestinatarioComunicacao, Entry<DestinatarioComunicacao,ProcessoExpediente>> agrupamento = obterAgrupamentoPorDestinatario(destinatario);
		if (agrupamento != null) {
			return agrupamento.getKey();
		}
		return null;
	}
	
	/**
	 * Retorna os dados do agrupamento do destinatário com outro destinatário, se houver.
	 * 
	 * @return Tupla contendo <agrupador, <agrupado, peAgrupado>>. 
	 */
	private Entry<DestinatarioComunicacao, Entry<DestinatarioComunicacao,ProcessoExpediente>> obterAgrupamentoPorDestinatario(DestinatarioComunicacao agrupado) {
		for (Entry<DestinatarioComunicacao, Set<Entry<DestinatarioComunicacao,ProcessoExpediente>>> entryAgrup : this.agrupamentos.entrySet()) {
			if (entryAgrup.getValue() != null) {
				for (Entry<DestinatarioComunicacao,ProcessoExpediente> entry : entryAgrup.getValue()) {
					if (entry.getKey().equals(agrupado)) {
						return new SimpleEntry<DestinatarioComunicacao, Entry<DestinatarioComunicacao,ProcessoExpediente>>(entryAgrup.getKey(), entry);
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Remove um agrupamento da coleção agrupamentos. 
	 * 
	 * @return true, se o destinatário agrupado foi removido da coleção agrupamentos.
	 */
	private boolean removerAgrupamentoPorDestinatario(DestinatarioComunicacao agrupado) {
		Entry<DestinatarioComunicacao, Entry<DestinatarioComunicacao,ProcessoExpediente>> agrupamento = obterAgrupamentoPorDestinatario(agrupado);
		if (agrupamento != null) {
			return removerAgrupamento(agrupamento.getKey(), agrupado);
		}
		return false;
	}

	/**
	 * Remove um agrupamento da coleção agrupamentos.
	 * 
	 * @return true, se o destinatário agrupado foi removido da coleção agrupamentos.
	 */
	private boolean removerAgrupamento(DestinatarioComunicacao agrupador, DestinatarioComunicacao agrupado) {
		for (Entry<DestinatarioComunicacao, Set<Entry<DestinatarioComunicacao,ProcessoExpediente>>> entryAgrup : this.agrupamentos.entrySet()) {
			if (entryAgrup.getKey().equals(agrupador) && entryAgrup.getValue() != null) {
				for (Entry<DestinatarioComunicacao,ProcessoExpediente> entry : entryAgrup.getValue()) {
					if (entry.getKey().equals(agrupado)) {
						return entryAgrup.getValue().remove(entry);
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Adiciona um agrupamento na coleção agrupamentos.
	 * 
	 * @return Verdadeiro se o destinatário agrupado foi adicionado na coleção agrupamentos. Falso, caso contrário.
	 */
	private boolean adicionarAgrupamento(DestinatarioComunicacao agrupador, DestinatarioComunicacao agrupado, ProcessoExpediente peAgrupado) {
		if (agrupador == null) {
			throw new IllegalArgumentException("O parâmetro 'agrupador' não pode ser nulo.");
		}
		if (agrupado == null) {
			throw new IllegalArgumentException("O parâmetro 'agrupado' não pode ser nulo.");
		}
		if (peAgrupado == null) {
			throw new IllegalArgumentException("O parâmetro 'peAgrupado' não pode ser nulo.");
		}
		if (agrupador.equals(agrupado)) {
			throw new IllegalArgumentException("Os parâmetros 'agrupador' e 'agrupado' não podem ser iguais.");
		}
		if (this.agrupamentos.get(agrupador) == null) {
			this.agrupamentos.put(agrupador, new HashSet<Entry<DestinatarioComunicacao,ProcessoExpediente>>());
		}
		return this.agrupamentos.get(agrupador).add(new SimpleEntry<DestinatarioComunicacao,ProcessoExpediente>(agrupado, peAgrupado));
	}
	
	/**
	 * Identifica se o destinatário selecionado está agrupado a outro destinatário.
	 * 
	 * @param pessoa a pessoa que se pretende verificar agrupamento.
	 * @return true, se o destinatário tem um agrupador selecionado.
	 */
	public boolean isDestinatarioAgrupado(DestinatarioComunicacao destinatario) {
		return this.mapaAgrupadoAgrupador.get(destinatario) != null;
	}

	/**
	 * Identifica se o destinatário selecionado está agrupado a outro destinatário.
	 * 
	 * @param pessoa a pessoa que se pretende verificar agrupamento.
	 * @return true, se o destinatário tem um agrupador selecionado.
	 */
	public boolean isDestinatarioAgrupador(DestinatarioComunicacao destinatario) {
		return this.mapaAgrupadoAgrupador.containsValue(destinatario);
	}
	
	/**
	 * Identifica se existe um agrupamento possível.
	 * 
	 * @return true, se é possível agrupar destinatários.
	 */
	public boolean existeAgrupamentoPossivel() {
		for (DestinatarioComunicacao destinatarioComunicacao : this.destinatarios.keySet()){
			if (!possiveisAgrupadores(destinatarioComunicacao).isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param pessoa Pessoa a ser removida da lista dos destinatários da comunicação.
	 */
	public boolean removerDestinatario(DestinatarioComunicacao destinatario){
		if (isDestinatarioAgrupador(destinatario)) {
			//Se o destinatário é um agrupador, ele não pode ser removido, 
			//pois outros destinatários dependem dele.
			return false;
		}
		
		this.destinatarios.remove(destinatario);
		this.destinatarioSelecionado = null;

		// se estiver apagando a pessoa sem endereço, todas as ocorrências dessa pessoa deve ser removidas do mapa. 
		if( destinatario.getEndereco() == null ) {
			this.mapaAgrupadoAgrupador.remove(destinatario);
			this.destinatarioAgrupadoIdAgrupador.remove(destinatario);
			removerAgrupamentoPorDestinatario(destinatario);
			
			Set<DestinatarioComunicacao> chaves = new HashSet<DestinatarioComunicacao>(destinatarios.keySet());
			for (DestinatarioComunicacao d: chaves) {
				if (destinatario.getPessoa().equals(d.getPessoa())){
					this.destinatarios.remove(d);
				}
			}
		} else { 
			enderecosSelecionados.put(destinatario.getEndereco(), false);
			mapaEnderecos.get(destinatario.getPessoa()).remove(destinatario.getEndereco());
			// Se esta pessoa for usar um expediente por endereço e não sobrou nenhuma ocorrência com endereço, exclui a pessoa do mapa
			if (mapaUmEnderecoPorExpediente.get(destinatario.getPessoa())) {
				boolean apagarPessoa = false;
				for (DestinatarioComunicacao d: this.destinatarios.keySet()) {
					apagarPessoa = true;
					if (destinatario.getPessoa().equals(d.getPessoa()) && d.getEndereco() != null ) {
						apagarPessoa = false;
						break;
					}
				}			
				if(apagarPessoa) {
					removerDestinatario(destinatario);
				}
			}
		}
		
		if (mapaProcuradoriaSelecionada != null && mapaProcuradoriaSelecionada.containsKey(destinatario.getPessoa())){
			mapaProcuradoriaSelecionada.remove(destinatario.getPessoa());
			procuradoriaSelecionada = null;
		}
		this.toggleMapaIntimacaoPessoal(destinatario.getPessoa());
		
		removerProcuradoriasDoMapa(destinatario.getPessoa());
		
		consolidaAtosComunicacao();
		
		return true;
	}

	/**
	 * Cria um novo endereço para o {@link PreparaAtoComunicacaoAction#destinatarioSelecionado}
	 */
	public void criaEndereco(){
		if (destinatarioSelecionado != null){
			EnderecoService enderecoService = ComponentUtil.getComponent(EnderecoService.class);
			this.enderecoNovo = enderecoService.getEndereco();
		}
	}

	/**
	 * Grava um novo endereço para o destinatário selecionado.
	 */
	public void gravaNovoEndereco(){
		try{
			Endereco novoEndereco = null;
			AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
			ProcessoParte processoParte = atoComunicacaoService.getProcessoParte(this.destinatarioSelecionado.getPessoa(), this.processoJudicial);
			EnderecoService enderecoService = ComponentUtil.getComponent(EnderecoService.class);
			if (this.incluirComoEnderecoProcessual){
				 novoEndereco = enderecoService.gravaEndereco(enderecoNovo, processoParte);
			} else {
				novoEndereco = enderecoService.gravaEndereco(this.enderecoNovo, this.destinatarioSelecionado.getPessoa());
			}
			
			// Atualiza cache de endereco
			if(novoEndereco != null){
				enderecosSelecionados.put(novoEndereco, true);
				adotaEnderecos();
				if (cacheEnderecosPossiveis.containsKey(destinatarioSelecionado.getPessoa())){
					cacheEnderecosPossiveis.get(destinatarioSelecionado.getPessoa()).add(novoEndereco.getIdEndereco());
					cacheEnderecosPossiveisDescricao.get(destinatarioSelecionado.getPessoa()).put(novoEndereco.getIdEndereco(), DESCRICAO_PARTE);
				}
			}
			this.enderecoNovo = null;
			this.incluirComoEnderecoProcessual = false;
		} catch (PJeBusinessException e){
			facesMessages.add(Severity.ERROR, "Não foi possível gravar o endereço: [{0}].", e.getLocalizedMessage());
		} catch (PJeDAOException e){
			facesMessages.add(Severity.ERROR, "Não foi possível gravar o endereço: [{0}].", e.getLocalizedMessage());
		}
	}

	/**
	 * Define o CEP do endereço que está sendo criado.
	 * 
	 * @param cep o CEP utilizado para atribuição ao endereço.
	 */
	public void defineCep(Cep cep){
		if (cep != null){
			enderecoNovo.setNomeEstado(cep.getMunicipio().getEstado().getEstado());
			enderecoNovo.setNomeCidade(cep.getMunicipio().getMunicipio());
			enderecoNovo.setNomeLogradouro(cep.getNomeLogradouro());
			enderecoNovo.setNomeBairro(cep.getNomeBairro());
			enderecoNovo.setComplemento(cep.getComplemento());
		}
	}

	/**
	 * Carrega o processo judicial.
	 * 
	 * @return o processo judicial vinculado a esta atividade.
	 */
	private ProcessoTrf loadProcessoJudicial(){
		try{
			ProcessInstance processInstance = org.jboss.seam.bpm.ProcessInstance.instance();
			ProcessoJudicialManager processoJudicialManager = ComponentUtil.getComponent(ProcessoJudicialManager.class);
			return processoJudicialManager.findByProcessInstance(processInstance);
		} catch (PJeBusinessException e){
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar obter o processo judicial.");
		} catch (PJeDAOException e){
			facesMessages.add(Severity.ERROR, "Houve um erro de banco de dados ao tentar obter o processo judicial.");
		}
		return null;
	}

	/**
	 * Vincula um dado documento já existente ao expediente que está sendo editado.
	 * 
	 * @param doc o documento a ser vinculado.
	 */
	public void setDocumentoComunicacao(ProcessoDocumento doc){
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		atoComunicacaoService.copiaDadosDocumento(doc, this.processoExpediente.getProcessoDocumento());
		this.processoExpediente.setDocumentoExistente(true);
		this.processoExpediente.setProcessoDocumentoVinculadoExpediente(doc);
	}

	/**
	 * Obtém os meios de comunicação passíveis de utilização para uma dada pessoa. Para identificação dos meios passíveis, veja
	 * {@link AtoComunicacaoService#obtemMetodosComunicacao(Pessoa, ProcessoTrf, boolean)}
	 * 
	 * @param p a pessoa a respeito da qual se pretende identificar os meios de comunicação disponíveis
	 * @param pessoal marca indicadora de que se pretende identificar apenas os meios de comunicação diretos, isto é, de que não devem ser retornados
	 *            os meios de comunicação por interposta pessoa, como no caso de pessoa representada por advogado.
	 * @param procuradoria Procuradoria que representa a pessoa no processo.
	 * 
	 * @return a lista de meios de comunicação disponíveis
	 */
	private List<ExpedicaoExpedienteEnum> getMeiosComunicacao(Pessoa p, boolean pessoal, Procuradoria procuradoria){
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		List<ExpedicaoExpedienteEnum> ret = atoComunicacaoService.recuperarMeiosComunicacao(p, processoJudicial, pessoal, procuradoria);
		ret = filtrarMeiosDeExpedientes(ret);
		ret = filtarMeiosDeExpedientesPeloMapaDeFiltros(p, ret);
		return ret;
	}

	private List<ExpedicaoExpedienteEnum> filtrarMeiosDeExpedientes(List<ExpedicaoExpedienteEnum> ret){
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();

		// restringir os meios de comunicacoes
		Object o = taskInstance.getVariable(VAR_MEIOSCOMUNICACOES + taskInstance.getId());
		if (o != null && o instanceof String) {
			
			List<ExpedicaoExpedienteEnum> meiosPermitidos = new ArrayList<ExpedicaoExpedienteEnum>();
			String[] meiosSetadosNoFluxo = ((String) o).split(",");
			
			for (String meio : meiosSetadosNoFluxo){
				meio = meio.trim();
				try {
					ExpedicaoExpedienteEnum meioPermitido = ExpedicaoExpedienteEnum.valueOf(meio);
					if (ret.contains(meioPermitido)) {
						meiosPermitidos.add(meioPermitido);
					}
				} catch (Exception e) {
					// swallow
				}
			}
			return meiosPermitidos;
		} else {
			return ret;
		}
	}
	
	public void setMeiosComunicacao(String meios) {
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();
		if(taskInstance != null) {
			taskInstance.getContextInstance().deleteVariable(VAR_MEIOSCOMUNICACOES + taskInstance.getId());
			taskInstance.getContextInstance().setVariable(VAR_MEIOSCOMUNICACOES + taskInstance.getId(), meios);
		}
	}

	public List<ExpedicaoExpedienteEnum> atualizaMeiosComunicacao(TipoProcessoDocumento tipoDocumento, Pessoa pessoa) {
	    AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
	    boolean checkBoxPessoalMarcada = Boolean.TRUE.equals(mapaIntimacaoPessoal.get(pessoa));

	    List<ExpedicaoExpedienteEnum> meiosComunicacao = getMeiosComunicacao(pessoa);

	    boolean isTipoIntimacao = tipoDocumento != null && "Intimação".equalsIgnoreCase(tipoDocumento.getTipoProcessoDocumento());
	    boolean isDocumentoValido = isDocumentoValido(tipoDocumento);

	    ProcessoParte processoParte = atoComunicacaoService.getProcessoParte(pessoa, processoJudicial);

		boolean isHabilitadaDomicilio = habilitaDomicilio(pessoa);

		if (processoParte != null) {
			if (atoComunicacaoService.verificarPossibilidadeIntimacaoEletronica(processoParte, checkBoxPessoalMarcada, false, isTipoIntimacao)
					|| (isDocumentoValido && isHabilitadaDomicilio)) {
				if (!meiosComunicacao.contains(ExpedicaoExpedienteEnum.E)) {
					meiosComunicacao.add(ExpedicaoExpedienteEnum.E);
				}
			}
		}

	    boolean isPessoaFisica = TipoPessoaEnum.F.equals(pessoa.getInTipoPessoa());
	    boolean isPessoaJuridicaNaoPublico = TipoPessoaEnum.J.equals(pessoa.getInTipoPessoa()) && !((PessoaJuridica) pessoa).getOrgaoPublico();
	    boolean isPFouPJnaoPublicoHabilitada = (isPessoaFisica || isPessoaJuridicaNaoPublico) && isHabilitadaDomicilio;

	    if (isPFouPJnaoPublicoHabilitada && isDocumentoValido && !checkBoxPessoalMarcada) {
	        meiosComunicacao.remove(ExpedicaoExpedienteEnum.E);
	    }

	    return meiosComunicacao;
	}

	public boolean isOrgaoPublicoHabilitado(TipoProcessoDocumento tipoDocumento, Pessoa pessoa) {
	    if (pessoa == null) {
	        return false;
	    }

		boolean isOrgaoPublico = TipoPessoaEnum.J.equals(pessoa.getInTipoPessoa())
				&& ((PessoaJuridica) pessoa).getOrgaoPublico();

		boolean isHabilitadaDomicilio = habilitaDomicilio(pessoa);

	    boolean isDocumentoValido = isDocumentoValido(tipoDocumento);

		return isOrgaoPublico && isHabilitadaDomicilio && isDocumentoValido;
	}

	private boolean isDocumentoValido(TipoProcessoDocumento tipoDocumento) {
		if (tipoDocumento == null || tipoDocumento.getTipoProcessoDocumento() == null) {
			return false;
		}

		String docType = tipoDocumento.getTipoProcessoDocumento();

		return "Intimação".equalsIgnoreCase(docType) 
				|| "Citação".equalsIgnoreCase(docType)
				|| "Notificação".equalsIgnoreCase(docType);
	}

	/**
	 * Este método verifica se para a lista de destinatarios existe algum meio que exige a seleção de tipo de prazo de mandado (TipoPrazoCentralMandadosEnum)
	 * PJEII-20844 - quando se cria um expediente do tipo "Central de Mandado" deve ser mostrada a opção de selecionar a partir de quando será contado o prazo para a resposta.
	 * @param meio
	 * @return
	 */
	public boolean isExisteMeioQueExigeTipoPrazoMandado(){
		Map<DestinatarioComunicacao, ProcessoExpediente> mapDestinaExped = getDestinatariosPessoa();
		if( mapDestinaExped == null || mapDestinaExped.isEmpty() ){
			return false; 
		}
		for (Map.Entry<DestinatarioComunicacao, ProcessoExpediente> entry : mapDestinaExped.entrySet()) {
			if( entry.getValue() != null && ExpedicaoExpedienteEnum.M.equals( entry.getValue().getMeioExpedicaoExpediente() ) ){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Este método verifica se o tipo de expediente informado exige a seleção de tipo de prazo de mandado (TipoPrazoCentralMandadosEnum)
	 * PJEII-20844 - quando se cria um expediente do tipo "Central de Mandado" deve ser mostrada a opção de selecionar a partir de quando será contado o prazo para a resposta.
	 * @param meio
	 * @return
	 */
	public boolean isMeioExigeTipoPrazoMandado(ExpedicaoExpedienteEnum meio){
		return ExpedicaoExpedienteEnum.M.equals(meio);
	}
	
	public List<ExpedicaoExpedienteEnum> getMeiosComunicacao(Pessoa p){
		Boolean isIntimacaoPessoal = mapaIntimacaoPessoal.get(p);
		Procuradoria procuradoriaSelecionada = this.mapaProcuradoriaSelecionada.get(p);
		
		if (isIntimacaoPessoal != null){
			return getMeiosComunicacao(p, isIntimacaoPessoal, procuradoriaSelecionada);
		} else{
			return getMeiosComunicacao(p, false, procuradoriaSelecionada);
		}
	}

	/**
	 * Grava os dados existentes para reaproveitamento em nova execução.
	 * 
	 */
	public void consolidaAtosComunicacao(){
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();

		taskInstance.getContextInstance().setVariable(VAR_MAPASEXP + taskInstance.getId(), destinatarios);
		taskInstance.getContextInstance().setVariable(VAR_MAPAINSTR + taskInstance.getId(), mapaInstrumento);
		taskInstance.getContextInstance().setVariable(VAR_MAPAINTPESS + taskInstance.getId(), mapaIntimacaoPessoal);
		taskInstance.getContextInstance().setVariable(VAR_MAPAAGRUP + taskInstance.getId(), agrupamentos);
		taskInstance.getContextInstance().setVariable(VAR_UM_EXPED_POR_ENDERECO + taskInstance.getId(), mapaUmEnderecoPorExpediente);
		taskInstance.getContextInstance().setVariable(VAR_MAPA_PROCURADORIAS + taskInstance.getId(), mapaProcuradorias);
		taskInstance.getContextInstance().setVariable(VAR_MAPA_PROCURADORIA_SELECIONADA + taskInstance.getId(), mapaProcuradoriaSelecionada);
	}

	/**
	 * Esquece os dados existentes da execução atual.
	 * 
	 */
	private void limpaAtosComunicacaoConsolidados(){
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();
		taskInstance.getContextInstance().deleteVariable(VAR_MAPASEXP + taskInstance.getId());
		taskInstance.getContextInstance().deleteVariable(VAR_MAPAINSTR + taskInstance.getId());
		taskInstance.getContextInstance().deleteVariable(VAR_MAPAINTPESS + taskInstance.getId());
		taskInstance.getContextInstance().deleteVariable(VAR_MAPAAGRUP + taskInstance.getId());
		taskInstance.getContextInstance().deleteVariable(VAR_MEIOSCOMUNICACOES + taskInstance.getId());
		taskInstance.getContextInstance().deleteVariable(VAR_UM_EXPED_POR_ENDERECO + taskInstance.getId());
		taskInstance.getContextInstance().deleteVariable(VAR_MAPA_PROCURADORIAS + taskInstance.getId());
		taskInstance.getContextInstance().deleteVariable(VAR_MAPA_PROCURADORIA_SELECIONADA + taskInstance.getId());
	}
	
	/**
	 * Efetiva a vinculação de um determinadoo documento ao expediente em edição.
	 */
	public void processaInstrumento(){

		ProcessoDocumento pd = this.processoExpediente.getProcessoDocumento();
		modelo = null;
		DocumentoJudicialService documentoJudicialService = ComponentUtil.getComponent(DocumentoJudicialService.class);
		ProcessoDocumento novo = documentoJudicialService.getDocumento();
		novo.setTipoProcessoDocumento(pd.getTipoProcessoDocumento());
		switch (mapaInstrumento.get(destinatarioSelecionado)){
		case DP:
			this.processoExpediente.setProcessoDocumento(novo);
			this.processoExpediente.setDocumentoExistente(true);
			break;
		case DN:
			this.processoExpediente.setProcessoDocumento(novo);
			this.processoExpediente.setDocumentoExistente(false);
			break;
		default:
			break;
		}
	}

	/**
	 * Obtém os possíveis instrumentos de comunicação.
	 * 
	 * @return Lista contendo os possíveis instrumentos de comunicação.
	 */
	public List<InstrumentoComunicacao> getInstrumentosComunicacao(){
		List<InstrumentoComunicacao> ret = new ArrayList<PreparaAtoComunicacaoAction.InstrumentoComunicacao>(2);
		ret.add(InstrumentoComunicacao.DP);
		ret.add(InstrumentoComunicacao.DN);
		return ret;
	}
	
	/**
	 * Metodo responsavel por refazer a pesquisa de endereco a partir
	 * do filtro de cep e endereco.
	 */
	public void filtrarEnderecos(){
		cacheEnderecosPossiveis.clear();
		getEnderecosPossiveis(cepFiltro, enderecoCompletoFiltro);
	}
	
	/**
	 * Metodo responsavel por limpar os filtros de endereco
	 * quando o usuario clicar no icone para editar os enderecos.
	 * 
	 */
	private void limpaFiltrosEndereco() {
		this.cepFiltro = null;
		this.enderecoCompletoFiltro = null;
		cacheEnderecosPossiveis.clear();
	}

	/**
	 * Recupera o objeto Endereco a partir do ID
	 * passado por parametro.
	 * @param idEndereco
	 * @return
	 */
	public Endereco getEnderecoById(Integer idEndereco) {
		try {
			return ComponentUtil.getComponent(EnderecoManager.class).findById(idEndereco);
		} catch (PJeBusinessException e) {
			logger.error(e);
		}
		return null;
	}
	
	/**
	 * Metodo responsavel por verificar se o endereco passado por parametro
	 * satisfaz as condicoes dos filtros (quando esses forem passados).
	 * @param endereco
	 * @param cepFiltro
	 * @param enderecoCompletoFiltro
	 * @return
	 */
	private boolean isEnderecoDaParteNoFiltro(Endereco endereco, String cepFiltro, String enderecoCompletoFiltro) {
		if(endereco == null) return false;

		if(cepFiltro == null && enderecoCompletoFiltro == null) {
			return true;
		}else {
			return	(cepFiltro != null && endereco.getCep() != null && endereco.getCep().getNumeroCep().equals(cepFiltro)) || 
					(enderecoCompletoFiltro != null && endereco.getEnderecoCompleto().contains(enderecoCompletoFiltro))	;
		}
	}
	
	/**
	 * Chama o metodo para obter a lista de enderecos possiveis de um 
	 * determinado destinatario.
	 * @return
	 */
	public List<Integer> getEnderecosPossiveis(){
		return getEnderecosPossiveis(null, null);
	}
	
	/**
	 * Obtém a lista de endereços possíveis para um determinado destinatário.
	 * 
	 * @return a lista de endereços possíveis para um dado destinatário.
	 */
	public List<Integer> getEnderecosPossiveis(String cepFiltro, String enderecoCompletoFiltro){
		this.destinatarioSelecionado.setPessoa((Pessoa)HibernateUtil.getSession().merge(this.destinatarioSelecionado.getPessoa())); 
		Pessoa pessoaSelecionada = this.destinatarioSelecionado.getPessoa();
		
		if (this.cacheEnderecosPossiveis.containsKey(pessoaSelecionada)) {
			return this.cacheEnderecosPossiveis.get(pessoaSelecionada);
		}
		
		Set<Integer> enderecosPossiveis = new HashSet<>(0);
		this.enderecoIdDescricao = new HashMap<>(0);

		List<Integer> idsEndereco = ComponentUtil.getComponent(EnderecoManager.class).
				getIdsEnderecosUnicosPeloEnderecoCompleto(pessoaSelecionada.getIdPessoa(), cepFiltro, enderecoCompletoFiltro);

		for (Integer idEndereco : idsEndereco) {
			adicionarEnderecoPossivelEADescricaoCorrespondente(enderecosPossiveis, idEndereco, DESCRICAO_PARTE);
		}
		
		
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		ProcessoParte processoParte = atoComunicacaoService.getProcessoParte(pessoaSelecionada, this.processoJudicial);
		if (processoParte != null) {
			adicionarEnderecosPartes(cepFiltro, enderecoCompletoFiltro, enderecosPossiveis, processoParte);

			if (!this.mapaIntimacaoPessoal.get(pessoaSelecionada)) {
				ProcessoJudicialManager processoJudicialManager = ComponentUtil.getComponent(ProcessoJudicialManager.class);
				List<ProcessoParteRepresentante> advogados = processoJudicialManager.getAdvogados(processoParte);
				if (advogados.size() > 0) {
					this.mapaAdvogados.put(pessoaSelecionada, advogados);
					adicionarEnderecosAdvogados(cepFiltro, enderecoCompletoFiltro, enderecosPossiveis, advogados);
				}
			}
		}
		if (!this.mapaIntimacaoPessoal.get(pessoaSelecionada) && this.mapaProcuradoriaSelecionada.get(pessoaSelecionada) != null) {
			Procuradoria procuradoria = this.mapaProcuradoriaSelecionada.get(pessoaSelecionada);
			Endereco enderecoOrgaoRepresentacao = ((Localizacao)HibernateUtil.getSession().merge(procuradoria.getLocalizacao())).getEndereco();

			adicionarEnderecoCasoPresenteNosFiltros(enderecoOrgaoRepresentacao, cepFiltro, enderecoCompletoFiltro, DESCRICAO_ORGAO_DE_REPRESENTACAO, enderecosPossiveis);
		}

		List<Integer> list = new ArrayList<>(enderecosPossiveis);
		this.cacheEnderecosPossiveis.put(pessoaSelecionada, list);
		this.cacheEnderecosPossiveisDescricao.put(pessoaSelecionada, this.enderecoIdDescricao);

		return list;
	}

	private void adicionarEnderecosPartes(String cepFiltro, String enderecoCompletoFiltro, Set<Integer> enderecosPossiveis, ProcessoParte processoParte) {
		for (ProcessoParteEndereco processoParteEndereco : processoParte.getProcessoParteEnderecoList()) {
			adicionarEnderecoCasoPresenteNosFiltros(processoParteEndereco.getEndereco(), cepFiltro, enderecoCompletoFiltro, DESCRICAO_ADVOGADO, enderecosPossiveis);
		}
	}

	private void adicionarEnderecosAdvogados(String cepFiltro, String enderecoCompletoFiltro, Set<Integer> enderecosPossiveis, List<ProcessoParteRepresentante> advogados) {
		for (ProcessoParteRepresentante advogado : advogados) {
			for (ProcessoParteEndereco processoParteEndereco : advogado.getParteRepresentante().getProcessoParteEnderecoList()) {
				adicionarEnderecoCasoPresenteNosFiltros(processoParteEndereco.getEndereco(), cepFiltro, enderecoCompletoFiltro, DESCRICAO_ADVOGADO, enderecosPossiveis);
			}
			for (Endereco endereco : advogado.getRepresentante().getEnderecoList()) {
				adicionarEnderecoCasoPresenteNosFiltros(endereco, cepFiltro, enderecoCompletoFiltro, DESCRICAO_ADVOGADO, enderecosPossiveis);
			}
		}
	}

	private void adicionarEnderecoCasoPresenteNosFiltros(Endereco endereco, String cepFiltro, String enderecoCompletoFiltro, String descricao, Set<Integer> enderecosPossiveis ) {
		if(isEnderecoDaParteNoFiltro(endereco, cepFiltro, enderecoCompletoFiltro)){
			adicionarEnderecoPossivelEADescricaoCorrespondente(enderecosPossiveis, endereco.getIdEndereco(), descricao);
		}
	}

	private void adicionarEnderecoPossivelEADescricaoCorrespondente(Set<Integer> enderecosPossiveis, int processoParteEndereco, String descricao) {
		enderecosPossiveis.add(processoParteEndereco);
		this.enderecoIdDescricao.put(processoParteEndereco, descricao);
	}

	/**
	 * Obtém o {@link ProcessoExpediente} que está atualmente sendo editado.
	 * 
	 * @return o ato de comunicação judicial que está sendo preparado.
	 */
	public ProcessoExpediente getProcessoExpediente(){
		return this.processoExpediente;
	}

	/**
	 * Atribui à variável processoExpediente um determinado ato de comunicação judicial para edição.
	 * 
	 * @param processoExpediente o ato a ser vinculado para edição.
	 */
	public void setProcessoExpediente(ProcessoExpediente processoExpediente){
		this.processoExpediente = processoExpediente;
	}

	/**
	 * Obtém os tipos de documentos disponíveis para prática nesse nó.
	 * 
	 * @return a lista de tipos de documento ({@link TipoProcessoDocumento}) que podem ser criados neste nó.
	 */
	public List<TipoProcessoDocumento> getTiposDocumentosDisponiveis(){
		if (tiposDocumentosDisponiveis == null || tiposDocumentosDisponiveis.isEmpty()) {
			tiposDocumentosDisponiveis = ComponentUtil.getComponent(DocumentoJudicialService.class).getTiposDocumentoMinuta();
		}
		return tiposDocumentosDisponiveis;
	}

	/**
	 * Atribui uma lista de tipos de documento como passíveis de criação neste nó.
	 * 
	 * @param tiposDocumentosDisponiveis a lista de tipos de documento passiveis de criação a ser atribuída.
	 * 
	 */
	public void setTiposDocumentosDisponiveis(List<TipoProcessoDocumento> tiposDocumentosDisponiveis){
		this.tiposDocumentosDisponiveis = tiposDocumentosDisponiveis;
	}

	/**
	 * Define o próximo passo em uma sequência de atos.
	 */
	public void proximoPasso(){
		switch (this.passo){
		
		case 0: //Aba Escolha dos destinatários
			limpaEnderecoDestinatarioNaoFisico();
			corrigeExpedientesComEndereco();
			
			if (validarSelecaoProcuradoria()) {
				this.passo = 2;
			} else {
				break;
			}
			if (getDestinatariosFisicos().size() > 0) {
				verificarPreSelecaoEndereco();
				this.passo = 1;									
			} else if(!haDestinatarios()){
				facesMessages.clear();
				facesMessages.add(Severity.ERROR, "Deve ser selecionado ao menos um destinatário");
			}
			inicializarProximoPasso();
			break;
			
		case 1: //Aba Definição dos endereços
			verificaUmExpedientePorEndereco();
			if (hasEnderecoFisicoSelecionado()) {
				this.passo = 2;
			} else {
				facesMessages.clear();
				facesMessages.add(Severity.ERROR, "Deve ser selecionado ao menos um endereço para cada destinatário");
			}
			inicializarProximoPasso();
			break;
			
		case 2: //Aba Preparar ato 
			
			// se não validar os caracteres no editor de texto, abre uma modal de confirmação de exclusão de caracteres indevidos sem entrar no passo 3
			if (!validaCaracteresEspeciaisEditorTexto()){
				anexarDocumentos.setFlagMostraModalRemocaoCaracteresEspeciais(true);
				break;
			} 
			
			if ((validaAtosDeComunicacao()) && (getAssinaturas() != null)) {
				this.passo = 3; //Aba Escolher documentos vinculados e finalizar
				
			}
			
			inicializarProximoPasso();
			break;
		
		default:
			inicializarProximoPasso();
			this.passo += 1;
			break;
		}
	}
	
	/**
	 * Valida se há necessidade de selecão de procuradoria para algum
	 * destinatário. Se o destinário possuir somente uma procuradoria então não
	 * há necessidade, tendo mais de uma é preciso escolher.
	 * 
	 * @return <code>true</code> se todo os destinatários que devem selecionar
	 *         procuradoria o fizeram e <code>false</code> caso algum não o
	 *         tenha feito.
	 */
	public boolean validarSelecaoProcuradoria() {
		if (haDestinatarios() && mapaProcuradorias != null && !mapaProcuradorias.isEmpty()) {
			Set<DestinatarioComunicacao> setDestinatarios = destinatarios.keySet();
			for (DestinatarioComunicacao destinatario : setDestinatarios) {
				if (!mapaIntimacaoPessoal.get(destinatario.getPessoa()) && 
						mapaProcuradorias.containsKey(destinatario.getPessoa()) && 
						mapaProcuradorias.get(destinatario.getPessoa()).size() > 1 
						&& (mapaProcuradoriaSelecionada == null || !mapaProcuradoriaSelecionada.containsKey(destinatario.getPessoa()))) {					
					facesMessages.clear();
					facesMessages.add(Severity.ERROR, "Deve ser selecionada uma procuradoria para " + destinatario.getPessoa().getNome());
					return false;
				}
			}			
		} 
		return true;
	}

	/**
	 * Método refatorado do proximoPasso(), para não ser mais chamado na primeira linha do método, pois no passo 2
	 * foi criado uma validação de caracteres especiais. Com as duas variáveis nulas, não renderiza certo a div do editor. 
	 */
	private void inicializarProximoPasso() {
		this.processoExpediente = null;
		this.destinatarioSelecionado = null;		
		this.consolidaAtosComunicacao();
	}


	/**
	 * Validação de caracteres especiais no editor de texto. Caso exista, o sistema exibirá um modal 
	 * de confirmação de exclusão de caracteres não permitidos.
	 *  
	 * @return boolean
	 */
	private boolean validaCaracteresEspeciaisEditorTexto() {
		ProcessoExpediente pe = getProcessoExpediente();
		String editorTexto = "";
		if (pe != null) {
			editorTexto = pe.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
			if (editorTexto != null) {
				if(!Util.isStringSemCaracterUnicode(editorTexto)) {
					anexarDocumentos.setFlagMostraModalRemocaoCaracteresEspeciais(true);
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Remove os caracteres do editor de texto que não são convertidos ao formato ISO8859-1, 
	 * se o usuario confirmar a exclusão em um modal panel, e atualiza o valor no editor de texto. 
	 */
	public void excluiCaracteresEspeciaisEditorTexto() {
		String texto = this.processoExpediente.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
		for (int i=0; i< texto.length(); i++) {
			if (!Util.isStringSemCaracterUnicode(Character.toString(texto.charAt(i)))) {
				texto = StringUtils.replace(texto, Character.toString(texto.charAt(i)), "");
				i--;
			}
		}
		
		this.processoExpediente.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(texto);
	}

	/**
	 * Define o passo anterior em uma sequência de atos.
	 */
	public void passoAnterior(){
		this.processoExpediente = null;
		this.destinatarioSelecionado = null;
		switch (this.passo){
			case 2:
				if (getDestinatariosFisicos().size() > 0){
					this.passo = 1;
				} else {
					this.passo = 0;
				}
				break;
			default:
				this.passo -= 1;
				break;
		}
		this.consolidaAtosComunicacao();
	}

	/**
	 * Apresente indicação textual do prazo conferido a um destinatário.
	 * 
	 * @return texto contendo o prazo conferido a um destinatário.
	 */
	public String getPrazo(){
		return getPrazo(destinatarioSelecionado);
	}
	
	/**
	 * Apresente indicação textual do prazo conferido a um destinatário.
	 * 
	 * @return texto contendo o prazo conferido a um destinatário.
	 */
	public String getPrazo(DestinatarioComunicacao destinatarioComunicacao){
		ProcessoExpediente pe = destinatarios.get(destinatarioComunicacao);
		if (pe == null){
			return "";
		}
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		ProcessoParteExpediente ppe = atoComunicacaoService.getAtoPessoal(pe, destinatarioComunicacao.getPessoa(), 
				mapaProcuradoriaSelecionada.get(destinatarioComunicacao.getPessoa()));
		StringBuilder sb = new StringBuilder();
		switch (ppe.getTipoPrazo()){
		case A:
			sb.append(ppe.getPrazoLegal());
			sb.append(" anos");
			break;
		case M:
			sb.append(ppe.getPrazoLegal());
			sb.append(" meses");
			break;
		case D:
			sb.append(ppe.getPrazoLegal());
			sb.append(" dias");
			break;
		case H:
			sb.append(ppe.getPrazoLegal());
			sb.append(" horas");
			break;
		case N:
			sb.append(ppe.getPrazoLegal());
			sb.append(" minutos");
			break;
		case C:
			Locale ptBR = new Locale("pt", "BR");
			DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, ptBR);
			sb.append("até ");
			sb.append(dateFormat.toString().toLowerCase());
			break;			
		case S:
			sb.append("sem prazo");
			break;
		default:
			break;
		}
		return sb.toString();
	}

	/**
	 * Obtém os endereços escolhidos para um destinatário selecionado.
	 * 
	 * @return String HTML contendo a lista de endereços escolhidos para o destinatário selecionado.
	 */
	public String getEnderecosDestino(){
		return getEnderecosDestino(destinatarioSelecionado);
	}
	
	/**
	 * Obtém os endereços escolhidos para um destinatário selecionado.
	 * 
	 * @return String HTML contendo a lista de endereços escolhidos para o destinatário selecionado.
	 */
	public String getEnderecosDestino(DestinatarioComunicacao destinatarioComunicacao){
		ProcessoExpediente pe = destinatarios.get(destinatarioComunicacao);
		if (pe == null){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		EnderecoService enderecoService = ComponentUtil.getComponent(EnderecoService.class);
		ProcessoParteManager processoParteManager = (ProcessoParteManager) Component.getInstance("processoParteManager");
		for (ProcessoParteExpediente ppe : pe.getProcessoParteExpedienteList()){
			if (sb.length() > 0) {
				sb.append("<br/>");
			}
			sb.append(
					processoParteManager
							.recuperaNomeUsadoNoProcesso(
									this.processoJudicial == null ? ppe.getProcessoJudicial().getIdProcessoTrf()
											: this.processoJudicial.getIdProcessoTrf(),
									ppe.getPessoaParte().getIdPessoa()));
			sb.append("<br/>");
			List<Endereco> enderecos = ppe.getEnderecos();
			if (enderecos != null && !enderecos.isEmpty()) {
				for (int i = 0; i < enderecos.size(); i++){
					Endereco e = enderecos.get(i);
					e = enderecoService.getEndereco(e.getIdEndereco());
					sb.append(e.getEnderecoCompleto());
					if (i < enderecos.size() - 1) {
						sb.append("; ");
					}
				}
				sb.append("<br/>");
			}
		}
		return sb.toString();
	}
	
	// Métodos utilizados em modelos do PAC
	
	/**
	 * @author Ronny Paterson (ronny.silva@trt8.jus.br)
	 * @category PJE-JT
	 * @return retorna uma string contendo o prazo do expediente
	 * para a parte
	 */
	public String getPartePrazoList() {
		StringBuilder partePrazo = new StringBuilder();

		for (DestinatarioComunicacao destinatarioComunicacao : this.destinatarios.keySet()) {
			if(destinatarioComunicacao.getPessoa().equals(destinatarioSelecionado.getPessoa())) {
				partePrazo.append(getPrazo(destinatarioComunicacao));				
			}
		}

		return partePrazo.toString();
	}
		
	/**
	 * @author Ronny Paterson (ronny.silva@trt8.jus.br)
	 * @since 1.4.4
	 * @category PJE-JT
	 * @return Nome do réu selecionado pelo usuário.
	 */	
	public String getNomeReuAtual(){
		StringBuilder nomeReuAtual = new StringBuilder();
		List<ProcessoParte> partes = processoJudicial.getListaPartePrincipal(ProcessoParteParticipacaoEnum.P);
		for (ProcessoParte parte : partes){
			for (DestinatarioComunicacao destinatarioComunicacao : this.destinatarios.keySet()) {
				if (destinatarioComunicacao.getPessoa().getIdUsuario() != null && 
						parte.getPessoa().getIdUsuario().intValue() == destinatarioComunicacao.getPessoa().getIdUsuario().intValue() &&
						destinatarioComunicacao.getPessoa().equals(destinatarioSelecionado.getPessoa())){
					nomeReuAtual.append(parte.toString());
					nomeReuAtual.append("<br />");
					break;
				}
			}
		}
		return nomeReuAtual.toString();
	}
		
	/**
	* @return string de nome e endereço das partes do expediente do destinatário selecionado
	*/
	public String getNomeEnderecoPartesSelecionadas() {
		if (this.destinatarioSelecionado != null) {
			return getEnderecosDestino(this.destinatarioSelecionado);
		} 
		else {
			return "";
		}
	}

	/**
	 * Vincula a lista de documentos selecionados pelo usuário a cada um dos expedientes em elaboração.
	 */
	public void vincularDocumentos(){
		Set<ProcessoDocumento> documentosVinculados = new HashSet<ProcessoDocumento>();
		numeroDocumentosVinculados = 0;
		DocumentoJudicialService documentoJudicialService = ComponentUtil.getComponent(DocumentoJudicialService.class);
		for (ProcessoDocumento pd : documentosSelecionados.keySet()){
			if (documentosSelecionados.get(pd)){
				ProcessoDocumento aux;
				try{
					aux = documentoJudicialService.getDocumento(pd.getIdProcessoDocumento());
					if(aux != null && aux.getProcessoTrf().getIdProcessoTrf() == processoJudicial.getIdProcessoTrf()) {
						aux.setSelected(true);
					}					
					documentosVinculados.add(aux);
					numeroDocumentosVinculados++;
				} catch (PJeBusinessException e){
					logger.debug("Não foi possível recuperar o documento com id {0}. {1}", pd.getIdProcessoDocumento(),
							e.getLocalizedMessage());
				}
			}
		}
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		for (ProcessoExpediente pe : destinatarios.values()){
			atoComunicacaoService.vincularDocumentos(pe, documentosVinculados);
		}
	}

	/**
	 * Verifica se um determinado expediente já é válido para conclusão da atividade de criação da comunicação.
	 * 
	 * @param pe o expediente sob análise
	 * @param destinatario o destinatário respectivo.
	 * @return true, se o expediente já estiver apto a servir como comunicação
	 * 
	 * @see AtoComunicacaoService#validaExpediente(ProcessoExpediente, Pessoa)
	 */
	public boolean validaExpediente(ProcessoExpediente pe, Pessoa destinatario){
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		return atoComunicacaoService.validaExpediente(pe, destinatario, mapaProcuradoriaSelecionada.get(destinatario));
	}

	/**
	 * Verifica se uma determinada comunicação pode ser assinada pelo usuário atual.
	 * 
	 * @param pe a comunicação a ser analisada
	 * @return true, se a comunicação pode ser assinada pelo usuário atual
	 * 
	 * @see AtoComunicacaoService#possivelSignatario(ProcessoExpediente)
	 */
	public boolean possivelSignatario(ProcessoExpediente pe){
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		return atoComunicacaoService.possivelSignatario(pe);
	}

	/**
	 * Verifica se uma determinada comunicação ainda exige assinatura de um determinado usuário.
	 * 
	 * @param pe a comunicação a ser analisada
	 * @return true, se a comunicação ainda exige uma assinatura.
	 * 
	 * @see AtoComunicacaoService#exigeAssinatura(ProcessoExpediente)
	 */
	public boolean exigeAssinatura(ProcessoExpediente pe){
		TipoProcessoDocumento tipoProcessoDocumento = pe.getProcessoDocumento().getTipoProcessoDocumento();
		pe.getProcessoDocumento().setTipoProcessoDocumento((TipoProcessoDocumento) HibernateUtil.getSession().merge(tipoProcessoDocumento));
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		return atoComunicacaoService.exigeAssinatura(pe);
	}
	
	/**
 	 * Verifica se botao assinar pode ser apresentado.
 	 * 
 	 * @return true se apresenta botao assinar
 	 */
 	public boolean isApresentaBotaoAssinar() {
 		Boolean retorno = true;
 		TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService = ComponentUtil.getComponent(TipoProcessoDocumentoPapelService.class);
 		for (ProcessoExpediente pe : getDestinatarios().values() ) {
 			if (tipoProcessoDocumentoPapelService.verificarExigibilidadeNaoAssina(
 					Authenticator.getPapelAtual(), 
 					pe.getProcessoDocumento().getTipoProcessoDocumento())){
 				retorno = false;
 				break;
 			} else {
 				if(!ComponentUtil.getAtoComunicacaoService().verificarServicoDisponivel(pe.getMeioExpedicaoExpediente())) {
 					mensagemIndisponibilidade = "O meio de comunicação " + pe.getMeioExpedicaoExpediente().getLabel() + "  está indisponível no momento. Utilize outro meio de comunicação ou tente novamente mais tarde.";
 					retorno = false;
 					break;
 				}
 			}
 		}
 		return retorno;
	}
	
	public boolean exibirBotaoProximo(){

		//retornar true:
		//caso o usuario tenha editado todos os documentos 
		for(DestinatarioComunicacao destinatarioComunicacao : destinatarios.keySet()){
			if(!validaExpediente(destinatarios.get(destinatarioComunicacao), destinatarioComunicacao.getPessoa())){
				return false;
			}
			
		}
		return true;
	}

	/**
	 * Obtém o identificador do momento de execução em curso.
	 * 
	 * @return o passo da atividade do caso de uso.
	 */
	public Integer getPasso(){
		return passo;
	}

	/**
	 * Atribui à ação a identificação do passo em que ela se encontra no caso de uso.
	 * 
	 * @param passo identificador do momento da execução do caso de uso.
	 */
	public void setPasso(Integer passo){
		this.destinatarioSelecionado = null;
		this.processoExpediente = null;
		this.passo = passo;
		
 		if(!haDestinatarios()) {
			facesMessages.clear();
			if (this.passo == 1) {
				facesMessages.add(Severity.ERROR, "Deve ser selecionado ao menos um destinatário para escolher um endereço.");
			} else if (this.passo == 2) {
				facesMessages.add(Severity.ERROR, "Deve ser selecionado ao menos um destinatário para preparar um ato de comunicação.");
			} else if (this.passo == 3) {
				facesMessages.add(Severity.ERROR, "Deve ser selecionado ao menos um destinatário para preparar escolher documentos e finalizar.");
			}
			this.passo = 0;
		
		} else if (getDestinatariosFisicos().size() > 0) {
			if (this.passo == 2 || this.passo == 3) {
				if (!hasEnderecoFisicoSelecionado()) {
					facesMessages.clear();
					facesMessages.add(Severity.ERROR, "Deve ser selecionado ao menos um endereço para cada destinatário na aba 'Definição dos endereços'");
					this.passo = 1;
				}
			}	
		}
 		
 		if(this.passo == 2){
 			verificaUmExpedientePorEndereco();
 		}else if (this.passo == 3) {
			if (validaAtosDeComunicacao()) {
				getAssinaturas();
			}
		}
		
		this.consolidaAtosComunicacao();
	}

	/**
	 * Obtém um {@link DocumentoJudicialDataModel} que permite a manipulação de grande quantidade de documentos com paginação e uso de cache de
	 * memória.
	 * 
	 * @return o modelo de dados de documentos judiciais a ser utilizado em tabelas.
	 */
	public DocumentoJudicialDataModel getDataModel(){
		return dataModel;
	}

	/**
	 * Atribui um {@link DocumentoJudicialDataModel} que permite a manipulação de grande quantidade de documentos com paginação e uso de cache de
	 * memória.
	 * 
	 * @param dataModel o modelo de dados de documentos judiciais a ser atribuído.
	 */
	public void setDataModel(DocumentoJudicialDataModel dataModel){
		this.dataModel = dataModel;
	}

	/**
	 * Obtém um mapa contendo o conjunto de documentos de trabalho, com o valor constituindo a marca de sua inclusão na lista.
	 * 
	 * @return mapa contendo os documentos do processo, marcados com valor verdadeiros quando tiverem sido selecionados.
	 */
	public Map<ProcessoDocumento, Boolean> getDocumentosSelecionados(){
		return documentosSelecionados;
	}

	/**
	 * Atribui um mapa contendo o conjunto de documentos de trabalho, com o valor constituindo a marca de sua inclusão na lista.
	 * 
	 * @param documentosSelecionados mapa contendo os documentos do processo, marcados com valor verdadeiros quando tiverem sido selecionados.
	 * 
	 */
	public void setDocumentosSelecionados(Map<ProcessoDocumento, Boolean> documentosSelecionados){
		this.documentosSelecionados = documentosSelecionados;
	}

	/**
	 * Identifica se o ato de comunicação em elaboração tem um documento editável.
	 * 
	 * @return true, se o documento vinculado ao ato de comunicação é editável.
	 */
	public Boolean getDocumentoEditavel(){
		return documentoEditavel;
	}

	/**
	 * Obtém o modelo de documento a ser atribuído a um ato de comunicação.
	 * 
	 * @return o modelo de documento escolhido.
	 */
	public ModeloDocumento getModelo(){
		return modelo;
	}

	/**
	 * Atribui um modelo de documento a um ato de comunicação.
	 * 
	 * @param modelo o modelo a ser atribuído
	 */
	public void setModelo(ModeloDocumento modelo){
		this.modelo = modelo;
	}

	/**
	 * Obtém um expediente pessoal relativo a uma pessoa dada.
	 * 
	 * @param p destinatário já existente cuja intimação pessoal se pretende obter
	 * @return {@link ProcessoParteExpediente} vinculado ao destinatário dado
	 * 
	 * @see AtoComunicacaoService#getAtoPessoal(ProcessoExpediente, Pessoa)
	 */
	public ProcessoParteExpediente getExpedientePessoal(Pessoa p){
		DestinatarioComunicacao destinatario = obterDestinatario(p, null);
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		ProcessoParteExpediente ppe = atoComunicacaoService.getAtoPessoal(destinatarios.get(destinatario), p, mapaProcuradoriaSelecionada.get(p));
		
		if(mapaProcuradoriaSelecionada.containsKey(p) && mapaProcuradoriaSelecionada.get(p) != null && procuradoriaSelecionada != null){
			ppe.setProcuradoria(mapaProcuradoriaSelecionada.get(p));
		} else if(!mapaProcuradoriaSelecionada.containsKey(p) && mapaProcuradorias.containsKey(p) && mapaProcuradorias.get(p) != null && mapaProcuradorias.get(p).size() > 0){
			ppe.setProcuradoria(mapaProcuradorias.get(p).get(0));
		}
		
		return ppe;
	}

	/**
	 * Obtém um expediente pessoal relativo a uma pessoa dada.
	 * 
	 * @return {@link ProcessoParteExpediente} vinculado ao destinatário dado
	 * 
	 * @see AtoComunicacaoService#getAtoPessoal(ProcessoExpediente, Pessoa)
	 */
	public ProcessoParteExpediente getExpedientePessoal(ProcessoParte pp){
		Pessoa p = pp.getPessoa();
		DestinatarioComunicacao destinatario = obterDestinatario(p, null);
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		ProcessoParteExpediente ppe = atoComunicacaoService.getAtoPessoal(destinatarios.get(destinatario), pp);
		
		if(mapaProcuradoriaSelecionada.containsKey(p) && mapaProcuradoriaSelecionada.get(p) != null && procuradoriaSelecionada != null){
			ppe.setProcuradoria(mapaProcuradoriaSelecionada.get(p));
		} else if(!mapaProcuradoriaSelecionada.containsKey(p) && mapaProcuradorias.containsKey(p) && mapaProcuradorias.get(p) != null && mapaProcuradorias.get(p).size() > 0){
			ppe.setProcuradoria(mapaProcuradorias.get(p).get(0));
		}
		
		return ppe;
	}

	/**
	 * Obtém os tipos de prazo da central de mandados disponíveis para seleção pelo usuário.
	 * 
	 * @return sequência de tipos de prazo central de mandados disponíveis
	 */
	public List<TipoCalculoMeioComunicacaoEnum> obtemTiposPrazoCentralMandado(){
		return Arrays.asList(TipoCalculoMeioComunicacaoEnum.values());
	}
	
	/**
	 * Obtém os tipos de prazo disponíveis para seleção pelo usuário.
	 * 
	 * @return sequência de tipos de prazo disponíveis
	 */
	public TipoPrazoEnum[] obtemTiposPrazo(){
		TipoPrazoEnum[] prazos =  TipoPrazoEnum.values();
		TramitacaoProcessualService tramitacaoProcessualService = ComponentUtil.getComponent(TramitacaoProcessualImpl.class);
		Boolean habilitaDataCertaPac = null;
		Object variavel = tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.VARIAVEL_HABILITAR_DATA_CERTA_PAC);
		
		try{
			habilitaDataCertaPac = (Boolean) variavel;
		}catch(ClassCastException e ) {
			habilitaDataCertaPac = (Boolean.valueOf((String) variavel));
		}
		
		if(BooleanUtils.isNotTrue(habilitaDataCertaPac)){
			return (TipoPrazoEnum[]) ArrayUtils.removeElement(prazos, TipoPrazoEnum.C);
		}		
		return prazos;
	}
	
	/**
	 * Substitui o documento vinculado ao ato de comunicação em edição por aquele resultado da adoção do modelo atribuído em
	 * {@link #setModelo(ModeloDocumento)}.
	 */
	public void substituirModelo(){
		DocumentoJudicialService documentoJudicialService = ComponentUtil.getComponent(DocumentoJudicialService.class);
		this.processoExpediente.getProcessoDocumento().getProcessoDocumentoBin()
				.setModeloDocumento(documentoJudicialService.processaConteudo(modelo));
	}

	/**
	 * Obtém o mapa de destinatários dos atos de comunicação, com seus respectivos atos de comunicação vinculados.
	 * 
	 * @return os destinatários dos atos de comunicação, com seus respectivos atos.
	 */
	public Map<DestinatarioComunicacao, ProcessoExpediente> getDestinatarios(){
		return destinatarios;
	}

	/**
	 * Atribui um mapa de destinatários de ato de comunicação.
	 * 
	 * @param destinatarios mapa de destinatários, com seus respectivos atos de comunicação
	 * @see ProcessoExpediente
	 */
	public void setDestinatarios(Map<DestinatarioComunicacao, ProcessoExpediente> destinatarios){
		this.destinatarios = destinatarios;
	}

	/**
	 * Entidade destinada a permitir que se identifique a que pessoa uma outra pessoa deve ser agrupada. Necessária para a camada de visão.
	 * 
	 * @return O mapa Agrupado -> Agrupador.
	 */
	public Map<DestinatarioComunicacao, DestinatarioComunicacao> getMapaAgrupadoAgrupador(){
		return this.mapaAgrupadoAgrupador;
	}
	
	public Map<DestinatarioComunicacao, String> getDestinatarioAgrupadoIdAgrupador() {
		return this.destinatarioAgrupadoIdAgrupador;
	}

	/**
	 * Obtém um destinatário selecionado para edição.
	 * 
	 * @return o destinatário selecionado para edição
	 */
	public DestinatarioComunicacao getDestinatarioSelecionado(){
		return destinatarioSelecionado;
	}
	
	/**
	 * Obtém o mapa de destinatários dos atos de comunicação, com seus respectivos atos de comunicação vinculados.
	 * 
	 * @return os destinatários dos atos de comunicação, com seus respectivos atos.
	 */	
	public Map<DestinatarioComunicacao, ProcessoExpediente> getDestinatarios(Boolean isMostrarEditaveis) {
		Map<DestinatarioComunicacao, ProcessoExpediente> destinatarioComEndereco = new HashMap<DestinatarioComunicacao, ProcessoExpediente>(0);
		for (DestinatarioComunicacao destinatario: destinatarios.keySet() ) {
			if (isMostrarEditaveis) {
				if (isDestinatarioUtilizado(destinatario)){ 
					continue;
				}
			}
			destinatarioComEndereco.put(destinatario, destinatarios.get(destinatario));
		}			
		return destinatarioComEndereco;
	}	

	/**
	 * Seta uma pessa como destinatario selecionado, convertendo antes para
	 * DestinatarioComunicação.
	 * 
	 * @param pessoa
	 * 
	 */
	public void setDestinatarioSelecionadoPessoa(Pessoa pessoa) {
		DestinatarioComunicacao destinatarioSelecionado = new DestinatarioComunicacao(pessoa, null);
		setDestinatarioSelecionado(destinatarioSelecionado);			
	}	
	
	/**
	 * Seta uma pessa como destinatario selecionado, convertendo antes para
	 * DestinatarioComunicação.
	 * 
	 * @param pessoa
	 * 
	 * @param procuradoriaDaPessoa
	 *            a procuradoria que foi selecionada no momento do
	 *            protocolamento do processo, caso exista. Será a procuradoria
	 *            padrão, caso a pessoa possua mais de uma representação e ela
	 *            ainda esteja ativa. Pode ser nulo.
	 */
	public void setDestinatarioSelecionadoPessoa(Pessoa pessoa, Procuradoria procuradoriaDaPessoa) {
		DestinatarioComunicacao destinatarioSelecionado = new DestinatarioComunicacao(pessoa, null);
		setDestinatarioSelecionado(destinatarioSelecionado, procuradoriaDaPessoa);			
	}	
	
	/**
	 * Obtém o mapa de endereços selecionados, utilizando como chave o destinatário.
	 * 
	 * @return {@link Map} de endereços utilizando como chave o destinatário
	 */
	public Map<Pessoa, Set<Endereco>> getMapaEnderecos(){
		return mapaEnderecos;
	}

	/**
	 * Atribui um mapa de endereços selecionados, utilizando como chave o destinatário.
	 * 
	 * @param mapaEnderecos {@link Map} de endereços a ser atribuído
	 */
	public void setMapaEnderecos(Map<Pessoa, Set<Endereco>> mapaEnderecos){
		this.mapaEnderecos = mapaEnderecos;
	}

	/**
	 * Obtém o endereço novo em edição.
	 * 
	 * @return o {@link Endereco} novo sob edição
	 */
	public Endereco getEnderecoNovo(){
		return enderecoNovo;
	}

	/**
	 * Atribui um endereço ao campo enderecoNovo.
	 * 
	 * @param enderecoNovo o {@link Endereco} a ser atribuído.
	 */
	public void setEnderecoNovo(Endereco enderecoNovo){
		this.enderecoNovo = enderecoNovo;
	}

	/**
	 * Obtém o mapa de endereços selecionáveis, tendo como valor um booleano indicativo de sua seleção.
	 * 
	 * @return o mapa de endereços selecionáveis, sendo considerado selecionado o endereço (chave) cujo valor seja verdadeiro
	 */
	public Map<Endereco, Boolean> getEnderecosSelecionados(){
		return enderecosSelecionados;
	}

	/**
	 * Atribui ao campo enderecosSelecionados um mapa tendo como valor um booleano indicativo de sua seleção.
	 * 
	 * @param enderecosSelecionados o mapa de endereços selecionáveis a ser atribuído
	 */
	public void setEnderecosSelecionados(Map<Endereco, Boolean> enderecosSelecionados){
		this.enderecosSelecionados = enderecosSelecionados;
	}

	/**
	 * Marca um dos destinatários como o selecionado para edição.
	 * 
	 * @param destinatarioSelecionado o destinatário a ser selecionado para edição.
	 */
	public void setDestinatarioSelecionado(
			DestinatarioComunicacao destinatarioSelecionado) {
		limpaFiltrosEndereco();
		setDestinatarioSelecionado(destinatarioSelecionado, null);
	}
	
	/**
	 * Método responsável por verificar se uma parte possui pelo menos um endereço cadastrado no processo.
	 * Caso não possua, é verificado se essa pessoa possui endereços cadastrados no sistema.
	 * Caso tenha somente um endereço cadastrado no sistema, este virá pré selecionado.
	 */
	private void verificarPreSelecaoEndereco() {
		Set<Endereco> enderecosDestinatario;
		for (DestinatarioComunicacao destinatarioComunicacao : destinatarios.keySet()) {
			Pessoa pessoa = destinatarioComunicacao.getPessoa();
			enderecosDestinatario = mapaEnderecos.get(pessoa);
			if (	isEnderecosVazio(enderecosDestinatario) &&
					ComponentUtil.getComponent(EnderecoManager.class).retornarQuantidadeEnderecosPorUsuario(pessoa.getIdPessoa()) == 1) {
					mapaEnderecos.put(pessoa, new HashSet<Endereco>(pessoa.getEnderecoList()));
			}
		}
	}

	private boolean isEnderecosVazio(Set<Endereco> enderecosDestinatario) {
		return (enderecosDestinatario == null || enderecosDestinatario.isEmpty());
	}

	/**
	 * Marca um dos destinatários como o selecionado para edição.
	 * 
	 * @param destinatarioSelecionado
	 *            o destinatário a ser selecionado para edição.
	 * 
	 * @param procuradoriaSelecionada
	 *            a procuradoria que foi selecionada no momento do
	 *            protocolamento do processo, caso exista. Será a procuradoria
	 *            padrão, caso a pessoa possua mais de uma representação e ela
	 *            ainda esteja ativa.
	 */
	public void setDestinatarioSelecionado(DestinatarioComunicacao destinatarioSelecionado,	Procuradoria procuradoriaSelecionada) {
		if (this.destinatarioSelecionado != destinatarioSelecionado){
			this.destinatarioSelecionado = destinatarioSelecionado;
			
			Pessoa pessoa = destinatarioSelecionado.getPessoa();
			salvarPessoa(pessoa);

			acrescentaDestinatario(destinatarioSelecionado, procuradoriaSelecionada);
			
			this.processoExpediente = destinatarios.get(destinatarioSelecionado);
			enderecoNovo = null;
			enderecosSelecionados.clear();
			
			if (mapaInstrumento.get(destinatarioSelecionado) == null){
				mapaInstrumento.put(destinatarioSelecionado, null);
			}
			
			Set<Endereco> enderecosDestinatario = mapaEnderecos.get(pessoa);
			
			if (isEnderecosVazio(enderecosDestinatario)) {
				adicionarEnderecosDasPartesDaPessoa(pessoa);
			} else {
				adicionarEnderecosPossiviesMasNaoSelecionados(pessoa, enderecosDestinatario);
			}
			adotaEnderecos();
		} 		
	}

	private void adicionarEnderecosPossiviesMasNaoSelecionados(Pessoa pessoa, Set<Endereco> enderecosDestinatario) {
		Iterator<Endereco> iterator = enderecosDestinatario.iterator();
		while (iterator.hasNext()) {
			Endereco e = iterator.next();
			if (getEnderecosPossiveis().contains(e.getIdEndereco()) && enderecosSelecionados.get(e) == null){
				Set<Endereco> conjuntoEnderecos = mapaEnderecos.get(pessoa);
				Boolean enderecoSelecionado = conjuntoEnderecos != null && (conjuntoEnderecos.isEmpty() || conjuntoEnderecos.contains(e));
				enderecosSelecionados.put(e, enderecoSelecionado);
			} else {
				iterator.remove();
			}
		}
	}

	private void adicionarEnderecosDasPartesDaPessoa(Pessoa pessoa) {
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		ProcessoParte processoParte = atoComunicacaoService.getProcessoParte(pessoa, this.processoJudicial);
		if (processoParte != null) {
			if (processoParte.getEnderecos().isEmpty()) {
				if (ComponentUtil.getComponent(EnderecoManager.class).retornarQuantidadeEnderecosPorUsuario(pessoa.getIdPessoa()) == 1) {
					enderecosSelecionados.put(pessoa.getEnderecoList().get(0), true);
				}
			} else {
				for (Endereco endereco : processoParte.getEnderecos()) {
					enderecosSelecionados.put(endereco, true);
				}
			}
		}
	}

	private void salvarPessoa(Pessoa pessoa) {
		if (pessoa.getIdUsuario() == null){
			try {
				PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
				pessoaService.persist(pessoa);
			} catch (PJeBusinessException e) {
				logger.error("Erro ao tentar gravar o destinatário: {0}", e.getLocalizedMessage());
				facesMessages.add(Severity.ERROR, "Erro ao tentar gravar o destinatário: {0}", e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Recupera os endereços vinculados a uma comunicação para sua utilização em tela.
	 * 
	 */
	public void adotaEnderecos(){
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		ProcessoParteExpediente ppeDestinatario  = atoComunicacaoService.getAtoPessoal(
				destinatarios.get(destinatarioSelecionado), destinatarioSelecionado.getPessoa(),
				mapaProcuradoriaSelecionada.get(destinatarioSelecionado.getPessoa()));
		ProcessoParte processoParte = atoComunicacaoService.getProcessoParte(destinatarioSelecionado.getPessoa(), this.processoJudicial);
		if(processoParte != null && ppeDestinatario.getProcuradoria() == null) {
			ProcuradoriaManager procuradoriaManager = (ProcuradoriaManager)Component.getInstance("procuradoriaManager");
			Procuradoria procuradoria = procuradoriaManager.getProcuradoria(processoParte);
			if(procuradoria != null) {
				ppeDestinatario.setProcuradoria(procuradoria);
			}
		}
		Set<Endereco> enderecos = mapaEnderecos.get(destinatarioSelecionado.getPessoa());
		for (Endereco e : enderecosSelecionados.keySet()){
			if (enderecosSelecionados.get(e)) {
				if(enderecos == null) {
					enderecos = new HashSet<Endereco>(0);
				}
				enderecos.add(e);
			}
			else if(enderecos != null) {
				enderecos.remove(e);
			}
		}
		if (enderecos != null && enderecos.size() == 1) {
			this.mapaUmEnderecoPorExpediente.put(this.destinatarioSelecionado.getPessoa(), false);
		}
		if (enderecos != null && destinatarioSelecionado.getEndereco() == null) {
			ppeDestinatario.getProcessoParteExpedienteEnderecoList().clear();
			mapaEnderecos.put(destinatarioSelecionado.getPessoa(), enderecos);
			for(Endereco end : enderecos){
				ProcessoParteExpedienteEndereco ppee = new ProcessoParteExpedienteEndereco();
				ppee.setProcessoParteExpediente(ppeDestinatario);
				ppee.setEndereco(end);
				ppeDestinatario.getProcessoParteExpedienteEnderecoList().add(ppee);
			}
		}
	}
	
	
	/**
	 * Cria um expediente com apenas um endereço baseado em outro expediente.
	 * 
	 * @param destinatarioModelo o expediente que será utilizado como modelo para criação do novo expediente.
	 * @param end o endereço a ser adotado.
	 * 
	 * @return processo expediente com apenas um endereço baseado no expediente do parâmetro
	 */
	private ProcessoExpediente criaExpedienteComUmEndereco(DestinatarioComunicacao destinatarioModelo, Endereco end){
		ProcessoExpediente peEnderecoUnico = null;
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		peEnderecoUnico = atoComunicacaoService.getAtoComunicacao();
		ProcessoExpediente pe = destinatarios.get(destinatarioModelo);
		peEnderecoUnico.setTipoProcessoDocumento(pe.getTipoProcessoDocumento());
		peEnderecoUnico.setMeioExpedicaoExpediente(pe.getMeioExpedicaoExpediente());
		peEnderecoUnico.setUrgencia(new Boolean (pe.getUrgencia().booleanValue()));
		peEnderecoUnico.setCheckado(new Boolean (pe.getCheckado().booleanValue()));
		peEnderecoUnico.setInTemporario(pe.getInTemporario());
		peEnderecoUnico.setDocumentoExistente(pe.getDocumentoExistente());
		DocumentoJudicialService documentoJudicialService = ComponentUtil.getComponent(DocumentoJudicialService.class);
		ProcessoDocumento novo = documentoJudicialService.getDocumento();
		novo.setTipoProcessoDocumento(pe.getProcessoDocumento().getTipoProcessoDocumento());
		peEnderecoUnico.setProcessoDocumento(novo);
		
		ProcessoParteExpedienteEndereco ppee = new ProcessoParteExpedienteEndereco();
		ProcessoParteExpediente ppe = atoComunicacaoService.getAtoPessoal(peEnderecoUnico, destinatarioModelo.getPessoa(),
										mapaProcuradoriaSelecionada.get(destinatarioModelo.getPessoa()));
		if(pe.getProcessoParteExpedienteList().get(0) != null && TipoPrazoEnum.S.equals(pe.getProcessoParteExpedienteList().get(0).getTipoPrazo())) {
			ppe.setPrazoLegal(null);
		} else {
			ppe.setPrazoLegal(pe.getProcessoParteExpedienteList().get(0).getPrazoLegal());
		}
		ppe.setTipoPrazo(pe.getProcessoParteExpedienteList().get(0).getTipoPrazo());
		ppe.getProcessoParteExpedienteEnderecoList().clear();
		ppee.setProcessoParteExpediente(ppe);
		ppee.setEndereco(end);
		ppe.getProcessoParteExpedienteEnderecoList().add(ppee);
		
		return peEnderecoUnico;
	}
	
	/**
	 * Método que verifica se as partes que vão ser entimadas por meio físico,
	 * tiveram endereços selecionados. Caso alguma parte não tenha o endereço 
	 * selecionado, o método retorna false
	 * 
	 * @return true se todos os endereços forem selecionados e false se alguma parte não tiver o endereço selecionado
	 */
	private boolean hasEnderecoFisicoSelecionado() {
		for (DestinatarioComunicacao destinatario: getDestinatariosFisicos()) {
			if (this.mapaEnderecos.get(destinatario.getPessoa()).size() == 0) {;
				return false;
			}
		}
		return true;
	}

	public String getListaOrgaos(List<Procuradoria> procuradorias){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < procuradorias.size(); i++){
			sb.append(procuradorias.get(i).getNome());
			if (i < (procuradorias.size() - 1)){
				sb.append(", ");
			}
		}
		return sb.toString();
	}
	
	public void expandirTodaArvore() {
		TreeState componentState = (TreeState) richTree.getComponentState();
		try {
			componentState.expandAll(richTree);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Obtém a indicação relativa à vinculação de um dado endereço a um processo.
	 * 
	 * @return true, se o endereço novo deve ser vinculado à parte como endereço no processo.
	 */
	public Boolean getIncluirComoEnderecoProcessual(){
		return incluirComoEnderecoProcessual;
	}

	/**
	 * Permite indicar que um endereço novo a ser incluído deve ser vinculado ao processo.
	 * 
	 * @param incluirComoEnderecoProcessual valor indicativo da utilização do endereço como processual.
	 */
	public void setIncluirComoEnderecoProcessual(Boolean incluirComoEnderecoProcessual){
		this.incluirComoEnderecoProcessual = incluirComoEnderecoProcessual;
	}

	/**
	 * @return the mapaIntimacaoPessoal
	 */
	public Map<Pessoa, Boolean> getMapaIntimacaoPessoal(){
		return mapaIntimacaoPessoal;
	}

	/**
	 * @param mapaIntimacaoPessoal the mapaIntimacaoPessoal to set
	 */
	public void setMapaIntimacaoPessoal(Map<Pessoa, Boolean> mapaIntimacaoPessoal){
		this.mapaIntimacaoPessoal = mapaIntimacaoPessoal;
	}

	/**
	 * @return the mapaInstrumento
	 */
	public Map<DestinatarioComunicacao, InstrumentoComunicacao> getMapaInstrumento(){
		return mapaInstrumento;
	}

	/**
	 * @param mapaInstrumento the mapaInstrumento to set
	 */
	public void setMapaInstrumento(Map<DestinatarioComunicacao, InstrumentoComunicacao> mapaInstrumento){
		this.mapaInstrumento = mapaInstrumento;
	}

	/**
	 * @return the assinaturas
	 */
	public List<ParAssinatura> getAssinaturas(){
		assinaturas = new ArrayList<PreparaAtoComunicacaoAction.ParAssinatura>();
		Set<ProcessoDocumentoBin> docs = new HashSet<ProcessoDocumentoBin>();
		for(DestinatarioComunicacao destinatario: destinatarios.keySet()){
			if (isDestinatarioUtilizado(destinatario)) {
				continue;
			}
			ProcessoExpediente pe = destinatarios.get(destinatario);
			ProcessoDocumentoBin doc = pe.getProcessoDocumento().getProcessoDocumentoBin();
			if(!docs.contains(doc)){
				docs.add(doc);
				ParAssinatura pa = new ParAssinatura();
				String contents;
				try {
					if(doc.isBinario()) {
						pa.setBinario(true);
						pa.setMd5Documento(doc.getMd5Documento());
						assinaturas.add(pa);
					}
					else if (doc.getModeloDocumento() == null) {
						facesMessages.clear();
						facesMessages.add(Severity.ERROR, "Existem atos de comunicação sem expedientes preparados. Favor preparar todos os expedientes na etapa 'Preparar ato'.");
						assinaturas = null;
						return null;
					} else {
						contents = new String(SigningUtilities.base64Encode(doc.getModeloDocumento().getBytes()));
						pa.setConteudo(contents);
						assinaturas.add(pa);
					}
				} catch (IOException e) {
					System.err.println("Erro ao obter assinatura.");
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
	 * @return the encodedCertChain
	 */
	public String getEncodedCertChain(){
		return encodedCertChain;
	}

	/**
	 * @param encodedCertChain the encodedCertChain to set
	 */
	public void setEncodedCertChain(String encodedCertChain){
		this.encodedCertChain = encodedCertChain;
	}

	/**
	 * @return the mapaAdvogados
	 */
	public Map<Pessoa, List<ProcessoParteRepresentante>> getMapaAdvogados(){
		return mapaAdvogados;
	}

	/**
	 * @return the mapaProcuradorias
	 */
	public Map<Pessoa, List<Procuradoria>> getMapaProcuradorias(){
		return mapaProcuradorias;
	}
	
	public Map<Pessoa, Procuradoria> getMapaProcuradoriaSelecionada() {
		return mapaProcuradoriaSelecionada;
	}
	
	public Procuradoria getProcuradoriaSelecionadaDoMapa(Pessoa pessoa) {
		if (mapaProcuradoriaSelecionada != null && mapaProcuradoriaSelecionada.containsKey(pessoa)) {
			return mapaProcuradoriaSelecionada.get(pessoa);
		}
		return null;
	}
	
	public void setProcuradoriaSelecionada(Procuradoria procuradoriaSelecionada) {
		this.procuradoriaSelecionada = procuradoriaSelecionada;
	}
	
	public Procuradoria getProcuradoriaSelecionada() {
		return procuradoriaSelecionada;
	}

	@Override
	protected BaseManager<ProcessoExpediente> getManager(){
		return null;
	}

	public Map<Pessoa, List<Integer>> getCacheEnderecosPossiveis(){
		return cacheEnderecosPossiveis;
	}

	public void setCacheEnderecosPossiveis(Map<Pessoa, List<Integer>> cacheEnderecosPossiveis){
		this.cacheEnderecosPossiveis = cacheEnderecosPossiveis;
	}
	
	public List<DocumentoJudicialDataModel> getDataModelVinculaveis() {
		return dataModelVinculaveis;
	}

	/**
 	 * Vincula a lista de documentos selecionados pelo usuário a cada um dos expedientes em elaboração.
 	 */
	public void setDataModelVinculaveis(
			List<DocumentoJudicialDataModel> dataModelVinculaveis) {
		this.dataModelVinculaveis = dataModelVinculaveis;
	}
	
	public void toggleMapaIntimacaoPessoal(Pessoa pessoa) {
		this.cacheEnderecosPossiveis.remove(pessoa);
		this.cacheEnderecosPossiveisDescricao.remove(pessoa);
	}
	
	/**
	 * Valida atos de comunicação em relação ao preenchimento do endereço.
	 * @return true, se válidos.
	 */
	public boolean validaAtosDeComunicacao() {
		for (DestinatarioComunicacao destinatario : destinatarios.keySet()) {
			if (isDestinatarioUtilizado(destinatario)) continue;
			
			ProcessoExpediente processoExpediente = destinatarios.get(destinatario);
			if (processoExpediente != null) {
				if (processoExpediente.getMeioExpedicaoExpediente().isExpedicaoFisica()) {
					if (mapaEnderecos.get(destinatario.getPessoa()).isEmpty()) {
						facesMessages.add(Severity.ERROR, "Existem atos de comunicação sem endereços especificados. Favor especificar endereços na etapa 'Definição dos endereços'.");
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public int getNumeroDocumentosVinculados(){
		return numeroDocumentosVinculados;
	}
	
	/**
	 * Método para retorno do cpf/cnpj na coluna de destinatários do PAC
	 * 
	 * @param pessoa
	 * @return
	 */
	public String obtemCpfCnpj(Pessoa pessoa) {
		String ret = "" ;
		
		if (Pessoa.instanceOf(pessoa, PessoaAdvogado.class) && ((PessoaFisica) pessoa).getPessoaAdvogado() != null) {
			String strOab = ((PessoaFisica) pessoa).getPessoaAdvogado().getOabFormatado();
			
			if (strOab != null) {
				ret = "OAB: " + strOab;
			} else {
				ret = "CPF: " + pessoa.getDocumentoCpfCnpj();
			}
		} else if (pessoa instanceof PessoaFisica && pessoa.getDocumentoCpfCnpj() != null){
			ret = "CPF: " + pessoa.getDocumentoCpfCnpj();
		} else if ( pessoa instanceof PessoaJuridica && pessoa.getDocumentoCpfCnpj() != null){
			ret = "CNPJ: " + pessoa.getDocumentoCpfCnpj();
		}
	
		return ret; 
	}

	/**
	 * Método para retorno do tipo da parte na coluna de destinatários do PAC
	 * 
	 * @param pessoa
	 * @return
	 */
	public String obtemTipoParte(Pessoa pessoa) {
		String ret = null ;
		for (ProcessoParte processoParte : processoJudicial.getProcessoParteList()) {
			if (processoParte.getPessoa().equals(pessoa)) {
				ret = processoParte.getTipoParte().getTipoParte();
				break;
			}
		}		
		
		return ret ;
	}	
	
	public void setarNumeroProcesso(ProcessoTrf processo) {
		boolean processoPesquisado = false;
		for (DocumentoJudicialDataModel aux: getDataModelVinculaveis()) {
			if (aux.getProcessoJudicial().equals(processo)) {
				processoPesquisado = true;
				break;
			}
		}
		
		if (!processoPesquisado) {
			DocumentoJudicialDataModel docJudicialDataModel = new DocumentoJudicialDataModel();
			docJudicialDataModel.setProcessoJudicial(processo);
			DocumentoJudicialService documentoJudicialService = ComponentUtil.getComponent(DocumentoJudicialService.class);
			docJudicialDataModel.setDocumentoJudicialService(documentoJudicialService);
			docJudicialDataModel.setOrdemDecrescente(true);
			docJudicialDataModel.setMostrarPdf(true);
			docJudicialDataModel.setIncluirComAssinaturaInvalidada(false);
			getDataModelVinculaveis().add(docJudicialDataModel);
		}
	}
	
	public void adicionarAnexo(){
		try{
			ProcessoDocumento pdPdf = anexarDocumentos.getPdPdf();
			pdPdf.setProcessoDocumentoBin(anexarDocumentos.getPdbPdf());
			FileHome fileHome = FileHome.instance();
			anexarDocumentos.ajustarProcessoDocumento(pdPdf);
			anexarDocumentos.ajustarProcessoDocumentoBin(pdPdf.getProcessoDocumentoBin(), fileHome);
			pdPdf.getProcessoDocumentoBin().setBinario(true);
			
			ParAnexo parAnexo = new ParAnexo();
			parAnexo.setPdPdf(pdPdf);
			parAnexo.setConteudo(fileHome.getData());
			
			anexosPdf.add(parAnexo);
			anexarDocumentos.limparTelaNewIntance();
		}catch (Exception e) {
			logger.error("Não foi possível adicionar anexo ao ato de comunicação: {0}", e.getLocalizedMessage());
			facesMessages.clear();
			facesMessages.add(Severity.ERROR, "Não foi possível adicionar anexo ao ato de comunicação.");
		}
	}
	
	public void removerPdf(int index) {
		anexosPdf.remove(index);
	}
	
	public String downloadAnexo() {
		exportData();
		return "/download.xhtml";
	}

	private void exportData() {
		FileHome fileHome = FileHome.instance();
		try {
			String indice = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("index");
			ParAnexo obj = anexosPdf.get(Integer.parseInt(indice));
			fileHome.setFileName(obj.getPdPdf().getProcessoDocumentoBin().getNomeArquivo());
			byte[] data = obj.getConteudo();
			fileHome.setData(data);
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Erro ao abrir o documento.");
			e.printStackTrace();
			logger.error("Erro ao abrir documento." + e.getMessage());
		}
		Contexts.getConversationContext().set("fileHome", fileHome);
	}
	
	public Boolean getModalRepresentacaoAberta() {
		return modalRepresentacaoAberta;
	}
	
	public void setModalRepresentacaoAberta(Boolean modalRepresentacaoAberta) {
		this.modalRepresentacaoAberta = modalRepresentacaoAberta;
	}
	
	public AnexarDocumentos getAnexarDocumentos() {
		return anexarDocumentos;
	}

	public void setAnexarDocumentos(AnexarDocumentos anexarDocumentos) {
		this.anexarDocumentos = anexarDocumentos;
	}
	
	public String getVinculacaoOutroDocumento() {
		return vinculacaoOutroDocumento;
	}

	public void setVinculacaoOutroDocumento(String vinculacaoOutroDocumento) {
		this.vinculacaoOutroDocumento = vinculacaoOutroDocumento;
	}

	public List<ParAnexo> getAnexosPdf() {
		return anexosPdf;
	}

	public void setAnexosPdf(List<ParAnexo> anexosPdf) {
		this.anexosPdf = anexosPdf;
	}
	
	

	@Override
	public EntityDataModel<ProcessoExpediente> getModel() {
		throw new UnsupportedOperationException("Não implementado.");
	}
	
	/**
	 * Método responsável por setar a propriedade "intimacaoPessoal" do objeto {@link ProcessoParteExpediente} 
	 * de acordo com o valor selecionado pelo usuário.
	 * 
	 * @param pessoa
	 */
	public void setIntimacaoPessoal(DestinatarioComunicacao destinatario){
		if(destinatarios != null && destinatarios.get(destinatario) != null){
			ProcessoExpediente processoExpediente = destinatarios.get(destinatario);
			List<ProcessoParteExpediente> processoParteExpedienteList = processoExpediente.getProcessoParteExpedienteList();
			for (ProcessoParteExpediente processoParteExpediente : processoParteExpedienteList) {
				if (processoParteExpediente.getPessoaParte().equals(destinatario.getPessoa())) {
					processoParteExpediente.setIntimacaoPessoal(mapaIntimacaoPessoal.get(destinatario.getPessoa()));
				}
			}
		}
		toggleMapaIntimacaoPessoal(destinatario.getPessoa());
	}

	/**
	 * Este método tem o objetivo de criar filtros para os meios de comunicação de uma parte (Pessoa).
	 * Esses filtros são gravados em uma variável de fluxo e são representados por uma mapa.
	 * O Mapa tem a seguinte estrutura: Map(nomeDoMetodoDePessoa, Map(valorPossivelDeRetorno, listaMeiosDeComunicacaoPermitidos)).
	 * <br>
	 * No mapa principal cada chave é um nome de método da classe Pessoa. 
	 * E o valor é representado por outro mapa que representa os valores/meios de comunicação.  
	 * Esse mapa interno possui como chave o valor de retorno do método e seus possíveis meios de comunicação.
	 * Os valores de retorno do método (valoresPossiveisDeRetorno) podem conter expressões LIKE ({@link #like(String, String)}.
	 * 
	 * @param meiosDeComunicacaoPermitidos
	 * @param nomeDoAtributoDePessoa
	 * @param valoresPossiveisDoAtributo
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public PreparaAtoComunicacaoAction adicionaFiltroParaMeioDeComunicacao(String meiosDeComunicacaoPermitidos, String nomeDoAtributoDePessoa, String valoresPossiveisDoAtributo) {
		// Recupera o mapa de filtros de meio de comunicação.
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();

		Map<String, Map<String, List<ExpedicaoExpedienteEnum>>> mapaFiltrosParaMeioDeComunicacao = (Map<String, Map<String, List<ExpedicaoExpedienteEnum>>>) taskInstance.getContextInstance().getVariable(getNomeVariavelMapaFiltrosMeiosComunicacoes());
		
		// Se não existir um mapa já criado neste fluxo, deve ser criado um novo mapa.
		if(mapaFiltrosParaMeioDeComunicacao == null){
			mapaFiltrosParaMeioDeComunicacao = new HashMap<String, Map<String, List<ExpedicaoExpedienteEnum>>>();
		}
	
		// Transforma em listas as variáveis 'meiosDeComunicacaoPermitidos' e 'valoresPossiveisDeRetorno', pois estavam separadas por vírgula.
		List<ExpedicaoExpedienteEnum> listaMeiosDeComunicacaoPermitidos = recuperarListaDeExpedicaoExpedienteEnum(meiosDeComunicacaoPermitidos);
		List<String> listaDeValoresDoAtributoDePessoa = recuperarListaDeValoresSeparadosPorVirgula(valoresPossiveisDoAtributo);
		
		// Recupera o mapa interno que representa um mapa de nomeDoMetodoDePessoa/meiosDeComunicacaoPermitidos.
		Map<String, List<ExpedicaoExpedienteEnum>> mapaValorRetornoEMeiosDeComunicacao = mapaFiltrosParaMeioDeComunicacao.get(nomeDoAtributoDePessoa);
		
		// Se não existir um mapa já criado, deve ser criado um novo mapa.
		if(mapaValorRetornoEMeiosDeComunicacao == null){
			mapaValorRetornoEMeiosDeComunicacao = new HashMap<String, List<ExpedicaoExpedienteEnum>>();
			//Relaciona o mapa 'mapaFiltrosParaMeioDeComunicacao' com o atributo passado como parâmetro.
			mapaFiltrosParaMeioDeComunicacao.put(nomeDoAtributoDePessoa, mapaValorRetornoEMeiosDeComunicacao);
		}
		
		// Adiciona no mapa mapaValorDoAtributoEMeios as chaves/valores:  valorDoAtributoPessoa e seus respectiovos meios de comunicação permitidos.
		for (String valorPossivelDeRetorno : listaDeValoresDoAtributoDePessoa) {
			mapaValorRetornoEMeiosDeComunicacao.put(valorPossivelDeRetorno.toLowerCase(), listaMeiosDeComunicacaoPermitidos);
		}
		
		// Insere o mapa como variável de fluxo.

		taskInstance.getContextInstance().deleteVariable(getNomeVariavelMapaFiltrosMeiosComunicacoes());
		taskInstance.getContextInstance().setVariable(getNomeVariavelMapaFiltrosMeiosComunicacoes(), mapaFiltrosParaMeioDeComunicacao);
		
		//Retorna a instância corrente desse método, para facilitar a chamada de vários métodos concatenados.
		return this;
	}
	

	/**
	 * Este método recebe uma String com valores separados por vírgula (,).
	 * O objeto é adicionar cada elemento/valor separado por vígula em uma lista (List).
	 * 
	 * @param valoresSeparadosPorVirgula
	 * @return listaDeValores
	 */
	public List<String> recuperarListaDeValoresSeparadosPorVirgula(
			String valoresSeparadosPorVirgula) {
		List<String> listaDeValores = new ArrayList<String>();
		if (valoresSeparadosPorVirgula != null) {
			String[] valores = valoresSeparadosPorVirgula.split(",");
			for (String valor : valores) {
				valor = valor.trim();
				listaDeValores.add(valor);
			}
			return listaDeValores;
		} else {
			return listaDeValores;
		}
	}
	
	/**
	 * Este método recebe uma String com valores do enum 'ExpedicaoExpedienteEnum' separados por vírgula.
	 * Cada valor da String é convertido em ExpedicaoExpedienteEnum e colocado em uma lista que será retornada após o processamento.
	 * @param meiosDeExpedicaoSeparadosPorVirgula
	 * @return listaDeMeiosDeComunicacao
	 */
	public List<ExpedicaoExpedienteEnum> recuperarListaDeExpedicaoExpedienteEnum(String meiosDeExpedicaoSeparadosPorVirgula) {
		List<ExpedicaoExpedienteEnum> listaDeMeiosDeComunicacao = new ArrayList<ExpedicaoExpedienteEnum>();
		if (meiosDeExpedicaoSeparadosPorVirgula != null) {
			String[] meiosDeExpedicao = meiosDeExpedicaoSeparadosPorVirgula.split(",");
			for (String meio : meiosDeExpedicao) {
				meio = meio.trim();
				try {
					ExpedicaoExpedienteEnum meioPermitido = ExpedicaoExpedienteEnum
							.valueOf(meio);
					listaDeMeiosDeComunicacao.add(meioPermitido);
				} catch (Exception e) {
					// swallow
				}
			}
			return listaDeMeiosDeComunicacao;
		} else {
			return listaDeMeiosDeComunicacao;
		}
	}
	
	/**
	 * Este método tem o objetivo de filtrar os valores de meios de comunicação.
	 * Este método recebe uma lista de meios de comunicação como entrada (meiosDeComunicacaoEntrada) e retorna os valores filtrados dessa lista.
	 * Essa filtragem é baseada no mapa de parâmetros {@value #VAR_MAPA_FILTROS_MEIOSCOMUNICACOES}.
	 * 
	 * @param pessoa
	 * @param meiosDeComunicacaoEntrada
	 * @return meiosDeComunicacaoPermitidos
	 */
	@SuppressWarnings("unchecked")
	private List<ExpedicaoExpedienteEnum> filtarMeiosDeExpedientesPeloMapaDeFiltros(Pessoa pessoa, List<ExpedicaoExpedienteEnum> meiosDeComunicacaoEntrada) {
		Map<String, Map<String, List<ExpedicaoExpedienteEnum>>> mapa = null;
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();


		// Recupera o mapa de filtros.
		mapa = (Map<String, Map<String, List<ExpedicaoExpedienteEnum>>>) taskInstance
				.getContextInstance().getVariable(
						getNomeVariavelMapaFiltrosMeiosComunicacoes());

		// Se não existir uma mapa de filtros como variável, a lista é retornada
		// e nenhum valor será filtrado.
		if (mapa == null) {
			return meiosDeComunicacaoEntrada;
		}

		// Recupera as chaves do mapa.
		// Cada chave deve ser o nome de um método da classe Pessoa.
		Set<String> nomesDeMetodoDePessoa = mapa.keySet();
		//
		for (String nomeDoMetodoDePessoa : nomesDeMetodoDePessoa) {
			Set<ExpedicaoExpedienteEnum> meiosDeComunicacaoSaida = new TreeSet<ExpedicaoExpedienteEnum>();
			List<ExpedicaoExpedienteEnum> meiosPermitidos = null;
			// Recupera o valor do atributo/método da do objeto pessoa.
			Object objetoDeRetorno = invocarMetodoGet(pessoa, nomeDoMetodoDePessoa);
			
			if (objetoDeRetorno != null) {
				// Se existir no mapa, um valor correspondente ao mesmo valor da classe pessoa,
				// será retornado os meios de comunicação permitido para esta parte.
				Set<String> chavesDeValoresPossiveis = mapa.get(nomeDoMetodoDePessoa).keySet();
				for (String valorPossivelParaOAtributo : chavesDeValoresPossiveis) {
					if(like(objetoDeRetorno.toString().toLowerCase(), valorPossivelParaOAtributo.toLowerCase())){
						meiosPermitidos = mapa.get(nomeDoMetodoDePessoa).get(valorPossivelParaOAtributo);
					}
				}				
			}

			// Se existir uma restrição cadastrada no mapa para esta pessoa, os meios de comunicação serão filtrados.
			if (meiosPermitidos != null) {
				// Filtra os meios de comunicação.
				for (ExpedicaoExpedienteEnum meioPermitido : meiosPermitidos) {
					if (meiosDeComunicacaoEntrada.contains(meioPermitido)) {
						meiosDeComunicacaoSaida.add(meioPermitido);
					}
				}
				meiosDeComunicacaoEntrada = new ArrayList<ExpedicaoExpedienteEnum>(meiosDeComunicacaoSaida);
			}
		}

		return meiosDeComunicacaoEntrada;
	}

	/**
	 * Invoca um método sem parâmetros por meio de Java-Reflection.
	 * @param objeto
	 * @param nomeDoMetodo
	 * @return retornoDoMetodo 
	 */
	
	private Object invocarMetodoGet(Object objeto, String nomeDoMetodo) {
		StringBuilder nomeMetodoGet = new StringBuilder();
		nomeMetodoGet.append("get");
		nomeMetodoGet.append(Character.toUpperCase(nomeDoMetodo.charAt(0)));
		nomeMetodoGet.append(nomeDoMetodo.substring(1));				

		try {
			Method metodo = objeto.getClass().getMethod(nomeMetodoGet.toString());
			return metodo.invoke(objeto);
		} catch (Exception e) {
			String nomeDaClasse = objeto == null ? "null" : objeto.getClass()
					.getName();
			throw new RuntimeException("Não foi possível invocar o método ["
					+ nomeDoMetodo + "] da classe [" + nomeDaClasse + "].");
		}
	}
	
	/**
	 * Retorna o nome da variável {@value #VAR_MAPA_FILTROS_MEIOSCOMUNICACOES} do fluxo corrente.
	 * @return NomeVariavelMapaFiltrosMeiosComunicacoes
	 */
	private String getNomeVariavelMapaFiltrosMeiosComunicacoes(){
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();
		return VAR_MAPA_FILTROS_MEIOSCOMUNICACOES + taskInstance.getId();
	}
	
	/**
	 *  Este método é utilizado para procurar uma string padrão, usando o LIKE com o Sublinhado (_) e Porcentagem/Coringa (%).
	 *  O caractere Coringa (%) é um substituto de nenhum ou muitos caracteres.
	 *  Sublinhado (_) - Serve para marcar uma posição Específica
	 *  Porcentagem (%) - Caracter coringa. Qualquer carácter a partir da posição especificada.
	 *  
	 * @param str - Valor onde será utilizada a expressão
	 * @param expr - Expressão que será pesquisada em str.
	 * @return true - se a expressão for encontrada em str.
	 */
	public static boolean like(final String str, final String expr) {
		if (str == null || expr == null) {
			return false;
		}

		String regex = quotemeta(expr);
		regex = regex.replace("_", ".").replace("%", ".*?");
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE
				| Pattern.DOTALL);
		return p.matcher(str).matches();
	}

	/**
	 * Este método produz uma String para ser utilizada na classe Pattern, porém com os caracteres especiais tratados como caracteres 
	 * normais. 
	 * Exemplo: 
	 *   Entrada: 'World' 
	 *   Saída: '\\World'
	 * @param s
	 * @return string tratada.
	 */
	public static String quotemeta(String s) {
		if (s == null) {
			throw new IllegalArgumentException("String não pode ser nula.");
		}

		int len = s.length();
		if (len == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder(len * 2);
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if ("[](){}.*+?$^|#\\".indexOf(c) != -1) {
				sb.append("\\");
			}
			sb.append(c);
		}
		return sb.toString();
	}
	
	/**
	 * Método responsável por verificar se o campo "Intimação Pessoal" deve vir habilitado ou não (RN345).
	 * 
	 * @param pessoa Parte que compõe do processo. 
	 * @return 
	 * 		<ul>
	 * 			<li><b>Verdadeiro</b> se a parte tem representantes e não é um ente ou autoridade.</li>			
	 * 			<li><b>Falso</b> nos demais casos.</li>
	 * 		</ul>
	 */
	public boolean isCampoIntimacaoPessoalHabilitado(final Pessoa pessoa){
		/* 
		 * Se a parte é um ente ou autoridade, o campo deve vir desabilitado (campo somente leitura).
		 */
		return !pessoa.getInTipoPessoa().equals(TipoPessoaEnum.A);
	}
	
	public void selecionarProcuradoria(Pessoa pessoa){
		if(pessoa != null && procuradoriaSelecionada != null){
			definirProcuradoriaSelecionada(pessoa, procuradoriaSelecionada);
			getExpedientePessoal(pessoa);
			procuradoriaSelecionada = null;
		} else {
			facesMessages.add(Severity.WARN, "Nenhuma procuradoria foi selecionada.");
		}
	}
	
	/**
	 * Verifica se os expedientes por endereço (que podem já ter sido gerados anteriormente) estão com o tipo de comunicação e meios corretos.
	 * Este método visa corrigir a seguinte situação:
	 * 1 - expedientes são gerados com a opção "um expediente por endereço" para um determinado meio de comunicação, prazo, tipo etc, 
	 * 2 - são escolhido os endereços na aba de escolha de endereço e o mapa <pessoa, endereço> é preenchido, e depois voltado para a tela inicial
	 * 3 - na tela inicial, alguma informação é alterada no mapa <pessoa,null>
	 * 4 - ao avançar, os mapas <pessoa, endereço> devem ser atualizados; este médtod faz isso  
	 */		
	private void corrigeExpedientesComEndereco() {
		// Percorre todos os destinatários
		for (Entry<DestinatarioComunicacao, ProcessoExpediente> entry : this.destinatarios.entrySet()){
			DestinatarioComunicacao destinatarioEndereco = entry.getKey();
			// pega apenas aqueles que tenham endereço
			if (destinatarioEndereco.getEndereco() != null) {
				// Verifica o expediente criado para a pessoa (antes da escolha do endereço; endereço == null e guarda as informações)
				DestinatarioComunicacao destinatarioPessoa = obterDestinatario(destinatarioEndereco.getPessoa(), null);
				ProcessoExpediente expedientePessoa = destinatarios.get(destinatarioPessoa);
				if (expedientePessoa != null) {
					ProcessoParteExpediente ppePessoa = expedientePessoa.getProcessoParteExpedienteList().get(0);
					
					TipoProcessoDocumento tipoProcessoDocumento = expedientePessoa.getTipoProcessoDocumento();
					ExpedicaoExpedienteEnum meioExpedicaoExpediente = expedientePessoa.getMeioExpedicaoExpediente();
					TipoPrazoEnum tipoPrazoLegal = ppePessoa.getTipoPrazo();
					Integer prazoLegal = ppePessoa.getPrazoLegal();
					Date dtPrazoLegal = ppePessoa.getDtPrazoLegal();
					TipoProcessoDocumento tipoProcessoDocumentoDoc = expedientePessoa.getProcessoDocumento().getTipoProcessoDocumento();
					
					// atribui ao expediente com endereço as informações obtidas do expediente da pessoa.
					ProcessoExpediente expedienteEndereco = entry.getValue();
					expedienteEndereco.setTipoProcessoDocumento(tipoProcessoDocumento);
					expedienteEndereco.setMeioExpedicaoExpediente(meioExpedicaoExpediente);
					ProcessoParteExpediente ppeEndereco = expedienteEndereco.getProcessoParteExpedienteList().get(0);
					ppeEndereco.setTipoPrazo(tipoPrazoLegal);
					ppeEndereco.setPrazoLegal(prazoLegal);
					ppeEndereco.setDtPrazoLegal(dtPrazoLegal);
					expedienteEndereco.getProcessoDocumento().setTipoProcessoDocumento(tipoProcessoDocumentoDoc);
				}
			}
		}
	}
				
	/**
	 * Limpa o endereço de expedientes dos destinatários não físicos. 
	 * Eles podem conter endereço caso o meio de comunicação físico tenha sido escolhido, 
	 * selecionado o endereço, e posteriormente escolhido meio não físico.
	 */
	private void limpaEnderecoDestinatarioNaoFisico() {
		for (DestinatarioComunicacao d : destinatarios.keySet()) {
			ProcessoExpediente pe = destinatarios.get(d);
			ExpedicaoExpedienteEnum meio = pe.getMeioExpedicaoExpediente();
			if (meio != ExpedicaoExpedienteEnum.C && meio != ExpedicaoExpedienteEnum.L && meio != ExpedicaoExpedienteEnum.M && meio != ExpedicaoExpedienteEnum.G){  // Não é físico
				this.mapaEnderecos.remove(d.getPessoa());
				pe.getProcessoParteExpedienteList().get(0).getProcessoParteExpedienteEnderecoList().clear(); // somente tem uma parte por expediente.
			}
		}		
	}

	/**
	 * Verifica se foi escolhido gerar um expediente por endereço e se a marcação do 
	 * destinatário está feita pelos endereços, e não destinatario sem endereço.
	 */
	private void verificaUmExpedientePorEndereco() {
		Set<DestinatarioComunicacao> chaves = new HashSet<DestinatarioComunicacao>(destinatarios.keySet());
		for (DestinatarioComunicacao destinatario : chaves) {
			if (mapaUmEnderecoPorExpediente.get(destinatario.getPessoa()) && destinatario.getEndereco() == null) {
				for (Endereco end : mapaEnderecos.get(destinatario.getPessoa())) {
					DestinatarioComunicacao d = obterDestinatario(destinatario.getPessoa(), end);
					if (d == null) {
						d = new DestinatarioComunicacao(destinatario.getPessoa(), end);
					}
					if (!destinatarios.containsKey(d)) {
						destinatarios.put(d, criaExpedienteComUmEndereco(destinatario, end));
					}
				}
				this.mapaAgrupadoAgrupador.remove(destinatario);
				this.destinatarioAgrupadoIdAgrupador.remove(destinatario);
			}
			if (!mapaUmEnderecoPorExpediente.get(destinatario.getPessoa()) && destinatario.getEndereco() != null) {
				this.destinatarios.remove(destinatario);
				this.mapaAgrupadoAgrupador.remove(destinatario);
				this.destinatarioAgrupadoIdAgrupador.remove(destinatario);
			}
		}
	}
	
	private DestinatarioComunicacao obterDestinatario(Pessoa pessoa, Endereco endereco) {
		for (DestinatarioComunicacao destinatario : this.destinatarios.keySet()) {
			if (destinatario.getPessoa().equals(pessoa) && 
					((endereco == null && destinatario.getEndereco() == null) || 
							(destinatario.getEndereco() != null && destinatario.getEndereco().equals(endereco)))) {
				
				return destinatario;
			}
		}
		return null;
	}
	
	/**
	 * Obtém o mapa que verifica se será gerado um expediente por endereço
	 * 
	 * @return mapa indicativo de geração de um expediente por endereço
	 */	
	public Map<Pessoa, Boolean> getMapaUmEnderecoPorExpediente() {
		return mapaUmEnderecoPorExpediente;
	}

	/**
	 * Seta o mapa que verifica se será gerado um expediente por endereço
	 * 
	 * @param mapaUmEnderecoPorExpediente
	 */
	public void setMapaUmEnderecoPorExpediente(Map<Pessoa, Boolean> mapaUmEnderecoPorExpediente) {
		this.mapaUmEnderecoPorExpediente = mapaUmEnderecoPorExpediente;
	}

	private boolean isDestinatarioUtilizado(DestinatarioComunicacao destinatario) {
		return (mapaUmEnderecoPorExpediente.get(destinatario.getPessoa()) && destinatario.getEndereco() == null) || 
				(!mapaUmEnderecoPorExpediente.get(destinatario.getPessoa()) && destinatario.getEndereco() != null); 
	}

	/**
	 * Variável que exibe os destinatarios
	 */
	public Map<DestinatarioComunicacao, ProcessoExpediente> getDestinatariosPessoa() {
		Map<DestinatarioComunicacao, ProcessoExpediente> destinatarioPessoa = new HashMap<DestinatarioComunicacao, ProcessoExpediente>(0);
		for (DestinatarioComunicacao destinatario: this.destinatarios.keySet()) {
			if (destinatario.getEndereco() == null) {
				destinatarioPessoa.put(destinatario, this.destinatarios.get(destinatario));
			}
		}			
		return destinatarioPessoa;
	}

	/**
	 * Recupera mapa de pessoa x Boolean, se houver, decorrente de execução anterior do nó de fluxo.
	 * 
	 * @return Mapa contendo as pessoas e se será gerado um expediente por endereço.
	 * 
	 */
	@SuppressWarnings("unchecked")
	private Map<Pessoa, Boolean> recuperaUmExpedientePorEndereco(){
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();

		if (taskInstance != null) {
			Object o = taskInstance.getVariable(VAR_UM_EXPED_POR_ENDERECO + taskInstance.getId());
			if (o != null) {
				HashMap<Pessoa, Boolean> auxDest;
				try {
					auxDest = (HashMap<Pessoa, Boolean>) o;
				}
				catch(Exception e) {
					taskInstance.deleteVariable(VAR_UM_EXPED_POR_ENDERECO);
					return new HashMap<Pessoa, Boolean>(0);
				}
				
				HashMap<Pessoa, Boolean> ret = new HashMap<Pessoa, Boolean>(auxDest.size());
				PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
				for (Pessoa p : auxDest.keySet()){
					Pessoa managed = null;
					try {
						managed = pessoaService.findById(p.getIdUsuario());
					} catch (PJeBusinessException e) {
						logger.error("Erro ao tentar recuperar a dados relativos à pessoa [{0}].", p.getNome());
						continue;
					}
					Boolean umExpedientePorEndereco = auxDest.get(p);
					ret.put(managed, umExpedientePorEndereco);
				}
				return ret;
			}
		}
		return new HashMap<Pessoa, Boolean>(0);
	}
	
	private List<ProcessoParte> suprimirPartesInativas(List<ProcessoParte> lista){
		List<ProcessoParte> listaSemInativos = new ArrayList<ProcessoParte>();
		for(ProcessoParte parte : lista){
			if(parte.getIsAtivo() && !parte.getIsBaixado()){
				listaSemInativos.add(parte);
			}
		}
		return listaSemInativos;
	}
	
	public List<ProcessoParte> recuperaListaPartePrincipalAtivo() {
		List<ProcessoParte> listaParteAtivo = processoJudicial.getListaPartePrincipal(true, ProcessoParteParticipacaoEnum.A);
		
		if(!exibirPartesInativas){
			listaParteAtivo = suprimirPartesInativas(listaParteAtivo);
		}
		
		return listaParteAtivo; 
	}
	
	public List<ProcessoParte> recuperaListaPartePrincipalPassivo() {
		List<ProcessoParte> listaPartePassivo = processoJudicial.getListaPartePrincipal(true, ProcessoParteParticipacaoEnum.P);
		
		if(!exibirPartesInativas){
			listaPartePassivo = suprimirPartesInativas(listaPartePassivo);
		}
		
		return listaPartePassivo; 
	}
	
	public List<ProcessoParte> recuperaListaPartePrincipalTerceiro() {
		List<ProcessoParte> listaParteTerceiro = processoJudicial.getListaPartePrincipal(true, ProcessoParteParticipacaoEnum.T);
		
		if(!exibirPartesInativas){
			listaParteTerceiro = suprimirPartesInativas(listaParteTerceiro);
		}
		
		return listaParteTerceiro; 
	}
	
	public boolean isExibirPartesInativas() {
		return exibirPartesInativas;
	}
	
	public void setExibirPartesInativas(boolean exibirPartesInativas) {
		this.exibirPartesInativas = exibirPartesInativas;
	}
	
	/**
	 * Método responsável por inicializar os atributos utilizados na modal de
	 * seleção da procuradoria de uma pessoa
	 */
	public void iniciarSelecaoProcuradoria(Pessoa pessoa) {
		if (pessoa != null) {
			this.pessoaProcuradoriaSelecionada = pessoa;
			if (mapaProcuradorias != null && mapaProcuradorias.containsKey(pessoa)) {
				this.listaComboProcuradorias = mapaProcuradorias.get(pessoa);
				if (this.listaComboProcuradorias == null) {
					this.listaComboProcuradorias = new ArrayList<Procuradoria>();					
				} else if (this.listaComboProcuradorias.size() == 1) {
					this.procuradoriaSelecionada = listaComboProcuradorias.get(0);
				}
				if (mapaProcuradoriaSelecionada != null && mapaProcuradoriaSelecionada.containsKey(pessoa)){
					this.procuradoriaSelecionada = mapaProcuradoriaSelecionada.get(pessoa);
				}
			} else {
				this.listaComboProcuradorias = new ArrayList<Procuradoria>();
			}
		} else {
			this.pessoaProcuradoriaSelecionada = null;
			this.listaComboProcuradorias = new ArrayList<Procuradoria>();
		}
	}

	public boolean habilitaDomicilio(Pessoa pessoa) {
		return DomicilioEletronicoService.instance().isPessoaHabilitada(pessoa);
	}

	public boolean habilitaDomicilio(String cpfCnpj) {
		return DomicilioEletronicoService.instance().isPessoaHabilitada(cpfCnpj);
	}

	/**
	 * Método chamada pelo panel de seleção de procuradoria.
	 */
	public void confirmarSelecaoProcuradoria() {
		this.selecionarProcuradoria(pessoaProcuradoriaSelecionada);
		this.pessoaProcuradoriaSelecionada = null;
		this.procuradoriaSelecionada = null;
		this.toggleMapaIntimacaoPessoal(this.destinatarioSelecionado.getPessoa());
	}

	public List<Procuradoria> getListaComboProcuradorias() {
		return listaComboProcuradorias;
	}

	public void setListaComboProcuradorias(
			List<Procuradoria> listaComboProcuradorias) {
		this.listaComboProcuradorias = listaComboProcuradorias;
	}

	public Pessoa getPessoaProcuradoriaSelecionada() {
		return pessoaProcuradoriaSelecionada;
	}

	public void setPessoaProcuradoriaSelecionada(
			Pessoa pessoaProcuradoriaSelecionada) {
		this.pessoaProcuradoriaSelecionada = pessoaProcuradoriaSelecionada;
	}

	public Map<Integer, String> getEnderecoIdDescricao() {
		if (cacheEnderecosPossiveisDescricao.containsKey(destinatarioSelecionado.getPessoa())) {
			return cacheEnderecosPossiveisDescricao.get(destinatarioSelecionado.getPessoa());
		}
		return enderecoIdDescricao;
	}



	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		if(assinaturas != null && !assinaturas.isEmpty()){
			for(ParAssinatura pd: assinaturas){
				if(pd.isBinario()) {
					pd.setAssinatura(arquivoAssinadoHash.getAssinatura());
					encodedCertChain = arquivoAssinadoHash.getCadeiaCertificado();
				}
				else {
					String conteudoDocumento = new String(SigningUtilities.base64Decode(pd.getConteudo()));
					String md5 = Crypto.encodeMD5(conteudoDocumento);
					if(md5.equals(arquivoAssinadoHash.getHash())){
						pd.setAssinatura(arquivoAssinadoHash.getAssinatura());
						encodedCertChain = arquivoAssinadoHash.getCadeiaCertificado();
						break;
					}
				}
			}
		}
	}

	@Override
	public String getActionName() {
		return "preparaAtoComunicacaoAction";
	}


	public String getDownloadLinks(){
		StringBuilder sb = new StringBuilder();
		if(assinaturas != null && !assinaturas.isEmpty()){
			int i = 0;
			int size = assinaturas.size();
			for(ParAssinatura pd: assinaturas){
				if(pd == null){
					continue;
				}
				createDownloadLink(pd, sb);
				if(i < (size - 1)){
					sb.append(",");
				}
				i++;
			}
		}
		return sb.toString();
	}

	private void createDownloadLink(ParAssinatura pd, StringBuilder sb) {
		if(!pd.isBinario() && StringUtil.isNullOrEmpty(pd.getConteudo())){
			return;
		}
		String conteudoDocumento = null;
		try {
			if(!pd.isBinario())
				conteudoDocumento = new String(SigningUtilities.base64Decode(pd.getConteudo()));
		} catch (IOException e) {
			return;
		}

		sb.append("id=1");
		sb.append("&codIni=1");
		sb.append("&md5=");
		sb.append(pd.isBinario() ? pd.getMd5Documento() : Crypto.encodeMD5(conteudoDocumento));
		sb.append("&isBin=false");
	}
	
	public void atribuirTipoCalculoMeioComunicacao(ExpedicaoExpedienteEnum meioComunicacao, ProcessoParteExpediente processoParteExpediente) {
		if (ExpedicaoExpedienteEnum.M.equals(meioComunicacao)) {
			processoParteExpediente.setTipoCalculoMeioComunicacao(processoJudicial.getCompetencia() == null ? 
					TipoCalculoMeioComunicacaoEnum.CD : processoJudicial.getCompetencia().getTipoCalculoMeioComunicacao());			
		} else {
			processoParteExpediente.setTipoCalculoMeioComunicacao(null);
		}
	}

	public String getMensagemIndisponibilidade() {
		return mensagemIndisponibilidade;
	}

	public void setMensagemIndisponibilidade(String mensagemIndisponibilidade) {
		this.mensagemIndisponibilidade = mensagemIndisponibilidade;
	}

	public String getMensagemPossivelAtrasoEnvioDomicilio() {
		return mensagemPossivelAtrasoEnvioDomicilio;
	}

	public void setMensagemPossivelAtrasoEnvioDomicilio(String mensagemPossivelAtrasoEnvioDomicilio) {
		this.mensagemPossivelAtrasoEnvioDomicilio = mensagemPossivelAtrasoEnvioDomicilio;
	}

	public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}

	public String getCepFiltro() {
		return cepFiltro;
	}

	public void setCepFiltro(String cepFiltro) {
		this.cepFiltro = cepFiltro;
	}

	public String getEnderecoCompletoFiltro() {
		return enderecoCompletoFiltro;
	}

	public void setEnderecoCompletoFiltro(String enderecoCompletoFiltro) {
		this.enderecoCompletoFiltro = enderecoCompletoFiltro;
	}

	/**
	 * @return the mapaCachePessoaMeioComunicacao
	 */
	protected Map<String, List<ExpedicaoExpedienteEnum>> getMapaCachePessoaMeioComunicacao() {
		if (mapaCachePessoaMeioComunicacao == null) {
			mapaCachePessoaMeioComunicacao = new HashMap<>();
		}
		return mapaCachePessoaMeioComunicacao;
	}

	/**
	 * Método executado quando a tarefa é carregada.
	 * 
	 */
	public void onFrameLoad() {
		DomicilioEletronicoService domicilio = DomicilioEletronicoService.instance();
		if (domicilio.isIntegracaoHabilitada() && domicilio.isUtilizaAlertaDomicilioOffline() && !domicilio.isOnline() ) {
			facesMessages.add(Severity.ERROR, "Domicílio Eletrônico offline! Os expedientes serão criados no PJe mas não serão enviados ao Domicílio Eletrônico neste momento.");
		}
	}

	public boolean isPossuiAlgumaParteHabilitadaDomicilio() {
		for (DestinatarioComunicacao destinatario : destinatarios.keySet()){
			ProcessoExpediente pe = destinatarios.get(destinatario);
			if (habilitaDomicilio(destinatario.getPessoa()) && pe.getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.E) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retorna true se for para exibir mensagem de alerta sobre possível atraso de envio de expedientes ao Domicílio (caso esteja offline).
	 * A mensagem só será exibida se:
	 * - O parâmetro PJE_DOMICILIO_ELETRONICO_UTILIZAR_ALERTA_OFFLINE estiver configurado como false;
	 * - O parâmetro PJE_DOMICILIO_ELETRONICO_MSG_POSSIVEL_ATRASO_ENVIO não estiver vazio;
	 * - Tiver alguma parte habilitada no Domicílio.
	 *
	 * @return Boleano
	 */
	public boolean isExibeMensagemPossivelAtrasoEnvioDomicilio() {
		DomicilioEletronicoService domicilio = DomicilioEletronicoService.instance();
		mensagemPossivelAtrasoEnvioDomicilio = domicilio.msgAlertaPossivelAtrasoEnvioDomicilio();
		return !BooleanUtils.toBoolean(domicilio.isUtilizaAlertaDomicilioOffline()) && isPossuiAlgumaParteHabilitadaDomicilio() && !Strings.isEmpty(mensagemPossivelAtrasoEnvioDomicilio);
	}
}
