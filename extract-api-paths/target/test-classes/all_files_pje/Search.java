/**
 * pje
 * Copyright (C) 2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.search;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Classe responsável por armazenar, para uma dada consulta, os critérios que
 * devem ser respeitados.
 * 
 * @author Antonio Augusto Silva Martins
 * @author Paulo Cristovão de Araújo Silva Filho
 * 
 */
public class Search {

	private static final AlphanumComparator comparator;

	private Integer first = 0;

	private Integer max = null;

	private Class<?> entityClass;

	private Map<String, Criteria> criterias = new HashMap<String, Criteria>();

	private Map<String, JoinAlias> joins = new TreeMap<String, JoinAlias>();

	private Map<String, Order> orders = new TreeMap<String, Order>(comparator);

	private int numProp = 0;

	private boolean distinct = false;

	private boolean count = false;

	private String retrieveField;
	
	private String groupBy;
	
	private boolean closed = false;
	
	private boolean readOnly = false;
	
	private boolean cacheable = true;

	static {
		comparator = new AlphanumComparator();
	}

	/**
	 * Classe indicativa do tipo de join a ser efetivado na pesquisa.
	 * 
	 * @author Paulo Cristovão de Araújo Silva Filho
	 * 
	 */
	public class JoinAlias {

		private String alias;

		private String parentProperty;

		private String propertyPath;

		private boolean required;

		/**
		 * Construtor padrão.
		 * 
		 * @param alias o apelido a ser utilizado
		 * @param parentProperty a propriedade superior à da pesquisa
		 * @param propertyPath o atributo representado por este join
		 * @param required indicação de que o campo final é obrigatório ou não na pesquisa
		 */
		public JoinAlias(String alias, String parentProperty, String propertyPath, boolean required) {
			this.alias = alias;
			this.parentProperty = parentProperty;
			this.propertyPath = propertyPath;
			this.required = required;
		}

		/**
		 * Recupera o apelido a ser utilizado na pesquisa.
		 * 
		 * @return o apelido
		 */
		public String getAlias() {
			return alias;
		}

		/**
		 * Recupera a propriedade pai vinculada a este join.
		 * 
		 * @return a propriedade pai
		 */
		public String getParentProperty() {
			return parentProperty;
		}

		/**
		 * Recupera o caminho original da propriedade a ser pesquisada.
		 * 
		 * @return o caminho original
		 */
		public String getPropertyPath() {
			return propertyPath;
		}

		/**
		 * Indica se o join utilizado deve exigir a existência de elementos para que a pesquisa seja positiva.
		 * Por padrão, será true.
		 * 
		 * @return true, se a lista indicada por este join não puder estar vazia nos elementos de resposta
		 */
		public boolean isRequired() {
			return required;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return parentProperty + "." + propertyPath;
		}
	}

	/**
	 * Constrói um objeto de consulta para uma dado tipo de objeto.
	 * 
	 * @param clazz
	 *            o tipo de objeto que será pesquisado
	 */
	public Search(Class<?> clazz) {
		entityClass = clazz;
	}

	/**
	 * Copia o objeto de consulta, assegurando o desacoplamento de seus elementos e 
	 * a não interferência no objeto original.
	 * 
	 * @return um objeto de consulta idêntico ao original, mas dele independente.
	 * @throws NoSuchFieldException caso algum dos campos de critérios inexista na entidade dada.
	 */
	public Search copy() throws NoSuchFieldException {
		Search ret = new Search(this.entityClass);
		ret.setDistinct(distinct);
		ret.setCount(count);
		ret.setFirst(first);
		ret.setMax(max);
		ret.setRetrieveField(retrieveField);
		for (Criteria c : criterias.values()) {
			if(c.isChild()) continue;
			ret.addCriteria(c.copy());
		}
		return ret;
	}

