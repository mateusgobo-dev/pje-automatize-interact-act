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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = Area.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_area", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_area"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Area implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<Area,Integer> {

	public static final String TABLE_NAME = "tb_area";
	private static final long serialVersionUID = -8344650588392344217L;

	private int idArea;
	private String dsArea;
	private Boolean ativo;
	private Double pesoDistribuicao;
	private CentralMandado centralMandado;

	@Id
	@GeneratedValue(generator = "gen_area")
	@Column(name = "id_area", unique = true, nullable = false)
	public int getIdArea() {
		return idArea;
	}

	public void setIdArea(int idArea) {
		this.idArea = idArea;
	}

	@Column(name = "ds_area", length = 50, nullable = false)
	@NotNull
	@Length(max = 50)
	public String getDsArea() {
		return dsArea;
	}

	public void setDsArea(String dsArea) {
		this.dsArea = dsArea;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "vl_peso", nullable = false)
	@NotNull
	public Double getPesoDistribuicao() {
		return pesoDistribuicao;
	}

	public void setPesoDistribuicao(Double pesoDistribuicao) {
		this.pesoDistribuicao = pesoDistribuicao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_central_mandado", nullable = false)
	@NotNull
	public CentralMandado getCentralMandado() {
		return centralMandado;
	}

	public void setCentralMandado(CentralMandado centralMandado) {
		this.centralMandado = centralMandado;
	}

	public static String getTableName() {
		return TABLE_NAME;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return this.dsArea;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Area> getEntityClass() {
		return Area.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdArea());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
