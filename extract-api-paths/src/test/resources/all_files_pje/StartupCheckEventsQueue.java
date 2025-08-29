package br.jus.pje.startup.check;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;

public class StartupCheckEventsQueue implements StartupCheckItem {
	
	private static final String QUEUE_NAME = "PJeEventsQueue";

	@SuppressWarnings("unused")
	@Override
	public void check() throws PJeCheckException{
		String jndiConexaoJms = "/ConnectionFactory";
		QueueConnection connection = null;
		InitialContext context = null;
		try {
			context = new InitialContext();
			connection = ((QueueConnectionFactory)context.lookup(jndiConexaoJms)).createQueueConnection();
			connection.start();
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = (Queue)context.lookup("queue/" + StartupCheckEventsQueue.QUEUE_NAME);
		}
		catch(Exception e) {
			String msg = String.format("Erro ao iniciar a fila %1$s. Verifique no arquivo de configuração do servidor se a fila %1$s está corretamente configurada: %2$s", StartupCheckEventsQueue.QUEUE_NAME, e.getLocalizedMessage());
			throw new PJeCheckException(msg);
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
