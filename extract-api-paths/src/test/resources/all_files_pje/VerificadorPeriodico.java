package br.jus.cnj.pje.controleprazos.verificadorperiodico;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzDispatcher;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.quartz.QuartzJobsInfo;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.CienciaAutomatica;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.CienciaAutomatizadaDiarioEletronico;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.DecursoPrazo;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.FecharPautaAutomaticamente;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.ProcessosAguardandoAudiencia;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.passos.ProsseguimentoSemPrazo;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisServiceImpl;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.cnj.pje.util.ControleTransactional;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.VerificadorPeriodicoLote;

@Name(VerificadorPeriodico.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class VerificadorPeriodico {

	public static final String NAME = "verificadorPeriodico";

	@Logger
	private Log log;

	@In(create = true, required = true)
	private VerificadorPeriodicoComum verificadorPeriodicoComum;

	@In(create = true, required = true)
	private ProcessosAguardandoAudiencia processosAguardandoAudiencia;

	@In(create = true, required = true)
	private CienciaAutomatica cienciaAutomatica;

	@In(create = true, required = true)
	private CienciaAutomatizadaDiarioEletronico cienciaAutomatizadaDiarioEletronico;

	@In(create = true, required = true)
	private DecursoPrazo decursoPrazo;

	@In(create = true, required = true)
	private ProsseguimentoSemPrazo prosseguimentoSemPrazo;

	@In(create = true, required = true)
	private FecharPautaAutomaticamente fecharPautaAutomaticamente;

	@In(create = true, required = true)
	private VerificadorPeriodicoAguardaProcessamentoPasso verificadorPeriodicoAguardaProcessamentoPasso;

	@Asynchronous
	public QuartzTriggerHandle execute(@IntervalCron String cron) {
		boolean utilizaHoraAtual = false;

		try {
			Scheduler scheduler = QuartzDispatcher.instance().getScheduler();

			utilizaHoraAtual = scheduler.getContext().getBoolean(QuartzJobsInfo.UTILIZA_HORA_ATUAL);

			scheduler.getContext().remove(QuartzJobsInfo.UTILIZA_HORA_ATUAL);
		} catch (SchedulerException e) {
			e.printStackTrace();
		} catch (ClassCastException cce) {
			utilizaHoraAtual = false;
		}

		realizaVerificacoesPrazos(utilizaHoraAtual);

		finalizaTransacaoJob();

		return null;
	}

	public void realizaVerificacoesPrazos(boolean utilizaHoraAtual) {
		Timer timerAguardaProcessamentoPasso = new Timer();

		if (ConfiguracaoIntegracaoCloud.isRabbitJobDJEEnabled()) {
			timerAguardaProcessamentoPasso.scheduleAtFixedRate(verificadorPeriodicoAguardaProcessamentoPasso, 0, 1000);
		}

		Usuario u = ParametroUtil.instance().getUsuarioSistema();

		if (u == null) {
			throw new RuntimeException(
					"Não foi possível localizar o usuário de sistema. Entre em contato com os administradores.");
		}

		Contexts.getEventContext().set("usuarioSistema", u);
		Contexts.getEventContext().set("ipOrigem", "localhost");
		Contexts.getEventContext().set("urlOrigem", "verificadorPeriodico");

		try {
			log.info("Verificação de prazos às " + new Date());

			Map<Integer, Calendario> mapaCalendarios = habilitarCacheCalculadorPrazo();

			VerificadorPeriodicoLote verificadorPeriodicoLotePAA = processosAguardandoAudiencia.run();

			VerificadorPeriodicoLote verificadorPeriodicoLoteCA = cienciaAutomatica.run(mapaCalendarios);

			VerificadorPeriodicoLote verificadorPeriodicoLoteCADE = cienciaAutomatizadaDiarioEletronico
					.run(mapaCalendarios);

			VerificadorPeriodicoLote verificadorPeriodicoLoteDP = decursoPrazo.run(utilizaHoraAtual, mapaCalendarios);

			VerificadorPeriodicoLote verificadorPeriodicoLotePSP = prosseguimentoSemPrazo.run();

			VerificadorPeriodicoLote verificadorPeriodicoLoteFPA = fecharPautaAutomaticamente.run();

			if (ConfiguracaoIntegracaoCloud.isRabbitJobDJEEnabled()) {
				enviarEmail(verificadorPeriodicoLotePAA, verificadorPeriodicoLoteCA, verificadorPeriodicoLoteCADE,
						verificadorPeriodicoLoteDP, verificadorPeriodicoLotePSP, verificadorPeriodicoLoteFPA);
			}
		} catch (Throwable t) {
			log.error("Erro ao executar verificacoes de prazos, mensagem interna: " + t.getMessage());

			t.printStackTrace();

			ComponentUtil.getComponent(PrazosProcessuaisServiceImpl.class).desabilitarCacheCalculadorPrazo();
		} finally {
			Contexts.getEventContext().set("usuarioSistema", null);

			log.info("Verificação de prazo concluída às [#0]", new Date());
		}

		timerAguardaProcessamentoPasso.cancel();
		timerAguardaProcessamentoPasso.purge();
	}

	private Map<Integer, Calendario> habilitarCacheCalculadorPrazo() {
		PrazosProcessuaisServiceImpl prazosProcessuaisService = ComponentUtil
				.getComponent(PrazosProcessuaisServiceImpl.class);

		Map<Integer, Calendario> mapaCalendarios = prazosProcessuaisService.obtemMapaCalendarios();

		prazosProcessuaisService.habilitarCacheCalculadorPrazo();

		return mapaCalendarios;
	}

	private void enviarEmail(VerificadorPeriodicoLote verificadorPeriodicoLotePAA,
			VerificadorPeriodicoLote verificadorPeriodicoLoteCA, VerificadorPeriodicoLote verificadorPeriodicoLoteCADE,
			VerificadorPeriodicoLote verificadorPeriodicoLoteDP, VerificadorPeriodicoLote verificadorPeriodicoLotePSP,
			VerificadorPeriodicoLote verificadorPeriodicoLoteFPA) {
		String parametroEmails = ComponentUtil.getComponent(ParametroService.class)
				.valueOf("verificadorPeriodicoEmails");

		if (parametroEmails != null) {
			List<String> emails = Arrays.asList(parametroEmails.split("\\s*,\\s*"));

			if (emails != null && !emails.isEmpty()) {
				verificadorPeriodicoComum.enviarEmail(Arrays.asList(verificadorPeriodicoLotePAA,
						verificadorPeriodicoLoteCA, verificadorPeriodicoLoteCADE, verificadorPeriodicoLoteDP,
						verificadorPeriodicoLotePSP, verificadorPeriodicoLoteFPA), emails, NAME);
			}
		}
	}

	private void finalizaTransacaoJob() {
		ControleTransactional.commitTransactionAndFlushAndClear();
	}
}