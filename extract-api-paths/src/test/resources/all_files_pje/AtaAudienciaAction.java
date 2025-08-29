package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;

import br.com.infox.cliente.home.ProcessoAudienciaHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name(AtaAudienciaAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class AtaAudienciaAction implements Serializable, ArquivoAssinadoUploader{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "ataAudienciaAction";
	
	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	private ProcessoDocumento ataAudiencia;
	
	@In(create = true)
	private DocumentoJudicialService documentoJudicialService;
	
	@In(create = true, required = true)
	private FacesMessages facesMessages;
	
	public void iniciar(){
		if(this.protocolarDocumentoBean == null){
			this.protocolarDocumentoBean = new ProtocolarDocumentoBean(ProcessoAudienciaHome.instance().getInstance().getProcessoTrf().getIdProcessoTrf(), 
																   ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL, 
																   getActionName());
		}
		
		ataAudiencia = this.protocolarDocumentoBean.getDocumentoPrincipal();
	}
	
	public void concluirAtaAudiencia(){
		try {
			if (ataAudiencia == null) {
				ataAudiencia = this.protocolarDocumentoBean.getDocumentoPrincipal();
			}
			this.protocolarDocumentoBean.concluirAssinaturaAction();
		} catch (Exception e) {
			e.printStackTrace();
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

	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}
	
	public void setProtocolarDocumentoBean(ProtocolarDocumentoBean protocolarDocumentoBean) {
		this.protocolarDocumentoBean = protocolarDocumentoBean;
	}
	
	public void limparTela( ) {
		setProtocolarDocumentoBean(null);
		setAtaAudiencia(null);
	}

	public static AtaAudienciaAction instance() {
		return ComponentUtil.getComponent(AtaAudienciaAction.NAME);
	}

	public ProcessoDocumento getAtaAudiencia() {
		return ataAudiencia;
	}

	public void setAtaAudiencia(ProcessoDocumento ataAudiencia) {
		this.ataAudiencia = ataAudiencia;
	}
}