package br.com.infox.test.cliente.home;

import junit.framework.Assert;

import org.junit.Test;

import br.com.infox.cliente.home.ProcessoExpedienteCentralMandadoHome;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;

public class ProcessoExpedienteCentralMandadoHomeTest {

	ProcessoExpedienteCentralMandadoHome pecmhListaVazia = new ProcessoExpedienteCentralMandadoHome();
	ProcessoExpedienteCentralMandadoHome pecmhListaPreenchida = new ProcessoExpedienteCentralMandadoHome();

	@Test
	public void verificaDistribuirExpediente() {
		ProcessoExpedienteCentralMandado pecm = new ProcessoExpedienteCentralMandado();
		pecm.setIdProcessoExpedienteCentralMandado(1);
		pecmhListaPreenchida.getListDistribuir().add(pecm);
		Assert.assertTrue(pecmhListaPreenchida.verificaDistribuirExpediente());
		Assert.assertFalse(pecmhListaVazia.verificaDistribuirExpediente());
	}
}
