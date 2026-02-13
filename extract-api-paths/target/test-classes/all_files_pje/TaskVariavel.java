package br.jus.cnj.fluxo;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.context.def.VariableAccess;
import org.jbpm.taskmgmt.def.TaskController;

import br.com.itx.util.ComponentUtil;

public enum TaskVariavel {
	
	DEFINIR_REVISOR("Processo_Fluxo_definirRevisor", "definirRevisorAction"),
	DEFINIR_COMPOSICAO_JULGAMENTO("Processo_Fluxo_definirComposicaoJulgamento", "definirComposicaoJulgamentoAction");	
	
	private String variavel;
	private String actionName;
	
	private TaskVariavel(String variavel, String actionName) {
		this.variavel = variavel;
		this.actionName = actionName; 
	}

	public String getVariavel() {
		return variavel;
	}

	public String getActionName() {
		return actionName;
	}
	
	public <C> C getAction() {
		return ComponentUtil.getComponent(getActionName());
	}
	
	public static List<TaskVariavel> recuperarTaskVariaveis(TaskController taskController) {

		List<VariableAccess> list = taskController.getVariableAccesses();
		List<TaskVariavel> taskVariaveis = new ArrayList<TaskVariavel>();
		
		for (VariableAccess varAccess : list) {
			
			String[] tokens = varAccess.getMappedName().split(":");
			
			//String type = tokens[0];
			String name = tokens[1];
									
			TaskVariavel tvEncontrada = null;
			
			// Verifica se a variavel esta mapeada
			for (TaskVariavel taskVariavel : values()) {
				if (taskVariavel.getVariavel().equals(name)) {
					tvEncontrada = taskVariavel;
					break;
				}
			}
			
			if (tvEncontrada != null) {
				taskVariaveis.add(tvEncontrada);
			}
		}

		return taskVariaveis;
	}
}
