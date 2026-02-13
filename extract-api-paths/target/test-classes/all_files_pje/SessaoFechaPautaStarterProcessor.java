package br.com.infox.pje.processor;

import java.util.Properties;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jbpm.util.ClassLoaderUtil;
import org.quartz.SchedulerException;

@Name(SessaoFechaPautaStarterProcessor.NAME)
@Scope(ScopeType.APPLICATION)
// ATENCAO: NAO ativar essa anotacao, pois o componente é transacional
// @BypassInterceptors
@Startup(depends = "org.jboss.seam.async.dispatcher")
public class SessaoFechaPautaStarterProcessor {

	private static final String DEFAULT_CRON_EXPRESSION = "0 0 0 * * ?";

	public static final String NAME = "sessaoFechaPautaStarterProcessor";

	public static final String ID_SESSAO_TIMER_PARAMETER = "idSessaoTimerParameter";
	// Arquivo de configuração para os temporizadores
	private static Properties quartzProperties = ClassLoaderUtil.getProperties("seam.quartz.properties");

	public SessaoFechaPautaStarterProcessor() {
	}

	//@Observer({ "org.jboss.seam.postInitialization", "org.jboss.seam.postReInitialization" })
	//@Transactional
	public void init() throws SchedulerException {
		// Comentado por o processo de fechamento automático da pauta estar sendo realizado pelo VerificadorPeriodico
		
//		String enabled = quartzProperties.getProperty("org.quartz.timer.enabled", "false");
//		if (!"true".equals(enabled)) {
//			return;
//		}
//
//		String idSessaoTimer = null;
//		try {
//			idSessaoTimer = TimerUtil.getParametro(ID_SESSAO_TIMER_PARAMETER);
//		} catch (IllegalArgumentException e) {
//		}
//		if (idSessaoTimer == null) {
//			Parametro p = new Parametro();
//			p.setAtivo(true);
//			p.setDescricaoVariavel("ID do timer da sessao pauta do sistema");
//			p.setDataAtualizacao(new Date());
//			p.setNomeVariavel(ID_SESSAO_TIMER_PARAMETER);
//			p.setSistema(true);
//
//			String cronExpression = quartzProperties.getProperty("org.quartz.cronExpression", DEFAULT_CRON_EXPRESSION);
//			SessaoFechaPautaProcessor processor = SessaoFechaPautaProcessor.instance();
//			QuartzTriggerHandle handle = processor.fecharPautaAutomatico(new Date(), cronExpression);
//			EntityUtil.getEntityManager().flush();
//			String triggerName = handle.getTrigger().getName();
//			p.setValorVariavel(triggerName);
//			EntityUtil.getEntityManager().persist(p);
//			EntityUtil.getEntityManager().flush();
//		}
	}

}
