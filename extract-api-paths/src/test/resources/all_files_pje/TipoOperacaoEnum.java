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
package br.jus.pje.jt.entidades;

/**
 * Enum criado para representar os tipos de operações possíveis no cadastro de
 * um débito trabalhista (inclusão, alteração e exclusão)
 * 
 * @author Kelly Leal
 * 
 */
public enum TipoOperacaoEnum {

	I("Inclusão", "I"), A("Alteração", "A"), E("Exclusão", "E");

	private String label;
	private String cod;

	TipoOperacaoEnum(String label, String cod) {
		this.label = label;
		this.cod = cod;
	}

	public String getLabel() {
		return this.label;
	}

	@Override
	public String toString() {
		return label;
	}

	public String getCod() {
		return cod;
	}

}