package br.jus.cnj.pje.controleprazos.verificadorperiodico.passos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.infox.pje.manager.ProcessoAudienciaManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.amqp.consumers.SinalizaProcessosAguardandoAudienciaConsumer;
import br.jus.cnj.pje.amqp.model.dto.jobs.SinalizaProcessosAguardandoAudienciaCloudEvent;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodicoComum;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.cnj.pje.util.ControleTransactional;
import br.jus.pje.nucleo.entidades.AMQPEvent;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.VerificadorPeriodicoLote;
import br.jus.pje.nucleo.enums.VerificadorPeriodicoPassosEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(ProcessosAguardandoAudiencia.NAME)
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class ProcessosAguardandoAudiencia {

	public static final String NAME = "processosAguardandoAudiencia";

	@Logger
	private Log log;

	@In(create = true, required = true)
	private ProcessoJudicialService processoJudicialService;

	@In(create = true, required = true)
	private VerificadorPeriodicoComum verificadorPeriodicoComum;

	@In(create = true, required = true)
	private SinalizaProcessosAguardandoAudienciaConsumer sinalizaProcessosAguardandoAudienciaConsumer;

	public VerificadorPeriodicoLote run() {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.SINALIZA_PROCESSOS_AGUARDANDO_AUDIENCIA;

		log.info("Localizando processos aguardando audiência para movimentá-los.");

		VerificadorPeriodicoLote verificadorPeriodicoLote = verificadorPeriodicoComum.insereRegistroRelatorio(passo);

		try {
			Integer numeroLimitadorExpedientesPendentes = verificadorPeriodicoComum.getNumeroLimitador(passo);

			if (numeroLimitadorExpedientesPendentes <= 0) {
				log.info("Limitando os expedientes em '" + passo.getLabel() + "' para '"
						+ numeroLimitadorExpedientesPendentes + "' itens a serem processados!");
			} else {
				Date dataAtual = DateUtil.getDataSemHora(
						((DateService) ComponentUtil.getComponent(DateService.class)).getDataHoraAtual());

				ProcessoAudienciaManager pam = ComponentUtil.getComponent(ProcessoAudienciaManager.class);

				List<Integer> audienciaIds = pam.findIdsByDate(dataAtual);

				if (ConfiguracaoIntegracaoCloud.isRabbitJobDJEEnabled()) {
					this.runComControleDeLote(audienciaIds);
				} else {
					this.runLocal(audienciaIds, true);
				}
			}

			verificadorPeriodicoLote.setProcessado(true);
		} catch (Exception e) {
			verificadorPeriodicoLote.setProcessado(false);

			String msg = String.format("Erro ao sinalizar processos com audiências designadas: [%s].",
					e.getLocalizedMessage());

			log.error(msg);

			log.error(e);
		}

		verificadorPeriodicoLote = verificadorPeriodicoComum.atualizaRegistroRelatorio(verificadorPeriodicoLote);

		log.info("Verificação de processos aguardando audiência finalizada.");

		return verificadorPeriodicoLote;
	}

	public List<Integer> runLocal(List<Integer> idsProcessoAudiencia, boolean processamentoBatch)
			throws PJeBusinessException {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.SINALIZA_PROCESSOS_AGUARDANDO_AUDIENCIA;

		int cont = 0;

		List<Integer> audienciasProcessadas = new ArrayList<>();
		List<Integer> audienciasProcessadosTmp = new ArrayList<>();

		ProcessoAudienciaManager pam = ComponentUtil.getComponent(ProcessoAudienciaManager.class);

		ControleTransactional.beginTransactionAndClearJbpm();

		for (Integer idProcessoAudiencia : idsProcessoAudiencia) {
			log.info("Processando item em '" + passo.getLabel() + "'. [idAudiencia: " + idProcessoAudiencia + "] ["
					+ ++cont + "/" + idsProcessoAudiencia.size() + "]");

			try {
				ProcessoAudiencia processoAudiencia = pam.findById(idProcessoAudiencia);

				processoJudicialService.sinalizaDataAudiencia(processoAudiencia.getProcessoTrf());

				audienciasProcessadosTmp.add(idProcessoAudiencia);

				if (ControleTransactional.verificarNecessidadeDeCommitAndClearJbpm(cont, processamentoBatch)) {
					audienciasProcessadas.addAll(audienciasProcessadosTmp.stream().collect(Collectors.toList()));

					audienciasProcessadosTmp.clear();
				}
			} catch (PJeBusinessException e) {
				ControleTransactional.rollbackTransaction();

				audienciasProcessadosTmp.clear();

				String msg = String.format("Erro ao sinalizar processos com audiências designadas: [%s].",
						e.getLocalizedMessage());

				log.error(msg);

				log.error(e);

				ControleTransactional.beginTransaction();
			}
		}

		ControleTransactional.commitTransactionAndFlushAndClear();

		if (audienciasProcessadosTmp != null && audienciasProcessadosTmp.size() > 0) {
			audienciasProcessadas.addAll(audienciasProcessadosTmp.stream().collect(Collectors.toList()));
		}

		return audienciasProcessadas;
	}

	private void runComControleDeLote(List<Integer> idsAudiencia) throws PJeBusinessException {
		sinalizaProcessosAguardandoAudienciaConsumer.purgeQueue();

		String passo = VerificadorPeriodicoPassosEnum.SINALIZA_PROCESSOS_AGUARDANDO_AUDIENCIA.getLabel();

		AMQPEventManager amqpManager = AMQPEventManager.instance();

		List<AMQPEvent> amqpEvents = new ArrayList<AMQPEvent>();

		UUID uuidLote = UUID.randomUUID();

		String uuidLoteString = uuidLote.toString();

		Integer tamanhoParticaoLote = ConfiguracaoIntegracaoCloud.getRabbitTamanhoParticaoLote();

		Collection<List<Integer>> idsAudienciaParticionado = verificadorPeriodicoComum
				.partitionBasedOnSize(idsAudiencia, tamanhoParticaoLote);

		verificadorPeriodicoComum.insereLoteNaTabelaDeControle(idsAudienciaParticionado, passo, uuidLote);

		int numJob = 0;

		for (List<Integer> ids : idsAudienciaParticionado) {
			SinalizaProcessosAguardandoAudienciaCloudEvent sinalizaProcessosAguardandoAudienciaCloudEvent = new SinalizaProcessosAguardandoAudienciaCloudEvent(
					ids, uuidLoteString, ++numJob);

			amqpEvents.add(amqpManager.prepararMensagem(sinalizaProcessosAguardandoAudienciaCloudEvent,
					SinalizaProcessosAguardandoAudienciaCloudEvent.class));
		}

		Integer tamanhoLote = idsAudienciaParticionado.size();

		try {
			verificadorPeriodicoComum.enviarMensagens(passo, amqpManager, amqpEvents, uuidLoteString, tamanhoLote);
		} catch (Exception e) {
			log.error(e);

			throw new PJeBusinessException(e.getMessage());
		}

		verificadorPeriodicoComum.aguardaProcessamentoPasso(passo, uuidLote, tamanhoLote);

		sinalizaProcessosAguardandoAudienciaConsumer.purgeQueue();
	}
}