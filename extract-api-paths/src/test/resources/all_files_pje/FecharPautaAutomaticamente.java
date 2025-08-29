package br.jus.cnj.pje.controleprazos.verificadorperiodico.passos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.amqp.consumers.FecharPautaAutomaticamenteConsumer;
import br.jus.cnj.pje.amqp.model.dto.jobs.FecharPautaAutomaticamenteCloudEvent;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodicoComum;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.nucleo.manager.SessaoJulgamentoManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.SessaoJulgamentoService;
import br.jus.cnj.pje.util.ControleTransactional;
import br.jus.pje.nucleo.entidades.AMQPEvent;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.VerificadorPeriodicoLote;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;
import br.jus.pje.nucleo.enums.VerificadorPeriodicoPassosEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(FecharPautaAutomaticamente.NAME)
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class FecharPautaAutomaticamente {

	public static final String NAME = "fecharPautaAutomaticamente";

	@Logger
	private Log log;

	@In(create = true, required = true)
	private VerificadorPeriodicoComum verificadorPeriodicoComum;

	@In(create = true, required = true)
	private FecharPautaAutomaticamenteConsumer fecharPautaAutomaticamenteConsumer;

	public VerificadorPeriodicoLote run() {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.FECHAR_PAUTA_AUTOMATICAMENTE;

		log.info("Fechando pauta automaticamente");

		VerificadorPeriodicoLote verificadorPeriodicoLote = verificadorPeriodicoComum.insereRegistroRelatorio(passo);

		try {
			Integer numeroLimitadorExpedientesPendentes = verificadorPeriodicoComum.getNumeroLimitador(passo);

			if (numeroLimitadorExpedientesPendentes <= 0) {
				log.info("Limitando os expedientes em '" + passo.getLabel() + "' para '"
						+ numeroLimitadorExpedientesPendentes + "' itens a serem processados!");
			} else {
				Date hoje = DateUtil.getBeginningOfToday();

				SessaoJulgamentoManager sessaoJulgamentoManager = ComponentUtil
						.getComponent(SessaoJulgamentoManager.class);

				List<Integer> sessoes = sessaoJulgamentoManager.recuperaIdsPendentesFechamentoAutomaticoPauta(hoje);

				if (sessoes.size() > numeroLimitadorExpedientesPendentes) {
					List<Integer> tmp = sessoes.subList(0, numeroLimitadorExpedientesPendentes);

					sessoes = tmp;

					log.info("Limitando os expedientes em '" + passo.getLabel() + "' para '"
							+ numeroLimitadorExpedientesPendentes + "' itens a serem processados!");
				}

				if (ConfiguracaoIntegracaoCloud.isRabbitJobDJEEnabled()) {
					this.runComControleDeLote(sessoes);
				} else {
					this.runLocal(sessoes, true);
				}

				processaSessoesContinuas(hoje, sessaoJulgamentoManager);
			}

			verificadorPeriodicoLote.setProcessado(true);
		} catch (Exception e) {
			verificadorPeriodicoLote.setProcessado(false);

			String msg = String.format("Não foi possível fechar a pauta da sessão [%d].", e.getLocalizedMessage());

			log.error(msg);

			log.error(e);
		}

		verificadorPeriodicoLote = verificadorPeriodicoComum.atualizaRegistroRelatorio(verificadorPeriodicoLote);

		log.info("Fechamento de pauta automática concluído.");

		return verificadorPeriodicoLote;
	}

	public List<Integer> runLocal(List<Integer> sessoes, boolean processamentoBatch) {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.FECHAR_PAUTA_AUTOMATICAMENTE;

		SessaoJulgamentoService sessaoJulgamentoService = ComponentUtil.getComponent(SessaoJulgamentoService.class);
		ProcessoJudicialService processoJudicialService = ComponentUtil.getComponent(ProcessoJudicialService.class);

		Date hoje = DateUtil.getBeginningOfToday();

		ControleTransactional.beginTransactionAndClearJbpm();

		int cont = 0;

		List<Integer> sessoesProcessadas = new ArrayList<>();
		List<Integer> sessoesProcessadasTmp = new ArrayList<>();

		for (Integer sessaoId : sessoes) {
			try {
				log.info("Processando item em '" + passo.getLabel() + "'. [idSessão: " + sessaoId + "] [" + ++cont + "/"
						+ sessoes.size() + "]");

				sessaoJulgamentoService.fecharPauta(sessaoId, hoje, false);

				sessoesProcessadasTmp.add(sessaoId);

				if (ControleTransactional.verificarNecessidadeDeCommitAndClearJbpm(cont, true)) {
					sessoesProcessadas.addAll(sessoesProcessadasTmp.stream().collect(Collectors.toList()));

					sessoesProcessadasTmp.clear();
				}
			} catch (Exception e) {
				ControleTransactional.rollbackTransaction();

				sessoesProcessadasTmp.clear();

				String msg = String.format("Não foi possível fechar a pauta da sessão com identificador [%d].",
						sessaoId);

				log.error(msg);

				log.error(e);

				ControleTransactional.beginTransaction();

				try {
					processoJudicialService.incluirAlerta(msg, CriticidadeAlertaEnum.C, sessaoId);

					ControleTransactional.commitTransactionAndFlushAndClear();
				} catch (Exception e1) {
					ControleTransactional.rollbackTransaction();

					String msg2 = String.format("Erro ao tentar registrar alerta de falha de fechamento de sessão.\n"
							+ "Mensagem: [%s]\n" + "Expediente(s): [%d]\n" + "Erro: [%s].", msg, sessaoId,
							e1.getLocalizedMessage());

					log.error(msg2);

					log.error(e1);
				}

				ControleTransactional.beginTransactionAndClearJbpm();
			}
		}

		ControleTransactional.commitTransactionAndFlushAndClear();

		if (sessoesProcessadasTmp != null && sessoesProcessadasTmp.size() > 0) {
			sessoesProcessadas.addAll(sessoesProcessadasTmp.stream().collect(Collectors.toList()));
		}

		return sessoesProcessadas;
	}

	private void runComControleDeLote(List<Integer> sessoes) throws PJeBusinessException {
		fecharPautaAutomaticamenteConsumer.purgeQueue();

		String passo = VerificadorPeriodicoPassosEnum.FECHAR_PAUTA_AUTOMATICAMENTE.getLabel();

		AMQPEventManager amqpManager = AMQPEventManager.instance();

		List<AMQPEvent> amqpEvents = new ArrayList<AMQPEvent>();

		UUID uuidLote = UUID.randomUUID();

		String uuidLoteString = uuidLote.toString();

		Integer tamanhoParticaoLote = ConfiguracaoIntegracaoCloud.getRabbitTamanhoParticaoLote();

		Collection<List<Integer>> idsSessoes = verificadorPeriodicoComum.partitionBasedOnSize(sessoes,
				tamanhoParticaoLote);

		verificadorPeriodicoComum.insereLoteNaTabelaDeControle(idsSessoes, passo, uuidLote);

		int numJob = 0;

		for (List<Integer> ids : idsSessoes) {
			FecharPautaAutomaticamenteCloudEvent fecharPautaAutomaticamenteCloudEvent = new FecharPautaAutomaticamenteCloudEvent(
					ids, uuidLoteString, ++numJob);

			amqpEvents.add(amqpManager.prepararMensagem(fecharPautaAutomaticamenteCloudEvent,
					FecharPautaAutomaticamenteCloudEvent.class));
		}

		Integer tamanhoLote = idsSessoes.size();

		try {
			verificadorPeriodicoComum.enviarMensagens(passo, amqpManager, amqpEvents, uuidLoteString, tamanhoLote);
		} catch (Exception e) {
			log.error(e);

			throw new PJeBusinessException(e.getMessage());
		}

		verificadorPeriodicoComum.aguardaProcessamentoPasso(passo, uuidLote, tamanhoLote);

		fecharPautaAutomaticamenteConsumer.purgeQueue();
	}

	private void processaSessoesContinuas(Date hoje, SessaoJulgamentoManager sessaoJulgamentoManager) {
		ControleTransactional.beginTransactionAndClearJbpm();

		try {
			List<Sessao> sessoesContinuasPorDataInicio = sessaoJulgamentoManager.recuperaContinuasPorDataInicio(hoje);

			for (Sessao s : sessoesContinuasPorDataInicio) {
				s.setIniciar(true);
				s.setDataAberturaSessao(hoje);
				sessaoJulgamentoManager.persist(s);
			}

			Date ontem = DateUtil.dataMenosDias(hoje, 1);

			List<Sessao> sessoesContinuasPorDataFim = sessaoJulgamentoManager.recuperaContinuasPorDataFim(ontem);

			for (Sessao s : sessoesContinuasPorDataFim) {
				s.setIniciar(false);
				sessaoJulgamentoManager.persist(s);
			}

			sessaoJulgamentoManager.flush();
		} catch (Exception e2) {
			ControleTransactional.rollbackTransaction();

			String msg = String.format("Erro ao iniciar ou finalizar a sessão continua.\n" + "Erro: [%s].",
					e2.getLocalizedMessage());
			log.error(msg);
		}

		ControleTransactional.commitTransactionAndFlushAndClear();
	}
}