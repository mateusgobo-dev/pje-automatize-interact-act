package br.jus.cnj.pje.controleprazos;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.Status;

import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;

import br.com.infox.component.quartz.JobInfo;
import br.com.infox.component.quartz.QuartzJobsInfo;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.component.Util;
import br.jus.cnj.pje.business.dao.ParametroDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.manager.ParametroManager;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.util.ControleTransactional;

@Name(TratamentoDeComunicacoesAoDomicilioEletronico.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class TratamentoDeComunicacoesAoDomicilioEletronico {

	public static final String NAME = "tratamentoDeComunicacoesAoDomicilioEletronico";

	@Logger
	private Log log;

	@In(create = true)
	private ProcessoTrfManager processoTrfManager;

	@In(create = true)
	private ParametroManager parametroManager;

	@In(create = true)
	private FluxoManager fluxoManager;

	@In(create = true)
	private DomicilioEletronicoService domicilioEletronicoService;

	@In(create = true)
	private ProcessoJudicialService processoJudicialService;

	@In(create = true)
	private ParametroDAO parametroDAO;

	@In(create = true)
	private QuartzJobsInfo quartzJobsInfo;

	/**
	 * @return Instância da classe.
	 */
	public static TratamentoDeComunicacoesAoDomicilioEletronico instance() {
		return (TratamentoDeComunicacoesAoDomicilioEletronico) Component.getInstance(NAME);
	}

	@Asynchronous
	@Transactional
	public QuartzTriggerHandle execute(@IntervalCron String cron) {
		try {
			JobInfo job = quartzJobsInfo.getTratamentoDeComunicacoesAoDomicilioEletronico();

			StringBuilder jobInfoMessage = new StringBuilder();

			DateTimeFormatter formatter = DateTimeFormatter
					.ofPattern("yyyy-MM-dd HH:mm:ss z")
					.withZone(ZoneId.of("America/Sao_Paulo"));

			jobInfoMessage.append("\n").append("GroupName.........: ").append(job.getGroupName());
			jobInfoMessage.append("\n").append("JobName...........: ").append(job.getJobName());
			jobInfoMessage.append("\n").append("TriggerName.......: ").append(job.getTriggerName());
			jobInfoMessage.append("\n").append("CronExpression....: ").append(job.getCronExpression());
			jobInfoMessage.append("\n").append("PreviousFireTime..: ").append(formatter.format(job.getPreviousFireTime().toInstant()));
			jobInfoMessage.append("\n").append("NextFireTime......: ").append(formatter.format(job.getNextFireTime().toInstant()));

			log.info(jobInfoMessage.toString());
		} catch (Exception e) {
			log.error("Erro na obtenção nas informações do JOB: ", e.getLocalizedMessage());
		}

		tratamentoDeComunicacoes();

		return null;
	}

	public void tratamentoDeComunicacoes() {
		if (!domicilioEletronicoService.isIntegracaoHabilitada()) {
			return;
		}

		try {
			log.info("Iniciado às [{0}] o tratamento de comunicações enviadas ao Domicílio Eletrônico (TCD).", 
					new Date());

			Instant start = Instant.now();

			begin();

			String dataDaUltimaExecucaoJOB = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			int[] idsProcessoTrfArray = consultarProcessosEnviadosAoDomicilioEletronico();

			Optional
				.ofNullable(idsProcessoTrfArray)
				.filter(ids -> ids.length > 0)
				.ifPresent(ids -> {
						log.info("Total de [{0}] processos encontrados para serem incluídos no fluxo de tratamento de comunicações (TCD).",
								ids.length);

						final int totalProcessos = ids.length;
						final AtomicInteger contador = new AtomicInteger(1);

				Arrays.stream(ids).forEach(idProcessoTrf -> {
					int processoAtual = contador.getAndIncrement();

					log.info("Processando processo com id [{0}] no fluxo de tratamento de comunicações - [{1}/{2}].",
							idProcessoTrf, processoAtual, totalProcessos);

					this.incluirNoFluxoTratamentoComunicacoes(idProcessoTrf);

		            ControleTransactional.verificarNecessidadeDeCommitAndClearJbpm(processoAtual, true);
				});
	        });

			atualizaDataHoraUltimaExecucaoJOB(dataDaUltimaExecucaoJOB);

			log.info("Atualizado o valor do parametro [{0}] de tratamento de comunicações (TCD) para [{1}].",
					Parametros.PJE_DOMICILIO_ELETRONICO_DATA_HORA_ULTIMA_EXECUCAO_JOB_TCD, dataDaUltimaExecucaoJOB);

			commit();

			Instant end = Instant.now();

			String formattedDuration = contadorDeTempoDeExecucao(start, end);

			log.info("Tempo total de execução job de tratamento de comunicações [{0}]", formattedDuration);
		} catch (Throwable e) {
			log.error("Erro no JOB de Tratamento de Comunicações: ", e.getLocalizedMessage());

			rollBack();
		} finally {
			log.info("Encerrado às [{0}] o tratamento de comunicações enviadas ao Domicílio Eletrônico (TCD).",
					new Date());
		}
	}

	public void incluirNoFluxoTratamentoComunicacoes(int idProcessoTrf) {
		try {
			String parametro = Parametros.PJE_DOMICILIO_ELETRONICO_FLUXO_TRATAMENTO_COMUNICACOES;

			String cdFluxo = parametroDAO.valueOf(parametro);

			if (StringUtils.isBlank(cdFluxo)) {
				throw new PJeBusinessException(String.format("Parâmetro '%s' retornando nulo ou vazio.", parametro));
			}

			processoJudicialService.incluirNovoFluxo(idProcessoTrf, cdFluxo);
		} catch (PJeBusinessException e) {
			log.warn(
					"Erro ao incluir no fluxo de tratamento de comunicações, para o Processo com [id: {0}]. Erro: {1} ",
					idProcessoTrf, e.getLocalizedMessage());
		}
	}

	private void atualizaDataHoraUltimaExecucaoJOB(String dataDaUltimaExecucaoJOB) throws Exception {
		parametroManager.atualizaDataHoraUltimaExecucaoJOB(dataDaUltimaExecucaoJOB);
	}

	private int[] consultarProcessosEnviadosAoDomicilioEletronico() throws Exception {
		log.info("Buscando processos para o tratamento de comunicações enviadas ao Domicílio Eletrônico (TCD).");
		return processoTrfManager.getProcessosEnviadosAoDomicilioEletronico();
	}

	private void commit() {
		Util.commitAndOpenJoinTransaction();
	}

	private void begin() {
		if (Util.isTransactionMarkedRollback() || Util.getStatus() != Status.STATUS_NO_TRANSACTION) {
			Util.rollbackTransaction();
		}

		Util.beginAndJoinTransaction();
	}

	private void rollBack() {
		Util.rollbackAndOpenJoinTransaction();
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 */
	private String contadorDeTempoDeExecucao(Instant start, Instant end) {
		Duration duration = Duration.between(start, end);

		long millis = duration.toMillis();
		long hours = millis / (1000 * 60 * 60);
		millis %= 1000 * 60 * 60;
		long minutes = millis / (1000 * 60);
		millis %= 1000 * 60;
		long seconds = millis / 1000;
		millis %= 1000;

		return String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, millis);
	}

	/**
	 * Classe estática com as constantes dos atributos/métodos da classe.
	 *
	 */
	public static final class ATTR {

		/**
		 * Contrutor
		 * 
		 */
		private ATTR() {
			// Construtor.
		}

		public static final String EXECUTE = "execute";
	}
}
