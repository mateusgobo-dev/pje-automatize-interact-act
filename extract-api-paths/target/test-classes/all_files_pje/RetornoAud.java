package br.jus.csjt.pje.business.service;

public class RetornoAud {

	// Inteiro com código de retorno para indicar erros, sucesso, etc.
	// (TODO: códigos de retorno a serem definidos)
	private Integer codRetorno;

	// Texto informativo relacionado ao código de retorno
	private String descricaoRetorno;

	public void setCodRetorno(Integer codRetorno) {
		this.codRetorno = codRetorno;
	}

	public Integer getCodRetorno() {
		return codRetorno;
	}

	public void setDescricaoRetorno(String descricaoRetorno) {
		this.descricaoRetorno = descricaoRetorno;
	}

	public String getDescricaoRetorno() {
		return descricaoRetorno;
	}

}
