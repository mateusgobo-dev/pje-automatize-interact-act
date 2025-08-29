package br.jus.pje.nucleo.enums;

import java.util.Arrays;
import java.util.List;

public enum RestricaoProtocoloCompetenciaEnum implements PJeEnum{

	TD("Todos"),
	AOJC("Apenas dos OJs e OJCs"), 
	AOJ("Apenas dos OJs");

	private String label;

	RestricaoProtocoloCompetenciaEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	/**
	 * Recupera a lista contendo os itens do enum.
	 * @return List<RestricaoProtocoloCompetenciaEnum>
	 */
	public static List<RestricaoProtocoloCompetenciaEnum> list() {
		return Arrays.asList(RestricaoProtocoloCompetenciaEnum.values());
	}
	
}