package br.com.infox.pje.processor;

import java.util.Date;
import java.util.Properties;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jbpm.util.ClassLoaderUtil;
import org.quartz.CronTrigger;
import org.quartz.SchedulerException;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.timer.TimerUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Parametro;

@Name(SessaoJTFechamentoPautaStarterProcessor.NAME)
@Scope(ScopeType.APPLICATION)
// ATENCAO: NAO ativar essa anotacao, pois o componente é transacional
//@BypassInterceptors
@Startup(depends="org.jboss.seam.async.dispatcher")
public class SessaoJTFechamentoPautaStarterProcessor {

	 /*[PJEII-6240] : Alterado o horário do agendamento de execução do fechamento de pauta.
	  * Estava agendado para rodar todo dia às 00:00:00h.
	  * Em função de eventuais diferenças de horário entre as intâncias de Jboss e o banco de dados as pautas não estavam sendo fechadas no dia correto. Ocorrendo até mesmo de nunca serem fechadas.
	  * Alterando o agendamento para 00:05:00 vai permitir uma diferença maior de horário (de 5 min).
	  * O ponto chave para o entendimento do problema é a Query.SESSOES_COM_DATA_FECHAMENTO_PAUTA_DIA_CORRENTE_QUERY que utiliza current_date() como critério de restrição.
	  */
     private static final String DEFAULT_CRON_EXPRESSION = "0 5 0 * * ?";
 
     public static final String NAME = "sessaoJTFechamentoPautaStarterProcessor";
 
     public static final String ID_SESSAO_TIMER_PARAMETER = "idFechamentoSessaoTimerParameter";
     //Arquivo de configuração para os temporizadores
     private static Properties quartzProperties = ClassLoaderUtil.getProperties("seam.quartz.properties");
     
     public SessaoJTFechamentoPautaStarterProcessor(){    	 
     }
 
     @Observer({"org.jboss.seam.postInitialization","org.jboss.seam.postReInitialization"})
     @Transactional
     public void init() throws SchedulerException{
    	 String enabled = quartzProperties.getProperty("org.quartz.timer.enabled", "false");
    	 if (!"true".equals(enabled)) {
    		 return;
    	 }

    	 String idSessaoTimer = null; 
    	 try {
    		 idSessaoTimer = TimerUtil.getParametro(ID_SESSAO_TIMER_PARAMETER);
    	 } catch (IllegalArgumentException e) {
    	 }
    	 if (idSessaoTimer == null) {
    		 Parametro p = new Parametro();
    		 p.setAtivo(true);
    		 p.setDescricaoVariavel("ID do timer para fechamento automatico da pauta da sessao");
    		 p.setDataAtualizacao(new Date());
    		 p.setNomeVariavel(ID_SESSAO_TIMER_PARAMETER);
    		 p.setSistema(true);

    		 String cronExpression = quartzProperties.getProperty("org.quartz.cronExpression", 
    				 DEFAULT_CRON_EXPRESSION);        
    		 SessaoJTFechamentoPautaProcessor processor = SessaoJTFechamentoPautaProcessor.instance();
    		 QuartzTriggerHandle handle = processor.fecharPautaAutomatico(new Date(), cronExpression);
				// PJEII-7617] Inclusão da condição para ignorar falhas execuções; Rafael Carvalho (CSJT); 2013-29-05 
				if (handle.getTrigger() != null && ParametroUtil.instance().isIgnoraFalhaDeExecucaoDoQuartz()) {
					handle.getTrigger().setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);	
				}
				// [PJEII-7617] Fim.
    		 EntityUtil.getEntityManager().flush();
    		 String triggerName = handle.getTrigger().getName();
    		 p.setValorVariavel(triggerName);
    		 EntityUtil.getEntityManager().persist(p);
    		 EntityUtil.getEntityManager().flush();
    	 }
     }
 
 }
