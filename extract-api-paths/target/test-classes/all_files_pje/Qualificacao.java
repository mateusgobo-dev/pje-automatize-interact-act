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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_qualificacao")
@org.hibernate.annotations.GenericGenerator(name = "gen_qualificacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_qualificacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Qualificacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Qualificacao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idQualificacao;
	private String qualificacao;
	private String componenteValidacao;
	private Boolean ativo;
	private List<TipoPessoaQualificacao> tipoPessoaQualificacaoList = new ArrayList<TipoPessoaQualificacao>(0);
	private List<PessoaQualificacao> pessoaQualificacaoList = new ArrayList<PessoaQualificacao>(0);
	private List<ComplementoQualificacao> complementoQualificacaoList = new ArrayList<ComplementoQualificacao>(0);

	public Qualificacao() {
	}

	@Id
	@GeneratedValue(generator = "gen_qualificacao")
	@Column(name = "id_qualificacao", unique = true, nullable = false)
	public int getIdQualificacao() {
		return this.idQualificacao;
	}

	public void setIdQualificacao(int idQualificacao) {
		this.idQualificacao = idQualificacao;
	}

	@Column(name = "ds_qualificacao", nullable = false, length = 50, unique = true)
	@NotNull
	@Length(max = 50)
	public String getQualificacao() {
		return this.qualificacao;
	}

	public void setQualificacao(String qualificacao) {
		this.qualificacao = qualificacao;
	}

	@Column(name = "ds_componente_validacao", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getComponenteValidacao() {
		return this.componenteValidacao;
	}

	public void setComponenteValidacao(String componenteValidacao) {
		this.componenteValidacao = componenteValidacao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "qualificacao")
	public List<TipoPessoaQualificacao> getTipoPessoaQualificacaoList() {
		return this.tipoPessoaQualificacaoList;
	}

	public void setTipoPessoaQualificacaoList(List<TipoPessoaQualificacao> tipoPessoaQualificacaoList) {
		this.tipoPessoaQualificacaoList = tipoPessoaQualificacaoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "qualificacao")
	public List<PessoaQualificacao> getPessoaQualificacaoList() {
		return this.pessoaQualificacaoList;
	}

	public void setPessoaQualificacaoList(List<PessoaQualificacao> pessoaQualificacaoList) {
		this.pessoaQualificacaoList = pessoaQualificacaoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "qualificacao")
	@OrderBy("complementoQualificacao")
	public List<ComplementoQualificacao> getComplementoQualificacaoList() {
		return this.complementoQualificacaoList;
	}

	public void setComplementoQualificacaoList(List<ComplementoQualificacao> complementoQualificacaoList) {
		this.complementoQualificacaoList = complementoQualificacaoList;
	}

	@Override
	public String toString() {
		return qualificacao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Qualificacao)) {
			return false;
		}
		Qualificacao other = (Qualificacao) obj;
		if (getIdQualificacao() != other.getIdQualificacao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdQualificacao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Qualificacao> getEntityClass() {
		return Qualificacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdQualificacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
