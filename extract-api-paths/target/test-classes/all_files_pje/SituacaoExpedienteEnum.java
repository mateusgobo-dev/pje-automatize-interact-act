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
 * Enum que representa a Situação do Prazo FP - Fechado parcialmente NF - Nenhum
 * fechado TF - Todos fechados
 * 
 * @author tassio
 * 
 */
public enum SituacaoExpedienteEnum {

	FP("Pelo menos um expediente fechado"), NF("Nenhum expediente fechado"), TF("Todos os expedientes fechados");

	private String label;

	SituacaoExpedienteEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}