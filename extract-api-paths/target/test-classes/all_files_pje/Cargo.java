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

@Entity
@javax.persistence.Cacheable(true)
@Table(name = Cargo.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_cargo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_cargo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Cargo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Cargo,Integer> {

	public static final String TABLE_NAME = "tb_cargo";
	private static final long serialVersionUID = 1L;

	private int idCargo;
	private String cargo;
	private Boolean ativo;
	private String sigla;

	public Cargo() {
	}

	@Id
	@GeneratedValue(generator = "gen_cargo")
	@Column(name = "id_cargo", unique = true, nullable = false)
	public int getIdCargo() {
		return this.idCargo;
	}

	public void setIdCargo(int idCargo) {
		this.idCargo = idCargo;
	}

	@Column(name = "ds_cargo", length = 100, nullable = false, unique = true)
	@Length(max = 100)
	@NotNull
	public String getCargo() {
		return this.cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "cd_cargo", nullable = false, length = 5, unique = true)
	@Length(max = 5)
	public String getSigla() {
		return this.sigla;
	}

	public void setSigla(String sigla) {
		this.sigla = sigla;
	}

	@Override
	public String toString() {
		return cargo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Cargo)) {
			return false;
		}
		Cargo other = (Cargo) obj;
		if (getIdCargo() != other.getIdCargo()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdCargo();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Cargo> getEntityClass() {
		return Cargo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdCargo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
