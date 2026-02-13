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

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = ComplementoPessoaQualificacao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_comple_pess_qualificacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_comple_pess_qualificacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ComplementoPessoaQualificacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ComplementoPessoaQualificacao,Integer> {

	public static final String TABLE_NAME = "tb_complem_pessoa_qualific";
	private static final long serialVersionUID = 1L;

	private int idComplementoPessoaQualificacao;
	private PessoaQualificacao pessoaQualificacao;
	private ComplementoQualificacao complementoQualificacao;
	private String valorComplementoPessoaQualificacao;

	public ComplementoPessoaQualificacao() {
	}

	@Id
	@GeneratedValue(generator = "gen_comple_pess_qualificacao")
	@Column(name = "id_complem_pessoa_qualificacao", unique = true, nullable = false)
	public int getIdComplementoPessoaQualificacao() {
		return this.idComplementoPessoaQualificacao;
	}

	public void setIdComplementoPessoaQualificacao(int idComplementoPessoaQualificacao) {
		this.idComplementoPessoaQualificacao = idComplementoPessoaQualificacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_qualificacao", nullable = false)
	@NotNull
	public PessoaQualificacao getPessoaQualificacao() {
		return this.pessoaQualificacao;
	}

	public void setPessoaQualificacao(PessoaQualificacao pessoaQualificacao) {
		this.pessoaQualificacao = pessoaQualificacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_complemento_qualificacao", nullable = false)
	@NotNull
	public ComplementoQualificacao getComplementoQualificacao() {
		return this.complementoQualificacao;
	}

	public void setComplementoQualificacao(ComplementoQualificacao complementoQualificacao) {
		this.complementoQualificacao = complementoQualificacao;
	}

	@Column(name = "ds_complem_pessoa_qualificacao", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getValorComplementoPessoaQualificacao() {
		return this.valorComplementoPessoaQualificacao;
	}

	public void setValorComplementoPessoaQualificacao(String valorComplementoPessoaQualificacao) {
		this.valorComplementoPessoaQualificacao = valorComplementoPessoaQualificacao;
	}

	@Override
	public String toString() {
		return complementoQualificacao + ": " + valorComplementoPessoaQualificacao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ComplementoPessoaQualificacao)) {
			return false;
		}
		ComplementoPessoaQualificacao other = (ComplementoPessoaQualificacao) obj;
		if (getIdComplementoPessoaQualificacao() != other.getIdComplementoPessoaQualificacao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdComplementoPessoaQualificacao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ComplementoPessoaQualificacao> getEntityClass() {
		return ComplementoPessoaQualificacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdComplementoPessoaQualificacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
