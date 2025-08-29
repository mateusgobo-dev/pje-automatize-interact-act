package br.jus.cnj.pje.nucleo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 *
 * @author <a href="mailto:tr300804@trf1.jus.br">Bruno Paz</a> 
 */
public class InscricaoMFUtilTest {

	@Test
	@SuppressWarnings("static-method")
	public void testaMascaraCnpj() {
		assertEquals("06.182.058/0001-80", InscricaoMFUtil.mascaraCnpj("06.182.058/0001-80"));
		assertEquals("06.182.058/0001-80", InscricaoMFUtil.mascaraCnpj("06182058000180"));
		assertEquals("06.182.058/0001-80", InscricaoMFUtil.mascaraCnpj("6182058000180"));
		assertEquals("00.000.158/0001-80", InscricaoMFUtil.mascaraCnpj("158000180"));
		assertEquals("06.182.058", InscricaoMFUtil.mascaraCnpj("06.182.058"));
		assertEquals("06.182.058", InscricaoMFUtil.mascaraCnpj("06182058"));
		assertEquals("06.182.058", InscricaoMFUtil.mascaraCnpj("6182058"));
	}

	@Test(expected=IllegalArgumentException.class)
	@SuppressWarnings("static-method")
	public void testaFormatacaoCnpjNull() {
		InscricaoMFUtil.mascaraCnpj(null);
	}	

	@Test(expected=IllegalArgumentException.class)
	@SuppressWarnings("static-method")
	public void testaFormatacaoCnpjVazio() {
		InscricaoMFUtil.mascaraCnpj("");
	}	
	
	@Test(expected=IllegalArgumentException.class)
	@SuppressWarnings("static-method")
	public void testaFormatacaoCnpjEspacoEmBranco() {
		InscricaoMFUtil.mascaraCnpj(" ");
	}		

	@Test
	@SuppressWarnings("static-method")
	public void testaMascaraCnpjAcrescentandoDV() {
		assertEquals("06.182.058/0001-80", InscricaoMFUtil.mascaraCnpjComComplemento("06.182.058/0001-80"));
		assertEquals("06.182.058/0001-80", InscricaoMFUtil.mascaraCnpjComComplemento("06182058000180"));
		assertEquals("06.182.058/0001-80", InscricaoMFUtil.mascaraCnpjComComplemento("6182058000180"));		
		assertEquals("06.182.058/0001-80", InscricaoMFUtil.mascaraCnpjComComplemento("06.182.058"));
		assertEquals("06.182.058/0001-80", InscricaoMFUtil.mascaraCnpjComComplemento("06182058"));
		assertEquals("06.182.058/0001-80", InscricaoMFUtil.mascaraCnpjComComplemento("6182058"));
	}
	
	@Test
	public void testaObtemRaizCnpj() {
		assertEquals("06.182.058", InscricaoMFUtil.obtemRaizDeCnpjSemValidacao("06.182.058/0001-80"));
		assertEquals("06.182.058", InscricaoMFUtil.obtemRaizDeCnpjSemValidacao("  06.182.058/0001-80  "));
		assertEquals("06.182.058", InscricaoMFUtil.obtemRaizDeCnpjSemValidacao("06.182.058/0001-00"));
		assertEquals("06.182.058", InscricaoMFUtil.obtemRaizDeCnpjSemValidacao("  06.182.058/0001-00  "));
		assertEquals("06182058", InscricaoMFUtil.obtemRaizDeCnpjSemValidacao("06182058000180"));
		assertEquals("06182058", InscricaoMFUtil.obtemRaizDeCnpjSemValidacao("06182058000100"));
		assertEquals("06182058", InscricaoMFUtil.obtemRaizDeCnpjSemValidacao("  06182058000180  "));
		assertEquals("06182058", InscricaoMFUtil.obtemRaizDeCnpjSemValidacao("  06182058000100  "));		
		assertNull(InscricaoMFUtil.obtemRaizDeCnpjSemValidacao("06182058ABCD180"));
		assertNull(InscricaoMFUtil.obtemRaizDeCnpjSemValidacao("16190904220"));
		assertNull(InscricaoMFUtil.obtemRaizDeCnpjSemValidacao("INVALID_29.403.763/0001-65"));
		assertNull(InscricaoMFUtil.obtemRaizDeCnpjSemValidacao(null));
	}

	@Test
	public void testaValidaCNPJ() {
		assertTrue(InscricaoMFUtil.validaCNPJ("06.182.058/0001-80"));
		assertTrue(InscricaoMFUtil.validaCNPJ("   06.182.058/0001-80   "));
		assertTrue(InscricaoMFUtil.validaCNPJ("06182058000180"));
		assertTrue(InscricaoMFUtil.validaCNPJ(" 06182058000180 "));
		assertFalse(InscricaoMFUtil.validaCNPJ("06182058000100"));
		assertFalse(InscricaoMFUtil.validaCNPJ("06182058ABCD180"));
		assertFalse(InscricaoMFUtil.validaCNPJ("0A182058ABCD180"));
		assertFalse(InscricaoMFUtil.validaCNPJ("16190904220"));
		assertFalse(InscricaoMFUtil.validaCNPJ("INVALID_29.403.763/0001-65"));
		assertFalse(InscricaoMFUtil.validaCNPJ(null));
	}
}
