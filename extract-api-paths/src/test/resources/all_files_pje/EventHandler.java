/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.jbpm.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jbpm.graph.action.Script;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.GraphElement;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Node.NodeType;

import javassist.expr.Instanceof;

public class EventHandler {

	private Event event;
	private String expression;
	private Action currentAction;
	private List<Action> actionList;
	private Boolean reExecutavel = Boolean.FALSE;

	private List<String> orderedReexecutableEvents = new ArrayList<String>(
			Arrays.asList(Event.EVENTTYPE_NODE_ENTER, Event.EVENTTYPE_TASK_CREATE, Event.EVENTTYPE_TASK_START));

	
	public EventHandler(Event event) {
		this.event = event;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public String getExpression() {
		if (currentAction != null) {
			return currentAction.getActionExpression();
		}
		if (expression == null) {
			if (event.getActions() != null && event.getActions().size() > 0) {
				Action action = event.getActions().get(0);
				if (action instanceof Script) {
					Script s = (Script) action;
					expression = s.getExpression();
				} else {
					expression = action.getActionExpression();
				}
			}
		}
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
		if (event.getActions() == null) {
			event.addAction(new Script());
		}
		if (event.getActions().size() > 0) {
			Action action = event.getActions().get(0);
			if (action instanceof Script) {
				Script s = (Script) action;
				s.setExpression(expression);
			}
		}
	}

	public static List<EventHandler> createList(GraphElement instance) {
		if (instance == null) {
			return null;
		}
		List<EventHandler> ret = new ArrayList<EventHandler>();
		Map<String, Event> events = instance.getEvents();
		if (events == null) {
			return ret;
		}
		for (Event event : events.values()) {
			EventHandler eh = new EventHandler(event);
			ret.add(eh);
		}
		return ret;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof EventHandler) {
			EventHandler ev = (EventHandler) obj;
			if (ev.getEvent() == null || ev.getEvent().getEventType() == null) {
				return false;
			}
			return ev.getEvent().getEventType().equals(this.getEvent().getEventType());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		if(this.getEvent() == null || this.getEvent().getEventType() == null){
			return -1;
		}
		return this.getEvent().getEventType().hashCode();
	}

	public void addAction() {
		event.addAction(new Action());
		actionList = null;
		setCurrentAction(null);
	}

	public void removeAction(Action a) {
		event.removeAction(a);
		
		if (a.equals(getCurrentAction())) {
			setCurrentAction(null);
		}
	}

	public List<Action> getActions() {
		if (actionList == null) {
			actionList = event.getActions();
			setCurrentAction(null);
		}
		return actionList;
	}

	public Action getCurrentAction() {
		return currentAction;
	}

	public void setCurrentAction(Action currentAction) {
		this.currentAction = currentAction;
	}

	public String getCurrentActionType() {
		return getIcon(currentAction);
	}

	public String getIcon(Action action) {
		if (action == null) {
			return null;
		}
		String type = "action";
		if (action instanceof Script) {
			type = "script";
		}
		return type;
	}

	public void setTemplate() {
		ActionTemplateHandler.instance().setCurrentActionTemplate(getExpression());
	}

	public void setReExecutavel(Boolean reExecutavel) {
		this.reExecutavel = reExecutavel;
	}

	public Boolean getReExecutavel() {
		if (currentAction == null || currentAction.getName() == null) {
			return false;
		}
		return currentAction.getName().equals("upd");
	}

	public void atualizaNome() {
		if (reExecutavel && currentAction != null) {
			currentAction.setName("upd");
		}
		if (!reExecutavel && currentAction != null) {
			currentAction.setName(null);
		}
	}

	public boolean podeSerMarcadoReexecutavel() {
		Node node = null;
		if(this.getEvent() != null && this.getEvent().getGraphElement() != null && this.getEvent().getGraphElement() instanceof Node) {
			node = (Node) this.getEvent().getGraphElement();
		}
		
		if((node == null || node.getNodeType() == NodeType.Task) && this.getEvent() != null && this.getEvent().getEventType() != null){
			return this.orderedReexecutableEvents.contains(this.getEvent().getEventType());
		}
		return false;
	}
}