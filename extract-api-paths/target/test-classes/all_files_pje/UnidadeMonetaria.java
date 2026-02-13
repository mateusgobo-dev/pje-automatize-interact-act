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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_unidade_monetaria")
@org.hibernate.annotations.GenericGenerator(name = "gen_unidade_monetaria", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_unidade_monetaria"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class UnidadeMonetaria implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<UnidadeMonetaria,Integer>{

	private static final long serialVersionUID = -35057487035424067L;
	private Integer id;
	private String descricao;
	private String simbolo;

	@Id
	@GeneratedValue(generator = "gen_unidade_monetaria")
	@Column(name = "id_unidade_monetaria", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "ds_unidade_monetaria", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name = "ds_simbolo", nullable = false, length = 6)
	@NotNull
	@Length(max = 6)
	public String getSimbolo() {
		return simbolo;
	}

	public void setSimbolo(String simbolo) {
		this.simbolo = simbolo;
	}

	@Override
	public String toString() {
		return getSimbolo();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends UnidadeMonetaria> getEntityClass() {
		return UnidadeMonetaria.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
