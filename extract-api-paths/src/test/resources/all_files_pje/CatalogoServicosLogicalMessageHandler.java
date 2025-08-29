package br.jus.cnj.pje.intercomunicacao.seguranca;

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;


/**
 * Abstração de LogicalHandler
 * @author rodrigo
 *
 */
public abstract class CatalogoServicosLogicalMessageHandler extends CatalogoServicosMessageHandler implements LogicalHandler<LogicalMessageContext>{

	@Override
	public void close(MessageContext context) {
	}

	@Override
	public boolean handleFault(LogicalMessageContext context) {
		return false;
	}

	@Override
	public boolean handleMessage(LogicalMessageContext context) {
		return doHandleMessage(context);
	}
	
	protected abstract boolean doHandleMessage(LogicalMessageContext context);
}
