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


public enum FrequenciaComparecimentoEmJuizo implements PJeEnum {
	QUINZENAL("Quinzenal"), MENSAL("Mensal"), BIMESTRAL("Bimestral"), TRIMESTRAL("Trimestral"), SEMESTRAL("Semestral"), ANUAL(
			"Anual");
	private String descricao;

	FrequenciaComparecimentoEmJuizo(String descricao) {
		this.setDescricao(descricao);
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

	@Override
	public String getLabel() {
		return getDescricao();
	}
}
