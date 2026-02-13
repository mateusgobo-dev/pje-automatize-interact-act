package br.com.infox.cliente.component.securitytoken;

import java.util.Date;
import java.util.Properties;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;
import org.jbpm.util.ClassLoaderUtil;
import org.quartz.CronTrigger;
import org.quartz.SchedulerException;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.service.LogService;
import br.com.infox.timer.TimerUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Parametro;

/**
 * 
 * @author rodrigo
 * 
 */
@Name(RemoveTokenExpiradosStarter.NAME)
@Scope(ScopeType.APPLICATION)
// ATENCAO: NAO ativar a anotacao @BypassInterceptors, pois o componente é transacional
@Startup(depends = {"org.jboss.seam.async.dispatcher", "genericDAO"})
@Install(dependencies = {"org.jboss.seam.async.dispatcher", "genericDAO"})
public class RemoveTokenExpiradosStarter{

	private static final String DEFAULT_CRON_EXPRESSION = "0 0/10 * * * ?";

	public static final String NAME = "removeTokenExpiradosStarter";
	@In
	private LogService logService;
	@Logger
	private Log log;

	private static final String ID_TIMER_PARAMETER = "idRemoveTokenExpiradosTimerParameter";
	// Arquivo de configuração para os temporizadores
	private static Properties quartzProperties = ClassLoaderUtil.getProperties("seam.quartz.properties");

	@Create
	@Transactional
	public void init() throws SchedulerException{
		try {
			String enabled = quartzProperties.getProperty("org.quartz.timer.enabled", "false");
			if (!"true".equals(enabled)){
				return;
			}
	
			String idIniciarFluxoTimer = TimerUtil.getParametro(ID_TIMER_PARAMETER);
	
			if (idIniciarFluxoTimer == null){
				Parametro p = new Parametro();
				p.setAtivo(true);
				p.setDescricaoVariavel("ID do timer do sistema");
				p.setDataAtualizacao(new Date());
				p.setNomeVariavel(ID_TIMER_PARAMETER);
				p.setSistema(true);
	
				String cronExpression = quartzProperties.getProperty(
							"org.quartz.cronExpression", DEFAULT_CRON_EXPRESSION);
	
				SecurityTokenControler tokenControler = SecurityTokenControler.instance();
	
				QuartzTriggerHandle handle = tokenControler.removeExpirados(new Date(), cronExpression);
				// PJEII-7617] Inclusão da condição para ignorar falhas execuções; Rafael Carvalho (CSJT); 2013-29-05 
				if (handle.getTrigger() != null && ParametroUtil.instance().isIgnoraFalhaDeExecucaoDoQuartz()) {
					handle.getTrigger().setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);	
				}
				// [PJEII-7617] Fim.
				EntityUtil.getEntityManager().flush();
				String triggerName = handle.getTrigger().getName();
				p.setValorVariavel(triggerName);
				EntityUtil.getEntityManager().persist(p);
				EntityUtil.getEntityManager().flush();
			}
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "init");
		}
	}

}
