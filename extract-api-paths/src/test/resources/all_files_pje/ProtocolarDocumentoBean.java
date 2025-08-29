/**
 * pje-web
 * Copyright (C) 2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/

package br.jus.cnj.pje.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.faces.event.ValueChangeEvent;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.jboss.seam.transaction.Transaction;
import org.jbpm.graph.exe.ProcessInstance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

import br.com.infox.cliente.actions.anexarDocumentos.AnexarDocumentos;
import br.com.infox.cliente.component.signfile.SignFile;
import br.com.infox.cliente.home.CustasJudiciaisHome;
import br.com.infox.cliente.home.DiligenciaHome;
import br.com.infox.cliente.home.ProcessoDocumentoBinPessoaAssinaturaHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.component.tree.EventoBean;
import br.com.infox.ibpm.component.tree.EventsTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.component.FileHome;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.certificado.CertificadoICP;
import br.jus.cnj.certificado.CertificadoICPBrUtil;
import br.jus.cnj.certificado.Signature;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.extensao.auxiliar.PontoExtensaoResposta;
import br.jus.cnj.pje.extensao.auxiliar.custas.CodigoRespostaGuiaRecolhimentoEnum;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ControleVersaoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.PapelService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.servicos.MimeUtilChecker;
import br.jus.cnj.pje.view.MultipleFileUploadAction.UploadedFile;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.business.service.HabilitacaoAutosService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.csjt.pje.business.service.PlantaoJudicialService;
import br.jus.csjt.pje.commons.exception.BusinessException;
import br.jus.pje.jt.enums.TipoDeclaracaoEnum;
import br.jus.pje.jt.enums.TipoSolicitacaoHabilitacaoEnum;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.enums.TipoDocumentoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.nucleo.util.Utf8ParaIso88591Util;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Javabean destinado a controlar o protocolo de um documento (e eventuais anexos).
 * 
 * Faz parte do escopo deste componente o controle de inserção de documento em processo
 * antes e depois de sua distribuição, com a exigência de movimento processual vinculado 
 * quando:
 * <li>o usuário que realiza a inclusão é usuário interno do Judiciário (tem o papel {@link Papeis#INTERNO}); e</li>
 * <li>o tipo de documento selecionado está associado a um agrupamento de movimentações processuais.</li>
 * 
 * @author cristof
 * 
 * @see TipoProcessoDocumento#getAgrupamento()
 *
 */
public class ProtocolarDocumentoBean{
	
	public static final int EXIGE_DOCUMENTO_PRINCIPAL = 1;
	
	public static final int PERMITE_SELECIONAR_MOVIMENTACAO = 2;
	
	public static final int LANCAR_MOVIMENTACAO = 4;
	
	public static final int UTILIZAR_MODELOS = 8;
	
	public static final int PERMITIR_VINCULACAO_RESPOSTA = 16;
	
	public static final int VINCULAR_DATA_JUNTADA = 32;
	
	public static final int NAO_ASSINA_DOCUMENTO_PRINCIPAL = 64;
	
	public static final int RECUPERA_DOCUMENTO_FLUXO = 128;
	
	public static final int CARREGA_DOCUMENTO_PENDENTE_DE_ATIVIDADE_ESPECIFICA = 256;
	
	public static final int MOSTRAR_ESCOLHA_PARTES = 512;
	
	private Log logger = Logging.getLog(ProtocolarDocumentoBean.class);
	private FacesMessages facesMessages = FacesMessages.instance();
	private DocumentoJudicialService documentoJudicialService;
	private MimeUtilChecker mimeUtilChecker;
	private PessoaService pessoaService;
	private UsuarioService usuarioService;
	private PapelService papelService;
	private ProcessoJudicialService processoJudicialService;
	private HabilitacaoAutosService habilitacaoAutosService;
	private int contador;
	private Integer idProcessoJudicial;
	private Date ultimaAtualizacao;
	private MultipleFileUploadAction multipleFileUploadAction;
	private ProcessoTrf processoJudicial;
	private ProcessoDocumento documentoPrincipal;
	private TipoProcessoDocumento tipoPrincipal;
	private EventoManager eventoManager;
	private ProcessoDocumentoManager processoDocumentoManager;
	private ProcessoParteManager processoParteManager;
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	private AnexarDocumentos anexarDocumentos;
	private TipoMovimentoProcessualBean tipoMovimentoProcessualBean;
	private RespostaExpedienteBean respostaBean;
	private ArrayList<ProcessoDocumento> arquivos;
        private TipoProcessoDocumento tipoSelecionadoParaUploadEmLoteNoCKEditor;
        private Boolean marcarDesmarcarTodosUploadEmLoteNoCKEditor;
	private EventsTreeHandler treeMovimentacoes;
	private List<String> errosUpload;
	private boolean exigeMovimentacao = false;
	private boolean exigeDocumentoPrincipal;
	private Evento tipoPai;
	private List<Evento> raizes; 
	private Set<String> codigosSelecionados;
	private String filtroMovimentacao;
	private boolean permiteSelecaoMovimentacao;
	private boolean lancaMovimentacao;
	private boolean utilizaModelos;
	private boolean permiteVincularResposta;
	private boolean dataJuntadaPropria;
	private boolean naoAssinaDocumentoPrincipal;
	private boolean recuperaDocumentoFluxo;
	private ModeloDocumento modelo;
	private List<ModeloDocumento> modelos;
	private ConsultaProcessualAction consultaProcessualAction;
	private TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService;
	private List<TipoProcessoDocumento> tiposDocumentosPossiveis;
	private ControleVersaoDocumentoManager controleVersaoDocumentoManager;
	private Map<ProcessoParte, Boolean> mapaProcessoParteAtivaSelecionada = new HashMap<ProcessoParte, Boolean>();
	private Map<ProcessoParte, Boolean> mapaProcessoPartePassivaSelecionada = new HashMap<ProcessoParte, Boolean>();
	private Boolean terceiroInteressado = false;
	private Boolean responderNenhumExpediente = false;
	private Boolean mostrarEscolhaPartes;
	private TipoDocumentoPrincipalEnum tipoDocumentoPrincipal = TipoDocumentoPrincipalEnum.PDF;
	private enum TipoDocumentoPrincipalEnum { PDF, HTML }
	private TipoAcaoProtocoloEnum tipoAcaoProtocolo;
	public enum TipoAcaoProtocoloEnum { 
		NOVO_PROCESSO, //Por ora, testado apenas para exibir ou não uma mensagem específica na conclusão da assinatura (método concluirAssinatura)
		JUNTADA, //Por ora, não é testado para nada
		HABILITACAO_AUTOS } //Por ora, não é testado para nada
	private CustasJudiciaisHome custasJudiciaisHome;

	/**
	 * Define o nome da action que recebera os arquivos assinados {@link ArquivoAssinadoUpload} 
	 */
	private String actionName;	
	
	/**
	 * Define a lista de arquivos assinados pelo assinador, os arquivos assinados e enviados pelo assinador
	 * sera armazenados nesta lista para posterior validacao e persistencia.
	 */
	private List<ArquivoAssinadoHash> arquivosAssinados = new ArrayList<ArquivoAssinadoHash>();
	
	private Boolean atendimentoPlantao = Boolean.FALSE;
	
	/**
	 * Construtor que possibilitará a seleção de movimentação processual e realizará
	 * lançamentos de movimentações associadas.
	 *  
	 * @param idProcessoJudicial o identificador do processo judicial ao qual o(s) documento(s)
	 * será(ão) vinculado(s).
	 */
	public ProtocolarDocumentoBean(Integer idProcessoJudicial){
		this(idProcessoJudicial, true, true, true, true, true);
	}
	
	/**
	 * Construtor padrão do componente.
	 * 
	 * @param idProcessoJudicial o identificador do processo judicial ao qual o(s) documento(s)
	 * será(ão) vinculado(s).
	 * @param modo o modo de execução do componente, que deve ser resultado de combinação dos modos 
	 */
	public ProtocolarDocumentoBean(Integer idProcessoJudicial, int modo){
		this(idProcessoJudicial, modo, TipoAcaoProtocoloEnum.JUNTADA);
	}
	
	public ProtocolarDocumentoBean(Integer idProcessoJudicial, int modo, TipoAcaoProtocoloEnum tipo){
		init(idProcessoJudicial, tipo,
				(modo & EXIGE_DOCUMENTO_PRINCIPAL) > 0, 
				(modo & PERMITE_SELECIONAR_MOVIMENTACAO) > 0, 
				(modo & LANCAR_MOVIMENTACAO) > 0, 
				(modo & UTILIZAR_MODELOS) > 0, 
				(modo & PERMITIR_VINCULACAO_RESPOSTA) > 0,
				(modo & VINCULAR_DATA_JUNTADA) > 0,
				(modo & NAO_ASSINA_DOCUMENTO_PRINCIPAL) > 0,
				(modo & RECUPERA_DOCUMENTO_FLUXO) > 0,
				(modo & CARREGA_DOCUMENTO_PENDENTE_DE_ATIVIDADE_ESPECIFICA) > 0,
				(modo & MOSTRAR_ESCOLHA_PARTES) > 0);
	}
	
	/**
	 * Construtor do componente
	 * 
	 * @param idProcessoTrf o identificador do processo judicial ao qual o(s) documento(s)
	 * @param modo o modo de execução do componente, que deve ser resultado de combinação dos modos
	 * @param actionName o nome da action que recebera os arquivos assinados {@link ArquivoAssinadoUpload} 
	 */
	public ProtocolarDocumentoBean(int idProcessoTrf, int modo, String actionName) {
		this(idProcessoTrf, modo, TipoAcaoProtocoloEnum.JUNTADA);
		this.actionName = actionName;
	}
	
	/**
	 * Construtor do componente
	 * 
	 * @param idProcessoTrf o identificador do processo judicial ao qual o(s) documento(s)
	 * @param modo o modo de execução do componente, que deve ser resultado de combinação dos modos
	 * @param actionName o nome da action que recebera os arquivos assinados {@link ArquivoAssinadoUpload}
	 * @param tipo Tipo de ação de protocolo a partir do TipoAcaoProtocoloEnum (NOVO_PROCESSO, JUNTADA, etc.)
	 */
	public ProtocolarDocumentoBean(int idProcessoTrf, int modo, String actionName, TipoAcaoProtocoloEnum tipo) {
		this(idProcessoTrf, modo, tipo);
		this.actionName = actionName;
	}
	
	/**
	 * Construtor padrão do componente.
	 * 
	 * @param idProcessoJudicial o identificador do processo judicial ao qual o(s) documento(s)
	 * será(ão) vinculado(s).
	 * @param exigeDocumentoPrincipal marca indicativa de que o componente deverá exigir um documento
	 * de texto principal.
	 * @param permiteSelecaoMovimentacao marca indicativa de que o componente deverá permitir
	 * a seleção de movimentações processuais quando possível
	 * @param lancaMovimentacao marca indicativa de que o componente deverá se responsabilizar por
	 * lançar a movimentação processual pertinente quando finalizado o protocolo
	 */
	public ProtocolarDocumentoBean(Integer idProcessoJudicial, boolean exigeDocumentoPrincipal, boolean permiteSelecaoMovimentacao, boolean lancaMovimentacao, boolean utilizaModelos, boolean permiteVincularResposta){
		init(idProcessoJudicial, exigeDocumentoPrincipal, permiteSelecaoMovimentacao, lancaMovimentacao, utilizaModelos, permiteVincularResposta,false,false,false,false,false);
	}
	
	public ProtocolarDocumentoBean(Integer idProcessoJudicial, boolean exigeDocumentoPrincipal, boolean permiteSelecaoMovimentacao, boolean lancaMovimentacao, boolean utilizaModelos, boolean permiteVincularResposta, boolean dataJuntadaPropria){
		init(idProcessoJudicial, exigeDocumentoPrincipal, permiteSelecaoMovimentacao, lancaMovimentacao, utilizaModelos, permiteVincularResposta,dataJuntadaPropria,false,false,false,false);
	}
	
	public ProtocolarDocumentoBean(Integer idProcessoJudicial, boolean exigeDocumentoPrincipal, boolean permiteSelecaoMovimentacao, boolean lancaMovimentacao, boolean utilizaModelos, boolean permiteVincularResposta, boolean dataJuntadaPropria, boolean naoAssinaDocumentoPrincipal){
		init(idProcessoJudicial, exigeDocumentoPrincipal, permiteSelecaoMovimentacao, lancaMovimentacao, utilizaModelos, permiteVincularResposta,dataJuntadaPropria,naoAssinaDocumentoPrincipal,false,false,false);
	}
	
	public ProtocolarDocumentoBean(Integer idProcessoJudicial, boolean exigeDocumentoPrincipal, boolean permiteSelecaoMovimentacao, boolean lancaMovimentacao, boolean utilizaModelos, boolean permiteVincularResposta, boolean dataJuntadaPropria, boolean naoAssinaDocumentoPrincipal,String actionName){
		init(idProcessoJudicial, exigeDocumentoPrincipal, permiteSelecaoMovimentacao, lancaMovimentacao, utilizaModelos, permiteVincularResposta,dataJuntadaPropria,naoAssinaDocumentoPrincipal,false,false,false);
		this.actionName = actionName;
	}	
	
	
	/**
	 * Indica se essa instância de componente permite a anexação. A anexação somente
	 * é permitida, quando se tratar de componente que exige documento principal, quando
	 * esse documento já foi incluído e salvo pelo menos uma vez.
	 *  
	 * @return true, se for possível a anexação.
	 */
	public boolean isPodeAnexar(){
		if(exigeDocumentoPrincipal){
			return isPodeAssinar() ? true : false;
		}
		return true;
	}

