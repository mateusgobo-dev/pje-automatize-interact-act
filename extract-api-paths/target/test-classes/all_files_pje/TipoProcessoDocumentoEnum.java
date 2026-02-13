package br.jus.cnj.pje.nucleo.enums;

import br.jus.pje.nucleo.enums.PJeEnum;

public enum TipoProcessoDocumentoEnum implements PJeEnum {

	I("Intimação"), C("Citação"), M("Mandado");

	private String label;

	TipoProcessoDocumentoEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}
