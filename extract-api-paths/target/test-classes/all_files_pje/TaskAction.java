package br.com.infox.bpm.action;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.hibernate.LazyInitializationException;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.ibpm.component.tree.AutomaticEventsTreeHandler;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.EntityUtil;

/**
 * Classe abstrata que prove todos os métodos comuns a exibição de uma tarefa do
 * fluxo, tais como lista das transições ou método para finalizar a tarefa.
 * 
 * @author Daniel
 * 
 */
public abstract class TaskAction implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final LogProvider log = Logging.getLogProvider(TaskAction.class);
	private String name;
	private List<Transition> avaliableTransitions;
	private TaskInstance taskInstance;
	private TaskInstance nextTaskInstance;

	/**
	 * Obtém a lista de transições possíveis para a taskInstance atual,
	 * verificando se a expression contida dentro do campo condition_ está true.
	 * 
	 * @return Lista com as transições possíveis.
	 */
	public List<Transition> getTransitions() {
		if (avaliableTransitions == null) {
			if (getTaskInstance() != null) {
				List<Transition> transitionsList = getTaskInstance().getAvailableTransitions();
				List<Transition> leavingTransitions = getLeavingTransitions();
				/*
				 * Loop necessário para garantir a mesma ordem definida no xml
				 * do processDefinition.
				 */
				avaliableTransitions = new ArrayList<Transition>();
				for (Transition transition : leavingTransitions) {
					if (transitionsList.contains(transition) && !TaskInstanceHome.hasOcculTransition(transition)) {
						avaliableTransitions.add(transition);
					}
				}
			}
		}
		return avaliableTransitions;
	}

	/**
	 * Foi criado esse metodo porque em alguns momentos dava erro de
	 * LazyInicializationException quando usava o getTaskInstance()
	 * 
	 * @return
	 */
	private List<Transition> getLeavingTransitions() {
		try {
			return getTaskInstance().getTask().getTaskNode().getLeavingTransitions();
		} catch (LazyInitializationException e) {
			if (org.jboss.seam.bpm.TaskInstance.instance() != null) {
				return org.jboss.seam.bpm.TaskInstance.instance().getTask().getTaskNode().getLeavingTransitions();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	public void updateTransitions() {
		avaliableTransitions = null;
		getTransitions();
	}

	/**
	 * Testa se á transição está disponivel
	 * 
	 * @param transitionName
	 * @return
	 */
	public boolean canTransit(String transitionName) {
		updateTransitions();
		for (Transition transition : getTaskInstance().getAvailableTransitions()) {
			if (transition.getName().equals(transitionName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Inicia a tarefa da instância atual caso ela não esteja iniciada.
	 */
	public void start() {
		Long taskId = getTaskInstance().getId();
		BusinessProcess.instance().setTaskId(taskId);
		org.jbpm.taskmgmt.exe.TaskInstance ti = (org.jbpm.taskmgmt.exe.TaskInstance) JbpmUtil.getJbpmSession().get(
				org.jbpm.taskmgmt.exe.TaskInstance.class, taskId);
		if (ti != null) {
			if (ti.getStart() == null) {
				BusinessProcess.instance().startTask();
			}
		}
	}

	/**
	 * @param transition
	 *            Nome da transição que deseja transitar o processInstance
	 * @return
	 */
	public void end(String transition) {
		try {
			if (!canTransit(transition)) {
				// melhor lançar o AplicationException para dar rollback do que
				// deixar o Jbpm lançar o erro.
				throw new AplicationException(MessageFormat.format(
						"A transição {0} não está disponível para a tarefa {1}", transition, getTaskInstance()
								.getName()));
			}
			TaskTransitionAction tta = new TaskTransitionAction();
			tta.canEndTask(getTaskInstance());
			BusinessProcess.instance().endTask(transition);
			AutomaticEventsTreeHandler.instance().registraEventos();
			setTaskInstance(null);
			setAvaliableTransitions(null);
			setTaskInstance(tta.canSeeNextTaskInstance(nextTaskInstance));
		} catch (Exception e) {
			throw new AplicationException(MessageFormat.format("Erro ao seguir para a transição {0}: {1}", transition,
					e.getMessage()), e);
		}
	}

	/**
	 * Refeita a combobox com as transições utilizando um f:selectItem pois o
	 * componente do Seam (s:convertEntity) estava dando problemas com as
	 * entidades do JBPM.
	 * 
	 * @return Lista das transições.
	 */
	public List<SelectItem> getTranstionsSelectItems() {
		List<SelectItem> selectList = new ArrayList<SelectItem>();
		for (Transition t : getTransitions()) {
			selectList.add(new SelectItem(t.getName(), t.getName()));
		}
		return selectList;
	}

	/**
	 * Observer necessário para verificar antes de terminar a requisição se o
	 * usuário irá poder continuar visualizando essa nova tarefa.
	 * 
	 * @param context
	 */
	//@Observer(Event.EVENTTYPE_TASK_CREATE)
	public void setNewTaskInstance(ExecutionContext context) {
		try {
			nextTaskInstance = context.getTaskInstance();
		} catch (Exception ex) {
			String action = "atribuir a taskInstance corrente ao currentTaskInstance: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"setCurrentTaskInstance()", "TaskInstanceHome", "BPM"));
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setAvaliableTransitions(List<Transition> avaliableTransitions) {
		this.avaliableTransitions = avaliableTransitions;
	}

	public List<Transition> getAvaliableTransitions() {
		return avaliableTransitions;
	}

	public void setTaskInstance(TaskInstance taskInstance) {
		this.taskInstance = taskInstance;
	}

	public TaskInstance getTaskInstance() {
		if (taskInstance == null) {
			taskInstance = org.jboss.seam.bpm.TaskInstance.instance();
		}
		return taskInstance;
	}

	public EntityManager getEntityManager() {
		return EntityUtil.getEntityManager();
	}

}