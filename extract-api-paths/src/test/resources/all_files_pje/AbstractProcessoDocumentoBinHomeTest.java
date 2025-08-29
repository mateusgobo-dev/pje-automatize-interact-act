package br.com.infox.teste.ibpm.home;

import org.junit.Test;

import br.com.infox.ibpm.home.AbstractProcessoDocumentoBinHome;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;

@SuppressWarnings("serial")
public class AbstractProcessoDocumentoBinHomeTest {

	@Test
	public void isModeloVazioTest() {
		AbstractProcessoDocumentoBinHome<ProcessoDocumentoBin> absProcDocBinHome = new AbstractProcessoDocumentoBinHome<ProcessoDocumentoBin>() {
		};
		ProcessoDocumentoBin bin = new ProcessoDocumentoBin();
		absProcDocBinHome.setIgnoraConteudoDocumento(true);

		bin.setModeloDocumento("<teste \\>\n \r &nbsp;");
		// Assert.assertTrue("O Modelo não está vazio.",
		// ProcessoDocumentoBinHome.isModeloVazio(bin));

		bin.setModeloDocumento("<teste \\>\n teste \r &nbsp;");
		// Assert.assertFalse("O Modelo está vazio.",
		// ProcessoDocumentoBinHome.isModeloVazio(bin));
	}

}
