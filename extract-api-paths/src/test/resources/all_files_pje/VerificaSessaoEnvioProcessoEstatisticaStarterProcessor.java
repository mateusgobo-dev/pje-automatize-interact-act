package br.com.infox.pje.processor;

import java.util.Date;
import java.util.Properties;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jbpm.util.ClassLoaderUtil;
import org.quartz.CronTrigger;
import org.quartz.SchedulerException;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.timer.TimerUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Parametro;

@Name(VerificaSessaoEnvioProcessoEstatisticaStarterProcessor.NAME)
@Scope(ScopeType.APPLICATION)
// ATENCAO: NAO ativar essa anotacao, pois o componente é transacional
// @BypassInterceptors
@Startup(depends = "org.jboss.seam.async.dispatcher")
public class VerificaSessaoEnvioProcessoEstatisticaStarterProcessor {

	private static final String DEFAULT_CRON_EXPRESSION = "0 0,30 18-19 * * ?";

	public static final String NAME = "verificaSessaoEnvioProcessoEstatisticaStarterProcessor";

	public static final String ID_SESSAO_ENVIO_PROCESSO_ESTATISTICA_TIMER_PARAMETER = "idSessaoEnvioProcessoEstatisticaTimerParameter";
	// Arquivo de configuração para os temporizadores
	private static Properties quartzProperties = ClassLoaderUtil.getProperties("seam.quartz.properties");

	public VerificaSessaoEnvioProcessoEstatisticaStarterProcessor() {
	}

	@Observer({ "org.jboss.seam.postInitialization", "org.jboss.seam.postReInitialization" })
	@Transactional
	public void init() throws SchedulerException {
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			String enabled = quartzProperties.getProperty("org.quartz.timer.enabled", "false");
			if (!"true".equals(enabled)) {
				return;
			}

			String idSessaoTimer = null;
			try {
				idSessaoTimer = TimerUtil.getParametro(ID_SESSAO_ENVIO_PROCESSO_ESTATISTICA_TIMER_PARAMETER);
			} catch (IllegalArgumentException e) {
			}
			if (idSessaoTimer == null) {

				Parametro p = new Parametro();
				p.setAtivo(true);
				p.setDescricaoVariavel("Variável para iniciar a thread que transfere todos os eventos do primeiro para o segundo grau.");
				p.setDataAtualizacao(new Date());
				p.setNomeVariavel(ID_SESSAO_ENVIO_PROCESSO_ESTATISTICA_TIMER_PARAMETER);
				p.setSistema(true);

				String cronExpression = quartzProperties.getProperty("org.quartz.cronExpression",
						DEFAULT_CRON_EXPRESSION);
				VerificaSecaoEnvioProcessoEstatisticaProcessor processor = VerificaSecaoEnvioProcessoEstatisticaProcessor
						.instance();
				QuartzTriggerHandle handle = processor.buscaEventosProcessos(cronExpression);
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
		}
	}

}
