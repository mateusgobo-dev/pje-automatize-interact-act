package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.RamoAtividade;

@Name("ramoAtividadeTree")
@BypassInterceptors
public class RamoAtividadeTreeHandler extends AbstractTreeHandler<RamoAtividade> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		return "select n from RamoAtividade n " + "where ramoAtividadePai = null " + "order by ramoAtividade";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from RamoAtividade n where ramoAtividadePai = :" + EntityNode.PARENT_NODE;
	}

	@Override
	protected RamoAtividade getEntityToIgnore() {
		return ComponentUtil.getInstance("ramoAtividadeHome");
	}
}