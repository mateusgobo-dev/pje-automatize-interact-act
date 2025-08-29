package br.com.je.pje.manager;

import java.util.Properties;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;
import org.jbpm.util.ClassLoaderUtil;

import br.jus.cnj.pje.controleprazos.AgendaServicosQuartz;
import br.jus.cnj.pje.nucleo.manager.LiberacaoPublicacaoDecisaoService;

@Name("liberacaoPublicacaoAgendamento")
@Scope(ScopeType.EVENT)
@AutoCreate
public class LiberacaoPublicacaoAgendamento {
	
	public static String NOME = "liberacaoPublicacaoAgendamento";

	@Logger
	private Log log;
	
	@In
	private transient AgendaServicosQuartz agendaServicosQuartz;
	
	@In
	private LiberacaoPublicacaoDecisaoService liberacaoPublicacaoDecisaoService;
	
	public LiberacaoPublicacaoAgendamento() {
	}

	@Observer("org.jboss.seam.postInitialization")
	public void agendaServicos() throws Exception {
		Properties quartzProperties = ClassLoaderUtil.getProperties("seam.quartz.properties");
		String enabled = quartzProperties.getProperty("org.quartz.timer.enabled", "false");
		if (!"true".equals(enabled)) {
			return;
		}
		
		String cron = "0 0 2 * * ?"; //todos os dias 2 horas da manhã
		log.info("Iniciando job de liberacoes: {0}", cron);
		if(!agendaServicosQuartz.servicoAgendado("liberacaoPublicacaoAgendamento.execute()", cron)) {
			agendaServicosQuartz.agendarServico("liberacaoPublicacaoAgendamento", "execute", cron);
		}
		log.info("Job finalizados de liberacoes: {0}", cron);
	}
	
	@Asynchronous
	@Transactional
	public QuartzTriggerHandle execute(@IntervalCron String cron) {
		liberacaoPublicacaoDecisaoService.verificarControlePublicacaoAgendamento();
		return null;
	}
}
	