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
@Table(name = "vs_aud_orgao_municipio")
public class AudOrgaoMunicipio implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idOrgaoJulgador;
	private String nomeOrgaoJulgador;
	private String orgaoJulgadorAtivo;
	private int idLocalizacao;
	private String nomeLocalizacao;
	private int idMunicipio;
	private String nomeMunicipio;
	private String nomeUF;
	private String siglaUF;

	@Id
	@Column(name = "id_orgao_julgador", nullable = false, insertable = false, updatable = false)
	public int getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}

	public void setIdOrgaoJulgador(int idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
	}

	@Column(name = "ds_orgao_julgador", insertable = false, updatable = false)
	public String getNomeOrgaoJulgador() {
		return nomeOrgaoJulgador;
	}

	public void setNomeOrgaoJulgador(String nomeOrgaoJulgador) {
		this.nomeOrgaoJulgador = nomeOrgaoJulgador;
	}

	@Column(name = "in_ativo", insertable = false, updatable = false)
	public String getOrgaoJulgadorAtivo() {
		return orgaoJulgadorAtivo;
	}

	public void setOrgaoJulgadorAtivo(String orgaoJulgadorAtivo) {
		this.orgaoJulgadorAtivo = orgaoJulgadorAtivo;
	}

	@Column(name = "id_localizacao", insertable = false, updatable = false)
	public int getIdLocalizacao() {
		return idLocalizacao;
	}

	public void setIdLocalizacao(int idLocalizacao) {
		this.idLocalizacao = idLocalizacao;
	}

	@Column(name = "ds_localizacao", insertable = false, updatable = false)
	public String getNomeLocalizacao() {
		return nomeLocalizacao;
	}

	public void setNomeLocalizacao(String nomeLocalizacao) {
		this.nomeLocalizacao = nomeLocalizacao;
	}

	@Column(name = "id_municipio", insertable = false, updatable = false)
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

	@Column(name = "ds_estado", insertable = false, updatable = false)
	public String getNomeUF() {
		return nomeUF;
	}

	public void setNomeUF(String nomeUF) {
		this.nomeUF = nomeUF;
	}

	@Column(name = "cd_estado", insertable = false, updatable = false)
	public String getSiglaUF() {
		return siglaUF;
	}

	public void setSiglaUF(String siglaUF) {
		this.siglaUF = siglaUF;
	}

}
