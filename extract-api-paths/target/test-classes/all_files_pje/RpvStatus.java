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
@Table(name = "tb_rpv_status")
@org.hibernate.annotations.GenericGenerator(name = "gen_rpv_status", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_rpv_status"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RpvStatus implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RpvStatus,Integer> {

	private static final long serialVersionUID = 1L;

	private int idRpvStatus;
	private String rpvStatus;
	private Boolean ativo;

	public RpvStatus() {
	}

	@Id
	@GeneratedValue(generator = "gen_rpv_status")
	@Column(name = "id_rpv_status", unique = true, nullable = false)
	public int getIdRpvStatus() {
		return this.idRpvStatus;
	}

	public void setIdRpvStatus(int idRpvStatus) {
		this.idRpvStatus = idRpvStatus;
	}

	@Column(name = "ds_rpv_status", nullable = false, length = 50)
	@NotNull
	@Length(max = 50)
	public String getRpvStatus() {
		return rpvStatus;
	}

	public void setRpvStatus(String rpvStatus) {
		this.rpvStatus = rpvStatus;
	}

	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return rpvStatus;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RpvStatus)) {
			return false;
		}
		RpvStatus other = (RpvStatus) obj;
		if (getIdRpvStatus() != other.getIdRpvStatus()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdRpvStatus();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RpvStatus> getEntityClass() {
		return RpvStatus.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRpvStatus());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
