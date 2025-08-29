package br.jus.pje.nucleo.enums;

public enum TipoCalculoMeioComunicacaoEnum implements PJeEnum {

	CD("Do cumprimento da diligência"),
	JCD("Da juntada da certidão da diligência");
	
	private String label;
	
	TipoCalculoMeioComunicacaoEnum(String label){
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

}
