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

public enum PrazoMinMesesMedidaSegurancaEnum {

	MES1("1 mês", 1), MES2("2 meses", 2), MES3("3 meses", 3), MES4("4 meses", 4), MES5("5 meses", 5), MES6("6 meses", 6), MES7(
			"7 meses", 7), MES8("8 meses", 8), MES9("9 meses", 9), MES10("10 meses", 10), MES11("11 meses", 11);

	private String label;
	private Integer value;

	/**
	 * Construtor referente à descrição a ser utilizada em seleção e valor a ser
	 * inserido em base de dados
	 * 
	 * @param label
	 * @param value
	 */
	PrazoMinMesesMedidaSegurancaEnum(String label, Integer value) {
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public Integer getValue() {
		return value;
	}
}
