package br.com.infox.pje.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Classe que obtem o conteúdo do html processado.
 * 
 * @author Daniel
 * 
 */
public class ContentCaptureServletResponse extends HttpServletResponseWrapper {

	private ByteArrayOutputStream contentBuffer;
	private PrintWriter writer;

	public ContentCaptureServletResponse(HttpServletResponse resp) {
		super(resp);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (writer == null) {
			contentBuffer = new ByteArrayOutputStream();
			writer = new PrintWriter(contentBuffer);
		}
		return writer;
	}

	/**
	 * Obtem o conteúdo da página html que será interpretado pelo navegador.
	 * 
	 * @return html processado
	 * @throws IOException
	 */
	public String getContent() throws IOException {
		getWriter().flush();
		String xhtmlContent = new String(contentBuffer.toByteArray());
		xhtmlContent = xhtmlContent.replaceAll("<thead>|</thead>|" + "<tbody>|</tbody>", "");
		return xhtmlContent;
	}
}