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

public enum ConclusaoEnum {PR("Dar provimento"),
    PP("Provimento parcial"),
    NP("Nega provimento"),
    AR("Acompanha o relator"),
    DP("Discorda em parte do relator"),
    DR("Discorda do relator"),
    RE("Relator"),
	NC("Não conhecer");

    private String label;

    ConclusaoEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}
