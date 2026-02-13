package br.com.infox.bpm.action;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jbpm.graph.def.Transition;

import br.com.infox.ibpm.jbpm.TaskInstanceHome;

/**
 * Classe usada para mudar os nomes de exibição das transições do fluxo. Usado
 * pois o JBPM se perde quando há transições e tarefas com nomes iguais mesmo em
 * fluxos diferentes.
 * 
 * @author rodrigo
 * 
 */
@Scope(ScopeType.EVENT)
@BypassInterceptors
@Name("transitionNameAction")
public class TransitionNameAction {


	public List<SelectItem> getTranstionsSelectItems() {
		return getTranstionsSelectItems(TaskInstanceHome.instance().getTransitions());
	}

	public List<SelectItem> getTranstionsSelectItems(List<Transition> transitions) {
		List<SelectItem> selectList = new ArrayList<SelectItem>();
		for (Transition t : transitions) {
			String name = t.getName();
			selectList.add(new SelectItem(name, name));
		}
		return selectList;
	}

}
