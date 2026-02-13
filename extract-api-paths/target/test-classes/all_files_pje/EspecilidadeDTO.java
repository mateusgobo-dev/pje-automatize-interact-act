package br.jus.cnj.pje.webservice.controller.modalPericiaLote.dto;

import java.io.Serializable;

public class EspecilidadeDTO implements Serializable {

	private static final long serialVersionUID = -3098440064543272321L;
	
	private Integer id;
	private String nome;
	
	public EspecilidadeDTO() {
		
	}
	
	public EspecilidadeDTO(Integer id, String nome) {
		this.id = id;
		this.nome = nome;
	}
	
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
