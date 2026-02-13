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
 * Enum para o prazo mínimo em anos para Tipo de Medida de Segurança a serem
 * usados como referência para cadastro de de Informação Criminal Relevante
 * chamado Sentença Absolutória Imprópria
 * 
 * @author lucas.souza
 * 
 */
public enum PrazoMinAnosMedidaSegurancaEnum {

	ANO1("1 ano", 1), ANO2("2 anos", 2), ANO3("3 anos", 3);

	private String label;
	private Integer value;

	/**
	 * Construtor referente a
	 * 
	 * @param label
	 * @param value
	 */
	PrazoMinAnosMedidaSegurancaEnum(String label, Integer value) {
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
