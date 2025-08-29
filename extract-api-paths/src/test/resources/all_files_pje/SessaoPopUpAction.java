package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.home.SessaoProcessoDocumentoHome;
import br.com.infox.cliente.home.SessaoProcessoDocumentoVotoHome;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;

@Name(SessaoPopUpAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class SessaoPopUpAction implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "sessaoPopUpAction";
	
	@In(create = true)
	private DocumentoJudicialService documentoJudicialService;
	
	private ProcessoDocumentoBinHome getProcessoDocumentoBinHome() {
		return ProcessoDocumentoBinHome.instance();
	}

	private SessaoProcessoDocumentoHome getSessaoProcessoDocumentoHome() {
		return SessaoProcessoDocumentoHome.instance();
	}
	
	private SessaoProcessoDocumentoVotoHome getSessaoProcessoDocumentoVotoHome() {
		return SessaoProcessoDocumentoVotoHome.instance();
	}
	
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest request, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		getProcessoDocumentoBinHome().setCertChain(arquivoAssinadoHash.getCadeiaCertificado());
		getProcessoDocumentoBinHome().setSignature(arquivoAssinadoHash.getAssinatura());
	}

	@Override
	public String getActionName() {
		return NAME;
	}
	
	public String getUrlVoto() {		
		if (getSessaoProcessoDocumentoVotoHome().getModeloDocumento() != null) {
			return this.documentoJudicialService.getDownloadLink(getSessaoProcessoDocumentoVotoHome().getModeloDocumento());
		}
		else {
			return "";
		}
	}
	
	public String getUrlRelatorio() {
		if (getSessaoProcessoDocumentoHome().getModeloDocumento() != null) {
			return this.documentoJudicialService.getDownloadLink(getSessaoProcessoDocumentoHome().getModeloDocumento());
		}
		else {
			return "";
		}
	}
	
	public String getUrlEmenta() {
		return getUrlRelatorio();
	}
}