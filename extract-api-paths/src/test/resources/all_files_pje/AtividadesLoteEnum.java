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


public enum AtividadesLoteEnum implements PJeEnum{

	/**
	 * Atividade de movimentação de processos de uma tarefa para outra em lote 
	 */
	M("Movimentação em lote"), 

	/**
	 * Atividade de assinatura de minutas de documentos em lote 
	 */
	A("Assinatura em lote"), 

	/**
	 * Atividade de criação de minutas de documentos em lote 
	 */
	E("Minutar em lote"), 

	/**
	 * Atividade de criação de intimações em lote 
	 */
	I("Intimar em lote"), 
	
	/**
	 * Atividade de assinatura de inteiro teor em lote 
	 */
	T("Assinar inteiro teor em lote"),
	
	/**
	 * Atividade de lançamento de movimentos processuais em lote 
	 */
	MM("Lançar movimentações em lote"),
	
	/**
	 * Atividade de designar audiências em lote 
	 */
	DA("Designar audiência em lote"),
	
	/**
	 * Atividade de designar perícias em lote 
	 */
	DP("Designar perícia em lote"),

	/**
	 * RENAJUD em lote 
	 */
	RE("RENAJUD em lote");

	private String label;

	AtividadesLoteEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}
