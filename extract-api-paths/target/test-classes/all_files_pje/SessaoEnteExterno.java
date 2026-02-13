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
@Table(name = "tb_sessao_ente_externo")
@org.hibernate.annotations.GenericGenerator(name = "gen_sessao_ente_externo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sessao_ente_externo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SessaoEnteExterno implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<SessaoEnteExterno,Integer> {

	private static final long serialVersionUID = 1L;

	private int idSessaoEnteExterno;
	private Sessao sessao;
	private Pessoa pessoaAcompanhaSessao;
	private String nomePessoa;
	private Boolean ativo;
	
	public SessaoEnteExterno() {
	}

	@Id
	@GeneratedValue(generator = "gen_sessao_ente_externo")
	@Column(name = "id_sessao_ente_externo", unique = true, nullable = false)
	public int getIdSessaoEnteExterno() {
		return this.idSessaoEnteExterno;
	}

	public void setIdSessaoEnteExterno(int idSessaoEnteExterno) {
		this.idSessaoEnteExterno = idSessaoEnteExterno;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sessao")
	public Sessao getSessao() {
		return this.sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}

	@Column(name = "ds_pessoa", length = 200)
	@Length(max = 200)
	public String getNomePessoa() {
		return nomePessoa;
	}

	public void setNomePessoa(String nomePessoa) {
		this.nomePessoa = nomePessoa;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa")
	public Pessoa getPessoaAcompanhaSessao() {
		return pessoaAcompanhaSessao;
	}

	public void setPessoaAcompanhaSessao(Pessoa pessoaAcompanhaSessao) {
		this.pessoaAcompanhaSessao = pessoaAcompanhaSessao;
	}
	
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idSessaoEnteExterno;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SessaoEnteExterno other = (SessaoEnteExterno) obj;
		if (idSessaoEnteExterno != other.idSessaoEnteExterno)
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SessaoEnteExterno> getEntityClass() {
		return SessaoEnteExterno.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdSessaoEnteExterno());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}