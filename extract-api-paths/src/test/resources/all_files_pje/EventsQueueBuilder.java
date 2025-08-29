package org.jboss.seam.core;

import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.nucleo.Parametros;

@Name(EventsQueueBuilder.NAME)
@Scope(ScopeType.APPLICATION)
@Install(precedence = Install.FRAMEWORK)
public class EventsQueueBuilder {
	
	public static final String NAME = "eventsQueueBuilder";
	public static final String QUEUE_NAME = "PJeEventsQueue";
	public static final String LOG_QUEUE_NAME = "PJeLogEventsQueue";
	
	private QueueConnection connection;
	private QueueConnection logConnection;
	
	private InitialContext context;
	private InitialContext logContext;
	
	private Queue queue;
	private Queue logQueue;
	
	@In(value= Parametros.JNDINAMEJMSCONNECTION, required=false, scope= ScopeType.APPLICATION)
	private String jndiConexaoJms;

	@Logger
	private Log log;
	
	@Create
	public void init() {
		initEventQueue();
		initLogEventQueue();
	}
	
	@PreDestroy
	public void destroy() {
		destroyEventConnection();
		destroyLogEventConnection();
	}
	
	private void initEventQueue() {
		try {
			this.context = new InitialContext();
			if(this.jndiConexaoJms == null){
				this.jndiConexaoJms = "/ConnectionFactory";
			}
			this.connection = ((QueueConnectionFactory)context.lookup(jndiConexaoJms)).createQueueConnection();
			this.connection.start();
			this.queue = (Queue)context.lookup("queue/" + EventsQueueBuilder.QUEUE_NAME);
		}
		catch(Exception e) {
			log.error("Erro ao iniciar a fila {0}. Verifique no arquivo de configuração do servidor se a fila {0} está corretamente configurada",EventsQueueBuilder.QUEUE_NAME, e);
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}

	private void initLogEventQueue() {
		try {
			this.logContext = new InitialContext();
			if(this.jndiConexaoJms == null){
				this.jndiConexaoJms = "/ConnectionFactory";
			}
			this.logConnection = ((QueueConnectionFactory)context.lookup(jndiConexaoJms)).createQueueConnection();
			this.logConnection.start();
			this.logQueue = (Queue)context.lookup("queue/" + EventsQueueBuilder.LOG_QUEUE_NAME);
		}
		catch(Exception e) {
			log.error("Erro ao iniciar a fila {0}. Verifique no arquivo de configuração do servidor se a fila {0} está corretamente configurada",EventsQueueBuilder.LOG_QUEUE_NAME, e);
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}

	private void destroyEventConnection() {
		if (this.connection != null) {
			try {
				this.connection.close();
			} catch (JMSException e) {
				log.error("Erro ao encerrar fila {0}.", EventsQueueBuilder.QUEUE_NAME, e);
			}
		}
	}

	private void destroyLogEventConnection() {
		if (this.logConnection != null) {
			try {
				this.logConnection.close();
			} catch (JMSException e) {
				log.error("Erro ao encerrar fila {0}.", EventsQueueBuilder.LOG_QUEUE_NAME, e);
			}
		}
	}

	public Queue getQueue() {
		return this.queue;
	}
	
	public QueueSession getSession() throws JMSException {
		return this.connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
	}
	
	public QueueConnection getConnection() {
		return this.connection;
	}

	public Queue getLogQueue() {
		return this.logQueue;
	}
	
	public QueueSession getLogSession() throws JMSException {
		return this.logConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
	}
	
	public QueueConnection getLogConnection() {
		return this.logConnection;
	}
}