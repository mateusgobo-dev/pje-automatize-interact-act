/**
 * 
 */
package br.jus.cnj.pje.view;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeExpandedEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

import br.jus.pje.search.Search;

/**
 * Componente de tratamento de entidade hierárquica.
 * 
 * @author cristof
 * 
 */
public class EntityTreeModel<E> {

	public interface DataRetriever<E> {
		public Object getId(E entity);
		public List<E> listChildren(E parent, Search search);
		public List<E> getRoots(Search search);
	};
	
	public class Node extends TreeNodeImpl<E>{
		private static final long serialVersionUID = 1L;
		private boolean processed = false;
		public boolean isProcessed() {
			return processed;
		}
		public void setProcessed(boolean processed) {
			this.processed = processed;
		}
	};
	
	private DataRetriever<E> retreiver;
	
	private Node root;
	
	private Search search;
	
	public EntityTreeModel(Class<E> clazz, DataRetriever<E> retreiver) {
		this.retreiver = retreiver;
		this.search = new Search(clazz);
	}
	
	public TreeNode<E> getTreeNode(){
		if(root == null){
			root = new Node();
			for(E e: retreiver.getRoots(search)){
				Node nn = new Node();
				nn.setData(e);
				nn.setParent(null);
				root.addChild(retreiver.getId(e), nn);
				addChildrenNodes(nn);
			}
			root.setProcessed(true);
		}
		return root;
	}
	
	@SuppressWarnings("unchecked")
	public void processExpansion(NodeExpandedEvent event) {
		Object src = event.getSource();
		if (src instanceof HtmlTree) {
			TreeNode<E> node = ((HtmlTree) src).getTreeNode();
			Iterator<Entry<Object,TreeNode<E>>> it = node.getChildren();
			while(it.hasNext()){
				Node n = (Node) it.next().getValue();
				if (n != null && n.isLeaf() && !n.isProcessed()) {
					addChildrenNodes(n);
				}
			}
		}
	}
	
	private void addChildrenNodes(Node node){
		for(E n: retreiver.listChildren(node.getData(), search)){
			Node nn = new Node();  
			nn.setData(n);  
			nn.setParent(node);  
			node.addChild(retreiver.getId(n), nn); 
		}
		node.setProcessed(true);
	}
	
	public void clear(){
		root = null;
	}
	
	public Search getSearch() {
		return search;
	}

}
