package br.jus.pje.nucleo.enums;

public enum SituacaoSessaoEnum {

	ATIVA("Ativa"), INATIVA("Inativa");
	
	private String label;

	SituacaoSessaoEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}
