package br.jus.pje.jt.enums;

import br.jus.pje.nucleo.enums.PJeEnum;

public enum TipoMetodoHabilitacaoEnum implements PJeEnum {
	A("Automática"),
	M("Manual");
	
	private String label;
	
	TipoMetodoHabilitacaoEnum(String label){
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return label;
	}

}