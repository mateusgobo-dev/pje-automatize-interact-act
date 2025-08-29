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

public enum TipoAnotacao {
	ANOTACAO("Anotação"), DIVERGENCIA("Divergência"), 
	SUGESTAO_DISPOSITIVO("Sugestão de Dispositivo"), CONSIDERACOES("Considerações"),
	VOTO("Voto");
	
	private String label;
	
	private TipoAnotacao(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
}
