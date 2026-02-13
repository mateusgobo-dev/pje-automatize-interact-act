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

public enum FasesUnificacaoEnum implements PJeEnum {

	FASE_01_SELECAO_PESSOAS("Seleção pessoas secundarias para unificação"),
	RESUMO_UNIFICACAO("Pagina final para confirmacao da unificacao"),
	CONCLUSAO_UNIFICACAO("Pagina final conclusao da unificacao");

	private String label;

	FasesUnificacaoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}
}