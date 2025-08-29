package br.jus.cnj.pje.amqp.consumers;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

import br.com.infox.cliente.util.CloudEventUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.util.GerenciadorAutoridadesCertificadoras;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.amqp.BaseConsumer;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.cnj.pje.pjecommons.model.services.autoridadescertificadoras.MetaDadosConjuntoACs;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;

@Name(AutoridadesCertificadorasConsumer.NAME)
@Scope(ScopeType.APPLICATION)
@Install(dependencies =  {
		PjeEurekaRegister.NAME,
		"org.jboss.seam.resteasy.bootstrap",
		ParametroUtil.NAME
		})
@Startup(depends = {
		PjeEurekaRegister.NAME,
		"org.jboss.seam.resteasy.bootstrap",
		ParametroUtil.NAME
		})
public class AutoridadesCertificadorasConsumer extends BaseConsumer<MetaDadosConjuntoACs>{
	
	public static final String NAME = "autoridadesCertificadorasConsumer";

	@Override
	protected boolean process(CloudEvent messageObject, MetaDadosConjuntoACs payload, long deliveryTag,
			long readyCloudMessages, long consumerCount) throws Exception {

		if(payload.getAtivo()) {
			GerenciadorAutoridadesCertificadoras gerenciadorAutoridadesCertificadoras = ComponentUtil.getComponent(GerenciadorAutoridadesCertificadoras.class);
			gerenciadorAutoridadesCertificadoras.validarAtualizacaoAutoridadesCertificadoras(payload);
		}

        return true;
	}

	@Override
	protected String getQueueName() {
		return CloudEventUtil.generateInstanceOnlyQueueNameFromSuffix("autoridadesCertificadoras.queue");
	}

	@Override
	protected String getBindingRoutingKey() {
		return "*.*.autoridades-certificadoras.AutoridadesCertificadoras.POST";
	}

	@Override
	protected int getMaxDeliveryMessages() {
		return ConfiguracaoIntegracaoCloud.getRabbitConsumerMaxDeliveryMessages();
	}
	
	@Override
	protected boolean isConsumerEnabled() {
		return ConfiguracaoIntegracaoCloud.isRabbitAutoridadesCertificadorasConsumer();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class getEntity() {
		return MetaDadosConjuntoACs.class;
	}
	
	@Override
	protected String getDecoderKey(CloudEvent messageBody) {
		return null;
	}
	@Override
	protected boolean isQueueAutoDelete() {
		return Boolean.TRUE;
	}
}
