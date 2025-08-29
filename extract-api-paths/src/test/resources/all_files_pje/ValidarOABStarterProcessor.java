package br.com.infox.pje.processor;

import java.util.Date;
import java.util.Properties;

import javax.transaction.Transactional;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jbpm.util.ClassLoaderUtil;
import org.quartz.CronTrigger;
import org.quartz.SchedulerException;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.timer.TimerUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Parametro;

@Name(ValidarOABStarterProcessor.NAME)
@Scope(ScopeType.APPLICATION)
@Startup(depends = "org.jboss.seam.async.dispatcher")
public class ValidarOABStarterProcessor {

	private static final String DEFAULT_CRON_EXPRESSION = "0 0 0 * * ?";

	public static final String NAME = "validarOABStarterProcessor";

	public static final String ID_VALIDAR_OAB_TIMER_PARAMETER = "idValidarOABTimerParameter";

	private static Properties quartzProperties = ClassLoaderUtil.getProperties("seam.quartz.properties");

	public ValidarOABStarterProcessor() {
	}

	@Observer({ "org.jboss.seam.postInitialization", "org.jboss.seam.postReInitialization" })
	public void init() throws SchedulerException {
		String enabled = quartzProperties.getProperty("org.quartz.timer.enabled", "false");
		if (!"true".equals(enabled)) {
			return;
		}

		String idValidacaoTimer = null;

		try {
			idValidacaoTimer = TimerUtil.getParametro(ID_VALIDAR_OAB_TIMER_PARAMETER);
		} catch (IllegalArgumentException e) {
		}

		if (idValidacaoTimer == null) {
			String cronExpression = quartzProperties.getProperty("org.quartz.cronExpression", DEFAULT_CRON_EXPRESSION);

			ValidarOABProcessor processor = ValidarOABProcessor.instance();
			QuartzTriggerHandle handle = processor.validarOAB(new Date(), cronExpression);

			// PJEII-7617] Inclusão da condição para ignorar falhas execuções; Rafael
			// Carvalho (CSJT); 2013-29-05

			if (handle.getTrigger() != null && ParametroUtil.instance().isIgnoraFalhaDeExecucaoDoQuartz()) {
				handle.getTrigger().setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
			}

			// [PJEII-7617] Fim.

			saveParameter(handle);
		}
	}

	@Transactional
	private void saveParameter(QuartzTriggerHandle handle) throws SchedulerException {
		EntityUtil.getEntityManager().flush();

		String triggerName = handle.getTrigger().getName();

		Parametro p = new Parametro();

		p.setAtivo(true);
		p.setDescricaoVariavel("ID do timer do sistema");
		p.setDataAtualizacao(new Date());
		p.setNomeVariavel(ID_VALIDAR_OAB_TIMER_PARAMETER);
		p.setSistema(true);
		p.setValorVariavel(triggerName);

		EntityUtil.getEntityManager().persist(p);
		EntityUtil.getEntityManager().flush();
	}
}