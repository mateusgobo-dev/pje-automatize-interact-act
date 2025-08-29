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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

/**
 * @author thiago.vieira
 */
@Entity
@Table(name = "tb_agrup_pessoas", uniqueConstraints = { @UniqueConstraint(columnNames = { "cd_agrupamento" }) })
@org.hibernate.annotations.GenericGenerator(name = "gen_agrup_pessoas", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_agrupamento_pessoas"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AgrupamentoPessoas implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AgrupamentoPessoas,Integer> {

	private static final long serialVersionUID = 8316135038258659181L;
	private int idAgrupamento;
	private String codAgrupamento;
	private String nome;
	private String descricao;
	private Boolean ativo;
	private Set<Pessoa> pessoas = new HashSet<Pessoa>(0);

	@Id
	@GeneratedValue(generator = "gen_agrup_pessoas")
	@Column(name = "id_agrupamento", unique = true, nullable = false)
	public int getIdAgrupamento() {
		return idAgrupamento;
	}

	public void setIdAgrupamento(int idAgrupamento) {
		this.idAgrupamento = idAgrupamento;
	}

	@Column(name = "cd_agrupamento", nullable = false, length = 32)
	@NotNull
	@Length(max = 32)
	public String getCodAgrupamento() {
		return codAgrupamento;
	}

	public void setCodAgrupamento(String codAgrupamento) {
		this.codAgrupamento = codAgrupamento;
	}

	@Column(name = "ds_nome", nullable = false, length = 64)
	@NotNull
	@Length(max = 64)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	@Column(name = "ds_descricao", nullable = false, length = 256)
	@NotNull
	@Length(max = 256)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToMany
	@JoinTable(name = "tb_agrup_pessoas_pessoa", joinColumns = @JoinColumn(name = "id_agrupamento"), inverseJoinColumns = @JoinColumn(name = "id_pessoa"))
	public Set<Pessoa> getPessoas() {
		return pessoas;
	}

	public void setPessoas(Set<Pessoa> pessoas) {
		this.pessoas = pessoas;
	}
	
	@Override
	public String toString() {
		return nome;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AgrupamentoPessoas> getEntityClass() {
		return AgrupamentoPessoas.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAgrupamento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
