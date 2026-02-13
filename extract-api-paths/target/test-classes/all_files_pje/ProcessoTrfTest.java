package br.com.infox.test.cliente.entity;

import org.junit.Assert;
import org.junit.Test;

import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public class ProcessoTrfTest {

	@Test
	public void getNumeroProcessoTest() {
		ProcessoTrf processoTrf = new ProcessoTrf();
		String numeroProcesso = "0000001-57.2010.4.05.0000";
		processoTrf.setProcesso(new Processo());
		processoTrf.getProcesso().setNumeroProcesso(numeroProcesso);
		Assert.assertEquals(numeroProcesso, processoTrf.getNumeroProcesso());

		processoTrf = new ProcessoTrf();
		processoTrf.setNumeroSequencia(0000001);
		processoTrf.setNumeroDigitoVerificador(57);
		processoTrf.setAno(2010);
		processoTrf.setNumeroOrgaoJustica(405);
		processoTrf.setNumeroOrigem(0000);

		Assert.assertEquals(numeroProcesso, processoTrf.getNumeroProcesso());

		Assert.assertEquals(numeroProcesso, processoTrf.getNumeroProcesso());
	}

	@Test
	public void getVlCausaTest() {
		ProcessoTrf processoTrf = new ProcessoTrf();
		processoTrf.setValorCausa(1024.00);

		Assert.assertEquals("1.024,00", processoTrf.getVlCausa());

		Assert.assertTrue("1.000,00" != processoTrf.getVlCausa());
	}

}
