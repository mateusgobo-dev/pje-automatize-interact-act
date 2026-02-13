package br.jus.pje.nucleo.enums;


public enum SessaoResultadoVotacaoEnum {

	UN("Relator do Processo - Unânime"), NU("Relator do processo - Maioria"), NR("Não relator");
	
	private String label;
	
	SessaoResultadoVotacaoEnum(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return this.label;
	}

}
