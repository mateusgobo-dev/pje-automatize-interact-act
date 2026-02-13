package br.com.infox.ibpm.jbpm.actions;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.ibpm.home.FluxoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.ProcessBuilder;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ProcessoInstanceManager;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoInstance;

@Name(JbpmEventsHandler.NAME)
@Install(precedence = Install.FRAMEWORK)
@BypassInterceptors
public class JbpmEventsHandler implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(JbpmEventsHandler.class);
	public static final String NAME = "jbpmEventsHandler";

	@Observer(Event.EVENTTYPE_TASK_END)
	public void removerProcessoLocalizacao(ExecutionContext context) {
		try {
			Long taskId = context.getTask().getId();
			Long processId = context.getProcessInstance().getId();
			TaskInstance ti = context.getTaskInstance();
			
			if(ti != null){
				try {
					String actorIdAtual = Actor.instance().getId();
					ti.setActorId(actorIdAtual);
				} catch (Exception e) {
					log.error("quando a tarefa é finalizada por movimentação via job do sistema, não há Actor para recuperar o id [" + processId + "]");
				}
				
				if(!ti.isOpen())
				{
					String sql = "DELETE FROM tb_proc_localizacao_ibpm " + 
								 "WHERE id_processinstance_jbpm = :processId " + 
								 "AND id_task_jbpm = :taskId "; 
					
					EntityUtil.createNativeQuery(sql, "tb_proc_localizacao_ibpm").setParameter("processId", processId)
					.setParameter("taskId", taskId).executeUpdate();
				}
			}
		} catch (Exception ex) {
			String action = "remover o processo da localizacao: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"removerProcessoLocalizacao()", "JbpmEventsHandler", "BPM"));
		}
	}
	
	@Observer(Event.EVENTTYPE_PROCESS_END)
	public void inativarFluxo(ExecutionContext ctx){
		try {
			ProcessInstance pi = ctx.getProcessInstance();
			ProcessoInstanceManager pim = (ProcessoInstanceManager) org.jboss.seam.Component.getInstance("processoInstanceManager");
			ProcessoInstance pin = pim.findById(pi.getId());
			if (pin != null){
			    pin.setAtivo(false);
			    pim.persistAndFlush(pin);
			}
			else {
				Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_PROCESSINSTANCE_FINALIZADA, pi.getId());
			}
		} catch (Throwable t) {
			log.error("Erro ao tentar inativar a instância de fluxo encerrada [" + ctx.getProcessInstance().getId() + "]");
		}
	}

	@Observer(Eventos.EVENTO_PROCESSINSTANCE_FINALIZADA)
	public void inativarProcessoInstance(Long id) {
		try {
			ProcessoInstanceManager pim = (ProcessoInstanceManager) org.jboss.seam.Component.getInstance("processoInstanceManager");
			ProcessoInstance pin = pim.findById(id);
			if (pin != null){
			    pin.setAtivo(false);
			    pim.persistAndFlush(pin);
			}
		} catch (Throwable t) {
			log.error("Erro ao tentar inativar a instância de fluxo encerrada [" + id + "]");
		}
	}
	@Observer(Event.EVENTTYPE_TASK_END)
	@End(beforeRedirect = true)
	public void refreshPainel(ExecutionContext context) {
		try {
			String q = "update tb_processo set nm_actor_id = null where id_processo = :id";
			EntityUtil.createNativeQuery(q, "tb_processo")
					.setParameter("id", JbpmUtil.getProcesso().getIdProcesso()).executeUpdate();
		} catch (Exception ex) {
			String action = "limpar as variaveis do painel para atualização: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"refreshPainel()", "JbpmEventsHandler", "BPM"));
		}
	}

	/**
	 * Atualiza o dicionário de Tarefas (tb_tarefa) com seus respectivos id's de
	 * todas as versões.
	 **/
	@Observer(ProcessBuilder.POST_DEPLOY_EVENT)
	public static void updatePostDeploy(ProcessDefinition pd) {
		try {
			atualizarProcessos(pd);
			inserirTarefas(pd);
			inserirVersoesTarefas(pd);
		} catch (Exception ex) {
			String action = "realizar atualização automáticas após publicação do fluxo: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(), "updatePostDeploy()", "JbpmEventsHandler", "BPM"));
		}
	}

	private static void atualizarProcessos(ProcessDefinition pd) {			    
		log.info("Iniciando atualizacao do fluxo " + pd.getName());
		
		String queryString = "select vl_variavel from core.tb_parametro "
				+ "where nm_variavel = 'pje:fluxo:definicao:publicarViaBancoDeDados'";
		org.hibernate.Query queryFunction = JbpmUtil.getJbpmSession().createSQLQuery(queryString);
		if ((queryFunction.uniqueResult() != null) && Boolean.parseBoolean(queryFunction.uniqueResult().toString()) == true) {
				// Alterado PJEII-26177
				String sql = "SELECT public.f_pubfluxo_atualizarProcessos(:id,:name)";
				log.info("Atualizando instancias de fluxo.");
				JbpmUtil.getJbpmSession().createSQLQuery(sql)
						.setParameter("id", pd.getId()).setParameter("name", pd.getName()).uniqueResult();		
			} else {
				String sql = "UPDATE jbpm_processinstance pi SET processdefinition_ = :id FROM jbpm_processdefinition pd WHERE pd.id_ = pi.processdefinition_ AND pd.name_ = :name AND pi.end_ IS NULL";
				log.info("Atualizando instâncias de fluxo.");
				JbpmUtil.getJbpmSession().createSQLQuery(sql)
						.addSynchronizedQuerySpace("jbpm_processinstance")
						.setParameter("id", pd.getId()).setParameter("name", pd.getName()).executeUpdate();
				log.info("Instâncias de fluxo atualizadas.");
				log.info("Atualizando ponteiros de fluxo.");
				sql = "UPDATE jbpm_token t SET node_ = "
						+ "	(SELECT n.id_ FROM jbpm_node n "
						+ "		INNER JOIN jbpm_processdefinition pd ON pd.id_ = n.processdefinition_  "
						+ "		WHERE n.name_ = n1.name_ AND pd.id_ = :id AND n.class_ = n1.class_) "
						+ "	FROM jbpm_node n1 INNER JOIN jbpm_processdefinition pd2 ON pd2.id_ = n1.processdefinition_ "
						+ "	WHERE n1.id_ = t.node_ AND t.end_ IS NULL AND pd2.name_ = :name";
				JbpmUtil.getJbpmSession().createSQLQuery(sql)
						.addSynchronizedQuerySpace("jbpm_token")
						.setParameter("id", pd.getId()).setParameter("name", pd.getName()).executeUpdate();
				log.info("Ponteiros atualizados.");
				log.info("Atualizando instâncias de tarefa.");
				sql = "UPDATE jbpm_taskinstance ti SET description_ = 'dirty', task_ = "
						+ "	(SELECT t.id_ FROM jbpm_task t "
						+ "		INNER JOIN jbpm_processdefinition pd ON pd.id_ = t.processdefinition_ "
						+ "		WHERE t.name_ = t1.name_ AND pd.id_ = :id) "
						+ "	FROM jbpm_task t1 INNER JOIN jbpm_processdefinition pd2 ON pd2.id_ = t1.processdefinition_ "
						+ "	WHERE t1.id_ = ti.task_ AND ti.end_ IS NULL AND pd2.name_ = :name ";
				JbpmUtil.getJbpmSession().createSQLQuery(sql)
						.addSynchronizedQuerySpace("jbpm_taskinstance")
						.setParameter("id", pd.getId()).setParameter("name", pd.getName()).executeUpdate();
				log.info("Instâncias de tarefa atualizadas.");		
				log.info("Inserindo novas instâncias de tarefas.");
				sql = "insert " + 
				        "    into" + 
				        "        core.tb_proc_localizacao_ibpm select" + 
				        "            nextval('core.sq_tb_proc_localizacao_ibpm')," + 
				        "            o.id_task," + 
				        "            o.id_process_instance," + 
				        "            o.id_processo_trf," + 
				        "            o.sw_id_localizacao," + 
				        "            o.sw_id_papel" + 
				        " from " + 
				        " (select " + 
				        "        distinct pt.id_task, pt.id_process_instance, pt.id_processo_trf, r.sw_id_localizacao, r.sw_id_papel" + 
				        "    from   " + 
				        "        client.tb_processo_tarefa pt" + 
				        "            inner join (select " + 
				        "                            pt.id_processo_tarefa," + 
				        "                            cast(split_part(regexp_split_to_table(substring( s.pooledactorsexpression_ from '[0-9].*[0-9]'),','),':',1) as integer) as sw_id_localizacao," + 
				        "                            cast(split_part(regexp_split_to_table(substring( s.pooledactorsexpression_ from '[0-9].*[0-9]'),','),':',2) as integer) as sw_id_papel" + 
				        "                        from" + 
				        "                            client.tb_processo_tarefa pt                        " + 
				        "                                inner join jbpm_task t on pt.id_task=t.id_" + 
				        "                                inner join jbpm_swimlane s on t.swimlane_=s.id_" + 
				        "                        where" + 
				        "                            pt.nm_fluxo=:name) r on pt.id_processo_tarefa=r.id_processo_tarefa           " + 
				        "    where  " + 
				        "        not exists (select i.id_processinstance_jbpm from core.tb_proc_localizacao_ibpm i where i.id_processo=pt.id_processo_trf and i.id_task_jbpm=pt.id_task and i.id_papel=r.sw_id_papel and i.id_localizacao=r.sw_id_localizacao)) o";
				int quant  = JbpmUtil.getJbpmSession().createSQLQuery(sql).setParameter("name", pd.getName()).executeUpdate();
				log.info("Novas tarefas inseridas: " + quant);
			 }
	}

	/**
	 * Popula a tabela tb_tarefa com todas as tarefas de todos os fluxos,
	 * considerando como chave o nome da tarefa task.name_
	 */
	private static void inserirTarefas(ProcessDefinition pd) throws Exception {
		StringBuilder builder = new StringBuilder();
		builder.append("insert into tb_tarefa (id_fluxo, ds_tarefa) ");
		builder.append("select f.id_fluxo, t.name_ ");
		builder.append("from jbpm_task t ");
		builder.append("inner join jbpm_processdefinition pd on (pd.id_ = t.processdefinition_) ");
		builder.append("inner join tb_fluxo f on (f.ds_fluxo = pd.name_) ");
		builder.append("inner join jbpm_node jn on (t.tasknode_ = jn.id_ and jn.class_ = 'K') ");
		builder.append("where pd.id_ = t.processdefinition_ and ");
		builder.append("not exists (select 1 from tb_tarefa ");
		builder.append("where ds_tarefa = t.name_ and ");
		builder.append("id_fluxo = f.id_fluxo) ");
		builder.append("group by f.id_fluxo, t.name_");
		javax.persistence.Query q = EntityUtil.createNativeQuery(builder, "tb_tarefa");
		q.executeUpdate();
	}

	/**
	 * Insere para cada tarefa na tabela de tb_tarefa todos os ids que esse já
	 * possuiu.
	 */
	private static void inserirVersoesTarefas(ProcessDefinition pd) throws Exception {
		StringBuilder builder = new StringBuilder();
		builder.append("insert into tb_tarefa_jbpm (id_tarefa, id_jbpm_task) ");
		builder.append("select t.id_tarefa, jt.id_ ");
		builder.append("from tb_tarefa t ");
		builder.append("inner join tb_fluxo f using (id_fluxo) ");
		builder.append("inner join jbpm_task jt on jt.name_ = t.ds_tarefa ");
		builder.append("inner join jbpm_processdefinition pd on pd.id_ = jt.processdefinition_ ");
		builder.append("where f.ds_fluxo = pd.name_ and ");
		builder.append("not exists (select 1 from tb_tarefa_jbpm tj ");
		builder.append("where tj.id_tarefa = t.id_tarefa ");
		builder.append("and tj.id_jbpm_task = jt.id_)");
		javax.persistence.Query q = EntityUtil.createNativeQuery(builder, "tb_tarefa_jbpm");
		q.executeUpdate();
	}

	/**
	 * Antes de terminar a tarefa, remove a caixa do processo
	 * 
	 * @param transition
	 */
	@Observer(Event.EVENTTYPE_TASK_END)
	public void removeCaixaProcesso(ExecutionContext context) {
		try {
			Integer idProcesso = (Integer) context.getVariable(Variaveis.VARIAVEL_PROCESSO);
			String sql = "update tb_processo set id_caixa = null where id_processo = :idProcesso";
			EntityUtil.createNativeQuery(sql, "tb_processo").setParameter("idProcesso", idProcesso).executeUpdate();			  
		} catch (Exception ex) {
			String action = "remover o processo da caixa: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"removeCaixaProcesso()", "JbpmEventsHandler", "BPM"));
		}
	}

	public void iniciarTask(Processo processo) {
		try {
			if (processo != null && processo.getIdJbpm() != null
					&& !processo.getIdJbpm().equals(BusinessProcess.instance().getProcessId())) {
				BusinessProcess.instance().setProcessId(processo.getIdJbpm());
				String sql = "select o.idTaskInstance from SituacaoProcesso o " + "where o.idProcesso = :id "
						+ "group by o.idTaskInstance";
				Query q = getEntityManager().createQuery(sql);
				q.setParameter("id", processo.getIdProcesso());
				Long taskId = EntityUtil.getSingleResult(q);

				if (taskId != null) {
					BusinessProcess.instance().setTaskId(taskId);
					TaskInstance ti = (TaskInstance) JbpmUtil.getJbpmSession().get(TaskInstance.class, taskId);
					if (ti != null) {
						if (ti.getStart() == null) {
							BusinessProcess.instance().startTask();
						}
						String actorId = Actor.instance().getId();
						processo.setActorId(actorId);
						getEntityManager().merge(processo);
						EntityUtil.flush();
						System.out.println("Tarefa: " + BusinessProcess.instance().getTaskId());
					}
				}
			}
		} catch (Exception ex) {
			String action = "iniciar a tarefa: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"iniciarTask()", "JbpmEventsHandler", "BPM"));
		}
	}

	@Observer(ProcessBuilder.POST_DEPLOY_EVENT)
	public void updateDataPublicacaoFluxo() {
		FluxoHome fluxoHome = FluxoHome.instance();
		if (fluxoHome != null) {
			Fluxo f = fluxoHome.getInstance();
			if (f != null && f.getIdFluxo() != 0) {
				f.setUltimaPublicacao(new Date());
				EntityUtil.getEntityManager().merge(f);
				EntityUtil.flush();
			}
		}
	}

	private EntityManager getEntityManager() {
		return EntityUtil.getEntityManager();
	}

	/**
	 * Retorna a instancia da classe JbpmEventsHandler
	 * 
	 * @return
	 */
	public static JbpmEventsHandler instance() {
		return ComponentUtil.getComponent(NAME);
	}
}