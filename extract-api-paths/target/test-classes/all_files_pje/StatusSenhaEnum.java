package br.jus.pje.nucleo.enums;

public enum StatusSenhaEnum implements PJeEnum {
	
	A("Ativa"), I("Inativa"), B("Bloqueada"), M("Migrada");
	
	private String label;
	
	StatusSenhaEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}
}
