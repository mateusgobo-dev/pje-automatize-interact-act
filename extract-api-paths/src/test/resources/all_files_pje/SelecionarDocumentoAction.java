package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.view.DocumentoJudicialDataModel;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("selecionarDocumentoAction")
@Scope(ScopeType.CONVERSATION)
public class SelecionarDocumentoAction implements Serializable{

	private static final long serialVersionUID = -8172455522040876040L;

	
	private DocumentoJudicialDataModel dataModel;
	private ProcessoTrf processoJudicial;
	private ProcessoDocumento documentoSelecionado;
	private String transicaoSaida;
	
	@In(create = true, required = true)
	private transient TaskInstanceUtil taskInstanceUtil;
	
	@In(create = false, required = true)
	private TaskInstanceHome taskInstanceHome;
	
	@In
	private FacesMessages facesMessages;
	
	@Create
	public void init() {
		processoJudicial = loadProcessoJudicial();
		dataModel = new DocumentoJudicialDataModel();
		dataModel.setProcessoJudicial(this.processoJudicial);
		DocumentoJudicialService documentoJudicialService = ComponentUtil.getComponent(DocumentoJudicialService.class);
		dataModel.setDocumentoJudicialService(documentoJudicialService);
		dataModel.setOrdemDecrescente(true);
		dataModel.setMostrarPdf(false);
		dataModel.setIncluirComAssinaturaInvalidada(true);
		dataModel.setSoDocumentosJuntados(true);
		
		transicaoSaida = (String)taskInstanceUtil.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
	}
	
	public DocumentoJudicialDataModel getDataModel(){
		return dataModel;
	}
	
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

	public ProcessoDocumento getDocumentoSelecionado() {
		return documentoSelecionado;
	}

	public void setDocumentoSelecionado(ProcessoDocumento documentoSelecionado) {
		this.documentoSelecionado = documentoSelecionado;
	}
	
	public void salvarVariavel() {
		if(documentoSelecionado == null) {
			facesMessages.add(Severity.ERROR, "Nenhum documento selecionado.");
			return;
		}
		ProcessInstance pi = taskInstanceUtil.getProcessInstance();
		pi.getContextInstance().setVariable("pje:fluxo:documentoSelecionado", documentoSelecionado.getIdProcessoDocumento());
		if (this.transicaoSaida != null) {
			taskInstanceHome.end(transicaoSaida);
		}	
		facesMessages.add(Severity.INFO,"Documento selecionado com sucesso.");
	}
}
