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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = Escolaridade.TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "ds_escolaridade" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_escolaridade", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_escolaridade"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler", "session", "flushMode", "persistenceContext"})
public class Escolaridade implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Escolaridade,Integer> {

	public static final String TABLE_NAME = "tb_escolaridade";
	private static final long serialVersionUID = 1L;

	private int idEscolaridade;
	private String escolaridade;
	private Boolean ativo;

	public Escolaridade() {
	}

	@Id
	@GeneratedValue(generator = "gen_escolaridade")
	@Column(name = "id_escolaridade", unique = true, nullable = false)
	public int getIdEscolaridade() {
		return this.idEscolaridade;
	}

	public void setIdEscolaridade(int idEscolaridade) {
		this.idEscolaridade = idEscolaridade;
	}

	@Column(name = "ds_escolaridade", nullable = false, length = 100, unique = true)
	@NotNull
	@Length(max = 100)
	public String getEscolaridade() {
		return this.escolaridade;
	}

	public void setEscolaridade(String escolaridade) {
		this.escolaridade = escolaridade;
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
		return escolaridade;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Escolaridade)) {
			return false;
		}
		Escolaridade other = (Escolaridade) obj;
		if (getIdEscolaridade() != other.getIdEscolaridade()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdEscolaridade();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Escolaridade> getEntityClass() {
		return Escolaridade.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEscolaridade());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
