/*
 * IBPM - Ferramenta de produtividade Java Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda. Este programa é software livre; você pode
 * redistribuí-lo e/ou modificá-lo sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela Free Software Foundation; versão 2 da
 * Licença. Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * COMERCIABILIDADE OU ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a GNU GPL para mais detalhes. Você deve ter recebido uma cópia da GNU GPL junto
 * com este programa; se não, veja em http://www.gnu.org/licenses/
 */
package br.com.infox.ibpm.jbpm;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jbpm.JbpmContext;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.context.exe.VariableInstance;
import org.jbpm.db.GraphSession;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Node.NodeType;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.persistence.db.DbPersistenceService;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.exe.SwimlaneInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.JbpmVariavelLabel;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.Status;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import java.lang.reflect.Method;
import org.hibernate.FlushMode;

@Name(JbpmUtil.NAME)
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Install(dependencies = {})
public class JbpmUtil{

	private static final LogProvider log = Logging.getLogProvider(JbpmUtil.class);

	public static final String NAME = "jbpmUtil";
	public static final int FROM_TASK_TRANSITION = 0;
	public static final int TO_TASK_TRANSITION = 1;
	private static final String VAR_NOME_TAREFA_ANTERIOR = "nomeTarefaAnterior";
	private static Map<String, String> messagesMap;

	/**
	 * Busca a localização de uma tarefa
	 * 
	 * @param task
	 * @return
	 */
	public Localizacao getLocalizacao(TaskInstance task){
		SwimlaneInstance swimlaneInstance = task.getSwimlaneInstance();
		if (swimlaneInstance != null){
			String expression = swimlaneInstance.getSwimlane().getPooledActorsExpression();
			if (expression == null)
				return null;
			// TODO: verificar se pode ser dado um tratamento melhor
			String localizacaoId = expression.substring(expression.indexOf("(") + 1);
			localizacaoId = localizacaoId.substring(0, localizacaoId.lastIndexOf(")"));
			if (localizacaoId.indexOf(":") > 0){
				localizacaoId = localizacaoId.replaceAll("'", "");
				localizacaoId = localizacaoId.split(":")[0];
			}
			Localizacao localizacao = EntityUtil.find(Localizacao.class, new Integer(localizacaoId));
			return localizacao;
		}
		return null;
	}

	/**
	 * Busca a localização de um processo
	 * 
	 * @param jbpmProcessId é o id do Processo no contexto jBPM (Processo.getIdJbpm())
	 * @return retorna a primeira localização encontrada
	 */
	public Localizacao getLocalizacao(long jbpmProcessId){
		ProcessInstance pi = ManagedJbpmContext.instance().getProcessInstance(jbpmProcessId);
		Token token = pi.getRootToken();
		for (org.jbpm.taskmgmt.exe.TaskInstance t : pi.getTaskMgmtInstance().getTaskInstances()){
			if (t.getTask().getTaskNode().equals(token.getNode())){
				return getLocalizacao(t);
			}
		}
		return null;
	}

	public static Session getJbpmSession(){
		return ManagedJbpmContext.instance().getSession();
	}

	@Factory(value = "jbpmMessages", scope = ScopeType.APPLICATION)
	public Map<String, String> getMessages(){
		return getJbpmMessages();
	}

	public static Map<String, String> getJbpmMessages(){
		if (messagesMap == null){
			Map<String, String> map = new HashMap<String, String>();
			List<JbpmVariavelLabel> l = EntityUtil.getEntityList(JbpmVariavelLabel.class);
			for (JbpmVariavelLabel j : l){
				map.put(j.getNomeFluxo() + j.getNomeTarefa() + j.getNomeVariavel(), j.getLabelVariavel());
			}
			messagesMap = map;
		}
		return messagesMap;
	}

