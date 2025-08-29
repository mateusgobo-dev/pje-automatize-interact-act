package br.com.infox.test.client;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.util.StringUtil;

public class StringUtilTest {

	@Test
	public void retiraZerosEsquerdaTest() {
		String result = StringUtil.retiraZerosEsquerda("0015200");
		assertEquals("15200", result);
	}

	@Test
	public void formartCpfTest() {
		String cpf = "83220607500";
		String teste = StringUtil.formartCpf(cpf);
		assertEquals("832.206.075-00", teste);
	}

	@Test
	public void formatCnpjTest() {
		assertEquals("12.345.678/0000-00", StringUtil.formatCnpj("12345678000000"));
	}

	@Test
	public void changeCharTest() {
		assertEquals("Tato", StringUtil.changeChar("Teto", 'e', "a"));
	}

	@Test
	public void replaceTest() {
		String subject = "Remover teste de replace.";
		String find = "replace";
		String replace = "permuta";

		assertEquals("Remover teste de permuta.", StringUtil.replace(subject, find, replace));
	}

	@Test
	public void getUsAsciiTest() {
		String text = "Remoção da acentuação do texto";
		assertEquals("Remocao da acentuacao do texto", StringUtil.getUsAscii(text));
	}

	@Test
	public void capitalizeAllWordsTest() {
		String word = "teste capitalize";
		assertEquals("Teste Capitalize", StringUtil.capitalizeAllWords(word));
	}

	@Test
	public void completaZerosTest() {
		String numero = "18";
		int tamanho = 5;
		assertEquals("00018", StringUtil.completaZeros(numero, tamanho));
	}

	@Test
	public void concatListTest() {
		List<Object> lista = new ArrayList<Object>();

		ClasseJudicial cj = new ClasseJudicial();
		cj.setIdClasseJudicial(1);
		cj.setClasseJudicial("CLASSE 1");
		cj.setCodClasseJudicial("123");
		lista.add(cj);

		cj = new ClasseJudicial();
		cj.setIdClasseJudicial(2);
		cj.setClasseJudicial("CLASSE 2");
		cj.setCodClasseJudicial("321");
		lista.add(cj);

		String delimitador = "|";
		String resultado = "CLASSE 1 (123)|CLASSE 2 (321)";
		assertEquals(resultado, StringUtil.concatList(lista, delimitador));
	}

	@Test
	public void padLeftTest() {
		String esperado = "  3";
		String atual = StringUtil.padLeft("3", 3);
		assertEquals(esperado, atual);
	}

	@Test
	public void padRightTest() {
		String esperado = "3  ";
		String atual = StringUtil.padRight("3", 3);
		assertEquals(esperado, atual);
	}
	
}