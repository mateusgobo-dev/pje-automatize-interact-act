package br.jus.pje.nucleo.enums;

public enum TipoUsuarioExternoEnum implements PJeEnum {
	A("Advogado"),
	AA("Assistente de advogado"),
	P("Procurador"),
	AP("Assistente de procuradoria"),
	O("Outro");

	private String label;

	TipoUsuarioExternoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}