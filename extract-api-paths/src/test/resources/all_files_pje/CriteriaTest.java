/**
 * pje-web
 * Copyright (C) 2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.search;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Conjunto de testes do objeto {@link Criteria}.
 * 
 * @author Paulo Cristovão de Araújo Silva Filho
 *
 */
public class CriteriaTest {

	/**
	 * Teste para o construtor de critérios de igualdade.
	 * 
	 * @see Criteria#equals(String, Object)
	 */
	@Test
	public void testEquals() {
		Criteria c = Criteria.equals("nome", "Valor");
		assertThat(c.getOperator(), is(Operator.equals));
		assertThat(c.getAttribute(), is("nome"));
		assertThat(c.getConcreteAttribute(), is("o.nome"));
		assertThat(c.getValue().size(), is(equalTo(1)));
		assertThat((String) c.getValue().get(0), is("Valor"));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		assertThat(c.isRequired("nome"), is(true));
	}

	/**
	 * Teste para o construtor de critérios de desigualdade.
	 * 
	 * @see Criteria#notEquals(String, Object)
	 */
	@Test
	public void testNotEquals() {
		Criteria c = Criteria.notEquals("nome", "Valor");
		assertThat(c.getOperator(), is(Operator.notEquals));
		assertThat(c.getAttribute(), is("nome"));
		assertThat(c.getConcreteAttribute(), is("o.nome"));
		assertThat(c.getValue().size(), is(equalTo(1)));
		assertThat((String) c.getValue().get(0), is("Valor"));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		assertThat(c.isRequired("nome"), is(true));	}

	/**
	 * Teste para o construtor de critérios de comparação por maior que.
	 * 
	 * @see Criteria#greater(String, Object)
	 */
	@Test
	public void testGreater() {
		Criteria c = Criteria.greater("nivelSigilo", 5);
		assertThat(c.getOperator(), is(Operator.greater));
		assertThat(c.getAttribute(), is("nivelSigilo"));
		assertThat(c.getConcreteAttribute(), is("o.nivelSigilo"));
		assertThat(c.getValue().size(), is(equalTo(1)));
		assertThat((Integer) c.getValue().get(0), is(5));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		assertThat(c.isRequired("nivelSigilo"), is(true));	
	}

	/**
	 * Teste para o construtor de critérios de comparação por maior ou igual a.
	 * 
	 * @see Criteria#greaterOrEquals(String, Object)
	 */
	@Test
	public void testGreaterOrEquals() {
		Criteria c = Criteria.greaterOrEquals("nivelSigilo", 5);
		assertThat(c.getOperator(), is(Operator.greaterOrEquals));
		assertThat(c.getAttribute(), is("nivelSigilo"));
		assertThat(c.getConcreteAttribute(), is("o.nivelSigilo"));
		assertThat(c.getValue().size(), is(equalTo(1)));
		assertThat((Integer) c.getValue().get(0), is(5));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		assertThat(c.isRequired("nivelSigilo"), is(true));	
	}

	/**
	 * Teste para o construtor de critérios de comparação por menor que.
	 * 
	 * @see Criteria#less(String, Object)
	 */
	@Test
	public void testLess() {
		Criteria c = Criteria.less("nivelSigilo", 5);
		assertThat(c.getOperator(), is(Operator.less));
		assertThat(c.getAttribute(), is("nivelSigilo"));
		assertThat(c.getConcreteAttribute(), is("o.nivelSigilo"));
		assertThat(c.getValue().size(), is(equalTo(1)));
		assertThat((Integer) c.getValue().get(0), is(5));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		assertThat(c.isRequired("nivelSigilo"), is(true));	
	}

	/**
	 * Teste para o construtor de critérios de comparação por menor ou igual a.
	 * 
	 * @see Criteria#lessOrEquals(String, Object)
	 */
	@Test
	public void testLessOrEquals() {
		Criteria c = Criteria.lessOrEquals("nivelSigilo", 5);
		assertThat(c.getOperator(), is(Operator.lessOrEquals));
		assertThat(c.getAttribute(), is("nivelSigilo"));
		assertThat(c.getConcreteAttribute(), is("o.nivelSigilo"));
		assertThat(c.getValue().size(), is(equalTo(1)));
		assertThat((Integer) c.getValue().get(0), is(5));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		assertThat(c.isRequired("nivelSigilo"), is(true));	
	}

