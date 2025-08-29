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

public enum ContextoVotoEnum {

	C("Concorda","C"),
	P("Concorda Parcialmente","P"),
	D("Discorda","D"),
	S("Suspeição","S"),
	I("Impedido","I"),
	N("Não conhece","N");

	private String descricao;
	private String contexto;
	
	private ContextoVotoEnum(String descricao, String contexto){
		this.descricao = descricao;
		this.contexto = contexto;
	}
	
	public String getDescricao(){
		return this.descricao;
	}
	
	public String getContexto(){
		return this.contexto;
	}
}