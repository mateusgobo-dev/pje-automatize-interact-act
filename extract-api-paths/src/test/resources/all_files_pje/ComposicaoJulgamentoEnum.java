package br.jus.pje.nucleo.enums;

/**
 * Composicao colegiado do julgamento Integraol ou Reduzida
 */
public enum ComposicaoJulgamentoEnum implements PJeEnum {

	I("Integral"), R("Reduzida");

	private String label;

	ComposicaoJulgamentoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}
}