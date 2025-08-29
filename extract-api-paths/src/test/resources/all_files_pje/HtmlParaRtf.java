package br.com.itx.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;

public class HtmlParaRtf {
	
	
	public static byte[] converte(String html) throws DocumentException, IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		StyleSheet styles = new StyleSheet();
		styles.loadTagStyle("body", "font-family", "times new roman");
		styles.loadTagStyle("body", "font-size", "12px");
		
		Document documento = new Document();
		documento.open();
		HTMLWorker worker = new HTMLWorker(documento); 
		worker.setStyleSheet(styles);

		StringReader stringReader = new StringReader(html); 
		worker.parse(stringReader); 

		documento.close(); 
		return out.toByteArray();
	}

}
