package br.jus.cnj.pje.webservice.controller.modalPericiaLote.dto;

import java.io.Serializable;

public class PericiaDTO implements Serializable {

	private static final long serialVersionUID = 8998980582522573830L;
	private String perito;
	private String data;
	private String hora;
	
	public PericiaDTO() {
		
	}
	
	public PericiaDTO(String perito, String data, String hora) {
		this.perito = perito;
		this.data = data;
		this.hora = hora;
	}
	
	public String getPerito() {
		return perito;
	}
	
	public void setPerito(String perito) {
		this.perito = perito;
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	public String getHora() {
		return hora;
	}
	
	public void setHora(String hora) {
		this.hora = hora;
	}
	
}
