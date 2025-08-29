package br.com.infox.test.cliente.home;

import java.util.Calendar;

import junit.framework.Assert;

import org.junit.Test;

import br.com.infox.cliente.home.ProcessoAudienciaHome;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;

public class ProcessoAudienciaHomeTest {

	private ProcessoAudienciaHome processoAudienciaHome = new ProcessoAudienciaHome();

	@Test
	public void diaSemana() {
		Assert.assertEquals("Domingo", processoAudienciaHome.diaSemana(1));
		Assert.assertEquals("Segunda-feira", processoAudienciaHome.diaSemana(2));
		Assert.assertEquals("Terça-feira", processoAudienciaHome.diaSemana(3));
		Assert.assertEquals("Quarta-feira", processoAudienciaHome.diaSemana(4));
		Assert.assertEquals("Quinta-feira", processoAudienciaHome.diaSemana(5));
		Assert.assertEquals("Sexta-feira", processoAudienciaHome.diaSemana(6));
		Assert.assertEquals("Sábado", processoAudienciaHome.diaSemana(7));
		Assert.assertNull(processoAudienciaHome.diaSemana(10));
	}

	@Test
	public void getStatusAudiencia() {
		String marcada = "M";
		String retornoVazio = "teste";
		Assert.assertEquals("Designada", processoAudienciaHome.getStatusAudiencia(marcada));
		Assert.assertEquals("---", processoAudienciaHome.getStatusAudiencia(retornoVazio));
	}

	@Test
	public void getDataAudiencia() {
		ProcessoAudiencia pa1 = new ProcessoAudiencia();
		ProcessoAudiencia pa2 = new ProcessoAudiencia();

		Calendar dataInicial = Calendar.getInstance();
		dataInicial.set(2010, 2, 2);

		Calendar dataCancelamento = Calendar.getInstance();
		dataCancelamento.set(2011, 2, 2);

		pa1.setDtInicio(dataInicial.getTime());
		pa1.setDtCancelamento(dataCancelamento.getTime());

		pa2.setDtInicio(dataInicial.getTime());

		Assert.assertEquals(dataCancelamento.getTime(), processoAudienciaHome.getDataAudiencia(pa1));
		Assert.assertEquals(dataInicial.getTime(), processoAudienciaHome.getDataAudiencia(pa2));

	}
}