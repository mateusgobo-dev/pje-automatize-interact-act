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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = EstadoCivil.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_estado_civil", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_estado_civil"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler", "session", "flushMode", "persistenceContext"})
public class EstadoCivil implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<EstadoCivil,Integer> {

	public static final String TABLE_NAME = "tb_estado_civil";
	private static final long serialVersionUID = 1L;

	private int idEstadoCivil;
	private String estadoCivil;
	private Boolean ativo;

	public EstadoCivil() {
	}

	@Id
	@GeneratedValue(generator = "gen_estado_civil")
	@Column(name = "id_estado_civil", unique = true, nullable = false)
	public int getIdEstadoCivil() {
		return this.idEstadoCivil;
	}

	public void setIdEstadoCivil(int idEstadoCivil) {
		this.idEstadoCivil = idEstadoCivil;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "ds_estado_civil", nullable = false, length = 100, unique = true)
	@NotNull
	@Length(max = 100)
	public String getEstadoCivil() {
		return this.estadoCivil;
	}

	public void setEstadoCivil(String estadoCivil) {
		this.estadoCivil = estadoCivil;
	}

	@Override
	public String toString() {
		return estadoCivil;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof EstadoCivil)) {
			return false;
		}
		EstadoCivil other = (EstadoCivil) obj;
		if (getIdEstadoCivil() != other.getIdEstadoCivil()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdEstadoCivil();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends EstadoCivil> getEntityClass() {
		return EstadoCivil.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEstadoCivil());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
