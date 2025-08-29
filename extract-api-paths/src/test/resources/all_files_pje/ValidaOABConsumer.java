package br.jus.cnj.pje.amqp.consumers;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

import br.com.infox.cliente.util.CloudEventUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.pje.processor.ValidarOABProcessor;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.amqp.BaseConsumer;
import br.jus.cnj.pje.amqp.model.dto.ValidaOABCloudEvent;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;

@Name(ValidaOABConsumer.NAME)
@Scope(ScopeType.APPLICATION)
@Install(dependencies = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap", ParametroUtil.NAME })
@Startup(depends = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap", ParametroUtil.NAME })
public class ValidaOABConsumer extends BaseConsumer<ValidaOABCloudEvent> {

	public static final String NAME = "validaOABConsumer";

	@Override
	protected boolean process(CloudEvent messageObject, ValidaOABCloudEvent payload, long deliveryTag,
			long readyCloudMessages, long consumerCount) throws Exception {
		ValidarOABProcessor validarOABProcessor = ComponentUtil.getComponent(ValidarOABProcessor.class);

		List<String> documentos = payload.getDocumentos();

		boolean resultado = false;

		try {
			try {
				validarOABProcessor.validarOab(documentos);
			} catch (Exception e) {
				logger.error("Erro ao processar item 'validarOab'. [documentos: " + documentos + "].");
			}

			resultado = true;
		} catch (Exception e) {
			logger.error("Erro ao processar item 'validarOab'. [documentos: " + documentos + "]. Erro: "
					+ e.getLocalizedMessage());
		}

		return resultado;
	}

	@Override
	protected String getQueueName() {
		return CloudEventUtil.generateQueueNameFromSuffix("validaOAB.queue");
	}

	@Override
	protected String getBindingRoutingKey() {
		return CloudEventUtil.generateRoutingKeyFromSuffix("ValidaOABCloudEvent.POST");
	}

	@Override
	protected int getMaxDeliveryMessages() {
		return ConfiguracaoIntegracaoCloud.getRabbitConsumerMaxDeliveryMessages();
	}

	@Override
	protected boolean isConsumerEnabled() {
		return ConfiguracaoIntegracaoCloud.isRabbitJobsConsumer();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class getEntity() {
		return ValidaOABCloudEvent.class;
	}

	protected String getDecoderKey(CloudEvent messageBody) {
		return ProjetoUtil.getChaveCriptografica();
	}
}