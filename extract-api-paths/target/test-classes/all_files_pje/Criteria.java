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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe de definição dos critérios a serem respeitados em uma consulta a
 * dados.
 * 
 * @author Antonio Augusto Silva Martins
 * @author Paulo Cristovão de Araújo Silva Filho
 * 
 */
public class Criteria {

	public static final String MATCH_ANY_CHAR = "_";

	/**
	 * O tipo de operação a ser respeitado quando da consulta por este critério.
	 */
	private Operator operator;

	/**
	 * O caminho, segundo o padrão JavaBean, a partir do objeto principal da
	 * consulta.
	 */
	private String attribute;

	private String concreteAttribute;

	private List<Object> value;

	protected Criteria parent;

	private Map<String, Boolean> requiredMap = new HashMap<String, Boolean>();
	
	private boolean filter = false;

	private Translate translate;
	
	/**
	 * Construtor do critério.
	 * 
	 * @param operator
	 *            o tipo de operador a ser respeitado no critério
	 * @param attribute
	 *            o campo que será utilizado como relevante no critério
	 * @param value
	 *            a lista de valores a serem utilizados para comparação
	 */
	private Criteria(Operator operator, String attribute, Object... value) {
		this.value = new ArrayList<Object>();
		this.value.addAll(Arrays.asList(value));
		init(operator, attribute);
	}

	/**
	 * Construtor do critério.
	 * 
	 * @param operator
	 *            o tipo de operador a ser respeitado no critério
	 * @param attribute
	 *            o campo que será utilizado como relevante no critério
	 * @param value
	 *            a lista de valores a serem utilizados para comparação
	 */
	private Criteria(Operator operator, String attribute, List<?> values) {
		this.value = new ArrayList<Object>(values.size());
		this.value.addAll(values);
		init(operator, attribute);
	}

	/**
	 * Copia o critério dado, assegurando o descoplamento dos objetos
	 * componentes e que o critério concreto ({@link #getConcreteAttribute()})
	 * seja recalculado caso a cópia seja incluída em um novo {@link Search}.
	 * 
	 * @return um critério idêntico ao original, mas dele independente.
	 */
	public Criteria copy() {
		Criteria ret = null;
		switch (operator) {
		case and:
		case or:
			List<Criteria> crits = new ArrayList<Criteria>();
			for (Object o : value) {
				Criteria c = (Criteria) o;
				crits.add(c.copy());
			}
			if(operator == Operator.and){
				ret = Criteria.and(crits.toArray(new Criteria[crits.size()]));
			}else{
				ret = Criteria.or(crits.toArray(new Criteria[crits.size()]));
			}
			break;
		case not:
			ret = Criteria.not(((Criteria) value.get(0)).copy());
			break;
		case between:
			ret = Criteria.between(getAttribute(), value.get(0), value.get(1));
			break;
		case contains:
			ret = Criteria.contains(getAttribute(), (String) value.get(0));
			break;
		case empty:
			ret = Criteria.empty(getAttribute());
			break;
		case endsWith:
			ret = Criteria.endsWith(getAttribute(), (String) value.get(0));
			break;
		case equals:
			ret = Criteria.equals(getAttribute(), value.get(0));
			break;
		case greater:
			ret = Criteria.greater(getAttribute(), value.get(0));
			break;
		case greaterOrEquals:
			ret = Criteria.greaterOrEquals(getAttribute(), value.get(0));
			break;
		case in:
			ret = Criteria.in(getAttribute(),
					value.toArray(new Object[value.size()]));
			break;
		case isNull:
			ret = Criteria.isNull(getAttribute());
			break;
		case less:
			ret = Criteria.less(getAttribute(), value.get(0));
			break;
		case lessOrEquals:
			ret = Criteria.lessOrEquals(getAttribute(), value.get(0));
			break;
		case notEquals:
			ret = Criteria.notEquals(getAttribute(), value.get(0));
			break;
		case startsWith:
			ret = Criteria.startsWith(getAttribute(), (String) value.get(0));
			break;
		default:
			throw new IllegalArgumentException(
					"Operador do critério a ser copiado não foi identificado.");
		}
		return ret;
	}

