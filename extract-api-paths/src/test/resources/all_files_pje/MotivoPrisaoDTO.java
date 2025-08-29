package br.jus.pje.nucleo.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MotivoPrisaoDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long id;
	private String motivoPrisao;
	private Boolean ativo;

	public MotivoPrisaoDTO() {
		super();
	}

	public MotivoPrisaoDTO(Long id, String motivoPrisao, Boolean ativo) {
		super();
		this.id = id;
		this.motivoPrisao = motivoPrisao;
		this.ativo = ativo;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getMotivoPrisao() {
		return motivoPrisao;
	}
	
	public void setMotivoPrisao(String motivoPrisao) {
		this.motivoPrisao = motivoPrisao;
	}
	
	public Boolean getAtivo() {
		return ativo;
	}
	
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
}
