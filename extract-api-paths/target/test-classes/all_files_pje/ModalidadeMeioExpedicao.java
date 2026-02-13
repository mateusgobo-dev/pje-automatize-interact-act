package br.jus.cnj.intercomunicacao.v222.beans;

public enum ModalidadeMeioExpedicao {
	P("DiarioEletronico"),
	M("Central de Mandados"),
	E("Enviar Via Sistema"),
	C("Correios"),
	L("Carta"),
	D("Edital");
	
	private String label;
	
	private ModalidadeMeioExpedicao(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	
}
