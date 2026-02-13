package br.com.infox.cliente.component.tree;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.component.tree.AutomaticEventsTreeHandler;

@Name(value = MinutaEventsTreeHandler.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class MinutaEventsTreeHandler extends AutomaticEventsTreeHandler {

	public static final String NAME = "minutaEventsTree";
	private static final long serialVersionUID = 1L;

	@Override
	public boolean getAllRootsSelected() {
		if (rootList == null) {
			return true;
		}
		if (getEventoBeanList() != null && getEventoBeanList().size() > 0) {
			return true;
		}
		return false;
	}

}