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
@Cacheable(true)
@Table(name = DiaSemana.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_dia_semana", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_dia_semana"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DiaSemana implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<DiaSemana,Integer> {

	public static final String TABLE_NAME = "tb_dia_semana";
	private static final long serialVersionUID = 1L;

	private int idDiaSemana;
	private String diaSemana;

	public DiaSemana() {
	}

	@Id
	@GeneratedValue(generator = "gen_dia_semana")
	@Column(name = "id_dia_semana", unique = true, nullable = false)
	public int getIdDiaSemana() {
		return this.idDiaSemana;
	}

	public void setIdDiaSemana(int idDiaSemana) {
		this.idDiaSemana = idDiaSemana;
	}

	@Column(name = "ds_dia_semana", nullable = false, length = 30)
	@NotNull
	@Length(max = 30)
	public String getDiaSemana() {
		return this.diaSemana;
	}

	public void setDiaSemana(String diaSemana) {
		this.diaSemana = diaSemana;
	}

	@Override
	public String toString() {
		return diaSemana;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DiaSemana)) {
			return false;
		}
		DiaSemana other = (DiaSemana) obj;
		if (getIdDiaSemana() != other.getIdDiaSemana()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdDiaSemana();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DiaSemana> getEntityClass() {
		return DiaSemana.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDiaSemana());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
