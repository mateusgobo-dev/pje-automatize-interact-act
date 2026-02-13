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

public enum StatusSessaoEnum {

	EASP("Em Andamento - Sem Pauta"), EACP("Em Andamento - Com Pauta"), A("Aberta"), R("Realizada"), RE("Registrado Evento"), F("Finalizada");

	private String label;

	StatusSessaoEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}
