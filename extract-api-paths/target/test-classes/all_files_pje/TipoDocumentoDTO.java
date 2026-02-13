package br.jus.pje.nucleo.dto.portal;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

public class TipoDocumentoDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@NotNull
	private String codigo;
	
	@NotNull
	private String descricao;
	
	public String getCodigo() {
		return codigo;
	}
	
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	
	public String getDescricao() {
		return descricao;
	}
	
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}
