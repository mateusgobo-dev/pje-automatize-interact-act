package br.com.infox.test.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public class NumeroProcessoUtilTest {

	private static final String NUMERO_PROCESSO = "0800006-12.2011.4.05.0000";
	private static final Integer ANO = 2011;
	private static final Integer NUMERO_ORIGEM_PROCESSO = 00;
	private static final Integer NUMERO_VARA = 405;
	private static final Integer NUMERO_SEQUENCIA = 800006;
	private static final Integer NUMERO_DIGITO_VERIFICADOR = 12;

	private ProcessoTrf pTrf = new ProcessoTrf();

	@Test
	public void completaZeroTest() {
		String result = NumeroProcessoUtil.completaZeros(123, 5);
		assertEquals("00123", result);
	}

	@Test
	public void calcDigitoVerificadorTest() {

		long numeroSequencia = 1;

		int digitoVerificador = NumeroProcessoUtil.calcDigitoVerificador(numeroSequencia, ANO, NUMERO_VARA,
				NUMERO_ORIGEM_PROCESSO);

		assertEquals(23, digitoVerificador);
	}

	@Test
	public void formatNumeroProcessoTest() {

		String formatNumeroProcesso = NumeroProcessoUtil.formatNumeroProcesso(NUMERO_SEQUENCIA,
				NUMERO_DIGITO_VERIFICADOR, ANO, NUMERO_VARA, NUMERO_ORIGEM_PROCESSO);

		assertEquals(NUMERO_PROCESSO, formatNumeroProcesso);

	}

	@Test
	public void formatNumeroProcessoObjTest() {

		pTrf.setNumeroSequencia(NUMERO_SEQUENCIA);
		pTrf.setNumeroDigitoVerificador(NUMERO_DIGITO_VERIFICADOR);
		pTrf.setAno(ANO);
		pTrf.setNumeroOrgaoJustica(NUMERO_VARA);
		pTrf.setNumeroOrigem(NUMERO_ORIGEM_PROCESSO);

		String formatNumeroProcesso = NumeroProcessoUtil.formatNumeroProcesso(pTrf);

		assertEquals(NUMERO_PROCESSO, formatNumeroProcesso);
	}

	@Test
	public void formatNumeroProcessoObjNullTest() {

		pTrf = null;

		assertNull(NumeroProcessoUtil.formatNumeroProcesso(pTrf));
	}

	
//	@Test
	public void numerarProcessoTest() {

		pTrf.setNumeroSequencia(NUMERO_SEQUENCIA);
		pTrf.setNumeroDigitoVerificador(NUMERO_DIGITO_VERIFICADOR);
		pTrf.setAno(ANO);
		pTrf.setNumeroOrgaoJustica(NUMERO_VARA);
		pTrf.setNumeroOrigem(NUMERO_ORIGEM_PROCESSO);

		NumeroProcessoUtil.numerarProcesso(pTrf, NUMERO_VARA, NUMERO_ORIGEM_PROCESSO);

		assertEquals(NUMERO_PROCESSO, pTrf.getNumeroProcesso());
	}

	@Test
	public void testarObtencaoDoNumeroOrgaoJusticaQuandoNumeroEstaValido() {
		String numeroOrgaoJusticaEsperado = "807";
		String numeroOrgaoJusticaRetornado = NumeroProcessoUtil.obterNumeroOrgaoJustica("0200226-12.2024.8.07.0001");
		assertEquals(numeroOrgaoJusticaEsperado, numeroOrgaoJusticaRetornado);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testarObtencaoDoNumeroOrgaoJusticaQuandoNumeroEstaInvalido() {
		NumeroProcessoUtil.obterNumeroOrgaoJustica("0200226-12.2024.1.11.0001");
	}
}
