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
import br.jus.cnj.pje.amqp.model.dto.jobs.ProsseguimentoSemPrazoCloudEvent;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodicoComum;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.ProsseguimentoSemPrazo;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;

@Name(ProsseguimentoSemPrazoConsumer.NAME)
@Scope(ScopeType.APPLICATION)
@Install(dependencies = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap", ParametroUtil.NAME })
@Startup(depends = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap", ParametroUtil.NAME })
public class ProsseguimentoSemPrazoConsumer extends BaseConsumer<ProsseguimentoSemPrazoCloudEvent> {

	public static final String NAME = "prosseguimentoSemPrazoConsumer";

	private ProsseguimentoSemPrazo prosseguimentoSemPrazo;

	private ProsseguimentoSemPrazo getProsseguimentoSemPrazo() {
		if (prosseguimentoSemPrazo == null) {
			prosseguimentoSemPrazo = (ProsseguimentoSemPrazo) Component.getInstance(ProsseguimentoSemPrazo.NAME);
		}

		return prosseguimentoSemPrazo;
	}

	@Override
	protected boolean process(CloudEvent messageObject, ProsseguimentoSemPrazoCloudEvent payload, long deliveryTag,
			long readyCloudMessages, long consumerCount) throws Exception {
		Integer numJob = payload.getBulkIdentification().getNumJob();

		Integer tamanhoJob = payload.getIdsProcessoParteExpediente() == null ? 0
				: payload.getIdsProcessoParteExpediente().size();

		List<Integer> idsProcessoParteExpediente = payload.getIdsProcessoParteExpediente();

		String lote = payload.getBulkIdentification().getUuidLote();

		boolean resultado = false;

		try {
			Integer totalProcessado = 0;

			try {
				List<Integer> expedientesProcessados = getProsseguimentoSemPrazo().runLocal(idsProcessoParteExpediente,
						true);

				totalProcessado += expedientesProcessados.size();

				if (expedientesProcessados == null || expedientesProcessados.isEmpty()
						|| expedientesProcessados.size() != idsProcessoParteExpediente.size()) {
					if (expedientesProcessados != null && expedientesProcessados.isEmpty() == false) {
						idsProcessoParteExpediente.removeAll(expedientesProcessados);
					}

					expedientesProcessados = getProsseguimentoSemPrazo().runLocal(idsProcessoParteExpediente, false);

					totalProcessado += expedientesProcessados.size();
				}
			} catch (Exception e) {
				logger.error("Erro ao processar item 'sinalizaProsseguimentoSemPrazo'. [idsProcessoParteExpediente: "
						+ payload.getIdsProcessoParteExpediente() + "].");
			}

			resultado = true;

			VerificadorPeriodicoComum verificadorPeriodicoComum = ComponentUtil
					.getComponent(VerificadorPeriodicoComum.class);

			verificadorPeriodicoComum.atualizaProcessamentoLote(payload.getBulkIdentification().getDataJob(), numJob,
					lote, true, totalProcessado, tamanhoJob);
		} catch (Exception e) {
			logger.error("Erro ao processar item 'sinalizaProsseguimentoSemPrazo'. [idsProcessoParteExpediente: "
					+ payload.getIdsProcessoParteExpediente() + "]. Erro: " + e.getLocalizedMessage());
		}

		return resultado;
	}

	@Override
	protected String getQueueName() {
		return CloudEventUtil.generateQueueNameFromSuffix("prosseguimentoSemPrazo.queue");
	}

	@Override
	protected String getBindingRoutingKey() {
		return CloudEventUtil.generateRoutingKeyFromSuffix("ProsseguimentoSemPrazoCloudEvent.POST");
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