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

public enum CadastroMensalEnum {

	O("Ordem de Classificação"), R("Repete no(s) dia(s)"), U("Ultimo(s) dia(s) do(s) mês(es)");

	private String label;

	CadastroMensalEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}
