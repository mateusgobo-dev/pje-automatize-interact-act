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

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.amqp.consumers.ProsseguimentoSemPrazoConsumer;
import br.jus.cnj.pje.amqp.model.dto.jobs.ProsseguimentoSemPrazoCloudEvent;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodicoComum;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoAlertaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.util.ControleTransactional;
import br.jus.pje.nucleo.entidades.AMQPEvent;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.VerificadorPeriodicoLote;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;
import br.jus.pje.nucleo.enums.VerificadorPeriodicoPassosEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name(ProsseguimentoSemPrazo.NAME)
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class ProsseguimentoSemPrazo {

	public static final String NAME = "prosseguimentoSemPrazo";

	@Logger
	private Log log;

	@In(create = true, required = true)
	private ProcessoJudicialService processoJudicialService;

	@In(create = true, required = true)
	private VerificadorPeriodicoComum verificadorPeriodicoComum;

	@In(create = true, required = true)
	private ProcessoParteExpedienteManager processoParteExpedienteManager;

	@In(create = true, required = true)
	private ProcessoAlertaManager processoAlertaManager;

	@In(create = true, required = true)
	private ProsseguimentoSemPrazoConsumer prosseguimentoSemPrazoConsumer;

	public VerificadorPeriodicoLote run() {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.SINALIZA_PROSSEGUIMENTO_SEM_PRAZO;

		log.info(
				"Localizando os prazos judiciais sem prazo e sinalizando para eventual prosseguimento caso a criação do ato supere o parâmetro [{0}].",
				Parametros.ESPERA_MAXIMA_SEM_PRAZO);

		VerificadorPeriodicoLote verificadorPeriodicoLote = verificadorPeriodicoComum.insereRegistroRelatorio(passo);

		try {
			Integer numeroLimitadorExpedientesPendentes = verificadorPeriodicoComum.getNumeroLimitador(passo);

			if (numeroLimitadorExpedientesPendentes <= 0) {
				log.info("Limitando os expedientes em '" + passo.getLabel() + "' para '"
						+ numeroLimitadorExpedientesPendentes + "' itens a serem processados!");
			} else {
				Date hoje = DateUtil.getBeginningOfToday();

				String maximoEsperaParam = ComponentUtil.getComponent(ParametroService.class)
						.valueOf(Parametros.ESPERA_MAXIMA_SEM_PRAZO);

				long esperaMaxima = 30;

				if (maximoEsperaParam != null && !StringUtil.fullTrim(maximoEsperaParam).isEmpty()) {
					try {
						esperaMaxima = Long.parseLong(maximoEsperaParam);
					} catch (NumberFormatException e) {
						log.error(
								"Erro ao tentar converter o parâmetro {0}. Utilizando o valor padrão para espera de manifestações ({1} dias).",
								Parametros.ESPERA_MAXIMA_SEM_PRAZO, esperaMaxima);
					}
				}

				List<Integer> expedientes = processoParteExpedienteManager
						.getAtosComunicacaoSemPrazoExpiradosIds(esperaMaxima, hoje);

				if (expedientes.size() > numeroLimitadorExpedientesPendentes) {
					List<Integer> tmp = expedientes.subList(0, numeroLimitadorExpedientesPendentes);

					expedientes = tmp;

					log.info("Limitando os expedientes em '" + passo.getLabel() + "' para '"
							+ numeroLimitadorExpedientesPendentes + "' itens a serem processados!");
				}

				if (ConfiguracaoIntegracaoCloud.isRabbitJobDJEEnabled()) {
					this.runComControleDeLote(expedientes);
				} else {
					this.runLocal(expedientes, true);
				}
			}

			verificadorPeriodicoLote.setProcessado(true);
		} catch (Exception e) {
			verificadorPeriodicoLote.setProcessado(false);

			String msg = String.format("Erro ao tentar recuperar os atos de comunicação pendentes de ciência: [%s].",
					e.getLocalizedMessage());

			log.error(msg);

			log.error(e);
		}

		verificadorPeriodicoLote = verificadorPeriodicoComum.atualizaRegistroRelatorio(verificadorPeriodicoLote);

		log.info("Verificação dos prazos judiciais sem prazo finalizada.");

		return verificadorPeriodicoLote;
	}

	public List<Integer> runLocal(List<Integer> expedientes, boolean processamentoBatch) {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.SINALIZA_PROSSEGUIMENTO_SEM_PRAZO;

		int cont = 0;

		List<Integer> expedientesProcessados = new ArrayList<>();
		List<Integer> expedientesProcessadosTmp = new ArrayList<>();

		ControleTransactional.beginTransactionAndClearJbpm();

		for (Integer expedienteId : expedientes) {
			try {
				log.info("Processando item em '" + passo.getLabel() + "'. [idProcessoParteExpediente: " + expedienteId
						+ "] [" + ++cont + "/" + expedientes.size() + "]");

				sinalizaProsseguimentoSemPrazo(expedienteId);

				ControleTransactional.verificarNecessidadeDeCommitAndClearJbpm(cont, true);

				expedientesProcessadosTmp.add(expedienteId);

				if (ControleTransactional.verificarNecessidadeDeCommitAndClearJbpm(cont, processamentoBatch)) {
					expedientesProcessados.addAll(expedientesProcessadosTmp.stream().collect(Collectors.toList()));

					expedientesProcessadosTmp.clear();
				}
			} catch (Exception e) {
				ControleTransactional.rollbackTransaction();

				expedientesProcessadosTmp.clear();

				String msg = String.format(
						"Não foi possível sinalizar para tramitação em fluxo quanto ao expediente sem prazo [%d].",
						expedienteId);

				log.error(msg);

				log.error(e);

				verificadorPeriodicoComum.incluirAlertaErroExpedientes(msg, CriticidadeAlertaEnum.C, expedienteId);

				ControleTransactional.commitTransactionAndFlushAndClear();
			}
		}

		ControleTransactional.commitTransactionAndFlushAndClear();

		if (expedientesProcessadosTmp != null && expedientesProcessadosTmp.size() > 0) {
			expedientesProcessados.addAll(expedientesProcessadosTmp.stream().collect(Collectors.toList()));
		}

		return expedientesProcessados;
	}

	private void runComControleDeLote(List<Integer> idsProcessoParteExpediente) throws PJeBusinessException {
		prosseguimentoSemPrazoConsumer.purgeQueue();

		String passo = VerificadorPeriodicoPassosEnum.SINALIZA_PROSSEGUIMENTO_SEM_PRAZO.getLabel();

		AMQPEventManager amqpManager = AMQPEventManager.instance();

		List<AMQPEvent> amqpEvents = new ArrayList<AMQPEvent>();

		UUID uuidLote = UUID.randomUUID();

		String uuidLoteString = uuidLote.toString();

		Integer tamanhoParticaoLote = ConfiguracaoIntegracaoCloud.getRabbitTamanhoParticaoLote();

		Collection<List<Integer>> idsProcessoParteExpedienteParticionado = verificadorPeriodicoComum
				.partitionBasedOnSize(idsProcessoParteExpediente, tamanhoParticaoLote);

		verificadorPeriodicoComum.insereLoteNaTabelaDeControle(idsProcessoParteExpedienteParticionado, passo, uuidLote);

		int numJob = 0;

		for (List<Integer> ids : idsProcessoParteExpedienteParticionado) {
			ProsseguimentoSemPrazoCloudEvent prosseguimentoSemPrazoCloudEvent = new ProsseguimentoSemPrazoCloudEvent(
					ids, uuidLoteString, ++numJob);

			amqpEvents.add(amqpManager.prepararMensagem(prosseguimentoSemPrazoCloudEvent,
					ProsseguimentoSemPrazoCloudEvent.class));
		}

		Integer tamanhoLote = idsProcessoParteExpedienteParticionado.size();

		try {
			verificadorPeriodicoComum.enviarMensagens(passo, amqpManager, amqpEvents, uuidLoteString, tamanhoLote);
		} catch (Exception e) {
			log.error(e);

			throw new PJeBusinessException(e.getMessage());
		}

		verificadorPeriodicoComum.aguardaProcessamentoPasso(passo, uuidLote, tamanhoLote);

		prosseguimentoSemPrazoConsumer.purgeQueue();
	}

	private void sinalizaProsseguimentoSemPrazo(Integer idExpediente) throws PJeBusinessException {
		try {
			ProcessoParteExpediente ppe = verificadorPeriodicoComum.getAtoComunicacaoPessoal(idExpediente);

			if (!ppe.getFechado()) {
				ppe.setFechado(true);

				processoParteExpedienteManager.persistAndFlush(ppe);

				processoJudicialService.observaPreclusaoManifestacao(ppe.getProcessoJudicial());
			}
		} catch (PJeBusinessException e) {
			log.error("Não foi possível finalizar a observação do expediente sem prazo [{0}].", idExpediente);

			throw new PJeRuntimeException("Não foi possível finalizar a observação do expediente sem prazo [{0}].", e,
					idExpediente);
		}
	}
}