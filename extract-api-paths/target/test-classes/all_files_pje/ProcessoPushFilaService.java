package br.jus.cnj.pje.servicos;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.SendFailedException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Exceptions;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.quartz.QuartzJobsInfo;
import br.com.infox.ibpm.service.EmailService;
import br.com.itx.component.Util;
import br.jus.cnj.pje.business.dao.ProcessoPushFilaDAO;
import br.jus.cnj.pje.template.FreeMakerTemplate;
import br.jus.pje.nucleo.dto.ProcessoPushFilaDTO;
import freemarker.template.Template;

@Name(ProcessoPushFilaService.NAME)
public class ProcessoPushFilaService {

	public static final String NAME = "processoPushFilaService";
	
	private static final String PUSH = "[Push]";

	private static final String ENVIADO_PROCESSO = " Enviado. Processo: ";

	@In(create = true)
	private ProcessoPushFilaDAO pushFilaDAO;

	@In
	private QuartzJobsInfo quartzJobsInfo;

	@Logger
	private Log log;

	@In
	private FreeMakerTemplate freeMakerTemplate;

	@In
	private EmailService emailService;

	@Asynchronous
	public void processarEventos(@IntervalCron String cron) {

		log.info(PUSH + " Iniciando instância do job. Gerando fila de processos.");

		Util.beginAndJoinTransaction();

		// Caso existam itens em processamento e não exista job rodando,
		// retornar todos os itens para a fila
		if (pushFilaDAO.existeItemEmProcessamento().booleanValue()  && !quartzJobsInfo.isJobEmExecucao(ProcessoPushFilaService.NAME, true).booleanValue()) {
			pushFilaDAO.retornarTodosItensParaFila();
		}

		// Gerar fila de processos com novos eventos
		pushFilaDAO.gerarFila();

		// Processar eventos e enviar emails enquanto existirem itens a processar na
		// fila
		while (pushFilaDAO.existeItemParaProcessar().booleanValue()) {

			// Buscar itens na fila para consumo e envio de emails
			List<ProcessoPushFilaDTO> itensParaProcessar = pushFilaDAO.consumirFila();

			// Envia emails e retornar não processados com emails válidos
			// Itens somente não emails inválidos não retornados e serão descartados em
			// seguida
			List<ProcessoPushFilaDTO> itensNaoProcessados = processarEnvioEmails(itensParaProcessar);

			// Em caso de itens não processos por falha de envio, porém com emails válidos,
			// atualizar lista de emails no banco de dados e retornar processo para a fila.
			// O item será processado em nova oportunidade de execução do job.
			pushFilaDAO.retornarItensParaFilaAtualizandoEmails(itensNaoProcessados);

			// Remover itens não processados (com email válido) da lista de itens a
			// serem removidos definitivamente da fila, pois já foram enviados ou
			// consta apenas email inválido.
			itensParaProcessar.removeAll(itensNaoProcessados);

			// Remover da fila itens processados
			pushFilaDAO.removerItensProcessados(itensParaProcessar);

			// Gerar novamente a fila caso existam novos itens para processamento
			pushFilaDAO.gerarFila();
		}

		Util.commitTransction();

		log.info(PUSH + " Finalizando instância do job. Fila vazia.");
	}

	// Envia os emails para os destinatários das movimentações processuais
	// Retorna uma lista de itens não enviados que tenham uma lista de emails
	// válidos
	// Itens somente não emails inválidos não serão retornados e serão descartados
	// em seguida
	private List<ProcessoPushFilaDTO> processarEnvioEmails(List<ProcessoPushFilaDTO> itensParaEnviar) {

		List<ProcessoPushFilaDTO> itensNaoProcessados = new ArrayList<>();
		Template emailTemplate = null;

		try {
			emailTemplate = freeMakerTemplate.getConfig().getTemplate("emailPush.ftlh");
		} catch (Exception e) {
			log.error(PUSH + " Não foi possí­vel criar o template de emails. Erro: " + e.getMessage());
			return itensParaEnviar;
		}

		log.info(PUSH + " Processando emails para " + itensParaEnviar.size() + " processo(s).");

		// Criando o objeto que será utilizado pra preencher
		// o template do corpo do email que será enviado
		Map<String, Object> dataModelMail = new HashMap<>();

		// Dados comuns a todos os emails que serão enviados
		dataModelMail.put("nomeSecaoJudiciaria", ParametroUtil.getParametro("nomeSecaoJudiciaria"));
		dataModelMail.put("linkPush", ParametroUtil.getParametro("dslinkPjePush") + "/Push/loginPush.seam");

		for (ProcessoPushFilaDTO itemFila : itensParaEnviar) {
			processarEmailIndividual(itemFila, dataModelMail, emailTemplate);
		}
		
		return itensNaoProcessados;
	}

