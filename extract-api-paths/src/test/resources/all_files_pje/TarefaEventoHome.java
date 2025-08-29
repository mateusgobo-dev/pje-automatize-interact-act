package br.com.infox.ibpm.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.GraphElement;
import org.jbpm.graph.def.Node.NodeType;

import br.com.infox.ibpm.jbpm.ProcessBuilder;
import br.com.infox.ibpm.jbpm.handler.TaskHandler;
import br.com.infox.ibpm.jbpm.handler.TransitionHandler;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.TarefaEvento;
import br.jus.pje.nucleo.entidades.TarefaEventoAgrupamento;
import br.jus.pje.nucleo.enums.TarefaEventoEnum;

@Name(TarefaEventoHome.NAME)
@BypassInterceptors
public class TarefaEventoHome extends AbstractTarefaEventoHome<TarefaEvento> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "tarefaEventoHome";
	private List<Tarefa> tarefaOrigemList;
	private List<Agrupamento> agrupamentos;
	private List<Agrupamento> registrados;
	private Boolean enableEvents = false;
	private String condicaoLancamentoTemporario = "#{false}";
	private static final String PREFIX = "#{lancadorMovimentosService.setCondicaoLancamentoMovimentosTemporarioNoFluxo('";

	public static TarefaEventoHome instance() {
		return ComponentUtil.getComponent(TarefaEventoHome.NAME);
	}

	@SuppressWarnings("unchecked")
	public TarefaEventoEnum[] getTarefaEventoItems() {
		if (tarefaEventoItems == null || tarefaEventoItems.length == 0) {
			if (ProcessBuilder.instance().getCurrentNode().getNodeType() == NodeType.Task) {
				if (isManaged()) {
					return TarefaEventoEnum.values();
				}
				String s = "select o from TarefaEvento o where o.tarefa = :tarefa";
				Query q = getEntityManager().createQuery(s).setParameter("tarefa", getTarefaAtual());
				List<TarefaEventoEnum> listEnum = new ArrayList<TarefaEventoEnum>();
				TarefaEventoEnum[] tee = TarefaEventoEnum.values();
				for (int i = 0; i < tee.length; i++) {
					listEnum.add(tee[i]);
				}
				for (TarefaEvento te : (List<TarefaEvento>) q.getResultList()) {
					if (te.getEvento() == TarefaEventoEnum.RT) {
						listEnum.remove(TarefaEventoEnum.RT);
					} else if (te.getEvento() == TarefaEventoEnum.ST) {
						listEnum.remove(te.getEvento());
					}
				}
				tarefaEventoItems = new TarefaEventoEnum[listEnum.size()];
				int count = 0;
				for (TarefaEventoEnum tarefaEventoEnum : listEnum) {
					tarefaEventoItems[count] = tarefaEventoEnum;
					count++;
				}
			} else {
				tarefaEventoItems = new TarefaEventoEnum[1];
				tarefaEventoItems[0] = TarefaEventoEnum.ET;
			}
		}
		return tarefaEventoItems;
	}

	@SuppressWarnings("unchecked")
	public void carregarAgrupamentos() {
		if (instance.getEvento().equals(TarefaEventoEnum.ET)) {
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct a from Agrupamento a inner join ").append("a.eventoAgrupamentoList eal where ")
					.append("eal.evento.eventoList.size = 0 and ").append("eal.multiplo = false");
			agrupamentos = getEntityManager().createQuery(sb.toString()).getResultList();
		} else {
			agrupamentos = getEntityManager().createQuery("select o from Agrupamento o").getResultList();
		}
		if (this.registrados == null) {
			this.registrados = new ArrayList<Agrupamento>();
		} else {
			this.registrados.clear();
		}
		for (TarefaEventoAgrupamento aet : instance.getTarefaEventoAgrupamentoList()) {
			registrados.add(aet.getAgrupamento());
		}
		
		StringBuilder newExpression = new StringBuilder(PREFIX);
		Action action = getExistingActionTaskStart(newExpression.toString());
		if (action != null){
			// já há uma expressão de lançamento temporário, carregá-la
			String antigaExpression = action.getActionExpression().replace(PREFIX, "");
			this.condicaoLancamentoTemporario = antigaExpression.substring(0, antigaExpression.length() - 3);
		}
		else{
			// por padrão, não lançar temporariamente no lançador de movimentos do fluxo
			this.condicaoLancamentoTemporario = "#{false}";
		}
		
	}

	@Observer(ProcessBuilder.SET_CURRENT_NODE_EVENT)
	public void onSetCurrentNode() {
		setTab("search");
		newInstance();
		tarefaAtual = null;
		tarefaOrigemList = null;
		canRegister();
	}

	@Override
	public String update() {
		instance.getTarefaEventoAgrupamentoList().clear();
		getEntityManager()
				.createQuery("delete from TarefaEventoAgrupamento " + "tea where tea.tarefaEvento = :tarefaEvento")
				.setParameter("tarefaEvento", instance).executeUpdate();
		for (Agrupamento ae : registrados) {
			TarefaEventoAgrupamento tea = new TarefaEventoAgrupamento();
			tea.setAgrupamento(ae);
			tea.setTarefaEvento(instance);
			instance.getTarefaEventoAgrupamentoList().add(tea);
			getEntityManager().persist(tea);
		}
		
		// Salvar condição de lancamento temporário de movimentos
		StringBuilder newExpression = new StringBuilder(PREFIX);
		Action action = getActionTaskStart(newExpression.toString());
		action.setName("Condicao do lancamento temporario de movimentos");
		newExpression.append(condicaoLancamentoTemporario);
		newExpression.append("')}");
		action.setActionExpression(newExpression.toString());
		
		EntityUtil.flush();
		FacesMessages.instance().add("Registros associados com sucesso!");
		return "updated";
	}

	public void setUpdate(TarefaEvento et) {
		setTab("form");
		instance = et;
	}

	public List<Agrupamento> getAgrupamentos() {
		if (agrupamentos == null) {
			carregarAgrupamentos();
		}
		return agrupamentos;
	}

	public void setAgrupamentos(List<Agrupamento> agrupamentos) {
		this.agrupamentos = agrupamentos;
	}

	public void setRegistrados(List<String> registrados) {
		if (this.registrados == null) {
			this.registrados = new ArrayList<Agrupamento>();
		} else {
			this.registrados.clear();
		}
		for (String s : registrados) {
			for (Agrupamento ae : agrupamentos) {
				if (ae.getAgrupamento().equals(s)) {
					this.registrados.add(ae);
					break;
				}
			}
		}
	}

	public List<Agrupamento> getRegistrados() {
		return registrados;
	}

	private void canRegister() {
		TaskHandler task = ProcessBuilder.instance().getCurrentTask();
		if (task == null) {
			enableEvents = false;
			return;
		}
		StringBuilder count = new StringBuilder();
		count.append("select count(t) from Tarefa t ")
				.append("where t.fluxo.fluxo = :fluxo and t.tarefa = :nomeTarefa");
		Query q = getEntityManager().createQuery(count.toString());
		q.setParameter("nomeTarefa", task.getTask().getName());
		q.setParameter("fluxo", task.getTask().getProcessDefinition().getName());
		if ((Long) q.getSingleResult() == 0) {
			enableEvents = false;
			return;
		}
		enableEvents = true;
	}

	public Boolean getEnableEvents() {
		return enableEvents;
	}

	public void setEnableEvents(Boolean enableEvents) {
		this.enableEvents = enableEvents;
	}

	public List<Tarefa> getTarefaOrigemList() {
		if (tarefaOrigemList == null) {
			String fluxo = ProcessBuilder.instance().getInstance().getName();
			tarefaOrigemList = new ArrayList<Tarefa>();
			String query = "select t from Tarefa t where t.tarefa = :tarefaOrigem " + "and t.fluxo.fluxo = :fluxo";
			Query q = getEntityManager().createQuery(query);
			for (TransitionHandler th : ProcessBuilder.instance().getArrivingTransitions()) {
				q.setParameter("tarefaOrigem", th.getTransition().getFrom().getName());
				q.setParameter("fluxo", fluxo);
				Tarefa t = EntityUtil.getSingleResult(q);
				if (t != null) {
					tarefaOrigemList.add(t);
				}
			}
		}
		return tarefaOrigemList;
	}

	public void setTarefaOrigemList(List<Tarefa> tarefaOrigemList) {
		this.tarefaOrigemList = tarefaOrigemList;
	}
	
	private Action getActionTaskStart(String newExpression){
		GraphElement parent = ProcessBuilder.instance().getCurrentTask().getTask().getParent();
		Event e = parent.getEvent(Event.EVENTTYPE_TASK_START);
		if (e == null){
			e = new Event(parent, Event.EVENTTYPE_TASK_START);
			parent.addEvent(e);
		}
		if (e.getActions() != null){
			for (Action a : e.getActions()){
				String exp = a.getActionExpression();
				if (exp != null && exp.startsWith(newExpression)){
					return a;
				}
			}
		}
		Action action = new Action();
		e.addAction(action);
		return action;
	}

	private Action getExistingActionTaskStart(String newExpression){
		GraphElement parent = ProcessBuilder.instance().getCurrentTask().getTask().getParent();
		Event e = parent.getEvent(Event.EVENTTYPE_TASK_START);
		if (e == null){
			e = new Event(parent, Event.EVENTTYPE_TASK_START);
			parent.addEvent(e);
		}
		if (e.getActions() != null){
			for (Action a : e.getActions()){
				String exp = a.getActionExpression();
				if (exp != null && exp.startsWith(newExpression)){
					return a;
				}
			}
		}
		return null;
	}
	
	public String getCondicaoLancamentoTemporario(){
		return condicaoLancamentoTemporario;
	}

	
	public void setCondicaoLancamentoTemporario(String condicaoLancamentoTemporario){
		this.condicaoLancamentoTemporario = condicaoLancamentoTemporario;
	}
}