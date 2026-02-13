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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = Etnia.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_etnia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_etnia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler", "session", "flushMode", "persistenceContext"})
public class Etnia implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Etnia,Integer> {

	public static final String TABLE_NAME = "tb_etnia";
	private static final long serialVersionUID = 1L;

	private int idEtnia;
	private String etnia;
	private Boolean ativo;

	public Etnia() {
	}

	@Id
	@GeneratedValue(generator = "gen_etnia")
	@Column(name = "id_etnia", unique = true, nullable = false)
	public int getIdEtnia() {
		return this.idEtnia;
	}

	public void setIdEtnia(int idEtnia) {
		this.idEtnia = idEtnia;
	}

	@Column(name = "ds_etnia", unique = true, nullable = false, length = 30)
	@NotNull
	@Length(max = 30)
	public String getEtnia() {
		return this.etnia;
	}

	public void setEtnia(String etnia) {
		if (etnia != null) {
			this.etnia = etnia.toUpperCase();
		} else {
			this.etnia = null;
		}

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
		return etnia;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Etnia)) {
			return false;
		}
		Etnia other = (Etnia) obj;
		if (getIdEtnia() != other.getIdEtnia()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdEtnia();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Etnia> getEntityClass() {
		return Etnia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEtnia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
