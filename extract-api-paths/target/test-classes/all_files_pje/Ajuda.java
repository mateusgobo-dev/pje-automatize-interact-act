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

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;

@Entity
@Table(name = "tb_ajuda")
@Analyzer(impl = BrazilianAnalyzer.class)
@Indexed
@org.hibernate.annotations.GenericGenerator(name = "gen_ajuda", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_ajuda"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Ajuda implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Ajuda,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idAjuda;
	private Date dataRegistro;
	private String texto;
	private Pagina pagina;
	private Usuario usuario;

	@Id
	@GeneratedValue(generator = "gen_ajuda")
	@Column(name = "id_ajuda", unique = true, nullable = false)
	public Integer getIdAjuda() {
		return idAjuda;
	}

	public void setIdAjuda(Integer idAjuda) {
		this.idAjuda = idAjuda;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_registro", nullable = false, length = 0)
	@NotNull
	public Date getDataRegistro() {
		return dataRegistro;
	}

	public void setDataRegistro(Date dataRegistro) {
		this.dataRegistro = dataRegistro;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_texto")
	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pagina", nullable = false)
	@NotNull
	public Pagina getPagina() {
		return pagina;
	}

	public void setPagina(Pagina pagina) {
		this.pagina = pagina;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", nullable = false)
	@NotNull
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Transient
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO, name = "texto")
	public String getTextoIndexavel() {
		return StringUtil.removeHtmlTags(texto);
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Ajuda> getEntityClass() {
		return Ajuda.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdAjuda();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
