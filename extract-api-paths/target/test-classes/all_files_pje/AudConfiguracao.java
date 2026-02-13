package br.jus.csjt.pje.business.service;

import java.io.Serializable;
import java.util.List;

public class AudConfiguracao implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer orgaoJulgador;
	private String nomeMunicipio;
	private String uf;
	private Integer numeroVara;
	private List<String> nomeDiretores;

	public Integer getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(Integer orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public String getNomeMunicipio() {
		return nomeMunicipio;
	}

	public void setNomeMunicipio(String nomeMunicipio) {
		this.nomeMunicipio = nomeMunicipio;
	}

	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	public Integer getNumeroVara() {
		return numeroVara;
	}

	public void setNumeroVara(Integer numeroVara) {
		this.numeroVara = numeroVara;
	}

	public List<String> getNomeDiretores() {
		return nomeDiretores;
	}

	public void setNomeDiretores(List<String> nomeDiretores) {
		this.nomeDiretores = nomeDiretores;
	}
}