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
import br.jus.cnj.pje.amqp.model.dto.jobs.EncerramentoPrazoNaoProcessualCloudEvent;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodicoComum;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificarDiarioPrazosExpNaoProc;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;

@Name(EncerramentoPrazoNaoProcessualConsumer.NAME)
@Scope(ScopeType.APPLICATION)
@Install(dependencies = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap", ParametroUtil.NAME })
@Startup(depends = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap", ParametroUtil.NAME })
public class EncerramentoPrazoNaoProcessualConsumer extends BaseConsumer<EncerramentoPrazoNaoProcessualCloudEvent> {

	public static final String NAME = "encerramentoPrazoNaoProcessualConsumer";

	private VerificarDiarioPrazosExpNaoProc verificarDiarioPrazosExpNaoProc;

	private VerificarDiarioPrazosExpNaoProc getVerificarDiarioPrazosExpNaoProc() {
		if (verificarDiarioPrazosExpNaoProc == null) {
			verificarDiarioPrazosExpNaoProc = (VerificarDiarioPrazosExpNaoProc) Component
					.getInstance(VerificarDiarioPrazosExpNaoProc.NAME);
		}

		return verificarDiarioPrazosExpNaoProc;
	}

	@Override
	protected boolean process(CloudEvent messageObject, EncerramentoPrazoNaoProcessualCloudEvent payload,
			long deliveryTag, long readyCloudMessages, long consumerCount) throws Exception {
		Integer numJob = payload.getBulkIdentification().getNumJob();

		Integer tamanhoJob = payload.getIdsProcesso() == null ? 0 : payload.getIdsProcesso().size();

		List<Integer> idsProcesso = payload.getIdsProcesso();

		String lote = payload.getBulkIdentification().getUuidLote();

		boolean resultado = false;

		try {
			Integer totalProcessado = 0;

			try {
				List<Integer> processosProcessados = getVerificarDiarioPrazosExpNaoProc()
						.encerramentoPrazoNaoProcessualLocal(idsProcesso, true);

				totalProcessado += processosProcessados.size();

				if (processosProcessados == null || processosProcessados.isEmpty()
						|| processosProcessados.size() != idsProcesso.size()) {
					if (processosProcessados != null && processosProcessados.isEmpty() == false) {
						idsProcesso.removeAll(processosProcessados);
					}

					processosProcessados = getVerificarDiarioPrazosExpNaoProc()
							.encerramentoPrazoNaoProcessualLocal(idsProcesso, false);

					totalProcessado += processosProcessados.size();
				}
			} catch (Exception e) {
				logger.error("Erro ao processar item 'encerramentoPrazoNaoProcessualLocal'. [idsProcesso: "
						+ payload.getIdsProcesso() + "].");
			}

			resultado = true;

			VerificadorPeriodicoComum verificadorPeriodicoComum = ComponentUtil
					.getComponent(VerificadorPeriodicoComum.class);

			verificadorPeriodicoComum.atualizaProcessamentoLote(payload.getBulkIdentification().getDataJob(), numJob,
					lote, true, totalProcessado, tamanhoJob);
		} catch (Exception e) {
			logger.error("Erro ao processar item 'encerramentoPrazoNaoProcessualLocal'. [idsProcesso: "
					+ payload.getIdsProcesso() + "]. Erro: " + e.getLocalizedMessage());
		}

		return resultado;
	}

	@Override
	protected String getQueueName() {
		return CloudEventUtil.generateQueueNameFromSuffix("encerramentoPrazoNaoProcessual.queue");
	}

	@Override
	protected String getBindingRoutingKey() {
		return CloudEventUtil.generateRoutingKeyFromSuffix("EncerramentoPrazoNaoProcessualCloudEvent.POST");
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
		return EncerramentoPrazoNaoProcessualCloudEvent.class;
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