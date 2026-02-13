package br.jus.cnj.pje.controleprazos.verificadorperiodico.passos;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.amqp.consumers.CienciaAutomatizadaDiarioEletronicoConsumer;
import br.jus.cnj.pje.amqp.model.dto.jobs.CienciaAutomatizadaDiarioEletronicoCloudEvent;
import br.jus.cnj.pje.controleprazos.CodigosMateriaExpediente;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodicoComum;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.PublicadorDJE;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.nucleo.manager.AlertaManager;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.PublicacaoDiarioEletronicoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisServiceImpl;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.util.ControleTransactional;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.entidades.AMQPEvent;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Parametro;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.PublicacaoDiarioEletronico;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.VerificadorPeriodicoLote;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.SituacaoPublicacaoDiarioEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.enums.VerificadorPeriodicoPassosEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(CienciaAutomatizadaDiarioEletronico.NAME)
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class CienciaAutomatizadaDiarioEletronico {

	public static final String NAME = "cienciaAutomatizadaDiarioEletronico";

	@Logger
	private Log log;

	@In(create = true, required = true)
	private EventoManager eventoManager;

	@In(create = true, required = true)
	private AlertaManager alertaManager;

	@In(create = true, required = true)
	private ProcessoEventoManager processoEventoManager;

	@In(create = true, required = true)
	private VerificadorPeriodicoComum verificadorPeriodicoComum;

	@In(create = true, required = true)
	private PublicacaoDiarioEletronicoManager publicacaoDiarioEletronicoManager;

	@In(create = true, required = true)
	private ProcessoExpedienteManager processoExpedienteManager;

	@In(create = true, required = true)
	private ProcessoParteExpedienteManager processoParteExpedienteManager;

	@In(create = true, required = true)
	private ProcessoJudicialService processoJudicialService;

	@In(create = true, required = true)
	private PrazosProcessuaisService prazosProcessuaisService;

	@In(create = true, required = false)
	private PublicadorDJE publicadorDJE;

	@In(create = true, required = true)
	private ParametroService parametroService;

	@In(create = true, required = true)
	private CienciaAutomatizadaDiarioEletronicoConsumer cienciaAutomatizadaDiarioEletronicoConsumer;

	private Evento movimentoPublicacaoDJE;

	private Evento movimentoDisponibilizacaoDJE;

	public VerificadorPeriodicoLote run(Map<Integer, Calendario> mapaCalendarios) {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.CIENCIA_AUTOMATIZADA_DIARIO_ELETRONICO;

		log.info(
				"Execução da rotina do DJe para cálculo de prazos, lançando movimentação de disponibilização/publicação.");

		VerificadorPeriodicoLote verificadorPeriodicoLote = verificadorPeriodicoComum.insereRegistroRelatorio(passo);

		try {
			if (publicadorDJeDisponivel()) {
				boolean consultaPeloRecibo = runPorData(mapaCalendarios);

				runPorMateria(mapaCalendarios, consultaPeloRecibo);
			} else {
				log.error("Não foi encontrado o ponto de extensão do DJE.");
			}

			verificadorPeriodicoLote.setProcessado(true);
		} catch (Exception e) {
			verificadorPeriodicoLote.setProcessado(false);

			String msg = String.format(
					"Erro ao tentar recuperar os atos de comunicação pendentes de ciência DJE: [%s].", e.getMessage());

			log.error(msg);

			log.error(e);
		}

		verificadorPeriodicoLote = verificadorPeriodicoComum.atualizaRegistroRelatorio(verificadorPeriodicoLote);

		log.info("Verificação DJe finalizada.");

		return verificadorPeriodicoLote;
	}

	private boolean runPorData(Map<Integer, Calendario> mapaCalendarios) throws PJeBusinessException {
		// Obtem o ultimo dia que consultou as materias publicadas com sucesso
		Calendar calendarJob = Calendar.getInstance();

		calendarJob.setTime(getUltimaDataJobDiario());

		// Incrementa um dia ja que o ultimo ja foi executado com sucesso
		calendarJob.add(Calendar.DAY_OF_MONTH, 1);

		Calendar calendarAtual = Calendar.getInstance();

		int dataAtualInt = DateUtil.converteDataToYYYYMMDD(calendarAtual);
		int dataJobInt = DateUtil.converteDataToYYYYMMDD(calendarJob);

		// Executa ate o dia anterior da data atual, garantindo que independentemente
		// do horario do job, sempre traga todas as materias publicadas, ja que o
		// horario de
		// publicacao pode variar entre os diversos diarios da justica eletronica.
		// Nao executa a data atual, ja que dependendo do horario, as materias podem
		// ainda nao
		// terem sido publicadas.
		boolean consultaPeloRecibo = ParametroUtil.instance().isConsultarMateriasPeloReciboDePublicacaoDJE();

		while (dataJobInt < dataAtualInt) {
			if (ConfiguracaoIntegracaoCloud.isRabbitJobDJEEnabled()) {
				runPorDataComControleDeLote(calendarJob, consultaPeloRecibo, mapaCalendarios);
			} else {
				pesquisaPublicacoesDJEPorData(calendarJob, consultaPeloRecibo, mapaCalendarios);
			}

			atualizarDataExecucaoJob(calendarJob.getTime());

			calendarJob.add(Calendar.DAY_OF_MONTH, 1);

			dataJobInt = DateUtil.converteDataToYYYYMMDD(calendarJob);
		}

		return consultaPeloRecibo;
	}

	private void runPorMateria(Map<Integer, Calendario> mapaCalendarios, boolean consultaPeloRecibo)
			throws PJeBusinessException {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.CIENCIA_AUTOMATIZADA_DIARIO_ELETRONICO_POR_MATERIA;

		int cont = 0;

		Integer numeroLimitadorExpedientesPendentes = verificadorPeriodicoComum.getNumeroLimitador(passo);

		if (numeroLimitadorExpedientesPendentes <= 0) {
			log.info("Limitando os expedientes em '" + passo.getLabel() + "' para '"
					+ numeroLimitadorExpedientesPendentes + "' itens a serem processados!");
		} else {
			List<Integer> idsProcessoExpediente = recuperarIdsMateriaPendentes(consultaPeloRecibo);

			if (idsProcessoExpediente.size() > numeroLimitadorExpedientesPendentes) {
				List<Integer> tmp = idsProcessoExpediente.subList(0, numeroLimitadorExpedientesPendentes);

				idsProcessoExpediente = tmp;

				log.info("Limitando os expedientes em '" + passo.getLabel() + "' para '"
						+ numeroLimitadorExpedientesPendentes + "' itens a serem processados!");
			}

			if (ConfiguracaoIntegracaoCloud.isRabbitJobDJEEnabled()) {
				List<CodigosMateriaExpediente> codigoMateriaList = new ArrayList<CodigosMateriaExpediente>();

				for (Integer idProcessoExpediente : idsProcessoExpediente) {
					if (consultaPeloRecibo) {
						ProcessoExpediente processoExpediente = processoExpedienteManager
								.findById(idProcessoExpediente);

						codigoMateriaList.add(
								new CodigosMateriaExpediente(idProcessoExpediente, publicacaoDiarioEletronicoManager
										.recuperaRecibosDJeAguardandoPublicacao(processoExpediente)));
					} else {
						List<String> codigos = new ArrayList<String>();

						codigos.add(String.valueOf(idProcessoExpediente));

						codigoMateriaList.add(new CodigosMateriaExpediente(idProcessoExpediente, codigos));
					}
				}

				runPorMateriaComControleDeLote(idsProcessoExpediente, codigoMateriaList, consultaPeloRecibo);
			} else {
				Map<Integer, List<String>> codigoMateriaList = new HashMap<>();

				for (Integer idProcessoExpediente : idsProcessoExpediente) {
					log.info("Processando item em '" + passo.getLabel() + "'. [idProcessoExpediente: "
							+ idProcessoExpediente + "] [" + ++cont + "/" + idsProcessoExpediente.size() + "]");

					if (consultaPeloRecibo) {
						ProcessoExpediente processoExpediente = processoExpedienteManager
								.findById(idProcessoExpediente);

						codigoMateriaList.put(idProcessoExpediente, publicacaoDiarioEletronicoManager
								.recuperaRecibosDJeAguardandoPublicacao(processoExpediente));
					} else {
						List<String> codigos = new ArrayList<String>();

						codigos.add(String.valueOf(idProcessoExpediente));

						codigoMateriaList.put(idProcessoExpediente, codigos);
					}

					if (codigoMateriaList != null && codigoMateriaList.isEmpty() == false) {
						pesquisaPublicacoesDJEPorMateriaLocal(idProcessoExpediente,
								codigoMateriaList.get(idProcessoExpediente), mapaCalendarios);
					}
				}
			}
		}
	}

	private void runPorDataComControleDeLote(Calendar dataDisponibilizacao, boolean consultaPeloRecibo,
			Map<Integer, Calendario> mapaCalendarios) throws PJeBusinessException {
		try {
			List<String> reciboPublicacao = recuperarMateriasPublicadasDJePorData(dataDisponibilizacao);

			indicaVerificacaoPublicacoes();

			if (CollectionUtilsPje.isNotEmpty(reciboPublicacao)) {
				log.info("[DIÁRIO ELETRÔNICO] Encontradas " + reciboPublicacao.size() + " materias publicadas em: "
						+ DateUtil.dateHourToString(dataDisponibilizacao.getTime()));

				List<Integer> idsProcessoParteExpediente = processoParteExpedienteManager
						.recuperaProcessoParteExpedientePorMateriaPublicadaDJEIds(consultaPeloRecibo,
								reciboPublicacao.stream().map(Integer::parseInt).collect(Collectors.toList()));

				if (CollectionUtilsPje.isNotEmpty(idsProcessoParteExpediente)) {
					runPorDataComControleDeLote(idsProcessoParteExpediente, dataDisponibilizacao);
				}
			} else {
				log.info("[DIÁRIO ELETRÔNICO] Nenhuma materia encontrada publicada em: "
						+ DateUtil.dateHourToString(dataDisponibilizacao.getTime()));
			}
		} catch (Exception e) {
			log.error(e);

			throw new PJeBusinessException(e.getMessage());
		}
	}

	private void runPorDataComControleDeLote(List<Integer> idsProcessoParteExpediente, Calendar dataDisponibilizacao)
			throws PJeBusinessException {
		cienciaAutomatizadaDiarioEletronicoConsumer.purgeQueue();

		final String passo = VerificadorPeriodicoPassosEnum.CIENCIA_AUTOMATIZADA_DIARIO_ELETRONICO_POR_DATA.getLabel();

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
			CienciaAutomatizadaDiarioEletronicoCloudEvent atualizarMateriasPublicadasDJECloudEvent = new CienciaAutomatizadaDiarioEletronicoCloudEvent(
					ids, dataDisponibilizacao, uuidLoteString, ++numJob);

			amqpEvents.add(amqpManager.prepararMensagem(atualizarMateriasPublicadasDJECloudEvent,
					CienciaAutomatizadaDiarioEletronicoCloudEvent.class));
		}

		Integer tamanhoLote = idsProcessoParteExpedienteParticionado.size();

		try {
			verificadorPeriodicoComum.enviarMensagens(passo, amqpManager, amqpEvents, uuidLoteString, tamanhoLote);
		} catch (Exception e) {
			log.error(e);

			throw new PJeBusinessException(e.getMessage());
		}

		verificadorPeriodicoComum.aguardaProcessamentoPasso(passo, uuidLote, tamanhoLote);

		cienciaAutomatizadaDiarioEletronicoConsumer.purgeQueue();
	}

	private void runPorMateriaComControleDeLote(List<Integer> idsProcessoExpediente,
			List<CodigosMateriaExpediente> codigoMateriaList, boolean consultaPeloRecibo) throws PJeBusinessException {
		cienciaAutomatizadaDiarioEletronicoConsumer.purgeQueue();

		final String passo = VerificadorPeriodicoPassosEnum.CIENCIA_AUTOMATIZADA_DIARIO_ELETRONICO_POR_MATERIA
				.getLabel();

		AMQPEventManager amqpManager = AMQPEventManager.instance();

		List<AMQPEvent> amqpEvents = new ArrayList<AMQPEvent>();

		UUID uuidLote = UUID.randomUUID();

		String uuidLoteString = uuidLote.toString();

		Integer tamanhoParticaoLote = ConfiguracaoIntegracaoCloud.getRabbitTamanhoParticaoLote();

		Collection<List<Integer>> idsProcessoExpedienteParticionado = verificadorPeriodicoComum
				.partitionBasedOnSize(idsProcessoExpediente, tamanhoParticaoLote);

		verificadorPeriodicoComum.insereLoteNaTabelaDeControle(idsProcessoExpedienteParticionado, passo, uuidLote);

		int numJob = 0;

		for (List<Integer> ids : idsProcessoExpedienteParticionado) {
			CienciaAutomatizadaDiarioEletronicoCloudEvent pesquisaMateriasPublicadas = new CienciaAutomatizadaDiarioEletronicoCloudEvent(
					ids, codigoMateriaList, consultaPeloRecibo, uuidLoteString, ++numJob);

			amqpEvents.add(amqpManager.prepararMensagem(pesquisaMateriasPublicadas,
					CienciaAutomatizadaDiarioEletronicoCloudEvent.class));
		}

		Integer tamanhoLote = idsProcessoExpedienteParticionado.size();

		try {
			verificadorPeriodicoComum.enviarMensagens(passo, amqpManager, amqpEvents, uuidLoteString, tamanhoLote);
		} catch (Exception e) {
			log.error(e);

			throw new PJeBusinessException(e.getMessage());
		}

		verificadorPeriodicoComum.aguardaProcessamentoPasso(passo, uuidLote, tamanhoLote);

		cienciaAutomatizadaDiarioEletronicoConsumer.purgeQueue();
	}

	private void atualizarDataExecucaoJob(Date data) {
		try {
			log.info("Atualizando data de execução do job. Data de execução: {0} ", data);

			ControleTransactional.beginTransaction();

			Parametro parametro = parametroService.findByName("ultimaDataJobDiario");

			if (parametro == null) {
				parametro = new Parametro();
				parametro.setAtivo(Boolean.TRUE);
				parametro.setSistema(Boolean.TRUE);
				parametro.setDataAtualizacao(new Date());
				parametro.setDescricaoVariavel("Data da última execução do Job");
				parametro.setNomeVariavel("ultimaDataJobDiario");
				parametro.setUsuarioModificacao(ParametroUtil.instance().getUsuarioSistema());
			}

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

			parametro.setValorVariavel(formatter.format(data));

			if (parametro.getIdParametro() == 0) {
				EntityUtil.getEntityManager().persist(parametro);
			} else {
				EntityUtil.getEntityManager().merge(parametro);
			}

			EntityUtil.getEntityManager().flush();

			ControleTransactional.commitTransactionAndFlushAndClear();
		} catch (Exception e) {
			ControleTransactional.rollbackTransaction();

			log.error("Erro ao tentar gravar a data de execução do Job. Data de execução: {0}", e, data);
		}
	}

	public boolean publicadorDJeDisponivel() {
		return publicadorDJE != null;
	}

	public Date getUltimaDataJobDiario() throws PJeBusinessException {
		String ultimaDataJobDiario = "2020-01-01";

		Parametro parametro = parametroService.findByName("ultimaDataJobDiario");

		if (parametro != null && StringUtils.isNotBlank(parametro.getValorVariavel())) {
			ultimaDataJobDiario = parametro.getValorVariavel();
		}

		Date data = null;

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		try {
			data = formatter.parse(ultimaDataJobDiario);
		} catch (ParseException ex1) {
			try {
				formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

				data = formatter.parse(ultimaDataJobDiario);
			} catch (ParseException e) {
				log.error("Erro ao buscar última data de execução do job", e);

				throw new PJeBusinessException(e.getMessage());
			}
		}

		return data;
	}

	private void pesquisaPublicacoesDJEPorData(Calendar dataDisponibilizacao, boolean consultaPeloRecibo,
			Map<Integer, Calendario> mapaCalendarios) throws PJeBusinessException {
		try {
			List<String> reciboPublicacao = recuperarMateriasPublicadasDJePorData(dataDisponibilizacao);

			indicaVerificacaoPublicacoes();

			if (CollectionUtilsPje.isNotEmpty(reciboPublicacao)) {
				log.info("[DIÁRIO ELETRÔNICO] Encontradas " + reciboPublicacao.size() + " materias publicadas em: "
						+ DateUtil.dateHourToString(dataDisponibilizacao.getTime()));

				List<Integer> idsProcessoParteExpediente = processoParteExpedienteManager
						.recuperaProcessoParteExpedientePorMateriaPublicadaDJEIds(consultaPeloRecibo,
								reciboPublicacao.stream().map(Integer::parseInt).collect(Collectors.toList()));

				if (CollectionUtilsPje.isNotEmpty(idsProcessoParteExpediente)) {
					atualizarMateriasPublicadasDJELocal(idsProcessoParteExpediente, dataDisponibilizacao,
							mapaCalendarios);
				}
			} else {
				log.info("[DIÁRIO ELETRÔNICO] Nenhuma materia encontrada publicada em: "
						+ DateUtil.dateHourToString(dataDisponibilizacao.getTime()));
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	private List<Integer> recuperarIdsMateriaPendentes(boolean restringirComReciboDJE) throws PJeBusinessException {
		return processoExpedienteManager.recuperarIdentificadorExpedientePendentes(restringirComReciboDJE);
	}

	public List<Integer> pesquisaPublicacoesDJEPorMateriaLocal(Integer idProcessoExpediente,
			List<String> codigoMateriaList, Map<Integer, Calendario> mapaCalendarios) throws PJeBusinessException {
		Calendar dtDisponibilizacao = null;

		boolean jaIndicouVerificacao = false;

		for (String codigoMateria : codigoMateriaList) {
			try {
				dtDisponibilizacao = recuperarDataDisponibilizacao(codigoMateria);

				if (!jaIndicouVerificacao) {
					indicaVerificacaoPublicacao(idProcessoExpediente);
				}

				jaIndicouVerificacao = true;

				if (dtDisponibilizacao != null) {
					break;
				}
			} catch (Exception e) {
				log.error("[DIÁRIO ELETRÔNICO] Erro ao atualizar matéria independente da data. Id matéria: {0} {1}",
						codigoMateria, e.getLocalizedMessage());
			}
		}

		if (dtDisponibilizacao != null) {
			List<Integer> idsProcessoParteExpediente = processoParteExpedienteManager
					.getIdsProcessoParteExpedienteByIdProcessoExpediente(idProcessoExpediente);

			List<Integer> expedientesProcessados = atualizarMateriasPublicadasDJELocal(idsProcessoParteExpediente,
					dtDisponibilizacao, mapaCalendarios);

			if (expedientesProcessados.size() == idsProcessoParteExpediente.size()) {
				return Arrays.asList(idProcessoExpediente);
			}
		} else {
			sinalizaPendenciaPublicacao(idProcessoExpediente);

			return Arrays.asList(idProcessoExpediente);
		}

		return Collections.emptyList();
	}

	private void sinalizaPendenciaPublicacao(Integer idProcessoExpediente) throws PJeBusinessException {
		try {
			ControleTransactional.beginTransaction();

			publicacaoDiarioEletronicoManager.sinalizaPendenciaPublicacao(idProcessoExpediente);

			ControleTransactional.commitTransactionAndFlushAndClear();
		} catch (Exception e) {
			ControleTransactional.rollbackTransaction();

			log.error(e);

			throw new PJeBusinessException(e.getMessage());
		}
	}

	private void indicaVerificacaoPublicacoes() throws PJeBusinessException {
		try {
			ControleTransactional.beginTransaction();

			publicacaoDiarioEletronicoManager.indicaVerificacaoPublicacoes(DateUtil.getBeginningOfTodayCalendar());

			ControleTransactional.commitTransactionAndFlushAndClear();
		} catch (Exception e) {
			ControleTransactional.rollbackTransaction();

			log.error(e);

			throw new PJeBusinessException(e.getMessage());
		}
	}

	private void indicaVerificacaoPublicacao(Integer idProcessoExpediente) throws PJeBusinessException {
		try {
			ControleTransactional.beginTransaction();

			publicacaoDiarioEletronicoManager.indicaVerificacaoPublicacao(idProcessoExpediente);

			ControleTransactional.commitTransactionAndFlushAndClear();
		} catch (Exception e) {
			ControleTransactional.rollbackTransaction();

			log.error(e);

			throw new PJeBusinessException(e.getMessage());
		}
	}

	private List<String> recuperarMateriasPublicadasDJePorData(Calendar calendar) throws PJeBusinessException {
		if (publicadorDJE != null) {
			try {
				return publicadorDJE.verificaPublicacoesPorData(calendar);
			} catch (PontoExtensaoException e) {
				log.error(e);

				throw new PJeBusinessException(e.getMessage());
			}
		}

		return Collections.emptyList();
	}

	public List<Integer> atualizarMateriasPublicadasDJELocal(List<Integer> idsProcessoParteExpediente,
			Calendar dataDisponibilizacao, Map<Integer, Calendario> mapaCalendarios) throws PJeBusinessException {
		if (mapaCalendarios == null || mapaCalendarios.isEmpty()) {
			prazosProcessuaisService = ComponentUtil.getComponent(PrazosProcessuaisServiceImpl.class);

			mapaCalendarios = prazosProcessuaisService.obtemMapaCalendarios();
		}

		int cont = 0;

		List<Integer> expedientesProcessados = new ArrayList<>();
		List<Integer> expedientesProcessadosTmp = new ArrayList<>();

		ControleTransactional.beginTransactionAndClearJbpm();

		for (Integer processoParteExpedienteId : idsProcessoParteExpediente) {
			try {
				log.info(
						"[DIÁRIO ELETRÔNICO] Processando item em 'atualizarMateriasPublicadasDJE'. [idProcessoParteExpediente: "
								+ processoParteExpedienteId + "] [" + ++cont + "/" + idsProcessoParteExpediente.size()
								+ "]");

				lancarMovimentosEAtualizarMateria(
						verificadorPeriodicoComum.getAtoComunicacaoPessoal(processoParteExpedienteId),
						dataDisponibilizacao.getTime(), mapaCalendarios);

				expedientesProcessadosTmp.add(processoParteExpedienteId);

				if (ControleTransactional.verificarNecessidadeDeCommitAndClearJbpm(cont, true)) {
					expedientesProcessados.addAll(expedientesProcessadosTmp.stream().collect(Collectors.toList()));

					expedientesProcessadosTmp.clear();
				}
			} catch (Exception e) {
				ControleTransactional.rollbackTransaction();

				expedientesProcessadosTmp.clear();

				log.error(
						"[DIÁRIO ELETRÔNICO] Erro ao processar item em 'atualizarMateriasPublicadasDJE'. [idProcessoParteExpediente: "
								+ processoParteExpedienteId + "]");

				log.error(e);

				ControleTransactional.beginTransaction();
			}
		}

		ControleTransactional.commitTransactionAndFlushAndClear();

		if (expedientesProcessadosTmp != null && expedientesProcessadosTmp.size() > 0) {
			expedientesProcessados.addAll(expedientesProcessadosTmp.stream().collect(Collectors.toList()));
		}

		return expedientesProcessados;
	}

	private Calendar recuperarDataDisponibilizacao(String idMateria) throws PJeBusinessException {
		if (publicadorDJE != null) {
			try {
				return publicadorDJE.verificaPublicacao(idMateria);
			} catch (PontoExtensaoException e) {
				log.error(e);

				throw new PJeBusinessException(e.getMessage());
			}
		}

		return null;
	}

	private void lancarMovimentosEAtualizarMateria(ProcessoParteExpediente ppe, Date dataDisponibilizacao,
			Map<Integer, Calendario> mapaCalendarios) throws PJeBusinessException {
		PublicacaoDiarioEletronico publicacaoDJE = publicacaoDiarioEletronicoManager.getPublicacao(ppe);

		if (publicacaoDJE != null && publicacaoDJE.getSituacao() == SituacaoPublicacaoDiarioEnum.P) {
			return; // a matéria já está publicada no DJE
		}

		if (ppe.getPessoaParte() != null) {
			ppe.getPessoaParte().getNome(); // Fora a carga de PessoaParteExpediente.pessoaParte. Do contrrio, em
											// decorrncia de um lazy loading provacado pelo listener de Pessoa
											// durante o flush, haver um erro...
		}

		Date dataAtual = new Date();

		Date dataCriacaoExpediente = ppe.getProcessoExpediente().getDtCriacao();

		int idProcessoParteExpediente = ppe.getIdProcessoParteExpediente();

		if (DateUtil.isDataMenorIgual(dataDisponibilizacao, dataAtual)) {
			Calendario calendario = null;

			OrgaoJulgador orgaoJulgador = ppe.getProcessoJudicial().getOrgaoJulgador();

			if (orgaoJulgador == null) {
				String msg = String.format(
						"Não foi possível lancar movimentos e atualizar a materia para o expediente [%d], que tem como destinatário [%s], pois esse processo está sem órgão julgador.",
						ppe.getIdProcessoParteExpediente(), ppe.getPessoaParte());

				log.error(msg);

				throw new PJeRuntimeException(msg);
			}

			if (mapaCalendarios == null || mapaCalendarios.isEmpty()) {
				calendario = prazosProcessuaisService.obtemCalendario(orgaoJulgador);
			} else {
				calendario = mapaCalendarios.get(orgaoJulgador.getIdOrgaoJulgador());
			}

			Date dataPublicacao = prazosProcessuaisService.obtemDiaUtilSeguinte(dataDisponibilizacao, calendario,
					false);

			if (dataCriacaoExpediente.before(dataPublicacao)) {
				boolean lancarMovimentoDisponibilizacaoDJE = !processoEventoManager.temMovimento(
						ppe.getProcessoJudicial(), getMovimentoDisponibilizacaoDJE(),
						DateUtil.getDataSemHora(dataDisponibilizacao), ppe.getProcessoDocumento());

				if (lancarMovimentoDisponibilizacaoDJE) {
					try {
						lancarMovimentoDisponibilizacaoDiario(ppe.getProcessoExpediente(), dataDisponibilizacao);
					} catch (Exception e) {
						log.error(
								"[DIÁRIO ELETRÔNICO] Erro ao registrar movimento de disponibilização "
										+ "para o processoParteExpediente com id [{0}]: [{1}]",
								idProcessoParteExpediente, e.getLocalizedMessage());

						log.error(e);

						throw new PJeBusinessException(e.getMessage());
					}
				}

				if (DateUtil.isDataMenorIgual(dataPublicacao, dataAtual)) {
					boolean lancarMovimentoPublicacaoDJE = !processoEventoManager.temMovimento(
							ppe.getProcessoJudicial(), getMovimentoPublicacaoDJE(),
							DateUtil.getDataSemHora(dataDisponibilizacao), ppe.getProcessoDocumento());

					try {
						boolean atualizarMateria = isPrecisaAtualizarMateria(ppe, dataPublicacao);

						if (atualizarMateria) {
							atualizaMateria(ppe, dataPublicacao, mapaCalendarios);
						}

						if (lancarMovimentoPublicacaoDJE) {
							lancarMovimentoPublicacaoDiario(ppe.getProcessoExpediente(), dataPublicacao);
						}
					} catch (Exception e) {
						log.error(
								"[DIÁRIO ELETRÔNICO] Erro ao atualizar data de ciência e movimentação de publicação "
										+ "para o processoParteExpediente com id [{0}]: [{1}]",
								idProcessoParteExpediente, e.getLocalizedMessage());

						log.error(e);

						throw new PJeBusinessException(e.getMessage());
					}
				} else {
					log.warn(
							"[DIÁRIO ELETRÔNICO] Data de publicação [{0}] posterior à data de execução do job [{1}] "
									+ "para o processoParteExpediente com id [{2}]. "
									+ "Não foi lançado o movimento de publicação e a materia não foi atualizada.",
							dataPublicacao, dataAtual, idProcessoParteExpediente);
				}
			} else {
				log.error("[DIÁRIO ELETRÔNICO] Data de publicação [{0}] anterior à data de criação do expediente [{1}] "
						+ "para o processoParteExpediente com id [{2}]. "
						+ "Não foi lançado o movimento de publicação e de disponibilização e a materia não foi atualizada.",
						dataPublicacao, dataCriacaoExpediente, idProcessoParteExpediente);
			}
		} else {
			log.error("[DIÁRIO ELETRÔNICO] Data de disponibilização [{0}] posterior à data de execução do job [{1}] "
					+ "para o processoParteExpediente com id [{2}]. "
					+ "Não foi lançado o movimento de publicação e de disponibilização e a materia não foi atualizada.",
					dataDisponibilizacao, dataAtual, idProcessoParteExpediente);
		}
	}

	/**
	 * Registra as datas de ciência e prazo legal da matéria.
	 * 
	 * @param idMateria
	 * @param dataPublicacao
	 * @param mapaCalendarios Um mapa de calendarios dos orgaos julgadores
	 * @throws PJeBusinessException
	 */
	private void atualizaMateria(ProcessoParteExpediente ppe, Date dataPublicacao,
			Map<Integer, Calendario> mapaCalendarios) throws PJeBusinessException {
		OrgaoJulgador orgaoJulgador = ppe.getProcessoExpediente().getProcessoTrf().getOrgaoJulgador();

		Calendario calendario = null;

		if (mapaCalendarios == null || mapaCalendarios.isEmpty()) {
			calendario = prazosProcessuaisService.obtemCalendario(orgaoJulgador);
		} else {
			calendario = mapaCalendarios.get(orgaoJulgador.getIdOrgaoJulgador());
		}

		Integer idProcessoParteExpeidente = ppe.getIdProcessoParteExpediente();

		// Atualiza apenas os processos que nao possuem data de ciencia
		if (ppe.getDtCienciaParte() == null) {
			ppe.setDtCienciaParte(DateUtil.getDataSemHora(dataPublicacao));

			ppe.setCienciaSistema(true);

			log.info("[DIÁRIO ELETRÔNICO] Atualiza data de ciência do processoParteExpediente com id [{0}] para [{1}]",
					idProcessoParteExpeidente, DateUtil.getDataSemHora(dataPublicacao));
		}

		if (ppe.getPrazoLegal() != null && ppe.getPrazoLegal() != 0 && ppe.getTipoPrazo() != TipoPrazoEnum.S
				&& ppe.getTipoPrazo() != TipoPrazoEnum.C) {
			Date fimPrazo = prazosProcessuaisService.calculaPrazoProcessual(ppe.getDtCienciaParte(),
					ppe.getPrazoLegal(), ppe.getTipoPrazo(), calendario,
					ppe.getProcessoJudicial().getCompetencia().getCategoriaPrazoProcessual(), ContagemPrazoEnum.M);

			ppe.setDtPrazoLegal(fimPrazo);

			log.info(
					"[DIÁRIO ELETRÔNICO] Atualiza data de prazo legal do processoParteExpediente com id [{0}] para [{1}]",
					idProcessoParteExpeidente, DateUtil.getDataFormatada(fimPrazo, "dd/MM/yyyy HH:mm:ss"));
		}

		// atualiza os metadados da publicacao
		PublicacaoDiarioEletronico publicacaoDJE = publicacaoDiarioEletronicoManager.getPublicacao(ppe);

		publicacaoDJE.setSituacao(SituacaoPublicacaoDiarioEnum.P);

		publicacaoDJE.setDtPublicacao(dataPublicacao);

		EntityManager entityManager = EntityUtil.getEntityManager();

		entityManager.merge(ppe);
		entityManager.merge(publicacaoDJE);
		entityManager.flush();
	}

	private boolean isPrecisaAtualizarMateria(ProcessoParteExpediente processoParteExpediente, Date dataPublicacao) {
		return (processoParteExpediente.getDtCienciaParte() == null
				|| (processoParteExpediente.getDtPrazoLegal() == null && processoParteExpediente.getPrazoLegal() != null
						&& processoParteExpediente.getPrazoLegal() != 0
						&& processoParteExpediente.getTipoPrazo() != TipoPrazoEnum.S
						&& processoParteExpediente.getTipoPrazo() != TipoPrazoEnum.C));
	}

	/**
	 * Efetua o lançamento do movimento de publicação no DJE para o
	 * {@link ProcessoExpediente} informado. A data de publicação informada será o
	 * complemento do movimento.
	 *
	 * @param processoExpediente {@link ProcessoExpediente}.
	 * @param dataPublicacao     Data de publicação.
	 */
	private void lancarMovimentoPublicacaoDiario(ProcessoExpediente processoExpediente, Date dataPublicacao) {
		if (processoExpediente.getDtCriacao() != null && dataPublicacao != null) {
			MovimentoAutomaticoService.preencherMovimento().deCodigo(getMovimentoPublicacaoDJE().getCodEvento())
					.associarAoProcesso(processoExpediente.getProcessoTrf().getProcesso())
					.associarAoDocumento(processoExpediente.getProcessoDocumento()).comProximoComplementoVazio()
					.preencherComObjeto(processoExpediente.getTipoProcessoDocumento()).comProximoComplementoVazio()
					.doTipoLivre().preencherComTexto(new SimpleDateFormat("dd/MM/yyyy").format(dataPublicacao))
					.lancarMovimento();

			processoJudicialService.sinalizaPublicacaoDJE(processoExpediente.getProcessoTrf());

			log.info("[DIÁRIO ELETRÔNICO] Lançado movimento de publicação para documento de id: [{0}] ",
					processoExpediente.getProcessoDocumento().getIdProcessoDocumento());
		}
	}

	private Evento getMovimentoDisponibilizacaoDJE() {
		if (movimentoDisponibilizacaoDJE == null) {
			String codMovimentoDisponibilizacaoDiario = CodigoMovimentoNacional.CODIGO_MOVIMENTO_DJE_DISPONIBILIZACAO;

			movimentoDisponibilizacaoDJE = eventoManager.findByCodigoCNJ(codMovimentoDisponibilizacaoDiario);
		}

		return movimentoDisponibilizacaoDJE;
	}

	private Evento getMovimentoPublicacaoDJE() {
		if (movimentoPublicacaoDJE == null) {
			String codMovimentoPublicacaoDiario = CodigoMovimentoNacional.CODIGO_MOVIMENTO_DJE_PUBLICACAO;

			movimentoPublicacaoDJE = eventoManager.findByCodigoCNJ(codMovimentoPublicacaoDiario);
		}

		return movimentoPublicacaoDJE;
	}

	/**
	 * Efetua o lançamento do movimento de publicação no DJE para o
	 * {@link ProcessoExpediente} informado. A data de dataDisponibilizacao
	 * informada será o complemento do movimento.
	 *
	 * @param processoExpediente   {@link ProcessoExpediente}.
	 * @param dataDisponibilizacao Data de disponibilização.
	 */
	private void lancarMovimentoDisponibilizacaoDiario(ProcessoExpediente processoExpediente,
			Date dataDisponibilizacao) {
		MovimentoAutomaticoService.preencherMovimento().deCodigo(getMovimentoDisponibilizacaoDJE().getCodEvento())
				.associarAoProcesso(processoExpediente.getProcessoTrf().getProcesso())
				.associarAoDocumento(processoExpediente.getProcessoDocumento()).comProximoComplementoVazio()
				.preencherComTexto(new SimpleDateFormat("dd/MM/yyyy")
						.format(dataDisponibilizacao == null ? new Date() : dataDisponibilizacao))
				.comProximoComplementoVazio().preencherComObjeto(processoExpediente.getTipoProcessoDocumento())
				.lancarMovimento();

		processoJudicialService.sinalizaDisponibilizacaoDJE(processoExpediente.getProcessoTrf());

		log.info("[DIÁRIO ELETRÔNICO] Lançado movimento de disponibilização para documento de id: [{0}] ",
				processoExpediente.getProcessoDocumento().getIdProcessoDocumento());
	}

	public void intimarEletronicamenteDJe(ProcessoExpediente processoExpediente) throws PJeBusinessException {
		if (processoExpediente != null) {
			List<ProcessoParteExpediente> expedientes = processoExpediente.getProcessoParteExpedienteList();

			PublicacaoDiarioEletronicoManager publicacaoDiarioEletronicoManager = ComponentUtil
					.getComponent(PublicacaoDiarioEletronicoManager.class);

			try {
				for (ProcessoParteExpediente ppe : expedientes) {
					PublicacaoDiarioEletronico publicacaoDJE = publicacaoDiarioEletronicoManager.getPublicacao(ppe);

					ProcessoDocumentoBin doc = processoExpediente.getProcessoDocumento().getProcessoDocumentoBin();

					byte[] documento = doc.isBinario() ? doc.getProcessoDocumento()
							: doc.getModeloDocumento().getBytes();

					OrgaoJulgador orgaoJulgador = ppe.getProcessoJudicial().getOrgaoJulgador();

					if (orgaoJulgador == null) {
						String msg = String.format(
								"Não foi possível intimar eletronicamente via DJe para o expediente [%d], que tem como destinatário [%s], pois esse processo está sem órgão julgador.",
								ppe.getIdProcessoParteExpediente(), ppe.getPessoaParte());

						log.error(msg);

						throw new PJeRuntimeException(msg);
					}

					String idOrgaoJulgador = Integer.toString(orgaoJulgador.getIdOrgaoJulgador());

					String reciboPublicacao = publicadorDJE.publicar(idOrgaoJulgador, ppe.getPessoaParte().getNome(),
							ppe.getPessoaParte().getDocumentoCpfCnpj(), documento,
							ppe.getProcessoJudicial().getIdProcessoTrf(), ppe.getIdProcessoParteExpediente());

					publicacaoDJE.setReciboPublicacaoDiarioEletronico(reciboPublicacao);

					publicacaoDiarioEletronicoManager.merge(publicacaoDJE);
					publicacaoDiarioEletronicoManager.flush();

					processoParteExpedienteManager.merge(ppe);
					processoParteExpedienteManager.flush();
				}
			} catch (PontoExtensaoException e) {
				log.error("Ocorreu um erro ao tentar publicar os documentos no DJE: \n {0}", e.getMessage());

				throw new PJeBusinessException(e.getMessage());
			}
		}
	}

	public void cienciaAutomatizadaDiarioEletronico() {
		try {
			// Obtem o ultimo dia que consultou as materias publicadas com sucesso
			Calendar execucaoJob = Calendar.getInstance();

			execucaoJob.setTime(getUltimaDataJobDiario());

			// Incrementa um dia ja que o ultimo ja foi executado com sucesso
			execucaoJob.add(Calendar.DAY_OF_MONTH, 1);

			// Obtem a data atual
			Calendar dataAtual = Calendar.getInstance();

			dataAtual.setTime(new Date());

			// Executa ate o dia anterior da data atual, garantindo que independentemente
			// do horario do job, sempre traga todas as materias publicadas, ja que o
			// horario de
			// publicacao pode variar entre os diversos diarios da justica eletronica.
			// Nao executa a data atual, ja que dependendo do horario, as materias podem
			// ainda nao
			// terem sido publicadas.
			while (comparaDatasDesprezandoHorario(execucaoJob.getTime(), dataAtual.getTime())) {
				consultarMateriasDisponibilizadasNoDia(execucaoJob.getTime(), true);

				execucaoJob.add(Calendar.DAY_OF_MONTH, 1);
			}
		} catch (Exception e) {
			log.error("[DIARIO ELETRONICO - ERRO] Erro ao consultar matérias no Diário Eletrônico ", e);

			log.error(e);
		}
	}

	public void consultarMateriasDisponibilizadasNoDia(Date data, boolean atualizarDataExecucaoJob)
			throws PJeBusinessException {
		// Verifica se possui conector para comunicação com Diário Eletrônico
		if (publicadorDJE != null) {
			ControleTransactional.beginTransaction();

			try {
				Calendar dataConsulta = Calendar.getInstance();

				dataConsulta.setTime(data);

				Calendario calendario = null;

				log.info("[DIARIO ELETRONICO - INFO] Chama o webservice do Diário");

				List<String> materias = publicadorDJE.verificaPublicacoesPorData(dataConsulta);

				OrgaoJulgador orgaoJulgador = null;

				if (materias != null && materias.size() > 0) {
					log.info("[DIARIO ELETRONICO - INFO] Quantidade de matérias encontradas: {0} para o dia {1}.",
							materias.size(), dataConsulta.getTime());

					for (String materia : materias) {
						log.info("[DIARIO ELETRONICO - INFO] Busca a matéria pelo código {0}", materia);

						ProcessoExpediente processoExpediente = obterProcessoExpediente(Long.parseLong(materia));

						if (processoExpediente != null) {
							log.info("[DIARIO ELETRONICO - INFO] Matéria " + materia + " encontrada");

							log.info("[DIARIO ELETRONICO - INFO] Atualiza data de disponibilização para expediente "
									+ processoExpediente.getIdProcessoExpediente());

							orgaoJulgador = processoExpediente.getProcessoTrf().getOrgaoJulgador();

							log.info("[DIARIO ELETRONICO - INFO] Busca lista de feriados para o Órgão Julgador {0}",
									orgaoJulgador.getSigla());

							calendario = prazosProcessuaisService.obtemCalendario(orgaoJulgador);

							log.info("[DIARIO ELETRONICO - INFO] Quantidade de feriados: {0}",
									(calendario != null) ? calendario.getEventos().size() : 0);

							// A data de publicação é a data de disponibilização mais um dia útil
							Date dataPublicacao = prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(
									dataConsulta.getTime(), calendario, 1,
									processoExpediente.getProcessoTrf().getCompetencia().getCategoriaPrazoCiencia(),
									ContagemPrazoEnum.C);

							EntityManager entityManager = EntityUtil.getEntityManager();
							entityManager.persist(processoExpediente);
							entityManager.flush();

							log.info("[DIARIO ELETRONICO - INFO] Lança movimentos");

							lancarMovimentosDiario(processoExpediente);

							for (ProcessoParteExpediente processoParteExpediente : processoExpediente
									.getProcessoParteExpedienteList()) {
								log.info("[DIARIO ELETRONICO - INFO] Atualiza prazos de {0}",
										processoParteExpediente.getPessoaParte().getNome());

								// A data de ciência é a data de publicação mais um dia útil
								Date dataCiencia = prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(
										dataPublicacao, calendario, 1, processoParteExpediente.getProcessoJudicial()
												.getCompetencia().getCategoriaPrazoCiencia(),
										ContagemPrazoEnum.M);

								processoParteExpediente.setDtCienciaParte(dataCiencia);

								log.info("[DIARIO ELETRONICO - INFO] Atualiza data de ciência para {0} para {1}",
										processoParteExpediente.getPessoaParte().getNome(), dataCiencia);

								Calendar dataPrazoLegal = Calendar.getInstance();

								// Adiciona o prazo legal

								// testa pelo tipo de prazo e verifica se prazo é nulo
								// se o prazo legal for nulo ou não houver prazo, haverá erros de NPE no
								// verificador periódico, daí a checagem.
								if (processoParteExpediente.getPrazoLegal() != null
										&& processoParteExpediente.getPrazoLegal() != 0
										&& processoParteExpediente.getTipoPrazo() != TipoPrazoEnum.S
										&& processoParteExpediente.getTipoPrazo() != TipoPrazoEnum.C) {
									dataPrazoLegal.setTime(dataCiencia);

									dataPrazoLegal.add(Calendar.DAY_OF_MONTH, processoParteExpediente.getPrazoLegal());

									processoParteExpediente.setDtPrazoLegal(prazosProcessuaisService
											.obtemDiaUtilSeguinte(dataPrazoLegal.getTime(), calendario, true));
									log.info(
											"[DIARIO ELETRONICO - INFO] Atualiza data de prazo legal para {0} para {1}",
											processoParteExpediente.getPessoaParte().getNome(),
											dataPrazoLegal.getTime());
								}

								entityManager.persist(processoParteExpediente);
								entityManager.flush();
							}
						} else {
							log.info("[DIARIO ELETRONICO - INFO] Expediente referente a matéria " + materia
									+ " não encontrado");
						}
					}
				} else {
					log.info(
							"[DIARIO ELETRONICO - INFO] Não foram encontradas matérias disponibilizadas em {0} para o Órgão Julgador {1} com a sigla {2}",
							dataConsulta.getTime(), (orgaoJulgador != null) ? orgaoJulgador.getOrgaoJulgador() : "",
							(orgaoJulgador != null) ? orgaoJulgador.getSigla() : "");
				}

				ControleTransactional.commitTransactionAndFlushAndClear();
			} catch (Exception e) {
				ControleTransactional.rollbackTransaction();

				log.error(e);

				throw new PJeBusinessException(e.getMessage());
			}

			// grava data de execução
			if (atualizarDataExecucaoJob) {
				atualizarDataExecucaoJob(data);
			}
		}
	}

	private Boolean comparaDatasDesprezandoHorario(Date dataUltimaExecucao, Date dataDeExecucaoAtual) {
		Calendar ultimaExecucao = Calendar.getInstance();

		ultimaExecucao.setTime(dataUltimaExecucao);

		Calendar execucaoAtual = Calendar.getInstance();

		execucaoAtual.setTime(dataDeExecucaoAtual);

		ultimaExecucao.set(Calendar.AM_PM, 0);
		ultimaExecucao.set(Calendar.HOUR_OF_DAY, 0);
		ultimaExecucao.set(Calendar.HOUR, 0);
		ultimaExecucao.set(Calendar.MINUTE, 0);
		ultimaExecucao.set(Calendar.SECOND, 0);
		ultimaExecucao.set(Calendar.MILLISECOND, 0);

		execucaoAtual.set(Calendar.AM_PM, 0);
		execucaoAtual.set(Calendar.HOUR_OF_DAY, 0);
		execucaoAtual.set(Calendar.HOUR, 0);
		execucaoAtual.set(Calendar.MINUTE, 0);
		execucaoAtual.set(Calendar.SECOND, 0);
		execucaoAtual.set(Calendar.MILLISECOND, 0);

		return ultimaExecucao.before(execucaoAtual);
	}

	// Busca a entidade processoExpediente a partir de seu código
	private ProcessoExpediente obterProcessoExpediente(Long codigo) {
		ProcessoExpediente processoExpediente = null;

		EntityManager entityManager = EntityUtil.getEntityManager();

		Query q = entityManager
				.createQuery("from ProcessoExpediente m where m.idProcessoExpediente= :idProcessoExpediente")
				.setParameter("idProcessoExpediente", codigo.intValue());

		try {
			processoExpediente = (ProcessoExpediente) q.getSingleResult();
		} catch (NonUniqueResultException e) {
			log.info("Mais de um Processo Expediente com o mesmo código: " + codigo, e);

			log.error(e);
		} catch (NoResultException e) {
			log.info("Processo Expediente não encontrado, seu código é: " + codigo, e);

			log.error(e);
		}

		return processoExpediente;
	}

	public void lancarMovimentosDiario(ProcessoExpediente processoExpediente) {
		if (processoExpediente.getDtCriacao() != null) {
			// Valor do complemento que será utilizado no complementos :
			// "ato disponibilizado" e "ato publicado"
			TipoProcessoDocumento tipoProcessoDocumento = processoExpediente.getTipoProcessoDocumento();

			// Código = 1061 - Descrição = Disponibilizado o(a) #{ato disponibilizado} no
			// Diário da Justiça Eletrônico
			// **************************************************************************************
			String codMovimentoDisponibilizacaoDiario = CodigoMovimentoNacional.CODIGO_MOVIMENTO_DJE_DISPONIBILIZACAO;

			MovimentoAutomaticoService.preencherMovimento().deCodigo(codMovimentoDisponibilizacaoDiario)
					.associarAoProcesso(processoExpediente.getProcessoTrf().getProcesso())
					.associarAoDocumento(processoExpediente.getProcessoDocumento()).comProximoComplementoVazio()
					.preencherComTexto(new SimpleDateFormat("dd/MM/yyyy").format(new Date()))
					.comProximoComplementoVazio().preencherComObjeto(tipoProcessoDocumento).lancarMovimento();

			// Código = 92 - Descrição = Publicado(a) o(a) #{ato publicado} em #{data da
			// publicação}
			// ***************************************************************************************
			String codMovimentoPublicacaoDiario = CodigoMovimentoNacional.CODIGO_MOVIMENTO_DJE_PUBLICACAO;

			PrazosProcessuaisService prazosProcessuaisServiceImpl = ComponentUtil
					.getComponent("prazosProcessuaisService");

			// Deve passar a data de disponibilização para calcular a data de publicação,
			// que deverá ser o próximo dia útil
			Date dataPublicacao = prazosProcessuaisServiceImpl.obtemDiaUtilSeguinte(processoExpediente.getDtCriacao(),
					processoExpediente.getProcessoTrf().getOrgaoJulgador(), false);

			SimpleDateFormat dataFormatada = new SimpleDateFormat("dd/MM/yyyy");

			String dataPublicacaoString = dataFormatada.format(dataPublicacao);

			MovimentoAutomaticoService.preencherMovimento().deCodigo(codMovimentoPublicacaoDiario)
					.associarAoProcesso(processoExpediente.getProcessoTrf().getProcesso())
					.associarAoDocumento(processoExpediente.getProcessoDocumento()).comProximoComplementoVazio()
					.preencherComObjeto(tipoProcessoDocumento).comProximoComplementoVazio().doTipoLivre()
					.preencherComTexto(dataPublicacaoString).lancarMovimento();
		}
	}
}