package br.jus.cnj.pje.nucleo.manager;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

@Name(ProcessInstanceUtil.NAME)
@Scope(ScopeType.EVENT)
public class ProcessInstanceUtil{

	static final String NAME = "processInstanceUtil";

	@In(create = true, required = false)
	private TaskInstance taskInstance;

	@In(create = true, required = false)
	private ProcessInstance processInstance;
	
	@Logger
	private Log logger;
	
	public void removeVariable(String name) {
		if (taskInstance != null
				&& taskInstance.getProcessInstance() != null
				&& taskInstance.getProcessInstance().getContextInstance() != null){
			taskInstance.getProcessInstance().getContextInstance().deleteVariable(name);
		}
		else{
			logger.error("Não foi possível remover a variável [{0}].", name);
		}
	}

	public static ProcessInstanceUtil instance(){
		return (ProcessInstanceUtil) org.jboss.seam.Component.getInstance(ProcessInstanceUtil.NAME);
	}

	/**
	 * Adiciona a variável no contexto do processo.
	 * 
	 * @param name Nome da variável.
	 * @param value Valor da variável.
	 */
	public void setVariable(String name, Object value) {
		if (isNotNullContextInstance()) {
			getContextInstance().setVariable(name, value);
		} else {
			logger.error("Não foi possível criar a variável [{0}] com o valor [{1}].", name, value);
		}
	}

	/**
	 * Retorna o valor da variável do contexto do processo.
	 * 
	 * @param name Nome da variável.
	 * @return Variável do contexto do processo.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getVariable(String name) {
		T resultado = null;
		
		if (isNotNullContextInstance()) {
			resultado = (T) getContextInstance().getVariable(name);
		} else {
			logger.error("Não foi possível buscar a variável [{0}].", name);
		}
		
		return resultado;
	}

	/**
	 * Remove a variável passada por parâmetro.
	 * 
	 * @param nome Nome da variável.
	 */
	public void deleteVariable(String nome) {
		getContextInstance().deleteVariable(nome);
	}
	
	/**
	 * Retorna as variáveis da tarefa atual.
	 * 
	 * @return Mapa de String com Object
	 */
	public Map<String, Object> getVariables() {
		Map<String, Object> resultado = new HashMap<String, Object>();
		
		if (isNotNullContextInstance()) {
			resultado = getContextInstance().getVariables();
		} else {
			logger.error("Não foi possível buscar as variáveis da tarefa.");
		}
		
		return resultado;
	}
	
	/**
	 * Retorna true se o ContextInstance não for nulo.
	 * 
	 * @return Booleano
	 */
	private Boolean isNotNullContextInstance() {
		return (getContextInstance() != null);
	}

	/**
	 * Retorna o ContextInstance do ProcessInstance.
	 * 
	 * @return ContextInstance
	 */
	private ContextInstance getContextInstance() {
		return (processInstance != null ? processInstance.getContextInstance() : null);
	}
}
