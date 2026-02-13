package br.com.infox.test.ibpm.entity;

import junit.framework.Assert;

import org.junit.Test;

import br.jus.pje.nucleo.entidades.Pesquisa;

public class PesquisaTest {

	private static final String NOME_PESQUISA = "Pesquisa 1";

	@Test
	public void teste() {
		Pesquisa p = new Pesquisa();
		p.setNome(NOME_PESQUISA);
		Assert.assertEquals(NOME_PESQUISA, p.toString());
	}

}
