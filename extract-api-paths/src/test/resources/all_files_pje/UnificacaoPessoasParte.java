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

@Entity
@Table(name = UnificacaoPessoasParte.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_unificacao_pessoas_parte", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_unificacao_pessoas_parte"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class UnificacaoPessoasParte implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<UnificacaoPessoasParte,Integer> {

	public static final String TABLE_NAME = "tb_unificacao_pessoas_parte";
	private static final long serialVersionUID = 1L;

	private int idUnificacaoPessoasParte;
	private UnificacaoPessoas unificacao;
	private ProcessoParte parte;
	private Boolean ativo;

	public UnificacaoPessoasParte() {
	}

	@Id
	@GeneratedValue(generator = "gen_unificacao_pessoas_parte")
	@Column(name = "id_unificacao_pessoas_parte", unique = true, nullable = false)
	public int getIdUnificacaoPessoasParte() {
		return this.idUnificacaoPessoasParte;
	}

	public void setIdUnificacaoPessoasParte(int idUnificacaoPessoasParte) {
		this.idUnificacaoPessoasParte = idUnificacaoPessoasParte;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_unificacao_pessoas", nullable = false)
	@NotNull
	public UnificacaoPessoas getUnificacao() {
		return this.unificacao;
	}

	public void setUnificacao(UnificacaoPessoas unificacao) {
		this.unificacao = unificacao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_parte", nullable = false)
	@NotNull
	public ProcessoParte getParte() {
		return this.parte;
	}

	public void setParte(ProcessoParte parte) {
		this.parte = parte;
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
	public String toString() {
		return parte.getPessoa().getNome();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends UnificacaoPessoasParte> getEntityClass() {
		return UnificacaoPessoasParte.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdUnificacaoPessoasParte());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
