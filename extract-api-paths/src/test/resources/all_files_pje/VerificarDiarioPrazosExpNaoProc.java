package br.jus.cnj.pje.controleprazos.verificadorperiodico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.amqp.consumers.EncerramentoPrazoNaoProcessualConsumer;
import br.jus.cnj.pje.amqp.model.dto.jobs.EncerramentoPrazoNaoProcessualCloudEvent;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.util.ControlePrazoExpedientesNaoProcessuaisManager;
import br.jus.cnj.pje.util.ControleTransactional;
import br.jus.pje.nucleo.entidades.AMQPEvent;
import br.jus.pje.nucleo.entidades.VerificadorPeriodicoLote;
import br.jus.pje.nucleo.enums.VerificadorPeriodicoPassosEnum;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * Classe responsável por executar a verificaçao diária de prazos de expedientes
 * não processuais. Ao encontrar processos com o prazo vencido, tramita-os para
 * a proxima tarefa, de acordo com a configuração do fluxo
 * 
 * @author luiz.mendes
 *
 */

@Name(VerificarDiarioPrazosExpNaoProc.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class VerificarDiarioPrazosExpNaoProc {

	public static final String NAME = "verificarDiarioPrazosExpNaoProc";

	@Logger
	private Log log;

	@In(create = true, required = true)
	private ProcessoJudicialService processoJudicialService;

	@In(create = true, required = true)
	private ControlePrazoExpedientesNaoProcessuaisManager controlePrazoExpedientesNaoProcessuaisManager;

	@In(create = true, required = true)
	private VerificadorPeriodicoComum verificadorPeriodicoComum;

	@In(create = true, required = true)
	private VerificadorPeriodicoAguardaProcessamentoPasso verificadorPeriodicoAguardaProcessamentoPasso;

	@In(create = true, required = true)
	private TramitacaoProcessualService tramitacaoProcessualService;

	@In(create = true, required = true)
	private EncerramentoPrazoNaoProcessualConsumer encerramentoPrazoNaoProcessualConsumer;

	/**
	 * Metodo utilizado pelo JOB que executa a verificaçao de prazos de expedientes
	 * nao processuais
	 * 
	 * @param cron - cron cadastrada no banco de dados, ao inicializar o JBoss pela
	 *             primeira vez (Classe AgendaServicosPeriodicos.java)
	 * @return null
	 * @throws PJeBusinessException
	 */
	@Asynchronous
	public QuartzTriggerHandle execute(@IntervalCron String cron) throws PJeBusinessException {
		Timer timerAguardaProcessamentoPasso = new Timer();

		if (ConfiguracaoIntegracaoCloud.isRabbitJobDJEEnabled()) {
			timerAguardaProcessamentoPasso.scheduleAtFixedRate(verificadorPeriodicoAguardaProcessamentoPasso, 0, 1000);
		}

		VerificadorPeriodicoLote verificadorPeriodicoLoteEPNP = run();

		enviarEmail(verificadorPeriodicoLoteEPNP);

		timerAguardaProcessamentoPasso.cancel();
		timerAguardaProcessamentoPasso.purge();

		return null;
	}

	/**
	 * Metodo que verifica se existem processos com prazo de expedientes nao
	 * processuais vencidos e os tramita para a proxima tarefa, configurada no fluxo
	 * respectivo
	 * 
	 * @return
	 * 
	 * @throws PJeBusinessException
	 */
	private VerificadorPeriodicoLote run() {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.ENCERRAMENTO_PRAZO_NAO_PROCESSUAL;

		log.info("VERIFICADOR DE CONTROLE DE PRAZOS NÃO PROCESSUAIS - QUARTZ TRIGGER HANDLE INICIADO");

		VerificadorPeriodicoLote verificadorPeriodicoLote = verificadorPeriodicoComum.insereRegistroRelatorio(passo);

		try {
			encerramentoPrazoNaoProcessual();

			verificadorPeriodicoLote.setProcessado(true);
		} catch (Exception e) {
			verificadorPeriodicoLote.setProcessado(false);

			log.error("Erro ao processar '" + passo.getLabel() + "'");

			e.printStackTrace();
		}

		verificadorPeriodicoLote = verificadorPeriodicoComum.atualizaRegistroRelatorio(verificadorPeriodicoLote);

		log.info("VERIFICADOR DE CONTROLE DE PRAZOS NÃO PROCESSUAIS - QUARTZ TRIGGER HANDLE FINALIZADO");

		return verificadorPeriodicoLote;
	}

	private void encerramentoPrazoNaoProcessual() {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.ENCERRAMENTO_PRAZO_NAO_PROCESSUAL;

		try {
			Integer numeroLimitadorProcessosPendentes = verificadorPeriodicoComum.getNumeroLimitador(passo);

			if (numeroLimitadorProcessosPendentes <= 0) {
				log.info("Limitando os processos em '" + passo.getLabel() + "' para '"
						+ numeroLimitadorProcessosPendentes + "' itens a serem processados!");

				return;
			}

			Date dataLimite = DateUtil.defineData(-1, 0, 0, 23, 59);

			List<Integer> processos = controlePrazoExpedientesNaoProcessuaisManager
					.recuperaIdProcessosTrfPrazoExpirado(dataLimite);

			if (processos.size() > numeroLimitadorProcessosPendentes) {
				List<Integer> tmp = processos.subList(0, numeroLimitadorProcessosPendentes);

				processos = tmp;

				log.info("Limitando os processos em '" + passo.getLabel() + "' para '"
						+ numeroLimitadorProcessosPendentes + "' itens a serem processados!");
			}

			if (ConfiguracaoIntegracaoCloud.isRabbitJobDJEEnabled()) {
				this.encerramentoPrazoNaoProcessualComControleDeLote(processos);
			} else {
				this.encerramentoPrazoNaoProcessualLocal(processos, true);
			}
		} catch (Exception e) {
			String msg = String.format("Erro ao encerrar prazo nao processual: [%s].", e.getLocalizedMessage());

			log.error(msg);

			e.printStackTrace();
		}
	}

	public List<Integer> encerramentoPrazoNaoProcessualLocal(List<Integer> processos, boolean processamentoBatch) {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.ENCERRAMENTO_PRAZO_NAO_PROCESSUAL;

		int cont = 0;

		List<Integer> processosProcessados = new ArrayList<>();
		List<Integer> processosProcessadosTmp = new ArrayList<>();

		ControleTransactional.beginTransaction();

		Date dataLimite = DateUtil.defineData(-1, 0, 0, 23, 59);

		for (Integer idProcesso : processos) {
			log.info("Processando item em '" + passo.getLabel() + "'. [idProcesso: " + idProcesso + "] [" + ++cont + "/"
					+ processos.size() + "]");

			ControleTransactional.verificarNecessidadeDeCommitAndClearJbpm(cont, true);

			try {
				processoJudicialService.sinalizaEncerramentoPrazoNaoProcessual(idProcesso, dataLimite);

				processosProcessadosTmp.add(idProcesso);

				if (ControleTransactional.verificarNecessidadeDeCommitAndClearJbpm(cont, processamentoBatch)) {
					processosProcessados.addAll(processosProcessadosTmp.stream().collect(Collectors.toList()));

					processosProcessadosTmp.clear();
				}
			} catch (Exception e) {
				ControleTransactional.rollbackTransaction();

				processosProcessadosTmp.clear();

				String texto = String.format(
						"Não foi possível processar o expediente com prazo nao processual [idProcesso = %d].",
						idProcesso);

				log.error(texto);

				e.printStackTrace();

				ControleTransactional.beginTransaction();
			}
		}

		ControleTransactional.commitTransactionAndFlushAndClear();

		if (processosProcessadosTmp != null && processosProcessadosTmp.size() > 0) {
			processosProcessados.addAll(processosProcessadosTmp.stream().collect(Collectors.toList()));
		}

		return processosProcessados;
	}

	private void encerramentoPrazoNaoProcessualComControleDeLote(List<Integer> idsProcesso) throws Exception {
		encerramentoPrazoNaoProcessualConsumer.purgeQueue();

		String passo = VerificadorPeriodicoPassosEnum.ENCERRAMENTO_PRAZO_NAO_PROCESSUAL.getLabel();

		AMQPEventManager amqpManager = AMQPEventManager.instance();

		List<AMQPEvent> amqpEvents = new ArrayList<AMQPEvent>();

		UUID uuidLote = UUID.randomUUID();

		String uuidLoteString = uuidLote.toString();

		Integer tamanhoParticaoLote = ConfiguracaoIntegracaoCloud.getRabbitTamanhoParticaoLote();

		Collection<List<Integer>> idsProcessoParticionado = verificadorPeriodicoComum.partitionBasedOnSize(idsProcesso,
				tamanhoParticaoLote);

		verificadorPeriodicoComum.insereLoteNaTabelaDeControle(idsProcessoParticionado, passo, uuidLote);

		int numJob = 0;

		for (List<Integer> ids : idsProcessoParticionado) {
			EncerramentoPrazoNaoProcessualCloudEvent encerramentoPrazoNaoProcessualCloudEvent = new EncerramentoPrazoNaoProcessualCloudEvent(
					ids, uuidLoteString, ++numJob);

			amqpEvents.add(amqpManager.prepararMensagem(encerramentoPrazoNaoProcessualCloudEvent,
					EncerramentoPrazoNaoProcessualCloudEvent.class));
		}

		Integer tamanhoLote = idsProcessoParticionado.size();

		verificadorPeriodicoComum.enviarMensagens(passo, amqpManager, amqpEvents, uuidLoteString, tamanhoLote);

		verificadorPeriodicoComum.aguardaProcessamentoPasso(passo, uuidLote, tamanhoLote);

		encerramentoPrazoNaoProcessualConsumer.purgeQueue();
	}

	private void enviarEmail(VerificadorPeriodicoLote verificadorPeriodicoLoteEPNP) {
		String parametroEmails = ComponentUtil.getComponent(ParametroService.class)
				.valueOf("verificadorPrazosExpNaoProcEmails");

		if (parametroEmails != null) {
			List<String> emails = Arrays.asList(parametroEmails.split("\\s*,\\s*"));

			if (emails != null && !emails.isEmpty()) {
				verificadorPeriodicoComum.enviarEmail(Arrays.asList(verificadorPeriodicoLoteEPNP), emails, NAME);
			}
		}
	}
}