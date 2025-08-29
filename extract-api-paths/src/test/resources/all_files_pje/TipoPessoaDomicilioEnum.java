package br.jus.pje.nucleo.enums.domicilioeletronico;

public enum TipoPessoaDomicilioEnum {
	JURIDICA("CNPJ"), FISICA("CPF");

	private final String tipoDocumento;

	TipoPessoaDomicilioEnum(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}
}
