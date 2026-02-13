package br.com.infox.component.quartz;

import java.io.Serializable;
import java.util.Date;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

public class TriggerListenerLog implements TriggerListener, Serializable {

	public static final String NAME = "TriggerListenerLog";
	private static final long serialVersionUID = 1L;
	private static transient final LogProvider log = Logging.getLogProvider(TriggerListenerLog.class);

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void triggerComplete(Trigger trigger, JobExecutionContext executionContext, int arg2) {
		if(log.isDebugEnabled()){
			long time = new Date().getTime() - executionContext.getFireTime().getTime();
			JobDataMap jobDataMap = executionContext.getJobDetail().getJobDataMap();
			log.debug("Atividade assíncrona finalizada: " + trigger.getJobName() + "/" + QuartzJobsInfo.getJobExpression(jobDataMap) + " [" + time + " ms]");
		}
	}

	@Override
	public void triggerFired(Trigger trigger, JobExecutionContext executionContext) {
		if(log.isDebugEnabled()){
			JobDataMap jobDataMap = executionContext.getJobDetail().getJobDataMap();
			log.debug("Atividade assíncrona finalizada: " + trigger.getJobName() + "/" + QuartzJobsInfo.getJobExpression(jobDataMap));
		}
	}

	@Override
	public void triggerMisfired(Trigger trigger) {
		log.warn("Falha ao disparar atividade assíncrona: " + trigger.getName());
	}

	@Override
	public boolean vetoJobExecution(Trigger arg0, JobExecutionContext arg1) {
		return false;
	}

}