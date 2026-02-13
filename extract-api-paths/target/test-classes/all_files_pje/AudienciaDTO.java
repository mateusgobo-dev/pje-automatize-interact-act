package br.jus.cnj.pje.webservice.controller.modalAudienciaLote.dto;

import java.io.Serializable;

public class AudienciaDTO implements Serializable {
	
	private static final long serialVersionUID = 1472222462031785120L;
	private String sala;
	private String dataInicio;
	
	public AudienciaDTO() {
		
	}
	
	public AudienciaDTO(String sala, String dataInicio) {
		this.sala = sala;
		this.dataInicio = dataInicio;
	}	
	
	public String getSala() {
		return sala;
	}
	
	public void setSala(String sala) {
		this.sala = sala;
	}
	
	public String getDataInicio() {
		return dataInicio;
	}
	
	public void setDataInicio(String dataInicio) {
		this.dataInicio = dataInicio;
	}

}
