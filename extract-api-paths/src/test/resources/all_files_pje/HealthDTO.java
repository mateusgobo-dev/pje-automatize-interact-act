package br.jus.cnj.pje.webservice.controller.status.dto;

import java.io.Serializable;

public class HealthDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public static final String STATUS_UP = "UP";
	public static final String STATUS_DOWN = "DOWN";

	public String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
