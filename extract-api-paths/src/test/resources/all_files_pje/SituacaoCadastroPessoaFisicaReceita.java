/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.ws.externo.srfb.util;

public class SituacaoCadastroPessoaFisicaReceita {

	public static final int REGULAR = 0;
	public static final int CANCELADA_ENCERRAMENTO_ESPOLIO = 1;
	public static final int SUSPENSA = 2;
	public static final int CANCELADA_OBITO_SEM_ESPOLIO = 3;
	public static final int PENDENTE_REGULARIZACAO = 4;
	public static final int CANCELADA_MULTIPLICIDADE = 5;
	public static final int NULA = 8;
	public static final int CANCELADA_DE_OFICIO = 9;

	public static String getDescricaoSituacao(String codigoSituacao) {
		if (codigoSituacao == null || codigoSituacao.isEmpty()) {
			return null;
		}
		int codigo = Integer.parseInt(codigoSituacao);
		String retorno = null;

		switch (codigo) {
		case REGULAR:
			retorno = "REGULAR";
			break;

		case CANCELADA_ENCERRAMENTO_ESPOLIO:
			retorno = "CANCELADA POR ENCERRAMENTO DE ESPÓLIO";
			break;

		case SUSPENSA:
			retorno = "SUSPENSA";
			break;

		case CANCELADA_OBITO_SEM_ESPOLIO:
			retorno = "CANCELADA POR ÓBITO SEM ESPÓLIO";
			break;

		case PENDENTE_REGULARIZACAO:
			retorno = "PENDENTE DE REGULARIZACAO";
			break;

		case CANCELADA_MULTIPLICIDADE:
			retorno = "CANCELADA POR MULTIPLICIDADE";
			break;

		case NULA:
			retorno = "NULA";
			break;

		case CANCELADA_DE_OFICIO:
			retorno = "CANCELADA DE OFÍCIO";
			break;

		default:
			break;
		}
		return retorno;
	}

}
