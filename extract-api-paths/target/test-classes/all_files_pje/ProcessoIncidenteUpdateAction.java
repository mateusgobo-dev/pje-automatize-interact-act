package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;

@Name(ProcessoIncidenteUpdateAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ProcessoIncidenteUpdateAction implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "processoIncidenteUpdateAction";

	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	@Create
	public void iniciar() {
		if (this.protocolarDocumentoBean == null) {
			ProcessoTrfHome processoTrfHome = ProcessoTrfHome.instance();
			this.protocolarDocumentoBean = new ProtocolarDocumentoBean(processoTrfHome.getInstance().getIdProcessoTrf(), ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL, getActionName());
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