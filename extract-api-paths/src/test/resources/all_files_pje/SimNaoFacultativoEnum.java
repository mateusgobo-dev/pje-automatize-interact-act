package br.jus.pje.nucleo.enums;

/**
 * Enum para verificacao de exigencia de revisor.
 */
public enum SimNaoFacultativoEnum implements PJeEnum {

	S("Sim"), N("Não"), F("Facultativo");

	private String label;

	SimNaoFacultativoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}
	
	public boolean isFacultativo() {
		return this == F;
	}
}
