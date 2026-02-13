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
import br.jus.cnj.pje.amqp.model.dto.jobs.SinalizaProcessosAguardandoAudienciaCloudEvent;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodicoComum;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.ProcessosAguardandoAudiencia;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;

@Name(SinalizaProcessosAguardandoAudienciaConsumer.NAME)
@Scope(ScopeType.APPLICATION)
@Install(dependencies = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap", ParametroUtil.NAME })
@Startup(depends = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap", ParametroUtil.NAME })
public class SinalizaProcessosAguardandoAudienciaConsumer
		extends BaseConsumer<SinalizaProcessosAguardandoAudienciaCloudEvent> {

	public static final String NAME = "sinalizaProcessosAguardandoAudienciaConsumer";

	private ProcessosAguardandoAudiencia processosAguardandoAudiencia;

	private ProcessosAguardandoAudiencia getProcessosAguardandoAudiencia() {
		if (processosAguardandoAudiencia == null) {
			processosAguardandoAudiencia = (ProcessosAguardandoAudiencia) Component
					.getInstance(ProcessosAguardandoAudiencia.NAME);
		}

		return processosAguardandoAudiencia;
	}

	@Override
	protected boolean process(CloudEvent messageObject, SinalizaProcessosAguardandoAudienciaCloudEvent payload,
			long deliveryTag, long readyCloudMessages, long consumerCount) throws Exception {
		Integer numJob = payload.getBulkIdentification().getNumJob();

		Integer tamanhoJob = payload.getIdsAudiencia() == null ? 0 : payload.getIdsAudiencia().size();

		List<Integer> idsAudiencia = payload.getIdsAudiencia();

		String lote = payload.getBulkIdentification().getUuidLote();

		boolean resultado = false;

		try {
			Integer totalProcessado = 0;

			try {
				List<Integer> audienciasProcessadas = getProcessosAguardandoAudiencia().runLocal(idsAudiencia, true);

				totalProcessado += audienciasProcessadas.size();

				if (audienciasProcessadas == null || audienciasProcessadas.isEmpty()
						|| audienciasProcessadas.size() != idsAudiencia.size()) {
					if (audienciasProcessadas != null && audienciasProcessadas.isEmpty() == false) {
						idsAudiencia.removeAll(audienciasProcessadas);
					}

					audienciasProcessadas = getProcessosAguardandoAudiencia().runLocal(idsAudiencia, false);

					totalProcessado += audienciasProcessadas.size();
				}
			} catch (Exception e) {
				logger.error("Erro ao processar item 'sinalizaProcessosAguardandoAudiencia'. [idsAudiencia: "
						+ idsAudiencia + "].");
			}

			resultado = true;

			VerificadorPeriodicoComum verificadorPeriodicoComum = ComponentUtil
					.getComponent(VerificadorPeriodicoComum.class);

			verificadorPeriodicoComum.atualizaProcessamentoLote(payload.getBulkIdentification().getDataJob(), numJob,
					lote, true, totalProcessado, tamanhoJob);
		} catch (Exception e) {
			logger.error("Erro ao processar item 'sinalizaProcessosAguardandoAudiencia'. [idsAudiencia: " + idsAudiencia
					+ "]. Erro: " + e.getLocalizedMessage());
		}

		return resultado;
	}

	@Override
	protected String getQueueName() {
		return CloudEventUtil.generateQueueNameFromSuffix("sinalizaProcessosAguardandoAudiencia.queue");
	}

	@Override
	protected String getBindingRoutingKey() {
		return CloudEventUtil.generateRoutingKeyFromSuffix("SinalizaProcessosAguardandoAudienciaCloudEvent.POST");
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
		return SinalizaProcessosAguardandoAudienciaCloudEvent.class;
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