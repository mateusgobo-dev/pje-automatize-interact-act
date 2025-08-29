package br.com.infox.ibpm.test.util;

import junit.framework.Assert;

import org.junit.Test;

import br.com.infox.ibpm.home.EnderecoHome;
import br.jus.pje.nucleo.entidades.Endereco;

public class EnderecoHomeTest {

	EnderecoHome enderecoHome = new EnderecoHome();

	@Test
	public void checkEndereco() {
		Endereco enderecoValido = new Endereco();
		Endereco enderecoInvalido = new Endereco();
		enderecoValido.setNomeLogradouro("CAIS DO APOLO, S/N");

		Assert.assertEquals(true, enderecoHome.checkEndereco(enderecoValido));
		Assert.assertEquals(false, enderecoHome.checkEndereco(enderecoInvalido));
	}
}
