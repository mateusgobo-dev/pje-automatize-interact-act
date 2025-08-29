package br.jus.pje.nucleo.enums;

public enum ClasseComposicaoJulgamentoEnum implements PJeEnum {

	I("Integral"), R("Reduzida"), F("Facultativo");

	private String label;

	ClasseComposicaoJulgamentoEnum(String label) {
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