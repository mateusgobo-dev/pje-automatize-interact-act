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
 * Enum criado para representar os tipos de solução da sentença Diferenciada:
 * Cada parte do processo recebe uma diferente solução Única: Todas as partes do
 * processo recebem a mesma solução
 * 
 * @author Kelly Leal, Rafael Barros
 * 
 */
public enum TipoSolucaoEnum {

	D("Diferenciada"), U("Única");

	private String label;

	TipoSolucaoEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
}
