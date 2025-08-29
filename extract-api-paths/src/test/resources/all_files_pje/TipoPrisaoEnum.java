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

public enum TipoPrisaoEnum {

	PRV("Preventiva"), PRVDM("Preventiva determinada ou mantida em decisão condenatória recorrível"), TMP("Temporária"), FLG(
			"Flagrante"), PRO("Provisória"), DEP("Para fins de deportação"), EXP("Para fins de expulsão"), EXT(
			"Para fins de extradição"), DEF("Definitiva");

	private String label;

	private TipoPrisaoEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}
