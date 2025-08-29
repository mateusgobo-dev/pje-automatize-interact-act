package br.jus.cnj.pje.amqp.consumers;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.amqp.BaseConsumer;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;

@Name(MovimentacaoProcessualConsumer.NAME)
@Scope(ScopeType.APPLICATION)
@Install(dependencies = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap",
		ParametroUtil.NAME }, value = false)
@Startup(depends = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap", ParametroUtil.NAME })
public class MovimentacaoProcessualConsumer extends BaseConsumer<CloudEvent> {

	public static final String NAME = "movimentacaoProcessualConsumer";

	@Override
	protected boolean process(CloudEvent messageObject, CloudEvent payload, long deliveryTag, long readyCloudMessages,
			long consumerCount) throws Exception {
		System.out.println("MESSAGE RECEIVED: " + messageObject.getPayload());

		return true;
	}

	@Override
	protected String getQueueName() {

		return "MovimentoDummy";
	}

	@Override
	protected String getBindingRoutingKey() {
		return "#";
	}

	@Override
	protected boolean isConsumerEnabled() {
		return false;
	}
}