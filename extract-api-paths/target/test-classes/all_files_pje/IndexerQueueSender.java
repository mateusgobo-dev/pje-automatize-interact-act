package br.jus.cnj.pje.indexer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.*;
import javax.naming.InitialContext;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;

@Name(IndexerQueueSender.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class IndexerQueueSender {

	public static final String NAME = "indexerQueueSender";
	
	private QueueConnection connection;

	private InitialContext context;

	@In(value= Parametros.JNDINAMEJMSCONNECTION, required=false, scope= ScopeType.APPLICATION)
	private String jndiConexaoJms;
	
	@Logger
	private Log logger;

	@PostConstruct
	public void init() {
		try {
			this.context = new InitialContext();

			if(jndiConexaoJms == null){
				jndiConexaoJms = "/ConnectionFactory";
			}

			this.connection = ((QueueConnectionFactory)context.lookup(jndiConexaoJms)).createQueueConnection();
			this.connection.start();
			
			QueueSession session = this.connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			
			Queue queue = (Queue)context.lookup("queue/PJeIndexerQueue");
			
			QueueReceiver receiver = session.createReceiver(queue);
			
			IndexerQueueListener indexerQueueListener = ComponentUtil.getComponent(IndexerQueueListener.NAME);
			receiver.setMessageListener(indexerQueueListener);
			
		} catch (Exception e) {
			logger.warn("Erro ao inicializar IndexerQueueSender.", e.getLocalizedMessage());
		}
	}
	
	@PreDestroy
	public void destroy() {
		if (this.connection != null) {
			try {
				this.connection.close();
			} catch (JMSException e) {
				logger.debug("Erro ao encerrar IndexerQueueSender.", e);
			}
		}
	}
	
	public void sendMessage(IndexVO indexVO) {
		MessageProducer messageProducer = null;
		try (QueueSession session = this.connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE)) {
			messageProducer = session.createProducer((Queue)this.context.lookup("queue/PJeIndexerQueue"));
			if (messageProducer==null) {
				logger.warn("Para que a indexação funcione, é necessário configurar uma fila JMS de nome: 'queue/PJeIndexerQueue'. Isso pode ser configurado no arquivo standalone.xml, por exemplo.");
				return;
			}
			try {
				messageProducer.setDeliveryDelay(5000);
				ObjectMessage message = session.createObjectMessage(indexVO);			
				messageProducer.send(message);
			} finally {
				messageProducer.close();
			}
		} catch (JMSException | NamingException e) {
			logger.info("O IndexerQueueSender falhou ao enviar mensagem de indexação.", e);
		}
	}
}
