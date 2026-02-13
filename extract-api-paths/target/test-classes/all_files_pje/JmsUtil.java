package br.jus.csjt.pje.commons.jms;


import java.util.Properties;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import br.com.infox.utils.Constantes;


public class JmsUtil {

	public static final String TOPICO_SELETOR = "Topico";

	private final static Logger logger = Logger.getLogger(JmsUtil.class.getName());
	
	private static TopicConnection conn;
	private static TopicSession session;
	private static Context context;

	static {
		try {
			initializeListener();
		} catch (Exception e) {
			logger.error("Falha na conexao com o servidor JMS", e);
		}
	}
	
	private static void initializeListener() throws JMSException, NamingException {

		Properties env = new Properties();
		env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.JndiLoginInitialContextFactory");
		env.setProperty(Context.PROVIDER_URL, Constantes.ServidorJNDI.URL_JNDI);
		env.setProperty(Context.SECURITY_PRINCIPAL, Constantes.ServidorJNDI.JNDI_USER);
		env.setProperty(Context.SECURITY_CREDENTIALS, Constantes.ServidorJNDI.JNDI_PASSWORD);

		context = new InitialContext(env);

		Object tmp = context.lookup("ConnectionFactory");

		TopicConnectionFactory tcf = (TopicConnectionFactory) tmp;
		conn = tcf.createTopicConnection();

		session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
		conn.start();
		
	}
	
	/**
	 * Inscricao em topico do JMS.
	 * 
	 * @param topicName nome do topico
	 * @param listener objeto que eh registrado no jms para tratar a chamada quando necessario. Deve implementar a interface {@link MessageListener}} 
	 * 
	 * @throws JMSException
	 * @throws NamingException
	 * 
	 * @see MessageListener
	 */
	public static void inscricaoTopico(String topicName, MessageListener listener) 
			throws JMSException, NamingException {
		Topic topic = (Topic) context.lookup(topicName);
		TopicSubscriber recv = session.createSubscriber(topic);
		recv.setMessageListener(listener);
		
		inscricaoTopico(topicName, listener, null);
	}
	
	/**
	 * Inscricao em topico do JMS.
	 * 
	 * @param topicName nome do topico
	 * @param listener objeto que eh registrado no jms para tratar a chamada quando necessario. Deve implementar a interface {@link MessageListener}}
	 * @param seletor utilizado para restringir as mensagens que serao tratadas pela subscricao.
	 * 
	 * @throws JMSException
	 * @throws NamingException
	 */
	public static void inscricaoTopico(String topicName, MessageListener listener, Integer seletor) 
			throws JMSException, NamingException {
		Topic topic = (Topic) context.lookup(topicName);
		TopicSubscriber recv = session.createSubscriber(topic, TOPICO_SELETOR + " = " + seletor, false);
		recv.setMessageListener(listener);
	}
	
	public static void removerTopico(String seletor) throws JMSException{
		session.unsubscribe(TOPICO_SELETOR + " = " + seletor);
	}
	
	/**
	 * @author thiago carvalho
	 * Valida se a infra de mensageria esta disponivel.
	 */
	public static boolean isInfraValida() {
		boolean valida = true;
		try {
			if (Constantes.ServidorJNDI.JNDI_NOME_TOPICO_SESSAO_JULGAMENTO != null) {
				context.lookup(Constantes.ServidorJNDI.JNDI_NOME_TOPICO_SESSAO_JULGAMENTO);
			} else {
				logger.error("Propriedade JNDI_NOME_TOPICO_SESSAO_JULGAMENTO não encontrada no sistema, favor configura-la.");
				return !valida;
			}
		} catch (NamingException e) {
			e.printStackTrace();
			logger.error("NamingException, nao possivel recuperar o topico, favor verificar a configuracao.");
			return !valida;
		}
		return valida;
	}
	
	/**
	 * Inscricao no topico de sessao de julgamento do JMS.
	 * 
	 * @param listener objeto que eh registrado no jms para tratar a chamada quando necessario. Deve implementar a interface {@link MessageListener}}
	 * @param seletor seletor utilizado para restringir as mensagens que serao tratadas pela subscricao.
	 * 
	 * @throws JMSException
	 * @throws NamingException
	 */
	public static void inscricaoSessaoJulgamento(MessageListener listener, Integer seletor) 
			throws JMSException, NamingException {
		inscricaoTopico(Constantes.ServidorJNDI.JNDI_NOME_TOPICO_SESSAO_JULGAMENTO, listener, seletor);
	}
	
	/**
	 * Envia uma mensagem a um topico do JMS.
	 * 
	 * @param topicName nome do topico
	 * @param mensagem
	 * 
	 * @throws JMSException
	 * @throws NamingException
	 */
	public static void enviarMensagem(String topicName, String mensagem) 
			throws JMSException, NamingException {
		Topic topic = (Topic) context.lookup(topicName);
		// Send a text msg
		TopicPublisher send = session.createPublisher(topic);
		
		TextMessage tm = session.createTextMessage(mensagem);
		
		tm.setStringProperty("JMSPriority", "t1");
		
		send.publish(tm);
		send.close();
	}
	
	/**
	 * Envia uma mensagem a um topico do JMS.
	 * 
	 * @param topicName nome do topico
	 * @param mensagem
	 * @param seletor utilizado para restringir as mensagens que serao tratadas pela subscricao.
	 * 
	 * @throws JMSException
	 * @throws NamingException
	 */
	public static void enviarMensagem(String topicName, String mensagem, Integer seletor) 
			throws JMSException, NamingException {
		Topic topic = (Topic) context.lookup(topicName);
		// Send a text msg
		TopicPublisher send = session.createPublisher(topic);
		
		TextMessage tm = session.createTextMessage(mensagem);
		
		tm.setIntProperty(TOPICO_SELETOR, seletor);
		
		send.publish(tm);
		send.close();
	}
	
	/**
	 * Envia uma mensagem ao topico da sessao de julgamento pelo JMS.
	 * 
	 * @param mensagem
	 * @param seletor utilizado para restringir as mensagens que serao tratadas pela subscricao.
	 * 
	 * @throws JMSException
	 * @throws NamingException
	 */
	public static void enviarMensagemSessaoJulgamento(String mensagem, Integer seletor) 
			throws JMSException, NamingException {
		Topic topic = (Topic) context.lookup(Constantes.ServidorJNDI.JNDI_NOME_TOPICO_SESSAO_JULGAMENTO);
		// Send a text msg
		TopicPublisher send = session.createPublisher(topic);
		
		TextMessage tm = session.createTextMessage(mensagem);
		
		tm.setIntProperty(TOPICO_SELETOR, seletor);
		
		send.publish(tm);
		send.close();
	}
	
	/**
	 * Disconecta do JMS
	 * 
	 * @throws JMSException
	 */
	public static void disconnect() throws JMSException {
		if (conn != null) {
			conn.stop();
		}

		if (session != null) {
			session.close();
		}

		if (conn != null) {
			conn.close();
		}
	}
	
	public static void main(String[] args) {
		try {
			String nome = Constantes.ServidorJNDI.JNDI_NOME_TOPICO_SESSAO_JULGAMENTO;
			JmsUtil.enviarMensagem(nome, "1", 1);
			Thread.sleep(1000);
			JmsUtil.enviarMensagem(nome, "2", 2);
			Thread.sleep(1000);
			JmsUtil.enviarMensagem(nome, "3", 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}