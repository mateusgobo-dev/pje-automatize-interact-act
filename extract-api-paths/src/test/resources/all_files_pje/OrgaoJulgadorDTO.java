package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;

public class OrgaoJulgadorDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private Integer idOrgaoJulgador;
	private String nomeOrgaoJulgador;
	
	public OrgaoJulgadorDTO() {
		super();
	}

	public OrgaoJulgadorDTO(Integer idOrgaoJulgador, String nomeOrgaoJulgador) {
		super();
		this.idOrgaoJulgador = idOrgaoJulgador;
		this.nomeOrgaoJulgador = nomeOrgaoJulgador;
	}

	public Integer getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}
	
	public void setIdOrgaoJulgador(Integer idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
	}
	
	public String getNomeOrgaoJulgador() {
		return nomeOrgaoJulgador;
	}
	
	public void setNomeOrgaoJulgador(String nomeOrgaoJulgador) {
		this.nomeOrgaoJulgador = nomeOrgaoJulgador;
	}
	
}
