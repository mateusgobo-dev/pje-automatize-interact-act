package br.jus.cnj.pje.servicos;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class RegraAgrupamentoPrevencaoEleicaoOrigemTest {

	@Test
	public void testaTokenAgrupamentoParaDistribuicao() {
		List<RegraAgrupamentoPrevencaoEleicaoOrigem> listaTokens = RegraAgrupamentoPrevencaoEleicaoOrigem.desmembraListaAgrupamentos("ou+G1, e-G2, ou-G3, e+G4");
		
		assertEquals(4, listaTokens.size());

		RegraAgrupamentoPrevencaoEleicaoOrigem token;
		
		token = listaTokens.get(0);
		assertTrue(token.isObrigatoriaPrevencao260CE());
		assertFalse(token.isProdutoCartesianoClasseAssunto());
		assertEquals("G1", token.getCodigoAgrupamentoClasseJudicial());
		
		token = listaTokens.get(1);
		assertFalse(token.isObrigatoriaPrevencao260CE());
		assertTrue(token.isProdutoCartesianoClasseAssunto());
		assertEquals("G2", token.getCodigoAgrupamentoClasseJudicial());

		token = listaTokens.get(2);
		assertFalse(token.isObrigatoriaPrevencao260CE());
		assertFalse(token.isProdutoCartesianoClasseAssunto());
		assertEquals("G3", token.getCodigoAgrupamentoClasseJudicial());

		token = listaTokens.get(3);
		assertTrue(token.isObrigatoriaPrevencao260CE());
		assertTrue(token.isProdutoCartesianoClasseAssunto());
		assertEquals("G4", token.getCodigoAgrupamentoClasseJudicial());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testaTokenAgrupamentoParaDistribuicaoInvalido01() {
		RegraAgrupamentoPrevencaoEleicaoOrigem.desmembraListaAgrupamentos("oi+G1, e-G2");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testaTokenAgrupamentoParaDistribuicaoInvalido02() {
		RegraAgrupamentoPrevencaoEleicaoOrigem.desmembraListaAgrupamentos("ou=G1, e-G2");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testaTokenAgrupamentoParaDistribuicaoInvalido03() {
		RegraAgrupamentoPrevencaoEleicaoOrigem.desmembraListaAgrupamentos("ou+G1,, e-G2");
	}

	@Test
	public void testaTokenAgrupamentoParaDistribuicaoVazio() {
		List<RegraAgrupamentoPrevencaoEleicaoOrigem> listaTokens = RegraAgrupamentoPrevencaoEleicaoOrigem.desmembraListaAgrupamentos("");
		assertTrue(listaTokens.isEmpty());
	}

}