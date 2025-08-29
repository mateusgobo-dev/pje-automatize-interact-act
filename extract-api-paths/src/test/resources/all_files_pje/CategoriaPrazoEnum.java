package br.jus.pje.nucleo.enums;

public enum CategoriaPrazoEnum implements PJeEnum {
	
	/**
	 * Prazo continuo, o prazo continuo será calculado computando os dias uteis, finais de semana, feriados e indisponibilidades do sistema. 
	 */
	C("Contínuo"), 
	
	/**
	 * Prazo em dias uteis, o prazo em dias uteis sera calculado computando somente o dias uteis portanto desconsiderando os finais de semana, feriados e dias com suspensao de prazo.
	 */
	U("Dias uteis");

	private String label;
	
	private CategoriaPrazoEnum(String label) {
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return label;
	}
}