	/**
	 * Teste para o construtor de critérios de verificação relativo a uma string conter uma outra.
	 * 
	 * @see Criteria#contains(String, String)
	 */
	@Test
	public void testContains() {
		Criteria c = Criteria.contains("nome", "fulano");
		assertThat(c.getOperator(), is(Operator.contains));
		assertThat(c.getAttribute(), is("nome"));
		assertThat(c.getConcreteAttribute(), is("o.nome"));
		assertThat(c.getValue().size(), is(equalTo(1)));
		assertThat((String) c.getValue().get(0), is("fulano"));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		assertThat(c.isRequired("nome"), is(true));	
	}

	/**
	 * Teste para o construtor de critérios de verificação relativo a uma string ser iniciada por outra.
	 * 
	 * @see Criteria#startsWith(String, String)
	 */
	@Test
	public void testStartsWith() {
		Criteria c = Criteria.startsWith("nome", "fulano");
		assertThat(c.getOperator(), is(Operator.startsWith));
		assertThat(c.getAttribute(), is("nome"));
		assertThat(c.getConcreteAttribute(), is("o.nome"));
		assertThat(c.getValue().size(), is(equalTo(1)));
		assertThat((String) c.getValue().get(0), is("fulano"));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		assertThat(c.isRequired("nome"), is(true));
	}

	/**
	 * Teste para o construtor de critérios de verificação relativo a uma string ser terminada por outra.
	 * 
	 * @see Criteria#endsWith(String, String)
	 */
	@Test
	public void testEndsWith() {
		Criteria c = Criteria.endsWith("nome", "fulano");
		assertThat(c.getOperator(), is(Operator.endsWith));
		assertThat(c.getAttribute(), is("nome"));
		assertThat(c.getConcreteAttribute(), is("o.nome"));
		assertThat(c.getValue().size(), is(equalTo(1)));
		assertThat((String) c.getValue().get(0), is("fulano"));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		assertThat(c.isRequired("nome"), is(true));
	}

	/**
	 * Teste para o construtor de critérios de verificação relativo a um valor estar entre dois outros.
	 * 
	 * @see Criteria#between(String, Object, Object)
	 */
	@Test
	public void testBetween() {
		Criteria c = Criteria.between("nome" ,"cicrano", "fulano");
		assertThat(c.getOperator(), is(Operator.between));
		assertThat(c.getAttribute(), is("nome"));
		assertThat(c.getConcreteAttribute(), is("o.nome"));
		assertThat(c.getValue().size(), is(equalTo(2)));
		assertThat((String) c.getValue().get(0), is("cicrano"));
		assertThat((String) c.getValue().get(1), is("fulano"));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		assertThat(c.isRequired("nome"), is(true));
	}

	/**
	 * Teste para o construtor de critérios de verificação relativo a um valor estar contido em uma dada lista.
	 * 
	 * @see Criteria#in(String, Object[])
	 */
	@Test
	public void testIn() {
		Criteria c = Criteria.in("nome", new Object[]{"fulano", "cicrano", "beltrano"});
		assertThat(c.getOperator(), is(Operator.in));
		assertThat(c.getAttribute(), is("nome"));
		assertThat(c.getConcreteAttribute(), is("o.nome"));
		assertThat(c.getValue().size(), is(equalTo(3)));
		assertThat((String) c.getValue().get(0), is("fulano"));
		assertThat((String) c.getValue().get(1), is("cicrano"));
		assertThat((String) c.getValue().get(2), is("beltrano"));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		assertThat(c.isRequired("nome"), is(true));
	}

	/**
	 * Teste para o construtor de critérios de verificação em que se verifica se uma lista está vazia.
	 * 
	 * @see Criteria#empty(String)
	 */
	@Test
	public void testEmpty() {
		Criteria c = Criteria.empty("nomeAlternativoList");
		assertThat(c.getOperator(), is(Operator.empty));
		assertThat(c.getAttribute(), is("nomeAlternativoList"));
		assertThat(c.getConcreteAttribute(), is("o.nomeAlternativoList"));
		assertThat(c.getValue().size(), is(equalTo(0)));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		assertThat(c.isRequired("nomeAlternativoList"), is(true));
	}

	/**
	 * Teste para o construtor de critérios de verificação em que se verifica um valor está vazio.
	 * 
	 * @see Criteria#isNull(String)
	 */
	@Test
	public void testIsNull() {
		Criteria c = Criteria.isNull("nome");
		assertThat(c.getOperator(), is(Operator.isNull));
		assertThat(c.getAttribute(), is("nome"));
		assertThat(c.getConcreteAttribute(), is("o.nome"));
		assertThat(c.getValue().size(), is(equalTo(0)));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		assertThat(c.isRequired("nome"), is(true));
	}

