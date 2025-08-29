package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Profissao;

@Name("profissaoTree")
@BypassInterceptors
public class ProfissaoTreeHandler extends AbstractTreeHandler<Profissao> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		return "select n from Profissao n " + "where profissaoSuperior is null " + "order by profissao";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from Profissao n where profissaoSuperior = :" + EntityNode.PARENT_NODE;
	}

	@Override
	protected Profissao getEntityToIgnore() {
		return ComponentUtil.getInstance("profissaoHome");
	}
}