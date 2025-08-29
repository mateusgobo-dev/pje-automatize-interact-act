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

@Entity
@Table(name = "tb_tipo_dispositivo_norma")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_dispositivo_norma", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_dispositivo_norma"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoDispositivoNorma implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoDispositivoNorma
,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idTipoDispositivo;
	private String dsTipoDispositivo;
	private Boolean inAtivo;

	// Constructor
	public TipoDispositivoNorma() {

	}

	public TipoDispositivoNorma(Integer id) {
		this.idTipoDispositivo = id;
	}

	@Override
	public String toString() {
		return this.getDsTipoDispositivo();
	}

	// GETTER'S AND SETTER'S
	@Id
	@GeneratedValue(generator = "gen_tipo_dispositivo_norma")
	@Column(name = "id_tipo_dispositivo", unique = true, nullable = false)
	public Integer getIdTipoDispositivo() {
		return idTipoDispositivo;
	}

	public void setIdTipoDispositivo(Integer idTipoDispositivo) {
		this.idTipoDispositivo = idTipoDispositivo;
	}

	@Column(name = "ds_tipo_dispositivo")
	@NotNull
	public String getDsTipoDispositivo() {
		return dsTipoDispositivo;
	}

	public void setDsTipoDispositivo(String dsTipoDispositivo) {
		this.dsTipoDispositivo = dsTipoDispositivo;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getInAtivo() {
		return inAtivo;
	}

	public void setInAtivo(Boolean inAtivo) {
		this.inAtivo = inAtivo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getDsTipoDispositivo() == null) ? 0 : dsTipoDispositivo.hashCode());
		result = prime * result + ((getIdTipoDispositivo() == null) ? 0 : idTipoDispositivo.hashCode());
		result = prime * result + ((getInAtivo() == null) ? 0 : inAtivo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TipoDispositivoNorma))
			return false;
		TipoDispositivoNorma other = (TipoDispositivoNorma) obj;
		if (getDsTipoDispositivo() == null) {
			if (other.getDsTipoDispositivo() != null)
				return false;
		} else if (!dsTipoDispositivo.equals(other.getDsTipoDispositivo()))
			return false;
		if (getIdTipoDispositivo() == null) {
			if (other.getIdTipoDispositivo() != null)
				return false;
		} else if (!idTipoDispositivo.equals(other.getIdTipoDispositivo()))
			return false;
		if (getInAtivo() == null) {
			if (other.getInAtivo() != null)
				return false;
		} else if (!inAtivo.equals(other.getInAtivo()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoDispositivoNorma> getEntityClass() {
		return TipoDispositivoNorma.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdTipoDispositivo();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
