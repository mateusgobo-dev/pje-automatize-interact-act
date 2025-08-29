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

public enum OrdemDiaDoMesEnum {

	PRIMEIRA("Na Primeira", 1), SEGUNDA("Na Segunda", 2), TERCEIRA("Na terceira", 3), QUARTA("Na Quarta", 4), QUINTA(
			"Na Quinta", 5);

	private String label;
	private int ordem;

	OrdemDiaDoMesEnum(String label, int ordem) {
		this.label = label;
		this.ordem = ordem;
	}

	public String getLabel() {
		return this.label;
	}

	public int getOrdem() {
		return this.ordem;
	}

}
