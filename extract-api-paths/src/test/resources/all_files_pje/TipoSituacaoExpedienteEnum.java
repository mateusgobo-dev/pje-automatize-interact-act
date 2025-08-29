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

public enum TipoSituacaoExpedienteEnum {

	PENDENTES_CIENCIA_RESPOSTA("Pendentes de ciência ou de resposta"), 
	PENDENTES_CIENCIA("Apenas pendentes de ciência"), 
	CIENCIA_DESTINATARIO("Ciência dada pelo destinatário direto ou indireto - pendente de resposta"), 
	CIENCIA_JUDICIARIO("Ciência dada pelo Judiciário - pendente de resposta"),
	PRAZO("Cujo prazo findou nos últimos 10 dias - sem resposta"),
	SEM_PRAZO("Sem prazo"),
	RESPONDIDOS("Respondidos nos últimos 10 dias");
	
	private String label;

	TipoSituacaoExpedienteEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}