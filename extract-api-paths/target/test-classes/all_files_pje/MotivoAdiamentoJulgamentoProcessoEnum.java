package br.jus.pje.nucleo.enums;

public enum MotivoAdiamentoJulgamentoProcessoEnum {

	RELAT_AUSENTE("Relator Ausente"),
	REVIS_AUSENTE("Revisor Ausente"),
	QUORUM_INSUFI("Quorum Insuficiente");

	private String label;

	MotivoAdiamentoJulgamentoProcessoEnum(String label) {
		this.label = label;
	}
	
	
	public String getLabel() {
		return this.label;
	}

}