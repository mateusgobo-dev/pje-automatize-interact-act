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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


/**
 * JurisdicaoMunicipio Entidade resposável pelo agrupamento de munícipios em uma
 * jurisdição
 */

@Entity
@javax.persistence.Cacheable(true)
@Table(name = JurisdicaoMunicipio.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_jurisdicao_municipio", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_jurisdicao_municipio"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class JurisdicaoMunicipio implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<JurisdicaoMunicipio,Integer> {

	public static final String TABLE_NAME = "tb_jurisdicao_municipio";
	private static final long serialVersionUID = 1L;

	private int idJurisdicaoMunicipio;
	private Jurisdicao jurisdicao;
	private Municipio municipio;
	private Boolean sede;

	public JurisdicaoMunicipio() {
	}

	@Id
	@GeneratedValue(generator = "gen_jurisdicao_municipio")
	@Column(name = "id_jurisdicao_municipio", unique = true, nullable = false)
	public int getIdJurisdicaoMunicipio() {
		return idJurisdicaoMunicipio;
	}

	public void setIdJurisdicaoMunicipio(int idJurisdicaoMunicipio) {
		this.idJurisdicaoMunicipio = idJurisdicaoMunicipio;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_jurisdicao", nullable = false)
	@NotNull
	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_municipio", nullable = false)
	@NotNull
	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	@Column(name = "in_sede", nullable = false)
	public Boolean getSede() {
		return sede;
	}

	public void setSede(Boolean sede) {
		this.sede = sede;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof JurisdicaoMunicipio)) {
			return false;
		}
		JurisdicaoMunicipio other = (JurisdicaoMunicipio) obj;
		if (getIdJurisdicaoMunicipio() != other.getIdJurisdicaoMunicipio()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdJurisdicaoMunicipio();
		return result;
	}
	
	@Override
	public String toString() {
		return municipio.getMunicipio();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends JurisdicaoMunicipio> getEntityClass() {
		return JurisdicaoMunicipio.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdJurisdicaoMunicipio());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
