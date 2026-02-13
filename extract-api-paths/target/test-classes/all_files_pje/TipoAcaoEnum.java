package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.enums;

import br.jus.pje.nucleo.enums.PJeEnum;

/**
 * Enum TipoAcaoEnum.
 * 
 * @author Adriano Pamplona
 **/
public enum TipoAcaoEnum implements PJeEnum {

	I("Individual"), C("Coletiva");

	private String label;

	TipoAcaoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	/**
	 * Retorna e enum do código passado por parametro.
	 * 
	 * @param codigo Codigo do enum. 
	 * @return Enum do código passado por parametro.
	 */
	public static TipoAcaoEnum get(String codigo) {
		TipoAcaoEnum resultado = null;
		
		TipoAcaoEnum[] enuns = values();
		for (int indice = 0; indice < enuns.length && resultado == null; indice++) {
			TipoAcaoEnum temp = enuns[indice];
			if (temp.name().equals(codigo)) {
				resultado = temp;
			}
		}

		return resultado;
	}
}
