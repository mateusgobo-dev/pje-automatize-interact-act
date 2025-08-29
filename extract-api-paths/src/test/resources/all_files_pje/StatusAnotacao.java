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
package br.jus.pje.nucleo.enums.editor;

public enum StatusAnotacao {
	C("Concluída"), L("Liberada"), R("Retirada"), E("Excluída"), N("Não Concluída");
	
	private String label;
	
	private StatusAnotacao(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
}
