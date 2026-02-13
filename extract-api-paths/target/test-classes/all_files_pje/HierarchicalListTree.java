package br.com.infox.editor.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.jboss.util.Strings;

import br.jus.pje.nucleo.enums.editor.Hierarchical;

/**
 * Tree montada a partir dos atributos nivel, ordem e numeração 
 * contidos nos seus itens.
 * @author tassio
 *
 * @param <T> Tipo dos itens armazendos nos nós da tree, que implementam a interface {@link Hierarchical}
 */
public class HierarchicalListTree<T extends Hierarchical> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "hierarchicalListTree";

	private static final String TITULO_PADRAO = "Tópicos do Documento";

	protected HierarchicalRoot hierarchicalRoot;
	protected DefaultMutableTreeNode root;
	protected DefaultMutableTreeNode selectedNode;
	protected List<T> hierarchicalList = new ArrayList<T>();
	
	public HierarchicalListTree() {
		this(TITULO_PADRAO);
	}
	
	public HierarchicalListTree(String titulo) {
		hierarchicalRoot = new HierarchicalRoot(titulo);
	}

	/**
	 * Constroi a tree caso ainda ela não tenha sido construída, a partir
	 * da hierarchicalList.
	 * @return raiz da tree
	 */
	public TreeNode getRoot() {
		if (root == null) {
			buildTree();
		}
		return root;
	}
	
	private void buildTree() {
		root = new DefaultMutableTreeNode(hierarchicalRoot);
		DefaultMutableTreeNode nodePai = root;
		int ultimoNivel = 1;
		ListIterator<T> listIterator = hierarchicalList.listIterator();
		while (listIterator.hasNext()) {
			T hierarchical = listIterator.next();
			
			if (hierarchical.getNivel() > ultimoNivel) {
				ultimoNivel = hierarchical.getNivel();
				nodePai = (DefaultMutableTreeNode) nodePai.getLastChild();
			} else if (hierarchical.getNivel() < ultimoNivel) {
				for (int nivel = ultimoNivel; nivel > hierarchical.getNivel(); nivel--) {
					nodePai = (DefaultMutableTreeNode) nodePai.getParent();
				}
				ultimoNivel = hierarchical.getNivel();
			}
			
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(hierarchical);
			nodePai.add(node);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void buildHierarchicalList() {
		Enumeration<DefaultMutableTreeNode> preorderEnumeration = ((DefaultMutableTreeNode) getRoot()).preorderEnumeration();
		hierarchicalList.clear();
		
		int i = 1;
		preorderEnumeration.nextElement(); // Ignora root
		while (preorderEnumeration.hasMoreElements()) {
			DefaultMutableTreeNode node = preorderEnumeration.nextElement();
			
			T hierarchical = (T) node.getUserObject();
			hierarchical.setNivel(node.getLevel());
			hierarchical.setNumeracao(hierarchical.isNumerado() ? getNumeracaoNo(node) : null);
			hierarchical.setOrdem(i++);
			
			hierarchicalList.add(hierarchical);
		}
	}
	
	public boolean isEmpty() {
		return hierarchicalList.isEmpty();
	}

	
	@SuppressWarnings("unchecked")
	private Integer getNumeracaoNo(DefaultMutableTreeNode node) {
		DefaultMutableTreeNode previousSibling = node.getPreviousSibling();
		if (previousSibling != null) {
			Integer numeracao = ((T) previousSibling.getUserObject()).getNumeracao();
			return numeracao != null ? numeracao + 1 : getNumeracaoNo(previousSibling); 
		} else {
			return 1;
		}
	}

	@SuppressWarnings("rawtypes")
	private DefaultMutableTreeNode getNode(T hierarchical) {
		if (hierarchical != null) {
			Enumeration preorderEnumeration = ((DefaultMutableTreeNode) getRoot()).preorderEnumeration();
			while (preorderEnumeration.hasMoreElements()) {
				DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) preorderEnumeration.nextElement();
				if (hierarchical.equals(nextElement.getUserObject())) {
					return nextElement;
				}
			}
		}
		return null;
	}
	
	/**
	 * Método para alterar a label do nó raiz da tree
	 * @param label nova label do nó raiz
	 */
	public void setTituloRoot(String label) {
		hierarchicalRoot.setTitulo(label);
	}
	
	/**
	 * Limpa a tree 
	 */
	public void clear() {
		hierarchicalList = new ArrayList<T>();
		root = null;
		selectedNode = null;
	}

	/**
	 * Seta o item selecionado na tree
	 * @param selected item a ser selecionado
	 */
	public void setSelected(T selected) {
		selectedNode = getNode(selected);
	}

	/**
	 * Retorna o item selecionado na tree
	 * @return item selecionado
	 */
	@SuppressWarnings("unchecked")
	public T getSelected() {
		return selectedNode != null ? (T) selectedNode.getUserObject() : null;
	}

	/**
	 * Seta a lista que popula a tree
	 * @param hierarchicalList lista contendo os itens que montam a tree
	 */
	public void setHierarchicalList(List<T> hierarchicalList) {
		this.hierarchicalList = new ArrayList<T>(hierarchicalList);
	}
	
	public List<T> getHierarchicalList() {
		return new ArrayList<T>(this.hierarchicalList);
	}
	
	/**
	 * Adiciona, abaixo do nó selecionado, um novo nó contendo o item passado 
	 * @param hierarchical item que será adicionado na tree
	 */
	public void addNode(T hierarchical) {
		addNode(hierarchical, getSelected());
	}
	
	/**
	 * Adiciona, abaixo do target, um nó contendo o item passado
	 * @param hierarchical item que será adicionado na tree
	 * @param target nó ao qual o item será adicionado logo abaixo
	 */
	public void addNode(T hierarchical, T target) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(hierarchical);
		
		if (target != null) {
			DefaultMutableTreeNode targetNode = getNode(target);
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) targetNode.getParent();
			parent.insert(node, parent.getIndex(targetNode)+1);
		} else {
			((DefaultMutableTreeNode) getRoot()).add(node);
		}
		buildHierarchicalList();
	}
	
	/**
	 * Remove o nó selecionado
	 */
	public void removeNode() {
		removeNode(getSelected());
	}
	
	/**
	 * Remove o nó que contem o item passado como parâmetro
	 * @param hierarchical item que será removido da tree
	 */
	public void removeNode(T hierarchical) {
		if (selectedNode != null && selectedNode.getUserObject().equals(hierarchical)) {
			selectedNode = null;
		}
		getNode(hierarchical).removeFromParent();
		buildHierarchicalList();
	}
	
	/**
	 * Adiciona um nó como filho do nó selecionado
	 * @param hierarchicalChild item que será adicionado na tree
	 */
	public void addChild(T hierarchicalChild) {
		addChild(hierarchicalChild, getSelected());
	}
	
	/**
	 * Adiciona um nó como filho do nó passado como pai
	 * @param hierarchicalChild item que será adicionado na tree
	 * @param hierarchicalParent item pai do item que será adicionado
	 */
	public void addChild(T hierarchicalChild, T hierarchicalParent) {
		DefaultMutableTreeNode child = new DefaultMutableTreeNode(hierarchicalChild);
		DefaultMutableTreeNode parent = getNode(hierarchicalParent);
		
		parent.add(child);
		buildHierarchicalList();
	}
	
	/**
	 * Adiciona um nó como filho do nó passado como pai no índice especificado
	 * @param hierarchicalChild item que será adicionado na tree
	 * @param hierarchicalParent item pai do item que será adicionado
	 * @param index índice onde será inserido o novo filho
	 */
	public void addChildAtIndex(T hierarchicalChild, T hierarchicalParent, int index) {
		DefaultMutableTreeNode child = new DefaultMutableTreeNode(hierarchicalChild);
		DefaultMutableTreeNode parent = getNode(hierarchicalParent);
		
		parent.insert(child, index);
		buildHierarchicalList();
	}
	
	/**
	 * Verifica se o o nó selecionado pode subir um nível
	 * @return <code>true</code>, caso o nó selecionado possa subir um nível, senão <code>false</code>
	 */
	public boolean canMoveRight() {
		return canMoveRight(getSelected());
	}
	
	/**
	 * Verifica se o nó contendo o item passado pode subir um nível 
	 * @param hierarchical item do nó que será verificado
	 * @return <code>true</code>, caso o nó que contém o item possa subir um nível, senão <code>false</code> 
	 */
	public boolean canMoveRight(T hierarchical) {
		return getNode(hierarchical).getPreviousSibling() != null;
	}
	
	/**
	 * Sobe o nível do nó selecionado, caso ele possa ser movido
	 */
	public void moveRight() {
		moveRight(getSelected());
	}
	
	/**
	 * Sobe o nível do nó que contém o item passado, caso ele possa ser movido
	 * @param item do nó que será movido
	 */
	public void moveRight(T hierarchical) {
		if (canMoveRight(hierarchical)) {
			DefaultMutableTreeNode node = getNode(hierarchical);
			DefaultMutableTreeNode previousBrother = node.getPreviousSibling();
			previousBrother.add(node);
			buildHierarchicalList();
		}
	}
	
	/**
	 * Verifica se o nó selecioando pode descer um nível
	 * @return <code>true</code>, caso o nó selecionado possa descer um nível, senão <code>false</code>
	 */
	public boolean canMoveLeft() {
		return canMoveLeft(getSelected());
	}
	
	/**
	 * Verifica se o nó que contém o item passado pode descer um nível
	 * @return <code>true</code>, caso o nó que contém o item possa descer um nível, senão <code>false</code>
	 */
	public boolean canMoveLeft(T hierarchical) {
		return getNode(hierarchical).getLevel() > 1;
	}
	
	/**
	 * Desce o nível do nó selecionado, caso ele possa ser movido
	 */
	public void moveLeft() {
		moveLeft(getSelected());
	}
	
	/**
	 * Desce o nível do nó que possui o item passado, caso ele possa ser movido
	 */
	public void moveLeft(T hierarchical) {
		if (canMoveLeft(hierarchical)) {
			DefaultMutableTreeNode node = getNode(hierarchical);
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
			DefaultMutableTreeNode grandParent = (DefaultMutableTreeNode) parent.getParent();
			
			grandParent.insert(node, grandParent.getIndex(parent) + 1);
			selectedNode = node;
			buildHierarchicalList();
		}
	}
	
	/**
	 * Verifica se o nó selecioando tem algum irmão antes dele
	 * @return <code>true</code>, caso o nó selecionado tem algum irmão antes dele, senão <code>false</code>
	 */
	public boolean canMoveUp() {
		return canMoveUp(getSelected());
	}
	
	/**
	 * Verifica se o nó que contém o item passado tem algum irmão antes dele
	 * @return <code>true</code>, caso o nó que contém o item tenha algum irmão antes dele, senão <code>false</code>
	 */
	public boolean canMoveUp(T hierarchical) {
		return getNode(hierarchical).getPreviousSibling() != null;
	}
	
	/**
	 * Move o nó selecionado para cima do irmão anterior, caso ele possa ser movido
	 */
	public void moveUp() {
		moveUp(getSelected());
	}
	
	/**
	 * Move o nó que contém o item passado para cima do irmão anterior, caso ele possa ser movido
	 */
	public void moveUp(T hierarchical) {
		if (canMoveUp(hierarchical)) {
			DefaultMutableTreeNode node = getNode(hierarchical);
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
			
			Integer index = parent.getIndex(node);
			parent.insert(node, index-1);
			
			buildHierarchicalList();
		}
	}
	
	/**
	 * Verifica se o nó selecioando tem algum irmão após ele
	 * @return <code>true</code>, caso o nó selecionado tem algum irmão após ele, senão <code>false</code>
	 */
	public boolean canMoveDown() {
		return canMoveDown(getSelected());
	}
	
	/**
	 * Verifica se o nó que contém o item passado tem algum irmão após ele
	 * @return <code>true</code>, caso o nó que contém o item tenha algum irmão após ele, senão <code>false</code>
	 */
	public boolean canMoveDown(T hierarchical) {
		return getNode(hierarchical).getNextSibling() != null;
	}
	
	/**
	 * Move o nó selecionado para baixo do irmão anterior, caso ele possa ser movido
	 */
	public void moveDown() {
		moveDown(getSelected());
	}
	
	/**
	 * Move o nó que contém o item passado para baixo do irmão anterior, caso ele possa ser movido
	 */
	public void moveDown(T hierarchical) {
		if (canMoveDown(hierarchical)) {
			DefaultMutableTreeNode node = getNode(hierarchical);
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
			
			Integer index = parent.getIndex(node);
			parent.insert(node, index+1);
			
			buildHierarchicalList();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<T> getChildren(T hierarchical) {
		List<T> children = new ArrayList<T>();
		Enumeration<DefaultMutableTreeNode> enumeration = getNode(hierarchical).children();
		while (enumeration.hasMoreElements()) {
			DefaultMutableTreeNode node = enumeration.nextElement();
			children.add((T) node.getUserObject());
		}
		return children;
	}

	@SuppressWarnings("unchecked")
	public T getParent(T hierarchical) {
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) getNode(hierarchical).getParent();
		return (T) parent.getUserObject();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (T hierarchical: hierarchicalList) {
			sb.append(hierarchical.getOrdem()); sb.append(" - ");
			sb.append(Strings.pad(" ", (hierarchical.getNivel()-1)*4));
			if (selectedNode != null && hierarchical.equals(selectedNode.getUserObject())) {
				sb.append("[");sb.append(hierarchical.toString());sb.append("]");
			} else {
				sb.append(hierarchical.toString());
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public class HierarchicalRoot implements Hierarchical {

		private static final long serialVersionUID = 1L;

		private String titulo;
		
		public HierarchicalRoot(String titulo) {
			this.titulo = titulo;
		}
		
		@Override
		public String toString() {
			return titulo;
		}
		
		public String getCodIdentificador() {
			return String.valueOf(super.hashCode());
		}
		
		public Integer getNivel() { return null; }
		public void setNivel(Integer nivel) { }
		public Integer getOrdem() { return null; }
		public void setOrdem(Integer ordem) { }
		public Integer getNumeracao() {	return null; }
		public void setNumeracao(Integer numeracao) { }
		public boolean isNumerado() { return false; }
		public String getTitulo() { return titulo; }
		public void setTitulo(String titulo) { this.titulo = titulo; }
	}

}
