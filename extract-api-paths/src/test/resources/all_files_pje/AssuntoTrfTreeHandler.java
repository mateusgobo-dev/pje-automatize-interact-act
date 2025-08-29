package br.com.infox.cliente.component.tree;

import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.AssuntoTrf;

@Name("assuntoTrfTree")
@BypassInterceptors
public class AssuntoTrfTreeHandler extends AbstractTreeHandlerCachedRoots<AssuntoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		// usando fetch eager para evitar as consultas N + 1 que ocorriam em todos os tree de assuntos (na criação do componente de tela)
		// TODO configurar cache de segundo nível para mitigar o N + 1 ao navegar na árvore
		return "select distinct n from AssuntoTrf n left join fetch n.assuntoTrfList f " + "where n.assuntoTrfSuperior is null " + "order by n.assuntoTrf, f.assuntoTrf";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from AssuntoTrf n where assuntoTrfSuperior = :" + EntityNode.PARENT_NODE;
	}
	
	@Override
	protected List<AssuntoTrf> getChildren(AssuntoTrf entity){
		return entity.getAssuntoTrfList();
	}

	@Override
	protected AssuntoTrf getEntityToIgnore() {
		return ComponentUtil.getInstance("assuntoTrfHome");
	}
	
	@Override
	public void setSelectedNode(EntityNode<AssuntoTrf> node) {
		if (getSelected() == null || getSelected().toString() == null) {
			setSelected(node.getEntity());
		}
		if (getSelectedNodesList().contains(node)) {
			getSelectedNodesList().remove(node);
			// this.selectAllParent(node, false);
			this.selectAllChildren(node, false);
		} else {
			getSelectedNodesList().add(node);
			this.selectAllParent(node, true);
			this.selectAllChildren(node, true);
		}

	}

	protected void selectAllParent(EntityNode<AssuntoTrf> selectedNode, boolean operation) {
		int count = 0;

		EntityNode<AssuntoTrf> node = selectedNode.getParent();
		EntityNode<AssuntoTrf> parent = null;
		while (node != null) {
			count++;
			if (node.getParent() != null) {
				node.setSelected(operation);
				parent = node;
				getSelectedNodesList().add(node);
				getSelectedNodesListParent().add(parent);
			}
			node = node.getParent();
		}
		for (int i = count; i <= 3; i--) {
			operation = false;
			if (parent != null) {
				parent.setSelected(operation);
			}
		}

	}

	private void selectAllChildren(EntityNode<AssuntoTrf> selectedNode, boolean operation) {
		for (EntityNode<AssuntoTrf> node : selectedNode.getNodes()) {
			selectAllChildren(node, operation);
			node.setSelected(operation);
			if (operation) {
				getSelectedNodesList().add(node);
			} else {
				getSelectedNodesList().remove(node);
			}
		}
	}

}