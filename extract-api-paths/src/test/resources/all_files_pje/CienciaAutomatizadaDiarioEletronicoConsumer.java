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
import br.jus.cnj.pje.amqp.model.dto.jobs.CienciaAutomatizadaDiarioEletronicoCloudEvent;
import br.jus.cnj.pje.controleprazos.CodigosMateriaExpediente;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodicoComum;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.CienciaAutomatizadaDiarioEletronico;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;
import br.jus.pje.nucleo.enums.TipoPesquisaDJEEnum;

@Name(CienciaAutomatizadaDiarioEletronicoConsumer.NAME)
@Scope(ScopeType.APPLICATION)
@Install(dependencies = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap", ParametroUtil.NAME })
@Startup(depends = { PjeEurekaRegister.NAME, "org.jboss.seam.resteasy.bootstrap", ParametroUtil.NAME })
public class CienciaAutomatizadaDiarioEletronicoConsumer
		extends BaseConsumer<CienciaAutomatizadaDiarioEletronicoCloudEvent> {

	public static final String NAME = "cienciaAutomatizadaDiarioEletronicoConsumer";

	private CienciaAutomatizadaDiarioEletronico cienciaAutomatizadaDiarioEletronico;

	private CienciaAutomatizadaDiarioEletronico getCienciaAutomatizadaDiarioEletronico() {
		if (cienciaAutomatizadaDiarioEletronico == null) {
			cienciaAutomatizadaDiarioEletronico = (CienciaAutomatizadaDiarioEletronico) Component
					.getInstance(CienciaAutomatizadaDiarioEletronico.NAME);
		}

		return cienciaAutomatizadaDiarioEletronico;
	}

	@Override
	protected boolean process(CloudEvent messageObject, CienciaAutomatizadaDiarioEletronicoCloudEvent payload,
			long deliveryTag, long readyCloudMessages, long consumerCount) throws Exception {
		Integer numJob = payload.getBulkIdentification().getNumJob();

		Integer tamanhoJob = payload.getIdsExpedientes() == null ? 0 : payload.getIdsExpedientes().size();

		List<Integer> idsExpedientes = payload.getIdsExpedientes();

		String lote = payload.getBulkIdentification().getUuidLote();

		boolean resultado = false;

		try {
			Integer totalProcessado = 0;

			if (payload.getTipoPesquisa() == TipoPesquisaDJEEnum.DATA) {
				try {
					List<Integer> expedientesProcessados = getCienciaAutomatizadaDiarioEletronico()
							.atualizarMateriasPublicadasDJELocal(idsExpedientes, payload.getDtPesquisa(), null);

					totalProcessado += expedientesProcessados.size();
				} catch (Exception e) {
					StringBuilder mensagem = new StringBuilder();

					mensagem.append("Erro ao processar item 'cienciaAutomatizadaDiarioEletronico'. ");
					mensagem.append("[Tipo pesquisa: " + payload.getTipoPesquisa().toString() + "] ");
					mensagem.append("[idsProcessoExpediente: " + idsExpedientes + "].");

					logger.error(mensagem.toString());
				}
			} else {
				List<CodigosMateriaExpediente> codigosMateria = payload.getCodigosMateriaExpedientes();

				for (Integer idProcessoExpediente : idsExpedientes) {
					List<String> codigos = codigosMateria.stream()
							.filter(r -> r.getIdExpediente().equals(idProcessoExpediente))
							.map(CodigosMateriaExpediente::getCodigosMateria).findFirst().orElse(null);

					try {
						List<Integer> expedientesProcessados = getCienciaAutomatizadaDiarioEletronico()
								.pesquisaPublicacoesDJEPorMateriaLocal(idProcessoExpediente, codigos, null);

						totalProcessado += expedientesProcessados.size();
					} catch (Exception e) {
						StringBuilder mensagem = new StringBuilder();

						mensagem.append("Erro ao processar item 'cienciaAutomatizadaDiarioEletronico'. ");
						mensagem.append("[Tipo pesquisa: " + payload.getTipoPesquisa().toString() + "] ");
						mensagem.append("[idsProcessoParteExpediente: " + idsExpedientes + "].");

						logger.error(mensagem.toString());
					}
				}
			}

			resultado = true;

			VerificadorPeriodicoComum verificadorPeriodicoComum = ComponentUtil
					.getComponent(VerificadorPeriodicoComum.class);

			verificadorPeriodicoComum.atualizaProcessamentoLote(payload.getBulkIdentification().getDataJob(), numJob,
					lote, true, totalProcessado, tamanhoJob);
		} catch (Exception e) {
			StringBuilder mensagem = new StringBuilder();

			mensagem.append("Erro ao processar item 'cienciaAutomatizadaDiarioEletronico'. ");
			mensagem.append("[Tipo pesquisa: " + payload.getTipoPesquisa().toString() + "] ");

			if (payload.getTipoPesquisa() == TipoPesquisaDJEEnum.MATERIA) {
				mensagem.append("[idsProcessoExpediente: " + idsExpedientes + "] ");
			} else {
				mensagem.append("[idsProcessoParteExpediente: " + idsExpedientes + "] ");
			}

			mensagem.append("- Erro: " + e.getLocalizedMessage());

			logger.error(mensagem.toString());
		}

		return resultado;
	}

	@Override
	protected String getQueueName() {
		return CloudEventUtil.generateQueueNameFromSuffix("cienciaAutomatizadaDiarioEletronico.queue");
	}

	@Override
	protected String getBindingRoutingKey() {
		return CloudEventUtil.generateRoutingKeyFromSuffix("CienciaAutomatizadaDiarioEletronicoCloudEvent.POST");
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
		return CienciaAutomatizadaDiarioEletronicoCloudEvent.class;
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