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
package br.jus.pje.nucleo.entidades.ajuda;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_pagina")
@org.hibernate.annotations.GenericGenerator(name = "gen_pagina", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pagina"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Pagina implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Pagina,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idPagina;
	private String descricao;
	private String url;

	public Pagina() {
	}

	@Id
	@GeneratedValue(generator = "gen_pagina")
	@Column(name = "id_pagina", unique = true, nullable = false)
	public Integer getIdPagina() {
		return this.idPagina;
	}

	public void setIdPagina(Integer idPagina) {
		this.idPagina = idPagina;
	}

	@Column(name = "ds_descricao", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getDescricao() {
		return this.descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name = "ds_url", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Pagina> getEntityClass() {
		return Pagina.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdPagina();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
