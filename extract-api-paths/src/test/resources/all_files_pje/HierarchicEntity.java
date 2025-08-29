/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

/**
 * Superclasse abstrata definidora de propriedades e métodos para entidades hierárquicas, ou seja,
 * que estão distribuídas em uma configuração de árvore, ainda que tenham raízes diversas.
 * 
 * @author cristof
 *
 */
@MappedSuperclass
public abstract class HierarchicEntity <T extends HierarchicEntity<T>> implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public static final String BREADCRUMB_SEPARATOR = ":";
	
	public static final String PATH_SEPARATOR = "|";

	public static final String UPDATE_CHILDREN_EVENT = "pje:evento:atualizarTrilhaDePao";

	private String code;

	private String description;
	
	private T parent;
	
	private List<T> children;
	
	private String pathDescription;
	
	private String breadcrumb;
	
	/**
	 * Recupera o identificador da entidade hierárquica concreta.
	 * 
	 * @return o identificador JPA da entidade concreta
	 */
	@Transient
	public abstract Object getId();
	
	/**
	 * Recupera o código unívoco de referência da entidade.
	 * 
	 * @return o código identificador unívoco da entidade.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Atribui à entidade um código identificador unívoco.
	 * 
	 * @param code o código a ser atribuído
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Recupera o descritor desta entidade.
	 * 
	 * @return o descritor desta entidade.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Atribui a esta entidade um descritor.
	 * 
	 * @param description a descrição a ser atribuída
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Recupera a entidade hierarquicamente superior a esta entidade.
	 * 
	 * @return a entidade hierarquicamente superior, ou nulo se esta entidade for
	 * uma entidade raiz.
	 */
	@ManyToOne(optional=true)
	public T getParent() {
		return parent;
	}

	/**
	 * Atribui a esta entidade uma entidade hierarquicamente superior.
	 * 
	 * @param parent a entidade a ser atribuída como superior
	 */
	public void setParent(T parent) {
		this.parent = parent;
	}

	/**
	 * Recupera os descendentes diretos desta entidade.
	 * 
	 * @return os descendentes diretos
	 */
	@OneToMany(fetch=FetchType.LAZY, mappedBy="parent")
	public List<T> getChildren() {
		return children;
	}

	/**
	 * Atribui a esta entidade uma lista de descendentes diretos.
	 * Não deve ser utilizado diretamente em razão da implementação JPA do hibernate.
	 * 
	 * @param children a lista de entidades descendentes diretas.
	 */
	public void setChildren(List<T> children) {
		this.children = children;
	}

	/**
	 * Recupera o caminho completo da hierarquia desta entidade por meio
	 * da concatenação dos descritores da entidade com um separador específico.
	 * 
	 * @return o caminho completo descritivo
	 */
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	public String getPathDescription() {
		return pathDescription;
	}

	/**
	 * Atribui a esta entidade um caminho completo de sua hierarquia, que deve
	 * ser resultado da concatenação dos descritores da entidade com um separador específico.
	 * 
	 * Não deve ser manipulado diretamente à exceção dos observadores de persistência.
	 * 
	 * @param pathDescription o caminho completo a ser atribuído
	 * @see HierarchicEntityListener
	 * @see #updateHierarchy()
	 */
	public void setPathDescription(String pathDescription) {
		this.pathDescription = pathDescription;
	}

	/**
	 * Recupera a trilha de pão completa dos códigos desta entidade, na ordem da hierarquia,
	 * ou seja, codigoRaiz:codigoAvo:codigoPai:codigoEstaEntidade.
	 * 
	 * @return a trilha de pão
	 */
	@Basic(optional=false)
	@Column(name="breadcrumb", length=256)
	public String getBreadcrumb() {
		return breadcrumb;
	}

	/**
	 * Atribui a esta entidade uma trilha de pão de seus códigos unívocos, na ordem da hierarquia.
	 * Exemplo: codigoRaiz:codigoAvo:codigoPai:codigoEstaEntidade.
	 * 
	 * Não deve ser manipulado diretamente, senão pelos observadores de persistência.
	 *  
	 * @param breadcrumb a trilha a ser atribuída.
	 * @see HierarchicEntityListener
	 * @see #updateHierarchy()
	 */
	public void setBreadcrumb(String breadcrumb) {
		this.breadcrumb = breadcrumb;
	}
	
	/**
	 * Verifica se esta entidade hierárquica tem como ancestral a entidade dada.
	 * 
	 * @param candidate o possível ancestral desta entidade
	 * @return true, se o candidato for um dos ancestrais desta entidade
	 */
	@Transient
	public boolean isDescendentOf(T candidate){
		if(candidate == null){
			return false;
		}
		List<T> hierarchy = getHierarchy();
		return hierarchy.subList(0, hierarchy.size() - 1).contains(candidate);
	}
	
	/**
	 * Verifica se esta entidade hierárquica é ancestral de uma entidade dada.
	 * 
	 * @param candidate o possível descendente desta entidade
	 * @return true, se o candidato tiver esta entidade como um de seus ancestrais
	 */
	@SuppressWarnings("unchecked")
	@Transient
	public boolean isAncestorOf(T candidate){
		if(candidate == null){
			return false;
		}
		return candidate.isDescendentOf((T) this);
	}
	
	/**
	 * Recupera a lista de entidades que compõe a hierarquia desta entidade,
	 * na ordem da raiz até a própria entidade. Se houver alguma referência circular,
	 * a lista será limitada à entidade ancestral imediatamente anterior a essa referência,
	 * como se essa entidade ancestral fosse a entidade raiz.
	 * 
	 * @return a lista de entidades da hierarquia desta entidade.
	 * @see HierarchicEntity#isCircular()
	 */
	@Transient
	@SuppressWarnings("unchecked")
	public List<T> getHierarchy(){
		List<T> ret = new ArrayList<T>();
		T p = (T) this;
		ret.add(0, p);
		while((p = p.getParent()) != null && p != this){
			ret.add(0, p);
		}
		return ret;
	}
	
	/**
	 * Atualiza as informações de hierarquia desta entidade.
	 * 
	 * @return true, se houve alguma atualização.
	 * @see #setPathDescription(String)
	 * @see #setBreadcrumb(String)
	 */
	@Transient
	public boolean updateHierarchy(){
		List<T> ancestors = getHierarchy();
		StringBuilder bcsb = new StringBuilder();
		StringBuilder pdsb = new StringBuilder();
		int size = ancestors.size();
		int i = 0;
		for(T r: ancestors){
			bcsb.append(r.getCode());
			pdsb.append(r.getDescription());
			if(i < (size - 1)){
				bcsb.append(HierarchicEntity.BREADCRUMB_SEPARATOR);
				pdsb.append(HierarchicEntity.PATH_SEPARATOR);
			}
			i++;
		}
		String bc = bcsb.toString();
		String pd = pdsb.toString();
		boolean ret = false;
		if(getBreadcrumb() == null || !getBreadcrumb().equals(bc)){
			setBreadcrumb(bc);
			ret = true;
		}
		if(getPathDescription() == null || !getPathDescription().equals(pd)){
			setPathDescription(pd);
			ret = true;
		}
		return ret;
	}
	
	/**
	 * Indica se esta entidade tem alguma referência circular em sua hierarquia,
	 * ou seja, se algum de seus ancestrais é ela mesma.
	 * 
	 * @return true, se houver referência circular.
	 */
	@Transient
	@SuppressWarnings("unchecked")
	public boolean isCircular(){
		T he = (T) this;
		boolean circular = false;
		while(!circular && (he = he.getParent()) != null){
			if(he.equals(this)){
				circular = true;
			}
		}
		return circular;
	}
	
}
