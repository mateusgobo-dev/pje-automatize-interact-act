package br.com.itx.test.component;

import org.junit.Assert;
import org.junit.Test;

import br.com.itx.component.Util;

public class UtilTest {

	private Util util = new Util();

	@Test
	public void getFormattedSizeTest() {
		String actual = util.getFormattedKb(1572864);
		Assert.assertEquals("O tamanho não é o mesmo", "1.536,00 Kb", actual);
		Assert.assertTrue("O tamanho deve ser diferente.", "1.536 Kb" != actual);
	}

	@Test
	public void getFormattedKbTest() {
		Integer bytes = 1024;

		Assert.assertEquals("1,00 Kb", util.getFormattedKb(bytes));

		bytes = 2048;
		Assert.assertEquals("2,00 Kb", util.getFormattedKb(bytes));
	}

	@Test
	public void removeCommentsHTMLTest() {
		String html = "<!-- Comentarios -->";
		Assert.assertEquals("", util.removeCommentsHTML(html));

		html = "codigo<!-- Comentarios -->";
		Assert.assertEquals("codigo", util.removeCommentsHTML(html));
	}
}
