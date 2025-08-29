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

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import br.jus.pje.search.Criteria;
import br.jus.pje.search.Operator;
import br.jus.pje.search.Search;
import br.jus.pje.search.Search.JoinAlias;

/**
 * Testes unitários do componente de consulta.
 *  
 * @author cristof
 *
 */
public class SearchTest {
	
	//-------- Entidades básicas para os testes ---------------//
	public class Entity1{
		private String prop;
		private String prop_;
		private List<Element1> elements;
		public String getProp() {
			return prop;
		}
		public void setProp(String prop) {
			this.prop = prop;
		}
		public String getProp_() {
			return prop_;
		}
		public void setProp_(String prop_) {
			this.prop_ = prop_;
		}
		public List<Element1> getElements() {
			return elements;
		}
		public void setElements(List<Element1> elements) {
			this.elements = elements;
		}
	}
	
	public class Element1{
		private String prop1;
		private Element2 prop2;
		public String getProp1() {
			return prop1;
		}
		public void setProp1(String prop1) {
			this.prop1 = prop1;
		}
		public Element2 getProp2() {
			return prop2;
		}
		public void setProp2(Element2 prop2) {
			this.prop2 = prop2;
		}
	}
	
	public class Element2{
		private Date prop1;
		private Set<Element3> elements;
		public Date getProp1() {
			return prop1;
		}
		public void setProp1(Date prop1) {
			this.prop1 = prop1;
		}
		public Set<Element3> getElements() {
			return elements;
		}
		public void setElements(Set<Element3> elements) {
			this.elements = elements;
		}
	}
	
	public class Element3{
		private String prop1;
		private String prop2;
		public String getProp1() {
			return prop1;
		}
		public void setProp1(String prop1) {
			this.prop1 = prop1;
		}
		public String getProp2() {
			return prop2;
		}
		public void setProp2(String prop2) {
			this.prop2 = prop2;
		}
	}
	//-------- Fim das entidades básicas para os testes ---------------//
	
	/**
	 * Cria um {@link Search} simples para os testes iniciais.
	 * 
	 * @return o search para o teste
	 * @throws NoSuchFieldException
	 */
	private Search getSimpleSearch() throws NoSuchFieldException{
		String path = "prop";
		Search search = new Search(Entity1.class);
		search.addCriteria(Criteria.startsWith(path, "value"));
		return search;
	}
	
	/**
	 * Testa as características padronizadas de um search.
	 *  
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testDefaults() throws NoSuchFieldException{
		Search s = getSimpleSearch();
		assertThat(s.getFirst(), is(0));
		assertThat(s.getMax(), is(nullValue()));
		assertThat(s.getRetrieveField(), is(nullValue()));
		assertThat(s.isCount(), is(false));
		assertThat(s.isDistinct(), is(false));
	}
	
	/**
	 * Testa a modificação do registro inicial a ser recuperada.
	 * 
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testModifiedFirst() throws NoSuchFieldException{
		Search s = getSimpleSearch();
		s.setFirst(10);
		assertThat(s.getFirst(), is(10));
	}
	
	/**
	 * Testa a modificação do número de registros a serem recuperados.
	 * 
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testModifiedMax() throws NoSuchFieldException{
		Search s = getSimpleSearch();
		s.setMax(10);
		assertThat(s.getMax(), is(10));
	}
	
	/**
	 * Testa a modificação do campo a ser recuperado.
	 * 
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testModifiedRetreive() throws NoSuchFieldException{
		Search s = getSimpleSearch();
		s.setRetrieveField("prop");
		assertThat(s.getRetrieveField(), is("prop"));
	}
	
	/**
	 * Testa a modificação da marca relativa à resposta ser um conjunto ou uma lista.
	 * 
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testModifiedDistinct() throws NoSuchFieldException{
		Search s = getSimpleSearch();
		s.setDistinct(true);
		assertThat(s.isDistinct(), is(true));
	}
	
	/**
	 * Testa a modificação da marca indicativa de que se pretende recuperar apenas a contagem
	 * de elementos.
	 * 
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testModifiedCount() throws NoSuchFieldException{
		Search s = getSimpleSearch();
		s.setCount(true);
		assertThat(s.isCount(), is(true));
	}
	
	/**
	 * Verifica se o construtor dispara a exceção de campo inexistente.
	 * 
	 * @throws NoSuchFieldException
	 */
	@Test(expected=NoSuchFieldException.class)
	public void testNSE() throws NoSuchFieldException{
		Search s = getSimpleSearch();
		s.addCriteria(Criteria.equals("nonExistentField", "value"));
	}
	
