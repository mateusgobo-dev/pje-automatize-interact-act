package br.jus.cnj.pje.controleprazos.verificadorperiodico.passos;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import br.jus.cnj.pje.amqp.consumers.DecursoPrazoConsumer;
import br.jus.cnj.pje.amqp.model.dto.jobs.DecursoPrazoCloudEvent;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodicoComum;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoAlertaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisServiceImpl;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.cnj.pje.util.ControleTransactional;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.entidades.AMQPEvent;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.VerificadorPeriodicoLote;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.enums.VerificadorPeriodicoPassosEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(DecursoPrazo.NAME)
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class DecursoPrazo {

	public static final String NAME = "decursoPrazo";

	@Logger
	private Log log;

	@In(create = true, required = true)
	private ProcessoJudicialService processoJudicialService;

	@In(create = true, required = true)
	private VerificadorPeriodicoComum verificadorPeriodicoComum;

	@In(create = true, required = true)
	private ProcessoParteExpedienteManager processoParteExpedienteManager;

	@In(create = true, required = true)
	private ProcessoAlertaManager processoAlertaManager;

	@In(create = true, required = true)
	private PrazosProcessuaisService prazosProcessuaisService;

	@In(create = true, required = true)
	private DecursoPrazoConsumer decursoPrazoConsumer;

	public VerificadorPeriodicoLote run(boolean utilizaHoraAtual, Map<Integer, Calendario> mapaCalendarios) {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.REGISTRAR_DECURSO_PRAZO;

		log.info(
				"Localizando os prazos judiciais expirados e lançando a movimentação de decurso pertinente nos processos.");

		VerificadorPeriodicoLote verificadorPeriodicoLote = verificadorPeriodicoComum.insereRegistroRelatorio(passo);

		try {
			Integer numeroLimitadorExpedientesPendentes = verificadorPeriodicoComum.getNumeroLimitador(passo);

			if (numeroLimitadorExpedientesPendentes <= 0) {
				log.info("Limitando os expedientes em '" + passo.getLabel() + "' para '"
						+ numeroLimitadorExpedientesPendentes + "' itens a serem processados!");
			} else {
				Date data = utilizaHoraAtual ? new Date() : DateUtil.getBeginningOfToday();

				List<Integer> expedientes = processoParteExpedienteManager.getAtosComunicacaoExpiradosIds(data);

				if (expedientes.size() > numeroLimitadorExpedientesPendentes) {
					List<Integer> tmp = expedientes.subList(0, numeroLimitadorExpedientesPendentes);

					expedientes = tmp;

					log.info("Limitando os expedientes em '" + passo.getLabel() + "' para '"
							+ numeroLimitadorExpedientesPendentes + "' itens a serem processados!");
				}

				if (ConfiguracaoIntegracaoCloud.isRabbitJobDJEEnabled()) {
					this.runComControleDeLote(expedientes, data);
				} else {
					this.runLocal(expedientes, data, mapaCalendarios, true);
				}
			}

			verificadorPeriodicoLote.setProcessado(true);
		} catch (Exception e) {
			verificadorPeriodicoLote.setProcessado(false);

			String msg = String.format("Erro ao tentar recuperar os atos de comunicação expirados: [%s].",
					e.getLocalizedMessage());

			log.error(msg);

			log.error(e);
		}

		verificadorPeriodicoLote = verificadorPeriodicoComum.atualizaRegistroRelatorio(verificadorPeriodicoLote);

		log.info("Verificação dos prazos judiciais expirados finalizada.");

		return verificadorPeriodicoLote;
	}

	public List<Integer> runLocal(List<Integer> potenciaisExpedientesExpirados, Date data,
			Map<Integer, Calendario> mapaCalendarios, boolean processamentoBatch) {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.REGISTRAR_DECURSO_PRAZO;

		if (mapaCalendarios == null || mapaCalendarios.isEmpty()) {
			PrazosProcessuaisServiceImpl prazosProcessuaisService = ComponentUtil
					.getComponent(PrazosProcessuaisServiceImpl.class);

			mapaCalendarios = prazosProcessuaisService.obtemMapaCalendarios();
		}

		int cont = 0;

		List<Integer> expedientesProcessados = new ArrayList<>();
		List<Integer> expedientesProcessadosTmp = new ArrayList<>();

		ControleTransactional.beginTransactionAndClearJbpm();

		for (Integer expedienteId : potenciaisExpedientesExpirados) {
			try {
				log.info("Processando item em '" + passo.getLabel() + "'. [idProcessoParteExpediente: " + expedienteId
						+ "] [" + ++cont + "/" + potenciaisExpedientesExpirados.size() + "]");

				registrarDecursoPrazoLocal(expedienteId, data, false, mapaCalendarios);

				expedientesProcessadosTmp.add(expedienteId);

				if (ControleTransactional.verificarNecessidadeDeCommitAndClearJbpm(cont, processamentoBatch)) {
					expedientesProcessados.addAll(expedientesProcessadosTmp.stream().collect(Collectors.toList()));

					expedientesProcessadosTmp.clear();
				}
			} catch (Exception e) {
				ControleTransactional.rollbackTransaction();

				expedientesProcessadosTmp.clear();

				String texto = String.format("Não foi possível registrar o decurso de prazo para o expediente [%d].",
						expedienteId);

				log.error(texto);

				log.error(e);

				verificadorPeriodicoComum.incluirAlertaErroExpedientes(texto, CriticidadeAlertaEnum.C, expedienteId);

				ControleTransactional.beginTransactionAndClearJbpm();
			}
		}

		ControleTransactional.commitTransactionAndFlushAndClear();

		if (expedientesProcessadosTmp != null && expedientesProcessadosTmp.size() > 0) {
			expedientesProcessados.addAll(expedientesProcessadosTmp.stream().collect(Collectors.toList()));
		}

		return expedientesProcessados;
	}

	private void runComControleDeLote(List<Integer> idsProcessoParteExpediente, Date dataDecurso)
			throws PJeBusinessException {
		decursoPrazoConsumer.purgeQueue();

		String passo = VerificadorPeriodicoPassosEnum.REGISTRAR_DECURSO_PRAZO.getLabel();

		AMQPEventManager amqpManager = AMQPEventManager.instance();

		List<AMQPEvent> amqpEvents = new ArrayList<AMQPEvent>();

		UUID uuidLote = UUID.randomUUID();

		String uuidLoteString = uuidLote.toString();

		Integer tamanhoParticaoLote = ConfiguracaoIntegracaoCloud.getRabbitTamanhoParticaoLote();

		Collection<List<Integer>> idsProcessoParteExpedienteParticionado = verificadorPeriodicoComum
				.partitionBasedOnSize(idsProcessoParteExpediente, tamanhoParticaoLote);

		verificadorPeriodicoComum.insereLoteNaTabelaDeControle(idsProcessoParteExpedienteParticionado, passo, uuidLote);

		int numJob = 0;

		for (List<Integer> ids : idsProcessoParteExpedienteParticionado) {
			DecursoPrazoCloudEvent decursoPrazoCloudEvent = new DecursoPrazoCloudEvent(dataDecurso, ids, uuidLoteString,
					++numJob);

			amqpEvents.add(amqpManager.prepararMensagem(decursoPrazoCloudEvent, DecursoPrazoCloudEvent.class));
		}

		Integer tamanhoLote = idsProcessoParteExpedienteParticionado.size();

		try {
			verificadorPeriodicoComum.enviarMensagens(passo, amqpManager, amqpEvents, uuidLoteString, tamanhoLote);
		} catch (Exception e) {
			log.error(e);

			throw new PJeBusinessException(e.getMessage());
		}

		verificadorPeriodicoComum.aguardaProcessamentoPasso(passo, uuidLote, tamanhoLote);

		decursoPrazoConsumer.purgeQueue();
	}

	public void registrarDecursoPrazoLocal(Integer idAtoComunicacao, Date date, boolean force,
			Map<Integer, Calendario> mapaCalendarios) throws PJeRuntimeException {
		try {
			ProcessoParteExpediente ppe = verificadorPeriodicoComum.getAtoComunicacaoPessoal(idAtoComunicacao);

			if (ppe.getDtCienciaParte() == null || ppe.getDtPrazoLegal() == null || !date.after(ppe.getDtPrazoLegal()) || 
					(ppe.getResposta() != null && ppe.getRespostaIntempestiva() && !force && ppe.getFechado())) {
				return;
			}

			OrgaoJulgador orgaoJulgador = ppe.getProcessoJudicial().getOrgaoJulgador();

			if (orgaoJulgador == null) {
				String msg = String.format(
						"Não foi possível registrar o decurso de prazo para o expediente [%d], que tem como destinatário [%s], pois esse processo está sem órgão julgador.",
						ppe.getIdProcessoParteExpediente(), ppe.getPessoaParte());

				log.error(msg);

				throw new PJeRuntimeException(msg);
			}

			Calendario calendario = null;

			if (mapaCalendarios == null || mapaCalendarios.isEmpty()) {
				calendario = prazosProcessuaisService.obtemCalendario(orgaoJulgador);
			} else {
				calendario = mapaCalendarios.get(orgaoJulgador.getIdOrgaoJulgador());
			}

			if (calendario == null) {
				throw new PJeRuntimeException("Não há calendario de eventos para o órgão julgador.");
			}

			if (ppe.getDtPrazoLegal() != null && date.after(ppe.getDtPrazoLegal())) {
				// Recalcula a data final do término do prazo a fim de evitar o registro
				// indevido de decurso caso tenha surgido um novo evento de calendário
				Date fimPrazo = ppe.getDtPrazoLegal();

				if (ppe.getTipoPrazo() != TipoPrazoEnum.C && ppe.getPrazoLegal() != null) {
					fimPrazo = prazosProcessuaisService.calculaPrazoProcessual(ppe.getDtCienciaParte(),
							ppe.getPrazoLegal(), ppe.getTipoPrazo(), calendario,
							ppe.getProcessoJudicial().getCompetencia().getCategoriaPrazoProcessual(),
							ContagemPrazoEnum.M);

					if (fimPrazo != null) {
						ppe.setDtPrazoLegal(fimPrazo);
					}
				}
				if (fimPrazo != null && date.after(fimPrazo)) {
					SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

					ppe.setFechado(true);

					String pessoaParte = "";

					try {
						pessoaParte = ppe.getPessoaParte().toString();
					} catch (Exception e) {
						pessoaParte = "PESSOA PARTE COM ERRO";
					}

					MovimentoAutomaticoService.preencherMovimento()
							.deCodigo(CodigoMovimentoNacional.CODIGO_MOVIMENTO_EXPEDIENTE_DECURSO_PRAZO)
							.associarAoProcesso(ppe.getProcessoJudicial())
							.comComplementoDeNome(CodigoMovimentoNacional.NOME_COMPLEMENTO_NOME_PARTE)
							.preencherComTexto(pessoaParte)
							.comComplementoDeNome(CodigoMovimentoNacional.NOME_COMPLEMENTO_DATA)
							.preencherComTexto(formatter.format(fimPrazo)).lancarMovimento();

					processoJudicialService.sinalizaPreclusaoManifestacao(ppe.getProcessoJudicial());
					processoJudicialService.sinalizaEstouroPrazo(ppe.getProcessoJudicial());
				}
			}
		} catch (PJeBusinessException e) {
			String msg = String.format("Não foi possível registrar o decurso de prazo para o expediente de id [{0}]",
					idAtoComunicacao);

			log.error(msg);

			throw new PJeRuntimeException(msg);
		}
	}
}