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

public enum MotivoRecebimentoEnum {
	
	PD("para distribuir","7086"),
	PIP("para incluir em pauta","7093");	
	
	private String label;
	private String codigo;
	
	MotivoRecebimentoEnum(String label, String codigo) {
		this.label = label;
		this.codigo = codigo;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public String getCodigo() {
		return this.codigo;
	}
}