	/**
	 * Testa as características básicas de um search em que se utiliza um campo simples por igualdade.
	 * 
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testSimpleCriteria() throws NoSuchFieldException{
		Search s = getSimpleSearch();
		assertThat(s.getJoins().size(), is(equalTo(0)));
		assertThat(s.getCriterias().size(), is(equalTo(1)));
		assertThat(s.getCriterias().containsKey("AND0"), is(true));
		assertThat(s.getCriterias().get("AND0").getConcreteAttribute(), is("o.prop"));
		assertThat(s.getCriterias().get("AND0").getValue().size(), is(equalTo(1)));
		assertThat(s.getCriterias().get("AND0").isRequired("prop"), is(true));
		assertThat((String) s.getCriterias().get("AND0").getValue().get(0), is("value"));
	}
	
	/**
	 * Testa as características básicas de um search em que dois critérios obrigatórios são combinados.
	 * 
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testCombination() throws NoSuchFieldException{
		Search s = getSimpleSearch();
		s.addCriteria(Criteria.contains("prop_", "value_"));
		assertThat(s.getJoins().size(), is(equalTo(0)));
		assertThat(s.getCriterias().size(), is(equalTo(2)));
		assertThat(s.getCriterias().containsKey("AND0"), is(true));
		assertThat(s.getCriterias().get("AND0").getConcreteAttribute(), is("o.prop"));
		assertThat(s.getCriterias().get("AND0").getValue().size(), is(equalTo(1)));
		assertThat(s.getCriterias().get("AND0").isRequired("prop"), is(true));
		assertThat((String) s.getCriterias().get("AND0").getValue().get(0), is("value"));
		assertThat(s.getCriterias().containsKey("AND1"), is(true));
		assertThat(s.getCriterias().get("AND1").getConcreteAttribute(), is("o.prop_"));
		assertThat(s.getCriterias().get("AND1").getValue().size(), is(equalTo(1)));
		assertThat(s.getCriterias().get("AND1").isRequired("prop_"), is(true));
		assertThat((String) s.getCriterias().get("AND1").getValue().get(0), is("value_"));
	}
	
	/**
	 * Testa as características básicas de um search em que dois critérios alternativos são combinados.
	 * 
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testOr() throws NoSuchFieldException {
		Search s = new Search(Entity1.class);
		s.addCriteria(Criteria.or(
				Criteria.equals("prop", "value"),
				Criteria.equals("prop_", "value_")	));
		assertThat(s.getJoins().size(), is(equalTo(0)));
		assertThat(s.getCriterias().size(), is(equalTo(3)));
		assertThat(s.getCriterias().get("AND0").getConcreteAttribute(), is("o.prop"));
		assertThat(s.getCriterias().get("AND0").getValue().size(), is(equalTo(1)));
		assertThat(s.getCriterias().get("AND0").isRequired("prop"), is(true));
		assertThat((String) s.getCriterias().get("AND0").getValue().get(0), is("value"));
		assertThat(((Criteria) s.getCriterias().get("AND0")).isChild(), is(true));
		assertThat(s.getCriterias().get("AND1").getConcreteAttribute(), is("o.prop_"));
		assertThat(s.getCriterias().get("AND1").getValue().size(), is(equalTo(1)));
		assertThat(s.getCriterias().get("AND1").isRequired("prop_"), is(true));
		assertThat((String) s.getCriterias().get("AND1").getValue().get(0), is("value_"));
		assertThat(((Criteria) s.getCriterias().get("AND1")).isChild(), is(true));
		assertThat(s.getCriterias().containsKey("OR2"), is(true));
		assertThat(s.getCriterias().get("OR2").getConcreteAttribute(), is("o.null"));
		assertThat(s.getCriterias().get("OR2").getValue().size(), is(equalTo(2)));
	}
	
	/**
	 * Testa as características básicas de um search em que um critério é negado.
	 * 
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testNot() throws NoSuchFieldException {
		Search s = new Search(Entity1.class);
		s.addCriteria(Criteria.not(Criteria.equals("prop", "value")	));
		assertThat(s.getJoins().size(), is(equalTo(0)));
		assertThat(s.getCriterias().size(), is(equalTo(2)));
		assertThat(s.getCriterias().get("AND0").getConcreteAttribute(), is("o.prop"));
		assertThat(s.getCriterias().get("AND0").getValue().size(), is(equalTo(1)));
		assertThat(s.getCriterias().get("AND0").isRequired("prop"), is(true));
		assertThat((String) s.getCriterias().get("AND0").getValue().get(0), is("value"));
		assertThat(((Criteria) s.getCriterias().get("AND0")).getParent().getOperator(), is(Operator.not));
		assertThat(s.getCriterias().get("AND1").getConcreteAttribute(), is("o.null"));
		assertThat(s.getCriterias().get("AND1").getValue().size(), is(equalTo(1)));
		assertThat(((Criteria) s.getCriterias().get("AND1").getValue().get(0)).getOperator(), is(Operator.equals));
	}
	
	@Test
	public void testEqualsPath() throws NoSuchFieldException{
		Search s = new Search(Entity1.class);
		s.addCriteria(Criteria.equals(Criteria.path("prop"), Criteria.path("elements.prop1")));
		assertThat(s.getJoins().size(), is(equalTo(1)));
		assertThat(s.getCriterias().size(), is(equalTo(3)));
		
	}
	
	/**
	 * Testa as características de um search em que há a navegação por propriedades do JavaBean original.
	 * 
	 * @throws NoSuchFieldException
	 */
	@Test
	public void testJoins() throws NoSuchFieldException{
		String path = "elements.prop2.elements.prop2";
		Search search = new Search(Entity1.class);
		search.addCriteria(Criteria.equals(path, "value"));
		Map<String, JoinAlias> joins = search.getJoins();
		assertThat(search.getCriterias().size(), is(equalTo(1)));
		assertThat(search.getCriterias().containsKey("AND0"), is(true));
		assertThat(search.getCriterias().containsKey("AND1"), is(false));
		assertThat(search.getCriterias().containsKey("OR1"), is(false));
		assertThat(joins.size(), is(equalTo(2)));
		assertThat(joins.keySet().contains("o.elements"), is(true));
		assertThat(joins.keySet().contains("o.elements.prop2"), is(false));
		assertThat(joins.keySet().contains("o.elements.prop2.elements"), is(true));
		assertThat(joins.keySet().contains("o.elements.prop2.elements.prop2"), is(false));
		assertThat(joins.get("o.elements.prop2"), is(nullValue()));
		assertThat(joins.get("o.elements.prop2.elements").toString(), is("p0.prop2.elements"));
	}
	
