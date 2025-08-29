/**
 * RabbitMQTest.java
 *
 * Data: 11/10/2019
 */
package br.jus.cnj.pje.amqp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * Classe de testes de integração com o RabbitMQ.
 * 
 * @author Adriano Pamplona
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RabbitMQTest {

	@Test
	public void testEnviarMensagem() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername("guest");
		factory.setPassword("guest");
		factory.setHost("localhost");

		try (Connection connection = factory.newConnection()) {
			Channel channel = connection.createChannel();
			channel.exchangeDeclarePassive("pje.exchange");
			Map<String, Object> argumentos = new HashMap<>();
			argumentos.put("x-queue-type", "classic");
			
			//channel.queueDeclare("pje.legacy", true, false, false, argumentos);
			BasicProperties bp = new BasicProperties();
			bp = bp.builder().contentType("application/json").build();
			String mensagem = "Olá Mundo!";
			channel.basicPublish("pje.exchange", "401.1.pje-legacy.ProcessoEvento.POST", bp, mensagem.getBytes("UTF-8"));
		}
	}

	@Test
	public void testConsumirMensagem() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername("guest");
		factory.setPassword("guest");
		factory.setHost("localhost");

		try (Connection connection = factory.newConnection()) {
			Channel channel = connection.createChannel();
			Map<String, Object> argumentos = new HashMap<>();
			argumentos.put("x-queue-type", "classic");
			
			channel.exchangeDeclarePassive("pje.exchange");
			//channel.queueDeclare("pje.legacy", true, false, false, argumentos);

			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					String mensagem = new String(body, "UTF-8");
					System.out.println("Mensagem recebida: " + mensagem);
				}
			};
			channel.basicConsume("pje.legacy", true, consumer);
		}
		
//		Scanner scanner = new Scanner(System.in);
//		String username = "";
//		while (!username.equalsIgnoreCase("q")) {
//			System.out.println("Comandos [q=quit]");
//			username = scanner.next();
//		}
//		System.out.println("Fim.");
	}
}
