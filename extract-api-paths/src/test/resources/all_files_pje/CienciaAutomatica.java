package br.jus.cnj.pje.controleprazos.verificadorperiodico.passos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.amqp.consumers.CienciaAutomaticaConsumer;
import br.jus.cnj.pje.amqp.model.dto.jobs.CienciaAutomaticaCloudEvent;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodicoComum;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.PessoaInvalidaException;
import br.jus.cnj.pje.nucleo.PjeRestClientException;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoAlertaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisServiceImpl;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.cnj.pje.util.ControleTransactional;
import br.jus.pje.nucleo.entidades.AMQPEvent;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.VerificadorPeriodicoLote;
import br.jus.pje.nucleo.enums.CategoriaPrazoEnum;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.enums.VerificadorPeriodicoPassosEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(CienciaAutomatica.NAME)
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class CienciaAutomatica {

	public static final String NAME = "cienciaAutomatica";

	@Logger
	private Log log;

	@In(create = true, required = true)
	private VerificadorPeriodicoComum verificadorPeriodicoComum;

	@In(create = true, required = true)
	private ProcessoParteExpedienteManager processoParteExpedienteManager;

	@In(create = true, required = true)
	private PrazosProcessuaisService prazosProcessuaisService;

	@In(create = true, required = true)
	private ProcessoAlertaManager processoAlertaManager;

	@In(create = true, required = true)
	private CienciaAutomaticaConsumer cienciaAutomaticaConsumer;

	@In(create = true, required = false)
	private DomicilioEletronicoService domicilioEletronicoService;

	public VerificadorPeriodicoLote run(Map<Integer, Calendario> mapaCalendarios) {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.REGISTRAR_CIENCIA_AUTOMATICA;

		log.info("Localizando prazos de graça expirados e registrando a ciência automatizada.");

		VerificadorPeriodicoLote verificadorPeriodicoLote = verificadorPeriodicoComum.insereRegistroRelatorio(passo);

		try {
			Integer numeroLimitadorExpedientesPendentes = verificadorPeriodicoComum.getNumeroLimitador(passo);

			if (numeroLimitadorExpedientesPendentes <= 0) {
				log.info("Limitando os expedientes em '" + passo.getLabel() + "' para '"
						+ numeroLimitadorExpedientesPendentes + "' itens a serem processados!");
			} else {
				String prazoMeioCorreio = ComponentUtil.getComponent(ParametroService.class)
						.valueOf("presuncaoEntregaCorrespondencia");

				Integer prazoPresuncaoCorreios = null;

				List<ExpedicaoExpedienteEnum> meios = new ArrayList<ExpedicaoExpedienteEnum>(2);

				meios.add(ExpedicaoExpedienteEnum.E);

				if (prazoMeioCorreio != null) {
					try {
						prazoPresuncaoCorreios = Integer.parseInt(prazoMeioCorreio);

						if (prazoPresuncaoCorreios > 0) {
							meios.add(ExpedicaoExpedienteEnum.C);
						}
					} catch (NumberFormatException ex) {
						log.error(
								"O valor do parâmetro [presuncaoEntregaCorrespondencia] não pode ser convertido para um número de dias.");
					}
				}

				if (prazoPresuncaoCorreios == null) {
					prazoPresuncaoCorreios = 0;
				}

				meios.add(ExpedicaoExpedienteEnum.G);

				Date data = DateUtil.getBeginningOfToday();

				List<Integer> expedientes = processoParteExpedienteManager.getAtosComunicacaoPendentesCienciaIds(meios);

				if (expedientes.size() > numeroLimitadorExpedientesPendentes) {
					List<Integer> tmp = expedientes.subList(0, numeroLimitadorExpedientesPendentes);

					expedientes = tmp;

					log.info("Limitando os expedientes em '" + passo.getLabel() + "' para '"
							+ numeroLimitadorExpedientesPendentes + "' itens a serem processados!");
				}

				if (ConfiguracaoIntegracaoCloud.isRabbitJobDJEEnabled()) {
					this.runComControleDeLote(expedientes, data, prazoPresuncaoCorreios);
				} else {
					this.runLocal(expedientes, data, prazoPresuncaoCorreios, mapaCalendarios, true);
				}
			}

			verificadorPeriodicoLote.setProcessado(true);
		} catch (Exception e) {
			verificadorPeriodicoLote.setProcessado(false);

			String msg = String.format("Erro ao tentar recuperar os atos de comunicação pendentes de ciência: [%s].",
					e.getLocalizedMessage());

			log.error(msg);

			log.error(e);
		}

		verificadorPeriodicoLote = verificadorPeriodicoComum.atualizaRegistroRelatorio(verificadorPeriodicoLote);

		log.info("Registrados os prazos de graça expirados e iniciados o prazos judiciais pertinentes.");

		return verificadorPeriodicoLote;
	}

	public List<Integer> runLocal(List<Integer> expedientes, Date data, Integer prazoPresuncaoCorreios,
			Map<Integer, Calendario> mapaCalendarios, boolean processamentoBatch) {
		final VerificadorPeriodicoPassosEnum passo = VerificadorPeriodicoPassosEnum.REGISTRAR_CIENCIA_AUTOMATICA;

		if (mapaCalendarios == null || mapaCalendarios.isEmpty()) {
			PrazosProcessuaisServiceImpl prazosProcessuaisService = ComponentUtil
					.getComponent(PrazosProcessuaisServiceImpl.class);

			mapaCalendarios = prazosProcessuaisService.obtemMapaCalendarios();
		}

		int cont = 0;

		List<Integer> expedientesProcessados = new ArrayList<>();
		List<Integer> expedientesProcessadosTmp = new ArrayList<>();

		ControleTransactional.beginTransactionAndClearJbpm();

		for (Integer expedienteId : expedientes) {
			try {
				log.info("Processando item em '" + passo.getLabel() + "'. [idProcessoParteExpediente: " + expedienteId
						+ "] [" + ++cont + "/" + expedientes.size() + "]");

				registraCienciaAutomatica(expedienteId, data, prazoPresuncaoCorreios, mapaCalendarios, false, false);

				expedientesProcessadosTmp.add(expedienteId);

				if (ControleTransactional.verificarNecessidadeDeCommitAndClearJbpm(cont, processamentoBatch)) {
					expedientesProcessados.addAll(expedientesProcessadosTmp.stream().collect(Collectors.toList()));

					expedientesProcessadosTmp.clear();
				}
			} catch (Exception e) {
				ControleTransactional.rollbackTransaction();

                expedientesProcessadosTmp.clear();

				String msg = String.format(
						"Não foi possível registrar ciência automática para o processoParteExpediente [%d].",
						expedienteId);

				log.error(msg);

				log.error(e);

				verificadorPeriodicoComum.incluirAlertaErroExpedientes(msg, CriticidadeAlertaEnum.C, expedienteId);

				ControleTransactional.beginTransactionAndClearJbpm();
			}
		}

		ControleTransactional.commitTransactionAndFlushAndClear();

		if (expedientesProcessadosTmp != null && expedientesProcessadosTmp.size() > 0) {
			expedientesProcessados.addAll(expedientesProcessadosTmp.stream().collect(Collectors.toList()));
		}

		return expedientesProcessados;
	}

	private void runComControleDeLote(List<Integer> idsProcessoParteExpediente, Date dataCienciaAutomatica,
			Integer prazoPresuncaoCorreios) throws PJeBusinessException {
		cienciaAutomaticaConsumer.purgeQueue();

		String passo = VerificadorPeriodicoPassosEnum.REGISTRAR_CIENCIA_AUTOMATICA.getLabel();

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
			CienciaAutomaticaCloudEvent cienciaAutomaticaCloudEvent = new CienciaAutomaticaCloudEvent(
					dataCienciaAutomatica, ids, prazoPresuncaoCorreios, uuidLoteString, ++numJob);

			amqpEvents
					.add(amqpManager.prepararMensagem(cienciaAutomaticaCloudEvent, CienciaAutomaticaCloudEvent.class));
		}

		Integer tamanhoLote = idsProcessoParteExpedienteParticionado.size();

		try {
			verificadorPeriodicoComum.enviarMensagens(passo, amqpManager, amqpEvents, uuidLoteString, tamanhoLote);
		} catch (Exception e) {
			log.error(e);

			throw new PJeBusinessException(e.getMessage());
		}

		verificadorPeriodicoComum.aguardaProcessamentoPasso(passo, uuidLote, tamanhoLote);

		cienciaAutomaticaConsumer.purgeQueue();
	}

	public void registraCienciaAutomatica(Integer idExpediente, Date date, Integer prazoPresuncaoCorreios,
			Map<Integer, Calendario> mapaCalendarios, boolean forcarPresuncaoCorreios, boolean forcarAtualizacaoCiencia)
			throws PJeBusinessException, PjeRestClientException, PessoaInvalidaException {
		registraCienciaAutomatica(idExpediente, date, prazoPresuncaoCorreios, mapaCalendarios, forcarPresuncaoCorreios,
				forcarAtualizacaoCiencia, false);
	}

	/**
	 * Registra a ciência automática para todos os atos de comunicação do tipo
	 * eletrônico, considerando as normas relativas à expiração do prazo de graça de
	 * que trata a Lei n. 11.419/2006.
	 *
	 * O método presume que eventual verificação de indisponibilidade já foi
	 * previamente executado e gerou os pertinentes eventos de calendário relativos
	 * à suspensão ou interrupção de prazos.
	 *
	 * @param date o momento limite para a realização da ciência pessoal.
	 * @throws PessoaInvalidaException 
	 * @throws PjeRestClientException 
	 */
	@Transactional
	public void registraCienciaAutomatica(Integer idExpediente, Date date, Integer prazoPresuncaoCorreios,
			Map<Integer, Calendario> mapaCalendarios, boolean forcarPresuncaoCorreios, boolean forcarAtualizacaoCiencia,
			boolean flush) throws PJeBusinessException, PjeRestClientException, PessoaInvalidaException {
		Date dataFinal = new Date();

		ProcessoParteExpediente ppe = verificadorPeriodicoComum.getAtoComunicacaoPessoal(idExpediente);

		if (((ppe.getProcessoExpediente().getMeioExpedicaoExpediente() != ExpedicaoExpedienteEnum.E) // não é eletrônico
				&& (ppe.getProcessoExpediente().getMeioExpedicaoExpediente() != ExpedicaoExpedienteEnum.C) // não é correios
				&& (ppe.getProcessoExpediente().getMeioExpedicaoExpediente() != ExpedicaoExpedienteEnum.G)) // não é telegrama

				|| (ppe.getDtCienciaParte() != null && !forcarAtualizacaoCiencia)) { // já houve a ciência, mas se solicitou forçar novo registro
			return;
		}

		OrgaoJulgador orgaoJulgador = ppe.getProcessoJudicial().getOrgaoJulgador();

		if (orgaoJulgador == null) {
			String msg = String.format(
					"Não foi possível registrar ciencia automatica para o expediente [%d], que tem como destinatário [%s], pois esse processo está sem órgão julgador.",
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
			throw new PJeBusinessException("Não há calendario de eventos para o órgão julgador.");
		}

		// Calculo da presunção de prazos para MEIO CORREIOS
		if (ppe.getProcessoExpediente().getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.C
				&& (forcarPresuncaoCorreios || (prazoPresuncaoCorreios != null && prazoPresuncaoCorreios > 0))) {
			if (ppe.getProcessoExpediente().getProcessoTrf().getOrgaoJulgador().getPresuncaoCorreios() != null) {
				dataFinal = prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(
						ppe.getDataDisponibilizacao(), calendario,
						Integer.parseInt(ppe.getProcessoExpediente().getProcessoTrf().getOrgaoJulgador().getPresuncaoCorreios()),
						ppe.getProcessoExpediente().getProcessoTrf().getCompetencia().getCategoriaPrazoCiencia(),
						ContagemPrazoEnum.C);
			} else if (prazoPresuncaoCorreios != null && prazoPresuncaoCorreios > 0) {
				dataFinal = prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(
						ppe.getDataDisponibilizacao(), calendario, prazoPresuncaoCorreios,
						ppe.getProcessoExpediente().getProcessoTrf().getCompetencia().getCategoriaPrazoCiencia(),
						ContagemPrazoEnum.C);
			} else {
				return;
			}
			// Cálculo da presunção de prazos para o meio Telegrama:
		} else if (ppe.getProcessoExpediente().getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.G) {
			return;
		} else if (ppe.getTipoPrazo() == TipoPrazoEnum.C && ppe.getDtPrazoLegal().before(prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(ppe.getDataDisponibilizacao(), calendario, CategoriaPrazoEnum.C, ContagemPrazoEnum.C))) { // Data Certa
			dataFinal = ppe.getDtPrazoLegal();

			date = new Date(System.currentTimeMillis());
		} else {
			dataFinal = obterDataPrazoLegalCiencia(ppe, prazoPresuncaoCorreios, mapaCalendarios, forcarPresuncaoCorreios);
		}

		if (dataFinal.before(date)) {
			if (domicilioEletronicoService.isCitacaoEnviadaDomicilioEletronico(ppe) && !domicilioEletronicoService.isPessoaJuridicaDeDireitoPublico(ppe.getPessoaParte().getDocumentoCpfCnpj())) {
				processoParteExpedienteManager.fecharExpediente(ppe);

				// COMENTADO A SOLUÇÃO DO CNJ, PARA PASSAR A USAR O JOB DE TRATAMENTO DE COMUNICAÇÕES E OS FLUXOS TCD E TCI
				// domicilioEletronicoService.criarFluxoCitacaoExpiradaAsync(ppe);
			}
			else {
				ppe.setCienciaSistema(true);
				processoParteExpedienteManager.registraCiencia(ppe, dataFinal, forcarAtualizacaoCiencia, calendario, flush);
			}
		} else {
			ppe.setDtPrazoLegal(dataFinal);

			processoParteExpedienteManager.persist(ppe);

			if (flush) {
				processoParteExpedienteManager.flush();
			}
		}
	}

	public Date obterDataPrazoLegalCiencia(ProcessoParteExpediente ppe, Integer prazoPresuncaoCorreios,
			Map<Integer, Calendario> mapaCalendarios, boolean forcarPresuncaoCorreios)
			throws PJeBusinessException, PjeRestClientException, PessoaInvalidaException {
		Date resultado = null;

		Calendario calendario = null;

		OrgaoJulgador orgaoJulgador = ppe.getProcessoJudicial().getOrgaoJulgador();

		if (Objects.isNull(orgaoJulgador)) {
			String msg = String.format(
					"Não foi possível obter a data do prazo legal da ciencia para o expediente [%d], que tem como destinatário [%s], pois esse processo está sem órgão julgador.",
					ppe.getIdProcessoParteExpediente(), ppe.getPessoaParte());

			log.error(msg);

			throw new PJeRuntimeException(msg);
		}

		if (MapUtils.isEmpty(mapaCalendarios)) {
			calendario = prazosProcessuaisService.obtemCalendario(orgaoJulgador);
		} else {
			calendario = mapaCalendarios.get(orgaoJulgador.getIdOrgaoJulgador());
		}

		if (calendario == null) {
			throw new PJeBusinessException("Não há calendario de eventos para o órgão julgador.");
		}

		ProcessoExpediente processoExpediente = ppe.getProcessoExpediente();

		ProcessoTrf processoTrf = processoExpediente.getProcessoTrf();

		OrgaoJulgador orgaoJulgadorDoProcesso = processoTrf.getOrgaoJulgador();

		Competencia competencia = processoTrf.getCompetencia();

		CategoriaPrazoEnum categoriaPrazoCiencia = competencia.getCategoriaPrazoCiencia();

		//Alterações realizadas pela ISSUE PJEII-4023 - Alteração realizada em 23/11/2012 por Rafael Barros
		// Calculo da presunção de prazos para MEIO CORREIOS
		if (processoExpediente.getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.C
				&& (forcarPresuncaoCorreios || (prazoPresuncaoCorreios != null && prazoPresuncaoCorreios > 0))) {

			if (orgaoJulgadorDoProcesso.getPresuncaoCorreios() != null) {
				resultado = prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(
						ppe.getDataDisponibilizacao(),
						calendario,
						Integer.parseInt(orgaoJulgadorDoProcesso.getPresuncaoCorreios()),
						categoriaPrazoCiencia,
						ContagemPrazoEnum.C);
			} else if (prazoPresuncaoCorreios != null && prazoPresuncaoCorreios > 0) {
				resultado = prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(
						ppe.getDataDisponibilizacao(),
						calendario,
						prazoPresuncaoCorreios,
						categoriaPrazoCiencia,
						ContagemPrazoEnum.C);
			} else {
				return null;
			}
		} else { // Caculo da presunção de prazos para MEIO ELETRONICO

			if (domicilioEletronicoService.isIntegracaoHabilitada()
					&& ProcessoParteExpedienteManager.instance().isMeioExpedicaoSistema(ppe)
					&& ppe.isEnviadoDomicilio()) {

				boolean isCitacao = ProcessoExpedienteManager.instance().isCitacao(ppe.getProcessoExpediente());
				boolean isDestinatarioPessoaJuridicaDireitoPublico = domicilioEletronicoService.isPessoaJuridicaDeDireitoPublico(ppe.getPessoaParte());
				boolean isCitacaoPessoaPrivada = isCitacao && !isDestinatarioPessoaJuridicaDireitoPublico;

				TipoPrazoEnum tipoPrazo = ParametroUtil.getTipoPrazoParametro(
				    Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_TIPO_PRAZO, 
				    TipoPrazoEnum.D 
				);

				Integer prazo = isCitacaoPessoaPrivada 
				    ? ParametroUtil.getPrazoParametro(
				          tipoPrazo,
				          Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_QTD_PRAZO_CITACAO_PF_PJ_DIR_PRIVADO,
				          DomicilioEletronicoService.QTD_DIAS_GRACA_PF_PJ_DIR_PRIVADO
				      )
				    : ParametroUtil.getPrazoParametro(
				          tipoPrazo,
				          Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_QTD_PRAZO_CITACAO_PJ_DIREITO_PUBLICO,
				          DomicilioEletronicoService.QTD_DIAS_GRACA_PJ_DIREITO_PUBLICO
				      );

				CategoriaPrazoEnum categoria = isCitacaoPessoaPrivada ? CategoriaPrazoEnum.U : CategoriaPrazoEnum.C;

				resultado = prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(
				    ppe.getDataDisponibilizacao(),
				    calendario,
				    prazo,
				    categoria,
				    ContagemPrazoEnum.C
				);
			} else {
				resultado = prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(
						ppe.getDataDisponibilizacao(),
						calendario,
						categoriaPrazoCiencia,
						ContagemPrazoEnum.C);
			}

		}

		return resultado;
	}
}