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

public enum RpvTipoRestricaoPagamentoEnum {

	SR("Sem Restrição"), AL("Alvará ou meio equivalente (valores serão liberados pelo Juízo da Execução)");

	private String label;

	RpvTipoRestricaoPagamentoEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}