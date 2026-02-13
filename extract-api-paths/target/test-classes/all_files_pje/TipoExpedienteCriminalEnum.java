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

public enum TipoExpedienteCriminalEnum {

	AS("Alvará Soltura", "AlvaraSoltura"), CM("Contramandado", "ContraMandado"), MP("Mandado de Prisão",
			"MandadoPrisao");

	private String label;
	private String nomeEntidade;

	TipoExpedienteCriminalEnum(String label, String nomeEntidade) {
		this.label = label;
		this.nomeEntidade = nomeEntidade;
	}

	public String getLabel() {
		return this.label;
	}

	public String getNomeEntidade() {
		return nomeEntidade;
	}
}
