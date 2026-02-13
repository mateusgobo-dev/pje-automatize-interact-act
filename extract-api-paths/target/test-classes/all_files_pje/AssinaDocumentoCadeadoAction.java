package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.transaction.Transaction;

import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name(AssinaDocumentoCadeadoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class AssinaDocumentoCadeadoAction implements Serializable, ArquivoAssinadoUploader{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "assinaDocumentoCadeadoAction";
	
	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	private ProcessoDocumento documentoPrincipal;
	
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In
	private FacesMessages facesMessages;
	
	@RequestParameter(value="idProcessoDocumento")
	private Integer idProcessoDocumento;
	
	
	@Create
	public void init(){
		if(idProcessoDocumento != null){
			try {
				documentoPrincipal = processoDocumentoManager.findById(idProcessoDocumento);
				protocolarDocumentoBean = new ProtocolarDocumentoBean(documentoPrincipal.getProcessoTrf().getIdProcessoTrf(), true, false, true, false, false, true, false);
				protocolarDocumentoBean.setDocumentoPrincipal(documentoPrincipal);
				protocolarDocumentoBean.loadArquivosAnexadosDocumentoPrincipal();
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void concluirAssinatura(){
		try {
			this.documentoJudicialService.gravarAssinaturaDeProcessoDocumento(this.protocolarDocumentoBean.getArquivosAssinados(), 
																				this.protocolarDocumentoBean.getProcessoDocumentosParaAssinatura());
			boolean resultado = getProtocolarDocumentoBean().concluir();
			
			if(resultado == false){
				throw new Exception("Não foi possível concluir a assinatura do documento!");
			} else {
				this.facesMessages.clear();
				this.facesMessages.add(Severity.INFO, "Assinatura realizada");
			}
		} catch (Exception e) {
			tratarExcecaoErroAssinatura(e);
		}
	}
	
	/**
	 * Limpa os arquivos assinados e exibe para o usuário a mensagem de erro que causou a exceção.
	 * 
	 * @param O erro que causou a falha da operação.
	 */
	private void tratarExcecaoErroAssinatura(Exception e) {
		this.protocolarDocumentoBean.setArquivosAssinados(new ArrayList<ArquivoAssinadoHash>());

		try {
			Transaction.instance().rollback();
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}	
		
		facesMessages.clear();
		facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
	}
	
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest,
			ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}
	
	@Override
	public String getActionName() {
		return NAME;
	}
	
	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}
	
	public Integer getIdProcessoDocumento() {
		return idProcessoDocumento;
	}
	
	public void setIdProcessoDocumento(Integer idProcessoDocumento) {
		this.idProcessoDocumento = idProcessoDocumento;
	}
	
	public ProcessoDocumento getDocumentoPrincipal() {
		return documentoPrincipal;
	}
	
	public void setDocumentoPrincipal(ProcessoDocumento documentoPrincipal) {
		this.documentoPrincipal = documentoPrincipal;
	}
}