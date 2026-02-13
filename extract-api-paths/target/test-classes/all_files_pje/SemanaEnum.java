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


public enum SemanaEnum implements PJeEnum {

	DOM("Domingo"), SEG("Segunda"), TER("Terça"), QUA("Quarta"), QUI("Quinta"), SEX("Sexta"), SAB("Sábado");

	private String label;

	SemanaEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	/**
	 * Pega o dia da semana equivalente ao numero informado.
	 * 
	 * @param position
	 *            - Sendo Domingo = 0
	 * @return
	 */
	public static SemanaEnum getSemanaEnum(int position) {
		SemanaEnum[] enums = SemanaEnum.values();
		return enums[position - 1];
	}

}
