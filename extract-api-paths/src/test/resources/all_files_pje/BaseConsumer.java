package br.jus.cnj.pje.amqp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import br.com.infox.cliente.util.CloudEventUtil;
import br.com.infox.cliente.util.JSONUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.nucleo.util.Crypto.SignatureAlgorithm;
import br.jus.pje.nucleo.util.Crypto.Type;
import br.jus.pje.nucleo.util.CryptoRSA;
import br.jus.pje.nucleo.util.StringUtil;

public abstract class BaseConsumer<T> implements Consumer {

	protected static final Logger logger = LoggerFactory.getLogger(BaseConsumer.class);

	private Channel channel;

	private ObjectMapper mapper = JSONUtil.novoObjectMapper();

	@Create
	public void init() throws IOException {
		if (isConsumerEnabled()) {
			logger.info("Starting consumer for queue: " + getQueueName());

			String consumerTag = CloudEventUtil.generateInstanceId() + ":" + UUID.randomUUID();

			try {
				this.channel = this.getClient().createChannel();
				this.channel.basicQos(getMaxDeliveryMessages(), true);
				this.channel.queueDeclare(this.getQueueName(), isQueueDurable(), isQueueExclusive(),
						isQueueAutoDelete(), getArguments());
				this.channel.queueBind(getQueueName(), getExchangeName(), getBindingRoutingKey());
				this.channel.basicConsume(this.getQueueName(), false, consumerTag, this);

				logger.info("Consumer for queue: " + getQueueName() + " was started");
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
				if (ConfiguracaoIntegracaoCloud.isRabbitConnectionRequired()) {
					logger.error("A conexão ao RabbitMQ é obrigatória");
					throw new IOException(e);
				}
			}
		} else {
			logger.info("Consumer for queue: " + getQueueName() + " is disabled");
		}
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties props, byte[] messageBody)
			throws IOException {
		CloudEvent messageObject = this.mapper.readValue(messageBody, CloudEvent.class);
		boolean ack = false;
		boolean wasRedelivered = envelope.isRedeliver();
		try {

			long deliveryTag = envelope.getDeliveryTag();

			if (this.channel == null || !this.channel.isOpen()) {
				throw new IOException("Channel is closed!!!");
			}
			startSeamContext();

			T payload = this.translatePayload(messageObject);
			ack = this.process(messageObject, payload, deliveryTag, getReadyMessagesCount(), getConsumersCount());

		} catch (Exception e) {
			this.error(new String(messageBody), e);
		} finally {
			if (ack) {
				this.channel.basicAck(envelope.getDeliveryTag(), false);
			} else {
				boolean requeue = !wasRedelivered; // just requeue a poisoned message that was not redelivered yet
				this.channel.basicNack(envelope.getDeliveryTag(), false, requeue);
			}
		}
	}

	@Override
	public void handleConsumeOk(String consumerTag) {
		logger.info("Doing nothing on: handleConsumeOk");
	}

	@Override
	public void handleCancelOk(String consumerTag) {
		logger.info("Doing nothing on: handleCancelOk");
	}

	@Override
	public void handleCancel(String consumerTag) throws IOException {
		logger.info("Doing nothing on: handleCancel");
	}

