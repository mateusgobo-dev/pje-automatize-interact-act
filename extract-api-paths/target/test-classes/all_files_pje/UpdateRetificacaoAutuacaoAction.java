package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;

import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;

@Name(UpdateRetificacaoAutuacaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class UpdateRetificacaoAutuacaoAction implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "updateRetificacaoAutuacaoAction";

	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	@In(required=false)
	private ProcessoJudicialAction processoJudicialAction;
	
	@RequestParameter
	private Integer idProcesso;
	
	@Create
	public void iniciar() {
		if (this.protocolarDocumentoBean == null) {
			if(this.idProcesso == null){
				idProcesso = processoJudicialAction.getProcessoJudicial().getIdProcessoTrf();
			}
			this.protocolarDocumentoBean = new ProtocolarDocumentoBean(this.idProcesso, ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL, getActionName());
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