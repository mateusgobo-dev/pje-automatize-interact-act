package br.com.infox.test.DAO;

import junit.framework.Assert;

import org.junit.Test;

import br.com.infox.DAO.SearchCriteria;
import br.com.infox.DAO.SearchField;

public class SearchFieldTest {

	@Test
	public void getExpressionTest() {
		// Sem passar a expressão no construtor
		SearchField searchField = new SearchField("entidadeList", "atributo", SearchCriteria.igual);
		Assert.assertEquals("o.atributo = #{entidadeList.entity.atributo}", searchField.getExpression());

		String expressao = "o.novoAtributo = #{entidadeHome.novoAtributo}";
		searchField.setExpression(expressao);
		Assert.assertEquals(expressao, searchField.getExpression());

		// Passando a expressão no construtor
		expressao = "o.idObjeto = #{objetoHome.idObjeto}";
		searchField = new SearchField("entidadeList", "atributo", SearchCriteria.igual, expressao);
		Assert.assertEquals(expressao, searchField.getExpression());
	}

}
