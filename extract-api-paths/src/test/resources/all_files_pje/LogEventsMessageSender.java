package org.jboss.seam.core;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.itx.util.ComponentUtil;

@Name(LogEventsMessageSender.NAME)
@Scope(ScopeType.APPLICATION)
public class LogEventsMessageSender {
	public static final String NAME = "logEventsMessageSender";
	private EventsQueueBuilder eventsQueueBuilder;
	
	@Logger
	private Log logger;
   
	@PostConstruct
	public void init() {
		try {
			eventsQueueBuilder = (EventsQueueBuilder)Component.getInstance(EventsQueueBuilder.class);
			QueueSession session = eventsQueueBuilder.getLogSession();
			Queue queue = eventsQueueBuilder.getLogQueue();
			QueueReceiver receiver = session.createReceiver(queue);
			LogEventsMessageListener listener = ComponentUtil.getComponent(LogEventsMessageListener.NAME);
			receiver.setMessageListener(listener);
			
		} catch (Exception e) {
			logger.error("Erro ao inicializar o listener da fila {0}. Erro: {1}", EventsQueueBuilder.LOG_QUEUE_NAME, e.getLocalizedMessage());
		}
	}
   
	public void sendMessage(String type, Object... parameters) {
		MessageProducer messageProducer = null;
		try (QueueSession session = eventsQueueBuilder.getLogSession()) {
			messageProducer = session.createProducer(eventsQueueBuilder.getLogQueue());
			if (messageProducer==null) {
				logger.error("Para que o evento assíncrono funcione, é necessário configurar uma fila JMS de nome: 'queue/{0}'. Isso pode ser configurado no arquivo standalone.xml, por exemplo.", EventsQueueBuilder.LOG_QUEUE_NAME);
				return;
			}
			try {
				EventsMessageVO vo = new EventsMessageVO();
				vo.setType(type);
				vo.setParameters(parameters);
				ObjectMessage message = session.createObjectMessage(vo);			
				messageProducer.send(message);
			} finally {
				messageProducer.close();
			}
		} catch (JMSException e) {
			logger.error("A fila {0} falhou ao enviar mensagem de evento assíncrono. Erro: {1}", EventsQueueBuilder.LOG_QUEUE_NAME, e.getLocalizedMessage());
		}
	}
}
