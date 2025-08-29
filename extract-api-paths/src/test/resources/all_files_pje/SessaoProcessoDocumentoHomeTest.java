package br.com.infox.test.cliente.home;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import br.com.infox.cliente.home.SessaoProcessoDocumentoHome;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;

public class SessaoProcessoDocumentoHomeTest {

	private SessaoProcessoDocumentoHome spdHome = new SessaoProcessoDocumentoHome();

	@Test
	public void verificaRelatorioAssinadoTest() {
		ProcessoDocumentoBin bin = new ProcessoDocumentoBin();
		bin.setCertChain("certchain");
		bin.setSignature("signature");

		ProcessoDocumento pd = new ProcessoDocumento();
		pd.setProcessoDocumentoBin(bin);

		SessaoProcessoDocumento spd = new SessaoProcessoDocumento();
		spd.setProcessoDocumento(pd);

		Assert.assertEquals(true, spdHome.verificaRelatorioAssinado(spd));
	}

	@Test
	public void addRemoveRowListTest() {
		SessaoProcessoDocumento spd = new SessaoProcessoDocumento();
		List<ProcessoDocumento> documentosAssinar = new ArrayList<ProcessoDocumento>();
		ProcessoDocumento procDocumento = null;
		for (int i = 1; i <= 5; i++) {
			procDocumento = new ProcessoDocumento();
			procDocumento.setIdProcessoDocumento((i));
			procDocumento.setProcessoDocumento("Documento " + (i));
			documentosAssinar.add(procDocumento);
			spdHome.setDocumentosAssinar(documentosAssinar);
		}
		procDocumento = new ProcessoDocumento();
		procDocumento.setIdProcessoDocumento(2);
		procDocumento.setProcessoDocumento("Documento 2");
		spd.setProcessoDocumento(procDocumento);

		spdHome.addRemoveRowList(spd);

		Assert.assertEquals(4, spdHome.getDocumentosAssinar().size());

		procDocumento.setIdProcessoDocumento(7);
		procDocumento.setProcessoDocumento("Documento 7");
		spd.setProcessoDocumento(procDocumento);

		spdHome.addRemoveRowList(spd);

		Assert.assertEquals(5, spdHome.getDocumentosAssinar().size());
	}
}