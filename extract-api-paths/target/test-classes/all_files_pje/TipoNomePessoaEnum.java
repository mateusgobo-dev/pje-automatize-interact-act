package br.jus.pje.nucleo.enums;

public enum TipoNomePessoaEnum implements PJeEnum {

	C("Civil"), S("Social"), D("Documento de identificacao"), A("Alternativo");

	private String label;

	TipoNomePessoaEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}
