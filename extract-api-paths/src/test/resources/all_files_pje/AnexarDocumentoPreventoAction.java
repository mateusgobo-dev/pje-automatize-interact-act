package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name(AnexarDocumentoPreventoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class AnexarDocumentoPreventoAction implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "anexarDocumentoPreventoAction";
	public static final String  MAPA_ASSINATURAS = "pje:assinatura:mapa";
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@Override
	public String getActionName() {
		return NAME;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		List<ArquivoAssinadoHash> arquivosAssinados = null;
		if (Contexts.getSessionContext().get(MAPA_ASSINATURAS) != null) {
			arquivosAssinados = (List<ArquivoAssinadoHash>)Contexts.getSessionContext().get(MAPA_ASSINATURAS);
		}
		else {
			arquivosAssinados = new ArrayList<ArquivoAssinadoHash>();
		}
		arquivosAssinados.add(arquivoAssinadoHash);
		Contexts.getSessionContext().set(MAPA_ASSINATURAS, arquivosAssinados);
	}
	
	public String getUrlDocsField() {
		List<ProcessoDocumento> documentos = new ArrayList<ProcessoDocumento>();
		documentos.add(ProcessoDocumentoHome.instance().getInstance());
		return this.documentoJudicialService.getDownloadLinks(documentos);
	}
	
	@SuppressWarnings("unchecked")
	public static List<ArquivoAssinadoHash> getAssinaturas(){
		if (Contexts.getSessionContext().get(MAPA_ASSINATURAS) != null) {
			return  (List<ArquivoAssinadoHash>)Contexts.getSessionContext().get(MAPA_ASSINATURAS);
		}
		return null;
	}
	
}