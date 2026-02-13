package br.com.infox.test.cliente.home;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

import br.com.infox.cliente.home.SessaoHome;
import br.jus.pje.nucleo.entidades.SalaHorario;
import br.jus.pje.nucleo.entidades.Sessao;

public class SessaoHomeTeste {

	private SessaoHome sessaoHome = new SessaoHome();

	@Test
	public void marcarSalaTeste() {
		SalaHorario salaHorario = new SalaHorario();
		salaHorario.setSelecionado(Boolean.TRUE);

		sessaoHome.marcarSala(salaHorario);
		Assert.assertEquals(1, sessaoHome.getSalaHorarioSet().size());
	}

	@Test
	public void dataSessaoTeste() {
		Calendar dataD = Calendar.getInstance();
		dataD.set(2000, 0, 1);
		Sessao sessao = new Sessao();
		sessao.setDataSessao(dataD.getTime());

		Assert.assertEquals("01/01/2000", sessaoHome.dataSessao(sessao));
	}

	@Test
	public void getStatusTeste() {
		Sessao s = new Sessao();

		Assert.assertEquals("Em Andamento", sessaoHome.getStatus(s));
		s.setDataAberturaSessao(new Date());
		Assert.assertEquals("Aberta", sessaoHome.getStatus(s));
		s.setDataRealizacaoSessao(new Date());
		Assert.assertEquals("Realizada", sessaoHome.getStatus(s));
		s.setDataRegistroEvento(new Date());
		Assert.assertEquals("Registrado Evento", sessaoHome.getStatus(s));
		s.setDataFechamentoSessao(new Date());
		Assert.assertEquals("Finalizada", sessaoHome.getStatus(s));
	}
}