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
package br.jus.pje.jt.enums;

public enum ObrigacaoFazerEnum {

	ACP("Anotação de Carteira Profissional"), CDR("Carta de Referência"), EGS("Entrega de Guias de Seguro Desemprego"), ETR(
			"Entrega do Termo de Rescisão Contratual"), REI("Reintegração"), OUT("Outras Obrigações");

	private String label;

	ObrigacaoFazerEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}