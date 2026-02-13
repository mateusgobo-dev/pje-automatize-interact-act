package org.jboss.seam.core;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name(LogEventsMessageListener.NAME)
@Scope(ScopeType.APPLICATION)
public class LogEventsMessageListener implements MessageListener {
	public static final String NAME = "logEventsMessageListener";
	@Logger
	private Log logger;
	
	@In
	private Events events;
	
	@Override
	public synchronized void onMessage(Message message) {
		try {
			if (message instanceof ObjectMessage) {
				Serializable obj = ((ObjectMessage)message).getObject();
				if (obj instanceof EventsMessageVO) {
					EventsMessageVO vo = (EventsMessageVO)obj;
					if( vo.getParameters() == null || vo.getParameters().length == 0) {
						events.raiseEvent(vo.getType());
					}
					else {
						events.raiseEvent(vo.getType(), vo.getParameters().length == 1 ? vo.getParameters()[0] : vo.getParameters());
					}
				}
			}
		} catch (JMSException ex) {
			logger.error("Erro ao processar mensagem JMS no listener de eventos: {0}", ex.getLocalizedMessage());
		}
	}
	
}
