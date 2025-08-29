package br.com.infox.test.cliente.home;

import junit.framework.Assert;

import org.junit.Test;

import br.com.infox.cliente.bean.ConsultaProcesso;
import br.com.infox.cliente.home.ConsultaProcessoHome;

public class ConsultaProcessoHomeTest {

	private ConsultaProcessoHome consultaProcessoHome = new ConsultaProcessoHome();

	@Test
	public void verificarCamposTest() {
		ConsultaProcesso consultaProcesso = new ConsultaProcesso();
		consultaProcesso.setAno(2011);
		consultaProcesso.setNomeParte("Witan");
		consultaProcessoHome.setInstance(consultaProcesso);
		Assert.assertTrue(consultaProcessoHome.verificarCampos());
		consultaProcesso.setAno(null);
		consultaProcesso.setNomeParte(null);
		consultaProcessoHome.setInstance(consultaProcesso);
		Assert.assertFalse(consultaProcessoHome.verificarCampos());

	}

}