	private void processarEmailIndividual(ProcessoPushFilaDTO itemFila, Map<String, Object> dataModelMail, Template emailTemplate) {
		
		StringWriter mensagem = new StringWriter();
		String tituloEmail = "";
		
		// Dados do processo
		dataModelMail.put("processo", itemFila);

		// Limpando o buffer de mensagem
		mensagem.getBuffer().setLength(0);

		// Título Email
		tituloEmail = "";

		try {
			// Preenche o corpo do email
			emailTemplate.process(dataModelMail, mensagem);

			tituloEmail = PUSH + " Movimentação processual do processo " + itemFila.getNrProcesso();

			// Validar emails
			List<String> emailsValidados = validarEmails(itemFila.getListaEmail());

			if (emailsValidados.isEmpty()) {
				return;
			}
			else {
				itemFila.setListaEmail(emailsValidados);
			}

			// Envia email agrupando os destinatários e utilizando CCO (BCC)
			emailService.enviarEmailAgrupado(itemFila.getListaEmail(), tituloEmail, mensagem.toString(),
					RecipientType.BCC);

			log.info(PUSH + ENVIADO_PROCESSO + itemFila.getNrProcesso() + " - Emails: " + StringUtils.join(itemFila.getListaEmail(),','));		
		
		} catch (Exception e) {
			// Transação é marcada para rollback em caso de erro na rotina do
			Util.rollbackAndOpenJoinTransaction();

			tratarFalhaEnvioDeEmail(itemFila, mensagem, tituloEmail, e);
		}
		
	}

	private void tratarFalhaEnvioDeEmail(ProcessoPushFilaDTO itemFila, StringWriter mensagem, String tituloEmail,
			Exception e) {
		// Em caso de falha no envio, verificar quais os emails válidos e
		// não enviados e tentar novamente
		if (ExceptionUtils.indexOfType(e, SendFailedException.class) != -1) {

			SendFailedException falhaEnvioEx = (SendFailedException) Exceptions.getCause(e);

			Address[] emailsEnviados = falhaEnvioEx.getValidSentAddresses();

			if (emailsEnviados != null && emailsEnviados.length > 0) {
				log.info(PUSH + ENVIADO_PROCESSO + itemFila.getNrProcesso() + " - Emails: " + StringUtils.join(emailsEnviados,','));
			}

			Address[] emailsInvalidos = falhaEnvioEx.getInvalidAddresses();

			if (emailsInvalidos != null && emailsInvalidos.length > 0) {
				log.error(PUSH + " Erro ao enviar movimentações do processo " + itemFila.getNrProcesso()
				+ " para o seguinte email inválido: "
				+ StringUtils.join(emailsInvalidos, ", "));
			}

			Address[] emailsValidosNaoEnviados = falhaEnvioEx.getValidUnsentAddresses();

			// Somente efetuar uma nova tentativa de envio se ainda existir email válido não enviado na lista
			if (emailsValidosNaoEnviados != null && emailsValidosNaoEnviados.length > 0) {
				tratarEmailValidoNaoEnviado(mensagem.toString(), tituloEmail, itemFila, emailsValidosNaoEnviados);
			}

		} else {
			// Não se trata de não envio por email inválido ou impossibilidade de entregar
			// email. Por se tratar de outros erros que não do email fornecido, não serão
			// retornados para a fila.
			log.error(PUSH + " Erro ao enviar movimentações do processo " + itemFila.getNrProcesso()
					+ " - Mensagem de erro: " + e.getLocalizedMessage());
		}
	}

	private void tratarEmailValidoNaoEnviado(String mensagem,
			String tituloEmail, ProcessoPushFilaDTO itemFila, Address[] falhaEnvioEx) {

		List<String> emailValidoList = new ArrayList<>();

		for (Address email : falhaEnvioEx) {

			// Nova tentativa de envio individual - Somente 1 email na lista do CC.
			emailValidoList.clear();
			emailValidoList.add(email.toString());

			// Reconfigura o item da fila somente com um único email
			itemFila.setListaEmail(emailValidoList);

			// Reenvio de email de forma individual
			boolean reenvioOK = reenviarMovimentacaoParaEmailValido(itemFila, tituloEmail, mensagem).booleanValue();

			if (!reenvioOK) {
				// Somente uma nova tentativa é realizada.
				log.error(PUSH + " A tentativa de reenvio das movimentações do processo " + itemFila.getNrProcesso()
				+ " para o seguinte email: " + StringUtils.join(itemFila.getListaEmail(), ", ")
				+ " não foi bem sucedida. Verifique se é um domínio válido.");
			}
			else {
				log.info(PUSH + ENVIADO_PROCESSO + itemFila.getNrProcesso() + " - Email: " + StringUtils.join(itemFila.getListaEmail(),','));
			}
		}
	}

	private Boolean reenviarMovimentacaoParaEmailValido(ProcessoPushFilaDTO itemFila, String tituloEmail,
			String mensagem) {

		Boolean reenvioOK = false;

		try {
			emailService.enviarEmailAgrupado(itemFila.getListaEmail(), tituloEmail, mensagem, RecipientType.BCC);
			reenvioOK = true;

		} catch (Exception e) {
			Util.rollbackAndOpenJoinTransaction();
			reenvioOK = false;
		}
		return reenvioOK;
	}

	private List<String> validarEmails(List<String> listaEmail) {

		List<String> emailsValidados = new ArrayList<>();
		EmailValidator validator = EmailValidator.getInstance();
		
		for(String email : listaEmail) {

			if (validator.isValid(email)) {
				emailsValidados.add(email);
			} 
			else {
				log.error(PUSH + " Ignorando email inválido: " + email);
			}
		}

		return emailsValidados;
	}
}
