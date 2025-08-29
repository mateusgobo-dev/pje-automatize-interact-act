package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.transaction.Transaction;

import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;

@Name(FormularioPeticionamentoAvulsoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class FormularioPeticionamentoAvulsoAction implements Serializable, ArquivoAssinadoUploader {
	
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "formularioPeticionamentoAvulsoAction";
	
	private Integer idProcessoTrf;

	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	@In(create = true)
	private DocumentoJudicialService documentoJudicialService;
	
	/**
	 * Define a lista de arquivos assinados pelo assinador, os arquivos assinados e enviados pelo assinador
	 * sera armazenados nesta lista para posterior validacao e persistencia.
	 */
	private List<ArquivoAssinadoHash> arquivosAssinados = new ArrayList<ArquivoAssinadoHash>();
	
	@In(create=false, required=true)
	private FacesMessages facesMessages;
	
	/**
	 * Este metodo sera invocado pelo jboss seam, conforme definicao no arquivo: 
	 * formularioPeticionamentoAvulso.page.xml 
	 */ 
	public void iniciar() {
		if(this.protocolarDocumentoBean == null){
			this.protocolarDocumentoBean = new ProtocolarDocumentoBean(getIdProcessoTrf(), ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL, getActionName());
		}
	}
	
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.arquivosAssinados.add(arquivoAssinadoHash);
	}
	
	public void concluirPeticionamento() {
		try {
			this.documentoJudicialService.gravarAssinaturaDeProcessoDocumento(this.arquivosAssinados, this.protocolarDocumentoBean.getProcessoDocumentosParaAssinatura());			
		
			boolean resultado = getProtocolarDocumentoBean().concluir();
			
			if (resultado == false) {
				throw new Exception("Não foi possível concluir o protocolo do documento!");
			}
			else {
				this.facesMessages.add(Severity.INFO, "O peticionamento foi concluído com sucesso");
			}
		}
		catch (Exception e) {
			
			this.arquivosAssinados = new ArrayList<ArquivoAssinadoHash>();
				
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
	public String getActionName() {
		return NAME;
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}

	public Integer getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(Integer idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}
}
