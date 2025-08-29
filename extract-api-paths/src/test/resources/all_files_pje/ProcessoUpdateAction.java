package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;

import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;

@Name(ProcessoUpdateAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ProcessoUpdateAction implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "processoUpdateAction";

	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	@In(create = true)
	private DocumentoJudicialService documentoJudicialService;
	
	@RequestParameter
	private Integer idProcesso;
	
	@In(create=false, required=true)
	private FacesMessages facesMessages;
	
	@Create
	public void iniciar() {
		if (this.protocolarDocumentoBean == null) {
			this.protocolarDocumentoBean = new ProtocolarDocumentoBean(this.idProcesso, ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL, getActionName(), ProtocolarDocumentoBean.TipoAcaoProtocoloEnum.NOVO_PROCESSO);
		}
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return this.protocolarDocumentoBean;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}

	@Override
	public String getActionName() {
		return NAME;
	}
}
