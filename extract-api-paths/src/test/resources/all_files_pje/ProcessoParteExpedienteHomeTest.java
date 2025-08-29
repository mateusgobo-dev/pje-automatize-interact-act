package br.com.infox.test.cliente.home;

import java.util.Calendar;

import junit.framework.Assert;

import org.junit.Test;

import br.com.infox.cliente.home.ProcessoParteExpedienteHome;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;

public class ProcessoParteExpedienteHomeTest {

	ProcessoParteExpedienteHome processoParteExpedienteHome = new ProcessoParteExpedienteHome();

	@Test
	public void verificarPrazosNulos() {
		ProcessoParte ppListaPreenchida = new ProcessoParte();
		ppListaPreenchida.setIdProcessoParte(1);
		ppListaPreenchida.setPrazoLegal(2);
		ppListaPreenchida.setCheckado(Boolean.TRUE);

		ProcessoParte ppListaVazia = new ProcessoParte();

		Assert.assertFalse(processoParteExpedienteHome.verificarPrazosNulos(ppListaPreenchida));
		Assert.assertTrue(processoParteExpedienteHome.verificarPrazosNulos(ppListaVazia));
		Assert.assertEquals(ppListaPreenchida.getCheckado().booleanValue(),
				processoParteExpedienteHome.verificarPrazosNulos(ppListaVazia));
	}

	@Test
	public void confirmacaoExpediente() {
		ProcessoParteExpediente ppeCienciaNaoNulo = new ProcessoParteExpediente();
		ppeCienciaNaoNulo.setCienciaSistema(true);

		ProcessoParteExpediente ppeCienciaNulo = new ProcessoParteExpediente();

		ProcessoParteExpediente ppeCienciaNulo1 = new ProcessoParteExpediente();
		Pessoa pessoaCiencia = new Pessoa();
		pessoaCiencia.setIdUsuario(2);
		pessoaCiencia.setNome("João");
		ppeCienciaNulo1.setCienciaSistema(false);
		ppeCienciaNulo1.setPessoaCiencia(pessoaCiencia);

		ProcessoParteExpediente ppeCienciaNulo2 = new ProcessoParteExpediente();
		ppeCienciaNulo2.setCienciaSistema(false);
		ppeCienciaNulo2.setNomePessoaCiencia("Maria");

		Assert.assertEquals("--", processoParteExpedienteHome.confirmacaoExpediente(ppeCienciaNulo));
		Assert.assertEquals("Sistema", processoParteExpedienteHome.confirmacaoExpediente(ppeCienciaNaoNulo));
		Assert.assertEquals("João", processoParteExpedienteHome.confirmacaoExpediente(ppeCienciaNulo1));
		Assert.assertEquals("Maria", processoParteExpedienteHome.confirmacaoExpediente(ppeCienciaNulo2));
	}

//	@Test
	public void calcularPrazoProcessualTest() {
		ProcessoParteExpedienteHome ppeh = new ProcessoParteExpedienteHome();
		final int acrescimoData = 10;
		Calendar data = Calendar.getInstance();
		data.add(Calendar.DAY_OF_MONTH, acrescimoData);

		Assert.assertEquals(data.getTime(), ppeh.calcularPrazoProcessual());
	}

}
