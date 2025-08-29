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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_municipio", uniqueConstraints = { @UniqueConstraint(columnNames = { "id_estado",
		"ds_municipio" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_municipio", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_municipio"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler", "session", "flushMode", "persistenceContext"})
public class Municipio implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Municipio,Integer> {

	private static final long serialVersionUID = 1L;

	private int idMunicipio;
	private String municipio;
	private String codigoIbge;
	private Estado estado;
	private Boolean ativo;

	private List<Cep> cepList = new ArrayList<Cep>(0);

	public Municipio() {
	}

	@Id
	@GeneratedValue(generator = "gen_municipio")
	@Column(name = "id_municipio", unique = true, nullable = false)
	public int getIdMunicipio() {
		return this.idMunicipio;
	}

	public void setIdMunicipio(int idMunicipio) {
		this.idMunicipio = idMunicipio;
	}

	@Column(name = "ds_municipio", length = 50)
	@Length(max = 50)
	public String getMunicipio() {
		return this.municipio;
	}

	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}
	
	
	@Column(name = "id_municipio_ibge", length = 10)
	@Length(max = 10)
	public String getCodigoIbge() {
		return this.codigoIbge;
	}
	
	
	public void setCodigoIbge(String codigoIbge) {
		this.codigoIbge = codigoIbge;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estado", nullable = false)
	@NotNull
	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "municipio")
	public List<Cep> getCepList() {
		return cepList;
	}

	public void setCepList(List<Cep> cepList) {
		this.cepList = cepList;
	}

	@Override
	public String toString() {
		return municipio;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Municipio)) {
			return false;
		}
		Municipio other = (Municipio) obj;
		if (getIdMunicipio() != other.getIdMunicipio()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdMunicipio();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Municipio> getEntityClass() {
		return Municipio.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdMunicipio());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
