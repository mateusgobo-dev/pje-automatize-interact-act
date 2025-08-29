package br.com.infox.cliente.component.tree;

import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.TipoPessoa;

@Name("entidadeTree")
@BypassInterceptors
public class EntidadeTreeHandler extends AbstractTreeHandlerCachedRoots<TipoPessoa> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		return "select distinct n from TipoPessoa n left join fetch n.tipoPessoaList f where n.tipoPessoaSuperior is null"
				+ " order by n.tipoPessoa, f.tipoPessoa";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from TipoPessoa n where tipoPessoaSuperior = :" + EntityNode.PARENT_NODE;
	}

	@Override
	protected TipoPessoa getEntityToIgnore() {
		return ComponentUtil.getInstance("tipoPessoaHome");
	}

	@Override
	protected List<TipoPessoa> getChildren(TipoPessoa entity){
		return entity.getTipoPessoaList();
	}
	
}