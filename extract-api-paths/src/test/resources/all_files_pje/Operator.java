/**
 * pje
 * Copyright (C) 2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.search;

import br.jus.pje.nucleo.enums.PJeEnum;

/**
 * Enumeração dos tipos de operações passíveis de realização em uma
 *  consulta a dados.
 *  
 * @author Antonio Augusto Silva Martins
 *
 */
public enum Operator implements PJeEnum {
	
	equals("="), 
	notEquals("!="), 
	greater(">"), 
	less("<"),
	greaterOrEquals(">="),
	lessOrEquals("<="),
	contains("like"),
	startsWith("like"),
	endsWith("like"),
	between("between"),
	in("in"),
	empty(" IS EMPTY "),
	or(" OR "), 
	isNull (" IS NULL "),
	not (" NOT "), 
	and (" AND "),
	path(" PATH "),
	bitwiseAnd("bitwise_and"),
	fulltext("full_text"),
	exists(" EXISTS "),
	notExists(" NOT EXISTS ");
	
	private String label;

	Operator(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}
}