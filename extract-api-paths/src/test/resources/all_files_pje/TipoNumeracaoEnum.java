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
package br.jus.pje.nucleo.enums;

public enum TipoNumeracaoEnum {

	TIPO1("1, 2, 3..."), TIPO2("I, II, III..."), TIPO3("i, ii, iii..."), TIPO4("a, b, c... z, aa, bb, cc..."), 
	TIPO5("A, B, C... Z, AA, BB, CC..."), TIPO6("Um, Dois, Três..."), TIPO7("Primeiro, Segundo, Terceiro...");
	
	private String label;

	TipoNumeracaoEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
}
