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


public enum TipoRedistribuicaoEnum implements PJeEnum {
	
	/**
	 * Tipos de redistribuicao gerais
	 */
	M("Erro material"),
	C("Alteração da competência do órgão"),
	P("Prevenção"), 
	W("Recusa de prevenção / dependência"),
	R("Incompetência"),
	
	/**
	 * Tipos de Redistribuição de 1º Grau
	 */
	D("Desaforamento"),
	E("Reunião de execuções fiscais"), 
	I("Impedimento"),  
	S("Suspeição"), 
	U("Criação de unidade judiciária"),
	X("Extinção de unidade judiciária"),  
	
	/**
	 * Tipos de Redistribuição de 2º Grau
	 */
	K("Em razão de posse do relator em cargo diretivo do tribunal"),
	A("Afastamento do relator"),
	T("Afastamento temporário do titular"),
	Z("Sucessão"),
	J("Determinação judicial"),
	N("Impedimento do relator"), 
	O("Suspeição do relator"),
	Y("Impedimento de Órgão Julgador Colegiado");

	private String label;

	TipoRedistribuicaoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}
