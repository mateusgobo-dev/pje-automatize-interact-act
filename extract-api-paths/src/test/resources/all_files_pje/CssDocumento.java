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
package br.jus.pje.nucleo.entidades.editor;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tb_css_documento")
@org.hibernate.annotations.GenericGenerator(name = "gen_css_documento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_css_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CssDocumento implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "gen_css_documento", strategy = GenerationType.SEQUENCE)
	@Column(name = "id_css_documento")
	private Integer idCssDocumento;

	@Column(name = "nm_css_documento", nullable = false, length = 100, unique = true)
	private String nome;

	@Column(name = "vl_css_documento", nullable = false)
	private String conteudo;
	
	@Column(name = "in_padrao", nullable = false)
	private Boolean padrao;
	
	@Column(name = "in_ativo", nullable = false)
	private Boolean ativo;

	public Integer getIdCssDocumento() {
		return idCssDocumento;
	}

	public void setIdCssDocumento(Integer idCssDocumento) {
		this.idCssDocumento = idCssDocumento;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	public Boolean getPadrao() {
		return padrao;
	}
	
	public void setPadrao(Boolean padrao) {
		this.padrao = padrao;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CssDocumento)) {
			return false;
		}
		CssDocumento other = (CssDocumento) obj;
		if (nome == null) {
			if (other.nome != null) {
				return false;
			}
		} else if (!nome.equals(other.nome)) {
			return false;
		}
		return true;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

}
