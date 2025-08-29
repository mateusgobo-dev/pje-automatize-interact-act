/*
 * IBPM - Ferramenta de produtividade Java Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda. Este programa é software livre; você pode
 * redistribuí-lo e/ou modificá-lo sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela Free Software Foundation; versão 2 da
 * Licença. Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * COMERCIABILIDADE OU ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a GNU GPL para mais detalhes. Você deve ter recebido uma cópia da GNU GPL junto
 * com este programa; se não, veja em http://www.gnu.org/licenses/
 */
package br.com.infox.component.tree;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIData;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Expressions;
import org.richfaces.component.UITree;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.component.html.HtmlTreeNode;
import org.richfaces.event.NodeSelectedEvent;
import org.richfaces.function.RichFunction;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.util.CollectionUtilsPje;

@Scope(ScopeType.CONVERSATION)
public abstract class AbstractTreeHandler<E> implements TreeHandler<E>, Serializable{

	private static final long serialVersionUID = 1L;

	private E selected;
	
	// it is the root of rootList nodes
	private E rootNode;
	
	private List<E> rootNodeList;
	
	// to show the root of rootLists
	private boolean showRootNode = Boolean.TRUE;
	
	protected List<EntityNode<E>> rootList;
	
	/**
	 * Variável utilizada para armazenar a árvore de movimentos inicial.
	 */
	protected List<EntityNode<E>> initialRootList;

	protected String treeId;

	private String iconFolder;

	private String iconLeaf;

	private boolean folderSelectable = true;

	private String expression;

	private List<EntityNode<E>> selectedNodesList = new ArrayList<EntityNode<E>>(0);
	private List<EntityNode<E>> selectedNodesListParent = new ArrayList<EntityNode<E>>(0);

	private Class<E> entityClass;
	
	private UIData dataTable;
	
	private Long numRows;

	@Override
	public void clearTree(){
		selectedNodesList = new ArrayList<EntityNode<E>>();
		rootList = null;
		initialRootList = null;
		selected = null;
		clearUITree();
		if (expression != null){
			Expressions.instance().createValueExpression(expression).setValue(null);
		}
	}

	private void clearUITree(){
		if (treeId != null){
			UITree tree = (UITree) RichFunction.findComponent(treeId);
			if (tree != null){
				tree.setSelected();
				tree.setRowKey(null);
				tree.setSelected();
			}
		}
	}

	@Override
	public List<EntityNode<E>> getRoots(){
		if (rootList == null){
			Query queryRoots = getEntityManager().createQuery(getQueryRoots());
			if(dataTable != null){
				queryRoots.setFirstResult(dataTable.getFirst());
				queryRoots.setMaxResults(dataTable.getRows());
			}
			EntityNode<E> entityNode = createNode();
			entityNode.setIgnore(getEntityToIgnore());
			rootList = entityNode.getRoots(queryRoots);
		}
		return rootList;
	}

	protected EntityNode<E> createNode(){
		return new EntityNode<E>(getQueryChildrenList());
	}

	/**
	 * Lista de queries que irão gerar os nós filhos Caso haja mais de uma query, deve-se sobrescrever esse método e retornar null no método
	 * getQueryChildren()
	 * 
	 * @return
	 */
	protected String[] getQueryChildrenList(){
		String[] querys = new String[1];
		querys[0] = getQueryChildren();
		return querys;
	}

	protected EntityManager getEntityManager(){
		return EntityUtil.getEntityManager();
	}

	@Override
	@SuppressWarnings("unchecked")
	public E getSelected(){
		if (expression == null){
			return selected;
		}
		Object value = null;
		try{
			value = Expressions.instance().createValueExpression(expression).getValue();
		} catch (Exception ignore){
		}
		return (E) value;
	}

	@Override
	public void setSelected(E selected){
		if (expression == null){
			this.selected = selected;
		}
		else{
			Expressions.instance().createValueExpression(expression).setValue(selected);
		}
	}

	public List<E> getRootNodeList() {
		return rootNodeList;
	}

	public void setRootNodeList(List<E> rootNodeList) {
		if(this.rootNodeList != rootNodeList) {
			rootList = null;
			this.rootNodeList = rootNodeList;
			if(CollectionUtilsPje.isNotEmpty(this.rootNodeList)) {
				this.rootNode = this.rootNodeList.get(0);
			}
		}
	}

	public E getRootNode() {
		return rootNode;
	}

	public void setRootNode(E rootNode) {
		if(rootNode == null || this.rootNode != rootNode) {
			rootList = null;
		}
		this.rootNode = rootNode;
	}

	public boolean isShowRootNode() {
		return showRootNode;
	}

