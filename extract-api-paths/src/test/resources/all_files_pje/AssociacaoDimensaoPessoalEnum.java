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
package br.jus.pje.nucleo.entidades;

import br.jus.pje.nucleo.enums.PJeEnum;

public enum AssociacaoDimensaoPessoalEnum implements PJeEnum {
	A("Contém"), E("Não contém");

	private String label;

	private AssociacaoDimensaoPessoalEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}
}