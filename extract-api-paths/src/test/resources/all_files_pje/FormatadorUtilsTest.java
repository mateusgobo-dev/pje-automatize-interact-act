package br.jus.cnj.pje.util;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Before;
import org.junit.Test;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


public class FormatadorUtilsTest {

	private FormatadorUtils formatadorUtils;
	
	private List<ProcessoTrf> source;
	
	private SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");
	
	private final String REGEX_NUMERO_PROCESSO = "\\d{7}-?\\d{2}\\.?\\d{4}\\.?\\d\\.?\\d{2}\\.?\\d{4}";
	
	private final String REGEX_TABLE = "<table[^>]*>(<tr[^>]*>(<td[^>]*>(.*?)</td>)</tr>)</table>";
	
	@Before
	public void before() throws PJeBusinessException{
		
		this.formatadorUtils = new FormatadorUtils();
		this.source = new ArrayList<ProcessoTrf>();
		
		ProcessoTrf processoTrf;
		Processo processo;
		OrgaoJulgador orgaoJulgador;
		
		/*
		 * Criar lista de Objetos para testar as situações
		 */
		try {
			
			processoTrf = new ProcessoTrf();
			processo = new Processo();
			orgaoJulgador = new OrgaoJulgador();
			processo.setNumeroProcesso("0600003-43.2013.6.00.0000");
			orgaoJulgador.setOrgaoJulgador("Ministro Dias Tóffoli");
			processoTrf.setProcesso(processo);
			processoTrf.setOrgaoJulgador(orgaoJulgador);
			processoTrf.setDataAutuacao(dateFormater.parse("10/04/2013"));
			processoTrf.setNumeroSequencia(3);
			source.add(processoTrf);
			
			processoTrf = new ProcessoTrf();
			processo = new Processo();
			processo.setNumeroProcesso("0600004-86.2013.6.00.0000");
			processoTrf.setProcesso(processo);
			processoTrf.setOrgaoJulgador(orgaoJulgador);
			processoTrf.setDataAutuacao(dateFormater.parse("10/04/2013"));
			processoTrf.setNumeroSequencia(4);
			source.add(processoTrf);
			
			processoTrf = new ProcessoTrf();
			processo = new Processo();
			orgaoJulgador = new OrgaoJulgador();
			processo.setNumeroProcesso("0600005-91.2013.6.00.0000");
			orgaoJulgador.setOrgaoJulgador("Ministro Henrique Neves");
			processoTrf.setProcesso(processo);
			processoTrf.setOrgaoJulgador(orgaoJulgador);
			processoTrf.setNumeroSequencia(5);
			source.add(processoTrf);
			source.add(null);
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new PJeBusinessException(e);
		}
	}
	
	@Test
	public void testeListaSimples() throws PJeBusinessException {
		String resultado = formatadorUtils.lista(source);
		List<String> tokens = new ArrayList<String>();
		System.out.println("********************* LISTA SIMPLES ***********************\n");
		System.out.println(resultado);
		System.out.println("\n***********************************************************\n\n");

		tokens.add(resultado.substring(0, resultado.indexOf(',')).trim());
		tokens.add(resultado.substring(resultado.indexOf(',')+1, resultado.indexOf('e')).trim());
		tokens.add(resultado.substring(resultado.indexOf('e')+1).trim());
		
		for (String token : tokens) {
			assertTrue(token.matches(REGEX_NUMERO_PROCESSO));
		}
	}
	
	@Test
	public void testeListaComSeparador() throws PJeBusinessException {
		String resultado = formatadorUtils.lista(source, ";");
		List<String> tokens = new ArrayList<String>();
		System.out.println("********************* LISTA SIMPLES ***********************\n");
		System.out.println(resultado);
		System.out.println("\n***********************************************************\n\n");
		
		tokens.add(resultado.substring(0, resultado.indexOf(';')).trim());
		tokens.add(resultado.substring(resultado.indexOf(';')+1, resultado.indexOf('e')).trim());
		tokens.add(resultado.substring(resultado.indexOf('e')+1).trim());
		
		for (String token : tokens) {
			assertTrue(token.matches(REGEX_NUMERO_PROCESSO));
		}
	}
	
	@Test
	public void testeListaComSeparadorConectorFalse() throws PJeBusinessException {
		String token, resultado = formatadorUtils.lista(source, ";", false);
		System.out.println("********************* LISTA SIMPLES ***********************\n");
		System.out.println(resultado);
		System.out.println("\n***********************************************************\n\n");
		
		StringTokenizer tokens = new StringTokenizer(resultado, ";");
		
		assertTrue(tokens.countTokens() == 3);
		
		while(tokens.hasMoreTokens()){
			token = tokens.nextToken().trim();
			assertTrue(token.matches(REGEX_NUMERO_PROCESSO));
		}
	}
	
