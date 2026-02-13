/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.jt.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vs_aud_conf")
//@PrimaryKeyJoinColumn(name = "id_municipio")
public class AudConf implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcesso;
	private int idMunicipio;
	private String nomeMunicipio;
	private int vara;
	private String uf;

	@Id
	@Column(name = "id_processo_trf", nullable = false, insertable = false, updatable = false)
	public int getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(int idProcesso) {
		this.idProcesso = idProcesso;
	}

	@Column(name = "id_municipio", nullable = false, insertable = false, updatable = false)
	public int getIdMunicipio() {
		return idMunicipio;
	}

	public void setIdMunicipio(int idMunicipio) {
		this.idMunicipio = idMunicipio;
	}

	@Column(name = "ds_municipio", insertable = false, updatable = false)
	public String getNomeMunicipio() {
		return nomeMunicipio;
	}

	public void setNomeMunicipio(String nomeMunicipio) {
		this.nomeMunicipio = nomeMunicipio;
	}

	@Column(name = "nr_vara", insertable = false, updatable = false)
	public int getVara() {
		return vara;
	}

	public void setVara(int vara) {
		this.vara = vara;
	}

	@Column(name = "cd_estado", insertable = false, updatable = false)
	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

}
