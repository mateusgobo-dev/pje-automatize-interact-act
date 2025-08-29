package br.jus.cnj.pje.webservice.controller.modalAudienciaLote.dto;

import java.io.Serializable;

public class TipoAudienciaDTO implements Serializable {
	
	private static final long serialVersionUID = -1363625834878637127L;
	
	private int id;
	private String tipo;
	
	public TipoAudienciaDTO() {
		
	}
	
	public TipoAudienciaDTO(Integer id, String tipo) {
		this.id = id;
		this.tipo = tipo;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getTipo() {
		return tipo;
	}
	
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
}
