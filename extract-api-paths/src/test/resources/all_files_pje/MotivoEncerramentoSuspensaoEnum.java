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
 * Enum para os Motivos de Encerramento de Suspenção de Processo a serem usados
 * como referência para cadastro de Informação Criminal Relevante chamada
 * Encerrar Suspenção de Processo
 * 
 * Caso de uso PJE_UC038
 * 
 * @author kledson.gomes
 * 
 */
public enum MotivoEncerramentoSuspensaoEnum {

	OBG("Revogação por causas obrigatórias"), FAC("Revogação por causas facultativas"), OUT("Outros");

	private String label;

	/**
	 * Atributo referente a Descrição da Motivo Encerramento
	 * 
	 * @param label
	 */
	MotivoEncerramentoSuspensaoEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
}
