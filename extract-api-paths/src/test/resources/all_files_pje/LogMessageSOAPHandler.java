/**
 * LogMessageSOAPHandler.java
 * 
 * Data: 28/08/2014
 */
package br.jus.cnj.pje.intercomunicacao.util;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import br.com.itx.util.XmlUtil;

/**
 * Classe responsável pela exibição da mensagem soap no console.
 * 
 * @author Adriano Pamplona
 */
public class LogMessageSOAPHandler implements SOAPHandler<SOAPMessageContext> {

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		StringBuilder log = new StringBuilder();
		SOAPMessage message = context.getMessage();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			message.writeTo(baos);
			baos.flush();
			baos.close();
			
			log.append(new String(baos.toByteArray()));
			System.out.println("--- INÍCIO ------------------------------------------------------");
			System.out.println(XmlUtil.formatar(log.toString()));
			System.out.println("--- FIM  --------------------------------------------------------");
		} catch (Exception e) {
		}
		return true;
	}

	@Override
	public void close(MessageContext arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean handleFault(SOAPMessageContext arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}
}
