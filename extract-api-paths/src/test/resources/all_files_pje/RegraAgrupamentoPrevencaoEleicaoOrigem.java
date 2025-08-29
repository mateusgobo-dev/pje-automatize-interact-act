package br.jus.cnj.pje.servicos;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegraAgrupamentoPrevencaoEleicaoOrigem {
	
	Boolean produtoCartesianoClasseAssunto = null;
	Boolean obrigatoriaPrevencao260CE = null;
	String codigoAgrupamentoClasseJudicial = null;

	public static List<RegraAgrupamentoPrevencaoEleicaoOrigem> desmembraListaAgrupamentos(String stringParaDesmembrar) {
		List<RegraAgrupamentoPrevencaoEleicaoOrigem> listaTokens = new ArrayList<RegraAgrupamentoPrevencaoEleicaoOrigem>();
		if (stringParaDesmembrar == null || stringParaDesmembrar.isEmpty()) {
			return listaTokens;
		}
		String[] tokensAsString = stringParaDesmembrar.replace(" ", "").split(",");
		for (String token : tokensAsString) {
			listaTokens.add(new RegraAgrupamentoPrevencaoEleicaoOrigem(token));
		}
		return listaTokens;
	}
	
	
	
	public RegraAgrupamentoPrevencaoEleicaoOrigem(String stringParaTokenizar) {
		tokenizar(stringParaTokenizar);
	}

	private void tokenizar(String stringParaTokenizar) {
		Pattern padrao = Pattern.compile("(e|ou)(\\+|-)(.*)");
		Matcher matcher = padrao.matcher(stringParaTokenizar);
		
		if (matcher.matches()) {
			if (matcher.group(1).equals("e".toLowerCase())) {
				produtoCartesianoClasseAssunto = true; 
			}
			if (matcher.group(1).equals("ou".toLowerCase())) {
				produtoCartesianoClasseAssunto = false; 
			}
			
			if (matcher.group(2).equals("+".toLowerCase())) {
				obrigatoriaPrevencao260CE = true; 
			}
			if (matcher.group(2).equals("-".toLowerCase())) {
				obrigatoriaPrevencao260CE = false; 
			}
			
			codigoAgrupamentoClasseJudicial = matcher.group(3);
		}
		
		if (! isValidToken()) {
			throw new IllegalArgumentException("Parâmetro 'listaAgrupamentosPrevencao260JE' tem um componente inválido: " + stringParaTokenizar);
		}
	}
	
	public boolean isProdutoCartesianoClasseAssunto() {
		return produtoCartesianoClasseAssunto;
	}

	public boolean isObrigatoriaPrevencao260CE() {
		return obrigatoriaPrevencao260CE;
	}

	public String getCodigoAgrupamentoClasseJudicial() {
		return codigoAgrupamentoClasseJudicial;
	}
	
	public boolean isValidToken() {
		return (produtoCartesianoClasseAssunto != null) 
				&& (obrigatoriaPrevencao260CE != null) 
				&& (codigoAgrupamentoClasseJudicial != null);
	}
	
}
