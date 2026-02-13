package br.jus.cnj.pje.webservice.controller.modalPericiaLote.dto;

import java.io.Serializable;

public class PeritoDTO implements Serializable {

	private static final long serialVersionUID = -3379758236858802649L;
	
	public PeritoDTO() {

	}
	
	public PeritoDTO(Integer id, String nome) {
		this.id = id;
		this.nome = nome;
	}
	
	private Integer id;
	private String nome;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

}
