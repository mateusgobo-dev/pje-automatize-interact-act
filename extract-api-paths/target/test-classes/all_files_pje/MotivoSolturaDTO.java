package br.jus.pje.nucleo.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MotivoSolturaDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long id;
	private String motivoSoltura;
	private Boolean ativo;
	
	public MotivoSolturaDTO() {
		super();
	}

	public MotivoSolturaDTO(Long id, String motivoSoltura, Boolean ativo) {
		super();
		this.id = id;
		this.motivoSoltura = motivoSoltura;
		this.ativo = ativo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMotivoSoltura() {
		return motivoSoltura;
	}

	public void setMotivoSoltura(String motivoSoltura) {
		this.motivoSoltura = motivoSoltura;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

}
