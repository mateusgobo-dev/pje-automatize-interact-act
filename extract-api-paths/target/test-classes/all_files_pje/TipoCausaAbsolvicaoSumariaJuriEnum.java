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
 * Enum para Tipo Causa da Absolvicao Sumária do Júria serem usados como
 * referência para cadastro de de Informação Criminal Relevante do tipo Sentença
 * Absolvição Sumária
 * 
 * Caso de uso PJE_UC047
 * 
 * @author kledson.oliveira
 * 
 */
public enum TipoCausaAbsolvicaoSumariaJuriEnum {

	PIF("Provada a inexistência do fato"), PNA("Provado não ser ele autor ou partícipe do fato"), DIP(
			"Demonstrada causa de isenção de pena ou de exclusão do crime"), FNP("O fato não constituir infração penal");

	private String label;

	/**
	 * Atributo referente a Descrição da Causa Absolvicao Sumária do Júri
	 * 
	 * @param label
	 */
	TipoCausaAbsolvicaoSumariaJuriEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
}
