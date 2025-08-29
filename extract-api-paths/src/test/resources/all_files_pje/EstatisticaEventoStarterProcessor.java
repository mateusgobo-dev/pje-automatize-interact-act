package br.com.infox.pje.processor;

import java.util.Date;
import java.util.Properties;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
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

/**
 * Classe responsável por iniciar o processador de estatística dos eventos
 * processuais.
 * 
 * @author Daniel
 * 
 */
@Name(EstatisticaEventoStarterProcessor.NAME)
@Scope(ScopeType.APPLICATION)
// ATENCAO: NAO ativar a anotacao @BypassInterceptors, pois o componente é
// transacional
@Startup(depends = "org.jboss.seam.async.dispatcher")
@Install(dependencies = { "org.jboss.seam.async.dispatcher" })
public class EstatisticaEventoStarterProcessor {

	public static final String NAME = "estatisticaEventoStarterProcessor";

	private static final String DEFAULT_CRON_EXPRESSION = "0 0/10 * 1/1 * ? *"; // a cada 10 min
	private static final String ID_ESTATISCA_EVENTO_PARAMETER = "IdEstatiscaEventoParameter";
	// Arquivo de configuração para os temporizadores
	private static Properties quartzProperties = ClassLoaderUtil.getProperties("seam.quartz.properties");

	public EstatisticaEventoStarterProcessor() {
	}

	@Create
	@Transactional
	public void init() throws SchedulerException {
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			String enabled = quartzProperties.getProperty("org.quartz.timer.enabled", "false");
			if (!"true".equals(enabled)) {
				return;
			}

			String idTratarEventoTimer = null;
			try {
				idTratarEventoTimer = TimerUtil.getParametro(ID_ESTATISCA_EVENTO_PARAMETER);
			} catch (IllegalArgumentException e) {
			}
			if (idTratarEventoTimer == null) {
				Parametro p = new Parametro();
				p.setAtivo(true);
				p.setDescricaoVariavel("ID do timer do sistema");
				p.setDataAtualizacao(new Date());
				p.setNomeVariavel(ID_ESTATISCA_EVENTO_PARAMETER);
				p.setSistema(true);
				String cronExpression = quartzProperties.getProperty("org.quartz.cronExpression",
						DEFAULT_CRON_EXPRESSION);
				EstatisticaEventoProcessor processor = EstatisticaEventoProcessor.instance();
				QuartzTriggerHandle handle = processor.processarEventos(cronExpression);
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