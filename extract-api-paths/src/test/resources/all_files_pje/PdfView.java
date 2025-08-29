package br.jus.cnj.pje.view;

import java.io.OutputStream;
import java.util.Date;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;

import br.com.infox.action.VisualizadorPdf;
import br.com.infox.cliente.component.ValidacaoAssinaturaProcessoDocumento;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.vo.ArquivoDownload;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;

/**
 * Componente para visualização de pdf.
 * 
 * @author Carlos Lisboa.
 *
 */

@Name(PdfView.NAME)
@Scope(ScopeType.EVENT)
public class PdfView {
	
	@In
	protected FacesContext facesContext;
	
	@In
	private ProcessoDocumentoBinManager processoDocumentoBinManager;
	
	@In
	private DocumentoBinManager documentoBinManager;
	
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	@RequestParameter
	public Integer idProcessoDocumento;
	
	public static final String NAME = "pdfView";
	
	VisualizadorPdf visualizadorPdf;

	/**
	 * @return PdfView
	 */
	public static PdfView instance() {
		return ComponentUtil.getComponent(PdfView.class);
	}
	
	/**
	 * Executa os métodos necessários para visualização do pdf
	 * @throws Exception
	 */
	public void visualizarPdf() throws Exception{
		
		try {
			ProcessoDocumento procDoc = recuperarProcessoDocumento();
			ProcessoDocumentoBin procDocBin = procDoc.getProcessoDocumentoBin();
			byte[] data = documentoBinManager.getData(procDoc.getProcessoDocumentoBin().getNumeroDocumentoStorage());
			ValidacaoAssinaturaProcessoDocumento validacaoAssinatura = ComponentUtil.getComponent(ValidacaoAssinaturaProcessoDocumento.NAME);
			data = validacaoAssinatura.montarTextoFolhaDeRosto(procDocBin, data);
			ArquivoDownload arquivo = new ArquivoDownload(procDocBin.getNomeArquivo(),data);
			gerarResponse(arquivo);
		} catch (Exception e) {
			// TODO: handle exception
			throw new Exception("Não foi possivel recuperar o documento pdf, mensagem interna: "+e.getMessage());
		}
		
	}

	/**
	 * Recupera o processoDocumenrto de acordo com o id do documento informado.
	 * @return
	 * @throws PJeBusinessException
	 */
	private ProcessoDocumento recuperarProcessoDocumento()	throws PJeBusinessException {
		ProcessoDocumento procDoc = processoDocumentoManager.findById(idProcessoDocumento);
		return procDoc;
	}

	/**
	 * gerar o response para devida exibição no iframe.
	 * @param documento
	 * @throws Exception
	 */
	public void gerarResponse (ProcessoDocumentoBin documento) throws Exception {
		if (documento != null) {
			byte[] data = DocumentoBinManager.instance().getData(documento.getNumeroDocumentoStorage());
			ArquivoDownload arquivo = new ArquivoDownload(documento.getNomeArquivo(), data);
			gerarResponse(arquivo);
		}
	}
	
	/**
	 * gerar o response para devida exibição no iframe.
	 * @param documentoPdf
	 * @throws Exception
	 */
	public void gerarResponse (ArquivoDownload documentoPdf) throws Exception {
		if(documentoPdf.getConteudo() != null){
			HttpServletResponse response = getHttpResponse();
			response.setContentType("application/pdf");
			response.setContentLength(documentoPdf.getConteudo().length);
			response.setHeader("Content-disposition", "inline;filename=\"" + documentoPdf.getNome() + "\"");
			OutputStream out = response.getOutputStream();
			out.write(documentoPdf.getConteudo());
			out.flush();
			facesContext.responseComplete();
		}
	}

	/**
	 * Retorna o HttpResponse
	 * @return
	 */
	private HttpServletResponse getHttpResponse() {
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		return response;
	}
	
	/**
	 * Retorna a url  para o processamento do pdf
	 * @param visualizadorPdf
	 * @return
	 */
	public String getUrl(VisualizadorPdf visualizadorPdf){
		return getRequest().getContextPath()+"/pdfView.seam?idProcessoDocumento="+visualizadorPdf.getIdProcessoDocumento()+"+&u="+new Date().getTime();
	}

	/**
	 * Retorna o Request
	 * @return
	 */
	private HttpServletRequest getRequest() {
		HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
		return request;
	}

}
