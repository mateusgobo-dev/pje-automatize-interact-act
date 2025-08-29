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


public enum TipoLocalMedidaProtetivaEnum implements PJeEnum {
	BAR("Bar"), RESTAURANTE("Restaurante"), ESTADIO("Estádio"), GINASIO(
			"Ginásio"), CENTROS_DE_COMPRAS("Centros de compras");
	private String descricao;

	TipoLocalMedidaProtetivaEnum(String descricao) {
		this.setDescricao(descricao);
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

	public String getCodigo() {
		return name();
	}

	@Override
	public String getLabel() {
		return getDescricao();
	}
}