	/**
	 * Inicializador dos dados básicos de um critério.
	 * 
	 * @param operator
	 *            o tipo de operador a ser respeitado
	 * @param attribute
	 *            o campo de interesse
	 */
	private void init(Operator operator, String attribute) {
		this.operator = operator;
		this.attribute = attribute;
		this.concreteAttribute = "o." + this.attribute;
		this.parent = null;
	}

	/**
	 * Indica se este critério é um critério contido em um outro critério de
	 * combinação.
	 * 
	 * @return true, se o critério estiver contido em um outro critério de
	 *         combinação
	 * @see #or(Criteria...)
	 */
	public boolean isChild() {
		return parent != null;
	}

	/**
	 * Recupera o critério pai deste critério, se existente. É aplicável para
	 * critérios incluídos em critérios do tipo {@link #and(Criteria...)},
	 * {@link #or(Criteria...)} e {@link #not(Criteria)}.
	 * 
	 * @return o critério pai, ou nulo se não for um critério filho
	 */
	public Criteria getParent() {
		return parent;
	}

	/**
	 * Indica o tipo de operação que deve ser utilizada neste critério.
	 * 
	 * @return a operação
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * Recupera o atributo a ser avaliado com este critério.
	 * 
	 * @return o atributo do objeto a ser avaliado
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * Recupera o atributo concreto a ser utilizado na consulta ao serviço de
	 * acesso a dados. Pode ser, por exemplo, o alias a ser utilizado no JPQL ou
	 * a URL de um serviço REST.
	 * 
	 * @return o atributo concreto a ser utilizado na consulta
	 */
	public String getConcreteAttribute() {
		return concreteAttribute;
	}

	/**
	 * Atribui a este critério um caminho concreto a ser utilizado na consulta
	 * resultante. Pode ser, por exemplo, o alias a ser utilizado no JPQL ou a
	 * URL de um serviço REST.
	 * 
	 * @param concreteAttribute
	 *            o caminho concreto a ser utilizado
	 */
	protected void setConcreteAttribute(String concreteAttribute) {
		this.concreteAttribute = concreteAttribute;
	}

	/**
	 * Recupera a lista de valores de comparação a serem utilizados quando da
	 * aplicação do critério.
	 * 
	 * @return a lista de valores
	 */
	public List<Object> getValue() {
		return value;
	}

	/**
	 * Indica se o campo com o caminho dado deste critério deve ter sua presença
	 * exigida ou não nos resultados de uma dada pesquisa. O comportamento
	 * padrão é que seja exigível, ou seja, que, sendo definido um caminho dado,
	 * o conjunto de elementos pesquisado somente incluam resultados que
	 * contenham objetos no caminho dado, ainda, que eles não satisfaçam o
	 * critério informado.
	 * 
	 * Marcar um campo como desnecessário seria o equivalente a apontar que um
	 * dado relacionamento de tabela deve ser incluído como um OUTER JOIN.
	 * 
	 * @param field
	 *            o caminho do elemento que se pretende identificar a
	 *            necessidade de existir no resultado
	 * @return true, se o elemento for necessário.
	 */
	public boolean isRequired(String field) {
		return requiredMap.get(field) == null ? true : requiredMap.get(field);
	}

	/**
	 * Permite indicar que o elemento ao final do caminho dado deve ter sua
	 * presença exigida ou dispensada nos resultados de uma dada pesquisa. O
	 * comportamento padrão é que seja exigível, ou seja, que, sendo definido um
	 * caminho dado, o conjunto de elementos pesquisado somente incluam
	 * resultados que contenham objetos no caminho dado, ainda, que eles não
	 * satisfaçam o critério informado.
	 * 
	 * Marcar um campo como desnecessário seria o equivalente a apontar que um
	 * dado relacionamento de tabela deve ser incluído como um OUTER JOIN.
	 * 
	 * @param field
	 *            o caminho até o elemento cuja presença se predente identificar
	 *            como necessária
	 * @param required
	 *            a existência de obrigatoriedade (true) ou não (false) a ser
	 *            atribuída
	 */
	public void setRequired(String field, boolean required) {		
		if (!getAttribute().startsWith(field)) {
			throw new IllegalArgumentException(
					"Não é possível critério de obrigatoriedade para campo não contido no caminho de pesquisa.");
		} else {
			requiredMap.put(field, required);
		}
	}