	/**
	 * Seleciona um tipo de movimento a ser lançado.
	 * 
	 * @param tipoMovimento o tipo de movimento a ser selecionado.
	 */
	public void selecionarMovimento(Evento tipoMovimento){
		if(tipoMovimento == null){
			return;
		}
		try {
			if(eventoManager.isLeaf(tipoMovimento)){
				acrescentaMovimento(tipoMovimento);
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Erro ao tentar selecionar o movimento.");
		}
	}
	
	/**
	 * Observa um evento de upload de documento. Esse método deve ser associado com o 
	 * componente de upload.
	 * 
	 * @param uploadEvent o evento de upload a ser observado
	 */
	public void listener(UploadEvent uploadEvent){
		errosUpload.clear();
		List<MultipleFileUploadAction.UploadedFile> uploaded;
		try {
			uploaded = multipleFileUploadAction.listener(uploadEvent);
			for(MultipleFileUploadAction.UploadedFile uf: uploaded){
				ProcessoDocumento pd = processaUpload_(uf);
				arquivos.add(pd);
				facesMessages.add(Severity.INFO, "Finalizado o upload do arquivo {0} com sucesso.", uf.getFileName());
				setContador(getContador() + 1);
			}
		} catch (PJeBusinessException e) {
			String error = String.format("Não foi possível receber um dos arquivos: %s.", e.getLocalizedMessage());
			logger.error(error);
			errosUpload.add(error);
		}
	}
	
	/**
	 * Recupera o tamanho de um dado arquivo em formato simples para leitura humana.
	 * 
	 * @param idArquivo o identificador do arquivo enviado.
	 * @return representação em texto do tamanho
	 */
	public String tamanhoBytes(int idArquivo) {
		if(arquivos.get(idArquivo) != null){
			return StringUtil.tamanhoBytes(arquivos.get(idArquivo).getProcessoDocumentoBin().getSize(), false);
		}
		return "0 B";
	}
	
	/**
	 * Recupera a descrição do formato de dados do arquivo.
	 * 
	 * @param idArquivo o identificador do arquivo enviado
	 * @return a descrição do tipo de arquivo
	 */
	public String formatoArquivo(int idArquivo){
		String mimetype = null;
		if(arquivos.get(idArquivo) != null){
			mimetype = arquivos.get(idArquivo).getProcessoDocumentoBin().getExtensao();
		}
		if(mimetype == null || StringUtil.fullTrim(mimetype).isEmpty()){
			return "Formato não conhecido";
		}
		if(mimetype.startsWith("image")){
			return "Arquivo de imagem";
		}else if(mimetype.startsWith("audio")){
			return "Arquivo de áudio";
		}else if(mimetype.startsWith("video")){
			return "Arquivo de vídeo";
		}else if(mimetype.startsWith("application/pdf")){
			return "Arquivo PDF";
		}else{
			return mimetype;
		}
	}
	
	/**
	 * Remove o documento com o identificador dado da lista de arquivos a serem tratados.
	 * 
	 * @param indexArquivo o identificador do documento a ser removido.
	 * @throws PJeBusinessException 
	 */
	public void remove(Integer indexArquivo) throws PJeBusinessException{
		if(!arquivos.isEmpty() && arquivos.size() > indexArquivo){
			ProcessoDocumento rem = processoDocumentoManager.findById(arquivos.get(indexArquivo).getIdProcessoDocumento());
			arquivos.remove(indexArquivo.intValue());
			if(rem != null && rem.getIdProcessoDocumento() != 0){
				if (rem.getDataJuntada() != null) {
					processoDocumentoManager.inactivate(rem);
				} else {
					ProcessoDocumentoHome.instance().removerDocumento(rem, this.idProcessoJudicial);
				}
			}
			documentoPrincipal.getDocumentosVinculados().remove(rem);			
			flush_();
			if (EntityUtil.getEntityManager().contains(documentoPrincipal)) {
				processoDocumentoManager.refresh(documentoPrincipal);
			}
			setContador(getContador() - 1);
			ordenarColecaoProcessoDocumentoPeloIndiceDaLista();
		}
	}
	
	/**
	 * Remove os arquivos vinculados ao documento principal.
	 * 
	 * @throws PJeBusinessException 
	 */
	public void removeArquivos() throws PJeBusinessException{
		if(arquivos != null){
			while(!arquivos.isEmpty()){
				remove(0);
			}
		}
	}
	
	/**
	 * Acrescenta uma movimentação processual do tipo dado à lista a ser lançada quando do sucesso
	 * do protocolo.
	 * 
	 * @param tipoMovimento o tipo de movimento a partir do qual a movimentação deverá ser criada.
	 */
	public void acrescentaMovimento(Evento tipoMovimento){
		if(!codigosSelecionados.contains(tipoMovimento.getCode())){
			getTreeMovimentacoes().addSelected(tipoMovimento);
			codigosSelecionados.add(tipoMovimento.getCode());
		}
	}
	
	/**
	 * Salva o documento principal a partir do upload de um PDF.
	 * @return True se deu tudo certo ao gravar. False caso contrário.
	 */

	public void gravarRascunho(){
		gravarRascunho(true);
	}

	public void gravarDocumentoPdf(){
		FileHome fileHome = FileHome.instance();
		if (fileHome.getData() == null || fileHome.getData().length == 0) {
			facesMessages.add(Severity.WARN, "Por favor, faça upload do documento antes.");
		} else if (fileHome.getData().length > 5242880) {
			facesMessages.add(Severity.ERROR, "O arquivo é maior que 5MB.");
		} else {
			File temp = null;
			try {
				temp = File.createTempFile(
						"docPrincipal" + processoJudicial.getIdProcessoTrf() + java.time.LocalDateTime.now().getNano(),
						null);
				FileOutputStream fos = new FileOutputStream(temp);
				fos.write(fileHome.getData());
				fos.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.error("Erro ao tentar gravar o arquivo: {0}.", e1.getLocalizedMessage());
				facesMessages.add(Severity.ERROR, "Erro ao tentar gravar o arquivo: {0}", e1.getLocalizedMessage());
				return;
			}
			gravarDadosDocumentoPrincipal(false, temp, fileHome.getFileName(), new Long(temp.length()).intValue());
		}
	}

	/**
	 * Grava o rascunho de um documento HTML.
	 *
	 * @return True se deu tudo certo ao gravar. False caso contrário.
	 */
	public void gravarRascunho(boolean controleVersaoHabilitado) {
		AjaxDataUtil ajaxDataUtil = ComponentUtil.getComponent(AjaxDataUtil.class);

		File file = null;
		String nomeArquivo = null;
		Integer size = null;

		if (documentoPrincipal != null) {
			ProcessoDocumentoBin pdb = documentoPrincipal.getProcessoDocumentoBin();

			if (pdb != null) {
				file = pdb.getFile();
				nomeArquivo = pdb.getNomeArquivo();
				size = pdb.getSize();
			}
		}

		if (gravarDadosDocumentoPrincipal(controleVersaoHabilitado, file, nomeArquivo, size)) {
			ajaxDataUtil.sucesso();
		} else {
			ajaxDataUtil.erro();
		}
	}

	/**
	 * Persiste as informaes do documento principal (vincula os anexos ao documento
	 * principal, vincula ele ao processo judicial, etc), inclusive persistindo no
	 * storage, conforme configurao (vide maven profile apropriado). Se o documento
	 * principal for HTML, grava sua verso.
	 * @param arquivo
	 *
	 * @return True se deu tudo certo ao gravar. False caso contrrio.
	 */
	private boolean gravarDadosDocumentoPrincipal(boolean controleVersaoHabilitado, File arquivo, String nomeArquivo,
			Integer tamanhoArquivo) {
		ProcessoDocumentoBin pdb = documentoPrincipal.getProcessoDocumentoBin();

		boolean docPrincipalIsPdf = getTipoDocumentoPrincipal(pdb, nomeArquivo,
				tamanhoArquivo) == TipoDocumentoPrincipalEnum.PDF;
		boolean docPrincipalIsHtml = getTipoDocumentoPrincipal(pdb, nomeArquivo,
				tamanhoArquivo) == TipoDocumentoPrincipalEnum.HTML;

		boolean docPrincipalHtmlVazio = docPrincipalIsHtml
				&& (pdb.getModeloDocumento() == null || "".equals(pdb.getModeloDocumento().trim()));
		boolean docPrincipalPdfVazio = docPrincipalIsPdf
				&& (nomeArquivo == null || nomeArquivo.isEmpty() || "".equals(nomeArquivo) || tamanhoArquivo == 0);

		if ((docPrincipalIsHtml && docPrincipalHtmlVazio == false)
				|| (docPrincipalIsPdf && docPrincipalPdfVazio == false)) {
			validarColecaoProcessoDocumento();
			if (isPodeOrdenarDocumentos()) {
				arquivos.forEach(pd -> pd.setDocumentoPrincipal(documentoPrincipal));
				documentoPrincipal.getDocumentosVinculados().clear();
				documentoPrincipal.getDocumentosVinculados().addAll(arquivos);
			}
			documentoPrincipal.setProcessoTrf(processoJudicial);
			documentoPrincipal.setProcesso(processoJudicial.getProcesso());
			documentoPrincipal.setPapel(papelService.getPapelAtual());
			documentoPrincipal.setLocalizacao(Authenticator.getLocalizacaoAtual());

			if (docPrincipalIsPdf) {
				if (arquivo != null) {
					pdb.setFile(arquivo);
					pdb.setNomeArquivo(nomeArquivo);
					pdb.setSize(tamanhoArquivo);

					configuraDocumentoParaPdf(pdb);
				}
			} else if (docPrincipalIsHtml) {
				configuraDocumentoParaHtml(pdb);
			}

			try {
				documentoJudicialService.persist(documentoPrincipal, true);
				if (controleVersaoHabilitado && docPrincipalIsHtml) {
					controleVersaoDocumentoManager.salvarVersaoDocumento(documentoPrincipal);
				}
				flush_();

				PontoExtensaoResposta pontoExtensaoResposta = custasJudiciaisHome
						.validarDadosCustasJuntada(documentoPrincipal);

				if (CodigoRespostaGuiaRecolhimentoEnum.INVALIDO.getLabel().equals(pontoExtensaoResposta.getCodigo())) {
					facesMessages.clear();
					facesMessages.add(Severity.ERROR, pontoExtensaoResposta.getMensagem());
				}
			} catch (PJeBusinessException e) {
				logger.error("Erro ao tentar gravar o arquivo: {0}. A conversao ficar ativa.", e.getLocalizedMessage());
				facesMessages.add(Severity.ERROR, "Erro ao tentar gravar o rascunho: {0}", e.getLocalizedMessage());
				return false;
			} catch (PJeDAOException e) {
				facesMessages.add(Severity.ERROR, "Erro ao tentar gravar o rascunho: {0}", e.getLocalizedMessage());
				return false;
			}
		} else {
			facesMessages.add(Severity.ERROR, "Documento nao pode estar vazio.");
			return false;
		}

		return true;
	}

	/**
	 * Método que deve ser chamado quando o usuário clicar em assinar.
	 */
	public void prepararAssinatura() {
		AjaxDataUtil ajaxDataUtil = ComponentUtil.getComponent(AjaxDataUtil.class);

		// Se é advogado, não está habilitado nos autos e isso é relevante neste caso...
		// Deveria ter escolhido o parte que representa
		if (isAdvogadoNaoHabilitado()) {
			if (!terceiroInteressado && !mapaProcessoParteAtivaSelecionada.containsValue(true)
					&& !mapaProcessoPartePassivaSelecionada.containsValue(true)) {
				facesMessages.add(Severity.ERROR,
						"Por favor, indique a parte que representa no processo ou marque a opção Não Represento Quaisquer das Partes.");
				return;
			}
		}

		// Se o usuário logado pode responer expedientes, há expedientes e nenhum foi
		// selecionado, critica...
		if (permiteVincularResposta && respostaBean != null && respostaBean.getExpedientes().size() > 0
				&& !respostaBean.getSelecionados().containsValue(true)) {
			if (!responderNenhumExpediente) {
				facesMessages.add(Severity.ERROR,
						"Por favor, indique qual expediente sua petição responde, ou marque a opção Minha Petição Não Responde Nenhum Expediente.");
				return;
			}
		}

		File file = null;
		String nomeArquivo = null;
		Integer size = null;

		if (documentoPrincipal != null) {
			ProcessoDocumentoBin pdb = documentoPrincipal.getProcessoDocumentoBin();

			if (pdb != null) {
				file = pdb.getFile();
				nomeArquivo = pdb.getNomeArquivo();
				size = pdb.getSize();
			}
		}

		if (gravarDadosDocumentoPrincipal(true, file, nomeArquivo, size)) {
			ajaxDataUtil.sucesso();
		} else {
			ajaxDataUtil.erro();
		}
	}

	public void excluirDocumentoPdf() {
		if (documentoPrincipal != null) {
			try {
				if (DiligenciaHome.instance() == null) {
					acaoRemoverTodos();

					ProcessoDocumentoHome.instance().removerDocumento(documentoPrincipal, idProcessoJudicial);
					sincronizarProcessoDocumentoComProcessoTrf(false);
				}

				documentoPrincipal.getProcessoDocumentoBin().setNomeArquivo(null);
				documentoPrincipal.getProcessoDocumentoBin().setFile(null);

				configuraDocumentoParaPdf(documentoPrincipal.getProcessoDocumentoBin());
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}
	}

	public void tipoDocumentoPrincipalChanged(ValueChangeEvent event) {
		if (Objects.nonNull(documentoPrincipal)) {
			excluirDocumentoPdf();

			if (event.getNewValue().toString().equals("PDF")) {
				configuraDocumentoParaPdf(documentoPrincipal.getProcessoDocumentoBin());
			} else if (event.getNewValue().toString().equals("HTML")) {
				configuraDocumentoParaHtml(documentoPrincipal.getProcessoDocumentoBin());
			}
		}
	}

	private TipoDocumentoPrincipalEnum getTipoDocumentoPrincipal(ProcessoDocumentoBin pdb, String nomeArquivo, Integer tamanhoArquivo) {
		if (pdb == null) {
			return TipoDocumentoPrincipalEnum.HTML;
		}

		boolean isExtensaoNula = StringUtil.isEmpty(pdb.getExtensao()) || "".equals(pdb.getExtensao());
		boolean isExtensaoPdf = isExtensaoNula == false && "application/pdf".equals(pdb.getExtensao());
		boolean modeloDocumentoIsNullOrEmpty = (StringUtil.isSet(pdb.getModeloDocumento()) == false)
				|| "".equals(pdb.getModeloDocumento());
		boolean arquivoIsNullOrEmpty = nomeArquivo == null || nomeArquivo.isEmpty() || "".equals(nomeArquivo)
				|| tamanhoArquivo == 0;

		boolean docPrincipalIsPDF = (isExtensaoPdf && modeloDocumentoIsNullOrEmpty) || (arquivoIsNullOrEmpty == false);
		boolean docPrincipalIsHtml = (arquivoIsNullOrEmpty == true && isExtensaoPdf == false)
				|| (isExtensaoNula && arquivoIsNullOrEmpty);

		if (docPrincipalIsPDF) {
			tipoDocumentoPrincipal = TipoDocumentoPrincipalEnum.PDF;
		} else if (docPrincipalIsHtml) {
			tipoDocumentoPrincipal = TipoDocumentoPrincipalEnum.HTML;
		}

		return tipoDocumentoPrincipal;
	}

	private void escolheTipoDocumentoPrincipal() {
		if (Objects.nonNull(documentoPrincipal)) {
			ProcessoDocumentoBin pdb = documentoPrincipal.getProcessoDocumentoBin();

			if (pdb != null) {
				if (pdb.getModeloDocumento() == null || "".equals(pdb.getModeloDocumento().trim())) {
					tipoDocumentoPrincipal = TipoDocumentoPrincipalEnum.PDF;
				} else {
					tipoDocumentoPrincipal = getTipoDocumentoPrincipal(pdb, pdb.getNomeArquivo(), pdb.getSize());
				}
			} else {
				tipoDocumentoPrincipal = TipoDocumentoPrincipalEnum.HTML;
			}
		} else {
			tipoDocumentoPrincipal = TipoDocumentoPrincipalEnum.HTML;
		}
	}

	private void configuraDocumentoParaPdf(ProcessoDocumentoBin pdb) {
		if (pdb == null) {
			return;
		}

		pdb.setBinario(true);
		pdb.setExtensao("application/pdf");
		pdb.setDataAssinatura(null);
		pdb.setModeloDocumento(null);

		if (pdb.getFile() != null) {
			pdb.setSize(new Long(pdb.getFile().length()).intValue());
		}

		tipoDocumentoPrincipal = TipoDocumentoPrincipalEnum.PDF;
	}

	private void configuraDocumentoParaHtml(ProcessoDocumentoBin pdb) {
		if (pdb == null) {
			return;
		}

		pdb.setBinario(false);
		pdb.setExtensao("text/html");
		pdb.setNomeArquivo(null);
		pdb.setDataAssinatura(null);
		pdb.setFile(null);

		if (pdb.getModeloDocumento() != null) {
			pdb.setSize(pdb.getModeloDocumento().length());
		}

		tipoDocumentoPrincipal = TipoDocumentoPrincipalEnum.HTML;
	}

	/**
	 * Apaga todos os documentos que foram salvos sem estarem preenchidos. Mesmo sendo rascunhos.
	 * Utilizado na elaboração do acórdão.
	 */
	public void removeRascunhosEmBranco(ProcessoTrf processoTrf) throws PJeBusinessException {
		TipoProcessoDocumento tipoDocRelatorio = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
		TipoProcessoDocumento tipoDocEmenta = ParametroUtil.instance().getTipoProcessoDocumentoEmenta();
		TipoProcessoDocumento tipoDocNotasOrais = ParametroUtil.instance().getTipoProcessoDocumentoNotasOrais();
		TipoProcessoDocumento tipoDocVoto = ParametroUtil.instance().getTipoProcessoDocumentoVoto();

		Integer[] tiposDocumento = new Integer[] {tipoDocRelatorio.getIdTipoProcessoDocumento(), tipoDocEmenta.getIdTipoProcessoDocumento(), tipoDocNotasOrais.getIdTipoProcessoDocumento(),
				tipoDocVoto.getIdTipoProcessoDocumento()};

		documentoJudicialService.excluirDocumentosEmBrancoPorTipo(processoTrf, tiposDocumento);
		documentoJudicialService.flush();
	}

	/**
	 * Grava os dados de eventual protocolo, sem concretizar o protocolo com a assinatura e a juntada.
	 * Deve ser utilizado para manter o conjunto de arquivos no sistema, mas sem a sua inclusão concreta
	 * no processo.
	 */
	public void gravarTodos(){
		if(arquivos.size() > 0){		
			for(ProcessoDocumento pd: arquivos){
				if (pd.getProcessoDocumentoBin().getFile() != null) {
					gravar(pd);
				}
			}
		}
	}

	/**
	 * Atualiza o tipo dos documentos dos arquivos enviados em lote no CKEditor.
	 * Utiliza a informação do tipo processo documento que foi selecionado anteriormente para atualizar os arquivos enviados.
	 */
        public void atualizarTiposDocumentosDosArquivosEnviadosEmLoteNoCKEditor(){
                if (this.getArquivos() != null){
                        for (ProcessoDocumento processoDocumento : this.getArquivos()){
                                if (processoDocumento.getSelecionadoParaUploadEmLote()){
                                        processoDocumento.setTipoProcessoDocumento(getTipoSelecionadoParaUploadEmLoteNoCKEditor());
                                }
                        }
                }
        }
        
	/**
	 * Atualiza a flag que define se o arquivo será enviado em lote.
	 * Marca (ou desmarca, de acordo com o valor da flag "marcarDesmarcarTodosUploadEmLoteNoCKEditor")
         * todos os arquivos como arquivos a serem enviados em lote. É utilizado por checkBox no front-end.
         */
        public void marcarDesmarcarTodosArquivosComoEnviadosEmLoteNoCKEditor(){
                if (this.getArquivos() != null){
                        for (ProcessoDocumento processoDocumento : this.getArquivos()){
                                processoDocumento.setSelecionadoParaUploadEmLote(getMarcarDesmarcarTodosUploadEmLoteNoCKEditor());
                        }
                }
        }
        
	/**
	 * Grava o(s) arquivo(s) passados via upload.
	 * 
	 * @param uploadEvent Objeto com o(s) arquivo(s) agregado(s).
	 */
	public void gravar(UploadEvent uploadEvent){
		errosUpload.clear();
                setMarcarDesmarcarTodosUploadEmLoteNoCKEditor(false);
                List<MultipleFileUploadAction.UploadedFile> uploaded;
                ProcessoDocumento pd = null;
		try {
			validarColecaoProcessoDocumento();
			uploaded = multipleFileUploadAction.listener(uploadEvent);
			for(int indice = 0; indice < uploaded.size(); indice++){
				uploaded.get(indice).setFileName(Utf8ParaIso88591Util.converter(uploaded.get(indice).getFileName())); 
				MultipleFileUploadAction.UploadedFile uf = uploaded.get(indice);
				pd = processaUpload_(uf);
                                pd.setSelecionadoParaUploadEmLote(false);
                                verificaAnexoLibreOffice(pd);
				gravar(pd);
				if (this.getArquivos() != null && this.getArquivos().contains(pd)){
					facesMessages.add(Severity.INFO, "Finalizado o upload do arquivo {0} com sucesso.", uf.getFileName());
				}
				setContador(getContador() + 1);
			}
			processoJudicial = processoJudicialService.findById(this.idProcessoJudicial);
		} catch (PJeBusinessException e) {
			if (this.getArquivos() != null && pd != null && this.getArquivos().contains(pd)){
				this.getArquivos().remove(pd);
			}
			String error = String.format("Não foi possível receber um dos arquivos: %s.", e.getLocalizedMessage());
			logger.error(error);
			errosUpload.add(error);
		}
		
		if(errosUpload != null && errosUpload.size()> 0){
			for (String erro : errosUpload) {
				facesMessages.add(Severity.ERROR, erro);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private ProcessoDocumento verificaAnexoLibreOffice(ProcessoDocumento pd) throws PJeBusinessException {
		if(ParametroUtil.instance().isEditorLibreOfficeHabilitado()) {
			ProcessInstance pi = null;
			pi = org.jboss.seam.bpm.ProcessInstance.instance();
			if(pi != null){
	    		Integer idMinuta = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(TaskInstance.instance());
	    	    if (idMinuta != null) {
	    	        documentoPrincipal = documentoJudicialService.getDocumento(idMinuta);
	    	        pd.setDocumentoPrincipal(documentoPrincipal);
	    	        pd.setTipoProcessoDocumento(obtemTipoDocumentoAnexo());
	    	        arquivos.add(pd);
					ordenarColecaoProcessoDocumentoPeloIndiceDaLista();
	    	    }
    	    }
		}
		return pd;
	}
	
	/**
	 * Grava um ProcessoDocumento.
	 * 
	 * @param pd ProcessoDocumento
	 */
	public void gravar(ProcessoDocumento pd){
		
		gravarRascunho();
		logger.info("O número de arquivos é de [{0}]" , arquivos.size());
		
		if(pd.getTipoProcessoDocumento() != null && pd.getProcessoDocumento() != null && !StringUtil.fullTrim(pd.getProcessoDocumento()).isEmpty()){
			try {
				documentoJudicialService.persist(pd, true);
				logger.info("Gravando o arquivo {0}, do tipo {1}, com tamanho {2}", pd.getProcessoDocumento(), pd.getTipoProcessoDocumento(), pd.getProcessoDocumentoBin().getSize());
			} catch (PJeBusinessException e) {
				if (this.getArquivos() != null && pd != null && this.getArquivos().contains(pd)){
					this.getArquivos().remove(pd);
				}
				facesMessages.add(Severity.ERROR, "Erro ao tentar protocolar: {0}", e.getLocalizedMessage());
				return;
			}
		} else if (pd.getTipoProcessoDocumento() == null) {
                        facesMessages.add(Severity.ERROR, "Erro ao tentar gravar o arquivo: o tipo do documento não foi informado.");
			return;
                } 
		flush_();
	}
	
	
	public boolean concluir() throws Exception {
		return concluir(true, false);
	}
	
	public boolean concluirDocumento() throws Exception {
		return concluir(false, false);
	}
	
	
	/**
	 * Finaliza a gravação do documento, fazendo sua pertinente juntada aos autos e lançando,
	 * quando pertinente, os movimentos associados e o registro de eventuais respostas.
	 * @throws Exception
	 */
	private boolean concluir(boolean refresh, boolean carregaDocumentoPendenteDeAtividadeEspecifica) throws Exception {
		Usuario usuarioLogado = Authenticator.getUsuarioLogado();
		Papel papelUsuarioLogado = Authenticator.getPapelAtual();
		Localizacao localizacaoUsuarioLogado = Authenticator.getLocalizacaoAtual();
		
		boolean guiaRecolhimentoInvalida = false;

		if (getAtendimentoPlantao() && !PlantaoJudicialService.instance().verificarPlantao()) {
			setAtendimentoPlantao(false);
		}
		
		if(documentoPrincipal != null && documentoPrincipal.getIdProcessoDocumento() > 0){
			for(ProcessoDocumento pd: arquivos){
				if (pd.getProcessoDocumentoBin().getFile() != null) {
					if(pd.getDocumentoPrincipal() != documentoPrincipal){
						pd.setDocumentoPrincipal(documentoPrincipal);
					}
				}
			}
			try {

				PontoExtensaoResposta pontoExtensaoResposta = custasJudiciaisHome
						.validarDadosCustasJuntada(documentoPrincipal);

				if (CodigoRespostaGuiaRecolhimentoEnum.INVALIDO.getLabel().equals(pontoExtensaoResposta.getCodigo())) {
					guiaRecolhimentoInvalida = true;
					throw new Exception(pontoExtensaoResposta.getMensagem());
				} else {
					pontoExtensaoResposta = custasJudiciaisHome.salvarDadosCustasJuntada(documentoPrincipal);

					if (CodigoRespostaGuiaRecolhimentoEnum.INVALIDO.getLabel()
							.equals(pontoExtensaoResposta.getCodigo())) {
						guiaRecolhimentoInvalida = true;
						throw new Exception(pontoExtensaoResposta.getMensagem());
					}
				}

				if(tipoDocumentoPrincipal == TipoDocumentoPrincipalEnum.HTML && (documentoPrincipal.getProcessoDocumentoBin().getModeloDocumento() == null || documentoPrincipal.getProcessoDocumentoBin().getModeloDocumento().isEmpty())){
					facesMessages.add(Severity.ERROR, "Erro ao tentar protocolar: o conteúdo do documento principal está vazio");
					return false;
				}
				
				if (isAdvogadoNaoHabilitado()) {
					habilitarNosAutos();
				}
				
				if (getAtendimentoPlantao()) {
					PlantaoJudicialService.instance().registraSeDeveIrParaPlantao();
				}
				
				if(refresh){
					documentoJudicialService.refresh(documentoPrincipal);
				}
				if(documentoPrincipal.getDocumentoSigiloso() && documentoPrincipal.getProcessoTrf() != null){
					documentoPrincipal.getProcessoTrf().setApreciadoSigilo(ProcessoTrfApreciadoEnum.A);
				}
				if (documentoPrincipal.getNomeUsuarioAlteracao() == null) {
					documentoPrincipal.setNomeUsuarioAlteracao(usuarioLogado.getNome());
				}
				if(documentoPrincipal.getDataJuntada() == null && processoJudicial.getProcessoStatus() == ProcessoStatusEnum.D){
					documentoPrincipal.setDataJuntada(new Date());
				}
				documentoJudicialService.configurarNomeUsuarioJuntada(documentoPrincipal, usuarioLogado, papelUsuarioLogado, localizacaoUsuarioLogado);
				if(lancaMovimentacao){
					lancarMovimentos();
				}
				if(permiteVincularResposta){
					respostaBean.registrarResposta(documentoPrincipal);
				}
				documentoJudicialService.dispararFluxo(documentoPrincipal);
				documentoJudicialService.flush();
				retiraInformacaoAssinaturaCacheGridDocumentos(documentoPrincipal);
				this.consultaProcessualAction = ComponentUtil.getComponent(ConsultaProcessualAction.NAME);
				this.consultaProcessualAction.verificarVisualizacaoProcesso();

				// Caso o usuário externo não esteja habilitado no processo, o documento será cadastrado como petição avulsa.
				if (!Identity.instance().hasRole(Papeis.INTERNO) && 
						ProcessoStatusEnum.D.equals(documentoPrincipal.getProcessoTrf().getProcessoStatus()) && 
						(!processoParteManager.isParte(documentoPrincipal.getProcessoTrf()))) {
					ProcessoDocumentoHome processoDocumentoHome = ProcessoDocumentoHome.instance();
					processoDocumentoHome.setInstance(this.documentoPrincipal);
					processoDocumentoHome.gravarProcessoDocumentoPeticao();
				}

				// remove as versões de documento na data de juntada
				controleVersaoDocumentoManager.deletarTodasVersoesIdDocumento(documentoPrincipal.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
			} catch (BusinessException | PJeBusinessException e) {
				if (guiaRecolhimentoInvalida) {
					throw new PJeBusinessException(e.getLocalizedMessage());
				}

				logger.error("Erro ao concluir gravação dos arquivos: {0}. A conversação ficará ativa.",
						e.getLocalizedMessage());
				facesMessages.add(Severity.ERROR, "Erro ao concluir gravação dos arquivos: {0}",
						e.getLocalizedMessage());
				return false;
			} catch (Throwable e){
				if (guiaRecolhimentoInvalida) {
					throw new Exception(e.getLocalizedMessage());
				}

				logger.fatal("Erro ao concluir gravação dos arquivos: {0}.", e.getLocalizedMessage());
				facesMessages.add(Severity.ERROR, "Erro ao concluir gravação dos arquivos: {0}",
						e.getLocalizedMessage());
				return false;
			}
		}
		
		if(arquivos != null && arquivos.size() > 0){
			logger.info("O número de arquivos é de [{0}]" , arquivos.size());
			try {
				GregorianCalendar gd = (GregorianCalendar)GregorianCalendar.getInstance();
				gd.setTime(documentoPrincipal.getDataInclusao());
				int qtdDocAnexosPreExistente = processoDocumentoManager.contagemDocumentosAnexos(documentoPrincipal);
				for(ProcessoDocumento pd: arquivos){
					if (pd.getIdProcessoDocumento() > 0) {
						gd.add(Calendar.MILLISECOND, 1);
						pd.setDataInclusao(gd.getTime());
						if (dataJuntadaPropria) {
							pd.setDataJuntada(new Date());
						} else if (documentoPrincipal != null && documentoPrincipal.getDataJuntada() != null && !pd.getProcessoDocumentoBin().getSignatarios().isEmpty()) {
							pd.setDataJuntada(documentoPrincipal.getDataJuntada());
						}
						documentoJudicialService.configurarNomeUsuarioJuntada(pd, usuarioLogado, papelUsuarioLogado, localizacaoUsuarioLogado);
						pd.getProcessoDocumentoBin().setValido(ProcessoDocumentoHome.instance().estaValido(documentoPrincipal));
						pd.setNumeroOrdem(pd.getNumeroOrdem() + qtdDocAnexosPreExistente);
						documentoJudicialService.persist(pd, false);
						logger.debug("Gravando o arquivo {0}, do tipo {1}, com tamanho {2}", pd.getProcessoDocumento(), pd.getTipoProcessoDocumento(), pd.getProcessoDocumentoBin().getSize());
						
						documentoPrincipal.getDocumentosVinculados().add(pd);
						retiraInformacaoAssinaturaCacheGridDocumentos(pd);
					}
				}
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Erro ao tentar protocolar: {0}", e.getLocalizedMessage());
				return false;
			} catch (Throwable e){
				facesMessages.add(Severity.ERROR, "Erro ao tentar protocolar: {0}", e.getLocalizedMessage());
				return false;
			}
		}else{
			logger.debug("Não há arquivos anexos para finalizar." );
		}
		flush_();
		this.gerarCertidao();
		try{
			String numeroGuia = documentoPrincipal.getNumeroGuia();

			documentoPrincipal = null;
			ultimaAtualizacao = null;
			arquivos.clear();
			arquivosAssinados.clear();
			setTreeMovimentacoes(new EventsTreeHandler(false));

			carregaDocumentoPendente(carregaDocumentoPendenteDeAtividadeEspecifica);
			setAtendimentoPlantao(false);

			Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_ATUALIZAR_GUIA_RECOLHIMENTO_POS_JUNTADA,getProcessoJudicial(),numeroGuia);
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
		} catch (BusinessException e) {
			facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
		}
		facesMessages.add(Severity.INFO, "Documento(s) assinado(s) com sucesso.");
		return true;
	}
	
	private void habilitarNosAutos() throws PJeBusinessException {
		
		//Só vincula o advogado a alguma parte se ele não escolheu a opção 'terceiro não habilitado'
		if (!terceiroInteressado) {
			
			PessoaAdvogado pessoaAdvogado = ((PessoaFisica) ProcessoHome.instance().getUsuarioLogado()).getPessoaAdvogado();
			Usuario usuarioSolicitante = ProcessoHome.instance().getUsuarioLogado();
			UsuarioLocalizacao usuarioLocalizacao = new UsuarioLocalizacao();
			usuarioLocalizacao.setUsuario(usuarioSolicitante);
			
			if (processoJudicial.getSegredoJustica() && !processoJudicialService.visivel(processoJudicial, usuarioLocalizacao, null)){
				habilitacaoAutosService.salvarHabilitacaoManual(pessoaAdvogado, processoJudicial);
			}
			else {
				if (!mapaProcessoParteAtivaSelecionada.containsValue(true) && !mapaProcessoPartePassivaSelecionada.containsValue(true)) {
					throw new PJeBusinessException("Erro. Nenhuma parte selecionada.");
				}
				
				List<ProcessoParte> processoParteList = new ArrayList<ProcessoParte>();
				
				if (mapaProcessoParteAtivaSelecionada.containsValue(true)) {
					mapaProcessoParteAtivaSelecionada.forEach((parte, selecionada) -> {
						if (selecionada) {
							processoParteList.add(parte);	
						}
					});
				}
				else {
					mapaProcessoPartePassivaSelecionada.forEach((parte, selecionada) -> {
						if (selecionada) {
							processoParteList.add(parte);	
						}
					});
				}
				
				habilitacaoAutosService.salvarHabilitacaoAutomatica(
					pessoaAdvogado,
					usuarioSolicitante,
					processoJudicial,
					processoParteList, 
					null,
					TipoDeclaracaoEnum.P, //<-?
					TipoSolicitacaoHabilitacaoEnum.S); //<-?
			}
		}
	}
	
	private void retiraInformacaoAssinaturaCacheGridDocumentos(ProcessoDocumento processoDocumento){
		String procDocBin = String.valueOf(processoDocumento.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
		
		if(ProcessoDocumentoBinPessoaAssinaturaHome.instance() != null &&
				ProcessoDocumentoBinPessoaAssinaturaHome.instance().getListaIdsAssinaturas() != null && 
				ProcessoDocumentoBinPessoaAssinaturaHome.instance().getListaIdsAssinaturas().containsKey(procDocBin)){
			ProcessoDocumentoBinPessoaAssinaturaHome.instance().getListaIdsAssinaturas().remove(procDocBin);			
		}
	}	
	
	/**
	 * Método responsável por gerar o documento de certidão de protocolo do documento para processos com o status distribuído.
	 */
	private void gerarCertidao() {
		if (documentoPrincipal != null && documentoPrincipal.getProcessoTrf() != null && 
				documentoPrincipal.getProcessoTrf().getProcessoStatus().equals(ProcessoStatusEnum.D)) {
			try {
				ComponentUtil.getComponent(DocumentoCertidaoAction.class).gerarCertidao(documentoPrincipal);
			} catch (PJeBusinessException ex) {
				facesMessages.add(Severity.ERROR, "Erro ao tentar gerar o documento de certidão.");
			}
		}
	}
	
	public List<TipoProcessoDocumento> getTiposDocumentosTextoJuntada(){
		try {
			List<TipoProcessoDocumento> tipos =  documentoJudicialService.getTiposDisponiveis(papelService.getPapelAtual(), true, TipoDocumentoEnum.P, TipoDocumentoEnum.T);
			
			if(processoJudicial != null){
				boolean isDistribuido = processoJudicial.getProcessoStatus().equals(ProcessoStatusEnum.D);
				
				if(isDistribuido) {
					tipos.remove(processoJudicial.getClasseJudicial().getTipoProcessoDocumentoInicial());
				}
			}
			return tipos;
		} catch (Exception e) {
			logger.error("Não foi possível recuperar a lista de tipos de documentos disponíveis: {0}", e.getLocalizedMessage());
			return Collections.emptyList();
		}
	}
	
	public List<TipoProcessoDocumento> getTiposDocumentosTexto(Boolean removeTiposSensiveis){
		try {
			List<TipoProcessoDocumento> tipos =  documentoJudicialService.getTiposDisponiveis(papelService.getPapelAtual(), TipoDocumentoEnum.P, TipoDocumentoEnum.T);
			if(removeTiposSensiveis && tipos != null && tipos.size() > 0){
				if(!ParametroUtil.instance().isPrimeiroGrau()){
					tipos.remove(ParametroUtil.instance().getTipoProcessoDocumentoAcordao());
					tipos.remove(ParametroUtil.instance().getTipoProcessoDocumentoRelatorio());
					tipos.remove(ParametroUtil.instance().getTipoProcessoDocumentoEmenta());
					tipos.remove(ParametroUtil.instance().getTipoProcessoDocumentoVoto());
				}
				tipos.remove(ParametroUtil.instance().getTipoProcessoDocumentoDecisao());
				tipos.remove(ParametroUtil.instance().getTipoProcessoDocumentoDespacho());
				tipos.remove(ParametroUtil.instance().getTipoProcessoDocumentoSentenca());
				
				if(processoJudicial != null){
					
					//[PJEII-25355] O tipo inicial não deve estar disponível para processos já distribuídos.
					boolean isDistribuido = processoJudicial.getProcessoStatus().equals(ProcessoStatusEnum.D);
					
					if(isDistribuido) {// || existePeticaoInicialValida()) {
						tipos.remove(processoJudicial.getClasseJudicial().getTipoProcessoDocumentoInicial());
					}
				}
			}
			return tipos;
		} catch (Exception e) {
			logger.error("Não foi possível recuperar a lista de tipos de documentos disponíveis: {0}", e.getLocalizedMessage());
			return Collections.emptyList();
		}
	}
	
	/**
	 * Recupera a lista de tipos de documento do tipo texto ou todos, ordenados pelo seu nome,
	 * permitidos para o tipo de papel do usuário atual.
	 * 
	 * @return a lista de tipos de documento
	 * #see {@link TipoDocumentoEnum#T}
	 * #see {@link TipoDocumentoEnum#P}
	 */
	public List<TipoProcessoDocumento> getTiposDocumentosTexto(){
		return getTiposDocumentosTexto(true);
	}
	
	/**
	 * Recupera a lista de tipos de documento que podem sofrer upload, ordenados por seu nome.
	 * 
	 * @return a lista de tipos de documento
	 * #see {@link TipoDocumentoEnum#T}
	 * #see {@link TipoDocumentoEnum#D}
	 */
	public List<TipoProcessoDocumento> getTiposDocumentos(){
		return getTiposDocumentos(TipoDocumentoEnum.D, TipoDocumentoEnum.T);
	}
	
	/**
	 * Recupera a lista de tipos de documento que podem sofrer upload, ordenados por seu nome.
	 *
	 * @param tipoDocumentoEnum Tipos do documentos que serão consultados.
	 * @return a lista de tipos de documento
	 * #see {@link TipoDocumentoEnum#T}
	 * #see {@link TipoDocumentoEnum#D}
	 */
	public List<TipoProcessoDocumento> getTiposDocumentos(TipoDocumentoEnum... tipoDocumentoEnum){
		try {
			List<TipoProcessoDocumento> tipos = documentoJudicialService.getTiposDisponiveis(papelService.getPapelAtual(), tipoDocumentoEnum);
			if(this.processoJudicial != null){
				tipos.remove(this.processoJudicial.getClasseJudicial().getTipoProcessoDocumentoInicial());
			}
			return tipos;
		} catch (Exception e) {
			logger.error("Não foi possível recuperar a lista de tipos de documentos disponíveis: {0}", e.getLocalizedMessage());
			return Collections.emptyList();
		}
	}
	
	/**
	 * Método responsável por retornar uma lista de tipos de documento associados 
	 * ao papel do usuário logado acrescido do tipo do documento passado por parâmetro.
	 * 
	 * @param documento Documento.
	 * @return 
	 */
	public List<TipoProcessoDocumento> obtemTiposDocumentos(ProcessoDocumento documento) {
		List<TipoProcessoDocumento> retorno = getTiposDocumentos();
		if(documento != null && documento.getTipoProcessoDocumento() != null && !retorno.contains(documento.getTipoProcessoDocumento())) {
			retorno.add(documento.getTipoProcessoDocumento());
		}
		return retorno;
	}
	
	/**
	 * Método responsável por retornar documento relativo ao anexo.
	 * 
	 * @return 
	 */
	public TipoProcessoDocumento obtemTipoDocumentoAnexo() {
		List<TipoProcessoDocumento> lista = getTiposDocumentos();
		return lista.get(0);
	}
	
	/**
	 * Recupera o documento principal deste componente.
	 * 
	 * @return o documento principal
	 */
	public ProcessoDocumento getDocumentoPrincipal() {
		return documentoPrincipal;
	}
	
	public void setDocumentoPrincipal(ProcessoDocumento documentoPrincipal){
		this.documentoPrincipal = documentoPrincipal;
	}
	
	/**
	 * Recupera o mapa de arquivos anexados ao documento principal.
	 * 
	 * @return o mapa de arquivos.
	 */
	public ArrayList<ProcessoDocumento> getArquivos() {
		if (arquivos == null) {			
			loadArquivosAnexadosDocumentoPrincipal();
		}
		return arquivos;
	}
	
	/**
	 * Recupera a lista de arquivos anexados ao documento principal.
	 * 
	 * @return lista de arquivos.
	 */
	public void loadArquivosAnexadosDocumentoPrincipal() {
		ArrayList<ProcessoDocumento> ret = new ArrayList<ProcessoDocumento>(0);
		if(this.exigeDocumentoPrincipal && this.documentoPrincipal != null && this.documentoPrincipal.getIdProcessoDocumento() != 0) {
			List<ProcessoDocumento> listaVinculados = processoDocumentoManager.getDocumentosVinculados(this.documentoPrincipal);
			for (ProcessoDocumento vinculado : listaVinculados) {
				if (vinculado.getAtivo() != null && vinculado.getAtivo()) {
					ret.add(vinculado);
				}
			}
		}
		arquivos = ret;
	}
	
	/**
	 * Recarrega a lista de documentos anexo e ordena.
	 * 
	 */
	public void reorganizarDocumentosAnexos(){
		loadArquivosAnexadosDocumentoPrincipal();
		ordenarColecaoProcessoDocumentoPeloNumeroOrdem();
	}

	/**
	 * Atribui a este componente um mapa de arquivos anexos.
	 * 
	 * @param arquivos o mapa a ser anexado.
	 */
	public void setArquivos(ArrayList<ProcessoDocumento> arquivos) {
		this.arquivos = new ArrayList<ProcessoDocumento>(arquivos);
	}
        
        
	/**
	 * Recupera o tipo de processo documento selecionado para upload em lote quando o anexo é feito dentro do CKEditor.
	 * 
	 * @return o tipo de processo documento.
	 */
        public TipoProcessoDocumento getTipoSelecionadoParaUploadEmLoteNoCKEditor() {
                return tipoSelecionadoParaUploadEmLoteNoCKEditor;
        }

	/**
	 * Atribui a este componente um tipo de processo documento.
	 * 
	 * @param tipoSelecionadoParaUploadEmLote o tipo de documento selecionado para upload em lote.
	 */
        public void setTipoSelecionadoParaUploadEmLoteNoCKEditor(TipoProcessoDocumento tipoSelecionadoParaUploadEmLote) {
                this.tipoSelecionadoParaUploadEmLoteNoCKEditor = tipoSelecionadoParaUploadEmLote;
        }
        
        /**
	 * Recupera o valor da flag marcarDesmarcarTodos quando o anexo é feito dentro do CKEditor.
	 * 
	 * @return o tipo de processo documento.
	 */        
        public Boolean getMarcarDesmarcarTodosUploadEmLoteNoCKEditor() {
                return marcarDesmarcarTodosUploadEmLoteNoCKEditor;
        }

        /**
	 * Atribui o valor da flag marcarDesmarcarTodos.
	 * 
	 * @param marcarDesmarcarTodosUploadEmLoteNoCKEditor o valor a ser preenchido na flag.
	 */
        public void setMarcarDesmarcarTodosUploadEmLoteNoCKEditor(Boolean marcarDesmarcarTodosUploadEmLoteNoCKEditor) {
                this.marcarDesmarcarTodosUploadEmLoteNoCKEditor = marcarDesmarcarTodosUploadEmLoteNoCKEditor;
        }       

	/**
	 * Recupera os links de downloads dos documentos para assinatura digital.
	 * 
	 * @return os links de downloads, separados por vírgula, sem o endereço principal, que deve ser configurado na applet 
	 */
	public String getDownloadLinks(){
		List<ProcessoDocumento> docs = this.getProcessoDocumentosParaAssinatura();
		return documentoJudicialService.getDownloadLinks(docs);
	}
	
	/**
	 * Recupera os links de download do documento principal (PDF).
	 * Para formar a URL completa, deve-se concatenar-se ao resultado deste método a string '#{util.contextPath}/downloadProcessoDocumento.seam?'.
	 * @return o links de download 
	 */
	public String getDownloadLinkDocumentoPrincipal(){
		List<ProcessoDocumento> doc = new ArrayList<ProcessoDocumento>();
		doc.add(this.documentoPrincipal);
		return documentoJudicialService.getDownloadLinks(doc);
	}
	
	/**
	 * Efetua download do documento em questão
	 * @param processoDocumento documento a ser realizado o download
	 */
	public void downloadDocumento(ProcessoDocumento processoDocumento) {
		try {
			processoDocumentoManager.downloadDocumento(processoDocumento);
		}
		catch (Exception e) {
			facesMessages.add(Severity.ERROR, e.getMessage());
		}
	}
	
	/**
	 * Recupera o momento da última gravação do arquivo principal.
	 * 
	 * @return o momento da última gravação
	 */
	public Date getUltimaAtualizacao() {
		return ultimaAtualizacao;
	}

	/**
	 * Indica se os documentos estão integralmente preparados para assinatura.
	 * Isso somente vai ocorrer quando as regaras abaixo listadas forem
	 * totalmente satisfeitas: 
	 * <li>Caso a variavel de fluxo
	 * "pje:fluxo:editorTexto:permiteAssinar" exista e esteja configurada com
	 * valor false, não é permitido assinar de nenhuma forma. Porem se estiver
	 * definida com valor true ou não for definida, as regras abaixo são
	 * verificadas.</li>
	 * 
	 * <li>Documento principal deve estar preenchido e salvo na base de dados</li>
	 *
	 * <li>O papel do usuario logado deve estar associado ao tipo de documento
	 * do documento principal de acordo com a RN302</li>
	 *
	 * <li>Verifica se o usuario esta com perfil de assistente, verificando se o
	 * assistente pode assinar</li>
	 * 
	 * <li>Verifica se é necessário o lançamento de movimentações processuais, e
	 * se caso for permitido ao usuario a seleção do movimento é verificado o
	 * preenchimento do mesmo</li>
	 * 
	 * @return true, se já for possível realizar a assinatura
	 */
	public boolean isPodeAssinar() {
		if(getVariavelPermiteAssinar() && exigeAssinaturaPeloPapel()){
			if(this.documentoPrincipal != null
				&& this.documentoPrincipal.getIdProcessoDocumento() > 0
				&& this.documentoPrincipal.getProcessoDocumentoBin() != null
				&& (
				(this.documentoPrincipal.getProcessoDocumentoBin().getModeloDocumento() != null && StringUtil.isNotEmpty(this.documentoPrincipal.getProcessoDocumentoBin().getModeloDocumento()))
				||
				(this.documentoPrincipal.getProcessoDocumentoBin().getBinario() && this.documentoPrincipal.getProcessoDocumentoBin().getNumeroDocumentoStorage()!=null))) {
				try {
					
					if(isPapelVinculadoAoTipoDeDocumento()){
					
						if(usuarioService.perfilAssistente() && !usuarioService.assistentePodeAssinar()){
							return Boolean.FALSE;
						}

						if (lancaMovimentacao && permiteSelecaoMovimentacao){
							if(raizes != null 
								&& !raizes.isEmpty()
								&& getTreeMovimentacoes().getEventoBeanList() != null){
								
								if(getTreeMovimentacoes().getEventoBeanList().isEmpty()){
									return Boolean.FALSE;
								}else{
									return getTreeMovimentacoes().complementosPreenchidos();
								} 
							}
						}
						
						return Boolean.TRUE;
					}
				} catch (PJeBusinessException e) {
					FacesMessages.instance().add(Severity.ERROR, "Erro ao verificar permissão de assinatura!", e.getLocalizedMessage());
				}
			}
		}
		return Boolean.FALSE;
	}
	
	/**
 	 * Metodo que verifica o papel e se no  necessrio a assinatura
 	 */
 	private Boolean exigeAssinaturaPeloPapel() {
 		return !tipoProcessoDocumentoPapelService.verificarExigibilidadeNaoAssina(
 				Authenticator.getPapelAtual(), this.documentoPrincipal.getTipoProcessoDocumento());
	}
	
	/**
	 * Recupera o valor da variavel de fluxo "pje:fluxo:editorTexto:permiteAssinar" quando a mesma existir no fluxo
	 * Caso não exista a variavel de fluxo criada, o valor default é true.
	 * 
	 * @return true quando for permitido assinar, false caso contrario
	 */
	private boolean getVariavelPermiteAssinar() {
		Object resultado = null;
		if(TaskInstance.instance() != null){
			resultado = TaskInstance.instance().getVariableLocally(Variaveis.VARIAVEL_PERMITE_ASSINAR);
		}
		
		if(resultado == null){
			resultado = Boolean.TRUE;
		}
		
		if(resultado instanceof String){
			resultado = Boolean.valueOf((String)resultado);
		}
		
		return (Boolean) resultado;
	}
	
	/**
	 * Verifica se o papel do usuario logado esta vinculado ao tipo de documento do documento principal de acordo com a RN302.
	 * 
	 * @return
	 * @throws PJeBusinessException
	 */
	private boolean isPapelVinculadoAoTipoDeDocumento() throws PJeBusinessException{
		if (usuarioService != null && usuarioService.getLocalizacaoAtual() != null ){
			return documentoJudicialService.podeAssinar(this.documentoPrincipal.getTipoProcessoDocumento(), usuarioService.getLocalizacaoAtual().getPapel());
		}
		return Boolean.FALSE;
	}
	
	/**
	 * Indica se o presente componente exige o lançamento de movimentações processuais.
	 * 
	 * @return true, se o componente exigir o lançamento de movimentações
	 */
	public boolean isExigeMovimentacao() {
		exigeMovimentacao = permiteSelecaoMovimentacao && lancaMovimentacao;
		return exigeMovimentacao;
	}
	
	/**
	 * Recupera o campo de texto que filtra o conjunto de movimentações selecionáveis.
	 * 
	 * @return o texto de seleção.
	 */
	public String getFiltroMovimentacao() {
		return filtroMovimentacao;
	}
	
	/**
	 * Atribui ao componente texto para filtragem do conjunto de movimentações selecionáveis.
	 * 
	 * @param filtroMovimentacao o texto a ser utilizado na filtragem
	 */
	public void setFiltroMovimentacao(String filtroMovimentacao) {
		if(!this.filtroMovimentacao.equals(filtroMovimentacao)){
			raizes = null;
		}
		this.filtroMovimentacao = filtroMovimentacao;
	}
	
	/**
	 * Recupera o conjunto de movimentos de selecionáveis em formato de árvore.
	 * 
	 * Não deve ser utilizado (ainda em desenvolvimento).
	 * 
	 * @return o conjunto de movimentos
	 */
	public TreeNode<Evento> getEventosRaiz(){
		if(!permiteSelecaoMovimentacao){
			TreeNode<Evento> ret = new TreeNodeImpl<Evento>();
			ret.setData(null);
			ret.setParent(null);
			return ret;
		}
		Search search = tipoMovimentoProcessualBean.getTreeModel().getSearch();
		search.clear();
		Agrupamento grp = documentoPrincipal.getTipoProcessoDocumento().getAgrupamento();
		Criteria crit = Criteria.equals("eventoAgrupamentoList.agrupamento", grp);
		boolean filtrar = filtroMovimentacao != null && !StringUtil.fullTrim(filtroMovimentacao).isEmpty();
		try {
			search.addCriteria(crit);
			if(filtrar){
				tipoMovimentoProcessualBean.getTreeModel().clear();
				search.addCriteria(Criteria.or(Criteria.equals("code", filtroMovimentacao), Criteria.contains("description", filtroMovimentacao)));
			}
		} catch (NoSuchFieldException e) {
			facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar.");
		}
		TreeNode<Evento> node = tipoMovimentoProcessualBean.getTreeModel().getTreeNode();
		return node;
	}
	
	/**
	 * Recupera o componente de seleção de tipo de movimentação processual.
	 * 
	 * @return o componente
	 */
	public TipoMovimentoProcessualBean getTipoMovimentoProcessualBean() {
		return tipoMovimentoProcessualBean;
	}

	/**
	 * Recupera a lista de erros constatados no upload.
	 * 
	 * Implementado para adequado funcionamento de tela em razão de
	 * <a href="https://issues.jboss.org/browse/RF-3566">RF-3566</a>
	 * 
	 * @return a lista de erros ocorridos no upload
	 */
	public List<String> getErrosUpload() {
		return errosUpload;
	}
	
	/**
	 * Recupera o tipo de movimentação processual selecionado para exibição dos descendentes diretos.
	 * 
	 * @return o tipo de movimentação pai selecionado.
	 */
	public Evento getTipoPai() {
		if(tipoPai == null){
			getRaizes();
		}
		return tipoPai;
	}
	
	/**
	 * Atribui ao componente um tipo de movimentação a ser tratado como o atualmente selecionado para
	 * exibição de seus descendentes.
	 * 
	 * @param tipoPai o tipo a ser definido.
	 */
	public void setTipoPai(Evento tipoPai) {
		if(!permiteSelecaoMovimentacao){
			return;
		}
		try {
			if(tipoPai != null && eventoManager.isLeaf(tipoPai)){
				selecionarMovimento(tipoPai);
				tipoPai = null;
			}else{
				this.tipoPai = tipoPai;
				loadRaizes(false);
			}
		} catch (PJeBusinessException e) {
			logger.error("Erro ao tentar definir as movimentações passíveis de seleção: {0}.", e.getLocalizedMessage());
			facesMessages.add(Severity.ERROR, "Erro ao tentar definir as movimentações passíveis de seleção.");
		}
	}
	
	/**
	 * Recupera os tipos de movimentações passíveis de seleção pelo usuário.
	 * 
	 * @return os tipos disponíveis
	 */
	public List<Evento> getRaizes() {
		if(raizes == null){
			loadRaizes(true);
		}
		return raizes;
	}

	/**
	 * Recupera o componente de controle das movimentações selecionadas para lançamento.
	 * 
	 * @return o componente de controle de lançamento de movimentações.
	 */
	public EventsTreeHandler getTreeMovimentacoes() {
		return treeMovimentacoes;
	}

	/**
	 * Atribui a este componente um novo componente de controle das movimentações selecionadas para lançamento.
	 * 
	 * @param treeMovimentacoes o componente a ser atribuído
	 */
	public void setTreeMovimentacoes(EventsTreeHandler treeMovimentacoes) {
		this.treeMovimentacoes = treeMovimentacoes;
		codigosSelecionados.clear();
	}
	
	/**
	 * Remove da lista de movimentos selecionados um movimento.
	 * 
	 * @param movimento o movimento a ser removido.
	 */
	public void removeMovimentacao(EventoBean movimento){
		if (getTreeMovimentacoes().getEventoBeanList() != null){
			getTreeMovimentacoes().getEventoBeanList().remove(movimento);
			
			Evento evento = getTreeMovimentacoes().getEventoById(movimento.getIdEvento());
			
			if (evento != null) {
				this.codigosSelecionados.remove(evento.getCode());
			}
		}
	}
	
	/**
	 * Recupera o modelo de documento atualmente selecionado.
	 * 
	 * @return o modelo de documento
	 */
	public ModeloDocumento getModelo() {
		return modelo;
	}
	
	/**
	 * Atribui ao documento principal um dado modelo, substituindo seu conteúdo.
	 * 
	 * @param modelo o modelo a ser atribuído.
	 */
	public void setModelo(ModeloDocumento modelo) {
		this.modelo = modelo;
		if(documentoPrincipal != null && modelo != null){
			documentoJudicialService.substituirModelo(documentoPrincipal, modelo);
		}
	}
	
	/**
	 * Recupera o conjunto de modelos de documento disponíveis para o usuário atual
	 * quanto ao tipo de documento selecionado.
	 * 
	 * @return o conjunto de modelos de documento disponíveis
	 * @see DocumentoJudicialService#getModelosLocais(TipoProcessoDocumento)
	 */
	public List<ModeloDocumento> getModelos() {
		if(utilizaModelos && modelos == null && tipoPrincipal != null){
			try {
				return documentoJudicialService.getModelosLocais(tipoPrincipal);
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recuperar os modelos de documentos disponíveis.");
				return Collections.emptyList();
			}
		}
		return modelos;
	}
	
	/**
	 * Recupera o tipo de documento atribuído ao documento principal.
	 * 
	 * @return o tipo de documento do documento principal;
	 */
	public TipoProcessoDocumento getTipoPrincipal(){
		if(documentoPrincipal != null){
			return documentoPrincipal.getTipoProcessoDocumento();
		}
		return null;
	}
	
	/**
	 * Atribui ao documento principal um tipo novo de documentos.
	 * 
	 * @param tipoPrincipal o tipo a ser atribuído.
	 */
	//PJEII-17356 - O campo descrição não é preenchido após documento ser assinado.Alteração foi realizada na primeira e segunda condição do método
	public void setTipoPrincipal(TipoProcessoDocumento tipoPrincipal) {
		if(documentoPrincipal != null && tipoPrincipal != null && (documentoPrincipal.getTipoProcessoDocumento() == null || !documentoPrincipal.getTipoProcessoDocumento().equals(tipoPrincipal))){
			documentoPrincipal.setTipoProcessoDocumento(tipoPrincipal);
			documentoPrincipal.setProcessoDocumento(tipoPrincipal.getTipoProcessoDocumento());
			if(permiteSelecaoMovimentacao && getTreeMovimentacoes() != null && this.tipoPrincipal != tipoPrincipal){
				setTreeMovimentacoes(new EventsTreeHandler(false));
				raizes = null;
				tipoPai = null;
				loadRaizes(true);
			}
			if(modelos != null){
				modelos.clear();
				modelos = null;
			}
		}
		this.tipoPrincipal = tipoPrincipal;
	}

	public String getDescricaoDocumento(){
		return documentoPrincipal.getProcessoDocumento();
	}

	public void setDescricaoDocumento(String descricaoDocumento) {
		documentoPrincipal.setProcessoDocumento(descricaoDocumento);
	}


	/**
	 * Indica se este componente permite a utilização de modelos.
	 * 
	 * @return true, se comportar a utilização de modelos
	 */
	public boolean isUtilizaModelos() {
		return utilizaModelos;
	}
	
	/**
	 * Indica se este componente permite vincular os documentos apresentados como 
	 * respostas a atos de comunicação.
	 * 
	 * @return true, se comportar a vinculação de respostas
	 */
	public boolean isPermiteVincularResposta() {
		return permiteVincularResposta;
	}
	
	public boolean isDataJuntadaPropria() {
		return dataJuntadaPropria;
	}

	public void setDataJuntadaPropria(boolean dataJuntadaPropria) {
		this.dataJuntadaPropria = dataJuntadaPropria;
	}

	/**
	 * Recupera o componente de tratamento de respostas a expedientes.
	 * 
	 * @return o componente de tratamento de respostas
	 */
	public RespostaExpedienteBean getRespostaBean() {
		return respostaBean;
	}

	/**
	 * Método de inicialização das propriedades internas do componente.
	 * 
	 * @param idProcessoJudicial o identificador do processo judicial ao qual o(s) documento(s)
	 * será(ão) vinculado(s).
	 * @param exigeDocumentoPrincipal marca indicativa de que o componente deverá exigir um documento
	 * de texto principal.
	 * @param permiteSelecaoMovimentacao marca indicativa de que o componente deverá permitir
	 * a seleção de movimentações processuais quando possível
	 * @param lancaMovimentacao marca indicativa de que o componente deverá se responsabilizar por
	 * lançar a movimentação processual pertinente quando finalizado o protocolo
	 * @param utilizaModelos permite a utilização de modelos de documentos
	 * @param carregaDocumentoPendenteDeAtividadeEspecifica define se, no carregamento do documento pendente,
		poderá ser carregado documento de atividade específica
	 */
	private void init(Integer idProcessoJudicial, TipoAcaoProtocoloEnum tipo, boolean exigeDocumentoPrincipal, boolean permiteSelecaoMovimentacao, boolean lancaMovimentacao, boolean utilizaModelos, boolean permiteVincularResposta, boolean dataJuntadaPropria,boolean naoAssinaDocumentoPrincipal,boolean recuperaDocumentoFluxo, boolean carregaDocumentoPendenteDeAtividadeEspecifica, boolean mostrarEscolhaPartes){
		this.tipoAcaoProtocolo = tipo;
		treeMovimentacoes = new EventsTreeHandler(false);
		errosUpload = new ArrayList<String>(0);
		this.idProcessoJudicial = idProcessoJudicial;
		this.exigeDocumentoPrincipal = exigeDocumentoPrincipal;
		this.permiteSelecaoMovimentacao = permiteSelecaoMovimentacao;
		this.lancaMovimentacao = lancaMovimentacao;
		this.utilizaModelos = utilizaModelos;
		this.permiteVincularResposta = permiteVincularResposta;
		this.dataJuntadaPropria = dataJuntadaPropria;
		this.modelo = null;
		this.naoAssinaDocumentoPrincipal = naoAssinaDocumentoPrincipal;
		this.recuperaDocumentoFluxo = recuperaDocumentoFluxo;
		this.mostrarEscolhaPartes = mostrarEscolhaPartes;
		filtroMovimentacao = "";		
		setContador(0);
		papelService = ComponentUtil.getComponent(PapelService.class);
		mimeUtilChecker = ComponentUtil.getComponent(MimeUtilChecker.class);
		processoJudicialService = ComponentUtil.getComponent(ProcessoJudicialService.class);
		eventoManager = ComponentUtil.getComponent(EventoManager.class);
		processoParteManager = ComponentUtil.getComponent(ProcessoParteManager.class);
		processoDocumentoManager = ComponentUtil.getComponent(ProcessoDocumentoManager.class);
		tipoProcessoDocumentoManager = ComponentUtil.getComponent(TipoProcessoDocumentoManager.class);
		anexarDocumentos = ComponentUtil.getComponent(AnexarDocumentos.class);
		documentoJudicialService = ComponentUtil.getComponent(DocumentoJudicialService.class);
		tipoProcessoDocumentoPapelService  = ComponentUtil.getComponent(TipoProcessoDocumentoPapelService.class);
		usuarioService = ComponentUtil.getComponent(UsuarioService.class);
		habilitacaoAutosService = ComponentUtil.getComponent(HabilitacaoAutosService.class);
		tipoMovimentoProcessualBean = new TipoMovimentoProcessualBean(eventoManager);
		multipleFileUploadAction = new MultipleFileUploadAction(mimeUtilChecker);
		controleVersaoDocumentoManager = ComponentUtil.getComponent(ControleVersaoDocumentoManager.class);
		custasJudiciaisHome = ComponentUtil.getComponent(CustasJudiciaisHome.class);
		arquivos = new ArrayList<ProcessoDocumento>();
		codigosSelecionados = new HashSet<String>();
		if(idProcessoJudicial != null){
			try {
				processoJudicial = processoJudicialService.findById(this.idProcessoJudicial);
				loadArquivosAnexadosDocumentoPrincipal();
			} catch (PJeBusinessException e) {
				logger.error("Não foi possível recuperar o processo com identificador {0}.", idProcessoJudicial);
			}
		}else{
			return;
		}
		if(processoJudicial != null){
			try {
				carregaDocumentoPendente(carregaDocumentoPendenteDeAtividadeEspecifica);

				boolean isDistribuido = processoJudicial.getProcessoStatus().equals(ProcessoStatusEnum.D);

				if (!isDistribuido) {
					custasJudiciaisHome.carregarDadosCustasDaInicial();
				}
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Não foi possível oportunizar o protocolo: {0}", e.getLocalizedMessage());
			}
			
			if(this.permiteVincularResposta){
				respostaBean = new RespostaExpedienteBean(processoJudicial, Authenticator.getPessoaLogada(), ComponentUtil.getComponent(Identity.class));
			}
		}
	}
	
	private void init(Integer idProcessoJudicial, boolean exigeDocumentoPrincipal, boolean permiteSelecaoMovimentacao, boolean lancaMovimentacao, boolean utilizaModelos, boolean permiteVincularResposta, boolean dataJuntadaPropria,boolean naoAssinaDocumentoPrincipal,boolean recuperaDocumentoFluxo, boolean carregaDocumentoPendenteDeAtividadeEspecifica, boolean mostrarEscolhaPartes){
		init(idProcessoJudicial, TipoAcaoProtocoloEnum.JUNTADA, exigeDocumentoPrincipal, permiteSelecaoMovimentacao, lancaMovimentacao, utilizaModelos, permiteVincularResposta, dataJuntadaPropria, naoAssinaDocumentoPrincipal, recuperaDocumentoFluxo, carregaDocumentoPendenteDeAtividadeEspecifica, mostrarEscolhaPartes);
	}

	/**
	 * Carrega documento pendente que não foi previamente juntado aos autos.
	 * 
	 * Esse método assegura a atribuição à propriedade {@link #documentoPrincipal} de:
	 * <li>petição inicial previamente gravada, mas não assinada ou juntada</li>
	 * <li>documento previamente existente não assinado, caso exista inicial assinada 
	 * ou protocolizada e um documento tal exista</li>
	 * <li>documento novo, caso não exista nenhum nas condições acima</li> 
	 * 
	 * @throws PJeBusinessException
	 */
	private void carregaDocumentoPendente(boolean carregaDocumentoPendenteDeAtividadeEspecifica) throws PJeBusinessException{
		List<ProcessoDocumento> iniciais = documentoJudicialService.getInicial(processoJudicial);
		
		boolean existeInicialValida = false;
		
		boolean isDistribuido = processoJudicial.getProcessoStatus().equals(ProcessoStatusEnum.D);
		
		if(!isDistribuido) {
			for(ProcessoDocumento doc: iniciais){
				if(doc.getProcessoDocumentoBin().getSignatarios().size() > 0){
					existeInicialValida = true;
					break;
				}else{
					documentoPrincipal = doc;
					break;
				}
			}	
		}else {
			existeInicialValida = true;
		}		
		
		trataDocumentoDeFluxo();
	    
		if(existeInicialValida && documentoPrincipal == null){
			// Já há uma inicial, mas não há outro documento do tipo inicial.
			configuraTipoPrincipal(carregaDocumentoPendenteDeAtividadeEspecifica);
		} else if(documentoPrincipal == null){
			trataTipoInicial();
		}

		trataAnexosEContadores();
	}

	private void trataTipoInicial() throws PJeBusinessException {
		documentoPrincipal = documentoJudicialService.getDocumento();
		TipoProcessoDocumento inicial = null;

		if(this.processoJudicial != null){
			inicial = this.processoJudicial.getClasseJudicial().getTipoProcessoDocumentoInicial();
		}

		if(inicial != null){
			documentoPrincipal.setTipoProcessoDocumento(inicial);
			documentoPrincipal.setProcessoDocumento(inicial.getTipoProcessoDocumento());
			documentoPrincipal.getProcessoDocumentoBin().setModeloDocumento(recuperarModeloDocumentoInicial(inicial));
			documentoPrincipal.setLocalizacao(Authenticator.getLocalizacaoFisicaAtual());
		}
	}

	private void configuraTipoPrincipal(boolean carregaDocumentoPendenteDeAtividadeEspecifica) throws PJeBusinessException {
		documentoPrincipal = documentoJudicialService.getDocumentoPendente(processoJudicial, Authenticator.getLocalizacaoFisicaAtual(), carregaDocumentoPendenteDeAtividadeEspecifica);
		if(documentoPrincipal == null){
			documentoPrincipal = documentoJudicialService.getDocumento();
			List<TipoProcessoDocumento> tiposDocumentosPossiveis = getTiposDocumentosPossiveis();
			if(!tiposDocumentosPossiveis.isEmpty() && tiposDocumentosPossiveis.size()==1){
				setTipoPrincipal(tiposDocumentosPossiveis.get(0));
			}
			else {
				setTipoPrincipal(null);
			}
		}
	}

	private void trataAnexosEContadores() {
		if (isProcessoDocumentoPersistente(documentoPrincipal) && Boolean.TRUE.equals(!isProcessoDocumentoAssinado(documentoPrincipal))) {
			for (ProcessoDocumento pd: documentoPrincipal.getDocumentosVinculados()) {
				if (Boolean.TRUE.equals(!isProcessoDocumentoAssinado(pd)) && !getArquivos().contains(pd)) {
					getArquivos().add(pd);
					setContador(getContador() + 1);
				}
			}
		}

		//Seleciona o radio button de acordo com o tipo do documento, ao carregar a aba
		tipoDocumentoPrincipal = TipoDocumentoPrincipalEnum.PDF;

		if (Objects.nonNull(documentoPrincipal)
				&& StringUtils.isNotEmpty(documentoPrincipal.getProcessoDocumentoBin().getExtensao())
				&& !documentoPrincipal.getProcessoDocumentoBin().getExtensao().equals("pdf")) {
			tipoDocumentoPrincipal = TipoDocumentoPrincipalEnum.HTML;
		}

		escolheTipoDocumentoPrincipal();
	}

	private void trataDocumentoDeFluxo() throws PJeBusinessException {
		//se servidor, verificar se o documento principal está em elaboração por fluxo (variável minutaEmElaboracao)
		ProcessInstance pi = null;


		if (recuperaDocumentoFluxo){
			pi = org.jboss.seam.bpm.ProcessInstance.instance();
			if(pi != null){
	    		Integer idMinuta = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(TaskInstance.instance());
	    	    if (idMinuta != null) {
	    	        documentoPrincipal = documentoJudicialService.getDocumento(idMinuta);
	    	    }
			}
		}
	}

	/**
	 * Recupera o texto do modelo de documento configurado como inicial.
	 * 
	 * @param tipoProcessoDocumento Tipo de documento ao qual o modelo pertence.
	 * @return Texto do modelo de documento configurado como inicial.
	 * 
	 * @throws PJeBusinessException 
	 */
	private String recuperarModeloDocumentoInicial(TipoProcessoDocumento tipoProcessoDocumento) throws PJeBusinessException {
		ModeloDocumento modeloDocumento = ParametroUtil.instance().getModeloPeticaoInicial();
		if (modeloDocumento != null) {
			List<ModeloDocumento> modeloDocumentoList = documentoJudicialService.getModelosLocais(tipoProcessoDocumento);
			/* Verifica se o modelo de documento configurado como inicial (via parâmetro idModeloPeticaoInicial) 
			 * é igual ao tipo de documento configurado como inicial (via parâmetro idTipoProcessoDocumentoPeticaoInicial). */
			if (modeloDocumentoList != null && modeloDocumentoList.contains(modeloDocumento)) {
				return ProcessoDocumentoHome.processarModelo(modeloDocumento.getModeloDocumento());
			}
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Trata o upload de um arquivo.
	 * 
	 * @param uf o arquivo de upload realizado.
	 * @return um {@link ProcessoDocumento} previamente tratado para exibição
	 */
	private ProcessoDocumento processaUpload_(UploadedFile uf){
		ProcessoDocumento pd = null;
		ProcessoDocumento preUpload = obterProcessoDocumento(uf.getFileName());
		if (preUpload == null) {
			pd = documentoJudicialService.getDocumento();
		} else {
			pd = preUpload;
		}
		String descricao = uf.getFileName();
		if (StringUtils.isBlank(pd.getProcessoDocumento())) {
			pd.setProcessoDocumento(acaoFormatarNomeArquivo(descricao));
		}
		pd.setProcesso(processoJudicial.getProcesso());
		pd.setProcessoTrf(processoJudicial);
		pd.setPapel(papelService.getPapelAtual());
		try {
			if (pd.getTipoProcessoDocumento() == null) {
				pd.setTipoProcessoDocumento(documentoJudicialService.presuncaoTipoDocumento(descricao));
			}
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar identificaro o tipo de documento: {0}.", e.getLocalizedMessage());
		}
		ProcessoDocumentoBin pdb = pd.getProcessoDocumentoBin();
		pdb.setModeloDocumento(null);
		pdb.setNomeArquivo(uf.getFileName());
		pdb.setSize(uf.getFileSize());
		if(uf.getSignedContentFile() != null){
			pdb.setFile(uf.getSignedContentFile());
		}else{
			pdb.setFile(uf.getFile());
		}
		pdb.setMd5Documento(uf.getContentsHash());
		pdb.setExtensao(uf.getMimeType());
		pdb.setBinario(true);
		if(uf.getSignedData() != null){
			for(Signature sig: uf.getSignedData().getSignatures()){
				try {
					ProcessoDocumentoBinPessoaAssinatura pdbpa = recuperaAssinatura_(sig); 
					pdbpa.setProcessoDocumentoBin(pdb);
					pdb.getSignatarios().add(pdbpa);
				} catch (IOException e) {
					logger.error("Erro ao recuperar assinatura do arquivo {0}: {1}.", uf.getFileName(), e.getLocalizedMessage());
				} catch (CertificateException e) {
					logger.error("Erro ao recuperar assinatura do arquivo {0}: {1}.", uf.getFileName(), e.getLocalizedMessage());
				}
			}
		}
		return pd;
	}

	/**
	 * Recupera as assinaturas de um documento, transformando o objeto básico no objeto pertinente do PJe.
	 * 
	 * @param sig a assinatura
	 * @return o objeto de assinatura do PJe.
	 * @throws IOException
	 * @throws CertificateException
	 */
	private ProcessoDocumentoBinPessoaAssinatura recuperaAssinatura_(Signature sig) throws IOException, CertificateException{
		ProcessoDocumentoBinPessoaAssinatura pdbpa = new ProcessoDocumentoBinPessoaAssinatura();
		pdbpa.setAlgoritmoDigest(sig.getHashAlgorithm());
		pdbpa.setAssinatura(new String(SigningUtilities.base64Encode(sig.getSignature())));
		pdbpa.setDataAssinatura(sig.getSigningDate());
		Certificate[] certs = new Certificate[1];
		certs[0] = sig.getSignerCertificate();
		CertificadoICP certificado = CertificadoICPBrUtil.getInstance((X509Certificate) certs[0]);
		pdbpa.setNomePessoa(certificado.getNome());
		pdbpa.setAssinaturaCMS(true);
		pdbpa.setCertChain(new String(SigningUtilities.encodeCertChain(certs)));
		try {
			Pessoa p = getPessoaService().findByInscricaoMF(certificado.getInscricaoMF());
			getPessoaService().persist(p);
			pdbpa.setPessoa(p);
		} catch (PJeBusinessException e) {
			logger.error("Não foi possível associar a pessoa com inscrição tributária [{0}] à assinatura do documento.", certificado.getInscricaoMF());
		}
		return pdbpa;
	}

	/**
	 * Assegura a gravação das alterações no banco de dados e atualiza o momento da última gravação.
	 */
	private void flush_(){
		try {
			documentoJudicialService.flush();
			ultimaAtualizacao = new Date();
		} catch (PJeBusinessException e) {
			logger.error("Erro ao tentar gravar os arquivos: {0}. A conversação ficará ativa.", e.getLocalizedMessage());
			facesMessages.add(Severity.ERROR, "Erro ao tentar protocolar: {0}", e.getLocalizedMessage());
		}
	}

	/**
	 * Lança o conjunto de movimentações escolhido pelo usuário ou, ausente esse conjunto,
	 * a movimentação de juntada, que será de documento se o usuário tiver o papel {@link Papeis#INTERNO}
	 * ou de petição, nos demais casos.
	 */
	private void lancarMovimentos() {
		if (permiteSelecaoMovimentacao && getTreeMovimentacoes().getEventoBeanList() != null
				&& !getTreeMovimentacoes().getEventoBeanList().isEmpty()) {

			getTreeMovimentacoes().registraEventosSemFluxo(processoJudicial.getProcesso(), documentoPrincipal);
		} else {
			lancarMovimentacaoProcessual(documentoPrincipal, permiteSelecaoMovimentacao);
		}
	}

	public static ProcessoEvento lancarMovimentacaoProcessual(ProcessoDocumento processoDocumento, boolean interno) {
		String codigo = interno ? CodigoMovimentoNacional.COD_MOVIMENTO_JUNTADA_DOCUMENTO : CodigoMovimentoNacional.COD_MOVIMENTO_JUNTADA_PETICAO;

		return MovimentoAutomaticoService.preencherMovimento()
				.deCodigo(codigo)
				.comProximoComplementoVazio()
				.doTipoLivre()
				.preencherComTexto(processoDocumento.getTipoProcessoDocumento().getTipoProcessoDocumento().toLowerCase())
				.associarAoDocumento(processoDocumento)
				.associarAoProcesso(processoDocumento.getProcesso())
				.lancarMovimento();

	}

	/**
	 * Recupera o serviço de tratamento de pessoa.
	 * 
	 * @return o serviço
	 */
	private PessoaService getPessoaService() {
		if(pessoaService == null){
			pessoaService = ComponentUtil.getComponent(PessoaService.class);
		}
		return pessoaService;
	}

	/**
	 * Carrega os tipos de movimentações selecionáveis da lista dada.
	 * 
	 * @param commonAncestors true, para recuperar como principal o primeiro ancestral 
	 */
	private void loadRaizes(boolean commonAncestors){
		try {
			if(tipoPai != null){
				raizes = tipoMovimentoProcessualBean.recuperaTipos(tipoPai, filtroMovimentacao, documentoPrincipal.getTipoProcessoDocumento().getAgrupamento());
				if(raizes.size() == 1){
					Evento tipo = raizes.get(0);
					for(Evento a: tipo.getHierarchy()){
						if(a.getParent() == tipoPai){
							raizes.clear();
							raizes.add(a);
							selecionarMovimento(tipo);
						}
					}
				}
			}else if(documentoPrincipal.getTipoProcessoDocumento() != null && documentoPrincipal.getTipoProcessoDocumento().getAgrupamento() != null){
				raizes = tipoMovimentoProcessualBean.recuperaTipos(tipoPai, filtroMovimentacao, documentoPrincipal.getTipoProcessoDocumento().getAgrupamento());
				if(commonAncestors){
					while(commonAncestors && !raizes.isEmpty() && raizes.size() == 1){
						tipoPai = raizes.get(0);
						raizes = tipoMovimentoProcessualBean.recuperaTipos(tipoPai, filtroMovimentacao, documentoPrincipal.getTipoProcessoDocumento().getAgrupamento());
						if(raizes.size() == 1 && eventoManager.isLeaf(raizes.get(0))){
							tipoPai = raizes.get(0).getParent();
							selecionarMovimento(raizes.get(0));
							break;
						}
					}
				}else{
					if(raizes.size() == 1){
						selecionarMovimento(raizes.get(0));
					}
				}
			}
		} catch (PJeBusinessException e) {
			logger.error("Erro ao tentar recuperar os tipos de movimentação passíveis de seleção: {0}.", e.getLocalizedMessage());
			facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os tipos de movimentação passíveis de seleção.");
		}
	}

	public int getContador() {
		return contador;
	}

	public void setContador(int contador) {
		this.contador = contador;
	}
	
	public void alterarOrdem(int posicaoAtual, int novaPosicao){
		if(posicaoAtual >= arquivos.size()){
			facesMessages.add(Severity.ERROR, "A posição original do arquivo não existe.");
			return;
		}
		int nPos = novaPosicao > (arquivos.size() - 1) ? arquivos.size() - 1 : novaPosicao;
		ProcessoDocumento pd = arquivos.get(posicaoAtual);
		arquivos.remove(posicaoAtual);
		arquivos.add(nPos, pd);
	}
	
	public List<Integer> getOrderPositions(){
		List<Integer> ret = new ArrayList<Integer>(arquivos.size());
		for(int i = 0; i < arquivos.size(); i++){
			ret.add(i);
		}
		return ret;
	}
	
	/*private boolean existePeticaoInicialValida() throws PJeBusinessException {
		List<ProcessoDocumento> iniciais = documentoJudicialService.getInicial(processoJudicial);
		for(ProcessoDocumento doc: iniciais){
			if(doc.getProcessoDocumentoBin().getValido()){
				return true;
			}
		}
		return false;
	}*/

	public boolean isAssinaDocumentoPrincipal() {
		return naoAssinaDocumentoPrincipal;
	}

	public void setAssinaDocumentoPrincipal(boolean assinaDocumentoPrincipal) {
		this.naoAssinaDocumentoPrincipal = assinaDocumentoPrincipal;
	}
	
	
	/**
	 * @return Retorna processoJudicial.
	 */
	public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}

	/**
	 * @return Retorna processoDocumentoManager.
	 */
	public ProcessoDocumentoManager getProcessoDocumentoManager() {
		return ComponentUtil.getComponent(ProcessoDocumentoManager.class);
	}

	/**
	 * Recebe uma coleção json com os dados dos arquivos que serão feitos
	 * upload. Quando os arquivos de upload são selecionados é enviada uma 
	 * requisição ajax para criar os objetos ProcessoDocumento para associar 
	 * os arquivos que serão submetidos.
	 * O formato dos arquivos ajax está exemplificado abaixo:<br/>
	 * <pre>
	 * {"array":"
	 * 	[
	 * 		{
	 * 		"nome\": "ex1mb-01.pdf", 
	 * 		"tamanho": 1384517, 
	 * 		"mime": "application/pdf"
	 * 		},{
	 * 		"nome": "ex1mb-03.pdf", 
	 * 		"tamanho": 1384517, 
	 * 		"mime": "application/pdf"
	 * 		}
	 * 	]
	 * "}
	 * </pre>
	 * @param jsonArray
	 * @throws JSONException
	 */
	public void acaoAdicionar(String[] jsonArray) throws JSONException {
		if (ArrayUtils.getLength(jsonArray) > 0) {

			String json = jsonArray[0];
			JSONObject objeto = new JSONObject(json);
			JSONArray array = new JSONArray(objeto.getString("array"));
			
			for (int indice = 0; indice < array.length(); indice++) {
				JSONObject pdJson = array.getJSONObject(indice);

				String nome = pdJson.getString("nome");

				pdJson.remove("nome");
				pdJson.put("nome", StringUtil.limparCaracteresEntreStrings(nome));
				
				ProcessoDocumento pd = documentoJudicialService.getDocumento();
				pd.setProcessoDocumento(acaoFormatarNomeArquivo(nome));
				pd.setNumeroOrdem(getArquivos().size() + 1);
				ProcessoDocumentoBin bin = pd.getProcessoDocumentoBin();
				bin.setNomeArquivo(StringUtils.substring(nome, 0, 300));
				bin.setSize(pdJson.getInt("tamanho"));
				bin.setExtensao(pdJson.getString("mime"));
				getArquivos().add(pd);
			}
		}
	}
	
	/**
	 * Remove o documento com o identificador dado da lista de arquivos a serem tratados.
	 * 
	 * @param docId o identificador do documento a ser removido.
	 * @throws PJeBusinessException 
	 */
	public void acaoRemoverTodos() throws PJeBusinessException{
		if(ProjetoUtil.isNotVazio(getArquivos())) {
			
			while (getArquivos().size() > 0) {
				remove(getArquivos().size() - 1);
			}
			
			arquivos.clear();
			codigosSelecionados.clear();
			setContador(0);
			errosUpload.clear();
		}
	}
	
	public void acaoOrdenar(ProcessoDocumento pd, Integer numeroOrdem) {
		
		if (pd != null && pd.getNumeroOrdem() != null && numeroOrdem != null) {
			int posicaoAtual = getArquivos().indexOf(pd);
			int posicaoNova = (numeroOrdem - 1);
			
			if(posicaoNova < 1){ posicaoNova = 0;}
			if(posicaoNova > (getArquivos().size() - 1)){posicaoNova = (getArquivos().size() - 1);}
			
			arquivos.remove(posicaoAtual);
			arquivos.add(posicaoNova, pd);
			ordenarColecaoProcessoDocumentoPeloIndiceDaLista();
		}
	}
	
	/**
	 * Converte o nome do arquivo para um padrão natural. 
	 * Ex: arquivo_teste.pdf => arquivo teste
	 * 
	 * @param nome Nome do arquivo
	 * @return nome do arquivo formatado
	 */
	public String acaoFormatarNomeArquivo(String nome) {
		String resultado = null;
		if (StringUtils.isNotBlank(nome)) {
			if (nome.indexOf(".") != -1) {
				nome = StringUtil.limparCaracteresEntreStrings(nome);
				resultado = nome.substring(0, nome.lastIndexOf("."));
			}
		}
		return resultado;
	}
	
	public void sincronizarProcessoDocumentoComProcessoTrf() {
		sincronizarProcessoDocumentoComProcessoTrf(false);
	}

	/**
	 * Sincroniza os documentos do ProcessoTrf com a coleção de arquivos da classe ProtocolarProcessoBean, 
	 * isso se torna necessário para quando os arquivos forem removidos da ProcessoTrf as mudanças sejam 
	 * refletidas na coleção de arquivos desta classe, caso contrário as listas de arquivos ficarão diferentes.
	 */
	public void sincronizarProcessoDocumentoComProcessoTrf(Boolean force) {

		try {
			//Documentos da classe ProcessoTrf.processo.processoDocumentoList
			List<ProcessoDocumento> incluidos = consultarProcessoDocumentoDoProcessoTrf();

			//Remove os documentos persistentes locais que não estão mais presentes na
			//ProcessoTrf.processo.processoDocumentoList, ou seja, foram removidos.
			removerProcessoDocumentoNaoPresenteNaColecao(incluidos);

			//Remove a petição local caso ela não exista mais na ProcessoTrf.processo.processoDocumentoList.
			if (force || !isProcessoDocumentoPresenteNaColecao(incluidos, getDocumentoPrincipal())) {
				setDocumentoPrincipal(null);
				getArquivos().clear();
				carregaDocumentoPendente(false);
			}

			ordenarColecaoProcessoDocumentoPeloNumeroOrdem();
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, "Não foi possível oportunizar o protocolo: {0}", e.getLocalizedMessage());
		}
	}

	/**
	 * Consulta a lista de ProcessoDocumento do ProcessoTrf.
	 * 
	 * @return lista de documentos.
	 */
	@SuppressWarnings("unchecked")
	protected List<ProcessoDocumento> consultarProcessoDocumentoDoProcessoTrf() {
		GridQuery grid = ComponentUtil.getComponent("processoTrfDocumentoGrid");
		grid.refresh();
		return new ArrayList<ProcessoDocumento>(grid.getFullList());
	}

	/**
	 * Remove da lista de arquivos da classe (ProtocolarDocumentoBean.arquivos) os arquivos persistentes 
	 * que não fazem parte da lista de documentos da ProcessoTrf.processo.processoDocumentoList. 
	 * A remoção é necessária quando um documento é excluído da classe ProcessoTrf.processo.processoDocumentoList, 
	 * assim as duas listas de documento tornam-se sincronizadas.
	 *  
	 * @param documentosProcessoTrf Documentos da classe ProcessoTrf.processo.processoDocumentoList
	 * @throws PJeBusinessException
	 */
	protected void removerProcessoDocumentoNaoPresenteNaColecao(final List<ProcessoDocumento> documentosProcessoTrf)
			throws PJeBusinessException {
		@SuppressWarnings("unchecked")
		ArrayList<ProcessoDocumento> arquivos = (ArrayList<ProcessoDocumento>) getArquivos().clone();

		for (ProcessoDocumento documento : arquivos) {
			if (!isProcessoDocumentoPersistente(documento)
					|| !isProcessoDocumentoPresenteNaColecao(documentosProcessoTrf, documento)) {
				remove(getArquivos().indexOf(documento));
			}
		}
	}
	
	/**
	 * Retorna true se o documento passado por parâmetro for um documento persistente E incluido na lista de documentos E inativo.
	 * @param documentos Lista de documentos.
	 * @param documento Documento que será verificado.
	 * @return true se o documento não existir na lista de documentos ou se for inativo.
	 */
	protected boolean isProcessoDocumentoPresenteNaColecao(final List<ProcessoDocumento> documentos, final ProcessoDocumento documento) {
		Boolean resultado = Boolean.FALSE;
		
		if (documentos != null && documento != null) {
			for (Iterator<ProcessoDocumento> iterator = documentos.iterator(); iterator.hasNext() && !resultado; ) {
				ProcessoDocumento pd = iterator.next();
				resultado = (pd.getIdProcessoDocumento() == documento.getIdProcessoDocumento() && pd.getAtivo());
			}
		}
		return resultado;
	}
	
	/**
	 * Retorna true se o documento passado por parâmetro for um objeto persistente e estiver assinado.
	 * 
	 * @param documento Documento que será validado.
	 * @return true se o documento estiver assinado.
	 */
	public Boolean isProcessoDocumentoAssinado(ProcessoDocumento documento) {
		Boolean resultado = Boolean.FALSE;
		
		if (documento != null && 
				documento.getIdProcessoDocumento() > 0 && 
				documento.getProcessoDocumentoBin() != null) {
			ProcessoDocumentoBin bin = documento.getProcessoDocumentoBin();
			resultado =  getProcessoDocumentoManager().isDocumentoAssinado(bin);
		}
		return resultado;
	}

	/**
	 * Retorna o ProcessoDocumento do nome passado como parâmetro.
	 * 
	 * @param nomeBusca
	 *            Nome do documento que será consultado.
	 * @return ProcessoDocumento
	 */
	protected ProcessoDocumento obterProcessoDocumento(final String nomeBusca) {
		Optional<ProcessoDocumento> optional = getArquivos().stream().parallel()
				.filter(p -> StringUtils.equals(p.getProcessoDocumentoBin().getNomeArquivo(), nomeBusca)).findFirst();

		return optional.isPresent() ? optional.get() : null;
	}

	/**
	 * Redefine o atributo numeroOrdem do objeto ProcessoDocumento, essa ordenação é necessária para 
	 * evitar numeroOrdem com intervalo. Após redefinir o número da ordem é invocado um flush para 
	 * alterar os objetos que já se encontram persistentes.
	 * A alteração é feita somente se houver necessidade, ou seja, se a sequencia estiver correta
	 * então nada é alterado.
	 */
	protected void ordenarColecaoProcessoDocumentoPeloNumeroOrdem() {
		Comparator<ProcessoDocumento> comparador = novoComparatorProcessoDocumentoPorNumeroOrdem();
		ArrayList<ProcessoDocumento> documentos = getArquivos();
		boolean houveAlteracao = Boolean.FALSE;
		
		Collections.sort(documentos, comparador);
		for (int indice = 0; indice < documentos.size(); indice++) {
			ProcessoDocumento documento = documentos.get(indice);
			Integer numeroOrdem = documento.getNumeroOrdem();
			
			if (numeroOrdem != null && numeroOrdem != (indice+1)) {
				houveAlteracao = Boolean.TRUE;
				documento.setNumeroOrdem(indice + 1);
			}
		}

		if (houveAlteracao) {
			flush_();
		}
	}
	
	/**
	 * Redefine o atributo numeroOrdem do objeto ProcessoDocumento, essa ordenação é necessária para 
	 * evitar numeroOrdem com intervalo. Após redefinir o número da ordem é invocado um flush para 
	 * alterar os objetos que já se encontram persistentes.
	 */
	protected void ordenarColecaoProcessoDocumentoPeloIndiceDaLista() {
		
		ArrayList<ProcessoDocumento> documentos = getArquivos();
		for (int indice = 0; indice < documentos.size(); indice++) {
			ProcessoDocumento documento = documentos.get(indice);
			documento.setNumeroOrdem(indice + 1);
		}
		flush_();
	}

	/**
	 * Retorna um novo filtro que pesquisa um ProcessoDocumento pelo ID e STATUS true.
	 * 
	 * @param documento
	 *            Documento de origem que será validado.
	 * @return Filtro.
	 */
	protected Predicate novoPredicateProcessoDocumentoPeloIdEhStatusTrue(
			final ProcessoDocumento documento) {
		return new Predicate() {

			@Override
			public boolean evaluate(Object objeto) {
				ProcessoDocumento pd = (ProcessoDocumento) objeto;
				return (pd.getIdProcessoDocumento() == documento.getIdProcessoDocumento() && pd.getAtivo());
			}
		};
	}
	
	/**
	 * Valida os objetos ProcessoDocumento que serão adicionados como anexos, as seguintes ações
	 * são tomadas sobre cada anexo.
	 * - Ajustar o nome do arquivo, caso seja do tipo UTF-8.
	 * - Ajustar a descrição do arquivo, caso seja do tipo UTF-8.
	 */
	protected void validarColecaoProcessoDocumento() {
		CollectionUtils.forAllDo(getArquivos(), new Closure() {
			
			@Override
			public void execute(Object objeto) {
				ProcessoDocumento pd = (ProcessoDocumento) objeto;
				ProcessoDocumentoBin pdb = pd.getProcessoDocumentoBin();
				
				String processoDocumento = StringUtils.substring(pd.getProcessoDocumento(), 0, 100);
				String nomeArquivo = StringUtils.substring(pdb.getNomeArquivo() == null? "":pdb.getNomeArquivo(), 0, 300);
				
				pd.setProcessoDocumento(Utf8ParaIso88591Util.converter(processoDocumento));
				pdb.setNomeArquivo(Utf8ParaIso88591Util.converter(nomeArquivo));
			}
		});
	}
	
	/**
	 * Retorna true se o documento já tiver sido salvo no banco.
	 * 
	 * @param documento Documento que será validado.
	 * @return booleano que indica se o documento já foi salvo.
	 */
	protected boolean isProcessoDocumentoPersistente(ProcessoDocumento documento) {
		return (documento != null && documento.getIdProcessoDocumento() > 0);
	}
	
	/**
	 * Retorna true se o documento passado por parâmetro estiver gravado no storage.
	 * 
	 * @param documento Documento validado.
	 * @return boleano que indica se o documento está persistido.
	 */
	protected boolean isProcessoDocumentoBinNoStorage(ProcessoDocumento documento) {
		ProcessoDocumentoBin bin = documento.getProcessoDocumentoBin();
		return (bin.getFile() != null) || (StringUtils.isNotBlank(bin.getNumeroDocumentoStorage()));
	}
	
	/**
	 * @return Comparador de ProcessoDocumento pelo numeroOrdem.
	 */
	protected Comparator<ProcessoDocumento> novoComparatorProcessoDocumentoPorNumeroOrdem() {
		return new Comparator<ProcessoDocumento>() {

			@Override
			public int compare(ProcessoDocumento pd1, ProcessoDocumento pd2) {
				Integer nr1 = pd1.getNumeroOrdem();
				Integer nr2 = pd2.getNumeroOrdem();
				
				return ((nr1 != null && nr2 != null) ? nr1.compareTo(nr2): 0);
			}
		};
	}
	
	/**
	 * Metodo responsavel por retornar o nome da action que utiliza este componente na tela
	 * @return Exemplo: RevisarMinutaAction
	 */
	public String getActionName() {
		return actionName;
	}
	
	/**
	 * Metodo responsavel por criar uma lista com os documentos disponiveis para assinatura
	 * @return Lista de documentos disponiveis para assinatura
	 */
	public List<ProcessoDocumento> getProcessoDocumentosParaAssinatura() {
		
		List<ProcessoDocumento> processoDocumentos = new ArrayList<ProcessoDocumento>();

		if (documentoPrincipal.getProcessoDocumentoBin().getExtensao() != null &&
			documentoPrincipal.getProcessoDocumentoBin().getExtensao().contains("html")) {
			this.setTipoDocumentoPrincipal(TipoDocumentoPrincipalEnum.HTML);

			documentoPrincipal.getProcessoDocumentoBin().setBinario(Boolean.FALSE);
			documentoPrincipal.getProcessoDocumentoBin().setNomeArquivo(null);
			documentoPrincipal.getProcessoDocumentoBin().setDataAssinatura(null);
			documentoPrincipal.getProcessoDocumentoBin().setFile(null);
		}

		if (documentoPrincipal.getDataJuntada() == null && documentoPrincipal.getProcessoDocumentoBin() != null && !naoAssinaDocumentoPrincipal &&
		   ((tipoDocumentoPrincipal == TipoDocumentoPrincipalEnum.PDF && documentoPrincipal.getProcessoDocumentoBin().getNumeroDocumentoStorage()!=null) || 
			(tipoDocumentoPrincipal == TipoDocumentoPrincipalEnum.HTML && documentoPrincipal.getProcessoDocumentoBin().getModeloDocumento() != null))) {

			processoDocumentos.add(documentoPrincipal);
			
			for (ProcessoDocumento pd : arquivos) {
				if (pd.getDataJuntada() == null && isProcessoDocumentoBinNoStorage(pd)) {
					processoDocumentos.add(pd);
				}
			}
		}
		
		return processoDocumentos;
	}
	
	/**
	 * Metodo responsavel por definir se o modo operacao utilizado sera o novo, utilizando {@link ArquivoAssinadoUpload} ao inves de {@link SignFile}
	 */
	public boolean isModoOperacaoNovo() {
		return !StringUtil.isNullOrEmpty(getActionName());
	}
	
	public List<ArquivoAssinadoHash> getArquivosAssinados() {
		return arquivosAssinados;
	}

	public void setArquivosAssinados(List<ArquivoAssinadoHash> arquivosAssinados) {
		this.arquivosAssinados = arquivosAssinados;
	}

	/**
	 * Metodo responsavel por adicionar um arquivo assinado pelo assinador
	 * 
	 * @param arquivoAssinadoHash O arquivo assinado que sera adicionado
	 */
	public void addArquivoAssinado(ArquivoAssinadoHash arquivoAssinadoHash) {
		this.arquivosAssinados.add(arquivoAssinadoHash);
	}

	/**
	 * Metodo responsavel por concluir a assinatura dos documentos e mostrar o feedback ao usuario em caso de sucesso ou erro
	 */	
	public void concluirAssinatura() {
		AjaxDataUtil ajaxDataUtil = ComponentUtil.getComponent(AjaxDataUtil.class);
		try {
			concluirAssinaturaAction();
			ajaxDataUtil.sucesso();
			
			if (this.tipoAcaoProtocolo == TipoAcaoProtocoloEnum.NOVO_PROCESSO) {
				facesMessages.add(Severity.INFO, "Proceda agora com o protocolo na próxima aba.");
			}
			
		} catch (PJeBusinessException pjebe) {
			ajaxDataUtil.erro();
			tratarExcecaoErroAssinatura(pjebe);
		} catch (Exception e) {
			ajaxDataUtil.erro();
			fazerRollbackTransacao();
			tratarExcecaoErroAssinatura(e);
		}
	}

	/**
	 * Metodo responsavel por concluir a assinatura dos documentos
	 * Este novo metodo sera chamado por todas as telas migradas para a nova arquitetura de assinatura digital
	 * Nesta nova arquitetura os dados dos documentos, da assinatura digital e a data juntada serao processados na mesma requisicao 
	 * @throws Exception 
	 */
	public void concluirAssinaturaAction() throws PJeBusinessException, Exception {

		List<ProcessoDocumento> documentosParaAssinar = this.getProcessoDocumentosParaAssinatura();
		
		if (documentosParaAssinar.size()>0) {
			this.documentoJudicialService.gravarAssinaturaDeProcessoDocumento(this.arquivosAssinados, documentosParaAssinar);
			boolean resultado = concluirDocumento();
			if (resultado == false) {
				throw new PJeBusinessException("Não foi possível concluir o peticionamento de documentos!");
			}
		}
		else {
			throw new PJeBusinessException("Não há documentos para assinar.");
		}
		
		this.arquivosAssinados.clear();
	}

	/**
	 * Limpa os arquivos assinados ao ocorrer uma exceção e exibe a mensagem de
	 * erro que causou a exceção para o usuário.
	 * 
	 * @param e erro que causou a falha da operação.
	 */
	private void tratarExcecaoErroAssinatura(Exception e) {
		arquivosAssinados.clear();
		facesMessages.clear();
		facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
	}

	/**
	 * Faz o rollback da transação associada a Thread atual.
	 */
	private void fazerRollbackTransacao() {
		try {
			Transaction.instance().rollback();
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
	}

	public List<TipoProcessoDocumento> getTiposDocumentosPossiveis() throws PJeBusinessException {
		if(tiposDocumentosPossiveis == null || tiposDocumentosPossiveis.size() == 0){
			if (tipoAcaoProtocolo == TipoAcaoProtocoloEnum.HABILITACAO_AUTOS) {
				tiposDocumentosPossiveis = getTiposDocumentosPossiveisHabilitacaoAutos();
			}
			else {
				tiposDocumentosPossiveis = getTiposDocumentosTexto();
			}
		}
		return tiposDocumentosPossiveis;
	}
	
	public List<TipoProcessoDocumento> getTiposDocumentosPossiveisHabilitacaoAutos() throws PJeBusinessException {
		List<TipoProcessoDocumento> retorno = new ArrayList<TipoProcessoDocumento>();
		List<TipoProcessoDocumentoTrf> lista = anexarDocumentos.tipoProcessoDocumentoHabilitacaoAutosItems();
		for (TipoProcessoDocumentoTrf tipoProcessoDocumento: lista) {
			if (tipoProcessoDocumento.getHabilitacaoAutos()) {
				retorno.add(tipoProcessoDocumentoManager.findById(tipoProcessoDocumento.getIdTipoProcessoDocumento()));
			}
		}
		return retorno;
	}

	public void setTiposDocumentosPossiveis(List<TipoProcessoDocumento> tiposDocumentosPossiveis) {
		this.tiposDocumentosPossiveis = tiposDocumentosPossiveis;
	}
	
	public void setIdTipoProcessoDocumentoLimpar(Integer posicao) {
		this.arquivos.get(posicao).setTipoProcessoDocumento(null);
	}
	
	public Boolean getAtendimentoPlantao() {
		return atendimentoPlantao;
	}
	
	public void setAtendimentoPlantao(Boolean atendimentoPlantao) {
		this.atendimentoPlantao = atendimentoPlantao;
	}
	
	public Boolean usuarioIsParteNoProcesso(Integer idUsuario) {
		List<ProcessoParte> partes = this.processoJudicial.getProcessoParteList();
		for (ProcessoParte parte: partes) {
			if (parte.getIdPessoa().equals(idUsuario) && parte.getIsAtivo()) {
				return true;
			}
		}
		return false;
	}

	public DocumentoJudicialService getDocumentoJudicialService() {
		return documentoJudicialService;
	}

	public void setDocumentoJudicialService(DocumentoJudicialService documentoJudicialService) {
		this.documentoJudicialService = documentoJudicialService;
	}
	
	public TipoDocumentoPrincipalEnum getTipoDocumentoPrincipal() {
		return tipoDocumentoPrincipal;
	}

	public void setTipoDocumentoPrincipal(TipoDocumentoPrincipalEnum tipoDocumentoPrincipal) {
		this.tipoDocumentoPrincipal = tipoDocumentoPrincipal;
	}
	
	public List<ProcessoParte> getListaPartesAtivasParaSelecao() {
		List<ProcessoParte>partesList = processoJudicial.getListaPartePrincipalAtivo();
		if (mapaProcessoParteAtivaSelecionada.size()==0) {
			for(ProcessoParte pp: partesList){
				mapaProcessoParteAtivaSelecionada.put(pp, false);
			}
		}
		return partesList;
	}
	
	public List<ProcessoParte> getListaPartesPassivasParaSelecao() {
		List<ProcessoParte>partesList  = processoJudicial.getListaPartePrincipalPassivo();
		if (mapaProcessoPartePassivaSelecionada.size()==0) {
			for(ProcessoParte pp: partesList){
				mapaProcessoPartePassivaSelecionada.put(pp, false);
			}
		}
		return partesList;
	}
	
	public void terceiroInteressadoChange() {
		if (this.terceiroInteressado) {
			mapaProcessoParteAtivaSelecionada.replaceAll((key,value)->value=false);
			mapaProcessoPartePassivaSelecionada.replaceAll((key,value)->value=false);
			respostaBean = new RespostaExpedienteBean(processoJudicial, Authenticator.getPessoaLogada(), ComponentUtil.getComponent(Identity.class));
		}
	}
	
	public void poloAtivoChange() {
		if (this.mapaProcessoParteAtivaSelecionada.containsValue(true)) {
			terceiroInteressado = false;
			mapaProcessoPartePassivaSelecionada.replaceAll((key,value)->value=false);
		}
		List<Pessoa> partes = new ArrayList<Pessoa>();
		mapaProcessoParteAtivaSelecionada.forEach((parte,selecionado)-> {
			if (selecionado) {
				partes.add(parte.getPessoa());
			}
		});
		respostaBean = new RespostaExpedienteBean(processoJudicial, partes, ComponentUtil.getComponent(Identity.class));
	}
	
	public void poloPassivoChange() {
		if (this.mapaProcessoPartePassivaSelecionada.containsValue(true)) {
			terceiroInteressado = false;
			mapaProcessoParteAtivaSelecionada.replaceAll((key,value)->value=false);
		}
		List<Pessoa> partes = new ArrayList<Pessoa>();
		mapaProcessoPartePassivaSelecionada.forEach((parte,selecionado)-> {
			if (selecionado) {
				partes.add(parte.getPessoa());
			}
		});
		respostaBean = new RespostaExpedienteBean(processoJudicial, partes, ComponentUtil.getComponent(Identity.class));
	}
	
	/** Exibe o nome do polo a partir do processo e do tipo da parte
	 * 
	 * @param processo Processo do qual se quer exibir o nome
	 * @param tipoParte Enum que indica o tipo de participação de uma pessoa em um processo
	 **/	
	public String nomeExibicaoPolo(ProcessoTrf processo, ProcessoParteParticipacaoEnum tipoParte)  {
		return processoJudicialService.getNomeExibicaoPolo(processo, tipoParte);
	}
	
	
	public Map<ProcessoParte, Boolean> getMapaProcessoParteAtivaSelecionada() {
		return mapaProcessoParteAtivaSelecionada;
	}

	public Map<ProcessoParte, Boolean> getMapaProcessoPartePassivaSelecionada() {
		return mapaProcessoPartePassivaSelecionada;
	}
	
	public void setMapaProcessoParteAtivaSelecionada(Map<ProcessoParte, Boolean> mapaProcessoParteAtivaSelecionada) {
		this.mapaProcessoParteAtivaSelecionada = mapaProcessoParteAtivaSelecionada;
	}

	public void setMapaProcessoPartePassivaSelecionada(Map<ProcessoParte, Boolean> mapaProcessoPartePassivaSelecionada) {
		this.mapaProcessoPartePassivaSelecionada = mapaProcessoPartePassivaSelecionada;
	}
	
	public Boolean getTerceiroInteressado() {
		return terceiroInteressado;
	}

	public void setTerceiroInteressado(Boolean terceiroInteressado) {
		this.terceiroInteressado = terceiroInteressado;
	}
	
	public ProcessoParteManager getProcessoParteManager() {
		return processoParteManager;
	}

	public Boolean getResponderNenhumExpediente() {
		return responderNenhumExpediente;
	}

	public void setResponderNenhumExpediente(Boolean responderNenhumExpediente) {
		this.responderNenhumExpediente = responderNenhumExpediente;
	}

	public Boolean getMostrarEscolhaPartes() {
		return mostrarEscolhaPartes;
	}

	public void setMostrarEscolhaPartes(Boolean mostrarEscolhaPartes) {
		this.mostrarEscolhaPartes = mostrarEscolhaPartes;
	}
	
	/**
	 * Indica se o usuário logado é advogado, não está habilitado nos autos e isso é relevante no 
	 * caso (ex: quando se trata de advogado fazendo peticionamento avulso)
	 * @return 
	 */
	public boolean isAdvogadoNaoHabilitado() {
		return Identity.instance().hasRole("advogado") && mostrarEscolhaPartes && !usuarioIsParteNoProcesso(Authenticator.getUsuarioLogado().getIdUsuario());
	}

	public boolean isPodeOrdenarDocumentos() {
		return getArquivos().parallelStream().noneMatch(pd -> Objects.isNull(pd.getTipoProcessoDocumento()));
	}

	public boolean isDocumentoPrincipalAssinado() {
		if (documentoPrincipal == null || documentoPrincipal.getProcessoDocumentoBin() == null) {
			return false;
		}

		return documentoPrincipal.getProcessoDocumentoBin().getDataAssinatura() != null;
	}
}
