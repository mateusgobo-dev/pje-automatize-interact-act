package br.jus.pje.nucleo.enums;

public enum ContagemPrazoEnum implements PJeEnum {
	
	/**
	 * Contagem para cálculo do prazo de ciência 
	 */
	C("Ciência"), 
	
	/**
	 * Contagem para cálculo do prazo de manifestação 
	 */
	M("Manifestação");

	private String label;
	
	private ContagemPrazoEnum(String label) {
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return label;
	}
}