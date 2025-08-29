package br.jus.csjt.pje.commons.util.dejt;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * 
 * @author Frederico Carneiro
 * @since 1.2.0
 * @category PJE-JT
 * 
 */
public class ValidacaoXML {

	private static String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
	
	public static Boolean validacaoXML(byte[] xml, URL caminhoXSD) {
		Boolean retorno = true;
		try {
			JAXBContext context = JAXBContext.newInstance("br.jus.pje.jt.util.dejt");
			Unmarshaller unmarshaller = context.createUnmarshaller();
			ByteArrayInputStream input = new ByteArrayInputStream(xml);
			if (caminhoXSD != null) {
				SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
				Schema schema = sf.newSchema(caminhoXSD);
				unmarshaller.setSchema(schema);
				unmarshaller.setEventHandler(new ValidationEventHandler() {
					@Override
					public boolean handleEvent(ValidationEvent ve) {
						System.out.println("Erro na validação do XML");
						// if (ve.getSeverity() != ValidationEvent.WARNING) {
						ValidationEventLocator vel = ve.getLocator();
						System.out.println("Line:Col[" + vel.getLineNumber() + ":" + vel.getColumnNumber() + "]:"
								+ ve.getMessage());
						// }
						return false;
					}
				});
			} else {
				System.out.println("Arquivo XSD para validação do XML não encontrado");
			}
			unmarshaller.unmarshal(input);
			retorno = true;
		} catch (SAXException se) {
			System.out.println("Erro na validação do XML: " + se);
			// se.printStackTrace();
			retorno = false;
		} catch (JAXBException je) {
			System.out.println("Erro na validação do XML: " + je);
			// je.printStackTrace();
			retorno = false;
		}

		return retorno;

	}

	public static Boolean validacaoXML(byte[] xml, File xsd) {
		Boolean retorno = true;
		try {
			JAXBContext context = JAXBContext.newInstance("br.jus.pje.jt.util.dejt");
			Unmarshaller unmarshaller = context.createUnmarshaller();
			ByteArrayInputStream input = new ByteArrayInputStream(xml);
			if (xsd != null) {
				SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
				Schema schema = sf.newSchema(xsd);
				unmarshaller.setSchema(schema);
				unmarshaller.setEventHandler(new ValidationEventHandler() {
					@Override
					public boolean handleEvent(ValidationEvent ve) {
						System.out.println("Erro na validação do XML");
						// if (ve.getSeverity() != ValidationEvent.WARNING) {
						ValidationEventLocator vel = ve.getLocator();
						System.out.println("Line:Col[" + vel.getLineNumber() + ":" + vel.getColumnNumber() + "]:"
								+ ve.getMessage());
						// }
						return false;
					}
				});
			} else {
				System.out.println("Arquivo XSD para validação do XML não encontrado");
			}
			unmarshaller.unmarshal(input);
			retorno = true;
		} catch (SAXException se) {
			System.out.println("Erro na validação do XML: " + se);
			// se.printStackTrace();
			retorno = false;
		} catch (JAXBException je) {
			System.out.println("Erro na validação do XML: " + je);
			// je.printStackTrace();
			retorno = false;
		}

		return retorno;

	}
}
