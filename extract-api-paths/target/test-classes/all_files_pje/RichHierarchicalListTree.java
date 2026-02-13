package br.com.infox.editor.tree;

import javax.swing.tree.DefaultMutableTreeNode;

import org.richfaces.component.UITree;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;

import br.jus.pje.nucleo.enums.editor.Hierarchical;

public class RichHierarchicalListTree<T extends Hierarchical> extends HierarchicalListTree<T> {
	
	private static final long serialVersionUID = 1L;
	
	public RichHierarchicalListTree() {
	}
	
	public RichHierarchicalListTree(String titulo) {
		super(titulo);
	}
	
	/**
	 * Listener para setar o nó selecionado quando houver uma ação do usuário
	 * @param ev evento lançado quando um nó é selecionado
	 */
	public void selectListener(NodeSelectedEvent ev) {
		HtmlTree tree = (HtmlTree) ev.getSource();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getData();
		selectedNode = node != null && !node.isRoot() ? node : null; 
	}
	
	/**
	 * Verifica se o nó atual está selecionado 
	 * @param tree componente tree do richfaces
	 * @return <code>true</code>, se o nó atual estiver selecionado, senão <code>false</code> 
	 */
	public Boolean adviseNodeSelected(UITree tree) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getData();
		return selectedNode != null && selectedNode.equals(node);
	}
	
	/**
	 * Informa que o nó atual deve ser expandido, retornando <code>true</code> para qualquer nó da tree
	 * @param tree componente tree do richfaces
	 * @return true
	 */
	public Boolean adviseNodeOpened(UITree tree) {
		return true;
	}
}
