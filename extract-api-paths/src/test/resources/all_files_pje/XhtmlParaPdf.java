/**
 * XhtmlParaPdf.java
 * 
 * Data: 16/10/2013
 */
package br.jus.csjt.pje.business.pdf;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.jboss.seam.document.ByteArrayDocumentData;
import org.jboss.seam.document.DocumentData;
import org.jboss.seam.document.DocumentStore;
import org.jboss.seam.faces.Renderer;

/**
 * Classe utilitária responsável pela geração do PDF a partir de um XHTML.
 * 
 * @author adriano.pamplona
 */
public final class XhtmlParaPdf {

	/**
	 * Construtor.
	 */
	private XhtmlParaPdf() {
		// construtor.
	}

	/**
	 * Converte o XHTML passado por parâmetro para um PDF.
	 * 
	 * @param xhtmlPath
	 * @return PDF
	 */
	public static byte[] converterParaBytes(String xhtmlPath) {
		Renderer renderer = Renderer.instance();
		renderer.render(xhtmlPath);

		return obterBytesPdf();
	}

	/**
	 * Converte o XHTML passado por parâmetro para um PDF.
	 * 
	 * @param xhtmlPath
	 *            Path do arquivo XHTML.
	 * @param pdfPath
	 *            Path do arquivo PDF que será gerado.
	 * @return OutputStream do PDF gerado.
	 * @throws PdfException
	 */
	public static OutputStream converterParaArquivo(String xhtmlPath,
			String pdfPath) throws PdfException {
		byte[] bytes = converterParaBytes(xhtmlPath);
		OutputStream resultado = null;
		try {
			resultado = new FileOutputStream(pdfPath);
			resultado.write(bytes);
			resultado.flush();
			resultado.close();
		} catch (Exception e) {
			throw new PdfException("Erro ao converter XHTML para PDF!", e);
		}
		return resultado;
	}

	/**
	 * Retorna o array de bytes do PDF renderizado através do Render.instance().
	 * 
	 * @return array de bytes do PDF renderizado.
	 */
	private static byte[] obterBytesPdf() {
		byte[] resultado = null;

		DocumentStore pdf = DocumentStore.instance();

		if (pdf != null) {
			DocumentData data = pdf.getDocumentData("1");
			if (data instanceof ByteArrayDocumentData) {
				ByteArrayDocumentData byteData = (ByteArrayDocumentData) data;
				resultado = byteData.getData();
			}
		}
		return resultado;
	}
}
