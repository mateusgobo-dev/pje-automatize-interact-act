package br.com.infox.pje.processor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.service.LogService;
import br.com.infox.listener.LogEventListener;
import br.com.infox.trf.webservice.ConsultaClienteOAB;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.amqp.model.dto.ValidaOABCloudEvent;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.util.ControleTransactional;
import br.jus.pje.nucleo.entidades.AMQPEvent;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.util.DateUtil;

@Name(ValidarOABProcessor.NAME)
@AutoCreate
public class ValidarOABProcessor {

	public final static String NAME = "validarOABProcessor";

	@In
	private LogService logService;

	@Logger
	private Log log;

	public static ValidarOABProcessor instance() {
		return (ValidarOABProcessor) Component.getInstance(NAME);
	}

	/**
	 * 
	 * @param inicio
	 * @param cron
	 * @return
	 */
	@Asynchronous
	public QuartzTriggerHandle validarOAB(@Expiration Date inicio, @IntervalCron String cron) {
		atualizarDadosAdvogado();
		atualizarDadosProcurador();

		return null;
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void atualizarDadosAdvogado() {
		try {
			List<String> registros = getRegistros(PessoaAdvogado.class);

			log.info("Iniciando atualização de " + registros.size() + " advogado(s)");

			enviarMensagens(registros);

			log.info("Atualização do(s) advogado(s) finalizada.");
		} catch (Exception ex) {
			log.error(ex);

			logService.enviarLogPorEmail(log, ex, this.getClass(), "validarOAB - atualizarDadosAdvogado");
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void atualizarDadosProcurador() {
		try {
			List<String> registros = getRegistros(PessoaProcurador.class);

			log.info("Iniciando atualização de " + registros.size() + " procurador(es)");

			enviarMensagens(registros);

			log.info("Atualização do(s) procurador(es) finalizada.");
		} catch (Exception ex) {
			log.error(ex);

			logService.enviarLogPorEmail(log, ex, this.getClass(), "validarOAB - atualizarDadosProcurador");
		}
	}

	/**
	 * Método responsável por retornar os CPFs das pessoas para uma possível
	 * atualização com os dados da OAB.
	 * 
	 * @param clazz Subclasse de PessoaFisicaEspecializada a ser consultada tanto
	 *              para advogado quanto para procurador.
	 * @return Lista de CPFs retornada da consulta.
	 * @throws Exception Caso algo de errado ocorra.
	 */
	@SuppressWarnings("unchecked")
	private List<String> getRegistros(Class<?> clazz) throws Exception {
		List<String> resultado = new ArrayList<String>(0);

		String jpql = obterJPQL(clazz);

		if (StringUtils.isNotBlank(jpql)) {
			Calendar dataPesquisa = Calendar.getInstance();

			dataPesquisa.add(Calendar.DAY_OF_MONTH, -15);

			Query query = EntityUtil.getEntityManager().createNativeQuery(jpql.toString());

			query.setParameter("dataPesquisa", DateUtil.getBeginningOfDay(dataPesquisa.getTime()));

			resultado = query.getResultList();
		}

		return resultado;
	}

	/**
	 * @param clazz
	 * @return
	 */
	private String obterJPQL(Class<?> clazz) {
		StringBuilder jpql = new StringBuilder();

		if (PessoaFisicaEspecializada.class.isAssignableFrom(clazz)) {
			jpql.append("SELECT REGEXP_REPLACE(c.nr_documento_identificacao, '[^0-9]+', '', 'g') FROM ");

			if (PessoaAdvogado.class.isAssignableFrom(clazz)) {
				jpql.append("client.tb_pessoa_advogado a ");
			} else if (PessoaProcurador.class.isAssignableFrom(clazz)) {
				jpql.append("client.tb_pessoa_procurador a ");
			}

			jpql.append("INNER JOIN client.tb_pessoa_fisica b ON a.id = b.id_pessoa_fisica ");
			jpql.append("INNER JOIN client.tb_pess_doc_identificacao c ON b.id_pessoa_fisica = c.id_pessoa ");
			jpql.append(
					"INNER JOIN client.tb_dado_oab_pess_advogado tdopa ON tdopa.nr_cpf = REGEXP_REPLACE(c.nr_documento_identificacao, '[^0-9]+', '', 'g')");
			jpql.append(
					"WHERE b.in_validado = true AND c.cd_tp_documento_identificacao = 'CPF' AND c.in_ativo = true AND tdopa.dt_cadastro < :dataPesquisa");
		}

		return jpql.toString();
	}

	/**
	 * 
	 * @param documento
	 * @throws Exception
	 */
	public void validarOab(List<String> documentos) throws Exception {
		LogEventListener.disableLogForEvent();

		int cont = 0;

		ConsultaClienteOAB consultaClienteOAB = new ConsultaClienteOAB();

		ControleTransactional.beginTransaction();

		for (String documento : documentos) {
			++cont;

			try {
				consultaClienteOAB.consultaDados(documento, true, true);

				ControleTransactional.verificarNecessidadeDeCommitAndClearJbpm(cont, true);
			} catch (Exception e) {
				ControleTransactional.rollbackTransaction();

				log.error(e);
			}
		}

		ControleTransactional.commitTransactionAndFlushAndClear();
	}

	private void enviarMensagens(List<String> documentoList) throws Exception {
		Integer tamanhoParticao = 64;

		AMQPEventManager amqpManager = AMQPEventManager.instance();

		List<AMQPEvent> amqpEvents = new ArrayList<AMQPEvent>();

		log.info("[Valida OAB] Particionando lista de documentos para enviar para a fila");

		Collection<List<String>> documentosParticionado = partitionBasedOnSize(documentoList, tamanhoParticao);

		for (List<String> documentos : documentosParticionado) {
			ValidaOABCloudEvent validaOABCloudEvent = new ValidaOABCloudEvent(documentos);

			amqpEvents.add(amqpManager.prepararMensagem(validaOABCloudEvent, ValidaOABCloudEvent.class));
		}

		Integer tamanhoLote = documentosParticionado.size();

		try {
			ControleTransactional.beginTransaction();

			amqpManager.enviarMensagens(amqpEvents);

			ControleTransactional.commitTransactionAndFlushAndClear();
		} catch (Exception e) {
			ControleTransactional.rollbackTransaction();

			e.printStackTrace();

			throw new Exception("[Valida OAB] Excecao ao enviar mensagens para a fila de mensageria.");
		}

		log.info("[Valida OAB] Finalizado o envio dos '" + tamanhoLote + "' itens do lote para a fila de mensageria.");
	}

	private Collection<List<String>> partitionBasedOnSize(List<String> inputList, int size) {
		final AtomicInteger counter = new AtomicInteger(0);

		return inputList.stream().collect(Collectors.groupingBy(s -> counter.getAndIncrement() / size)).values();
	}
}