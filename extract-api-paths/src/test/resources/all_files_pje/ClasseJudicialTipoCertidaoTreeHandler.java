package br.com.infox.cliente.component.tree;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.ClasseJudicialTipoCertidaoHome;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ClasseJudicial;

@Name("classeJudicialTipoCertidaoTree")
@BypassInterceptors
public class ClasseJudicialTipoCertidaoTreeHandler extends AbstractTreeHandler<ClasseJudicial> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		ClasseJudicialTipoCertidaoHome classeJudicialTipoCertidaoHome = (ClasseJudicialTipoCertidaoHome) Component
				.getInstance("classeJudicialTipoCertidaoHome", false);
		ClasseJudicial classeJudicial = classeJudicialTipoCertidaoHome.getClasseJudicialTree();

		return "select n from ClasseJudicial n " + "where id = " + classeJudicial.getIdClasseJudicial()
				+ " order by classeJudicial";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from ClasseJudicial n where classeJudicialPai = :" + EntityNode.PARENT_NODE;
	}

	@Override
	protected ClasseJudicial getEntityToIgnore() {
		return ComponentUtil.getInstance("classeJudicialHome");
	}
}