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



public enum TipoDocumentoEnum implements PJeEnum {

	/**
	 * Tipo de documento que deve ser digitado no sistema. 
	 */
	P("Texto"), 
	
	/**
	 * Tipo de documento cuja forma de inserção é, unicamente, por upload. 
	 */
	D("Documento"), 
	
	/**
	 * Tipo de documento que pode ser digitado no sistema ou ser inserido por upload. 
	 */
	T("Todos"),	
	
	/**
	 * Tipo de documento que é utilizado na integração do BNMPII
	 */
	B("Bnmp"),	
	
	/**
	 * Tipo de documento classificável como expediente.
	 */
	@Deprecated
	E("Expediente");//TODO: remover!

	private String label;

	TipoDocumentoEnum(String label) {
		this.label = label;
	}

	/* (non-Javadoc)
	 * @see br.jus.pje.nucleo.enums.PJeEnum#getLabel()
	 */
	@Override
	public String getLabel() {
		return this.label;
	}

}
