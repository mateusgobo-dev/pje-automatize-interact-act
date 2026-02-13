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

public class SituacaoCadastroPessoaJuridicaReceita {

	public static final String NULA = "01";
	public static final String ATIVA = "02";
	public static final String SUSPENSA = "03";
	public static final String INAPTA = "04";
	public static final String BAIXADA = "08";

	public static String getDescricaoSituacao(String codigoSituacao) {
		if (codigoSituacao == null || codigoSituacao.isEmpty()) {
			return null;
		}
		int codigo = Integer.parseInt(codigoSituacao);
		String retorno = null;

		if (codigo == Integer.parseInt(NULA)) {
			retorno = "NULA";
		}

		if (codigo == Integer.parseInt(ATIVA)) {
			retorno = "ATIVA";
		}

		if (codigo == Integer.parseInt(SUSPENSA)) {
			retorno = "SUSPENSA";
		}

		if (codigo == Integer.parseInt(INAPTA)) {
			retorno = "INAPTA";
		}

		if (codigo == Integer.parseInt(BAIXADA)) {
			retorno = "BAIXADA";
		}

		return retorno;
	}
}
