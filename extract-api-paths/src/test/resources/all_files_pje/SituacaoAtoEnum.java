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
 * Enum que representa a Situação do Ato EP - Enviado parcialmente NE - Não
 * enviado SE - Segredo/Sigilo
 * 
 * @author tassio
 * 
 */
public enum SituacaoAtoEnum {

	EP("Ato não enviado para ao menos uma parte"), NE("Ato não enviado para nenhuma parte"), SE("Segredo/Sigilo");

	private String label;

	SituacaoAtoEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}