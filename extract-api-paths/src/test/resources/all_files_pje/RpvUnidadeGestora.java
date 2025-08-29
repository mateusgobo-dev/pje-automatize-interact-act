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
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_rpv_unidade_gestora")
public class RpvUnidadeGestora implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private int codigoUnidadeGestora;
	private String unidadeGestora;

	public RpvUnidadeGestora() {
	}

	@Id
	@Column(name = "cd_unidade_gestora", nullable = false)
	@NotNull
	public int getCodigoUnidadeGestora() {
		return codigoUnidadeGestora;
	}

	public void setCodigoUnidadeGestora(int codigoUnidadeGestora) {
		this.codigoUnidadeGestora = codigoUnidadeGestora;
	}

	@Column(name = "ds_unidade_gestora")
	public String getUnidadeGestora() {
		return unidadeGestora;
	}

	public void setUnidadeGestora(String unidadeGestora) {
		this.unidadeGestora = unidadeGestora;
	}

	@Override
	public String toString() {
		return String.valueOf(codigoUnidadeGestora);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RpvUnidadeGestora)) {
			return false;
		}
		RpvUnidadeGestora other = (RpvUnidadeGestora) obj;
		if (getCodigoUnidadeGestora() != other.getCodigoUnidadeGestora()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getCodigoUnidadeGestora();
		return result;
	}
}