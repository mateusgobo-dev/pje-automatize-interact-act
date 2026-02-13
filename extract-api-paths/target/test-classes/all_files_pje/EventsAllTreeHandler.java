package br.com.infox.ibpm.component.tree;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.EntityNode;
import br.jus.pje.nucleo.entidades.Evento;

@Name(EventsAllTreeHandler.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EventsAllTreeHandler extends AutomaticEventsTreeHandler {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "eventsAllTreeHandler";

	public static EventsAllTreeHandler instance() {
		return (EventsAllTreeHandler) org.jboss.seam.Component.getInstance(EventsAllTreeHandler.NAME);
	}

	@Override
	protected String getQueryRoots() {
		StringBuilder sb = new StringBuilder();
		sb.append("select ea from EventoAgrupamento ea ");
		sb.append(" WHERE ea.evento.ativo = true ");
		sb.append("order by ea.evento");
		return sb.toString();
	}

	@Override
	public List<EntityNode<Evento>> getRoots() {
		if (rootList == null) {
			Query queryRoots = getEntityManager().createQuery(getQueryRoots());
			EntityNode<Evento> entityNode = createNode();
			entityNode.setIgnore(getEntityToIgnore());
			rootList = entityNode.getRoots(queryRoots);
		}
		return rootList;
	}
}
