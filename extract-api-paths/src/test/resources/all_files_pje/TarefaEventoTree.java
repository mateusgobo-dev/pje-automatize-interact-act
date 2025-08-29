package br.com.infox.ibpm.bean;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jbpm.graph.def.Event;

import br.com.infox.ibpm.component.tree.AutomaticEventsTreeHandler;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.TarefaEvento;
import br.jus.pje.nucleo.enums.TarefaEventoEnum;

@Name(value = TarefaEventoTree.NAME)
@Scope(ScopeType.CONVERSATION)
public class TarefaEventoTree implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final LogProvider log = Logging.getLogProvider(TarefaEventoTree.class);

	public static final String NAME = "tarefaEventoTree";

	private Integer agrupamentos;
	private boolean canLeave;
	private TarefaEvento currentEvent;

	public static TarefaEventoTree instance() {
		return (TarefaEventoTree) Component.getInstance(TarefaEventoTree.NAME);
	}

	/**
	 * Verifica se existe alguem Agrupamento para ser registrado naquela tarefa
	 * e atribui ao currentEvent o tipo do evento corrente.
	 */
	public Integer getAgrupamentos() {
		if (agrupamentos == null) {
			currentEvent = getNextEvent();
			StringBuilder sb = new StringBuilder();
			sb.append("select a from Agrupamento a ")
					.append("inner join a.agrupamentoTarefaList at ")
					.append("inner join at.tarefaEvento et ")
					.append("where et = :tarefaEvento")
					.append("		ORDER BY et.idTarefaEvento DESC");

			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("tarefaEvento", currentEvent);
			Agrupamento a = (Agrupamento) EntityUtil.getSingleResult(q);
			this.agrupamentos = a.getIdAgrupamento();
		}
		return agrupamentos;
	}

	/**
	 * Retorna o próximo evento a que deve ser registrado, porém se refere ao
	 * evento TarefaEvento, que possuem os tipos do TarefaEventoEnum.
	 * 
	 * @return O próximo evento a ser registrado.
	 */
	private TarefaEvento getNextEvent() {
		StringBuilder sb = new StringBuilder();
		sb.append("select et from ProcessoTarefaEvento o ").append("inner join o.tarefaEvento et ")
				.append("inner join et.tarefa t ")
				.append("where ")
				.append("o.processo = :processo and ").append("t.tarefa = :tarefa and ")
				.append("t.fluxo.fluxo = :fluxo ").append("order by et.evento");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processo", JbpmUtil.getProcesso());
		q.setParameter("tarefa", TaskInstance.instance().getTask().getName());
		q.setParameter("fluxo", TaskInstance.instance().getProcessInstance().getProcessDefinition().getName());
		q.setMaxResults(1);
		return (TarefaEvento) EntityUtil.getSingleResult(q);
	}

	@Observer(AutomaticEventsTreeHandler.AFTER_REGISTER_EVENT)
	/**
	 * Observer para o método que registra os eventos, pois é necessário verificar
	 * se existe mais algum evento na mesma tarefa para remontar a tree e efetuar
	 * a regra de negócio necessária.
	 */
	public void afterRegisterEvent() {
		if (currentEvent != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("update ProcessoTarefaEvento set registrado = true where ").append(
					"tarefaEvento = :tarefaEvento and processo = :processo");
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("tarefaEvento", currentEvent);
			q.setParameter("processo", JbpmUtil.getProcesso());
			q.executeUpdate();
			currentEvent = null;
			canLeave = !hasNextEvent();
			if (!canLeave) {
				agrupamentos = null;
				AutomaticEventsTreeHandler.instance().getRoots(getAgrupamentos());
				AutomaticEventsTreeHandler.instance().setRegistred(false);
				if (currentEvent.getEvento().equals(TarefaEventoEnum.ST)) {
					canLeave = true;
					AutomaticEventsTreeHandler.instance().setRegistred(true);
				}
			}
		}
	}

	/**
	 * Verifica se ainda resta algum TarefaEvento a ser registrado.
	 * 
	 * @return True se existir.
	 */
	private boolean hasNextEvent() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from ProcessoTarefaEvento o ").append("inner join o.tarefaEvento et ")
				.append("inner join et.tarefa t ")
				.append("where o.processo.idProcesso = :idProcesso and ")
				.append("t.tarefa = :tarefa and ")
				.append("t.fluxo.fluxo = :fluxo ")
				.append("and o.registrado = false");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcesso", JbpmUtil.getProcesso().getIdProcesso());
		q.setParameter("tarefa", TaskInstance.instance().getTask().getName());
		q.setParameter("fluxo", TaskInstance.instance().getProcessInstance().getProcessDefinition().getName());
		return EntityUtil.getSingleResultCount(q) == 0 ? false : true;
	}

	/**
	 * Método invocado quando finalmente o processo sai da tarefa, apagando
	 * assim os registros de ProcessoTarefaEvento referente aquele processo,
	 * pois caso este mesmo retorne para esta tarefa, deve ser possível
	 * registrar todos os eventos novamente.
	 */
	@Observer(Event.EVENTTYPE_TASK_END)
	public void onLeaveTask() {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("delete from ProcessoTarefaEvento o ").append("where o.processo = :processo and ")
					.append("exists (select 1 from TarefaEvento et ").append("inner join et.tarefa t ")
					.append("where et = o.tarefaEvento and ").append("t.tarefa = :tarefa and ")
					.append("t.fluxo.fluxo = :fluxo)");
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("processo", JbpmUtil.getProcesso());
			q.setParameter("tarefa", TaskInstance.instance().getTask().getName());
			q.setParameter("fluxo", TaskInstance.instance().getProcessInstance().getProcessDefinition().getName());
			q.executeUpdate();
		} catch (Exception ex) {
			String action = "deletar da tabela processoTarefaEvento os eventos finalizados";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"onLeaveTask()", "TarefaEventoTreeHandler", "BPM"));
		}
	}

	public TarefaEvento getCurrentEvent() {
		return currentEvent;
	}

	public void setCurrentEvent(TarefaEvento currentEvent) {
		this.currentEvent = currentEvent;
	}

	public Boolean getCanLeave() {
		return canLeave;
	}

	public void setCanLeave(Boolean canLeave) {
		this.canLeave = canLeave;
	}

	public static EntityManager getEntityManager() {
		return EntityUtil.getEntityManager();
	}

}
