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
@Table(name = "tb_tipo_audiencia")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_audiencia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_audiencia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoAudiencia implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoAudiencia,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoAudiencia;
	private String tipoAudiencia;
	private Boolean ativo;

	public TipoAudiencia() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_audiencia")
	@Column(name = "id_tipo_audiencia", unique = true, nullable = false)
	public int getIdTipoAudiencia() {
		return this.idTipoAudiencia;
	}

	public void setIdTipoAudiencia(int idTipoAudiencia) {
		this.idTipoAudiencia = idTipoAudiencia;
	}

	@Column(name = "ds_tipo_audiencia", unique = true, nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getTipoAudiencia() {
		return this.tipoAudiencia;
	}

	public void setTipoAudiencia(String tipoAudiencia) {
		this.tipoAudiencia = tipoAudiencia;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return tipoAudiencia;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoAudiencia)) {
			return false;
		}
		TipoAudiencia other = (TipoAudiencia) obj;
		if (getIdTipoAudiencia() != other.getIdTipoAudiencia()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoAudiencia();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoAudiencia> getEntityClass() {
		return TipoAudiencia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoAudiencia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