	// TODO verificar por que tem registro duplicado na base
	public void storeLabel(String name, String label){
		Map<String, String> map = ComponentUtil.getComponent("jbpmMessages");
		String old = map.get(name);
		if (old != null || !label.equals(old)){
			map.put(name, label);
			JbpmVariavelLabel j = new JbpmVariavelLabel();
			j.setNomeVariavel(name);
			j.setLabelVariavel(label);
			EntityManager em = EntityUtil.getEntityManager();
			em.joinTransaction();
			em.persist(j);
            em.flush();
			
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> getProcessNames(){
		StringBuilder sb = new StringBuilder();
		sb.append("select pd.name ");
		sb.append("from org.jbpm.graph.def.ProcessDefinition as pd ");
		sb.append("group by pd.name order by pd.name");
		Session session = ManagedJbpmContext.instance().getSession();
		List<String> l = session.createQuery(sb.toString()).list();
		return l;
	}

	@SuppressWarnings("unchecked")
	public List<TaskInstance> getAllTasks(){
		StringBuilder sb = new StringBuilder();
		sb.append("select ti from org.jbpm.taskmgmt.exe.TaskInstance ti ");
		sb.append("where ti.isSuspended = false ");
		sb.append("and ti.isOpen = true ");
		sb.append("order by ti.name");
		return getJbpmSession().createQuery(sb.toString()).list();
	}

	@SuppressWarnings("unchecked")
	public static <T>T getProcessVariable(String name){
		ProcessInstance processInstance = org.jboss.seam.bpm.ProcessInstance.instance();
		if (processInstance != null){
			ContextInstance contextInstance = processInstance.getContextInstance();
			T value = (T) contextInstance.getVariable(name);
			return value;
		}
		else{
			return null;
		}
	}

	public static void setProcessVariable(String name, Object value){
		ContextInstance contextInstance = org.jboss.seam.bpm.ProcessInstance.instance().getContextInstance();
		contextInstance.setVariable(name, value);
	}

	public static void createProcessVariable(String name, Object value){
		ContextInstance contextInstance = org.jboss.seam.bpm.ProcessInstance.instance().getContextInstance();
		contextInstance.createVariable(name, value);
	}

	/**
	 * Retorna as tarefas (from / to) de uma transição Pode ocorrer null quando algum dos nós não é de tarefa
	 */
	public static Task[] getTasksFromTransition(Transition t){
		Task[] ret = new Task[2];
		ret[0] = getTaskFromNode(t.getFrom());
		ret[1] = getTaskFromNode(t.getTo());
		return ret;
	}

	/**
	 * Retorna a tarefas de um nó Pode ocorrer null quando o nó não é de tarefa
	 */
	public static Task getTaskFromNode(Node node){
		Task t = null;
		if (node.getNodeType().equals(NodeType.Task)){
			TaskNode tn = (TaskNode) JbpmUtil.getJbpmSession().load(TaskNode.class, node.getId());
			if (!tn.getTasks().isEmpty()){
				t = tn.getTasks().iterator().next();
			}
		}
		return t;
	}

	public String valorProcessoDocumento(Integer idProcDoc){
		String conteudoProcDoc = null;
		ProcessoDocumento processoDocumento = EntityUtil.find(ProcessoDocumento.class, idProcDoc);
		if (processoDocumento == null){
			log.warn("ProcessoDocumento não encontrado: " + idProcDoc);
		}else{
			conteudoProcDoc = processoDocumento.getProcessoDocumentoBin().getModeloDocumento(); 
		}
		return conteudoProcDoc;
	}
	
	public Integer recuperarIdDocumentoEmElaboracao(TaskInstance taskInstance, String variavel) {
		Integer idMinuta = null;
		Object idMinutaObj = taskInstance.getProcessInstance().getContextInstance().getVariable(variavel);
		if(idMinutaObj != null) {
			if(idMinutaObj instanceof String) {
				idMinuta = Integer.valueOf((String) idMinutaObj);
			}else {
				idMinuta = (Integer) idMinutaObj;
			}
		}
		return idMinuta;
	}
	
	public Integer recuperarIdMinutaEmElaboracao(TaskInstance taskInstance){
		return recuperarIdDocumentoEmElaboracao(taskInstance, Variaveis.MINUTA_EM_ELABORACAO);
	}
	
	public void apagaMinutaEmElaboracao(TaskInstance taskInstance) {
		taskInstance.getProcessInstance().getContextInstance().deleteVariable(Variaveis.MINUTA_EM_ELABORACAO);
	}
	
	public ProcessoDocumento recuperarMinutaEmElaboracao(TaskInstance taskInstance){
		ProcessoDocumento pd = recuperarProcessoDocumento(taskInstance, recuperarIdMinutaEmElaboracao(taskInstance)); 
		if(pd == null){
			apagaMinutaEmElaboracao(taskInstance);
		}
		return pd;
	}
	
	public Object getConteudoMinutaEmElaboracao(Integer idMinuta){
		return valorProcessoDocumento(idMinuta);
	}
	
	/**
	 * Tenta recuperar o processo documento de uma dada tarefa e um idProcDoc
	 *  
	 * @param taskInstance
	 * @param idProcDoc
	 * @return
	 */
	public ProcessoDocumento recuperarProcessoDocumento(TaskInstance taskInstance, Integer idProcDoc) {
		ProcessoDocumento processoDocumento = null;
		
		if (idProcDoc != null) {
			processoDocumento = EntityUtil.find(ProcessoDocumento.class, idProcDoc);

			if (processoDocumento != null && !processoDocumento.getAtivo()) {
				processoDocumento = null;
			}
		} 

		return processoDocumento;
	}
	
	public Object getConteudo(VariableAccess var, TaskInstance taskInstance){
		String[] tokens = var.getMappedName().split(":");
		String type = tokens[0];
		String name = tokens[1];		
		Object variable = taskInstance.getVariable(var.getMappedName());

		if (isTypeEditor(type, name)){
			Integer id = null;
			if (variable instanceof Integer){
				id = (Integer) variable;
			}
			if (id != null){
				variable = valorProcessoDocumento(id);
			}
		}
		return variable;
	}

	public static JbpmUtil instance(){
		return (JbpmUtil) ComponentUtil.getComponent(NAME, ScopeType.APPLICATION);
	}

	public static GraphSession getGraphSession(){
		return new GraphSession(getJbpmSession());
	}

	public static Processo getProcesso(){
		Integer idProcesso = getIdProcesso();
		return idProcesso != null ? EntityUtil.find(Processo.class, idProcesso) : null;
	}

	public static Integer getIdProcesso() {
		Integer idProcesso = JbpmUtil.getProcessVariable("processo");
		return idProcesso;
	}

	@SuppressWarnings("unchecked")
	public static List<Task> getTasksForLocalizacaoAtual(){
		UsuarioLocalizacao loc = (UsuarioLocalizacao) Contexts.getSessionContext().get("usuarioLogadoLocalizacaoAtual");
		StringBuilder sb = new StringBuilder();
		sb.append("select t.* from JBPM_TASK t, JBPM_SWIMLANE s ");
		sb.append("where t.SWIMLANE_=s.ID_ and (t.PROCESSDEFINITION_ in (");
		sb.append("select max(p.ID_) from JBPM_PROCESSDEFINITION p ");
		sb.append("group by p.NAME_)) and (s.POOLEDACTORSEXPRESSION_ ");
		sb.append("like :param");
		sb.append(") ");
		String param = "%" + loc.getLocalizacaoModelo().getIdLocalizacao() + ":%";
		List<Task> list = JbpmUtil.getJbpmSession().createSQLQuery(sb.toString()).addEntity(Task.class)
				.setString("param", param).list();
		return list;
	}

	/**
	 * 
	 * @param tarefa Nome da Tarefa
	 * @param fluxo Nome do Fluxo
	 * @return Retorna a entidade Tarefa referente a tarefa do fluxo informado.
	 */
	public static Tarefa getTarefa(String tarefa, String fluxo){
		if (tarefa == null || fluxo == null){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select t from Tarefa t where t.tarefa = :tarefa and ").append("t.fluxo.fluxo = :fluxo");
		Query q = EntityUtil.createQuery(sb.toString());
		q.setParameter("tarefa", tarefa);
		q.setParameter("fluxo", fluxo);
		return (Tarefa) EntityUtil.getSingleResult(q);
	}

	/**
	 * @param idJbpmTask Id da Task do jbpm
	 * @return Devolve a Tarefa relacionada com a task do Jbpm
	 */
	public static Tarefa getTarefa(long idJbpmTask){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Tarefa o ");
		sb.append("inner join o.tarefaJbpmList tJbpm ");
		sb.append("where tJbpm.idJbpmTask = :idJbpmTask");
		Query query = EntityUtil.createQuery(sb.toString());
		return EntityUtil.getSingleResult(query.setParameter("idJbpmTask", idJbpmTask));
	}

	/**
	 * 
	 * @param processo
	 * @return Retorna a tarefa anterior do processo
	 */
	public static Tarefa getTarefaAnterior(Processo processo){
		Query query = EntityUtil
				.createQuery("select o.idPreviousTask from SituacaoProcesso o where o.idProcesso = :idProcesso");
		Integer idJbpmTaskAnterior = EntityUtil.getSingleResult(query.setParameter("idProcesso",
				processo.getIdProcesso()));
		return idJbpmTaskAnterior != null ? getTarefa(idJbpmTaskAnterior.longValue()) : null;
	}

	/**
	 * @param processo
	 * @return Retorna o nome da tarefa anterior no fluxo
	 */
	public String getNomeTarefaAnterior(Processo processo){
		Tarefa tarefaAnterior = getTarefaAnterior(processo);
		return tarefaAnterior != null ? tarefaAnterior.getTarefa() : null;
	}

	/**
	 * @param processo
	 * @return Retorna o nome da tarefa anterior no fluxo
	 */
	@Factory(scope = ScopeType.EVENT, value = VAR_NOME_TAREFA_ANTERIOR)
	public String getNomeTarefaAnterior(){
		Processo processo = JbpmUtil.getProcesso();
		Tarefa tarefaAnterior = getTarefaAnterior(processo);
		return tarefaAnterior != null ? tarefaAnterior.getTarefa() : null;
	}

	public boolean checkNomeTarefaAnterior(String... nomeTarefas){
		String nomeTarefaAnterior = getNomeTarefaAnterior();
		for (String tarefa : nomeTarefas){
			if (tarefa.equals(nomeTarefaAnterior)){
				return true;
			}
		}
		return false;
	}

	public String getNomeTarefaAnteriorFromCurrentExecutionContext(){
		Transition transition = getCurrentTransition();
		Node from = null;
		if (transition != null && (from = transition.getFrom()) != null){
			return from.getName();
		}
		return null;
	}

	public Transition getCurrentTransition(){
		ExecutionContext currentExecutionContext = ExecutionContext.currentExecutionContext();
		if (currentExecutionContext != null){
			return currentExecutionContext.getTransition();
		}
		return null;
	}

	/**
	 * Obtem o evento desejado informando o nome do evento.
	 * 
	 * @param evento Descrição do evento.
	 * @return Evento
	 */
	public static Evento getEvento(String evento){
		String hql = "select e from Evento e where e.evento = :dsEvento";
		Query q = EntityUtil.createQuery(hql);
		q.setParameter("dsEvento", evento);
		return EntityUtil.getSingleResult(q);
	}

	/**
	 * Obtem o evento desejado informando o nome do evento.
	 * 
	 * @param status Descrição do evento.
	 * @return Evento
	 */
	public static Status getStatus(String status){
		String hql = "select s from Status s where s.status = :status";
		Query q = EntityUtil.createQuery(hql);
		q.setParameter("status", status);
		return EntityUtil.getSingleResult(q);
	}

	/**
	 * Resume a instancia de uma tarefa e devolve o taskInstance da mesma.
	 * 
	 * @param idTaskInstance
	 * @return
	 */
	public static TaskInstance resumeTask(Long idTaskInstance){
		BusinessProcess.instance().setTaskId(idTaskInstance);

		// Mudança de assinatura entre as versões do seam ORG e EAP
		try{
			BusinessProcess.instance().resumeTask(idTaskInstance);
		} catch (Exception e){
			throw new RuntimeException(e);
		}

		return org.jboss.seam.bpm.TaskInstance.instance();
	}

	/**
	 * Verifica se uam transição de destino está disponível
	 * 
	 * @param taskInstance
	 * @param transitionDestino
	 * @return
	 */
	public static boolean canTransitTo(TaskInstance taskInstance, String transitionDestino){
		List<Transition> availableTransitions = taskInstance.getAvailableTransitions();
		for (Transition transition : availableTransitions){
			if (transition.getName().equals(transitionDestino)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica se uam transição de destino existe. Ela pode não estar disponível.
	 * 
	 * @param taskInstance
	 * @param transitionDestino
	 * @return
	 */
	public static boolean transitionExists(TaskInstance taskInstance, String transitionDestino){
		List<Transition> transitionList = taskInstance.getTask().getTaskNode().getLeavingTransitions();
		for (Transition transition : transitionList){
			if (transition.getName().equals(transitionDestino)){
				return true;
			}
		}
		return false;
	}

	public static boolean isTypeEditor(String type, String name){
		return type.equals("textEditCombo") || type.equals("textEditComboDESCONTINUADO") 
				|| type.equals("textEditSignature") || type.equals("textEditSignatureDESCONTINUADO")
				|| type.equals("textEditAndAttachmentSignature") || type.equals("textEditAndAttachmentSignatureDESCONTINUADO")
				|| type.equals("textEditSecretariaJT") 
				|| type.equals("textEditGabineteJT") 
				|| (type.equals("frame") && name.equals("Processo_Fluxo_revisarMinuta") );
	}

	public static boolean isTypeEditorEstruturado(String type){
		return type.equals("textEditGabineteJTEstruturado");
	}

	public void storeLabel(String processDefinition, String task, String variable, String label){
		Map<String, String> map = ComponentUtil.getComponent("jbpmMessages");
		String old = map.get(processDefinition + task + variable);
		if (old != null || !label.equals(old)){
			map.put(processDefinition + task + variable, label);
			JbpmVariavelLabel j = new JbpmVariavelLabel();
			j.setNomeFluxo(processDefinition);
			j.setNomeTarefa(task);
			j.setNomeVariavel(variable);
			j.setLabelVariavel(label);
			EntityManager em = EntityUtil.getEntityManager();
			em.joinTransaction();
			em.persist(j);
            em.flush();
		}

	}

	/**
	 * @author Rafael Carvalho (CSJT) [PJEII-364] Processos estão ficando inconsistentes, sem estar em nenhuma tarefa do fluxo - JIRA CNJ. Limpa
	 *         sessao do hibernate (JBPM) e fecha o contexto antes de dar o flush.
	 * 
	 * @param context
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void clearAndClose(JbpmContext context){
		try{
			log.info("Pegando o DBPersistenceService do contexto JBPM");
			DbPersistenceService dbPersistenceService = (DbPersistenceService) context.getServices().getPersistenceService();
			// Dar clean na sessao do JBPM.
			Session s = dbPersistenceService.getSession();
			log.info("Sessao do Hibernate: " + s);
			if (s != null && s.isOpen()){
				s.clear();
				log.info("Sessao do Hibernate limpa");
			}

			limparAutoSaveProcessInstances(context);
			// fechando o contexto para evitar flush.
			context.close();
		} catch (Exception e){
			log.error("Erro ao limpar o contexto JBPM", e);
			throw new AplicationException("Erro ao limpar e fechar o contexto JBPM. metodo clearAndClose da classe JbpmUtil", e);
		}
	}
	
	/**
	 * @author Rafael Carvalho (CSJT) [PJEII-364] Processos estão ficando inconsistentes, sem estar em nenhuma tarefa do fluxo - JIRA CNJ. Limpa
	 *         sessao do hibernate (JBPM) e fecha o contexto antes de dar o flush.
	 * 
	 * @param context
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void clearWithoutClose(JbpmContext context) {
		if (context == null) {
			log.warn("O contexto JBPM é nulo. Nenhuma ação será realizada.");
			return;
		}

		try {
			log.info("Obtendo o DBPersistenceService do contexto JBPM.");
			DbPersistenceService dbPersistenceService = (DbPersistenceService) context.getServices().getPersistenceService();

			if (dbPersistenceService != null) {
				Session session = dbPersistenceService.getSession();
				log.info("Sessão do Hibernate: " + session);

				if (session != null && session.isOpen()) {
					session.clear();
					log.info("Sessão do Hibernate limpa.");
				}
			} else {
				log.warn("DbPersistenceService nulo. Não foi possível obter a sessão do Hibernate.");
			}

			limparAutoSaveProcessInstances(context);
		} catch (Exception e) {
			log.error("Erro ao limpar o contexto JBPM: ", e);
			throw new AplicationException("Erro ao limpar o contexto JBPM no método clearWithoutClose.", e);
		}
	}

	private static void limparAutoSaveProcessInstances(JbpmContext context) {
		if (context == null) {
			log.warn("O contexto JBPM é nulo. Nenhuma ação será realizada.");
			return;
		}

		try {
			Field field = context.getClass().getDeclaredField("autoSaveProcessInstances");
			field.setAccessible(true);

			@SuppressWarnings("unchecked")
			List<ProcessInstance> autoSaveProcessInstances = (List<ProcessInstance>) field.get(context);

			if (autoSaveProcessInstances != null && !autoSaveProcessInstances.isEmpty()) {
				log.info("Lista de ProcessInstance antes da limpeza: " + autoSaveProcessInstances);
				autoSaveProcessInstances.clear();
				log.info("Lista de ProcessInstance limpa com sucesso.");
			}

			field.setAccessible(false);
		} catch (NoSuchFieldException e) {
			log.error("O campo autoSaveProcessInstances não foi encontrado no contexto JBPM: ", e);
			throw new AplicationException("Erro ao acessar o campo autoSaveProcessInstances no contexto JBPM.", e);
		} catch (IllegalAccessException e) {
			log.error("Erro ao acessar o campo autoSaveProcessInstances: ", e);
			throw new AplicationException("Erro ao acessar o campo autoSaveProcessInstances no contexto JBPM.", e);
		} catch (Exception e) {
			log.error("Erro inesperado ao limpar o contexto JBPM: ", e);
			throw new AplicationException("Erro inesperado ao limpar o contexto JBPM.", e);
		}
	}

	public static void limpaSessaoSemFecharContexto(JbpmContext context) {
		try {
			DbPersistenceService dbPersistenceService = (DbPersistenceService) context.getServices()
					.getPersistenceService();

			StringBuilder messageLog = new StringBuilder();

			// Dar clean na sessao do JBPM.
			Session s = dbPersistenceService.getSession();
			if (s != null && s.isOpen()) {
				s.clear();
				messageLog.append("Efetuado limpeza em: ");
				messageLog.append("Sessão do Hibernate");
			}

			// limpar a pilha de ProcessInstance para autoSave do jbpmContext.
			// Necessario reflexao, pois o atributo e protected.
			Field field;
			field = context.getClass().getDeclaredField("autoSaveProcessInstances");
			// setando o atributo para public
			field.setAccessible(true);

			@SuppressWarnings("unchecked")
			List<ProcessInstance> autoSaveProcessInstances = (List<ProcessInstance>) field.get(context);
			if (autoSaveProcessInstances != null && !autoSaveProcessInstances.isEmpty()) {
				autoSaveProcessInstances.clear();
				if (messageLog != null && messageLog.toString().length() > 0) {
					messageLog.append(", Lista de ProcessInstance.");
				} else {
					messageLog.append("Efetuado limpeza em: ");
					messageLog.append("Lista de ProcessInstance.");
				}
			}

			if (messageLog != null && messageLog.toString().length() > 0) {
				log.info(messageLog.toString());
			}

			// voltando para nao publico.
			field.setAccessible(false);
		} catch (Exception e) {
			log.error("Erro ao limpar o contexto JBPM", e);
			throw new AplicationException(
					"Erro ao limpar e fechar o contexto JBPM. metodo clearAndClose da classe JbpmUtil", e);
		}
	}

	public static void saveFlushAndClear(JbpmContext context){
		saveFlushAndClear(context, true);
	}
	
	public static void saveFlushAndClear(JbpmContext context, boolean clear){
		try{
			try {
				// limpar a pilha de ProcessInstance para autoSave do jbpmContext.
				// Necessario reflexao, pois o atributo e protected.
				Method methodAutoSave = context.getClass().getDeclaredMethod("autoSave");
				methodAutoSave.setAccessible(true);
				methodAutoSave.invoke(context);
				
			} catch (Exception ex) {
				if (clear) {
				limparAutoSaveProcessInstances(context);
				}
				throw ex;
				
			} finally {
				Session session = context.getSession();
				if (session!=null) {
					try {
						if (session.isOpen()) {
						if (session.getFlushMode().lessThan(FlushMode.ALWAYS))
							session.flush();
						}
					} finally {
						if (clear) {
						session.clear();
					}
				}
			}
			}
			
		} catch (Exception e){
			log.error("Erro ao salvar o contexto JBPM", e);
			throw new AplicationException("Erro ao limpar e fechar o contexto JBPM. metodo clearAndClose da classe JbpmUtil", e);
		}
	}

	/**
	 * Verifica se o contexto atual está sendo executado dentro de uma tarefa (frame)
	 * @return true caso o contexto atual esteja sendo executado dentro de uma tarefa. 
	 */
    public static boolean isExecutandoEmFrameTarefa() {
		try{
			ProcessInstance pi = org.jboss.seam.bpm.ProcessInstance.instance();
			org.jbpm.taskmgmt.exe.TaskInstance ti = org.jboss.seam.bpm.TaskInstance.instance();
			return (pi != null && ti != null);
		}			
		catch(Exception e){
			return false;	
		}
	}

    /**
     * Restaura as variáveis da tarefa para evitar erros de LazyInitializationException e InvalidClassException.
     * Segue abaixo as medidas adotadas em cada caso.
     * - LazyInitializationException: a entidade será consultada novamente.
     * - InvalidClassException: a classe foi alterada e o objeto serializado não é mais compatível 
     * com a classe do classloader, neste caso se a variável será removida para evitar o erro.
     *
     * @param taskInstance
     */
    public static void restaurarVariaveis(TaskInstance taskInstance) {

        if (taskInstance != null) {
            ContextInstance contextInstance = taskInstance.getContextInstance();
            if (contextInstance == null) {
                return;
            }

            String variavelKey = null;
            Object variavelVal = null;

            List<VariableInstance> variaveis = consultarVariaveis(taskInstance);
            // Loop nas variáveis para efetuar a leitura e capturar possíveis exceções.
            for (VariableInstance variavel : variaveis) {
                try {
                    if (variavel == null) {
                        continue;
                    }

                    variavelKey = variavel.getName();
                    variavelVal = variavel.getValue();
                    if (variavelVal != null) {
                        String.valueOf(variavelVal);
                    }
                } catch (LazyInitializationException e) {
                    setProcessVariable(variavelKey, restaurarVariavel(variavelVal));
                } catch (Exception e) { // Erro de serializable, ou seja, versionamento de classe.
                    if (variavelKey != null) {
                        contextInstance.deleteVariable(variavelKey);
                    }
                } finally {
                    // Garante que as variáveis locais sejam limpas
                    variavelKey = null;
                    variavelVal = null;
                }
            }
        }
    }

    /**
     * Consulta as variáveis da tarefa.
     *
     * @param taskInstance Tarefa.
     * @return Lista de VariableInstance.
     */
    @SuppressWarnings("unchecked")
    private static List<VariableInstance> consultarVariaveis(TaskInstance taskInstance) {
        if (taskInstance == null || taskInstance.getProcessInstance() == null) {
            return Collections.emptyList();
        }
        ProcessInstance processInstance = taskInstance.getProcessInstance();
        long processInstanceId = processInstance.getId();

        StringBuilder hql = new StringBuilder();
        hql.append("select o ");
        hql.append("from org.jbpm.context.exe.VariableInstance o ");
        hql.append("where  ");
        hql.append("    o.processInstance.id = :processInstanceId ");

        org.hibernate.Query query = getJbpmSession().createQuery(hql.toString());
        query.setParameter("processInstanceId", processInstanceId);

        try {
            return query.list();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Restaura uma variável do tipo entidade presente no fluxo, isso é necessário para evitar 
     * problemas de LazyInitializationException.
     *
     * @param variavel Variável do fluxo.
     * @return Objeto da variável.
     */
    @SuppressWarnings({ "rawtypes" })
    private static Object restaurarVariavel(Object variavel) {
        Object resultado = variavel;

        if (variavel != null) {

            if (EntityUtil.isEntity(variavel)) {
                resultado = EntityUtil.refreshEntity(variavel);
            } else if (variavel instanceof Map) {
                Map mapa = (Map) variavel;
                Map<Object, Object> novo = new HashMap<>();
                Iterator iterator = mapa.keySet().iterator();
                while (iterator.hasNext()) {
                    Object key = iterator.next();
                    Object val = mapa.get(key);

                    novo.put(restaurarVariavel(key), restaurarVariavel(val));
                }
                resultado = novo;
            }
        }
        return resultado;
    }

	public static void desbloqueia(TaskInstance ti) {
		if (ti == null) {
			log.warn("TaskInstance é nulo. Não é possível desbloquear.");
			return;
		}
		try {
			desbloqueia(ti.getToken());
		} catch (Exception e) {
			log.error("Erro ao tentar desbloquear o Token do TaskInstance: {0}", e);
		}
	}

	public static void desbloqueia(Token tk) {
		if (tk == null) {
			log.warn("Token é nulo. Não é possível desbloquear.");
			return;
		}
		try {
			if (tk.isLocked()) {
				tk.foreUnlock();
				log.info("Token desbloqueado com sucesso: " + tk.getId());
			} else {
				log.debug("Token não está bloqueado: " + tk.getId());
			}
		} catch (Exception e) {
			log.error("Erro ao tentar desbloquear o Token: {0}", e);
		}
	}
}
