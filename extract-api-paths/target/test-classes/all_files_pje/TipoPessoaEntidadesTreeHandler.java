package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.TipoPessoa;

@Name("tipoPessoaEntidadesTree")
@BypassInterceptors
public class TipoPessoaEntidadesTreeHandler extends AbstractTreeHandler<TipoPessoa> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		return "select n from TipoPessoa n " + "where n.tipoPessoaSuperior.tipoPessoa = 'Entidades' "
				+ "order by tipoPessoa";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from TipoPessoa n where tipoPessoaSuperior = :" + EntityNode.PARENT_NODE;
	}

	@Override
	protected TipoPessoa getEntityToIgnore() {
		return ComponentUtil.getInstance("tipoPessoaHome");
	}
}