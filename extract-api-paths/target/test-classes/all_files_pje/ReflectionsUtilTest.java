package br.com.itx.test.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import br.com.itx.util.ReflectionsUtil;
import br.jus.pje.nucleo.entidades.Pesquisa;

public class ReflectionsUtilTest {

	@Test
	public void getFieldInvalidTest() throws IllegalArgumentException, IllegalAccessException {
		Pesquisa p = new Pesquisa();

		Field field = ReflectionsUtil.getField(p, "invalidField");
		Assert.assertNull(field);
	}

	@Test
	public void getValueTest() {
		Pesquisa p = new Pesquisa();
		p.setNome("nomePesquisa");

		Assert.assertEquals(p.getNome(), ReflectionsUtil.getValue(p, "nome"));
	}

	@Test
	public void setValueTest() {
		Pesquisa p = new Pesquisa();

		String valorColunaOrdenacao = "coluna";
		ReflectionsUtil.setValue(p, "colunaOrdenacao", valorColunaOrdenacao);

		Assert.assertEquals(valorColunaOrdenacao, p.getColunaOrdenacao());
	}

	@Test
	public void getFieldsTest() {
		List<String> nameFields = new ArrayList<String>();
		for (Field field : ReflectionsUtil.getFields(Pesquisa.class)) {
			nameFields.add(field.getName());
		}

		Assert.assertTrue(nameFields.contains("nome"));
		Assert.assertTrue(nameFields.contains("descricao"));
		Assert.assertTrue(nameFields.contains("colunaOrdenacao"));
	}

}
