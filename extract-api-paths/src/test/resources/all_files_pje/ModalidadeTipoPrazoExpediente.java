package br.jus.cnj.intercomunicacao.v222.beans;

public enum ModalidadeTipoPrazoExpediente {
	H("Hora"), D("dia"), M("mês"), A("Ano");
	
	private String label;
	
	private ModalidadeTipoPrazoExpediente(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
}
