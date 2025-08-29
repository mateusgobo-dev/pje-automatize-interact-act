package br.com.infox.test.cliente.home;

import junit.framework.Assert;

import org.junit.Test;

import br.com.infox.cliente.home.SessaoProcessoDocumentoVotoHome;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;

public class SessaoProcessoDocumentoVotoHomeTest {

	private SessaoProcessoDocumentoVotoHome spdvHome = new SessaoProcessoDocumentoVotoHome();

	@Test
	public void verificaAssinaturaMagiTest() {
		ProcessoDocumentoBin pdb = new ProcessoDocumentoBin();
		pdb.setCertChain("certchain");
		pdb.setSignature("signature");

		ProcessoDocumento pd = new ProcessoDocumento();
		pd.setProcessoDocumentoBin(pdb);

		SessaoProcessoDocumentoVoto spdv = new SessaoProcessoDocumentoVoto();
		spdv.setProcessoDocumento(pd);

		Assert.assertEquals(Boolean.TRUE, spdvHome.verificaAssinaturaMagi(spdv));
	}
}