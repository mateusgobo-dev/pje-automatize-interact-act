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

/**
 * @author Haroldo de Lima Arouca
 * @since 1.4.2
 * @created 2011-08-30
 * @category PJE-JT
 */

public enum TipoExpedienteEnum {

	M("Mandado"), O("Ofício"), C("Carta"), D("Diário Eletrônico");

	private String label;

	TipoExpedienteEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}
