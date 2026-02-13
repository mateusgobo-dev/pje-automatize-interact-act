package br.com.infox.test.client.type;

import junit.framework.Assert;

import org.junit.Test;

import br.jus.pje.nucleo.enums.RevisadoEnum;

public class RevisadoEnumTest {

	@Test
	public void getLabelRealizacaoTest() {
		String label = RevisadoEnum.N.getLabel();

		Assert.assertEquals("Processo não Revisado", label);
	}

	@Test
	public void getLabelSolicitacaoTest() {
		String label = RevisadoEnum.S.getLabel();

		Assert.assertEquals("Processo Revisado", label);
	}

}
