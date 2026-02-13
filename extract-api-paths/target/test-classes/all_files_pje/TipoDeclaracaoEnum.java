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
package br.jus.pje.jt.enums;

import br.jus.pje.nucleo.enums.PJeEnum;

public enum TipoDeclaracaoEnum implements PJeEnum {
	
	P("Declaro, sob as penas da lei, que neste ato apresentei instrumento de mandato."), 
	M("Protesto pela apresentação oportuna do instrumento de mandato, na forma da lei.");

	private String label;

	TipoDeclaracaoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}
}

