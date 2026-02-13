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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = TipoResultadoDiligencia.TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = {
		"id_tipo_resultado_diligencia", "ds_tipo_resultado_diligencia" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_tp_resultado_diligencia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tp_resultado_diligencia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoResultadoDiligencia implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoResultadoDiligencia,Integer> {

	public static final String TABLE_NAME = "tb_tp_resultado_diligencia";
	private static final long serialVersionUID = 1L;

	private int idTipoResultadoDiligencia;
	private String tipoResultadoDiligencia;
	private String codigoResultadoDiligencia;
	private Boolean ativo;

	public TipoResultadoDiligencia() {
	}

	@Id
	@GeneratedValue(generator = "gen_tp_resultado_diligencia")
	@Column(name = "id_tipo_resultado_diligencia", unique = true, nullable = false)
	public int getIdTipoResultadoDiligencia() {
		return this.idTipoResultadoDiligencia;
	}

	public void setIdTipoResultadoDiligencia(int idTipoResultadoDiligencia) {
		this.idTipoResultadoDiligencia = idTipoResultadoDiligencia;
	}

	@Column(name = "ds_tipo_resultado_diligencia", unique = true, nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getTipoResultadoDiligencia() {
		return this.tipoResultadoDiligencia;
	}

	public void setTipoResultadoDiligencia(String tipoResultadoDiligencia) {
		this.tipoResultadoDiligencia = tipoResultadoDiligencia;
	}

	@Column(name = "cd_tipo_resultado_diligencia", length = 15)
	@Length(max = 15)
	public String getCodigoResultadoDiligencia() {
		return codigoResultadoDiligencia;
	}

	public void setCodigoResultadoDiligencia(String codigoResultadoDiligencia) {
		this.codigoResultadoDiligencia = codigoResultadoDiligencia;
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
		return tipoResultadoDiligencia;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoResultadoDiligencia)) {
			return false;
		}
		TipoResultadoDiligencia other = (TipoResultadoDiligencia) obj;
		if (getIdTipoResultadoDiligencia() != other.getIdTipoResultadoDiligencia()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoResultadoDiligencia();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoResultadoDiligencia> getEntityClass() {
		return TipoResultadoDiligencia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoResultadoDiligencia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
