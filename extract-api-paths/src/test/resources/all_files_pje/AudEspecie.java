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
package br.jus.pje.jt.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vs_aud_especie")
//@PrimaryKeyJoinColumn(name = "id_classe_judicial")
public class AudEspecie implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String idClasseJudicial;
	private String dsClasseJudicial;
	private Integer grupoEspecie;
	private String descAutor;
	private String descReu;
	private String pluralAutor;
	private String pluralReu;
	private String femininoAutor;
	private String femininoReu;
	private String autor;
	private String reu;
	private String sufixoFemininoAutor;
	private String sufixoFemininoReu;
	private String sufixoPluralAutor;
	private String sufixoPluralReu;

	@Id
	@Column(name = "id", unique = true, insertable = false, updatable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "id_classe_judicial", unique = true, insertable = false, updatable = false)
	public String getIdClasseJudicial() {
		return idClasseJudicial;
	}

	public void setIdClasseJudicial(String idClasseJudicial) {
		this.idClasseJudicial = idClasseJudicial;
	}

	@Column(name = "ds_classe_judicial", unique = true, insertable = false, updatable = false)
	public String getDsClasseJudicial() {
		return dsClasseJudicial;
	}

	public void setDsClasseJudicial(String dsClasseJudicial) {
		this.dsClasseJudicial = dsClasseJudicial;
	}

	@Column(name = "grupo_especie", unique = true, insertable = false, updatable = false)
	public Integer getGrupoEspecie() {
		return grupoEspecie;
	}

	public void setGrupoEspecie(Integer grupoEspecie) {
		this.grupoEspecie = grupoEspecie;
	}

	@Column(name = "desc_autor", unique = true, insertable = false, updatable = false)
	public String getDescAutor() {
		return descAutor;
	}

	public void setDescAutor(String descAutor) {
		this.descAutor = descAutor;
	}

	@Column(name = "desc_reu", unique = true, insertable = false, updatable = false)
	public String getDescReu() {
		return descReu;
	}

	public void setDescReu(String descReu) {
		this.descReu = descReu;
	}

	@Column(name = "plural_autor", unique = true, insertable = false, updatable = false)
	public String getPluralAutor() {
		return pluralAutor;
	}

	public void setPluralAutor(String pluralAutor) {
		this.pluralAutor = pluralAutor;
	}

	@Column(name = "plural_reu", unique = true, insertable = false, updatable = false)
	public String getPluralReu() {
		return pluralReu;
	}

	public void setPluralReu(String pluralReu) {
		this.pluralReu = pluralReu;
	}

	@Column(name = "fem_autor", unique = true, insertable = false, updatable = false)
	public String getFemininoAutor() {
		return femininoAutor;
	}

	public void setFemininoAutor(String femininoAutor) {
		this.femininoAutor = femininoAutor;
	}

	@Column(name = "fem_reu", unique = true, insertable = false, updatable = false)
	public String getFemininoReu() {
		return femininoReu;
	}

	public void setFemininoReu(String femininoReu) {
		this.femininoReu = femininoReu;
	}

	@Column(name = "autor", unique = true, insertable = false, updatable = false)
	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	@Column(name = "reu", unique = true, insertable = false, updatable = false)
	public String getReu() {
		return reu;
	}

	public void setReu(String reu) {
		this.reu = reu;
	}

	@Column(name = "suf_fem_autor", unique = true, insertable = false, updatable = false)
	public String getSufixoFemininoAutor() {
		return sufixoFemininoAutor;
	}

	public void setSufixoFemininoAutor(String sufixoFemininoAutor) {
		this.sufixoFemininoAutor = sufixoFemininoAutor;
	}

	@Column(name = "suf_fem_reu", unique = true, insertable = false, updatable = false)
	public String getSufixoFemininoReu() {
		return sufixoFemininoReu;
	}

	public void setSufixoFemininoReu(String sufixoFemininoReu) {
		this.sufixoFemininoReu = sufixoFemininoReu;
	}

	@Column(name = "suf_plural_autor", unique = true, insertable = false, updatable = false)
	public String getSufixoPluralAutor() {
		return sufixoPluralAutor;
	}

	public void setSufixoPluralAutor(String sufixoPluralAutor) {
		this.sufixoPluralAutor = sufixoPluralAutor;
	}

	@Column(name = "suf_plural_reu", unique = true, insertable = false, updatable = false)
	public String getSufixoPluralReu() {
		return sufixoPluralReu;
	}

	public void setSufixoPluralReu(String sufixoPluralReu) {
		this.sufixoPluralReu = sufixoPluralReu;
	}
}
