package br.jus.pje.nucleo.enums;

public enum CompetenciaEnum implements PJeEnum {
	
	ADMINISTRATIVA("Administrativa"),
	EXCLUSIVA_PRESIDENCIA("Exclusiva da presidência"),
	GERAL_ELEITORAL("Geral eleitoral"),
	JUIZES_AUXILIARES("Juízes Auxiliares"),
	CORREGEDOR_GERAL_ELEITORAL("Corregedor(a)-Geral Eleitoral");
	
	private String label;

	CompetenciaEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}	
}