	@Override
	public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
		logger.info("Doing nothing on: handleShutdownSignal");
	}

	@Override
	public void handleRecoverOk(String consumerTag) {
		logger.info("Doing nothing on: handleRecoverOk");
	}

	protected RabbitMQClient getClient() {
		return RabbitMQClient.instance();
	}

	protected boolean isConsumerEnabled() {
		return ConfiguracaoIntegracaoCloud.isRabbitJobsConsumer();
	}

	protected abstract boolean process(CloudEvent messageObject, T payload, long deliveryTag, long readyCloudMessages,
			long consumerCount) throws Exception;

	protected void error(String json, Exception e) {
		logger.error("Fail to process item: " + json, e);
		logger.error(e.getLocalizedMessage());
		e.printStackTrace();
	}

	@SuppressWarnings("rawtypes")
	protected Class getEntity() {
		return CloudEvent.class;
	}

	protected abstract String getQueueName();

	protected abstract String getBindingRoutingKey();

	protected String getExchangeName() {
		return ConfiguracaoIntegracaoCloud.getRabbitExchangeName();
	}

	protected int getMaxDeliveryMessages() {
		return ConfiguracaoIntegracaoCloud.getRabbitConsumerMaxDeliveryMessages();
	}

	/**
	 * Default value is NULL, if a consumer implement a different value, it will be
	 * used to decode payloadHashSignature against this KEY and the value will be
	 * compared with payloadHash to check if the messages was sent from a trusted
	 * source
	 * 
	 * @return
	 */
	protected String getDecoderKey(CloudEvent messageBody) {
		String decoderKey = null;
		if (messageBody.getSignatureAlgorithm().equals(SignatureAlgorithm.RSA.toString())) {
			decoderKey = messageBody.getSignaturePublicKey();
		}
		return decoderKey;
	}

	protected boolean isQueueDurable() {
		return Boolean.TRUE;
	}

	protected boolean isQueueExclusive() {
		return Boolean.FALSE;
	}

	protected boolean isQueueAutoDelete() {
		return Boolean.FALSE;
	}

	protected void purgeQueue() throws IOException {
		logger.warn("Deletando " + getReadyMessagesCount() + " mensagens da fila " + getQueueName());

		this.channel.queuePurge(getQueueName());
	}

	protected Map<String, Object> getArguments() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("x-dead-letter-exchange", ConfiguracaoIntegracaoCloud.getRabbitDeadLetterExchangeName());
		return args;
	}

	@SuppressWarnings("unchecked")
	private T translatePayload(CloudEvent messageBody) throws PJeBusinessException, JsonProcessingException {
		T payloadCloudEvent = null;

		if (messageBody != null) {
			if (messageBody.getPayload() != null) {
				if (getDecoderKey(messageBody) != null) {
					String payloadHashDecoded = null;
					if (messageBody.getSignatureAlgorithm().equals(SignatureAlgorithm.DES.toString())) {
						Crypto crypto = new Crypto(getDecoderKey(messageBody));
						payloadHashDecoded = crypto.decodeDES(messageBody.getPayloadHashSigned());
					} else {
						if (messageBody.getSignatureAlgorithm().equals(SignatureAlgorithm.RSA.toString())) {
							CryptoRSA crypto = new CryptoRSA();
							crypto.initPublicKey(getDecoderKey(messageBody));
							payloadHashDecoded = crypto.unsign(messageBody.getPayloadHashSigned());
						}
					}
					if (!payloadHashDecoded.equals(messageBody.getPayloadHash())) {
						logger.error("Failed to check payload signature");
						throw new PJeBusinessException("CloudEvent Failed to check payload signature");
					}
				}
				payloadCloudEvent = (T) this.mapper.convertValue(messageBody.getPayload(), getEntity());

				String hashAlgorithm = messageBody.getHashAlgorithm();
				if (StringUtil.isEmpty(hashAlgorithm)) {
					hashAlgorithm = Type.MD5.toString(); // default: MD5
				}
				this.checkPayloadHash(messageBody.getPayloadHash(), payloadCloudEvent, hashAlgorithm);
			}
		} else {
			throw new PJeBusinessException("There is no arguments to translate CloudEvent correctly");
		}
		return payloadCloudEvent;
	}

	private boolean checkPayloadHash(String informedHash, T payloadCloudEvent, String hashAlgorithm)
			throws JsonProcessingException, PJeBusinessException {
		boolean ret = false;
		String calculatedHash = "-1";
		if (informedHash != null) {
			informedHash = informedHash.toUpperCase();
			calculatedHash = Crypto.encode(this.mapper.writeValueAsBytes(payloadCloudEvent), hashAlgorithm)
					.toUpperCase();
			ret = calculatedHash.equals(informedHash);
		}
		if (!ret) {
			logger.error("Failed to check payloadHash - received hash: [" + informedHash + "], calculated hash: ["
					+ calculatedHash + "]");
			throw new PJeBusinessException("CloudEvent Failed to check payloadHash - received hash: [" + informedHash
					+ "], calculated hash: [" + calculatedHash + "]");
		}
		return ret;

	}

	private long getReadyMessagesCount() {
		try {
			if (this.channel.isOpen()) {
				return this.channel.messageCount(getQueueName());
			}
		} catch (IOException e) {
			logger.error("Fail to get ready messages count", e);
		}
		return 0;
	}

	private long getConsumersCount() {
		try {
			if (this.channel.isOpen()) {
				return this.channel.consumerCount(getQueueName());
			}
		} catch (IOException e) {
			logger.error("Fail to get consumers count", e);
		}
		return 0;
	}

	private void startSeamContext() {
		if (!Contexts.isApplicationContextActive() || !Contexts.isEventContextActive()
				|| !Contexts.isSessionContextActive()) {
			Lifecycle.beginCall();
		}
	}

}
