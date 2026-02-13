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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_pessoa_qualificacao")
@org.hibernate.annotations.GenericGenerator(name = "gen_pessoa_qualificacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pessoa_qualificacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaQualificacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaQualificacao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idPessoaQualificacao;
	private Qualificacao qualificacao;
	private Pessoa pessoa;
	private String pessoaQualificacao;
	private List<ComplementoPessoaQualificacao> complementoPessoaQualificacaoList = new ArrayList<ComplementoPessoaQualificacao>(
			0);

	public PessoaQualificacao() {
	}

	@Id
	@GeneratedValue(generator = "gen_pessoa_qualificacao")
	@Column(name = "id_pessoa_qualificacao", unique = true, nullable = false)
	public int getIdPessoaQualificacao() {
		return this.idPessoaQualificacao;
	}

	public void setIdPessoaQualificacao(int idPessoaQualificacao) {
		this.idPessoaQualificacao = idPessoaQualificacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_qualificacao", nullable = false)
	@NotNull
	public Qualificacao getQualificacao() {
		return this.qualificacao;
	}

	public void setQualificacao(Qualificacao qualificacao) {
		this.qualificacao = qualificacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa", nullable = false)
	@NotNull
	public Pessoa getPessoa() {
		return this.pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	@Column(name = "ds_pessoa_qualificacao", nullable = false, length = 50)
	@NotNull
	@Length(max = 50)
	public String getPessoaQualificacao() {
		return this.pessoaQualificacao;
	}

	public void setPessoaQualificacao(String pessoaQualificacao) {
		this.pessoaQualificacao = pessoaQualificacao;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "pessoaQualificacao")
	public List<ComplementoPessoaQualificacao> getComplementoPessoaQualificacaoList() {
		return this.complementoPessoaQualificacaoList;
	}

	public void setComplementoPessoaQualificacaoList(
			List<ComplementoPessoaQualificacao> complementoPessoaQualificacaoList) {
		this.complementoPessoaQualificacaoList = complementoPessoaQualificacaoList;
	}

	@Override
	public String toString() {
		return pessoaQualificacao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PessoaQualificacao)) {
			return false;
		}
		PessoaQualificacao other = (PessoaQualificacao) obj;
		if (getIdPessoaQualificacao() != other.getIdPessoaQualificacao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPessoaQualificacao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaQualificacao> getEntityClass() {
		return PessoaQualificacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPessoaQualificacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
