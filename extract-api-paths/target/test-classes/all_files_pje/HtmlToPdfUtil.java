package br.com.itx.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ajax4jsf.org.w3c.tidy.Tidy;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

public class HtmlToPdfUtil {
	
	private static Tidy tidy;
	
	static {
		tidy = new Tidy();
		tidy.setShowErrors(0);
		tidy.setQuiet(true);
		tidy.setErrout(null);
	}
	
	private HtmlToPdfUtil() {}
	
	public static byte[] convertToPdf(String conteudo) throws DocumentException {
		return convertToPdf(conteudo.getBytes());
	}
	
	public static byte[] convertToPdf(byte[] conteudo) throws DocumentException {
		ByteArrayInputStream in = null;
		ByteArrayOutputStream out = null;
		byte[] result = null;

		try {
			in = new ByteArrayInputStream(conteudo);
			out = new ByteArrayOutputStream();
			convertToPdf(in, out);
			result = out.toByteArray();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}
	
	public static void convertToPdf(InputStream in, OutputStream out) throws DocumentException {
		ITextRenderer renderer = new ITextRenderer(); 
		renderer.setDocument(createDocument(in), null);  
	    renderer.layout();
	    renderer.createPDF(out);
	}
	
	private static Document createDocument(InputStream in) {
		return tidy.parseDOM(in, null);
	}
}
