package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.log.Log;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.util.TransitionComparator;
import br.jus.pje.nucleo.entidades.ProcessoInstance;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * @author cristof
 * 
 */
@Name(TaskInstanceUtil.NAME)
@Scope(ScopeType.EVENT)
public class TaskInstanceUtil {

	static final String NAME = "taskInstanceUtil";

	@In(create = true, required = false)
	private TaskInstance taskInstance;

	@Logger
	private Log logger;

	public String getTaskChain() {
		StringBuilder sb = new StringBuilder();
		if (taskInstance != null) {
			ProcessInstance pi = getProcessInstance();
			sb.append(taskInstance.getName());
			Token tk = pi.getSuperProcessToken();
			while (tk != null && tk.getNode() != null) {
				sb.insert(0, tk.getNode().getName() + " \u2022 ");
				tk = tk.getProcessInstance().getSuperProcessToken();
			}
		}
		return sb.toString();
	}

	public ProcessInstance getProcessInstance() {
		if (taskInstance != null) {
			ProcessInstance pi = taskInstance.getProcessInstance();
			return pi;
		}
		return null;
	}

	public void setDefaultTransition(String name, String value) {
		if (taskInstance != null) {
			taskInstance.setVariableLocally(name, value);
		}
	}

	public void setFrameDefaultTransition(String value) {
		if (taskInstance != null) {
			setDefaultTransition(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION, value);
		}
	}

	public void setVariable(String name, Object value) {
		if (taskInstance != null) {
			taskInstance.setVariableLocally(name, value);
		} else {
			logger.error(
					"Não foi possível criar a variável [{0}] com o valor [{1}].",
					name, value);
		}
	}

	public Object getVariable(String name) {
		if (taskInstance != null) {
			return taskInstance.getVariableLocally(name);
		} else {
			logger.error("Não foi possível buscar a variável [{0}].", name);
			return null;
		}
	}

	/**
	 * Método responsável por excluir a variável especificada da tarefa
	 * 
	 * @param name
	 *            Nome da variável a ser excluída da tarefa
	 */
	public void deleteVariableLocally(String name) {
		if (taskInstance != null) {
			taskInstance.deleteVariableLocally(name);
			JbpmContext context = JbpmConfiguration.getInstance()
					.getCurrentJbpmContext();
			context.getSession().flush();
		} else {
			logger.error("Não foi possível apagar a variável [{0}].", name);
		}
	}

	public static TaskInstanceUtil instance() {
		return (TaskInstanceUtil) org.jboss.seam.Component
				.getInstance(TaskInstanceUtil.NAME);
	}
	
	public List<org.jbpm.taskmgmt.exe.TaskInstance> retornaTasks(){
		Usuario usu = Authenticator.getUsuarioLogado();
		if(usu == null){
			return null;
		}
		return retornaTasks(usu);
	}
	
	public List<org.jbpm.taskmgmt.exe.TaskInstance> retornaTasks(Usuario usuario){
		if(usuario == null){
			return new ArrayList<org.jbpm.taskmgmt.exe.TaskInstance>();
		}
		ParametroService parametroService = (ParametroService)Component.getInstance(ParametroService.class);
		String param = parametroService.valueOf("pje:painel:ultimasTarefas:quantidade");
		Integer qtdRes = null;
		if(param != null){
			qtdRes = Integer.parseInt(param);
		}
		if(qtdRes == null){
			qtdRes = 10;
		}
		List<org.jbpm.taskmgmt.exe.TaskInstance> lista = new ArrayList<org.jbpm.taskmgmt.exe.TaskInstance>();
		Session session  = ManagedJbpmContext.instance().getSession();
		org.hibernate.Query q = session.createQuery("select ti from org.jbpm.taskmgmt.exe.TaskInstance ti " +
				" where ti.end is not null and actorId  = (:actorId)  order by ti.end desc");
		q.setParameter("actorId", usuario.getLogin());
		q.setMaxResults(qtdRes);
		lista.addAll(q.list());
		return lista;
	}
	
	public ProcessoTrf getProcesso(Long processInstanceId){
		ProcessoTrf processoTrf = null;
		try {
			ProcessoInstanceManager processoInstanceManager = (ProcessoInstanceManager)Component.getInstance(ProcessoInstanceManager.class);
			ProcessoInstance processo = processoInstanceManager.findById(processInstanceId);
			if(processo != null){
				ProcessoJudicialManager pjm = (ProcessoJudicialManager)Component.getInstance(ProcessoJudicialManager.class);
				processoTrf = pjm.findById(processo.getIdProcesso());
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return processoTrf;
	}

	@SuppressWarnings("unchecked")
	public String getNomeTarefasAtuais(int idProcessoJudicial){
		if(idProcessoJudicial == 0){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Query q = EntityUtil.getEntityManager().createQuery("select distinct o.nomeTask from ConsultaProcessoIbpm o where o.idProcesso = :idProcesso order by o.nomeTask");
		q.setParameter("idProcesso", idProcessoJudicial);
		List<String> lista = q.getResultList();
		for (String nomeTarefa : lista) {
			if(sb.length() > 0) {
				sb.append(", <BR/>");
			}
			sb.append(nomeTarefa);
		}

		return sb.toString();
	}
	
	public List<String> getTransitions(Long idTaskInstance){
		TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idTaskInstance);
		BusinessProcess.instance().resumeProcess(taskInstance.getProcessInstance().getId());
		List<Transition> availableTransitions = taskInstance.getAvailableTransitions();
		List<Transition> leavingTransitions = taskInstance.getTask().getTaskNode().getLeavingTransitions();

		List<String> ret = new ArrayList<String>();

		if (availableTransitions == null){
			return ret;
		}
		
		if (ParametroUtil.instance().isOrdenarTransicoesAlfabeticamente()) {
			Collections.sort(leavingTransitions, new TransitionComparator());
		}
		
		// pega da definicao para garantir a mesma ordem do XML
		for (Transition transition : leavingTransitions){
			if (availableTransitions.contains(transition) && !"#{true}".equals(transition.getCondition())){
				ret.add(transition.getName());
			}
		}
		return ret;
	}

	/**
	 * @return valor da variável 'frameDefaultLeavingTransition'.
	 */
	public String getVariableFrameDefaultLeavingTransition() {
		return (String) getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
	}
}