	/**
	 * Acrescenta uma lista de critérios a esta consulta.
	 * 
	 * @param criterias os critérios a serem acrescentados
	 * @throws NoSuchFieldException caso algum dos campos de critérios inexista na entidade dada.
	 */
	public void addCriteria(Collection<Criteria> criterias) throws NoSuchFieldException {
		for (Criteria c : criterias) {
			addCriteria(c);
		}
	}

	/**
	 * Acrescenta um critério de consulta a ser utilizado em uma pesquisa.
	 * 
	 * @param criteria
	 *            o critério a ser utilizado
	 * @throws NoSuchFieldException
	 *             caso o campo de pesquisa não exista no grafo do tipo de
	 *             objeto pesquisado
	 * @throws SecurityException
	 *             caso o campo de pesquisa não seja acessível no tipo de objeto
	 *             pesquisado
	 */
	public void addCriteria(Criteria criteria) throws NoSuchFieldException {
		if(closed || criteria == null)
			return;
		addRecursivally(criteria.getAttribute(), entityClass, "o", "o", criteria);
	}

	/**
	 * Acrescenta um critério de ordenação da pesquisa.
	 * 
	 * @param attribute
	 *            o campo a ser considerado para ordenação
	 * @param order
	 *            a indicação quanto à ordenação dever ser crescente ou
	 *            decrescente
	 */
	public void addOrder(String attribute, Order order) {
		this.orders.put(attribute, order == null ? Order.ASC : order);
	}

	/**
	 * Acrescenta um critério recursivamente, criando apelidos para seus
	 * caminhos de pesquisa.
	 * 
	 * @param property
	 *            a propriedade que deve ser atingida pelo critério
	 * @param clazz
	 *            o tipo de dado ao qual deve pertencer a propriedade
	 * @param parentProperty
	 *            a propriedade superior (ou anterior) à de pesquisa
	 * @param criteria
	 *            o critério original, que será manipulado recursivamente quanto
	 *            a seu campo {@link Criteria#getConcreteAttribute()}
	 * @throws NoSuchFieldException
	 *             caso o campo de pesquisa não exista no grafo do tipo de
	 *             objeto pesquisado
	 */
	private void addRecursivally(String property, Class<?> clazz, String parentProperty, String path, Criteria criteria) throws NoSuchFieldException {
		Field f = null;
		if (parentProperty == null || parentProperty.isEmpty()) {
			parentProperty = "o";
		}
		Operator op = criteria.getOperator();
		if (op == Operator.or || op == Operator.not || op == Operator.and) {
			parseSpecialOperator(criteria);
		}else if((property == null || property.isEmpty()) && !criteria.getValue().isEmpty() && criteria.getValue().get(0) instanceof Criteria){
			for(Object o: criteria.getValue()){
				Criteria c = (Criteria) o;
				addCriteria(c);
			}
			addCriteria_(criteria);
		}else if (property.indexOf('.') == -1) { // é o último campo da chamada
			f = getDeclaredField(clazz, property);
			if (Collection.class.isAssignableFrom(f.getType())) { // o critério afeta diretamente uma collection
				if (criteria.getOperator() != Operator.empty) {
					throw new IllegalArgumentException("Não é possível definir uma coleção como propriedade pura utilizando operadores diversos de empty e not empty.");
				} else {
					addCriteria_(criteria);
				}
			} else { // o critério é uma propriedade da classe clazz
				addCriteria_(criteria);
			}
		} else { // é propriedade de algo que está inserido em outra propriedade
			if(op != Operator.exists && op != Operator.notExists){
				String prop = property.substring(0, property.indexOf('.'));
				String inner = property.substring(prop.length() + 1);
				String joinProperty = parentProperty + "." + prop;
				String newPath = path + "." + prop;
				f = getDeclaredField(clazz, prop);
				String alias = null;
				if (Collection.class.isAssignableFrom(f.getType()) || !criteria.isRequired(newPath.replaceFirst("o.", ""))) {
					if (joins.get(newPath) != null) {
						alias = joins.get(newPath).getAlias();
					} else {
						alias = "p" + numProp++;
						joins.put(newPath, new JoinAlias(alias, parentProperty, prop, criteria.isRequired(newPath.replaceFirst("o.", ""))));
					}
					criteria.setConcreteAttribute(criteria.getConcreteAttribute().replace(joinProperty, alias));
					if (Collection.class.isAssignableFrom(f.getType())) {
						addRecursivally(inner, inferCollectionContents(f), alias, newPath, criteria);
					} else {
						addRecursivally(inner, f.getType(), alias, newPath, criteria);
					}
				} else {
					alias = joinProperty;
					criteria.setConcreteAttribute(criteria.getConcreteAttribute().replace(joinProperty, alias));
					addRecursivally(inner, f.getType(), alias, newPath, criteria);
				}
			} else if (op == Operator.notExists || op == Operator.exists) {
				criteria.setConcreteAttribute(criteria.getAttribute());
				addCriteria_(criteria);
			} else {
				criteria.setConcreteAttribute(criteria.getAttribute());
			}
		}
	}
	
