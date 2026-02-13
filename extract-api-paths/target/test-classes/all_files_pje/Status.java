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
@Table(name = Status.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_status", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_status"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Status implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Status,Integer> {

	public static final String TABLE_NAME = "tb_status";
	private static final long serialVersionUID = 1L;

	private int idStatus;
	private String status;
	private Boolean ativo;
	private Boolean mensuravel;

	@Id
	@GeneratedValue(generator = "gen_status")
	@Column(name = "id_status", unique = true, nullable = false)
	public int getIdStatus() {
		return this.idStatus;
	}

	public void setIdStatus(int idStatus) {
		this.idStatus = idStatus;
	}

	@Column(name = "ds_status", length = 100, unique = true, nullable = false)
	@Length(max = 100)
	@NotNull
	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "in_mensuravel", nullable = false)
	@NotNull
	public Boolean getMensuravel() {
		return mensuravel;
	}

	public void setMensuravel(Boolean mensuravel) {
		this.mensuravel = mensuravel;
	}

	@Override
	public String toString() {
		return status;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Status)) {
			return false;
		}
		Status other = (Status) obj;
		if (getIdStatus() != other.getIdStatus()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdStatus();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Status> getEntityClass() {
		return Status.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdStatus());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
