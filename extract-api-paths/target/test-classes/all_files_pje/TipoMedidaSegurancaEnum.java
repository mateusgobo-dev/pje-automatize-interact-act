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
 * Enum para Tipo de Medida de Segurança a serem usados como referência para
 * cadastro de de Informação Criminal Relevante chamado Sentença Absolutória
 * Imprópria
 * 
 * Caso de uso PJE_UC023
 * 
 * @author lucas.souza
 * 
 */
public enum TipoMedidaSegurancaEnum {

	INT("Internação de hospital de custodia"), TRA("Tratamento ambulatorial");

	private String label;

	/**
	 * Atributo referente a Descrição da Medida de Segurança
	 * 
	 * @param label
	 */
	TipoMedidaSegurancaEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
}
