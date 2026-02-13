/*
 * IBPM - Ferramenta de produtividade Java Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda. Este programa é software livre; você pode
 * redistribuí-lo e/ou modificá-lo sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela Free Software Foundation; versão 2 da
 * Licença. Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * COMERCIABILIDADE OU ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a GNU GPL para mais detalhes. Você deve ter recebido uma cópia da GNU GPL junto
 * com este programa; se não, veja em http://www.gnu.org/licenses/
 */
package br.com.infox.component.tree;
 
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.ArrayUtils;
import org.jboss.seam.core.Events;

import br.com.itx.util.EntityUtil;

@SuppressWarnings("unchecked")
public class EntityNode<E> implements Serializable{

	private static final long serialVersionUID = 1L;
	/**
	 * Indica o nome do parametro que contém o nivel pai dos nós a serem retornados
	 */
	public static final String PARENT_NODE = "parent";	
	private E entity;
	private E ignore;
	private boolean leaf;
	protected String[] queryChildren;
	protected List<EntityNode<E>> rootNodes;
	private List<EntityNode<E>> nodes;
	// Variavel para adição da selectBooleanCheckBox
	private Boolean selected = false;
	private EntityNode<E> parent;

	public EntityNode(){
	}
	
	/**
	 * 
	 * @param queryChildren query que retorna os nós filhos da entidade selecionada
	 */
	public EntityNode(String queryChildren){
		this.queryChildren = new String[1];
		this.queryChildren[0] = queryChildren;
	}

	public EntityNode(String[] queryChildren){
		this.queryChildren = queryChildren;
	}

	public EntityNode(EntityNode<E> parent, E entity, String[] queryChildren){
		this.queryChildren = queryChildren;
		this.parent = parent;
		this.entity = entity;
	}

	public EntityNode(EntityNode<E> parent, E entity, String queryChildren) {
		this.parent = parent;
		this.entity = entity;
		this.queryChildren = new String[1];
		this.queryChildren[0] = queryChildren;
	}

	/**
	 * Busca os nós filhos. Dispara um evento entityNodesPostGetNodes
	 * 
	 * @return lista de nós filhos da entidade passada no construtor
	 */
	public List<EntityNode<E>> getNodes(){
		if (nodes == null){
			nodes = new ArrayList<EntityNode<E>>();
			boolean parent = true;
			if (!isLeaf()){
				criarNosFilhos(parent);
				parent = false;
			}
			Events.instance().raiseEvent("entityNodesPostGetNodes", nodes);
		}
		return nodes;
	}
	
	/**
	 * Cria os nos filhos para as localizacoes encontradas.
	 * @param parent 
	 */
	private void criarNosFilhos(boolean parent) {
		for(E e : getFilhos(queryChildren)){
			if (!e.equals(getIgnore())){
				EntityNode<E> node = createChildNode(e);
				node.setIgnore(ignore);
				node.setLeaf(!parent);
				getNodes().add(node);
			}
		}
	}
	
	/**
	 * Rertorna a lista de localizacoes encontradas para as querys, caso seja passada mais de uma query filha deve ser 
	 * 		retornada a lista por ordem.
	 * @param querys Array que contem as query para retornar os nos filhos
	 * @return
	 */
	private List<E> getFilhos(String[] querys){
		List<E> filhos = null;
		for (String query : querys){
			filhos = getChildrenList(query, getEntity());
			
			if(ArrayUtils.isNotEmpty(querys) && querys.length > 1 && filhos != null && !filhos.isEmpty()){ //CollectionUtils.isEmpty(filhos)
				queryChildren = alterarArrayQuery(querys);
				break;
			}
		}
		return filhos;
	}
	
	/**
	 * Retorna o array de querys inicial menos a primeira query (possivelmente a ja executada e com o retorno previsto)
	 * @param querys Querys filhas
	 * @return querys filhas menos a primeira
	 */
	private String[] alterarArrayQuery(String[] querys){
		return Arrays.copyOfRange(querys, 1, querys.length);
	}
	
	protected List<E> getChildrenList(String hql, E entity){
		Query query = EntityUtil.createQuery(hql);
		return query.setParameter(PARENT_NODE, entity).getResultList();
	}

	protected EntityNode<E> createChildNode(E n){
		return new EntityNode<E>(this, n, this.queryChildren);
	}

	protected EntityNode<E> createRootNode(E n){
		return new EntityNode<E>(null, n, this.queryChildren);
	}

	/**
	 * 
	 * @return a entidade representada pelo nó
	 */
	public E getEntity(){
		return entity;
	}

	/**
	 * @param entity
	 */
	public void setEntity(E entity){
		this.entity = entity;
	}
	
	/**
	 * 
	 * @param queryRoots query que retorna os nós do primeiro nível
	 * @return lista dos nós do primeiro nível
	 */
	public List<EntityNode<E>> getRoots(Query queryRoots){
		if (rootNodes == null){
			rootNodes = new ArrayList<EntityNode<E>>();
			List<E> roots = queryRoots.getResultList();
			for (E e : roots){
				if (!e.equals(ignore)){
					EntityNode<E> node = createRootNode(e);
					node.setIgnore(ignore);
					rootNodes.add(node);
				}
			}
		}
		return rootNodes;
	}

	public boolean isLeaf(){
		return leaf;
	}

	public void setLeaf(boolean leaf){
		this.leaf = leaf;
	}

	public String getType(){
		return isLeaf() ? "leaf" : "folder";
	}

	@Override
	public String toString(){
		return entity.toString();
	}

	public void setSelected(Boolean selected){
		this.selected = selected;
	}

	public Boolean getSelected(){
		return selected;
	}

	/**
	 * Metodo que adiciona a entidade que deve ser ignorada na composição da tree
	 * 
	 * @param ignore
	 */
	public void setIgnore(E ignore){
		this.ignore = ignore;
	}

	public E getIgnore(){
		return ignore;
	}

	public EntityNode<E> getParent(){
		return parent;
	}

	public String[] getQueryChildren(){
		return queryChildren;
	}

	public boolean canSelect(){
		return true;
	}

	public void setNodes(List<EntityNode<E>> nodes){
		this.nodes = nodes;
	}
}