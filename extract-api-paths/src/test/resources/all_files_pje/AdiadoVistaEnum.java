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

public enum AdiadoVistaEnum {

	AD("Adiado"), PV("Pedido de Vista");

	private String label;

	AdiadoVistaEnum(String label) {
		this.label = label;
	}
	
	/**
	 * Método para verificar se a String descricaoAdiamento está contida no Enum
	 * retornando true caso esteja contida.
	 * 
	 * @param string
	 *            descricaoAdiamento do enum ou label que será verificado.
	 * @return boolean
	 */
	public static boolean isComtem(String descricaoAdiamento) {
		for (AdiadoVistaEnum item : values()) {
			if (item.name().equals(descricaoAdiamento)) {
				return true;
			}

		}
		return false;
	}

	/**
	 * Retorna o AdiadoVistaEnum pelo valor passado por descrição do adiamento.
	 * Se não pertençe ao domínio retorna null.
	 * 
	 * @param string
	 *            descricaoAdiamento do enum ou label.
	 * @return AdiadoVistaEnum
	 */

	public static AdiadoVistaEnum getEnum(String descricaoAdiamento) {
		for (AdiadoVistaEnum item : values()) {
			if (item.name().equals(descricaoAdiamento)) {
				return item;
			}
		}
		return null;
	}

	public String getLabel() {
		return this.label;
	}

}