	/**
	 * Instancia um critério de pesquisa em que o valor do atributo dado deve
	 * ser igual ao do valor indicado.
	 * 
	 * @param attribute
	 *            o atributo cujo valor deve ser igual ao dado
	 * @param value
	 *            o valor paradigma para a comparação
	 * @return o critério instanciado
	 */
	public static Criteria equals(String attribute, Object value) {
		return new Criteria(Operator.equals, attribute, value);
	}
	
	/**
	 * Instancia um critério de pesquisa em que o valor do atributo dado deve
	 * ser diferente do valor indicado.
	 * 
	 * @param attribute
	 *            o atributo cujo valor deve ser diferente do dado
	 * @param value
	 *            o valor paradigma para a comparação
	 * @return o critério instanciado
	 */
	public static Criteria notEquals(String attribute, Object value) {
		return new Criteria(Operator.notEquals, attribute, value);
	}

	/**
	 * Instancia um critério de pesquisa em que o valor do atributo dado deve
	 * ser maior que o do valor indicado. A comparação deve seguir os meios de
	 * comparação naturais dos objetos ou do serviço de acesso a dados.
	 * 
	 * @param attribute
	 *            o atributo cujo valor deve ser maior que o dado
	 * @param value
	 *            o valor paradigma para a comparação
	 * @return o critério instanciado
	 */
	public static Criteria greater(String attribute, Object value) {
		return new Criteria(Operator.greater, attribute, value);
	}

	/**
	 * Instancia um critério de pesquisa em que o valor do atributo dado deve
	 * ser maior ou igual ao valor indicado. A comparação deve seguir os meios
	 * de comparação naturais dos objetos ou do serviço de acesso a dados.
	 * 
	 * @param attribute
	 *            o atributo cujo valor deve ser maior ou igual ao dado
	 * @param value
	 *            o valor paradigma para a comparação
	 * @return o critério instanciado
	 */
	public static Criteria greaterOrEquals(String attribute, Object value) {
		return new Criteria(Operator.greaterOrEquals, attribute, value);
	}

	/**
	 * Instancia um critério de pesquisa em que o valor do atributo dado deve
	 * ser menor que o do valor indicado. A comparação deve seguir os meios de
	 * comparação naturais dos objetos ou do serviço de acesso a dados.
	 * 
	 * @param attribute
	 *            o atributo cujo valor deve ser menor que o dado
	 * @param value
	 *            o valor paradigma para a comparação
	 * @return o critério instanciado
	 */
	public static Criteria less(String attribute, Object value) {
		return new Criteria(Operator.less, attribute, value);
	}

	/**
	 * Instancia um critério de pesquisa em que o valor do atributo dado deve
	 * ser menor ou igual ao valor indicado. A comparação deve seguir os meios
	 * de comparação naturais dos objetos ou do serviço de acesso a dados.
	 * 
	 * @param attribute
	 *            o atributo cujo valor deve ser menor ou igual ao dado
	 * @param value
	 *            o valor paradigma para a comparação
	 * @return o critério instanciado
	 */
	public static Criteria lessOrEquals(String attribute, Object value) {
		return new Criteria(Operator.lessOrEquals, attribute, value);
	}

	/**
	 * Instancia um critério de pesquisa em que o valor do atributo dado deve
	 * estar contido no valor indicado. Normalmente será utilizado para
	 * identificar se o texto contido no atributo contém o texto dado.
	 * 
	 * @param attribute
	 *            o atributo cujo texto deve conter o dado
	 * @param value
	 *            o texto a ser pesquisado
	 * @return o critério instanciado
	 */
	public static Criteria contains(String attribute, String value) {
		return new Criteria(Operator.contains, attribute, value);
	}
	
