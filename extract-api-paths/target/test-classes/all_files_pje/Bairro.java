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
package br.jus.pje.nucleo.entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;
import org.hibernate.validator.constraints.Length;


@Entity
@Table(name = "tb_bairro")
@org.hibernate.annotations.GenericGenerator(name = "gen_bairro", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_bairro"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Bairro implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<Bairro,Integer> {

	private static final long serialVersionUID = 4594147880663942968L;

	private Integer idBairro;
	private Boolean ativo = Boolean.TRUE;
	private String dsBairro;
	private Area area;
	private Municipio municipio;

	private List<FaixaCep> faixasCep = new ArrayList<FaixaCep>(0);

	@Id
	@GeneratedValue(generator = "gen_bairro")
	@Column(name = "id_bairro")
	public Integer getIdBairro() {
		return idBairro;
	}

	public void setIdBairro(Integer idBairro) {
		this.idBairro = idBairro;
	}

	@Column(name = "in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "ds_bairro", length = 50, nullable = false)
	@Length(max = 50)
	@NotNull
	public String getDsBairro() {
		return dsBairro;
	}

	public void setDsBairro(String dsBairro) {
		this.dsBairro = dsBairro;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_municipio")
	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_area")
	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "bairro")
	@Cascade(value = { org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	public List<FaixaCep> getFaixasCep() {
		return faixasCep;
	}

	public void setFaixasCep(List<FaixaCep> faixasCep) {
		this.faixasCep = faixasCep;
	}

	@Override
	public String toString() {
		return this.dsBairro;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Bairro> getEntityClass() {
		return Bairro.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdBairro();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
