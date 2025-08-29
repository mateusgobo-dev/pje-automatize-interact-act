package br.jus.cnj.pje.intercomunicacao.seguranca;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * Abstração de SOAPHandler
 * @author rodrigo
 *
 */
public abstract class CatalogoServicosSOAPMessageHandler extends CatalogoServicosMessageHandler implements SOAPHandler<SOAPMessageContext> {

	@Override
	public void close(MessageContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		return doHandleMessage(context);
	}
	
	protected abstract boolean doHandleMessage(SOAPMessageContext context);
	

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

}
