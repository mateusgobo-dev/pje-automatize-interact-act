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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_profissao")
@org.hibernate.annotations.GenericGenerator(name = "gen_profissao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_profissao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@JsonIgnoreProperties({"profissaoList","pessoaFisicaList", "profissaoSinonimoList","hibernateLazyInitializer", "handler", "fieldHandler", "session", "flushMode", "persistenceContext"})
public class Profissao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Profissao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProfissao;
	private String codCbo;
	private String profissao;
	private Profissao profissaoSuperior;
	private Boolean ativo;
	private List<Profissao> profissaoList = new ArrayList<Profissao>(0);
	private List<PessoaFisica> pessoaFisicaList = new ArrayList<PessoaFisica>(0);

	private List<ProfissaoSinonimo> profissaoSinonimoList = new ArrayList<ProfissaoSinonimo>(0);

	public Profissao() {
	}

	@Id
	@GeneratedValue(generator = "gen_profissao")
	@Column(name = "id_profissao", unique = true, nullable = false)
	public int getIdProfissao() {
		return this.idProfissao;
	}

	public void setIdProfissao(int idProfissao) {
		this.idProfissao = idProfissao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_profissao_superior")
	public Profissao getProfissaoSuperior() {
		return this.profissaoSuperior;
	}

	public void setProfissaoSuperior(Profissao profissaoSuperior) {
		this.profissaoSuperior = profissaoSuperior;
	}

	@Column(name = "cd_profissao", unique = true, length = 15)
	@Length(max = 15)
	public String getCodCbo() {
		return this.codCbo;
	}

	public void setCodCbo(String codCbo) {
		this.codCbo = codCbo;
	}

	@Column(name = "ds_profissao", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getProfissao() {
		return this.profissao;
	}

	public void setProfissao(String profissao) {
		this.profissao = profissao.toUpperCase();
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "profissaoSuperior")
	@OrderBy("profissao")
	public List<Profissao> getProfissaoList() {
		return this.profissaoList;
	}

	public void setProfissaoList(List<Profissao> profissaoList) {
		this.profissaoList = profissaoList;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "profissao")
	public List<PessoaFisica> getPessoaFisicaList() {
		return this.pessoaFisicaList;
	}

	public void setPessoaFisicaList(List<PessoaFisica> pessoaFisicaList) {
		this.pessoaFisicaList = pessoaFisicaList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "profissao")
	public List<ProfissaoSinonimo> getProfissaoSinonimoList() {
		return this.profissaoSinonimoList;
	}

	public void setProfissaoSinonimoList(List<ProfissaoSinonimo> profissaoSinonimoList) {
		this.profissaoSinonimoList = profissaoSinonimoList;
	}

	@Override
	public String toString() {
		return profissao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getCodCbo() == null) ? 0 : codCbo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Profissao))
			return false;
		Profissao other = (Profissao) obj;
		if (getCodCbo() == null) {
			if (other.getCodCbo() != null)
				return false;
		} else if (!codCbo.equals(other.getCodCbo()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Profissao> getEntityClass() {
		return Profissao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProfissao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
