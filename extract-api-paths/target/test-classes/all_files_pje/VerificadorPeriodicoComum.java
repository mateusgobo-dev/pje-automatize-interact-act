package br.jus.cnj.pje.controleprazos.verificadorperiodico;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.service.EmailService;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoAlertaManager;
import br.jus.cnj.pje.nucleo.manager.VerificadorPeriodicoLoteManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.util.ControleTransactional;
import br.jus.pje.nucleo.entidades.AMQPEvent;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.VerificadorPeriodicoLote;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;
import br.jus.pje.nucleo.enums.VerificadorPeriodicoPassosEnum;

@Name(VerificadorPeriodicoComum.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class VerificadorPeriodicoComum {

	public static final String NAME = "verificadorPeriodicoComum";

	public static final String RELATORIO = "RELATORIO-";

	@In(create = true)
	private VerificadorPeriodicoLoteManager verificadorPeriodicoLoteManager;

	@In(create = true)
	private VerificadorPeriodicoAguardaProcessamentoPasso verificadorPeriodicoAguardaProcessamentoPasso;

	@In
	private ProcessoAlertaManager processoAlertaManager;

	@Logger
	private Log log;

	public void enviarMensagens(String passo, AMQPEventManager amqpManager, List<AMQPEvent> amqpEvents,
			String uuidLoteString, Integer tamanhoLote) throws Exception {
		try {
			ControleTransactional.beginTransaction();

			amqpManager.enviarMensagens(amqpEvents);

			ControleTransactional.commitTransactionAndFlushAndClear();
		} catch (Exception e) {
			ControleTransactional.rollbackTransaction();

			e.printStackTrace();

			throw new Exception("[" + passo + "] Exceção ao enviar mensagens do lote '" + uuidLoteString
					+ "' para a fila de mensageria.");
		}

		log.info("[" + passo + "] Finalizado o envio dos '" + tamanhoLote + "' itens do lote '" + uuidLoteString
				+ "' para a fila de mensageria.");
	}

	public void insereLoteNaTabelaDeControle(Collection<List<Integer>> idsParticionado, String passo, UUID uuidLote) {
		ControleTransactional.beginTransaction();

		int count = 0;

		for (List<Integer> ids : idsParticionado) {
			try {
				++count;

				VerificadorPeriodicoLote verificadorPeriodicoLote = new VerificadorPeriodicoLote(passo, uuidLote, count,
						0, ids.size());

				verificadorPeriodicoLoteManager.persist(verificadorPeriodicoLote);

				ControleTransactional.verificarNecessidadeDeCommitAndClearJbpm(count, true);
			} catch (Exception e) {
				ControleTransactional.rollbackTransaction();

				log.error("Erro ao processar item em [" + passo + "]. [ids: " + ids + "]. Erro: " + e.getMessage());

				e.printStackTrace();

				ControleTransactional.beginTransaction();
			}
		}

		ControleTransactional.commitTransactionAndFlushAndClear();
	}

	public VerificadorPeriodicoLote insereRegistroRelatorio(
			VerificadorPeriodicoPassosEnum verificadorPeriodicoPassosEnum) {
		ControleTransactional.beginTransaction();

		VerificadorPeriodicoLote verificadorPeriodicoLote = new VerificadorPeriodicoLote(
				RELATORIO + verificadorPeriodicoPassosEnum.getLabel(), UUID.randomUUID(), 0, 0, 0, new Date());

		verificadorPeriodicoLoteManager.persist(verificadorPeriodicoLote);

		ControleTransactional.commitTransactionAndFlushAndClear();

		return verificadorPeriodicoLote;
	}

	public void aguardaProcessamentoPasso(String passo, UUID loteUUID, Integer tamanhoLote) {
		verificadorPeriodicoAguardaProcessamentoPasso.setProibidoExecutar(true);
		verificadorPeriodicoAguardaProcessamentoPasso.setPasso(passo);
		verificadorPeriodicoAguardaProcessamentoPasso.setLote(loteUUID);
		verificadorPeriodicoAguardaProcessamentoPasso.setTamanhoLote(tamanhoLote);
		verificadorPeriodicoAguardaProcessamentoPasso.limpaValores();
		verificadorPeriodicoAguardaProcessamentoPasso.setProibidoExecutar(false);
		verificadorPeriodicoAguardaProcessamentoPasso.aguardaProcessamentoPassoTerminar();
		verificadorPeriodicoAguardaProcessamentoPasso.setProibidoExecutar(true);
	}

	public VerificadorPeriodicoLote atualizaRegistroRelatorio(VerificadorPeriodicoLote verificadorPeriodicoLote) {
		VerificadorPeriodicoLote verificadorPeriodicoLoteProcessado = verificadorPeriodicoLoteManager.getProcessado(
				verificadorPeriodicoLote.getIdVerificadorPeriodicoLote(),
				verificadorPeriodicoLote.getPasso().substring(RELATORIO.length()));

		return atualizaProcessamentoLote(verificadorPeriodicoLote.getDataJob(), 0,
				verificadorPeriodicoLote.getLote().toString(), verificadorPeriodicoLote.isProcessado(),
				verificadorPeriodicoLoteProcessado == null ? 0
						: verificadorPeriodicoLoteProcessado.getQtProcessadoJob(),
				verificadorPeriodicoLoteProcessado == null ? 0 : verificadorPeriodicoLoteProcessado.getTamanhoJob());
	}

	public VerificadorPeriodicoLote atualizaProcessamentoLote(Date dataJob, int numJob, String lote,
			boolean isProcessado, Integer qtProcessadoJob, Integer tamanhoJob) {
		String passo = "";

		try {
			VerificadorPeriodicoLote verificadorPeriodicoLote = verificadorPeriodicoLoteManager
					.getByIdJobAndLote(numJob, UUID.fromString(lote));

			if (verificadorPeriodicoLote != null) {
				ControleTransactional.beginTransaction();

				passo = verificadorPeriodicoLote.getPasso();

				verificadorPeriodicoLote.setProcessado(isProcessado);
				verificadorPeriodicoLote.setDataJob(dataJob);
				verificadorPeriodicoLote.setDataProcessado(new Date());
				verificadorPeriodicoLote.setQtProcessadoJob(qtProcessadoJob);
				verificadorPeriodicoLote.setTamanhoJob(tamanhoJob);

				verificadorPeriodicoLote = verificadorPeriodicoLoteManager.mergeAndFlush(verificadorPeriodicoLote);

				ControleTransactional.commitTransactionAndFlushAndClear();
			}

			return verificadorPeriodicoLote;
		} catch (Exception e) {
			ControleTransactional.rollbackTransaction();

			log.error("Erro ao tentar atualizar o processamento do lote. Passo = " + passo + ", Lote = " + lote
					+ ", Job = " + numJob);
		}

		return null;
	}

	public Collection<List<Integer>> partitionBasedOnSize(List<Integer> inputList, int size) {
		final AtomicInteger counter = new AtomicInteger(0);

		return inputList.stream().collect(Collectors.groupingBy(s -> counter.getAndIncrement() / size)).values();
	}

	public Integer getNumeroLimitador(VerificadorPeriodicoPassosEnum passo) {
		Integer numeroLimitador = 100000;
		String nomeParametroNumeroLimitador = "";

		switch (passo) {
		case SINALIZA_PROCESSOS_AGUARDANDO_AUDIENCIA:
			nomeParametroNumeroLimitador = "tjrj:parametroNumeroLimitadorProcessosAguardandoAudiencia";
			break;

		case REGISTRAR_CIENCIA_AUTOMATICA:
			nomeParametroNumeroLimitador = "tjrj:parametroNumeroLimitadorExpedientesPendentesCienciaAutomatica";
			break;

		case CIENCIA_AUTOMATIZADA_DIARIO_ELETRONICO_POR_MATERIA:
			nomeParametroNumeroLimitador = "tjrj:parametroNumeroLimitadorExpedientesAguardandoPublicacaoDiarioEletronico";
			break;

		case REGISTRAR_DECURSO_PRAZO:
			nomeParametroNumeroLimitador = "tjrj:parametroNumeroLimitadorExpedientesExpiradosDecursoDoPrazo";
			break;

		case SINALIZA_PROSSEGUIMENTO_SEM_PRAZO:
			nomeParametroNumeroLimitador = "tjrj:parametroNumeroLimitadorExpedientesProsseguimentoSemPrazo";
			break;

		case FECHAR_PAUTA_AUTOMATICAMENTE:
			nomeParametroNumeroLimitador = "tjrj:parametroNumeroLimitadorPendentesFechamentoAutomaticoPauta";
			break;

		case ENCERRAMENTO_PRAZO_NAO_PROCESSUAL:
			nomeParametroNumeroLimitador = "tjrj:parametroNumeroLimitadorPendentesEncerramentoPrazoNaoProcessual";
			break;

		default:
			nomeParametroNumeroLimitador = "";
			break;
		}

		try {
			String parametroNumeroLimitador = ComponentUtil.getComponent(ParametroService.class)
					.valueOf(nomeParametroNumeroLimitador);

			numeroLimitador = Integer.parseInt(parametroNumeroLimitador);
		} catch (NumberFormatException ex) {
			log.error("O valor do parametro " + nomeParametroNumeroLimitador + " nao pode ser convertido para numero");
		}

		return numeroLimitador;
	}

	public void enviarEmail(List<VerificadorPeriodicoLote> verificadorPeriodicoLoteList, List<String> emails,
			String jobName) {
		try {
			EmailService emailService = ComponentUtil.getComponent(EmailService.class);

			StringBuilder body = new StringBuilder();

			long total = 0;

			for (VerificadorPeriodicoLote verificadorPeriodicoLote : verificadorPeriodicoLoteList) {
				if (verificadorPeriodicoLote != null) {
					body.append("<b>");

					body.append("Passo - " + verificadorPeriodicoLote.getPasso().substring(RELATORIO.length()));

					body.append("</b>");

					body.append("<br/>");

					body.append("Passo processado: ");

					if (verificadorPeriodicoLote.isProcessado()) {
						body.append("Sim");

						body.append("<br/>");

						body.append("Tempo de processamento: ");

						long diff = verificadorPeriodicoLote.getDataProcessado().getTime()
								- verificadorPeriodicoLote.getDataJob().getTime();

						total += diff;

						formatTime(body, diff);

						body.append("<br/>");

						body.append("Processados: ");

						if (verificadorPeriodicoLote.getQtProcessadoJob() < verificadorPeriodicoLote.getTamanhoJob()) {
							body.append("<font color=\"#ff0000\">");

							body.append(verificadorPeriodicoLote.getQtProcessadoJob());

							body.append("/");

							body.append(verificadorPeriodicoLote.getTamanhoJob());

							body.append("</font>");
						} else {
							body.append(verificadorPeriodicoLote.getQtProcessadoJob());

							body.append("/");

							body.append(verificadorPeriodicoLote.getTamanhoJob());
						}
					} else {
						body.append("Nao");

						body.append("<br/>");

						body.append("Tempo de processamento: ");

						body.append("-");

						body.append("<br/>");

						body.append("Processados: ");

						body.append("-");

						body.append("/");

						body.append("-");
					}

					body.append("<br/><br/>");
				}
			}

			body.append("<b>");

			body.append("Tempo total de processamento: ");

			formatTime(body, total);

			body.append("</b>");

			emailService.enviarEmail("", emails, "Relatório do job '" + jobName + "'", body.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void formatTime(StringBuilder body, long diff) {
		long diffSeconds = diff / 1000 % 60;
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffHours = diff / (60 * 60 * 1000) % 24;
		long diffMiliSeconds = diff - (diffHours * 3600 * 1000) - (diffMinutes * 60 * 1000) - (diffSeconds * 1000);

		body.append(String.format("%02d", diffHours));

		body.append(":");

		body.append(String.format("%02d", diffMinutes));

		body.append(":");

		body.append(String.format("%02d", diffSeconds));

		body.append(".");

		body.append(String.format("%03d", diffMiliSeconds));
	}

	public void incluirAlertaErroExpedientes(String textoAlerta, CriticidadeAlertaEnum criticidade,
			Integer... idsAtos) {
		if (idsAtos != null && idsAtos.length > 0) {
			for (Integer id : idsAtos) {
				ControleTransactional.beginTransaction();

				try {
					ProcessoTrf processo = getAtoComunicacaoPessoal(id).getProcessoJudicial();

					processoAlertaManager.incluirAlertaAtivo(processo, textoAlerta, criticidade);

					ControleTransactional.commitTransactionAndFlushAndClear();
				} catch (Exception e) {
					ControleTransactional.rollbackTransaction();

					try {
						String msg = String.format(
								"Erro ao tentar registrar alerta de falha de registro de ocorrência relativa a expedientes.\n"
										+ "Mensagem: [%s]\n" + "Expediente(s): [%d]\n" + "Erro: [%s].",
								textoAlerta, idsAtos, e.getLocalizedMessage());

						log.error(msg);
					} catch (Exception e2) {
						log.error(
								"Erro ao tentar registrar, em LOG, alerta de falha de registro de ocorrência relativa a expedientes.\n",
								e2);
					}
				}
			}
		}
	}

	public ProcessoParteExpediente getAtoComunicacaoPessoal(Integer id) throws PJeBusinessException {
		String select = "SELECT DISTINCT ppe FROM ProcessoParteExpediente ppe WHERE "
				+ "idProcessoParteExpediente = :idProcessoParteExpediente";

		Query q = EntityUtil.getEntityManager().createQuery(select);

		q.setParameter("idProcessoParteExpediente", id);

		return (ProcessoParteExpediente) q.getSingleResult();
	}
}