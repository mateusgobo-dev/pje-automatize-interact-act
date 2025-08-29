package br.com.infox.test.ibpm.entity;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoPapel;
import br.jus.pje.nucleo.enums.ExigibilidadeAssinaturaEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

public class ExigibilidadeTest {

	@Mock
	ProcessoDocumentoHome processoDocumentoHome;

	@Test
	public void testSeExigibilidadeFacultativa() throws Exception {
		TipoProcessoDocumentoPapel tipoVerificacao = criarTipoProcessoDocumento(ExigibilidadeAssinaturaEnum.F);
		Assert.assertTrue(tipoVerificacao.getExigibilidade().isFacultativo());
	}

	@Test
	public void testSeExigibilidadeObrigatoria() throws Exception {
		TipoProcessoDocumentoPapel tipoVerificacao = criarTipoProcessoDocumento(ExigibilidadeAssinaturaEnum.O);
		Assert.assertTrue(tipoVerificacao.getExigibilidade().isObrigatorio());
	}

	@Test
	public void testSeExigibilidadeNaoAssina() throws Exception {
		TipoProcessoDocumentoPapel tipoVerificacao = criarTipoProcessoDocumento(ExigibilidadeAssinaturaEnum.N);
		Assert.assertTrue(tipoVerificacao.getExigibilidade().isSemAssinatura());
	}

	@Test
	public void testSeExigibilidadeSuficiente() throws Exception {
		TipoProcessoDocumentoPapel tipoVerificacao = criarTipoProcessoDocumento(ExigibilidadeAssinaturaEnum.S);
		Assert.assertTrue(tipoVerificacao.getExigibilidade().isSuficiente());
	}

	private TipoProcessoDocumentoPapel criarTipoProcessoDocumento(ExigibilidadeAssinaturaEnum exigibilidade) {
		TipoProcessoDocumentoPapel tipoRetorno = new TipoProcessoDocumentoPapel();
		tipoRetorno.setExigibilidade(exigibilidade);
		return tipoRetorno;
	}

}