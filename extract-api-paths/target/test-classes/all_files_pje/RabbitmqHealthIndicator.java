package br.jus.cnj.pje.status;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.amqp.RabbitMQClient;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;

public class RabbitmqHealthIndicator extends AbstractHealthIndicator{

	@Override
	public Health doHealthCheck() {
		try {
			RabbitMQClient amqpClient = ComponentUtil.getComponent(RabbitMQClient.NAME);

			amqpClient.sendMessage("test", "test".getBytes(), false);
			this.getDetails().put("success", "Mensagem enviada ao broker com sucesso.");
			this.getDetails().put("url", ConfiguracaoIntegracaoCloud.getRabbitHost());
			this.getDetails().put("port", ConfiguracaoIntegracaoCloud.getRabbitPort());
			this.setHealth(new Health(Status.UP, this.getDetails()));
			
		} catch (IOException | TimeoutException e) {
			this.getDetails().put("error", e.getCause().getLocalizedMessage());
			this.getDetails().put("url", ConfiguracaoIntegracaoCloud.getRabbitHost());
			this.getDetails().put("port", ConfiguracaoIntegracaoCloud.getRabbitPort());
			this.setHealth(new Health(Status.DOWN, this.getDetails()));
		} catch (Exception e) {
			this.getDetails().put("error", e.getCause().getCause().getLocalizedMessage());
			this.getDetails().put("url", ConfiguracaoIntegracaoCloud.getRabbitHost());
			this.getDetails().put("port", ConfiguracaoIntegracaoCloud.getRabbitPort());
			this.setHealth(new Health(Status.DOWN, this.getDetails()));
		}
		
		return this.getHealth();
	}

}
