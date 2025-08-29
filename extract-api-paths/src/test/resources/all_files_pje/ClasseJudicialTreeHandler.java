package br.com.infox.cliente.component.tree;

import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ClasseJudicial;

@Name("classeJudicialTree")
@BypassInterceptors
public class ClasseJudicialTreeHandler extends AbstractTreeHandlerCachedRoots<ClasseJudicial> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		return "select distinct n from ClasseJudicial n left join fetch n.classeJudicialList f " + "where n.classeJudicialPai is null " + "order by n.classeJudicial, f.classeJudicial";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from ClasseJudicial n where classeJudicialPai = :" + EntityNode.PARENT_NODE;
	}

	@Override
	protected List<ClasseJudicial> getChildren(ClasseJudicial entity){
		return entity.getClasseJudicialList();
	}
	
	@Override
	protected ClasseJudicial getEntityToIgnore() {
		return ComponentUtil.getInstance("classeJudicialHome");
	}

}