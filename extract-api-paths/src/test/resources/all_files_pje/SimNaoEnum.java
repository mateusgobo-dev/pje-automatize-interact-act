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


public enum SimNaoEnum implements PJeEnum {

	S("Sim"), N("Não");

	private String label;

	SimNaoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	/**
	 * Retorna o SimNaoEnum pelo valor passado por boleano.
	 * 
	 * @param booleano
	 * @return SimNaoEnum
	 */
	public static SimNaoEnum getEnum(boolean booleano) {
		return (booleano ? SimNaoEnum.S : SimNaoEnum.N);
	}
}
