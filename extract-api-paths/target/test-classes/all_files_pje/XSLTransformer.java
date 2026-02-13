package br.com.infox.editor.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Scanner;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XSLTransformer {
	
	private static TransformerFactory factory = TransformerFactory.newInstance();
	private Transformer transformer;
	
	public XSLTransformer(String inXSL) throws TransformerConfigurationException {
		transformer = factory.newTransformer(getStreamSource(inXSL));
	}

	public String transform(String inXML) throws TransformerException {
		StringWriter stringWriter = new StringWriter();
		transformer.transform(getStreamSource(inXML), new StreamResult(stringWriter));
		return stringWriter.toString();
	}

	private StreamSource getStreamSource(String inXML) {
		StringReader stringReader = new StringReader(inXML);
		return new StreamSource(stringReader);
	}

	public static void main(String[] args) throws TransformerConfigurationException, TransformerException, FileNotFoundException {
		Scanner sc = new Scanner(new File("/temp/text.xsl"));
		sc.useDelimiter("\\Z");
		String inXSL = sc.next();
		String inXML = "<topico><titulo>Empire Burlesque</titulo></topico>";
		String inXML2 = "<topico><conteudo>Novo</conteudo></topico>";
		
		XSLTransformer st = new XSLTransformer(inXSL);
		String out = st.transform(inXML);
		String out2 = st.transform(inXML2);
		
		System.out.println(out);
		System.out.println(out2);
	}
}
