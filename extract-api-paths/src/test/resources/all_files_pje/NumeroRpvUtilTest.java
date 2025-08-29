package br.com.infox.test.client;

import junit.framework.Assert;

import org.junit.Test;

import br.com.infox.cliente.NumeroRpvUtil;
import br.jus.pje.nucleo.entidades.Rpv;

public class NumeroRpvUtilTest {

	private static final String NUMERO_RPV = "2011.01.23.405.800006";
	private static final Integer ANO = 2011;
	private static final Integer NUMERO_ORIGEM_PROCESSO = 123;
	private static final Integer NUMERO_VARA = 405;
	private static final Integer NUMERO_SEQUENCIA = 800006;

	private Rpv rpv = new Rpv();

	@Test
	public void formatNumeroRpvTest() {

		String formatNumeroProcesso = NumeroRpvUtil.formatNumeroRpv(ANO, NUMERO_ORIGEM_PROCESSO, NUMERO_VARA, NUMERO_SEQUENCIA);

		Assert.assertEquals(NUMERO_RPV, formatNumeroProcesso);

	}

	@Test
	public void formatNumeroRpvObjTest() {

		rpv.setNumeroSequencia(NUMERO_SEQUENCIA);
		rpv.setAno(ANO);
		rpv.setNumeroVara(NUMERO_VARA);
		rpv.setNumeroOrigemProcesso(NUMERO_ORIGEM_PROCESSO);

		String formatNumeroProcesso = NumeroRpvUtil.formatNumeroRpv(rpv);

		Assert.assertEquals(NUMERO_RPV, formatNumeroProcesso);
	}

	@Test
	public void formatNumeroRpvObjNullTest() {

		rpv = null;

		Assert.assertNull(NumeroRpvUtil.formatNumeroRpv(rpv));
	}
}
