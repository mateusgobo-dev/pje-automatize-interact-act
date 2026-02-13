package br.jus.pje.nucleo.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TipoPrisaoDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long id;
	private String tipoPrisao;
	private Boolean ativo;
	
	public TipoPrisaoDTO() {
		super();
	}

	public TipoPrisaoDTO(Long id, String tipoPrisao, Boolean ativo) {
		super();
		this.id = id;
		this.tipoPrisao = tipoPrisao;
		this.ativo = ativo;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getTipoPrisao() {
		return tipoPrisao;
	}
	
	public void setTipoPrisao(String tipoPrisao) {
		this.tipoPrisao = tipoPrisao;
	}
	
	public Boolean getAtivo() {
		return ativo;
	}
	
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
}
