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


public enum TipoResultadoAvisoRecebimentoEnum implements PJeEnum {

	/**
	 * Houve recebimento do objeto postal de comunicação
	 */
	R("Recebido"), 
	/**
	 * Não houve recebimento do objeto postal de comunicação em razão de mudança 
	 */
	M("Mudou-se"), 
	/**
	 * Não houve recebimento do objeto postal de comunicação por o endereço ser insuficiente 
	 */
	E("Endereço insuficiente"), 
	/**
	 * Não houve recebimento do objeto postal de comunicação por o endereço inexistir
	 */
	N("Não existe o número "), 
	/**
	 * Não houve recebimento do objeto postal de comunicação por o destinatário ser desconhecido no endereço 
	 */
	D("Desconhecido"), 
	/**
	 * Não houve recebimento do objeto postal de comunicação por recusa. 
	 */
	C("Recusado"), 
	/**
	 * Não houve recebimento do objeto postal de comunicação por ele estar em posta restante e não ter havido procura. 
	 */
	P("Não procurado"), 
	/**
	 * Não houve recebimento do objeto postal de comunicação por ausência do destinatário. 
	 */
	A("Ausente"), 
	/**
	 * Não houve recebimento do objeto postal de comunicação por falecimento do destinatário. 
	 */
	F("Falecido"), 
	/**
	 * Não houve recebimento do objeto postal de comunicação por razão não catalogada. 
	 */
	O("Outros ");

	private String label;

	TipoResultadoAvisoRecebimentoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}