	/**
	 * Instancia um critério de pesquisa em que o valor do atributo transformado deve
	 * estar contido no valor indicado. Normalmente será utilizado para
	 * identificar se o texto contido no atributo transformado contém o texto dado.
	 * 
	 * @param attribute
	 *            o atributo cujo texto deve conter o dado
	 * @param translate
	 *            o transformador do atributo
	 * @param value
	 *            o texto a ser pesquisado
	 * @return o critério instanciado
	 */
	public static Criteria contains(String attribute, Translate translate, String value) {
		Criteria criteria = contains(attribute, value);;
		if(translate != null) {
			translate.setAtribute(criteria.concreteAttribute);
			criteria.translate = translate;
		}
		return criteria;
	}

	/**
	 * Instancia um critério de pesquisa em que o valor do atributo dado deve
	 * iniciar pelo texto dado.
	 * 
	 * @param attribute
	 *            o atributo cujo texto deve iniciar com o dado
	 * @param value
	 *            o texto a ser pesquisado
	 * @return o critério instanciado
	 */
	public static Criteria startsWith(String attribute, String value) {
		return new Criteria(Operator.startsWith, attribute, value);
	}

	/**
	 * Instancia um critério de pesquisa em que o valor do atributo dado deve
	 * estar terminar com o texto dado.
	 * 
	 * @param attribute
	 *            o atributo cujo texto deve terminar com o dado
	 * @param value
	 *            o texto a ser pesquisado
	 * @return o critério instanciado
	 */
	public static Criteria endsWith(String attribute, String value) {
		return new Criteria(Operator.endsWith, attribute, value);
	}

	/**
	 * Instancia um critério de pesquisa em que o valor do atributo dado deve
	 * estar, do ponto de vista da comparação natural do tipo de dado, entre
	 * dois valores.
	 * 
	 * @param attribute
	 *            o atributo a ser comparado
	 * @param first
	 *            o limite inferior do intervalo de comparação
	 * @param last
	 *            o limite superior do intervalo de comparação
	 * @return o critério instanciado
	 */
	public static Criteria between(String attribute, Object first, Object last) {
		return new Criteria(Operator.between, attribute, first, last);
	}

	/**
	 * Instancia um critério de pesquisa em que o valor do atributo dado deve
	 * estar contido em um conjunto de valores.
	 * 
	 * @param attribute
	 *            o atributo a ser comparado
	 * @param values
	 *            os valores do conjunto no qual o valor deve estar contido
	 * @return o critério instanciado
	 */
	public static Criteria in(String attribute, Object[] values) {
		return new Criteria(Operator.in, attribute, values);
	}

	/**
	 * Instancia um critério de pesquisa em que o valor do atributo dado, que
	 * deve ser uma lista, deve estar vazio.
	 * 
	 * @param attribute
	 *            o atributo que deve estar vazio
	 * @return o critério instanciado
	 */
	public static Criteria empty(String attribute) {
		Criteria ret = new Criteria(Operator.empty, attribute, new Object[] {});
		ret.value.clear();
		return ret;
	}

	/**
	 * Instancia um critério de pesquisa em que o valor do atributo dado deve
	 * ser nulo.
	 * 
	 * @param attribute
	 *            o atributo que se espera estar nulo.
	 * @return o critério instanciado
	 */
	public static Criteria isNull(String attribute) {
		Criteria ret = new Criteria(Operator.isNull, attribute, new Object[] {});
		ret.value.clear();
		return ret;
	}
	
	public static Criteria path(String attribute){
		Criteria ret = new Criteria(Operator.path, attribute, new Object[]{});
		ret.value.clear();
		return ret;
	}
	
	public static Criteria bitwiseAnd(String attibute, Object first, Object second){
		return new Criteria(Operator.bitwiseAnd, attibute, first, second);
	}
	
	public static Criteria exists(String attribute){
		return new Criteria(Operator.exists, attribute, new Object[]{});
	}
	
	public static Criteria notExists(String attribute){
		return new Criteria(Operator.notExists, attribute, new Object[]{});
	}
	
	public static Criteria fullText(String attribute, String value){
		return new Criteria(Operator.fulltext, attribute, value);
	}

	/**
	 * Instancia um critério de pesquisa em que o critério dado deve ser negado.
	 * 
	 * @param criteria
	 *            o critério que deve ser negado
	 * @return o critério instanciado
	 */
	public static Criteria not(Criteria criteria) {
		Criteria ret = new Criteria(Operator.not, null, criteria);
		for (int i = 0; i < ret.value.size(); i++) {
			((Criteria) ret.value.get(i)).parent = ret;
		}
		return ret;
	}
	
