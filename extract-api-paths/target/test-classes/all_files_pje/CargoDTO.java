package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;

public class CargoDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long idCargo;
	private String nomeCargo;
	
	public CargoDTO() {
		super();
	}

	public CargoDTO(Long idCargo, String nomeCargo) {
		super();
		this.idCargo = idCargo;
		this.nomeCargo = nomeCargo;
	}

	public Long getIdCargo() {
		return idCargo;
	}

	public void setIdCargo(Long idCargo) {
		this.idCargo = idCargo;
	}

	public String getNomeCargo() {
		return nomeCargo;
	}

	public void setNomeCargo(String nomeCargo) {
		this.nomeCargo = nomeCargo;
	}
	
	

	
}
