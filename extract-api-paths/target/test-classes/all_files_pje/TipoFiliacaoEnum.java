package br.jus.pje.nucleo.enums;

public enum TipoFiliacaoEnum implements PJeEnum{
	
	M("Mãe"), P("Pai");

	private String label;

	TipoFiliacaoEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}	

}
