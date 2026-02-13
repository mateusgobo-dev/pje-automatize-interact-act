package br.jus.cnj.pje.controleprazos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.BooleanUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;
import org.quartz.SchedulerException;

import br.com.infox.component.quartz.QuartzJobsInfo;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;

@Name("agendaServicosQuartz")
@Scope(ScopeType.EVENT)
@AutoCreate
public class AgendaServicosQuartz {
	
	@Logger
	private Log logger;
	
	public boolean servicoAgendado(String servico, String cron) {
		QuartzJobsInfo jobs = ComponentUtil.getComponent(QuartzJobsInfo.class);
		return jobs.isJobAgendado(servico, cron);
	}
	
	@Transactional
	public void agendarServico(String nomeComponente, String nomeMetodo, String cron) throws SchedulerException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
		agendarServico(nomeComponente, nomeMetodo, cron, true);
	}
	
	@Transactional
	public void agendarServico(String nomeComponente, String nomeMetodo, String cron, Boolean flush) throws SchedulerException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
		logger.info("Agendando serviços com a definição de cron {0}.", cron);
		Object componente = Component.getInstance(nomeComponente);
		Method metodo = componente.getClass().getMethod(nomeMetodo, String.class);
		QuartzTriggerHandle handle = (QuartzTriggerHandle)metodo.invoke(componente, cron);
		if (BooleanUtils.isTrue(flush)) {
			EntityUtil.getEntityManager().flush();
		}
		logger.info("Serviço agendado: {0}", handle);
	}
	

}
