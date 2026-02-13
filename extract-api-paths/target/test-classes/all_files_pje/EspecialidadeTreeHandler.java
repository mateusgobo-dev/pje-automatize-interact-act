package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Especialidade;

@Name("especialidadeTree")
@BypassInterceptors
public class EspecialidadeTreeHandler extends AbstractTreeHandler<Especialidade> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		return "select n from Especialidade n " + "where especialidadePai is null " + "order by especialidade";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from Especialidade n " + "where especialidadePai = :" + EntityNode.PARENT_NODE + " order by especialidade";
	}

	@Override
	protected Especialidade getEntityToIgnore() {
		return ComponentUtil.getInstance("especialidadeHome");
	}
}