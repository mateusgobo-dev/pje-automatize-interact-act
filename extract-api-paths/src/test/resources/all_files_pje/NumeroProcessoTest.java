package br.com.infox.teste.cliente.component;

import junit.framework.Assert;

import org.junit.Test;

import br.com.infox.cliente.component.NumeroProcesso;

public class NumeroProcessoTest {

	@Test
	public void setNumeroProcessoTest() {
		NumeroProcesso numeroProcesso = new NumeroProcesso();
		String numeroProcessoStr = "0000005-94.2010.4.05.0000";
		numeroProcesso.setNumeroProcesso(numeroProcessoStr);

		Assert.assertEquals(0000005, numeroProcesso.getNumeroSequencia().intValue());
		Assert.assertEquals(94, numeroProcesso.getNumeroDigitoVerificador().intValue());
		Assert.assertEquals(2010, numeroProcesso.getAno().intValue());
		Assert.assertEquals(405, numeroProcesso.getNumeroOrgaoJustica().intValue());
		Assert.assertEquals(0000, numeroProcesso.getNumeroOrigem().intValue());

		numeroProcessoStr = "_______-94.____.4.05.____";
		numeroProcesso.setNumeroProcesso(numeroProcessoStr);

		Assert.assertNull(numeroProcesso.getNumeroSequencia());
		Assert.assertEquals(94, numeroProcesso.getNumeroDigitoVerificador().intValue());
		Assert.assertNull(numeroProcesso.getAno());
		Assert.assertEquals(405, numeroProcesso.getNumeroOrgaoJustica().intValue());
		Assert.assertNull(numeroProcesso.getNumeroOrigem());
	}

	@Test
	public void setNumeroProcessoParticionadoTest() {
		Integer numeroSequencia = 0000005;
		Integer numeroDigitoVerificador = 94;
		Integer ano = 2010;
		Integer numeroOrgaoJustica = 405;
		Integer numeroOrigem = 0000;
		NumeroProcesso numeroProcesso = new NumeroProcesso();
		numeroProcesso.setNumeroProcesso(numeroSequencia, ano, numeroDigitoVerificador, numeroOrgaoJustica,
				numeroOrigem);

		Assert.assertEquals(0000005, numeroProcesso.getNumeroSequencia().intValue());
		Assert.assertEquals(94, numeroProcesso.getNumeroDigitoVerificador().intValue());
		Assert.assertEquals(2010, numeroProcesso.getAno().intValue());
		Assert.assertEquals(405, numeroProcesso.getNumeroOrgaoJustica().intValue());
		Assert.assertEquals(0000, numeroProcesso.getNumeroOrigem().intValue());
	}

}