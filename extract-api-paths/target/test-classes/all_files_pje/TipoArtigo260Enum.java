package br.jus.pje.je.enums;

import br.jus.pje.nucleo.enums.PJeEnum;

public enum TipoArtigo260Enum implements PJeEnum {
	
	ProcessosQueCriaramCadeiaPrevencao("Processos que criaram cadeia de prevenção"), 
	ProcessosPreventos("Processo preventos pelo art. 260");

	private String label;

	TipoArtigo260Enum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}
}
