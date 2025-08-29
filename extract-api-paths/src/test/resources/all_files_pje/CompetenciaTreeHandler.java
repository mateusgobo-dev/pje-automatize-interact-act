package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Competencia;

@Name("competenciaTree")
@BypassInterceptors
public class CompetenciaTreeHandler extends AbstractTreeHandler<Competencia> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		return "select n from Competencia n " + "where competenciaPai is null and n.ativo = true "
				+ "order by competencia";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from Competencia n where competenciaPai = :" + EntityNode.PARENT_NODE;
	}

	@Override
	protected Competencia getEntityToIgnore() {
		return ComponentUtil.getInstance("competenciaHome");
	}
}
