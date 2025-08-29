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
 * Interface definidora de função auxiliar para recuperação de descrição
 * de uma enumeração.
 * 
 * @author CNJ
 *
 */
public interface PJeEnum {

	/**
	 * Recupera o texto de exibição padrão de um valor enumerado.
	 * 
	 * @return o texto de exibição.
	 */
	public String getLabel();

}
