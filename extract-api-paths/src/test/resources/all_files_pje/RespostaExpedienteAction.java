/**
 * 
 */
package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.transaction.Transaction;

import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;

/**
 * @author cristof
 *
 */
@Name(RespostaExpedienteAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class RespostaExpedienteAction implements Serializable, ArquivoAssinadoUploader{
	
	private static final long serialVersionUID = -4623208673431833677L;
	
	public static final String NAME = "respostaExpedienteAction";

	@Logger
	private Log logger;
	
	@RequestParameter
	private Integer processoJudicialId;
	
	@RequestParameter
	private Integer expedienteId;

	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@Create
	public void init(){
		protocolarDocumentoBean = new ProtocolarDocumentoBean(processoJudicialId, 
				ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL 
				| ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO 
				| ProtocolarDocumentoBean.PERMITIR_VINCULACAO_RESPOSTA, getActionName());
		if(expedienteId != null){
			for(ProcessoParteExpediente e: protocolarDocumentoBean.getRespostaBean().getExpedientes()){
				if(expedienteId.equals(e.getIdProcessoParteExpediente())){
					protocolarDocumentoBean.getRespostaBean().getSelecionados().put(e, true);
				}
			}
		}
	}
	
	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}
	
	public void concluirPeticionamentoResposta() {
		try {
			this.documentoJudicialService.gravarAssinaturaDeProcessoDocumento(this.protocolarDocumentoBean.getArquivosAssinados(), this.protocolarDocumentoBean.getProcessoDocumentosParaAssinatura());
				
			boolean resultado = getProtocolarDocumentoBean().concluir();
			
			if (resultado == false) {
				throw new Exception("Não foi possível concluir o protocolo do documento!");
			}
			else {
				this.facesMessages.add(Severity.INFO, "A resposta foi concluída com sucesso");
			}
		}
		catch (Exception e) {
			
			this.protocolarDocumentoBean.setArquivosAssinados(new ArrayList<ArquivoAssinadoHash>());
				
			try {
				Transaction.instance().rollback();
			} 
			catch (Exception e1) {
				e1.printStackTrace();
			}

			this.facesMessages.clear();
			this.facesMessages.add(Severity.ERROR, e.getMessage());		
		}			
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
}
