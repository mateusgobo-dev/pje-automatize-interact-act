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

public enum GeneroPenaEnum {

	MU("Multa"), PL("Privativa de Liberdade"), RD("Restritiva de Direito");

	private String label;

	GeneroPenaEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}
