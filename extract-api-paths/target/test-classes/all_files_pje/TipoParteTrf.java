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

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_tipo_parte_trf")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_parte_trf", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_parte_trf"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@Cacheable
public class TipoParteTrf implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoParteTrf,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoParteTrf;
	private Integer codigoTipoParteTrf;
	private String tipoParteTrf;
	private Boolean ativo;

	public TipoParteTrf() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_parte_trf")
	@Column(name = "id_tipo_parte_trf", unique = true, nullable = false)
	public int getIdTipoParteTrf() {
		return this.idTipoParteTrf;
	}

	public void setIdTipoParteTrf(int idTipoParteTrf) {
		this.idTipoParteTrf = idTipoParteTrf;
	}

	@Column(name = "cd_tipo_parte_trf")
	public int getCodigoTipoParteTrf() {
		return this.codigoTipoParteTrf;
	}

	public void setCodigoTipoParteTrf(Integer codigoTipoParteTrf) {
		this.codigoTipoParteTrf = codigoTipoParteTrf;
	}

	@Column(name = "ds_tipo_parte_trf")
	@Length(max = 50)
	public String getTipoParteTrf() {
		return tipoParteTrf;
	}

	public void setTipoParteTrf(String tipoParteTrf) {
		this.tipoParteTrf = tipoParteTrf;
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
		return tipoParteTrf;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoParteTrf)) {
			return false;
		}
		TipoParteTrf other = (TipoParteTrf) obj;
		if (getIdTipoParteTrf() != other.getIdTipoParteTrf()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoParteTrf();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoParteTrf> getEntityClass() {
		return TipoParteTrf.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoParteTrf());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
