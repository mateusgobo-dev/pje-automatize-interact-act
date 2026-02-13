package br.com.infox.component.quartz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.async.QuartzDispatcher;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.controleprazos.TratamentoDeComunicacoesAoDomicilioEletronico;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodico;
import br.jus.cnj.pje.servicos.ConsolidadorDocumentosService;

@Name("quartzJobsInfo")
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Startup(depends = "org.jboss.seam.async.dispatcher")
@Install(dependencies = { "org.jboss.seam.async.dispatcher" })
public class QuartzJobsInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Pattern patternExpr = Pattern.compile("^AsynchronousInvocation\\((.*)\\)$");
	
	public static final String UTILIZA_HORA_ATUAL = "utilizaHoraAtual"; 

	public static Scheduler getScheduler() {
		return QuartzDispatcher.instance().getScheduler();
	}

	public List<JobInfo> getDetailJobsInfo() {
		List<JobInfo> listAll = new ArrayList<JobInfo>();
		try {
			Scheduler scheduler = getScheduler();
			String[] jobGroupNames = scheduler.getJobGroupNames();
			for (String groupName : jobGroupNames) {
				List<JobInfo> mapInfoGroup = getListMapInfoGroupFromJobs(groupName);
				listAll.addAll(mapInfoGroup);
			}
		} catch (SchedulerException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao obter os detalhes dos jobs do quartz.", e);
		}
		Collections.sort(listAll);
		return listAll;
	}

	private List<JobInfo> getListMapInfoGroupFromJobs(String groupName) throws SchedulerException {
		Scheduler scheduler = getScheduler();
		String[] jobNames = scheduler.getJobNames(groupName);
		List<JobInfo> jobInfos = new ArrayList<JobInfo>(jobNames.length);
		for (String jobName : jobNames) {
			JobDetail jobDetail = scheduler.getJobDetail(jobName, groupName);
			Trigger[] triggersOfJob = scheduler.getTriggersOfJob(jobName, groupName);
			for (Trigger trigger : triggersOfJob) {
				jobInfos.add(getTrigerDetailList(jobDetail, trigger));
			}
		}
		return jobInfos;
	}

	private JobInfo getTrigerDetailList(JobDetail jobDetail, Trigger trigger) {
		JobInfo job = new JobInfo();
		String jobName = trigger.getJobName();
		JobDataMap jobDataMap = jobDetail.getJobDataMap();
		job.setTriggerName(trigger.getName());
		job.setJobName(jobName);
		job.setGroupName(jobDetail.getGroup());
		job.setNextFireTime(trigger.getNextFireTime());
		job.setPreviousFireTime(trigger.getPreviousFireTime());
		String jobExpression = getJobExpression(jobDataMap);
		job.setJobExpression(jobExpression);
		job.setValid(isJobValid(jobExpression));
		if (trigger instanceof CronTrigger) {
			CronTrigger cronTrigger = (CronTrigger) trigger;
			job.setCronExpression(cronTrigger.getCronExpression());
		}
		return job;
	}

	public static String getJobExpression(JobDataMap dataMap) {
		Collection<?> values = dataMap.values();
		if (values != null && !values.isEmpty()) {
			String dataJobDetail = values.iterator().next().toString();
			Matcher matcher = patternExpr.matcher(dataJobDetail);
			if (matcher.find()) {
				return matcher.group(1);
			} else {
				return dataJobDetail;
			}
		}
		return null;
	}

	/**
	 * Testa se o a expressão do job é válida.
	 * 
	 * @param jobExpression
	 * @return
	 */
	private boolean isJobValid(String jobExpression) {
		if (jobExpression == null || jobExpression.indexOf(".") == -1) {
			return false;
		}
		String[] split = jobExpression.split("\\.");
		String componentName = split[0];
		Object component = ComponentUtil.getComponent(componentName);
		if (component == null) {
			return false;
		}
		String medothName = split[1].replaceAll("[()]", "");
		return isMethodValid(component, medothName);
	}

	private boolean isMethodValid(Object component, String medothName) {
		try {
			component.getClass().getDeclaredMethod(medothName, Date.class, String.class);
			return true;
		} catch (Exception e) { /* not found */
		}
		try {
			component.getClass().getDeclaredMethod(medothName, String.class);
			return true;
		} catch (Exception e) { /* not found */
		}
		return false;
	}

	public void triggerJob(String jobName, String groupName) {
		try {
			getScheduler().getContext().put(UTILIZA_HORA_ATUAL, true);
			getScheduler().triggerJob(jobName, groupName);
			FacesMessages.instance().add(Severity.INFO, "Job executado com sucesso: " + jobName);
		} catch (SchedulerException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao executar job " + jobName, e);
			e.printStackTrace();
		}
	}

	public void deleteJob(String jobName, String groupName) {
		try {
			getScheduler().deleteJob(jobName, groupName);
			FacesMessages.instance().add(Severity.INFO, "Job removido com sucesso: " + jobName);
		} catch (SchedulerException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao remover job " + jobName, e);
			e.printStackTrace();
		}
	}

	@Create
	public void addGlobalTriggerListener() throws SchedulerException {
		Scheduler scheduler = QuartzJobsInfo.getScheduler();
		if (scheduler.getGlobalTriggerListeners().isEmpty()) {
			scheduler.addGlobalTriggerListener(new TriggerListenerLog());
		}
	}

	
	public void apagarJobs() {
		Identity.instance().checkRole("admin");
		String sql = "delete from qrtz_cron_triggers";
		EntityUtil.createNativeQuery(sql, "qrtz_cron_triggers").executeUpdate();
		
		sql = "delete from qrtz_fired_triggers";
		EntityUtil.createNativeQuery(sql, "qrtz_fired_triggers").executeUpdate();
		
		sql = "delete from qrtz_simple_triggers";
		EntityUtil.createNativeQuery(sql, "qrtz_simple_triggers").executeUpdate();
		
		sql = "delete from qrtz_triggers";
		EntityUtil.createNativeQuery(sql, "qrtz_triggers").executeUpdate();
		
		sql = "delete from qrtz_job_details";
		EntityUtil.createNativeQuery(sql, "qrtz_job_details").executeUpdate();
		
		sql = "delete from tb_parametro where vl_variavel like '%:%:-7%'";
		EntityUtil.createNativeQuery(sql, "tb_parametro").executeUpdate();
		
		FacesMessages.instance().add(Severity.INFO,
				"Jobs apagados com sucesso. Reinicie o servidor para que os Jobs sejam refeitos.");
	}

	
	public void apagarHistoricoEstatisticaEventoProcesso() {
		Identity.instance().checkRole("admin");
		String hql = "delete from HistoricoEstatisticaEventoProcesso o where cast(o.dtUltimaAtualizacao as date) = current_date";
		FacesMessages.instance().add(Severity.INFO,
				"HistoricoEstatisticaEventoProcesso para o dia atual foi apagado com sucesso.");
		EntityUtil.createQuery(hql).executeUpdate();
	}

	/**
	 * Retorna o job do consolidador de documentos.
	 * 
	 * @return Consolidador de documentos.
	 */
	public JobInfo getConsolidadorDocumentosJobInfo() {
		return getJobInfo(ConsolidadorDocumentosService.NAME);
	}
	
	/**
	 * Retorna o job do verificador periódico.
	 * 
	 * @return Verificador periódico.
	 */
	public JobInfo getVerificadorPeriodico() {
		return getJobInfo(VerificadorPeriodico.NAME);
	}

	/**
	 * Executa o job VerificadorPeriodico.
	 */
	public void verificadorPeriodicoJob() {
		JobInfo job = getVerificadorPeriodico();
		triggerJob(job);
	}

	/**
	 * Retorna o job do tratamento de comunicações ao domicilio eletrônico (TCI).
	 * 
	 * @return TCI.
	 */
	public JobInfo getTratamentoDeComunicacoesAoDomicilioEletronico() {
		return getJobInfo(TratamentoDeComunicacoesAoDomicilioEletronico.NAME);
	}

	/**
	 * Executa o job TratamentoDeComunicacoesAoDomicilioEletronico.
	 */
	public void TratamentoDeComunicacoesAoDomicilioEletronicoJob() {
		JobInfo job = getTratamentoDeComunicacoesAoDomicilioEletronico();
		triggerJob(job);
	}
	
	/**
	 * Executa o job passado por parâmetro.
	 * 
	 * @param job JobInfo.
	 */
	public void triggerJob(JobInfo job) {
		
		if (job != null) {
			triggerJob(job.getJobName(), job.getGroupName());
		}
	}
	
	/**
	 * Retorna o JobInfo solicitado.
	 * 
	 * @param nomeJob Nome do job.
	 * @return JobInfo.
	 */
	protected JobInfo getJobInfo(String nomeJob) {
		JobInfo resultado = null;
		
		List<JobInfo> jobs = getDetailJobsInfo();
		for (int indice = 0; indice < jobs.size() && resultado == null; indice++) {
			JobInfo job = (JobInfo) jobs.get(indice);
			String expression = job.getJobExpression();
			
			if (job.isValid() && StringUtils.startsWith(expression, nomeJob)) {
				resultado = job;
			}
		}
		
		return resultado;
	}
	
	/**
	 * Retorna true se já existir o job para o período cron informado.
	 * 
	 * @param nomeJob Nome do job.
	 * @param cron Período.
	 * @return Boleano.
	 */
	public Boolean isJobAgendado(String nomeJob, String cron) {
		JobInfo resultado = null;
		
		List<JobInfo> jobs = getDetailJobsInfo();
		for (int indice = 0; indice < jobs.size() && resultado == null; indice++) {
			JobInfo job = (JobInfo) jobs.get(indice);
			String expression = job.getJobExpression();
			String expressionCron = job.getCronExpression();
			
			if (job.isValid() && StringUtils.startsWith(expression, nomeJob) && StringUtils.equals(expressionCron, cron)) {
				resultado = job;
			}
		}
		
		return (resultado != null);
	}
	
	/**
	 * Verifica a quantidade de instâncias do job que estão em execução
	 *
	 */	
	@SuppressWarnings("unchecked")
	public Integer getQuantidadeInstanciasJobEmExecucao(String nomeJob) {
		
	    List<JobExecutionContext> jobsEmExecucao;
	    
		try {
			// Em caso de utilização do quartz em 
			// modo cluster (desabilitado por padrão no PJe), 
			// deve-se ir direto na tabela qrtz_fired_triggers.
			// O método getCurrentlyExecutingJobs() somente busca
			// a contagem do scheduler da instância atual.
			jobsEmExecucao = getScheduler().getCurrentlyExecutingJobs();
			
		} catch (SchedulerException e) {
			// Não foi possível acessar o Scheduler
			return null;
		}
		
	    Integer totalInstancias = 0;
	    JobInfo jobAtual = getJobInfo(nomeJob);
	    
	    if(jobAtual != null) {
	    	
		    for (JobExecutionContext jobCtx : jobsEmExecucao) {
		    	
		        String jobNameDB = jobCtx.getJobDetail().getName();  
		        
		        // Considerar somente o nome do job. O grupo da trigger
		        // em execução pode variar atualmente no PJe entre
		        // DEFAULT e MANUAL_TRIGGER (quando acionado manualmente)
		        if (jobAtual.getJobName().equalsIgnoreCase(jobNameDB)) {
		        	totalInstancias++;
		        }
		    }
	    }
	    return totalInstancias;
	}

	/** 
	 * Verifica se o job está em execução
	 * nomeJob -> Nome do componente. 
	 * ex.: VerificadorPeriodico.NAME
	 */
	public Boolean isJobEmExecucao(String nomeJob) {
		
		return isJobEmExecucao(nomeJob, false);
	}
	
	/**
	 * Verifica se do job está em execução
	 * - Se o método for chamado de dentro do próprio job deve-se 
	 * - setar isProprioJobChamando = true para que ele mesmo
	 * - não seja contabilizado.
	 */	
	public Boolean isJobEmExecucao(String nomeJob, Boolean isProprioJobChamando) {
		
		Integer totalInstancias = getQuantidadeInstanciasJobEmExecucao(nomeJob);
		
		if(isProprioJobChamando.booleanValue()) {
			return totalInstancias > 1; 
		}
		else {
			return totalInstancias != 0;
		}
	}	
}
