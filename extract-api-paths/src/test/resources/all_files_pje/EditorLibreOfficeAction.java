package br.jus.cnj.pje.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.transaction.Transaction;
import org.jbpm.graph.exe.ProcessInstance;
import org.richfaces.event.UploadEvent;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.component.tree.EventsHomologarMovimentosTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.ModeloDocumentoLocalManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.editor.lool.LibreOfficeManager;
import br.jus.cnj.pje.editor.lool.LoolException;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.je.pje.entity.vo.BinarioVO;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.TipoEditorEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(EditorLibreOfficeAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EditorLibreOfficeAction implements Serializable, ArquivoAssinadoUploader {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "editorLibreOfficeAction";
	
	private LibreOfficeManager libreOfficeManager;
	
	private boolean usarLibreOffice = false;
	
	private boolean mostrarPDF = false;
	
	@In(create = false, required = true)
	private FacesMessages facesMessages;
	
	@In(create=true)
	private AjaxDataUtil ajaxDataUtil;

	@In(create = false, required = true)
	private TaskInstanceHome taskInstanceHome;

	@In(create = true, required = true)
	private transient TaskInstanceUtil taskInstanceUtil;
	
	@In(create = true)
	private transient DocumentoJudicialService documentoJudicialService;
	
	@In(create = true, required = true)
	private transient ProcessoJudicialManager processoJudicialManager;
	
	@In
	private Expressions expressions;
	
	@In
	private ModeloDocumentoManager modeloDocumentoManager;
	
	@In
	private ModeloDocumentoLocalManager modeloDocumentoLocalManager;
	
	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	private ProcessoTrf processoTrf;
	
	private ModeloDocumento modeloDocumento;
	
	private String nomeModeloDocumento;
	
	private String transicaoSaida;
	
	private boolean conteudoVazio = true;
	
	private boolean conteudoAlterado = false;

	private String actionInstanceId;
	
	@RequestParameter("iddoc_")
	private Integer documentoAnexo;
	
	private boolean lancarMovimentoComAssinatura = false;
		
	@Create
	public void init() throws Exception {
		this.geraActionInstanceId();
		ProcessInstance pi = taskInstanceUtil.getProcessInstance();
		
		TaskInstanceHome.instance().setTaskId( TaskInstance.instance().getId() );
						
		recuperarProcessoTrf(pi);
		
		protocolarDocumentoBean = new ProtocolarDocumentoBean(
				this.processoTrf.getIdProcessoTrf(),
				ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.RECUPERA_DOCUMENTO_FLUXO,
				getActionName());
		
		ProcessoDocumento minutaEmElaboracao = recuperarMinutaEmElaboracao(pi, TaskInstance.instance());
		
		
		
		
		getProtocolarDocumentoBean().setDocumentoPrincipal(minutaEmElaboracao);

		this.setConteudoVazio(!this.isManaged());
		
		transicaoSaida = (String)taskInstanceUtil.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		String lancarNaAssinatura = (String) pi.getContextInstance().getVariable(Variaveis.VARIABLE_CONDICAO_LANCAMENTO_MOVIMENTOS_TEMPORARIO);
		if(lancarNaAssinatura != null && !lancarNaAssinatura.isEmpty()){
			try{
				lancarMovimentoComAssinatura = !((Boolean) expressions.createValueExpression(lancarNaAssinatura).getValue());
			}catch (Exception e) {
				if(lancarNaAssinatura.equalsIgnoreCase("true")){
					lancarMovimentoComAssinatura = false;
				}
			}
		}

		// Se houver arquivos anexados ainda não persistidos, adicioná-los à lista para exibição em tela.		
        if (minutaEmElaboracao.getDocumentosVinculados() != null && !minutaEmElaboracao.getDocumentosVinculados().isEmpty()){
            getProtocolarDocumentoBean().getArquivos().clear();
            getProtocolarDocumentoBean().getArquivos().addAll(minutaEmElaboracao.getDocumentosVinculados());
        }
		
		loadLibreOffice(minutaEmElaboracao);
		
		taskInstanceHome.limparComponentesTarefa(this.getActionInstanceId());
		this.validarAction();
	}

	public void mudarSigiloDocumento() {
		getProcessoDocumento().setDocumentoSigiloso(getProcessoDocumento().getDocumentoSigiloso() == true ? false : true);
		getProtocolarDocumentoBean().gravar(getProcessoDocumento());
		facesMessages.add(Severity.INFO, "Sigilo do documento alterado com sucesso.");
	}

	public boolean verificarSigiloDocumento() {
		return getProcessoDocumento().getDocumentoSigiloso();
	}
	
	public List<TipoProcessoDocumento> obterTiposDocumentos(ProcessoDocumento documento) {
		return this.protocolarDocumentoBean.obtemTiposDocumentos(documento);
	}
	
	public void onSelectProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		ProcessoHome.instance().onSelectProcessoDocumento(tipoProcessoDocumento, EventsHomologarMovimentosTreeHandler.instance());
		this.validarAction();
	}

	public void onSelectProcDocAnexo(ProcessoDocumento proc) {
		getProtocolarDocumentoBean().gravar(proc);
		facesMessages.add(Severity.INFO, "Tipo de documento alterado com sucesso.");
	}
	
	public void carregarNovoDocumento() {
		ModeloDocumento modelo = ParametroUtil.instance().getModeloDocumentoBaseLibreOffice();
		if(modelo != null) {
			try {
				this.modeloDocumento = modelo;
				substituirModelo();
			} catch (Exception e) {
				facesMessages.add(Severity.ERROR, "Erro ao inicializar um novo documento: " + e.getMessage(), e);
			} 
			
		}
		else {
			this.libreOfficeManager = new LibreOfficeManager(TaskInstance.instance().getId()+"", "odt");
			try {
				this.libreOfficeManager.carregarNovoDocumento();
				this.mostrarPDF = true;
				gravarAlteracoes();
			} catch (LoolException e) {
				facesMessages.add(Severity.ERROR, e.getMessage(), e);
			}
		}
	}
	
	public void atualizarDocumentoUpload(UploadEvent uploadEvent) {
		TipoProcessoDocumento tpd = getProcessoDocumento().getTipoProcessoDocumento();
		removerDocumento();
		getProcessoDocumento().setTipoProcessoDocumento(tpd);
		onSelectProcessoDocumento(tpd);
		listener(uploadEvent);
	}
	
	
	public void listener(UploadEvent uploadEvent) {
		
		String fileName = uploadEvent.getUploadItem().getFileName();
		String extension = fileName.substring(fileName.lastIndexOf('.')+1, fileName.length());
		
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(uploadEvent.getUploadItem().getFile());
		} catch (FileNotFoundException e) {
			facesMessages.add(Severity.ERROR, "Erro ao ler arquivo enviado", e);
			return;
		}
		
		try {
			this.libreOfficeManager = new LibreOfficeManager(TaskInstance.instance().getId()+"", extension);
			this.libreOfficeManager.carregarDocumentoImportacao(inputStream);
		} catch (LoolException e) {
			facesMessages.add(Severity.ERROR, e.getMessage(), e);
		}
		
		gravarAlteracoes();
		
	}
	
	public void uploadDragDrop(String name, String conteudo) {
		
		String extension = name.substring(name.lastIndexOf('.')+1, name.length());
		InputStream inputStream;
		try {
			inputStream = new ByteArrayInputStream(conteudo.getBytes("ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			facesMessages.add(Severity.ERROR, "Erro ao ler o arquivo enviado", e);
			return;
		}
		
		try {
			this.libreOfficeManager = new LibreOfficeManager(TaskInstance.instance().getId()+"", extension);
			this.libreOfficeManager.carregarDocumentoImportacao(inputStream);
		} catch (LoolException e) {
			facesMessages.add(Severity.ERROR, e.getMessage(), e);
		}
		
		gravarAlteracoes();
		
	}
	
	private void loadLibreOffice(ProcessoDocumento minutaEmElaboracao) throws IOException, LoolException {
		usarLibreOffice = true;
		
		String nomeDocumento = minutaEmElaboracao.getProcessoDocumentoBin().getNomeDocumentoWopi();
		
		if (nomeDocumento != null ) {
			this.libreOfficeManager = new LibreOfficeManager(nomeDocumento);
			carregarPdfInicial();
		} else if (minutaEmElaboracao.getProcessoDocumentoBin().getIdProcessoDocumentoBin()>0) {
			String modeloDocumentoHtmlPreenchido = getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().getModeloDocumento();
			this.libreOfficeManager = new LibreOfficeManager(TaskInstance.instance().getId()+"" ,"docx");
			this.libreOfficeManager.carregarModeloDocumento(modeloDocumentoHtmlPreenchido);
			gravarAlteracoes();
		}
		
	}
	
	public void substituirModelo() {
		ModeloDocumentoLocal modeloLocal = this.modeloDocumentoLocalManager.findById(this.modeloDocumento.getIdModeloDocumento());
		if ( modeloLocal.getTipoEditor()==TipoEditorEnum.L ) {
			try {
				this.documentoJudicialService.substituirModeloODT(getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin(), this.modeloDocumento);
				String base64ODT = getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().getModeloDocumento();
				this.libreOfficeManager = new LibreOfficeManager(TaskInstance.instance().getId()+"" ,"odt");
				this.libreOfficeManager.carregarModeloDocumento(Base64.getDecoder().decode(base64ODT));
				this.nomeModeloDocumento = null;
			} catch (Exception e) {
				facesMessages.add(Severity.ERROR, "Erro ao substituir modelo ODT", e);
			}
		} else {
			this.documentoJudicialService.substituirModelo(getProtocolarDocumentoBean().getDocumentoPrincipal(), this.modeloDocumento);
			try {
				String modeloDocumentoHtmlPreenchido = getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().getModeloDocumento();
				this.libreOfficeManager = new LibreOfficeManager(TaskInstance.instance().getId()+"" ,"docx");
				this.libreOfficeManager.carregarModeloDocumento(modeloDocumentoHtmlPreenchido);
				this.nomeModeloDocumento = null;
			} catch (LoolException e) {
				facesMessages.add(Severity.ERROR, e.getMessage(), e);
			}
		}
		
		this.gravarAlteracoes();
		this.validarAction();
		
	}
	
	public ProcessoDocumento recuperarMinutaEmElaboracao(ProcessInstance pi, org.jbpm.taskmgmt.exe.TaskInstance taskInstance) throws PJeBusinessException {
		ProcessoDocumento processoDocumento = null;
		
		Integer idMinuta = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(taskInstance);

		if (idMinuta != null) {
            processoDocumento = documentoJudicialService.getDocumento(idMinuta);
                        
            if (processoDocumento != null && (!processoDocumento.getAtivo() || processoDocumento.getDataJuntada() != null)) {
                JbpmUtil.instance().apagaMinutaEmElaboracao(taskInstance);
                processoDocumento = documentoJudicialService.getDocumento();
            }
        } else {
            if (ProcessoDocumentoHome.instance().getInstance().getProcessoDocumentoBin() != null) {
                processoDocumento = ProcessoDocumentoHome.instance().getInstance();
            }
        }
		
		if (processoDocumento == null){
			processoDocumento = documentoJudicialService.getDocumento();
		}
		
		return processoDocumento;
	}
	
	private void recuperarProcessoTrf(ProcessInstance pi) throws PJeBusinessException {
		Integer procId = (Integer) pi.getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO);			
		processoTrf = this.processoJudicialManager.findById(procId);
		//Adicionando o valor na sessão para ser recuperado ao alterar a ordem dos documentos vinculados. 
		Contexts.getSessionContext().set("idProcessoSessao", procId);
	}
	
	public List<TipoProcessoDocumento> getTiposDisponiveis() {
		try {
			return this.documentoJudicialService.getTiposDisponiveis();
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Não foi possível obter os tipos de documentos disponíveis [{0}:{1}]", e
					.getClass().getCanonicalName(), e.getLocalizedMessage());
			return new ArrayList<TipoProcessoDocumento>(0);
		}
	}
	
	public List<ModeloDocumento> getModelosDisponiveis() {
		try {
			return this.documentoJudicialService.getModelosDisponiveis();
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Não foi possível obter os modelos disponíveis [{0}:{1}]", e.getClass()
					.getCanonicalName(), e.getLocalizedMessage());
			return new ArrayList<ModeloDocumento>(0);
		}
	}
	
	public void carregarPdfInicial() {
		this.mostrarPDF = true;
		ProcessoDocumentoBin bin = getProcessoDocumento().getProcessoDocumentoBin();
		ProcessoDocumentoBinManager binManager = (ProcessoDocumentoBinManager) Component.getInstance(ProcessoDocumentoBinManager.class);
		try {
			binManager.refresh(bin);
			bin.setFile(null);
		} catch (PJeBusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BinarioVO binario = new BinarioVO();
		binario.setIdBinario(bin.getIdProcessoDocumentoBin());
		binario.setMimeType(bin.getExtensao());
		binario.setNomeArquivo(bin.getNomeArquivo());
		binario.setNumeroStorage(bin.getNumeroDocumentoStorage());
		Contexts.getSessionContext().set("download-binario", binario);
		
	}
	
	public void gravarAlteracoes() {
		
		try {
			ProcessInstance pi = taskInstanceUtil.getProcessInstance();
			
			ProcessoDocumento processoDocumento = getProtocolarDocumentoBean().getDocumentoPrincipal();
			
			if (usarLibreOffice) {	
				InputStream pdf = this.libreOfficeManager.getPDFContent();
				int size = pdf.available();
				File arquivoBinario = processoDocumento.getProcessoDocumentoBin().getFile();
				if ( arquivoBinario==null ) {
					arquivoBinario = this.libreOfficeManager.salvarPDFTemp(pdf);
					processoDocumento.getProcessoDocumentoBin().setFile(arquivoBinario);
				} else {
					IOUtils.copy(pdf, new FileOutputStream(arquivoBinario));
				}
				processoDocumento.getProcessoDocumentoBin().setSize(size);
				this.libreOfficeManager.mostrarPDF(processoDocumento.getProcessoDocumentoBin().getFile());
				this.mostrarPDF = true;
				processoDocumento.getProcessoDocumentoBin().setNomeDocumentoWopi(this.libreOfficeManager.getArquivo());
				processoDocumento.getProcessoDocumentoBin().setExtensao("application/pdf");
				processoDocumento.getProcessoDocumentoBin().setBinario(true);
				processoDocumento.getProcessoDocumentoBin().setModeloDocumento(null);
				
			}
			
			if (processoDocumento.getIdProcessoDocumento() == 0) {
				processoDocumento = documentoJudicialService.persist(processoDocumento, processoTrf, true);
				pi.getContextInstance().setVariable(Variaveis.MINUTA_EM_ELABORACAO, processoDocumento.getIdProcessoDocumento());
			} 
			else {
				processoDocumento = documentoJudicialService.persist(processoDocumento, true);
			}
			documentoJudicialService.flush();
			
			getProtocolarDocumentoBean().setDocumentoPrincipal(processoDocumento);
			
			if (lancarMovimentoComAssinatura){
				LancadorMovimentosService.instance().setMovimentosTemporarios(pi, EventsHomologarMovimentosTreeHandler.instance().getEventoBeanList());
			}

			this.setConteudoAlterado(false);
			this.setConteudoVazio(false);
			this.ajaxDataUtil.sucesso();
			this.validarAction();
		}
		catch (Exception e) {
			
			this.ajaxDataUtil.erro();
			
			facesMessages.add(Severity.ERROR, "Não foi possível gravar o documento [{0}:{1}]", e.getClass().getCanonicalName(), e.getLocalizedMessage());
		}
		
	}
	
	private void gravaInformacaoValidacao(boolean valido, String mensagem) {
		TaskInstance.instance().setVariableLocally(Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_RESULTADO.concat(this.getActionInstanceId()), valido);
		TaskInstance.instance().setVariableLocally(Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_MENSAGEM.concat(this.getActionInstanceId()), mensagem);
	}
	
	public boolean validarAction() {
		boolean valido = (this.isManaged() && !this.isConteudoVazio() && !this.isConteudoAlterado());
		String mensagem = "";
		
		if(!valido) {
			if(!this.isManaged()) {
				mensagem = "Documento não foi salvo";
			}else if(this.isConteudoVazio()) {
				mensagem = "O documento está com o conteúdo vazio";
			}else if(this.isConteudoAlterado()) {
				mensagem = "Há informações não salvas no documento";
			}
		}
		this.gravaInformacaoValidacao(valido, mensagem);
		return valido;
	}
	
	public void removerDocumento(){
		try {
			if (getProtocolarDocumentoBean().getDocumentoPrincipal()!=null) {
				Long taskId = taskInstanceHome.getTaskId();
				org.jbpm.taskmgmt.exe.TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(taskId);
				ProcessInstance pi = taskInstance.getProcessInstance();
				Integer idMinuta = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(taskInstance);
				ProcessoDocumento processoDocumento = documentoJudicialService.getDocumento(idMinuta);
				if(processoDocumento != null) {
					ProcessoDocumentoHome processoDocumentoHome = ComponentUtil.getComponent(ProcessoDocumentoHome.class);
					processoDocumentoHome.excluir(processoDocumento);	
					this.libreOfficeManager.apagarDocumento();
					pi.getContextInstance().deleteVariable(Variaveis.MINUTA_EM_ELABORACAO);
					Contexts.getSessionContext().remove("download-binario");
					this.ajaxDataUtil.sucesso();
				}
				this.mostrarPDF = false;
				init();
			}
		} catch (Exception e) {
			this.ajaxDataUtil.erro();
			facesMessages.add(Severity.ERROR, "Não foi possível remover o documento [{0}:{1}]", e.getClass().getCanonicalName(), e.getLocalizedMessage());
		}
	}
	
	public void removerDocumentoAnexo(ProcessoDocumento documento) {
		if (getProtocolarDocumentoBean().getDocumentoPrincipal()!=null) {
			try {
				ProcessoDocumentoHome processoDocumentoHome = ComponentUtil.getComponent(ProcessoDocumentoHome.class);
				processoDocumentoHome.excluir(documento);
				getProtocolarDocumentoBean().reorganizarDocumentosAnexos();
				this.ajaxDataUtil.sucesso();
			} catch (Exception e) {
				this.ajaxDataUtil.erro();
				facesMessages.add(Severity.ERROR, "Houve um erro ao remover o(s) arquivo(s).");
			}
		}
	}
	
	public void acaoRemoverTodos() {
		if (getProtocolarDocumentoBean().getDocumentoPrincipal() != null && getProtocolarDocumentoBean().getArquivos() != null) {
			try {
				getProtocolarDocumentoBean().acaoRemoverTodos();
				getDocumentosVinculados().clear();
				this.ajaxDataUtil.sucesso();
			} catch (Exception e) {
				this.ajaxDataUtil.erro();
				facesMessages.add(Severity.ERROR, "Houve um erro ao remover o(s) arquivo(s).");
			}
		}
	}
	
	public void gravarDocumentoAnexo(UploadEvent uploadEvent) {
		ProcessoDocumento processoDocumento = getProtocolarDocumentoBean().getDocumentoPrincipal();
		if (processoDocumento != null) {
			try {
				getProtocolarDocumentoBean().listener(uploadEvent);
				List<ProcessoDocumento> arquivos = getProtocolarDocumentoBean().getArquivos();
				for(ProcessoDocumento arq : arquivos) {
					arq.setDocumentoPrincipal(processoDocumento);
					arq.setTipoProcessoDocumento(processoDocumento.getTipoProcessoDocumento());
					getProtocolarDocumentoBean().gravar(arq);				
					}
				getProtocolarDocumentoBean().ordenarColecaoProcessoDocumentoPeloIndiceDaLista();
				getDocumentosVinculados().addAll(arquivos);
				getProtocolarDocumentoBean().getArquivos().addAll(processoDocumento.getDocumentosVinculados());
				this.ajaxDataUtil.sucesso();
			} catch (Exception e) {
				this.ajaxDataUtil.erro();
				facesMessages.add(Severity.ERROR, "Houve um erro ao fazer o upload do(s) arquivo(s).");
			}
		}
	}
	
	public void reordenaNumeracaoDocumentoVinculado(String strRelacaoIds) throws PJeBusinessException {
		ProcessoDocumentoManager pd = getProtocolarDocumentoBean().getProcessoDocumentoManager();
		String[] arrRelacaoIds = strRelacaoIds.split(",");
		for (int i=0; i<arrRelacaoIds.length; i++) {
			Integer idProcessoDocumento = Integer.parseInt(arrRelacaoIds[i]);
			ProcessoDocumento processoDocumento = pd.findById(idProcessoDocumento);
			processoDocumento.setNumeroOrdem(i+1);
			pd.persist(processoDocumento);
		}
		pd.flush();
	}

	
	public void acaoAdicionar(String[] jsonArray) {
		ProcessoDocumento processoDocumento = getProtocolarDocumentoBean().getDocumentoPrincipal();
		if (processoDocumento != null) {
			try {
				getProtocolarDocumentoBean().acaoAdicionar(jsonArray);
				List<ProcessoDocumento> arquivos = getProtocolarDocumentoBean().getArquivos();
				for(ProcessoDocumento arq : arquivos) {
					arq.setProcessoTrf(processoDocumento.getProcessoTrf());
					arq.setDocumentoPrincipal(processoDocumento);
					arq.setTipoProcessoDocumento(processoDocumento.getTipoProcessoDocumento());
					}
				getProtocolarDocumentoBean().ordenarColecaoProcessoDocumentoPeloIndiceDaLista();
				getDocumentosVinculados().addAll(arquivos);
				this.ajaxDataUtil.sucesso();
			} catch (Exception e) {
				this.ajaxDataUtil.erro();
				facesMessages.add(Severity.ERROR, "Houve um erro ao fazer o upload do(s) arquivo(s).");
			}
		}
	}
	
	public List<ModeloDocumento> pesquisarModeloDocumento(Object valor) throws PJeBusinessException{
		modeloDocumento = null;
		String txt = ((String) valor).trim();
		List<ModeloDocumento> ret = new ArrayList<ModeloDocumento>();
		ModeloDocumentoManager manager = ComponentUtil.getComponent(ModeloDocumentoManager.class);
		try {
			if (txt.matches("\\d*")) {
				ret.addAll(manager.getModelosPorTipoTituloOuDescricao(getProcessoDocumento().getTipoProcessoDocumento(),
						null, Integer.parseInt(txt)));
			} else {

				String textoPesquisa = txt.replaceAll("\\s", "%");
				textoPesquisa = txt.replaceAll("\\*", "%");
				ret.addAll(manager.getModelosPorTipoTituloOuDescricao(getProcessoDocumento().getTipoProcessoDocumento(),
						textoPesquisa, null));
			}
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao buscar os modelos de documento.");
		}
		return ret;
	}
	
	/**
 	 * @return True se pode Excluir
 	 */
	public boolean isPermiteExcluir(){
 		return protocolarDocumentoBean.isProcessoDocumentoPersistente(getProcessoDocumento());
	}
	
	/**
 	 * Metodo que verifica se  obrigatrio a assinatura pelo tipo processo documento 
 	 * e papel do usurio logado, verificando se o tipo de documento possui um agrupador de movimentos relacionado e há algum movimento selecinado
 	 * 
 	 * @return True se pode assinar
 	 */
	public boolean isHabilitaBotaoAssinar(){
 		return isManaged() && documentoJudicialService.verificaPossibilidadeAssinatura(
 				getProcessoDocumento(), 
 				Authenticator.getUsuarioLogado(), 
 				EventsHomologarMovimentosTreeHandler.instance().getEventoBeanList(), 
 				Authenticator.getPapelAtual()).getResultado();
	}
 	
 	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}
 	
 	public void concluir() {

		try {					
			// Guarda a referencia ao processoDocumento
			ProcessoDocumento processoDocumento = getProcessoDocumento();
			
			processoDocumento.getProcessoDocumentoBin().setModeloDocumento( this.libreOfficeManager.getHtmlContent() );
			processoDocumento.getProcessoDocumentoBin().setNomeDocumentoWopi(null);

			this.protocolarDocumentoBean.concluirAssinaturaAction();	
			
			// Atualiza o processoDocumento
			this.documentoJudicialService.refresh(processoDocumento);
			
			Long taskId = taskInstanceHome.getTaskId();
			org.jbpm.taskmgmt.exe.TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(taskId);
			
			if (processoDocumento.getProcessoDocumentoBin().getSignatarios().isEmpty()){
				throw new Exception("O documento no foi assinado!");
			}
			
			Pessoa signatario = ((PessoaService) Component.getInstance("pessoaService")).findById(Authenticator.getUsuarioLogado().getIdUsuario());
			this.documentoJudicialService.finalizaDocumento(processoDocumento, processoTrf, taskId, true, false, false, signatario, true);
			
			ProcessoHome.instance().setIdProcessoDocumento(processoDocumento.getIdProcessoDocumento());
			
			ProcessInstance pi = taskInstance.getProcessInstance();
			Integer identificadorMinutaEmElaboracao = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(taskInstance);
			if(identificadorMinutaEmElaboracao != null) {
				if(processoDocumento.getTipoProcessoDocumento().getDocumentoAtoProferido()) {
					pi.getContextInstance().setVariable(Variaveis.ATO_PROFERIDO, identificadorMinutaEmElaboracao);
				}
			}
			
			pi.getContextInstance().setVariable(Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO, identificadorMinutaEmElaboracao);

			getProtocolarDocumentoBean().getArquivos().clear();
			
			if(lancarMovimentoComAssinatura) {
				LancadorMovimentosService.instance().lancarMovimentosTemporariosAssociandoAoDocumento(pi,processoDocumento);
			}

			this.validarAction();
			if (this.transicaoSaida != null) {
				String transicao = taskInstanceHome.end(transicaoSaida);
				if ( transicao==null ) {
					throw new Exception("Erro ao finalizar atividade");
				}
			}
			
			pi.getContextInstance().deleteVariable(Variaveis.MINUTA_EM_ELABORACAO);
			
			this.libreOfficeManager.apagarDocumento();
			
			this.ajaxDataUtil.sucesso();
		}
		catch (Exception e) {
			
			this.ajaxDataUtil.erro();			
		
			try {
				Transaction.instance().rollback();
			}
			catch (Exception e1) {
				throw new RuntimeException(e1);	
			}
			e.printStackTrace();
			facesMessages.clear();
			facesMessages.add(Severity.ERROR, e.getMessage());
		}
	}

	
	private void geraActionInstanceId() {
		actionInstanceId = this.getActionName();	
	}

	public String getActionInstanceId() {
		return actionInstanceId;
	}
	
	public boolean isConteudoAlterado() {
		return conteudoAlterado;
	}

	public void setConteudoAlterado(boolean conteudoAlterado) {
		this.conteudoAlterado = conteudoAlterado;
		this.validarAction();
	}
	
	public boolean isConteudoVazio() {
		return conteudoVazio;
	}
	
	public void setConteudoVazio(boolean conteudoVazio) {
		this.conteudoVazio = conteudoVazio;
		this.validarAction();
	}
	
	public boolean isManaged() {
		return getProcessoDocumento().getIdProcessoDocumento() > 0;
	}
	
	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}
	
	public String getNomeModeloDocumento() {
		return this.nomeModeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
		this.nomeModeloDocumento = modeloDocumento.getTituloModeloDocumento();
		this.validarAction();
	}
	
	public String getActionName() {
		return NAME;
	}
	
	public boolean isUsarLibreOffice() {
		return usarLibreOffice;
	}

	public void setUsarLibreOffice(boolean usarLibreOffice) {
		this.usarLibreOffice = usarLibreOffice;
	}

	public LibreOfficeManager getLibreOfficeManager() {
		return libreOfficeManager;
	}

	public void setLibreOfficeManager(LibreOfficeManager libreOfficeManager) {
		this.libreOfficeManager = libreOfficeManager;
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}

	public void setProtocolarDocumentoBean(ProtocolarDocumentoBean protocolarDocumentoBean) {
		this.protocolarDocumentoBean = protocolarDocumentoBean;
	}
	
	public ProcessoDocumento getProcessoDocumento() {
		return getProtocolarDocumentoBean().getDocumentoPrincipal();
	}

	public List<ProcessoDocumento> getDocumentosVinculados() {
		atualizarListaDocumentosVinculados();
		ordenarDocumentosVinculados();
		return new ArrayList<ProcessoDocumento>(getProtocolarDocumentoBean().getArquivos());
	}

	public void atualizarListaDocumentosVinculados() {
		getProtocolarDocumentoBean().loadArquivosAnexadosDocumentoPrincipal();
		getProtocolarDocumentoBean().getDocumentoPrincipal().getDocumentosVinculados()
				.addAll(getProtocolarDocumentoBean().getArquivos());
	}

	public void ordenarDocumentosVinculados() {
		getProtocolarDocumentoBean().ordenarColecaoProcessoDocumentoPeloNumeroOrdem();
	}

	public String getTransicaoSaida() {
		return transicaoSaida;
	}

	public void setTransicaoSaida(String transicaoSaida) {
		this.transicaoSaida = transicaoSaida;
	}

	public boolean isMostrarPDF() {
		return mostrarPDF;
	}
	
	public void setNomeModeloDocumento(String nomeModeloDocumento) {
		this.nomeModeloDocumento = nomeModeloDocumento;
	}
	
}
