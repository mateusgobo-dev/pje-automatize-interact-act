package br.jus.pje.nucleo.enums;

public enum PlataformaDispositivoEnum {
	
	A("Android"), I("IOS");

	private String label;

	PlataformaDispositivoEnum(String label) {
		this.label = label;
	}


	public String getLabel() {
		return label;
	}

}
