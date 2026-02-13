package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.transaction.Transaction;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.def.TaskController;

import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.view.AjaxDataUtil;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

/**
 * classe para o xhtml textEditSignature
 * @author francisco.paulino
 *
 */
@Name(TextEditSignatureAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class TextEditSignatureAction implements Serializable, ArquivoAssinadoUploader {

	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "textEditSignatureAction";

	@In(create = true, required = true)
	private transient TaskInstanceUtil taskInstanceUtil;

	@In(create = true, required = true)
	private transient ProcessoJudicialManager processoJudicialManager;

	@In
	private ProcessoDocumentoManager processoDocumentoManager;

	private ProtocolarDocumentoBean protocolarDocumentoBean;
	private String transicaoSaida;

	@Create
	public void load() throws Exception {

		org.jbpm.taskmgmt.exe.TaskInstance ti = TaskInstance.instance();

		ProcessInstance pi = taskInstanceUtil.getProcessInstance();

		Integer procId = (Integer) pi.getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO);

		protocolarDocumentoBean = new ProtocolarDocumentoBean(procId, ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.UTILIZAR_MODELOS | ProtocolarDocumentoBean.PERMITE_SELECIONAR_MOVIMENTACAO | ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO, getActionName());

		DocumentoJudicialService djs = (DocumentoJudicialService) Component.getInstance(DocumentoJudicialService.class);
		protocolarDocumentoBean.setTiposDocumentosPossiveis(djs.getTiposDocumentoMinuta());

		ProcessoDocumento docPrincipal = processoDocumentoManager.recuperaDocumentoNaoAssinadoPorTarefa(procId,ti.getId());

		if(docPrincipal != null) {
			getProtocolarDocumentoBean().setDocumentoPrincipal(docPrincipal);
		}

		transicaoSaida = (String)taskInstanceUtil.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean(){
		return protocolarDocumentoBean;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}

	public String getActionName(){
		return NAME;
	}
	
	/**
	 * metodo responsavel por gravar o documento em banco de dados
	 * @throws Exception 
	 */
	public void gravarAlteracoes() throws Exception {				
		gravaVariavelDocumentoEmFluxo(protocolarDocumentoBean.getDocumentoPrincipal().getIdProcessoDocumento());
	}

	private void gravaVariavelDocumentoEmFluxo(Integer idProcessoDocumento){
		org.jbpm.taskmgmt.exe.TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();
		if(taskInstance != null){
			ProcessoHome.instance().carregarDadosFluxo(idProcessoDocumento);
			
			TaskController taskController = taskInstance.getTask().getTaskController();
			if (taskController != null){
				List<VariableAccess> list = taskController.getVariableAccesses();
				for (VariableAccess var : list){
					String type = var.getMappedName().split(":")[0];
					String name = var.getMappedName().split(":")[1];
					if(JbpmUtil.isTypeEditor(type, name)){
						taskInstance.setVariable(var.getMappedName(), idProcessoDocumento);
						break;
					}
				}
			}
		}
	}
	
	public void concluir(){
		try {
			TaskInstanceHome taskInstanceHome = (TaskInstanceHome) Component.getInstance("taskInstanceHome");
					
			// Guarda a referencia ao processoDocumento
			ProcessoDocumento processoDocumento = this.protocolarDocumentoBean.getDocumentoPrincipal();

			this.protocolarDocumentoBean.concluirAssinaturaAction();

			// Atualiza o processoDocumento
			ComponentUtil.getComponent(DocumentoJudicialService.class).refresh(processoDocumento);


			if (processoDocumento.getProcessoDocumentoBin().getSignatarios().isEmpty()){
				throw new Exception("O documento não foi assinado!");
			}

			if (this.transicaoSaida != null) {
				taskInstanceHome.setSalvarDocumentoPorFluxo(Boolean.FALSE);
				taskInstanceHome.end(transicaoSaida);
				taskInstanceHome.setSalvarDocumentoPorFluxo(Boolean.TRUE);
			}

			ComponentUtil.getComponent(AjaxDataUtil.class).sucesso();
		}
		catch (Exception e) {

			ComponentUtil.getComponent(AjaxDataUtil.class).erro();

			try {
				Transaction.instance().rollback();
			}
			catch (Exception e1) {
				throw new RuntimeException(e1);
			}

			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
		}
	}
}