	private void parseSpecialOperator(Criteria criteria) throws NoSuchFieldException{
		Operator op = criteria.getOperator();
		boolean removeAnd = op == Operator.and && !criteria.isChild();
		for (int i = 0; i < criteria.getValue().size(); i++) {
			Criteria crit = (Criteria) criteria.getValue().get(i);
			if (removeAnd) {
				crit.parent = null;
			}
			addCriteria(crit);
		}
		if (!removeAnd) {
			addCriteria_(criteria);
		}
	}

	/**
	 * Recupera, por reflexão, um campo de uma dada classe, inclusive quanto às
	 * classes superiores na hierarquia.
	 * 
	 * @param clazz a classe à qual deve pertencer o campo
	 * @param property o nome do campo
	 * @return o campo
	 * @throws NoSuchFieldException caso inexista o campo na classe e em todas as suas superclasses
	 */
	private Field getDeclaredField(Class<?> clazz, String property) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(property);
		} catch (NoSuchFieldException e) {
			if (clazz.getSuperclass() != null) {
				return getDeclaredField(clazz.getSuperclass(), property);
			} else {
				throw e;
			}
		}
	}

	/**
	 * Recupera o tipo de dado contido em uma dada coleção.
	 * 
	 * @param f o campo que contém uma coleção
	 * @return o tipo de dado contido no campo
	 */
	private Class<?> inferCollectionContents(Field f) {
		ParameterizedType ptype = (ParameterizedType) f.getGenericType();
		return (Class<?>) ptype.getActualTypeArguments()[0];
	}

	/**
	 * Acrescenta o critério aos desta pesquisa. O alias dado será iniciado com
	 * OR, se o critério for do tipo alternativo, e AND, caso contrário.
	 * 
	 * @param criteria o critério a ser acrescentado.
	 */
	private void addCriteria_(Criteria criteria) {
		if (criteria.getOperator() == Operator.or) {
			getCriterias().put("OR" + getCriterias().size(), criteria);
		} else {
			getCriterias().put("AND" + getCriterias().size(), criteria);
		}
	}

	/**
	 * Limpa os critérios e ordens contidos nesta pesquisa.
	 */
	public void clear() {
		criterias.clear();
		joins.clear();
//		orders.clear();
		closed = false;
	}

	/**
	 * Recupera o mapa de critérios desta pesquisa.
	 * 
	 * @return o mapa de critérios desta pesquisa.
	 */
	public Map<String, Criteria> getCriterias() {
		return criterias;
	}

	/**
	 * Recupera o mapa de pontos de interação entre objetos desta pesquisa.
	 * 
	 * @return o mapa de pontos de interação entre objetos
	 */
	public Map<String, JoinAlias> getJoins() {
		return joins;
	}

	/**
	 * Permite marcar que a pesquisa resultante deve ser um conjunto ou pode
	 * conter elementos repetidos.
	 * 
	 * @param distinct
	 *            true, para assegurar que não haverá repetição de elementos na
	 *            resposta
	 */
	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	/**
	 * Indica se a pesquisa resultante deve ser um conjunto (um só elemento
	 * unívoco na coleção).
	 * 
	 * @return true, se a pesquisa deve retornar um conjunto
	 */
	public boolean isDistinct() {
		return distinct;
	}

	/**
	 * Permite indicar que essa pesquisa se limita a contar o número total de
	 * elementos de uma resposta.
	 * 
	 * @param count
	 *            true, para indicar que se pretende recuperar apenas o número
	 *            total de elementos.
	 */
	public void setCount(boolean count) {
		this.count = count;
	}

	/**
	 * Indica se essa pesquisa deve se limitar a contar o número total de
	 * elementos da pesquisa concreta.
	 * 
	 * @return true, se a pesquisa deve ser apenas uma contagem.
	 */
	public boolean isCount() {
		return count;
	}

	/**
	 * Indica que a pesquisa deve retornar elemento diverso da classe da
	 * pesquisa, mas uma propriedade a ela vinculada.
	 * 
	 * @param retrieveField
	 *            a propriedade ou campo do tipo de dado que deve ser
	 *            recuperado.
	 */
	public void setRetrieveField(String retrieveField) {
		if (retrieveField != null && retrieveField.isEmpty()) {
			throw new IllegalArgumentException("Não é possível recuperar um campo vazio.");
		}
		this.retrieveField = retrieveField;
	}

	/**
	 * Recupera o elemento que deve estar contido na resposta à consulta.
	 * 
	 * @return o elemento ou propriedade do tipo de dado que deve ser recuperado
	 *         na pesquisa
	 */
	public String getRetrieveField() {
		return retrieveField;
	}

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		if (groupBy != null && groupBy.isEmpty()) {
			throw new IllegalArgumentException("Não é possível agrupar um campo vazio.");
		}
		this.groupBy = groupBy;
	}

	/**
	 * Recupera os campos de ordenação que devem ser utilizados na pesquisa.
	 * 
	 * @return o mapa de campos de ordenação
	 */
	public Map<String, Order> getOrders() {
		return orders;
	}

	/**
	 * Indica qual o primeiro elemento a ser respondido a partir da consulta.
	 * 
	 * @return o primeiro elemento a ser incluído em eventual resposta.
	 */
	public Integer getFirst() {
		return first;
	}

	/**
	 * Atribui a esta consulta um número de ordem para indicar qual o primeiro elemento 
	 * da lista de todos os que atendem os critérios que deve ser respondido.
	 * 
	 * @param first o número de ordem do primeiro elemento a ser incluído na resposta.
	 */
	public void setFirst(Integer first) {
		this.first = first;
	}

	/**
	 * Indica qual o número máximo de elementos que devem estar contidos na resposta.
	 * 
	 * @return o número máximo de elementos
	 */
	public Integer getMax() {
		return max;
	}

	/**
	 * Atribui a essa consulta um número máximo de elementos a serem contidos na resposta.
	 * 
	 * @param max o máximo número de elementos a ser respondido
	 */
	public void setMax(Integer max) {
		this.max = max;
	}
	
	/**
	 * Indica que este objeto de consulta não poderá receber mais novos critérios de restrição. Não deve ser utilizado pelo desenvolvedor.
	 */
	public void close(){
		closed = true;
	}
	
	public boolean closed(){
		return closed;
	}
	
	public Class<?> getEntityClass() {
		return entityClass;
	}
	
	public String toString() {
		return "Search: " + (entityClass==null ? "" : entityClass.getSimpleName());
	}

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @return the cacheable
	 */
	public boolean isCacheable() {
		return cacheable;
	}

	/**
	 * @param readOnly the readOnly to set
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * @param cacheable the cacheable to set
	 */
	public void setCacheable(boolean cacheable) {
		this.cacheable = cacheable;
	}
	
}