	/**
	 * Teste para o construtor de critérios de negação de um outro critério.
	 * 
	 * @see Criteria#not(Criteria)
	 */
	@Test
	public void testNot() {
		Criteria internal = Criteria.equals("nome", "fulano");
		Criteria c = Criteria.not(internal);
		assertThat(c.getOperator(), is(Operator.not));
		assertThat(c.getAttribute(), is(nullValue()));
		assertThat(c.getConcreteAttribute(), is("o.null"));
		assertThat(c.getValue().size(), is(equalTo(1)));
		assertThat((Criteria) c.getValue().get(0), is(internal));
		assertThat(c.getParent(), nullValue());
		assertThat(((Criteria) c.getValue().get(0)).isChild(), is(true));
		assertThat(((Criteria) c.getValue().get(0)).getParent(), is(c));
	}

	/**
	 * Teste para o construtor de critérios de verificação em que se permite o atendimento a dois ou mais critérios, alternativamente.
	 * 
	 * @see Criteria#or(Criteria...)
	 */
	@Test
	public void testOr() {
		Criteria or1 = Criteria.equals("nome", "fulano");
		Criteria or2 = Criteria.notEquals("nome", "beltrano");
		Criteria c = Criteria.or(or1, or2);
		assertThat(c.getOperator(), is(Operator.or));
		assertThat(c.getAttribute(), is(nullValue()));
		assertThat(c.getConcreteAttribute(), is("o.null"));
		assertThat(c.getValue().size(), is(equalTo(2)));
		assertThat((Criteria) c.getValue().get(0), is(or1));
		assertThat((Criteria) c.getValue().get(1), is(or2));
		assertThat(c.getParent(), nullValue());
		assertThat(((Criteria) c.getValue().get(0)).isChild(), is(true));
		assertThat(((Criteria) c.getValue().get(0)).getParent(), is(c));
		assertThat(((Criteria) c.getValue().get(1)).isChild(), is(true));
		assertThat(((Criteria) c.getValue().get(1)).getParent(), is(c));
	}

	/**
	 * Teste para o construtor de critérios de verificação em que se exige que dois ou mais critérios sejam atendidos.
	 * 
	 * @see Criteria#and(Criteria...)
	 */
	@Test
	public void testAnd() {
		Criteria and1 = Criteria.equals("nome", "fulano");
		Criteria and2 = Criteria.notEquals("nome", "beltrano");
		Criteria c = Criteria.and(and1, and2);
		assertThat(c.getOperator(), is(Operator.and));
		assertThat(c.getAttribute(), is(nullValue()));
		assertThat(c.getConcreteAttribute(), is("o.null"));
		assertThat(c.getValue().size(), is(equalTo(2)));
		assertThat((Criteria) c.getValue().get(0), is(and1));
		assertThat((Criteria) c.getValue().get(1), is(and2));
		assertThat(c.getParent(), nullValue());
		assertThat(((Criteria) c.getValue().get(0)).isChild(), is(true));
		assertThat(((Criteria) c.getValue().get(0)).getParent(), is(c));
		assertThat(((Criteria) c.getValue().get(1)).isChild(), is(true));
		assertThat(((Criteria) c.getValue().get(1)).getParent(), is(c));
	}

	/**
	 * Teste para o método de cópia de um outro critério, assegurando-se o desacoplamento dos objetos.
	 * 
	 * @see Criteria#copy()
	 */
	@Test
	public void testCopy() {
		Criteria eq = Criteria.equals("nome", "fulano");
		Criteria orig = Criteria.not(eq);
		Criteria c = orig.copy();
		assertThat(c, is(not(orig)));
		assertThat(c.getOperator(), is(Operator.not));
		assertThat(c.getAttribute(), is(nullValue()));
		assertThat(c.getConcreteAttribute(), is("o.null"));
		assertThat(c.getValue().size(), is(equalTo(1)));
		assertThat(c.getParent(), nullValue());
		assertThat(c.isChild(), is(false));
		Criteria eqn = (Criteria) c.getValue().get(0);
		assertThat(eqn, is(not(eq)));
		assertThat(eqn.getAttribute(), is("nome"));
		assertThat(eqn.getConcreteAttribute(), is("o.nome"));
		assertThat(eqn.getValue().size(), is(equalTo(1)));
		assertThat((String) eqn.getValue().get(0), is("fulano"));
		assertThat(eqn.isChild(), is(true));
		assertThat(eqn.getParent(), is(not(orig)));
		assertThat(eqn.getParent(), is(c));
	}

}
