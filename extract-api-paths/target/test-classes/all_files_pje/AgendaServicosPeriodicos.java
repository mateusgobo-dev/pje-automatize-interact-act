/**
 * 
 */
package br.jus.cnj.pje.controleprazos;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.jbpm.util.ClassLoaderUtil;
import org.quartz.SchedulerException;

import br.com.infox.cliente.util.InicializaIndices;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.processor.CacheLocalDomicilioProcessor;
import br.com.infox.pje.processor.DomicilioEletronicoProcessor;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.ParametroDAO;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificadorPeriodico;
import br.jus.cnj.pje.controleprazos.verificadorperiodico.VerificarDiarioPrazosExpNaoProc;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.servicos.AMQPService;

/**
 * Componente de aplicação Seam responsável por agendar serviços periódicos.
 * Atualmente, agenda o serviço periódico de verificação de prazos após realizar
 * uma verificação na inicialização. O componente é iniciado com a aplicação.
 * 
 * @author Paulo Cristovão de Araújo Silva Filho
 * 
 */
@Name("agendaServicos")
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class AgendaServicosPeriodicos implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Logger
	private Log log;

	@In(create = true)
	private transient VerificadorPeriodico verificadorPeriodico;

	private static Properties quartzProperties = ClassLoaderUtil.getProperties("seam.quartz.properties");
	
	private static Properties jobsProperties = ClassLoaderUtil.getProperties("jobs.properties");
	
	@In
	private transient ParametroService parametroService;
	
	@In
	private transient AgendaServicosQuartz agendaServicosQuartz;
	
	@In(create = true)
	private transient InicializaIndices inicializaIndices;
	
	@In
	private transient VerificarDiarioPrazosExpNaoProc verificarDiarioPrazosExpNaoProc;
	
	@In
	private transient AtualizarPrazosExpedientesAbertos atualizarPrazosExpedientesAbertos;

	@In(create = true)
	private ParametroDAO parametroDAO;
	
	public static final String CRON_EXPRESSION_REMESSA_MANIFESTACAO_PROCESSUAL = "0 0 2 1 * ?";
	public static final String CRON_CONTROLE_PRAZO_EXPEDIENTE_NAO_PROCESSUAL = "0 15 0 * * ?";
	public static final String CRON_EXPRESSION_PUSH = "0 0/30 * 1/1 * ? *";
	/**
	 * Método de inicialização padrão do componente responsável por realizar os agendamentos
	 * dos serviços periódicos do PJe. 
	 * 
	 * @throws Exception
	 */
	@Observer("org.jboss.seam.postInitialization")
	public void agendarServicos() throws Exception {
		agendarServicos(false);
	}
	
	public void agendarServicos(boolean force) throws Exception {
		executeAcumuladorCaixasExpedientes();
		if(!force) {
			String enabled = quartzProperties.getProperty("org.quartz.timer.enabled", "false");
			if (!"true".equals(enabled)) {
				return;
			}
		}
		executeVerificadorPeriodico();
		executeControlePrazoExpedienteNaoProcessual();
		try {
			executeConsolidadorDocumentos();
			executeRemessaManifestacaoProcessual();
			executeAtualizarPrazosExpedientesAbertos();
			executeAtualizarPrazosExpedientesAbertosDomicilioEletronico();
			executeReenvioMensagensAMQP();
			executeReenvioRequisicoesDomicilioEletronico();
		}
		catch(Exception e) {
			log.error("Houve um erro ao tentar agendar serviços periódicos: {0}", e.getLocalizedMessage());
		}

		try {
			executeTratamentoDeComunicacoesAoDomicilioEletronico();
		} catch (Exception e) {
			log.error("Houve um erro ao tentar agendar serviços periódicos: {0}", e.getLocalizedMessage());
		}
		
		try {
			executeCachePessoaDomicilioProcessor();
		} catch (Exception e) {
			log.error("Houve um erro ao tentar agendar serviços periódicos: {0}", e.getLocalizedMessage());
		}
		
		try {
			executeAtualizaIndices();
		} catch (Exception e) {
			log.error("Houve um erro ao agendar o serviço de atualização dos indíces: {0}", e.getLocalizedMessage());
		}
		
		try {
			executePush();
		} catch (Exception e) {
			log.error("Houve um erro ao agendar o serviço de push: {0}", e.getLocalizedMessage());
		}
	}
	
	/**
	 * Metodo de cadastro e execução do JOB responsavel por verificar prazos de expedientes nao processuais vencidos, diariamente, às 00H15
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws SchedulerException
	 */
	private void executeControlePrazoExpedienteNaoProcessual() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException, SchedulerException {
		String cron = CRON_CONTROLE_PRAZO_EXPEDIENTE_NAO_PROCESSUAL; // Execucao diariamente às 00H15
		agendarServico(cron, "verificarDiarioPrazosExpNaoProc", "execute");
	}
	
	private void executeAcumuladorCaixasExpedientes() {
		String sql = "select id_caixa_adv_proc as idCaixa,count(0) as qtd from client.tb_proc_parte_exp_caixa_adv_proc caixa "+
					 "inner join client.tb_proc_parte_expediente ppe on ppe.id_processo_parte_expediente = caixa.id_processo_parte_expediente "+ 
					 "where in_fechado = false group by id_caixa_adv_proc";
		Query q = EntityUtil.getEntityManager().createNativeQuery(sql);
		List<Object[]> resultList = q.getResultList();
		Map<Integer,BigInteger> res= new HashMap<Integer,BigInteger>(0);
				for (Object[] borderTypes: resultList) {
					res.put((Integer)borderTypes[0], (BigInteger)borderTypes[1]);
				   }		
		Contexts.getApplicationContext().set("contadorExpedientesCaixas", res);
		executeAcumuladorCaixasExpedientesNaoSigilosos();
	}
	
	private void executeAcumuladorCaixasExpedientesNaoSigilosos() {
		String sql = "select id_caixa_adv_proc as idCaixa,count(0) as qtd from client.tb_proc_parte_exp_caixa_adv_proc caixa "+
					 "inner join client.tb_proc_parte_expediente ppe on ppe.id_processo_parte_expediente = caixa.id_processo_parte_expediente "+
					 "inner join client.tb_processo_trf ptf on ppe.id_processo_trf = ptf.id_processo_trf " +
					 "where in_fechado = false and in_segredo_justica = false group by id_caixa_adv_proc";
		Query q = EntityUtil.getEntityManager().createNativeQuery(sql);
		List<Object[]> resultList = q.getResultList();
		Map<Integer,BigInteger> res= new HashMap<Integer,BigInteger>(0);
				for (Object[] borderTypes: resultList) {
					res.put((Integer)borderTypes[0], (BigInteger)borderTypes[1]);
				   }		
		Contexts.getApplicationContext().set("contadorExpedientesCaixasNaoSigilosos", res);
	}

	/**
	 * Método responsável por fazer a verificação inicial da contagem de prazos no PJe e,
	 * caso ainda não esteja agendada, pelo agendamento dessa verificação de prazos todos
	 * os dias, à 0h0m01s.
	 * Caso haja alguma exceção durante as verificações, o PJe não será iniciado!
	 * 
	 * @throws SchedulerException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	private void executeVerificadorPeriodico() throws SchedulerException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String cron = "1 0 0 * * ?"; // Todos os dias, às 0h0m1s.
		agendarServico(cron, "verificadorPeriodico", "execute");
	}
	
	/**
	 * Método responsável por consolidar os documentos enviados por meio da remessa de processos
	 * entre instâncias do PJe.
	 * 
	 * @throws SchedulerException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	private void executeConsolidadorDocumentos() throws SchedulerException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String cron = "0 0 1 * * ?"; //todos os dias 1 hora da manhã
		agendarServico(cron, "consolidadorDocumentosService", "execute");
	}
	
	/**
	 * /**
	 * Remeter todas as manifestações processuais que estão localizadas na
	 * tarefa Parametros.REMESSA_MANIFESTACAO_PROCESSUAL_NOME_TAREFA
	 * 	 * 
	 * @throws SchedulerException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	private void executeRemessaManifestacaoProcessual() throws SchedulerException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
  	
	  	String cron = CRON_EXPRESSION_REMESSA_MANIFESTACAO_PROCESSUAL;
	  	agendarServico(cron, "remessaManifestacaoProcessual", "execute");
	}
	
	/**
	 * Método responsável por realizar a indexação definida na classe InicializaIndices e,
	 * caso ainda não esteja agendada, pelo agendamento dessa verificação de prazos todos
	 * os dias, à 4h0m00s.
	 * 
	 * @throws SchedulerException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	private void executeAtualizaIndices() throws SchedulerException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String cron = "0 0 4 * * ?"; // Todos os dias, às 4h0m0s.
		agendarServico(cron, "inicializaIndices", InicializaIndices.ATTR.EXECUTE);
	}

	/**
	 * Método responsável pela execução do serviço de atualização de prazos dos expedientes abertos.
	 * 
	 * @throws SchedulerException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	private void executeAtualizarPrazosExpedientesAbertos() throws SchedulerException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
  	
	  	executeAtualizarPrazosExpedientesAbertos("59 59 23 31 12 ? 2099");
	}
	
	/**
	 * Método responsável pela execução do serviço de atualização de prazos dos expedientes abertos.
	 * 
	 * @throws SchedulerException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public void executeAtualizarPrazosExpedientesAbertos(String cron) throws SchedulerException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
  	
		agendarServico(cron, "atualizarPrazosExpedientesAbertos", "execute");
	}
	
	/**
    * Método responsável pela execução do serviço de atualização de prazos dos expedientes abertos e que foram enviados para o Domicílio Eletrônico.
    *
    * @throws SchedulerException
    * @throws IllegalArgumentException
    * @throws SecurityException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    * @throws NoSuchMethodException
    */

    private void executeAtualizarPrazosExpedientesAbertosDomicilioEletronico() throws SchedulerException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String cron = "0 0 23 * * ?"; //todos os dias 23:00 horas da noite
        agendarServico(cron, "atualizarPrazosExpedientesAbertosDomicilioEletronico", "execute");
    }

    /**
     * Método responsável pela execução do serviço de atualização de prazos dos expedientes abertos e que foram enviados para o Domicílio Eletrônico.
     *
     * @throws SchedulerException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */

    public void executeAtualizarPrazosExpedientesAbertosDomicilioEletronico(String cron) throws SchedulerException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        agendarServico(cron, "atualizarPrazosExpedientesAbertosDomicilioEletronico", "execute");
    }
    
	/**
	 * Método responsável por reenviar mensagens que 
	 * não conseguiram chegar ao message broker
	 * 
	 * @throws SchedulerException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */	
	private void executeReenvioMensagensAMQP() throws SchedulerException, IllegalArgumentException, SecurityException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String cron = parametroDAO.valueOf(Parametros.PJE_JOB_REENVIO_MENSAGENS_AMQP_CRON);

		if (StringUtils.isBlank(cron)) { // SE FOR NULO OU VAZIO SETA O VALOR DEFAULT.
			cron = "0 0/5 * * * ?"; // TODOS OS DIAS A CADA 5 MINUTOS.
		}

		agendarServico(cron, AMQPService.NAME, AMQPService.ATTR.EXECUTE);
	}	

	/**
	 * Agenda um job para executar conforme a expressão cron passada por parâmetro.
	 * 
	 * @param cron
	 * @param objeto
	 * @param metodo
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws SchedulerException
	 */
	public void agendarServico(String cron, String objeto, String metodo) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException, SchedulerException {
		if (!isServicoAgendado(cron, objeto, metodo)) {
			log.info(String.format("Agendando serviço '%s' para o período '%s'.", objeto, cron));
			agendaServicosQuartz.agendarServico(objeto, metodo, cron);
		}
	}
	
	/**
	 * Agenda um job para executar conforme a expressão cron passada por parâmetro.
	 * 
	 * @param cron
	 * @param objeto
	 * @param metodo
	 * @param flush Se true o flush será invocado.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws SchedulerException
	 */
	public void agendarServico(String cron, String objeto, String metodo, Boolean flush) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException, SchedulerException {
		if (!isServicoAgendado(cron, objeto, metodo)) {
			log.info(String.format("Agendando serviço '%s' para o período '%s'.", objeto, cron));
			agendaServicosQuartz.agendarServico(objeto, metodo, cron, flush);
		}
	}
	
	/**
	 * Retorna true se o job já tiver sido agendado.
	 * 
	 * @param cron
	 * @param objeto
	 * @param metodo
	 * @return Booleano
	 */
	public Boolean isServicoAgendado(String cron, String objeto, String metodo) {
		String servico = objeto +"."+ metodo + "()";
		return agendaServicosQuartz.servicoAgendado(servico, cron);
	}

	private void executePush() throws SchedulerException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String cron = ParametroUtil.getParametro("pje:jobs:push:cron");
		if (cron == null) {
			cron = jobsProperties.getProperty(Variaveis.CRON_EXPRESSION_PUSH, CRON_EXPRESSION_PUSH);
		}
		agendarServico(cron,"processoPushFilaService", "processarEventos");
	}
	
	/**
	 * Método responsável por reenviar as requisições ao Domicílio Eletrônico que deram erro.
	 * 
	 * @throws SchedulerException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */	
	private void executeReenvioRequisicoesDomicilioEletronico() throws SchedulerException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String jobAtiva = parametroDAO.valueOf(Parametros.PJE_JOB_REENVIO_REQUISICOES_DOMICILIO_ELETRONICO_ATIVA);

		if (jobAtiva != null && Boolean.FALSE.toString().equalsIgnoreCase(jobAtiva.trim())) {
			return;
		}

		String cron = parametroDAO.valueOf(Parametros.PJE_JOB_REENVIO_REQUISICOES_DOMICILIO_ELETRONICO_CRON); 

		if (StringUtils.isBlank(cron)) { // SE FOR NULO OU VAZIO SETA O VALOR DEFAULT.
			cron = "0 0/10 * * * ?";     // TODOS OS DIAS A CADA 10 MINUTOS.
		}

		agendarServico(cron, DomicilioEletronicoProcessor.NAME, DomicilioEletronicoProcessor.ATTR.EXECUTE);
	}	
	
	private void executeCachePessoaDomicilioProcessor() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException, SchedulerException {
		String cron = "0 30 4 * * ?"; // todos os dias 4h30
		agendarServico(cron, CacheLocalDomicilioProcessor.NAME, "execute");
	}
	
	/**
	 * Método responsável por movimentar os processos no fluxo de tratamento de comunicações ao domicílio eletrônico.
	 * 
	 * @throws SchedulerException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */	
	private void executeTratamentoDeComunicacoesAoDomicilioEletronico() throws SchedulerException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String jobAtiva = parametroDAO.valueOf(Parametros.PJE_JOB_TRATAMENTO_COMUNICACOES_DOMICILIO_ELETRONICO_ATIVA);

		if (jobAtiva != null && Boolean.FALSE.toString().equalsIgnoreCase(jobAtiva.trim())) {
			return;
		}

		String cron = parametroDAO.valueOf(Parametros.PJE_JOB_TRATAMENTO_COMUNICACOES_DOMICILIO_ELETRONICO_CRON);

		if (StringUtils.isBlank(cron)) { // SE FOR NULO OU VAZIO SETA O VALOR DEFAULT.
			cron = "0 0 0/12 * * ?";     // TODOS OS DIAS AO MEIO DIA E A MEIA NOITE.
		}

		agendarServico(cron, TratamentoDeComunicacoesAoDomicilioEletronico.NAME, TratamentoDeComunicacoesAoDomicilioEletronico.ATTR.EXECUTE);
	}
}
