/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.TaskInstanceDAO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.Fluxo;

@Name(TaskInstanceManager.NAME)
public class TaskInstanceManager extends BaseManager<TaskInstance> {
	
	public static final String NAME = "taskInstanceManager";
	
	@In
	private TaskInstanceDAO taskInstanceDAO;

	@Override
	protected BaseDAO<TaskInstance> getDAO() {
		return taskInstanceDAO;
	}
	
	public void limparActorId(TaskInstance taskInstance) {
		taskInstanceDAO.limparActorId(taskInstance);
	}
	
	public List<String> obterNomeNosTarefas(Long idTaskInstance) {
		return this.taskInstanceDAO.obterNomeNosTarefas(idTaskInstance);
	}
	
	public Fluxo getHistoricoTarefas(Integer idProcesso) {
		return this.taskInstanceDAO.getHistoricoTarefas(idProcesso);
	}
	
	public List<org.jbpm.taskmgmt.exe.TaskInstance> getTarefasProcessoAsc(Integer idProcesso){
		return this.taskInstanceDAO.getTarefasProcesso(idProcesso, "ASC");
	}

	public List<org.jbpm.taskmgmt.exe.TaskInstance> getTarefasProcessoDesc(Integer idProcesso){
		return this.taskInstanceDAO.getTarefasProcesso(idProcesso, "DESC");
	}

	public org.jbpm.taskmgmt.exe.TaskInstance getUltimaTarefaProcesso(Integer idProcesso){
		return this.taskInstanceDAO.getUltimaTarefa(idProcesso);
	}
	
	public List<org.jbpm.taskmgmt.exe.TaskInstance> getTarefasProcessoByIdProcessInstanceAsc(Long idProcessInstance){
		return this.taskInstanceDAO.getTaskInstanceByIdProcessInstance(idProcessInstance, "ASC");
	}
	
	public List<org.jbpm.taskmgmt.exe.TaskInstance> getTarefasProcessoByIdProcessInstanceDesc(Long idProcessInstance){
		return this.taskInstanceDAO.getTaskInstanceByIdProcessInstance(idProcessInstance, "DESC");
	}
	
}
