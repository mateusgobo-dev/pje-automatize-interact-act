package br.jus.pje.nucleo.enums;

import br.jus.pje.nucleo.enums.PJeEnum;

public enum RegraImpedimentoSuspeicaoEnum implements PJeEnum {
	
	P("Pela parte"), U("Por estado"), E("Por ano de eleição"), A("Pelo advogado");

	private String label;
	
	RegraImpedimentoSuspeicaoEnum(String label) {
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return this.label;
	}

}