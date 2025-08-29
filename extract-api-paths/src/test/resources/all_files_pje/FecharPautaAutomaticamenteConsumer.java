package br.jus.cnj.pje.amqp.consumers;

import java.io.IOException;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

import br.com.infox.cliente.util.CloudEventUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.amqp.BaseConsumer;
import br.jus.cnj.pje.amqp.model.dto.jobs.FecharPautaAutomaticamenteCloudEvent;
import br.jus.cnj.pje.amqp.model.dto.jobs.ProsseguimentoSemPrazoCloudEvent;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodicoComum;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.FecharPautaAutomaticamente;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;

@Name(FecharPautaAutomaticamenteConsumer.NAME)
@Scope(ScopeType.APPLICATION)
@Install(dependencies = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap", ParametroUtil.NAME })
@Startup(depends = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap", ParametroUtil.NAME })
public class FecharPautaAutomaticamenteConsumer extends BaseConsumer<FecharPautaAutomaticamenteCloudEvent> {

	public static final String NAME = "fecharPautaAutomaticamenteConsumer";

	private FecharPautaAutomaticamente fecharPautaAutomaticamente;

	private FecharPautaAutomaticamente getFecharPautaAutomaticamente() {
		if (fecharPautaAutomaticamente == null) {
			fecharPautaAutomaticamente = (FecharPautaAutomaticamente) Component
					.getInstance(FecharPautaAutomaticamente.NAME);
		}

		return fecharPautaAutomaticamente;
	}

	@Override
	protected boolean process(CloudEvent messageObject, FecharPautaAutomaticamenteCloudEvent payload, long deliveryTag,
			long readyCloudMessages, long consumerCount) throws Exception {
		Integer numJob = payload.getBulkIdentification().getNumJob();

		Integer tamanhoJob = payload.getIdsSessoes() == null ? 0 : payload.getIdsSessoes().size();

		List<Integer> idsSessoes = payload.getIdsSessoes();

		String lote = payload.getBulkIdentification().getUuidLote();

		boolean resultado = false;

		try {
			Integer totalProcessado = 0;

			try {
				List<Integer> sessoesProcessadas = getFecharPautaAutomaticamente().runLocal(idsSessoes, true);

				totalProcessado += sessoesProcessadas.size();

				if (sessoesProcessadas == null || sessoesProcessadas.isEmpty()
						|| sessoesProcessadas.size() != idsSessoes.size()) {
					if (sessoesProcessadas != null && sessoesProcessadas.isEmpty() == false) {
						idsSessoes.removeAll(sessoesProcessadas);
					}

					sessoesProcessadas = getFecharPautaAutomaticamente().runLocal(idsSessoes, false);

					totalProcessado += sessoesProcessadas.size();
				}
			} catch (Exception e) {
				logger.error("Erro ao processar item 'fecharPautaAutomaticamente'. [idsSessoes: "
						+ payload.getIdsSessoes() + "].");
			}

			resultado = true;

			VerificadorPeriodicoComum verificadorPeriodicoComum = ComponentUtil
					.getComponent(VerificadorPeriodicoComum.class);

			verificadorPeriodicoComum.atualizaProcessamentoLote(payload.getBulkIdentification().getDataJob(), numJob,
					lote, true, totalProcessado, tamanhoJob);
		} catch (Exception e) {
			logger.error("Erro ao processar item 'fecharPautaAutomaticamente'. [idsSessoes: " + payload.getIdsSessoes()
					+ "]. Erro: " + e.getLocalizedMessage());
		}

		return resultado;
	}

	@Override
	protected String getQueueName() {
		return CloudEventUtil.generateQueueNameFromSuffix("fecharPautaAutomaticamente.queue");
	}

	@Override
	protected String getBindingRoutingKey() {
		return CloudEventUtil.generateRoutingKeyFromSuffix("FecharPautaAutomaticamenteCloudEvent.POST");
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
		return ProsseguimentoSemPrazoCloudEvent.class;
	}

	@Override
	protected String getDecoderKey(CloudEvent messageBody) {
		return ProjetoUtil.getChaveCriptografica();
	}

	public void purgeQueue() {
		try {
			super.purgeQueue();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
}