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


public enum StatusPericiaEnum implements PJeEnum {
	R("Remarcada"), M("Marcada"), C("Cancelada"), F("Feita");

	private String label;

	StatusPericiaEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}
}