	public static Criteria greater(Criteria first, Criteria second) {
		if(first.operator != Operator.path || second.operator != Operator.path){
			throw new IllegalArgumentException("Somente é possível criar critérios de 'maior que' entre caminhos.");
		}
		Criteria ret = new Criteria(Operator.greater, null, Arrays.asList(new Object[]{first, second}));
		for(Object o: ret.value){
			Criteria c = (Criteria) o;
			c.parent = ret;
		}
		return ret;
	}
	
	public static Criteria equals(Criteria first, Criteria second) {
		if(first.operator != Operator.path || second.operator != Operator.path){
			throw new IllegalArgumentException("Somente é possível criar critérios de igualdade entre caminhos.");
		}
		Criteria ret = new Criteria(Operator.equals, null, Arrays.asList(new Object[]{first, second}));
		for(Object o: ret.value){
			Criteria c = (Criteria) o;
			c.parent = ret;
		}
		return ret;
	}

	/**
	 * Instancia um critério de pesquisa em que os critérios fornecidos serão
	 * considerados alternativamente para uma resposta positiva. Em outras
	 * palavras, a implementação de uma pesquisa utilizando um critério tal deve
	 * assegurar que o resultado inclua os objetos que atendam a quaisquer dos
	 * critérios incluídos neste critério.
	 * 
	 * @param criterias
	 *            os critérios alternativos
	 * @return o critério instanciado
	 */

	public static Criteria or(Criteria... criterias) {
		Criteria ret = new Criteria(Operator.or, null, retornaCriteriosFinais(criterias));
		for (int i = 0; i < ret.value.size(); i++) {
			Criteria c = (Criteria) ret.value.get(i);
			c.parent = ret;
			if (c.getOperator() == Operator.and) {
				for (int j = 0; j < c.getValue().size(); j++) {
					Criteria child = (Criteria) c.getValue().get(j);
					child.parent = c;
				}
			}
		}
		return ret;
	}
	
	/**
	 * Instancia um critério de pesquisa em que os critérios fornecidos serão
	 * considerados obrigatórios para uma resposta positiva. Em outras palavras,
	 * a implementação de uma pesquisa utilizando um critério tal deve assegurar
	 * que o resultado inclua os objetos que atendam a todos os critérios
	 * incluídos neste critério.
	 * 
	 * Idealmente, somente deve ser utilizado quando da construção de critérios
	 * {@link #or(Criteria...)} em que os critérios alternativos internos têm,
	 * em si mesmos, a conjunção de critérios obrigatórios.
	 * 
	 * @param criterias
	 *            os critérios obrigatórios
	 * @return o critério instanciado
	 */
	public static Criteria and(Criteria... criterias) {
		Criteria ret = new Criteria(Operator.and, null, retornaCriteriosFinais(criterias));
		for (int i = 0; i < ret.value.size(); i++) {
			((Criteria) ret.value.get(i)).parent = ret;
		}
		return ret;
	}
	
	/**
	 * Recebe uma lista de argumentos variáveis do tipo Criteria 
	 * e retorna uma lista Criteria sem os objetos nulos.
	 * 
	 * @param criterias
	 * @return List<Criteria>
	 */
	private static List<Criteria> retornaCriteriosFinais(Criteria... criterias){
		List<Criteria> criteriosFinais = new ArrayList<Criteria>(0);
		for (Criteria criteria : criterias) {
			if (criteria != null){
				criteriosFinais.add(criteria);
			}
		}
		return criteriosFinais;
	}
	
	@Override
	public String toString() {
		return operator.toString() + ":[" + attribute + "]:[" + value.toString() + "]";
	}
	
	public Criteria asFilter(){
		this.filter = true;
		if(operator == Operator.and || operator == Operator.or || operator == Operator.not){
			for(Object o: value){
				Criteria crit = (Criteria) o;
				crit.asFilter();
			}
		}
		return this;
	}
	
	public boolean isFilter() {
		return filter;
	}

	public Translate getTranslate() {
		return translate;
	}

}