	@Test
	public void testCopy() throws NoSuchFieldException{
		Search search = new Search(Entity1.class);
		search.addCriteria(Criteria.or(
				Criteria.equals("prop", "value"),
				Criteria.equals("prop_", "value_")	));
		Search s = search.copy();
		assertThat(s, is(not(search)));
		assertThat(s.getJoins().size(), is(equalTo(0)));
		assertThat(s.getCriterias().size(), is(equalTo(3)));
		assertThat(s.getCriterias().get("AND0").getConcreteAttribute(), is("o.prop"));
		assertThat(s.getCriterias().get("AND0").getValue().size(), is(equalTo(1)));
		assertThat(s.getCriterias().get("AND0").isRequired("prop"), is(true));
		assertThat((String) s.getCriterias().get("AND0").getValue().get(0), is("value"));
		assertThat(((Criteria) s.getCriterias().get("AND0")).isChild(), is(true));
		assertThat(s.getCriterias().get("AND1").getConcreteAttribute(), is("o.prop_"));
		assertThat(s.getCriterias().get("AND1").getValue().size(), is(equalTo(1)));
		assertThat(s.getCriterias().get("AND1").isRequired("prop_"), is(true));
		assertThat((String) s.getCriterias().get("AND1").getValue().get(0), is("value_"));
		assertThat(((Criteria) s.getCriterias().get("AND1")).isChild(), is(true));
		assertThat(s.getCriterias().containsKey("OR2"), is(true));
		assertThat(s.getCriterias().get("OR2").getConcreteAttribute(), is("o.null"));
		assertThat(s.getCriterias().get("OR2").getValue().size(), is(equalTo(2)));
		assertThat(s.getCriterias().get("AND0"), is(not(search.getCriterias().get("AND0"))));
		assertThat(s.getCriterias().get("AND1"), is(not(search.getCriterias().get("AND1"))));
		assertThat(s.getCriterias().get("OR2"), is(not(search.getCriterias().get("OR2"))));
	}
	
}
