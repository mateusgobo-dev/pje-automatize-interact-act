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


public enum ProcessoTrfApreciadoEnum implements PJeEnum {

	/**
	 * Indica que um dado pedido pende de apreciaÃ§Ã£o 
	 */
	A("Apreciar"), 
	/**
	 * Indica que um dado pedido foi apreciado e foi deferido. 
	 */
	S("Apreciado"), 
	/**
	 * Indica que um dado pedido foi apreciado e foi negado. 
	 */
	N("Apreciado");

	private String label;

	ProcessoTrfApreciadoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}
