package br.com.infox.test.cliente.home;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;

import br.com.infox.cliente.home.ProcessoParteHome;
import br.jus.pje.nucleo.entidades.ProcessoParte;

public class ProcessoParteHomeTest {

	ProcessoParteHome processoParteHome = new ProcessoParteHome();

	@Test
	public void pesquisa() {
		ProcessoParte pp = new ProcessoParte();
		ProcessoParte pp1 = new ProcessoParte();
		List<ProcessoParte> lista = new ArrayList<ProcessoParte>();
		pp1.setIdProcessoParte(1);
		lista.add(pp1);
		pp1.setIdProcessoParte(2);
		lista.add(pp1);
		pp1.setIdProcessoParte(3);
		lista.add(pp1);
		Assert.assertEquals(false, processoParteHome.pesquisa(pp, lista));
		Assert.assertEquals(true, processoParteHome.pesquisa(pp1, lista));
	}
}
