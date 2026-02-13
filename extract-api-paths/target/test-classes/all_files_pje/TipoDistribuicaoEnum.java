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


public enum TipoDistribuicaoEnum implements PJeEnum {

	/**
	 * Distribuição ou redistribuição automática 
	 */
	A("Automática"), 
	CE("Por competência exclusiva"),
	I("Incidental"), 
	PD("Por dependência"),	
	/**
	 * Distribuição ou redistribuição por prevenção. 
	 */
	PP("Por prevenção"), 
	/**
	 * Distribuição ou redistribuição por sorteio. 
	 */
	S("Por sorteio"),
	Z("Por sucessão"),
	
	/**
 	* Distribuição ou Redistribuição por encaminhamento. 
 	*/
	EN("Por encaminhamento");

	private String label;

	TipoDistribuicaoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	public boolean isDependencia(){
		return this.equals(PD);
	}
	
	public boolean isSorteio(){
		return this.equals(S);
	}
	
	public boolean isEncaminhamento(){
		return this.equals(EN);
	}
}