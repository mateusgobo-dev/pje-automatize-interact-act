package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.TipoPena;

@Name("tipoPenaTree")
@BypassInterceptors
public class TipoPenaTreeHandler extends AbstractTreeHandler<TipoPena> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() { // PEGA NÍVEL INICIAL DA HIERARQUIA ...
		return "select n from TipoPena n " + "where tipoPenaPai is null "
				+ "and n.ativo = true and n.generoPena = 'PL'" + "order by dsTipoPena ";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from TipoPena n where n.ativo = true and tipoPenaPai = :" + EntityNode.PARENT_NODE;
	}

	@Override
	protected TipoPena getEntityToIgnore() {
		return ComponentUtil.getInstance("tipoPenaHome");
	}
}
