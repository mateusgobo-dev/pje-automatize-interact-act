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


public enum ResultadoVotacaoEnum {

	UN("Unânime"), UP("Unânime com pendência"), MA("Maioria"), MP("Maioria com pendência");
	
	private String label;
	
	ResultadoVotacaoEnum(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return this.label;
	}

}
