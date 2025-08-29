package br.com.infox.test.client.type;

import junit.framework.Assert;

import org.junit.Test;

import br.jus.pje.nucleo.enums.PagamentoEnum;

public class PagamentoEnumTest {

	@Test
	public void getLabelRealizacaoTest() {
		String label = PagamentoEnum.R.getLabel();

		Assert.assertEquals("Realização", label);
	}

	@Test
	public void getLabelSolicitacaoTest() {
		String label = PagamentoEnum.S.getLabel();

		Assert.assertEquals("Solicitação", label);
	}

}
