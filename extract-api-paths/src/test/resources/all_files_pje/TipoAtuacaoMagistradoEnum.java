package br.jus.pje.nucleo.enums;

public enum TipoAtuacaoMagistradoEnum {

	RELAT("Relator"),
	REVIS("Revisor"),
	VOGAL("Vogal");
	
	private String label;

	TipoAtuacaoMagistradoEnum(String label) {
		this.label = label;
	}
	
	
	public String getLabel() {
		return this.label;
	}

}