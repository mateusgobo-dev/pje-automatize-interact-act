package br.jus.pje.nucleo.enums;

public enum TipoNomeAlternativoEnum implements PJeEnum{
	
	O("Outros nomes"), A("Alcunha");

	private String label;

	TipoNomeAlternativoEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}
	
}
