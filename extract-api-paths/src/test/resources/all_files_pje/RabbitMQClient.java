package br.jus.cnj.pje.amqp;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.transaction.Transaction;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.hibernate.search.batchindexing.impl.Executors;
import org.jboss.seam.transaction.UserTransaction;

@Name(RabbitMQClient.NAME)
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class RabbitMQClient {

	public static final String NAME = "rabbitMQClient";
	
	private static final int MIN_THREADS = Math.min(8, Runtime.getRuntime().availableProcessors()+2);
	private static final int MAX_THREADS = (int)Math.min(Runtime.getRuntime().availableProcessors()*1.5+8, 32);
	private static final int MAX_QUEUE_SIZE = 10000;
	
	private ConnectionFactory factory = null;
	private Connection connection = null;
	
	private volatile boolean wasInitialized = false;
	private static AtomicBoolean loaded = new AtomicBoolean();
	private String exchangeName = ConfiguracaoIntegracaoCloud.getRabbitExchangeName();
	private String host = ConfiguracaoIntegracaoCloud.getRabbitHost();
	private String virtualHost = ConfiguracaoIntegracaoCloud.getRabbitVirtualHost();
	private String username = ConfiguracaoIntegracaoCloud.getRabbitUsername();
	private String password = ConfiguracaoIntegracaoCloud.getRabbitPassword();
	private Integer port = ConfiguracaoIntegracaoCloud.getRabbitPort();

	private String queueName = ConfiguracaoIntegracaoCloud.getRabbitQueueName();
	
	private ThreadPoolExecutor executor;
	
	public static RabbitMQClient instance() {
		return ComponentUtil.getComponent(RabbitMQClient.NAME);
	}
	
	@Logger
	private Log logger;
	
	@Create
	public void init() throws IOException, TimeoutException {
		if (loaded.getAndSet(true))
			throw new IllegalStateException(NAME + " já carregado!");
		
		this.wasInitialized = true;
		try {
			SeamLifecycleThreadFactory tf = new SeamLifecycleThreadFactory(NAME);
			this.executor = Executors.newScalableThreadPool(MIN_THREADS, MAX_THREADS, NAME, MAX_QUEUE_SIZE);
			this.executor.setThreadFactory(tf);
			
			factory = new ConnectionFactory();
			factory.setHost(this.host);
			factory.setPort(this.port);
			factory.setVirtualHost(this.virtualHost);
			factory.setUsername(this.username);
			factory.setPassword(this.password);
			factory.setAutomaticRecoveryEnabled(Boolean.TRUE);			
			factory.setSharedExecutor(this.executor);
			factory.setThreadFactory(tf);
			connection = factory.newConnection(this.executor);
		}catch (Exception e) {
			logger.error("Fail to connect with rabbit: " + e.getLocalizedMessage());
			e.printStackTrace();
			if(e instanceof IOException){
				throw new IOException(e.getLocalizedMessage(), e);
			}
		}
	}
	
	@Destroy
	public void destroy() {
		if (wasInitialized)
			loaded.set(false);
		
		if (connection != null) {
			try {
				connection.close(5000);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (this.executor!=null && !this.executor.isShutdown()) {
			this.executor.shutdown();
		}
	}
	
	public ISendMessage createMessageSender(boolean transactional) throws IOException, TimeoutException{
		final BasicProperties bp = new BasicProperties()
			.builder().contentType("application/json").build();
		
		if(connection == null) {
			this.init();
		}
		
		if(connection == null) 
			throw new IllegalStateException("A connection is required!");
		
		final String _exchangeName = this.exchangeName;
		final Channel channel = connection.createChannel();
		channel.exchangeDeclarePassive(_exchangeName);
		
		return new ISendMessage() {
			List<String> routingKeys = transactional ? new ArrayList<>() : null;
			private volatile boolean closed, enlisted;
			private volatile int sendMessageCounter, transactionCounter;
			
			public void setupTransaction() throws IOException {
				final UserTransaction trans = Transaction.instance();
				channel.txSelect();
				transactionCounter++;
				trans.registerSynchronization(new Synchronization() {

					@Override
					public void beforeCompletion() {
						try {
							if (!channel.isOpen() && !routingKeys.isEmpty()) {
								trans.setRollbackOnly();
							}
						} catch (Exception ex) {
							java.util.logging.Logger.getLogger(RabbitMQClient.class.getName()).log(Level.SEVERE, null, ex);
						}
					}

					@Override
					public void afterCompletion(int status) {
						try {
							try {
								if(status == Status.STATUS_COMMITTED) {
									//Como o registro AMQPEvent é apagado do banco de dados, o correto seria completar o envio das mensagens antes do commit e não depois...
									channel.txCommit();
									logger.info("RabbitMQ: Enviada mensagem. Routing Key: " + String.join(", ", routingKeys));
								} else {
									channel.txRollback();
								}
							} finally {
								enlisted = false;
								routingKeys = new ArrayList<>();
								if (closed) {
									channel.close();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				enlisted = true;
			}

			@Override
			public void sendMessage(String routingKey, byte[] message) throws IOException {
				if (transactional && !enlisted) {
					setupTransaction();
				}
				channel.basicPublish(_exchangeName, routingKey, bp, message);
				sendMessageCounter++;
				if (transactional) {
					routingKeys.add(routingKey);
				} else {
					logger.info("RabbitMQ: Enviada mensagem #" + sendMessageCounter + " Routing Key: " + routingKey);
				}
			}

			@Override
			public Channel getChannel() {
				return channel;
			}

			@Override
			public void close() throws IOException {
				closed = true;
				if (!enlisted) {
					try {
						channel.close();
					} catch (TimeoutException ex) {
						throw new IOException(ex);
					}
				}
			}
			
			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder("ISendMessage: ");
				sb.append("exchangeName: '").append(_exchangeName).append("'; ");
				sb.append("messsageCount: ").append(sendMessageCounter).append("; ");
				if (transactional) {
					sb.append("transactional; ");
					sb.append("transactionCount: ").append(transactionCounter).append("; ");
				}
				if (closed)
					sb.append("closed; ");
				if (channel!=null)
					sb.append("Channel #").append(channel.getChannelNumber()).append("; ");
				return sb.toString().trim();
			}
			
		};		
	}

	public ISendMessage createTransactionalMessageSenderSyncBeforeCommit() throws IOException, TimeoutException{
		final BasicProperties bp = new BasicProperties()
			.builder().contentType("application/json").build();
		
		if(connection == null) {
			this.init();
		}
		
		if(connection == null) 
			throw new IllegalStateException("A connection is required!");
		
		final String _exchangeName = this.exchangeName;
		final Channel channel = connection.createChannel();
		channel.exchangeDeclarePassive(_exchangeName);
		
		return new ISendMessage() {
			List<String> routingKeys = new ArrayList<>();
			private volatile boolean closed, enlisted;
			private volatile int sendMessageCounter, transactionCounter;
			
			public void setupTransaction() throws IOException {
				final UserTransaction trans = Transaction.instance();
				channel.txSelect();
				transactionCounter++;
				trans.registerSynchronization(new Synchronization() {

					@Override
					public void beforeCompletion() {
						try {
							try {
								int status = trans.getStatus();
								if (status==Status.STATUS_ACTIVE) {
									channel.txCommit();
									logger.info("RabbitMQ: Enviada mensagem. Routing Key: " + String.join(", ", routingKeys));
								}
							} catch (Exception ex) {
								trans.setRollbackOnly();
								throw ex;
							}
						} catch (Exception ex) {
							java.util.logging.Logger.getLogger(RabbitMQClient.class.getName()).log(Level.SEVERE, null, ex);
						} 
					}

					@Override
					public void afterCompletion(int status) {
						try {
							try {
								if(status != Status.STATUS_COMMITTED) {
									channel.txRollback();
								}
							} finally {
								enlisted = false;
								routingKeys = new ArrayList<>();
								if (closed) {
									channel.close();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				enlisted = true;
			}

			@Override
			public void sendMessage(String routingKey, byte[] message) throws IOException {
				if (!enlisted) {
					setupTransaction();
				}
				channel.basicPublish(_exchangeName, routingKey, bp, message);
				sendMessageCounter++;
				routingKeys.add(routingKey);
			}

			@Override
			public Channel getChannel() {
				return channel;
			}

			@Override
			public void close() throws IOException {
				closed = true;
				if (!enlisted) {
					try {
						channel.close();
					} catch (TimeoutException ex) {
						throw new IOException(ex);
					}
				}
			}
			
			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder("ISendMessage: ");
				sb.append("exchangeName: '").append(_exchangeName).append("'; ");
				sb.append("messsageCount: ").append(sendMessageCounter).append("; ");
				sb.append("transactional; ");
				sb.append("transactionCount: ").append(transactionCounter).append("; ");
				if (closed)
					sb.append("closed; ");
				if (channel!=null)
					sb.append("Channel #").append(channel.getChannelNumber()).append("; ");
				return sb.toString().trim();
			}
			
		};		
	}

	public void sendMessage(String routingKey, byte[] message, boolean transactional) throws IOException, TimeoutException{
		ISendMessage sender = null;
		try {
			sender = createMessageSender(transactional);
			sender.sendMessage(routingKey, message);
		} finally {
			if (sender!=null) {
				sender.close();
			}				
		}
	}

	public void sendMessage(String routingKey, byte[] message) throws IOException, TimeoutException {
		this.sendMessage(routingKey, message, true);
	}
	
	/**
	 * Uses
	 * 	- default exchange: pje.exchange
	 *  - default queuename: pje.legacy
	 *  - default routingKey: pje.#
	 * @return
	 * @throws IOException
	 */
	public Channel createGenericChannel() throws IOException{
		return this.createChannel(this.queueName, this.exchangeName, "pje.#");
	}
	
	/**
	 * Uses the default exchange: pje.exchange
	 * 
	 * @param queueName
	 * @param routingKey
	 * @return
	 * @throws IOException
	 */
	public Channel createChannel(String queueName, String routingKey) throws IOException{
		return this.createChannel(queueName, this.exchangeName, routingKey);
	}
	
	public Channel createChannel(String queueName, String exchange, String routingKey) throws IOException{
		Channel channel = connection.createChannel();
		channel.queueDeclare(queueName, true, false, false, null);
		channel.queueBind(queueName, this.exchangeName, routingKey);
		
		return channel;
	}
	
	public Channel createChannel() throws IOException {
		return this.connection.createChannel();
	}
	
	public static interface ISendMessage extends Closeable {
		void sendMessage(String routingKey, byte[] message) throws IOException;
		Channel getChannel();
	}
}
