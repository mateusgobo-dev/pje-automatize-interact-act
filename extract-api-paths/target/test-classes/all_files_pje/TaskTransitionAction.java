package br.com.infox.bpm.action;

import java.io.Serializable;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jbpm.taskmgmt.exe.TaskInstance;
import br.com.infox.ibpm.component.tree.TarefasTreeHandler;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.component.Util;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.EntityUtil;

/**
 * Classe responsável pelas validações na transição do fluxo.
 * 
 * @author Daniel
 * 
 */
public class TaskTransitionAction implements Serializable{

	private static final long serialVersionUID = 1L;

	/**
	 * Verifica se a mesma tarefa não foi encerrada por outro usuário, afinal se a taskInstance for diferente da tempTask obtida no ato da requisição,
	 * significa que alguém movimentou a tarefa enquanto o usuário estava com a janela aberta.
	 */
	public void canEndTask(TaskInstance currentTaskInstance){
		TaskInstance tempTask = org.jboss.seam.bpm.TaskInstance.instance();
		if (currentTaskInstance != null){
			if (tempTask == null || tempTask.getId() != currentTaskInstance.getId()){
				FacesMessages.instance().clear();
				throw new AplicationException("Você não pode mais efetuar transações "
						+ "neste registro, verifique se ele não foi movimentado");
			}
		}
	}

	/**
	 * Verifica se o usuario que está transitando o processo no fluxo, pode visualizar a próxima <code>newTaskInstance</code> informada.
	 * 
	 * @param nextTaskInstance - próxima tarefa para onde o fluxo levou o processo.
	 * @return null se não puder.
	 */
	public TaskInstance canSeeNextTaskInstance(TaskInstance nextTaskInstance){
		boolean canOpenTask = false;
		if (nextTaskInstance == null){
			Util.setToEventContext("canClosePanel", true);
		}
		else{
			if (canOpenTask(nextTaskInstance.getId())){
				canOpenTask = true;
				Util.setToEventContext("newTaskId", nextTaskInstance.getId());
			}
			else{
				Util.setToEventContext("canClosePanel", true);
			}
		}
		Util.setToEventContext("taskCompleted", true);
		return canOpenTask ? nextTaskInstance : null;
	}

	/**
	 * Verifica se a tarefa destino da transição apareceria no painel do usuario o que indicia se o ele é da localizacao/papel da swimlane da tarefa
	 * criada
	 * 
	 * @param currentTaskId
	 * @return true se ele pode visualizar a próxima tarefa
	 */
	private boolean canOpenTask(long currentTaskId){
		JbpmUtil.getJbpmSession().flush();
		Events.instance().raiseEvent(TarefasTreeHandler.FILTER_TAREFAS_TREE);
		StringBuilder sql = new StringBuilder("");
		sql.append(" select count(o) from SituacaoProcesso o ");
		sql.append(" where o.idTaskInstance = :ti ");
		Query query = EntityUtil.getEntityManager().createQuery(sql.toString());
		query.setParameter("ti", currentTaskId);
		try {
			Long result = (Long)query.getSingleResult();
			return result > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

}