package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@Name(TarefaSegredoTreeHandler.NAME)
@BypassInterceptors
public class TarefaSegredoTreeHandler extends TarefasTreeHandler {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "tarefasSegredoTree";

	@Override
	public boolean isSegredo() {
		return true;
	}
}
