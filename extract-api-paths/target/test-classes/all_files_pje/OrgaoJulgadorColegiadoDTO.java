package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgaoJulgadorColegiadoDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private int idOrgaoJulgadorColegiado;
	private String orgaoJulgadorColegiado;
	
	public OrgaoJulgadorColegiadoDTO() {
		super();
	}
	
	public OrgaoJulgadorColegiadoDTO(int idOrgaoJulgadorColegiado, String orgaoJulgadorColegiado) {
		this.idOrgaoJulgadorColegiado = idOrgaoJulgadorColegiado;
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	public int getIdOrgaoJulgadorColegiado() {
		return idOrgaoJulgadorColegiado;
	}

	public void setIdOrgaoJulgadorColegiado(int idOrgaoJulgadorColegiado) {
		this.idOrgaoJulgadorColegiado = idOrgaoJulgadorColegiado;
	}

	public String getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(String orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	
}
