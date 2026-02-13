package br.com.itx.util;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.servicos.MimeUtilChecker;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoTrf;
import br.jus.pje.nucleo.util.StringUtil;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import javax.faces.context.FacesContext;

import java.io.IOException;
import java.io.OutputStream;
import br.jus.csjt.pje.business.pdf.GeradorPdfUnificado;
import br.jus.csjt.pje.business.pdf.PdfException;

import javax.servlet.http.HttpServletRequest;

import com.lowagie.text.pdf.PdfReader;

import org.apache.commons.lang3.StringUtils;

public final class PdfUtil {

	private PdfUtil() {
	}

	public static boolean verificarTamanhoValidoPagina(PdfReader pdf, TipoProcessoDocumentoTrf tipo) {
		if(tipo.getTamanhoMaximoPagina() != null){
			Integer numeroPaginas = pdf.getNumberOfPages();
			Integer tamanhoArquivo = pdf.getFileLength();
			double tamanhoMedioArquivo = tamanhoArquivo/numeroPaginas.doubleValue();
			if ( tipo.getTamanhoMaximoPagina() == null ) {
				return  (tamanhoMedioArquivo <= 1500*1024d);
			}
			double tamanhoMaximoPaginaBytes = tipo.getTamanhoMaximoPagina()*1024d;
			if (tamanhoMedioArquivo <= tamanhoMaximoPaginaBytes) {
				return true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Metodo responsavel por formatar a folha de estilo do PDF quando gerado
	 * pelo CKEditor
	 * 
	 * @param html
	 *            o HTML gerado no CKEditor
	 * @return <code>String</code>, o HTML do PDF
	 */
	public static String formatarCSSGeradoPeloCKEditor(String html){
		if(html != null && html.contains(".paginaA4")) {
			String cssImpressao = "body{background: #fff; margin: 0 auto; padding: 0; width: 100%} .conteudoPaginaA4{margin: 0; margin-left: 10mm; padding: 0; height: 100%; overflow: visible} .paginaA4 {padding: 0; margin: 0; border: initial; border-radius: initial; box-shadow: initial; background: initial; page-break-after: avoid; width: 100%; min-height: 100%}";
			String pattern = "(@media\\sprint\\s?[{])((\\s+?[a-zA-Z0-9:;{}.-]+)+(\\s+?[}]\\s+?[}]))";
			html = html.replaceAll(pattern, "$1" + cssImpressao + "}");
			
			byte[] bytes = html.getBytes();
			String encoding = new MimeUtilChecker().getEncoding(bytes);
			html = StringUtil.transformarParaUTF8(bytes, encoding);
		}
		return html;
	}
	
	public static String formatarCSSEditor(String html){		
		if (!StringUtils.isBlank(html)) {
			String cssPDF = ParametroUtil.instance().cssPDFDocumento();
			if (!StringUtils.isBlank(cssPDF)) {
				cssPDF = "<style>" + cssPDF + "</style>";
				if (!html.contains(cssPDF)) {
					html = cssPDF + html;
				}
			}
		}
		return formatarCSSGeradoPeloCKEditor(html);
	}

	/**
	 * Registra o cookie 'cookieTemporizadorDownload' para que a tela identifique o fim do 
	 * processamento de download.
	 * 
	 * @param response HttpServletResponse
	 */
	private static void registrarCookieTemporizadorDownload(HttpServletResponse response) {
		Cookie cookie = new Cookie("cookieTemporizadorDownload", "finalizado");  
		cookie.setMaxAge(30); //tempo de vida de 30 segundos.
		response.addCookie(cookie);
	}
	
	public static void download(String conteudo, String nomeArquivo, ProcessoTrf processo) {
		String extensao = ".pdf";
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.reset();
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""	+ nomeArquivo + extensao + "\"");
		gerarPdf(request, response, conteudo, processo);
		registrarCookieTemporizadorDownload(response);
		facesContext.responseComplete();
	}
	
	private static void gerarPdf(HttpServletRequest request, HttpServletResponse response, String conteudo, ProcessoTrf processo)  {
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			String resourcePath = request.getScheme() + "://"
					+ request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath();
			GeradorPdfUnificado geradorPdf = new GeradorPdfUnificado();
			geradorPdf.setResurcePath(resourcePath);
			geradorPdf.gerarPdfCabecalho(conteudo, out, processo);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PdfException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}