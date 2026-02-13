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
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.nucleo.entidades.Estado;

@Entity
@Table(name = MunicipioIBGE.TABLE_NAME)
public class MunicipioIBGE implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8201936352291165018L;

	public static final String TABLE_NAME = "vs_municipio_ibge";

	private int idMunicipio;
	private Estado uf;
	private String nomeMunicipio;
	private String codigoMunicipioIBGE;
	private Date dataAtualizacao;
	private Boolean ativo;

	@Id
	@Column(name = "id_municipio", unique = true, nullable = false)
	@NotNull
	public int getIdMunicipio() {
		return idMunicipio;
	}

	public void setIdMunicipio(int idMunicipio) {
		this.idMunicipio = idMunicipio;
	}

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "id_uf", nullable = false)
	@ForeignKey(name = "fk_tb_municipio_ibge_tb_estado")
	@NotNull
	public Estado getUf() {
		return uf;
	}

	public void setUf(Estado uf) {
		this.uf = uf;
	}

	@Column(name = "nome_municipio", nullable = false)
	@NotNull
	public String getNomeMunicipio() {
		return nomeMunicipio;
	}

	public void setNomeMunicipio(String nomeMunicipio) {
		this.nomeMunicipio = nomeMunicipio;
	}

	@Column(name = "id_municipio_ibge", nullable = false)
	@NotNull
	public String getCodigoMunicipioIBGE() {
		return codigoMunicipioIBGE;
	}

	public void setCodigoMunicipioIBGE(String codigoMunicipioIBGE) {
		this.codigoMunicipioIBGE = codigoMunicipioIBGE;
	}

	@Column(name = "dt_atualizacao", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return nomeMunicipio;
	}
}
