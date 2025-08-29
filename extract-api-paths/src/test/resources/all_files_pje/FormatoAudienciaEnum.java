package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.enums;

import br.jus.pje.nucleo.enums.PJeEnum;

/**
 * Enum FormatoAudienciaEnum.
 * 
 * @author Adriano Pamplona
 **/
public enum FormatoAudienciaEnum implements PJeEnum {

	V("Virtual"), P("Presencial");

	private String label;

	FormatoAudienciaEnum(String label) {
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
	public static FormatoAudienciaEnum get(String codigo) {
		FormatoAudienciaEnum resultado = null;
		
		FormatoAudienciaEnum[] enuns = values();
		for (int indice = 0; indice < enuns.length && resultado == null; indice++) {
			FormatoAudienciaEnum temp = enuns[indice];
			if (temp.name().equals(codigo)) {
				resultado = temp;
			}
		}

		return resultado;
	}
	
}
