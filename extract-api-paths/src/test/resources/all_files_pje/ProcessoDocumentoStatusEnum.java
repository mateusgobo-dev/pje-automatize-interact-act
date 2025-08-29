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

public enum ProcessoDocumentoStatusEnum {

	NN("Não Feito e Não Liberado"), FN("Feito e Não Liberado"), FL("Feito e Liberado");

	private String label;

	ProcessoDocumentoStatusEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}