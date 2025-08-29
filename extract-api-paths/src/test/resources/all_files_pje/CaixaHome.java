package br.com.infox.ibpm.home;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.ibpm.component.tree.TarefasTreeHandler;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.actions.JbpmEventsHandler;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Caixa;
import br.jus.pje.nucleo.entidades.Tarefa;

@Name(CaixaHome.NAME)
@BypassInterceptors
public class CaixaHome extends AbstractCaixaHome<Caixa> {

	public static final String NAME = "caixaHome";
	public static final String ADD_CAIXA_EVENT = "addCaixaEvent";
	private static final long serialVersionUID = 1L;
	private String nomeNovaCaixa;
	private Integer idTarefaAnterior;
	private Integer idTarefaSearch;

	@Override
	protected boolean beforePersistOrUpdate() {
		if (verificaRegistroDuplicado()) {
			if (idTarefaAnterior != null) {
				instance.setTarefaAnterior(getEntityManager().find(Tarefa.class, idTarefaAnterior));
			} else {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado");
				// newInstance();
				getEntityManager().refresh(getInstance());
				return false;
			}
		}
		if (idTarefaAnterior != null) {
			instance.setTarefaAnterior(getEntityManager().find(Tarefa.class, idTarefaAnterior));
		} else {
			instance.setTarefaAnterior(null);
		}
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		if (idTarefaAnterior != null) {
			instance.setTarefaAnterior(getEntityManager().find(Tarefa.class, idTarefaAnterior));
		}
		return super.persist();
	}

	public boolean verificaRegistroDuplicado() {
		//String s = "select count(o) from CaixaFiltro o where o.orgaoJulgador = (select cf.orgaoJulgador from CaixaFiltro cf where cf.idCaixa = :idCaixa) and lower(to_ascii(o.nomeCaixa)) = lower(to_ascii(:nomeCaixa)) and o.tarefa = :tarefa";
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from CaixaFiltro o ");
		sb.append("where o.orgaoJulgador = (select cf.orgaoJulgador from CaixaFiltro cf where cf.idCaixa = :idCaixa) ");
		sb.append("and lower(to_ascii(o.nomeCaixa)) = lower(to_ascii(:nomeCaixa)) ");		
		sb.append("and o.tarefa = :tarefa ");
		if (getInstance().getIdCaixa() != 0) {
			sb.append("and o.idCaixa <> :idCaixa ");
		}
		
		javax.persistence.Query q = getEntityManager().createQuery(sb.toString());

		q.setParameter("idCaixa", getInstance().getIdCaixa());
		q.setParameter("nomeCaixa", getInstance().getNomeCaixa());
		q.setParameter("tarefa", getInstance().getTarefa());

		Long c1 = (Long) EntityUtil.getSingleResult(q);

		if (c1 > 0) {
			return true;
		}

		return false;
	}

	public List<SelectItem> getPreviousTasks() {
		return getPreviousTasks(instance.getTarefa());
	}

	public List<SelectItem> getPreviousTasks(Integer idTarefa) {
		return getPreviousTasks(EntityUtil.find(Tarefa.class, idTarefa));
	}

	@SuppressWarnings("unchecked")
	public List<SelectItem> getPreviousTasks(Tarefa tarefa) {
		List<SelectItem> previousTasksItems = new ArrayList<SelectItem>();
		Session session = JbpmUtil.getJbpmSession();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT taskFrom.id_, taskFrom.name_ ").append("FROM jbpm_transition transFrom ")
				.append("join jbpm_node nodeFrom on transFrom.from_ = nodeFrom.id_ ")
				.append("join jbpm_node nodeTo on transFrom.to_ = nodeTo.id_ ")
				.append("join jbpm_task taskFrom on taskFrom.tasknode_ = nodeFrom.id_ ")
				.append("join jbpm_task taskTo on taskTo.tasknode_ = nodeTo.id_ ")
				.append("where taskTo.id_ = :idTask order by 2");
		Query query = session.createSQLQuery(sql.toString());
		if (tarefa == null) {
			return previousTasksItems;
		}
		Tarefa t = getEntityManager().find(Tarefa.class, tarefa.getIdTarefa());
		query.setParameter("idTask", t.getLastIdJbpmTask());
		previousTasksItems.add(new SelectItem(null, "Selecione a Tarefa Anterior"));
		String arg = "select t from Tarefa t where t.tarefa = :tarefa and t.fluxo = :fluxo";
		javax.persistence.Query q = getEntityManager().createQuery(arg);
		for (Object[] obj : (List<Object[]>) query.list()) {
			q.setParameter("tarefa", obj[1].toString());
			q.setParameter("fluxo", tarefa.getFluxo());
			Tarefa tarefaAnterior = EntityUtil.getSingleResult(q);
			if (tarefaAnterior != null) {
				previousTasksItems.add(new SelectItem(tarefaAnterior.getIdTarefa(), tarefaAnterior.getTarefa()));
			}
		}
		return previousTasksItems;
	}

	public void setNomeNovaCaixa(String nomeNovaCaixa) {
		this.nomeNovaCaixa = nomeNovaCaixa;
	}

	public String getNomeNovaCaixa() {
		return nomeNovaCaixa;
	}

	@Override
	public void newInstance() {
		nomeNovaCaixa = null;
		super.newInstance();
	}

	public void addCaixa(int idTarefa) {
		instance = createInstance();
		instance.setNomeCaixa(nomeNovaCaixa);
		instance.setTarefa(getEntityManager().find(Tarefa.class, idTarefa));
		String persist = super.persist();
		if (persist != null && !"".equals(persist)) {
			EntityUtil.flush();
		}
		TarefasTreeHandler tree = getComponent("tarefasTree");
		tree.clearTree();
		getEntityManager().clear();
		newInstance();
	}

	public static CaixaHome instance() {
		return (CaixaHome) Contexts.getConversationContext().get(NAME);
	}
	
	
	/*
	 * [PJEII-2170] PJE-JT: Sérgio Ricardo : PJE-1.4.4 
	 * Replicação do método instance() - para efeito de compatibilidade - mas utilizando o retorno do método ComponentUtil.getComponent no lugar
	 * de Contexts.getConversationContext
	 */
	public static CaixaHome instanceCaixa() {
		return ComponentUtil.getComponent("caixaHome");
	}

	public Integer getIdTarefaSearch() {
		return idTarefaSearch;
	}

	public void setIdTarefaSearch(Integer idTarefaSearch) {
		this.idTarefaSearch = idTarefaSearch;
	}

	public Integer getIdTarefaAnterior() {
		if (isManaged()) {
			if (instance.getTarefaAnterior() != null) {
				idTarefaAnterior = instance.getTarefaAnterior().getIdTarefa();
			}
		}
		return idTarefaAnterior;
	}

	public void setIdTarefaAnterior(Integer idTarefaAnterior) {
		this.idTarefaAnterior = idTarefaAnterior;
	}

	@Override
	public void removeCaixa(int idCaixa) {
		super.removeCaixa(idCaixa);
		TarefasTreeHandler tree = ComponentUtil.getComponent("tarefasTree");
		/*
		 * removendo qq nó selecionado na tree para permitir que os observers 
		 * de Eventos#SELECIONADA_TAREFA possam ter efeito sobre o método onSelected
		 */
		tree.clearTree();
	}
}