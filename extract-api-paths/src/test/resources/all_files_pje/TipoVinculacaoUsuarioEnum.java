package br.jus.pje.nucleo.enums;

public enum TipoVinculacaoUsuarioEnum implements PJeEnum {

	EGA("Estrutura de Gabinete"),
	COL("Estrutura de Secretaria"),
	O("Outros");

	private String label;

	TipoVinculacaoUsuarioEnum(String label) {
		this.label = label;
	}
	
	
	public String getLabel() {
		return this.label;
	}

}