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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.TipoPessoaContatoEnum;

@Entity
@Table(name = "tb_tipo_contato")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_contato", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_contato"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoContato implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoContato,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoContato;
	private String tipoContato;
	private Boolean ativo;
	private TipoPessoaContatoEnum tipoPessoa;
	private String regexValidacao;
	private String mascara;
	private List<MeioContato> meioContatoList = new ArrayList<MeioContato>(0);

	public TipoContato() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_contato")
	@Column(name = "id_tipo_contato", unique = true, nullable = false)
	public int getIdTipoContato() {
		return this.idTipoContato;
	}

	public void setIdTipoContato(int idTipoContato) {
		this.idTipoContato = idTipoContato;
	}

	@Column(name = "ds_tipo_contato", nullable = false, length = 30, unique = true)
	@NotNull
	@Length(max = 30)
	public String getTipoContato() {
		return this.tipoContato;
	}

	public void setTipoContato(String tipoContato) {
		this.tipoContato = tipoContato;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "in_tipo_pessoa", length = 1)
	@Enumerated(EnumType.STRING)
	public TipoPessoaContatoEnum getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoaContatoEnum tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	@Column(name = "ds_regex_validacao")
	public String getRegexValidacao() {
		return regexValidacao;
	}

	public void setRegexValidacao(String regexValidacao) {
		this.regexValidacao = regexValidacao;
	}
	
	@Column(name = "ds_mascara")
	public String getMascara() {
		return mascara;
	}

	public void setMascara(String mascara) {
		this.mascara = mascara;
	}
	
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "tipoContato")
	public List<MeioContato> getMeioContatoList() {
		return this.meioContatoList;
	}

	public void setMeioContatoList(List<MeioContato> meioContatoList) {
		this.meioContatoList = meioContatoList;
	}
	
	@Override
	public String toString() {
		return tipoContato;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoContato)) {
			return false;
		}
		TipoContato other = (TipoContato) obj;
		if (getIdTipoContato() != other.getIdTipoContato()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoContato();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoContato> getEntityClass() {
		return TipoContato.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoContato());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