	public void setShowRootNode(boolean showRootNode) {
		if(this.showRootNode != showRootNode) {
			rootList = null;
		}
		this.showRootNode = showRootNode;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void selectListener(NodeSelectedEvent ev){
		HtmlTree tree = (HtmlTree) ev.getSource();
		treeId = tree.getId();
		EntityNode<E> en = (EntityNode<E>) tree.getData();
		setSelected(en.getEntity());
		Events.instance().raiseEvent(getEventSelected(), getSelected());
	}
	
	
	@SuppressWarnings("unchecked")
	public void selectListenerCaixa(NodeSelectedEvent ev){
		HtmlTreeNode tree = (HtmlTreeNode) ev.getSource();
		EntityNode<E> en = (EntityNode<E>) tree.getData();
		EntityNode<E> parent = en.getParent();
		
		Events.instance().raiseEvent(getEventSelectedCaixa(), parent.getEntity());
	}

	protected String getEventSelected(){
		return null;
	}
	
	protected String getEventSelectedCaixa(){
		return null;
	}


	protected abstract String getQueryRoots();

	protected abstract String getQueryChildren();
	
	@Override
	public String getIconFolder(){
		return iconFolder;
	}

	@Override
	public void setIconFolder(String iconFolder){
		this.iconFolder = iconFolder;
	}

	@Override
	public String getIconLeaf(){
		return iconLeaf;
	}

	@Override
	public void setIconLeaf(String iconLeaf){
		this.iconLeaf = iconLeaf;
	}

	@Override
	public boolean isFolderSelectable(){
		return folderSelectable;
	}

	@Override
	public void setFolderSelectable(boolean folderSelectable){
		this.folderSelectable = folderSelectable;
	}

	public String getExpression(){
		return expression;
	}

	public void setExpression(String expression){
		this.expression = "#{" + expression + "}";
	}

	/**
	 * Tratamento para que a string não fique maior que o tamanho do campo
	 * 
	 * @param selected
	 * @return
	 */
	public String getSelectedView(E selected){
		String selecionado = "";
		if (selected == null || selected.toString() == null){
			return selecionado;
		}
		else{
			if (selected.toString().length() > 25){
				selecionado = selected.toString().substring(0, 25) + "...";
			}
			else{
				selecionado = selected.toString();
			}
			return selecionado;
		}
	}

	/**
	 * Método que retorna a lista dos itens selecionados.
	 * 
	 * @return - Lista dos itens selecionados.
	 */
	public List<E> getSelectedTree(){
		List<E> selectedList = new ArrayList<E>();
		for (EntityNode<E> node : selectedNodesList){
			selectedList.add(node.getEntity());
		}
		return selectedList;
	}

	public List<EntityNode<E>> getSelectedNodesList(){
		return selectedNodesList;
	}

	public void setSelectedNodesList(List<EntityNode<E>> selectedNodesList){
		this.selectedNodesList = selectedNodesList;
	}

	public List<EntityNode<E>> getSelectedNodesListParent(){
		return selectedNodesListParent;
	}

	public void setSelectedNodesListParent(List<EntityNode<E>> selectedNodesListParent){
		this.selectedNodesListParent = selectedNodesListParent;
	}

	/**
	 * Insere o nó selecionado pela checkBox na lista dos nós selecionados.
	 * 
	 * @param node - Nó selecionado pelo usuário
	 */
	public void setSelectedNode(EntityNode<E> node){
		if (getSelected() == null || getSelected().toString() == null){
			setSelected(node.getEntity());
		}
		if (selectedNodesList.contains(node)){
			selectedNodesList.remove(node);
			selectAllChildren(node, false);
		}
		else{
			selectedNodesList.add(node);
			selectAllChildren(node, true);
		}
	}

	private void selectAllChildren(EntityNode<E> selectedNode, boolean operation){
		for (EntityNode<E> node : selectedNode.getNodes()){
			selectAllChildren(node, operation);
			node.setSelected(operation);
			if (operation){
				selectedNodesList.add(node);
			}
			else{
				selectedNodesList.remove(node);
			}
		}
	}

	/**
	 * Metodo que retorna a entidade que deve ser ignorada na montagem do treeview
	 * 
	 * @return
	 */
	protected E getEntityToIgnore(){
		return null;
	}

	public E findById(Integer id){
		return getEntityManager().find(getEntityClass(), id);
	}

	@SuppressWarnings("unchecked")
	public Class<E> getEntityClass(){
		if (entityClass == null){
			Type type = getClass().getGenericSuperclass();
			if (type instanceof ParameterizedType){
				ParameterizedType paramType = (ParameterizedType) type;
				if (paramType.getActualTypeArguments().length == 2){
					if (paramType.getActualTypeArguments()[1] instanceof TypeVariable){
						throw new IllegalArgumentException("Could not guess entity class by reflection");
					}
					else{
						entityClass = (Class<E>) paramType.getActualTypeArguments()[1];
					}
				}
				else{
					entityClass = (Class<E>) paramType.getActualTypeArguments()[0];
				}
			}
			else{
				throw new IllegalArgumentException("Could not guess entity class by reflection");
			}
		}
		return entityClass;
	}

	public UIData getDataTable() {
		return dataTable;
	}

	public void setDataTable(UIData dataTable) {
		this.dataTable = dataTable;
	}

	public Long getNumRows() {
		return numRows;
	}

	public void setNumRows(Long numRows) {
		this.numRows = numRows;
	}
}