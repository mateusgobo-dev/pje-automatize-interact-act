package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.ibpm.component.tree.EventsEditorTreeHandler;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.AjaxDataUtil;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * 
 * @author luiz.mendes
 *
 */
@Name(TextEditAndAttachmentsAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class TextEditAndAttachmentsAction implements Serializable, ArquivoAssinadoUploader {

	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "textEditAndAttachmentsAction";
	
	Integer idDocumento = null;
	private String transicaoSaida;
	private ModeloDocumento modeloDocumento;
	private ProtocolarDocumentoBean protocolarDocumentoBean;
	private ProcessoTrf processoTrf;
	
	@In
    private TramitacaoProcessualService tramitacaoProcessualService;

	@In(create = true)
	private transient DocumentoJudicialService documentoJudicialService;

	@In(create = true, required = true)
	private transient ProcessoJudicialManager processoJudicialManager;
	
	@In(create = true)
	private transient ProcessoDocumentoManager processoDocumentoManager;
	
	@In(create = false, required = true)
	private FacesMessages facesMessages;

	@In(create = false, required = true)
	private TaskInstanceHome taskInstanceHome;

	@In(create = true, required = true)
	private transient TaskInstanceUtil taskInstanceUtil;
	
	@In(create=true)
	private AjaxDataUtil ajaxDataUtil;
	
	@Create
	public void load() throws Exception {
		ProcessInstance processInstance = taskInstanceUtil.getProcessInstance();
		this.processoTrf = recuperarProcessoTrfbyProcessInstance(processInstance);
		this.protocolarDocumentoBean = new ProtocolarDocumentoBean(this.processoTrf.getIdProcessoTrf(), ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL, getActionName());
		
		ProcessoDocumento documentoEmElaboracao = recuperarDocumentoEmElaboracao(processInstance, TaskInstance.instance());		
		this.protocolarDocumentoBean.setDocumentoPrincipal(documentoEmElaboracao);
		if(documentoEmElaboracao.getTipoProcessoDocumento() != null) {
			this.protocolarDocumentoBean.setTipoPrincipal(documentoEmElaboracao.getTipoProcessoDocumento());
		}
		
		if(documentoEmElaboracao.getIdProcessoDocumento() > 0) {
			List<ProcessoDocumento> anexos = processoDocumentoManager.getDocumentosVinculados(documentoEmElaboracao);
			if (anexos != null && !anexos.isEmpty()) {
				this.protocolarDocumentoBean.getArquivos().addAll(processoDocumentoManager.getDocumentosVinculados(documentoEmElaboracao));
			}
		}
		
		this.transicaoSaida = (String)taskInstanceUtil.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
	}
	
	/**
	 * metodo responsavel por recuperar o documento principal da edicao de documentos
	 * @param processInstace
	 * @param taskInstance
	 * @return processoDocumento carregado do banco / processoDocumento novo
	 * @throws PJeBusinessException
	 */
	private ProcessoDocumento recuperarDocumentoEmElaboracao(ProcessInstance processInstace, org.jbpm.taskmgmt.exe.TaskInstance taskInstance) throws PJeBusinessException {
		ProcessoDocumento processoDocumento = null;
		
		idDocumento = (Integer) processInstace.getContextInstance().getVariable(Variaveis.VARIAVEL_DOCUMENTO_TEXT_EDITOR_ASSINATURA);
		//se nao encontrar uma variavel da processinstance com o id do documento, tenta encontrar pela taskinstance
		if (idDocumento == null) {
			idDocumento = (Integer) taskInstance.getProcessInstance().getContextInstance().getVariable(Variaveis.VARIAVEL_DOCUMENTO_TEXT_EDITOR_ASSINATURA);
		}
		//se encontrou o id do documento, tenta recuperá-lo
		if (idDocumento != null) {
			processoDocumento = documentoJudicialService.getDocumento(idDocumento);		
			//caso encontre o doumento mas este estiver inativo, deleta-o e cria um novo, apagando tambem a variavel pre-existente
			if (processoDocumento != null && !processoDocumento.getAtivo()) {
				processInstace.getContextInstance().deleteVariable(Variaveis.VARIAVEL_DOCUMENTO_TEXT_EDITOR_ASSINATURA);
				processoDocumento = documentoJudicialService.getDocumento();
			}
		} 
		//se ainda o processodocumento estiver nulo, cria um novo
		if (processoDocumento == null){
			idDocumento = null;
			processoDocumento = documentoJudicialService.getDocumento();
		}
		return processoDocumento;
	}

	/**
	 * recupera o processo da tarefa pelo id contido na process instance
	 * @param pi - processInstance
	 * @return 
	 * @throws PJeBusinessException
	 */
	private ProcessoTrf recuperarProcessoTrfbyProcessInstance(ProcessInstance pi) throws PJeBusinessException {
		return processoJudicialManager.findById((Integer) pi.getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO));
	}
	
	/**
	 * verifica se o  documento existe em BD
	 * @return true / false
	 */
	public boolean isManaged() {
		return this.protocolarDocumentoBean.getDocumentoPrincipal().getIdProcessoDocumento() > 0;
	}

	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}

	/**
	 * metodo responsavel por gravar o documento em banco de dados
	 * @throws Exception 
	 */
	public void gravarAlteracoes() throws Exception {			
		try {
			if(!isDocumentoVazio()){
				ProcessInstance pi = taskInstanceUtil.getProcessInstance();
				
				ProcessoDocumento processoDocumentoPrincipal = getProtocolarDocumentoBean().getDocumentoPrincipal();
				
				if (processoDocumentoPrincipal.getIdProcessoDocumento() == 0) {
					processoDocumentoPrincipal = documentoJudicialService.persist(processoDocumentoPrincipal, processoTrf, true);
					pi.getContextInstance().setVariable(Variaveis.VARIAVEL_DOCUMENTO_TEXT_EDITOR_ASSINATURA, processoDocumentoPrincipal.getIdProcessoDocumento());
				} else {
					processoDocumentoPrincipal = documentoJudicialService.persist(processoDocumentoPrincipal, true);
				}
				documentoJudicialService.flush();
				this.protocolarDocumentoBean.setDocumentoPrincipal(processoDocumentoPrincipal);
				this.protocolarDocumentoBean.setTipoPrincipal(processoDocumentoPrincipal.getTipoProcessoDocumento());
				
				if(this.protocolarDocumentoBean.getArquivos() != null && !this.protocolarDocumentoBean.getArquivos().isEmpty()) {
					for (ProcessoDocumento procDoc : this.protocolarDocumentoBean.getArquivos()) {
						procDoc.setDocumentoPrincipal(processoDocumentoPrincipal);
						documentoJudicialService.persist(procDoc, true);
					}
					documentoJudicialService.flush();
				}
				ajaxDataUtil.sucesso();
				facesMessages.add(Severity.INFO, "O documento foi gravado com sucesso!");
			} else {
				facesMessages.add(Severity.WARN, "Nenhum conteúdo foi inserido no editor");
			}
		} catch (Exception e) {
			ajaxDataUtil.erro();
			facesMessages.add(Severity.ERROR, "Não foi possível gravar o documento [{0}:{1}]", e.getClass().getCanonicalName(), e.getLocalizedMessage());
			e.printStackTrace();
			throw new Exception("erro ao gravar o documento");	
		}
	}

	private boolean isDocumentoVazio(){
		boolean retorno = true;		
		if(getProcessoDocumento() != null 
				&& getProcessoDocumento().getProcessoDocumentoBin() != null
				&& StringUtil.isSet(getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento())){
			retorno = false;
		}		
		return retorno;
	}
	
	public void descartarAlteracoes() {
		try {
			documentoJudicialService.refresh(this.protocolarDocumentoBean.getDocumentoPrincipal());
		}catch (Exception e) {
			facesMessages.add(Severity.ERROR, "Não foi possível recarregar o documento [{0}:{1}]", e.getClass().getCanonicalName(), e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	public void substituirModelo() {
		documentoJudicialService.substituirModelo(getProtocolarDocumentoBean().getDocumentoPrincipal(), this.modeloDocumento);
	}
	
	/**
	 * método usado para liberar o botão de certificação digital de acordo com o
	 * papel do documento e papel do usuário e verifica se usuário já assinou o
	 * documento.
	 * 
	 * @return boolean
	 */
	public boolean liberaCertificacao() {
		return ProcessoDocumentoHome.instance().liberaCertificacao(getProcessoDocumento());
	}

	public boolean isAppletAssinaturaRendered() {
		boolean rendered = false;
		if(!isDocumentoVazio() && liberaCertificacao()) {
			rendered = true;
			// Se houver algum anexo sem tipo de documento informado, não renderiza o botão de assinatura
			for (ProcessoDocumento pd : getProtocolarDocumentoBean().getArquivos()) {
			    if (pd.getTipoProcessoDocumento() == null){
			        rendered = false;
			        break;
			    }
            }
			
		}
		return rendered;
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}
	
	public ProcessoDocumento getProcessoDocumento() {
		return this.protocolarDocumentoBean.getDocumentoPrincipal();
	}
	
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}
	
	@Override
	public String getActionName() {
		return NAME;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}
	
	public void finalizarDocumento() {
		try {			
			ProcessInstance pi = taskInstanceUtil.getProcessInstance();
			
				ProcessoTrf processoJudicial = this.processoJudicialManager.findByProcessInstance(pi);
				
				ProcessoDocumento documentoEmElaboracao = recuperarDocumentoEmElaboracao(pi, TaskInstance.instance());	
				
				this.documentoJudicialService.finalizaDocumento(documentoEmElaboracao, processoJudicial, taskInstanceHome.getTaskId(), true);
				
				List<ProcessoDocumento> anexos = processoDocumentoManager.getDocumentosVinculados(documentoEmElaboracao);
		
                if(CollectionUtilsPje.isNotEmpty(anexos)) {
                   for(ProcessoDocumento pd : anexos) {
                      this.documentoJudicialService.finalizaDocumento(pd, processoJudicial, taskInstanceHome.getTaskId(), true);
                   }
                }
                
				if (idDocumento == null) {
					tramitacaoProcessualService.gravaVariavel(Variaveis.VARIAVEL_DOCUMENTO_TEXT_EDITOR_ASSINATURA, protocolarDocumentoBean.getDocumentoPrincipal().getIdProcessoDocumento());
				}
				
				if (this.transicaoSaida != null) {
					finalizar();
				}
				documentoJudicialService.flush();			
		} catch (CertificadoException e){
			facesMessages.add(Severity.ERROR, "Houve uma inconsistência na verificação da assinatura. {0}: {1}.", e.getClass().getCanonicalName(), e.getLocalizedMessage());
			e.printStackTrace();
		} catch (Exception e ){
			facesMessages.add(Severity.ERROR, "Não foi possível finalizar o documento. {0}: {1}.", e.getClass().getCanonicalName(), e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	public void finalizar()	{
		if (EventsEditorTreeHandler.instance().validarMovimentacao()) {
			ProcessoHome.instance().setIdProcessoDocumento(protocolarDocumentoBean.getDocumentoPrincipal().getIdProcessoDocumento());
			EventsEditorTreeHandler.instance().registraEventos();
		}
		tramitacaoProcessualService.apagaVariavel(Variaveis.VARIAVEL_DOCUMENTO_TEXT_EDITOR_ASSINATURA);
		taskInstanceHome.saidaDireta(transicaoSaida);
		Contexts.getBusinessProcessContext().flush();		
	}
}