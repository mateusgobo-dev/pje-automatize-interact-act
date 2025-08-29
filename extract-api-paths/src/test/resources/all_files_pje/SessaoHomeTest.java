package br.com.infox.test.cliente.home;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

import br.com.infox.cliente.home.SessaoHome;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.DiaSemana;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SalaHorario;
import br.jus.pje.nucleo.entidades.Sessao;

public class SessaoHomeTest {

	private SessaoHome sessaoHome = new SessaoHome();

	@Test
	public void marcarSalaTeste() {
		SalaHorario salaHorario = new SalaHorario();
		salaHorario.setSelecionado(Boolean.TRUE);

		sessaoHome.marcarSala(salaHorario);
		Assert.assertEquals(1, sessaoHome.getSalaHorarioSet().size());
	}

	@Test
	public void verificaDiaUtilTest() {
		Date data = new Date(2011, 06, 06);
		Assert.assertEquals(true, sessaoHome.verificaDiaUtil(data));
	}

	@Test
	public void exibeMsgQtdSecCadastradasTest() {
		Assert.assertEquals("Foram cadastradas 9 no periodo informado.", sessaoHome.exibeMsgQtdSecCadastradas(9));
		Assert.assertEquals("Não houve ocorrência(s) para o período informado!",
				sessaoHome.exibeMsgQtdSecCadastradas(0));
	}

	@Test
	public void verificaPossibilidadeFechamentoPautaTest() {
		SessaoHome sh = new SessaoHome();
		sh.setDataFechamentoPauta(new Date(2012, 06, 06));
		sh.setDataInicial(new Date(2011, 06, 06));
		Assert.assertEquals(false, sh.verificaPossibilidadeFechamentoPauta());
	}

	@Test
	public void tratarDataTest() {
		Date data = new Date(2011, 06, 06, 0, 0, 0);
		Assert.assertEquals(data, sessaoHome.tratarData(new Date(2011, 06, 06, 9, 9, 9)));
	}

	@Test
	public void getProximaSessaoTest() {
		DiaSemana diaSem = new DiaSemana();
		diaSem.setDiaSemana("Segunda");
		SalaHorario sh = new SalaHorario();
		sh.setDiaSemana(diaSem);

		Calendar cDataInicial = Calendar.getInstance();
		// data criada é uma terça feira -> obs: janeiro = mês 0
		cDataInicial.set(2011, 05, 14);
		Date dtResultado = sessaoHome.getProximaSessao(sh, cDataInicial.getTime());
		Calendar resultadoData = Calendar.getInstance();
		resultadoData.setTime(dtResultado);
		Assert.assertEquals(20, resultadoData.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	public void dataSessao() {
		Calendar dataD = Calendar.getInstance();
		dataD.set(2000, 0, 1);
		Sessao sessao = new Sessao();
		sessao.setDataSessao(dataD.getTime());

		Assert.assertEquals("01/01/2000", sessaoHome.dataSessao(sessao));
	}

	@Test
	public void habilitaCombo() {
		sessaoHome.setHabilitaCombo(Boolean.FALSE);

		Assert.assertEquals(Boolean.FALSE, sessaoHome.getHabilitaCombo());
	}

	@Test
	public void exigePauta() {
		ProcessoTrf processoTrf = new ProcessoTrf();
		ClasseJudicial cj = new ClasseJudicial();

		cj.setClasseJudicial("classeJudicial");
		cj.setPauta(true);

		processoTrf.setIdProcessoTrf(1);
		processoTrf.setClasseJudicial(cj);

		sessaoHome.setProcessoTrf(processoTrf);
		Assert.assertEquals(true, sessaoHome.exigePauta());
	}
}