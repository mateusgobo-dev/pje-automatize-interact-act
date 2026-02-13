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


/**
 * Indica a participação de uma pessoa em um processo, pode ser utilizado para classificar a participação de uma pessoa avulsa qualquer com relação ao processo
 *
 */
public enum ProcessoParteParticipacaoEnum implements PJeEnum {

	/**
	 * O polo ativo de um processo judicial. 
	 */
	A("Ativo"), 
	/**
	 * O polo passivo de um processo judicial 
	 */
	P("Passivo"), 
	/**
	 * Vinculado ao processo como Outros participantes
	 */
	T("Outros participantes"),
	/**
	 * Pessoas não vinculadas ao processo
	 */
	N("Não vinculados");

	private String label;

	ProcessoParteParticipacaoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}
