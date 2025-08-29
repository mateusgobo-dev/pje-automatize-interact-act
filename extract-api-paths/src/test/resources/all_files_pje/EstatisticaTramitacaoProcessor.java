package br.com.infox.pje.processor;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.service.LogService;
import br.com.infox.pje.manager.EstatisticaEventoProcessoManager;
import br.com.infox.timer.TimerUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.EstatisticaEventoProcesso;
import br.jus.pje.nucleo.entidades.Evento;

/**
 * @author Daniel
 * 
 */
@Name(EstatisticaTramitacaoProcessor.NAME)
@AutoCreate
public class EstatisticaTramitacaoProcessor {

	public static final String NAME = "estatisticaTramitacaoProcessor";
	@In
	private EstatisticaEventoProcessoManager estatisticaEventoProcessoManager;
	@In
	private GenericManager genericManager;

	@In
	private LogService logService;

	@Logger
	private Log log;

	public static EstatisticaTramitacaoProcessor instance() {
		return (EstatisticaTramitacaoProcessor) Component.getInstance(NAME);
	}

	/**
	 * @param cron
	 * @return
	 */
	@Asynchronous
	@Transactional
	public QuartzTriggerHandle verificarProcessosTramitacao(@IntervalCron String cron) {
		// PJEII-4881  Tratamento de excecao para evitar que a aplicação nao inicie.
		try {
			verificarProcessosTramitacao();
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "verificarProcessosTramitacao");
		}
		return null;
	}
	
	private Object verificarProcessosTramitacao() {
		String cronName = null;
		try {
			cronName = TimerUtil.getParametro(EstatisticaTramitacaoStarterProcessor.ID_ESTATISCA_TRAMITACAO_PARAMETER);
		} catch (IllegalArgumentException e) {
		}
		QuartzTriggerHandle handle = new QuartzTriggerHandle(cronName);
		Trigger trigger = null;
		try {
			trigger = handle.getTrigger();
		} catch (SchedulerException e1) {
			e1.printStackTrace();
		}
		if (trigger != null) {
			Date now = trigger.getPreviousFireTime();
			Calendar previousMonthCal = Calendar.getInstance();
			previousMonthCal.setTime(now);
			previousMonthCal.add(Calendar.MONTH, -1);
			Date previousMonth = previousMonthCal.getTime();
			Evento arquivamentoDefinitivo = ParametroUtil.instance()
					.getEventoArquivamentoDefinitivoProcessual();
			Evento baixaDefinitiva = ParametroUtil.instance().getEventoBaixaDefinitivaProcessual();
			if (arquivamentoDefinitivo != null && baixaDefinitiva != null) {
				List<Object[]> ultimoEvento = estatisticaEventoProcessoManager.listUltimoEventoProcessos(previousMonth,
						now);
				for (Object[] o : ultimoEvento) {
					Integer id = Integer.parseInt(o[1].toString());
					EstatisticaEventoProcesso eep = EntityUtil.find(EstatisticaEventoProcesso.class, id);
					if (!arquivamentoDefinitivo.getCodEvento().equals(eep.getCodEvento())
							&& !baixaDefinitiva.getCodEvento().equals(eep.getCodEvento())) {
						EstatisticaEventoProcesso ee = new EstatisticaEventoProcesso();
						ee.setClasseJudicial(eep.getClasseJudicial());
						ee.setCodEstado(eep.getCodEstado());
						ee.setCodEvento(null);
						ee.setCompetencia(eep.getCompetencia());
						ee.setDataInclusao(now);
						ee.setDocumentoApelacao(eep.getDocumentoApelacao());
						ee.setDocumentoSentenca(eep.getDocumentoSentenca());
						ee.setJurisdicao(eep.getJurisdicao());
						ee.setOrgaoJulgador(eep.getOrgaoJulgador());
						ee.setIdProcessoTrf(eep.getIdProcessoTrf());
						genericManager.persist(ee);
					}
				}
			}
		}
		return null;
	}

}
