package br.com.infox.ibpm.home;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jbpm.graph.def.Transition;

import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.ProcessBuilder;
import br.com.infox.ibpm.jbpm.actions.VerificaEventoAction;
import br.com.infox.ibpm.jbpm.handler.ActionTemplateHandler;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.TarefaTransicaoEvento;
import br.jus.pje.nucleo.entidades.TarefaTransicaoEventoAgrupamento;

@Name(TarefaTransicaoEventoHome.NAME)
@BypassInterceptors
public class TarefaTransicaoEventoHome extends AbstractTarefaTransicaoEventoHome<TarefaTransicaoEvento> {

	public static final String NAME = "tarefaTransicaoEventoHome";
	private static final long serialVersionUID = 1L;
	private Boolean enableTransition = false;
	private List<Agrupamento> agrupamentos;
	private String createdExpression;
	private List<Agrupamento> registrados = new ArrayList<Agrupamento>();

	public static TarefaTransicaoEventoHome instance() {
		return ComponentUtil.getComponent(TarefaTransicaoEventoHome.NAME);
	}

	@SuppressWarnings("unchecked")
	public void definirCondicao(Transition transition) {
		instance = null;
		registrados = new ArrayList<Agrupamento>();
		ProcessBuilder.instance().setCurrentTransition(transition);
		String from = transition.getFrom().getName();
		String to = transition.getTo().getName();
		StringBuilder sb = new StringBuilder();
		sb.append("select o from TarefaTransicaoEvento o where o.tarefaOrigem.tarefa = :from ").append(
				"and o.tarefaDestino.tarefa = :to");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("from", from);
		q.setParameter("to", to);
		List<TarefaTransicaoEvento> veList = q.getResultList();
		if (veList != null && veList.size() != 0) {
			instance = veList.get(0);
			for (TarefaTransicaoEventoAgrupamento ave : instance.getTarefaTransicaoEventoAgrupamentoList()) {
				registrados.add(ave.getAgrupamento());
			}
		}
		agrupamentos = getEntityManager().createQuery("select o from Agrupamento o").getResultList();
	}

	public List<Agrupamento> getAgrupamentos() {
		if (agrupamentos == null) {
			agrupamentos = new ArrayList<Agrupamento>();
			definirCondicao(ProcessBuilder.instance().getCurrentTransition());
		}
		return agrupamentos;
	}

	public String gravar() {
		String ret = null;
		if (instance == null && registrados.size() > 0) {
			String fluxo = ProcessBuilder.instance().getCurrentTransition().getFrom().getProcessDefinition().getName();
			String tarefaFrom = ProcessBuilder.instance().getCurrentTransition().getFrom().getName();
			String tarefaTo = ProcessBuilder.instance().getCurrentTransition().getTo().getName();
			newInstance();
			Tarefa tarefaOrigem = JbpmUtil.getTarefa(tarefaFrom, fluxo);
			Tarefa tarefaDestino = JbpmUtil.getTarefa(tarefaTo, fluxo);
			instance.setTarefaOrigem(tarefaOrigem);
			instance.setTarefaDestino(tarefaDestino);
			adicionarAgrupamentos();
			ret = persist();
			if ("persisted".equals(ret)) {
				createExpression(tarefaOrigem, tarefaDestino);
				ProcessBuilder.instance().update();
			}
		} else if (instance != null && registrados.size() == 0) {
			createdExpression = null;
			ret = remove();
			if ("removed".equals(ret)) {
				ProcessBuilder.instance().update();
			}
			instance = null;
		} else if (instance != null && registrados.size() > 0) {
			atribuirEventos();
			ret = update();
		}
		return ret;
	}

	public void createExpression(Tarefa from, Tarefa to) {
		StringBuilder sb = new StringBuilder("#{");
		sb.append(VerificaEventoAction.VERIFICA_EVENTO_EXPRESSION).append("(").append(from.getIdTarefa()).append(", ")
				.append(to.getIdTarefa()).append(")}");
		createdExpression = sb.toString();
	}

	public String getCreatedExpression() {
		return createdExpression;
	}

	public void setAgrupamentos(List<Agrupamento> agrupamentos) {
		this.agrupamentos = agrupamentos;
	}

	public List<Agrupamento> getRegistrados() {
		return registrados;
	}

	private void atribuirEventos() {
		for (Iterator<?> iterator = instance.getTarefaTransicaoEventoAgrupamentoList().iterator(); iterator.hasNext();) {
			TarefaTransicaoEventoAgrupamento ave = (TarefaTransicaoEventoAgrupamento) iterator.next();
			if (registrados.contains(ave.getAgrupamento())) {
				registrados.remove(ave.getAgrupamento());
			} else {
				iterator.remove();
			}
		}
		adicionarAgrupamentos();
	}

	private void adicionarAgrupamentos() {
		for (Agrupamento ae : registrados) {
			TarefaTransicaoEventoAgrupamento ave = new TarefaTransicaoEventoAgrupamento();
			ave.setAgrupamento(ae);
			ave.setTarefaTransicaoEvento(instance);
			instance.getTarefaTransicaoEventoAgrupamentoList().add(ave);
		}
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

	@Observer(ActionTemplateHandler.SET_CURRENT_TEMPLATE_EVENT)
	public void canRegister() {
		Transition t = ProcessBuilder.instance().getCurrentTransition();
		if (t == null || t.getFrom() == null || t.getTo() == null) {
			return;
		}
		StringBuilder count = new StringBuilder();
		count.append("select count(t) from Tarefa t ")
				.append("where t.fluxo.fluxo = :fluxo and t.tarefa = :nomeTarefa");
		Query q = getEntityManager().createQuery(count.toString());
		q.setParameter("nomeTarefa", t.getFrom().getName());
		q.setParameter("fluxo", t.getFrom().getProcessDefinition().getName());
		if ((Long) q.getSingleResult() == 0) {
			enableTransition = false;
			return;
		}
		q = getEntityManager().createQuery(count.toString());
		q.setParameter("nomeTarefa", t.getTo().getName());
		q.setParameter("fluxo", t.getTo().getProcessDefinition().getName());
		if ((Long) q.getSingleResult() == 0) {
			enableTransition = false;
			return;
		}
		enableTransition = true;
	}

	public Boolean getEnableTransition() {
		return enableTransition;
	}

	public void setEnableTransition(Boolean enableTransition) {
		this.enableTransition = enableTransition;
	}

}
