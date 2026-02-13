package br.jus.pje.nucleo.enums;

public enum SituacaoPublicacaoDiarioEnum implements PJeEnum {

	A("Aguardando publicação"), 
	P("Publicada"),
	F("Falhou"),
	C("Cancelada");

	private String label;
	
	SituacaoPublicacaoDiarioEnum(String label){
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

}