	@Test
	public void testeListaComSeparadorConectorUmaPropriedade() throws PJeBusinessException {
		
		String token, resultado = formatadorUtils.lista(source, ";", false, "numeroProcesso");
		System.out.println("********************* LISTA SIMPLES ***********************\n");
		System.out.println(resultado);
		System.out.println("\n***********************************************************\n\n");
		
		StringTokenizer tokens = new StringTokenizer(resultado, ";");
		
		assertTrue(tokens.countTokens() == 3);
		
		while(tokens.hasMoreTokens()){
			token = tokens.nextToken().trim();
			assertTrue(token.matches(REGEX_NUMERO_PROCESSO));
		}
		
	}
	
	@Test
	public void testeListaComSeparadorConectorDuasPropriedades() throws PJeBusinessException {
		
		String token, resultado = formatadorUtils.lista(source, ";", false, "numeroProcesso;orgaoJulgador");
		System.out.println("********************* LISTA SIMPLES ***********************\n");
		System.out.println(resultado);
		System.out.println("\n***********************************************************\n\n");
		
		StringTokenizer tokens = new StringTokenizer(resultado, ";");
		
		assertTrue(tokens.countTokens() == 3);
		
		while(tokens.hasMoreTokens()){
			token = tokens.nextToken().trim();
			assertTrue(token.matches(REGEX_NUMERO_PROCESSO + "\\s\\(.+\\)"));
		}
		
	}
	
	@Test
	public void testeListaComSeparadorConectorMaisQueDuasPropriedades() throws PJeBusinessException {
		
		String token, resultado = formatadorUtils.lista(source, ";", false, "numeroProcesso;orgaoJulgador;dataAutuacao");
		System.out.println("********************* LISTA SIMPLES ***********************\n");
		System.out.println(resultado);
		System.out.println("\n***********************************************************\n\n");
		
		StringTokenizer tokens = new StringTokenizer(resultado, ";");
		
		assertTrue(tokens.countTokens() == 3);
		
		while(tokens.hasMoreTokens()){
			token = tokens.nextToken().trim();
			assertTrue(token.matches(REGEX_NUMERO_PROCESSO + "\\s\\(.+-?.+\\)"));
		}
		
	}
	

	@Test
	public void testeTabelaSimples() throws PJeBusinessException {
		
		String resultado = formatadorUtils.tabela(source);
		System.out.println("************** TABELA SIMPLES COLUNA SIMPLES ******************\n");
		System.out.println(resultado);
		System.out.println("\n***********************************************************\n\n");
		
		assertTrue(resultado.matches(REGEX_TABLE));
		
	}
	
	@Test
	public void testeTabelaPropriedadesColunaUnica() throws PJeBusinessException {
		
		String resultado = formatadorUtils.tabela(source, true, "numeroProcesso;orgaoJulgador;dataAutuacao");
		System.out.println("************** TABELA LISTA COLUNA SIMPLES ******************\n");
		System.out.println(resultado);
		System.out.println("\n***********************************************************\n\n");
		
		assertTrue(resultado.matches(REGEX_TABLE));
		
	}
	
	@Test
	public void testeTabelaPropriedadesVariasColunas() throws PJeBusinessException {
		
		String resultado = formatadorUtils.tabela(source, false, "numeroProcesso;orgaoJulgador;dataAutuacao");
		System.out.println("*************** TABELA COLUNAS ATRIBUTOS *******************\n");
		System.out.println(resultado);
		System.out.println("\n***********************************************************\n\n");
		
		assertTrue(resultado.matches(REGEX_TABLE));
		
	}
	
	@Test
	public void testeListaComElementosNulos() throws PJeBusinessException{
		List<String> nomes = Arrays.asList(new String[]{null, "Nome 1", null, null, null, "Nome 2", null, "Nome 3", null});
		assertEquals(9, nomes.size());
		String resultado = formatadorUtils.lista(nomes, ",");
		assertEquals("Nome 1,Nome 2 e Nome 3", resultado);
	}

	@Test
 	public void testeVariavelCaixaAlta(){
 		String resultado = formatadorUtils.caixaAlta("caixa alta");
 		assertEquals("CAIXA ALTA", resultado);
 	}
 	
 	@Test
 	public void testeVariavelCaixaBaixa(){
 		String resultado = formatadorUtils.caixaBaixa("CAIXA BAIXA");
 		assertEquals("caixa baixa", resultado);
 	}
 	
 	@Test
 	public void testeValorNuloCaixaAlta(){
 		String resultado = formatadorUtils.caixaAlta(null);
 		assertEquals("", resultado);
	}	
}
