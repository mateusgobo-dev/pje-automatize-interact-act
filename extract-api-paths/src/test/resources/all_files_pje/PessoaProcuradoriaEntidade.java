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
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_pess_prcrdoria_entidade")
@org.hibernate.annotations.GenericGenerator(name = "gen_pess_prcrdoria_entidade", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pess_prcrdoria_entidade"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaProcuradoriaEntidade implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaProcuradoriaEntidade,Integer> {

	private static final long serialVersionUID = 1L;

	private int idPessoaProcuradoriaEntidade;
	private Procuradoria procuradoria;
	private Pessoa pessoa;
	private Integer idPessoa;

	private List<PessoaProcuradorProcuradoria> pessoaProcuradorProcuradoriaList = new ArrayList<PessoaProcuradorProcuradoria>(
			0);

	public PessoaProcuradoriaEntidade() {
	}

	@Id
	@GeneratedValue(generator = "gen_pess_prcrdoria_entidade")
	@Column(name = "id_pess_procuradoria_entidade", unique = true, nullable = false)
	public int getIdPessoaProcuradoriaEntidade() {
		return idPessoaProcuradoriaEntidade;
	}

	public void setIdPessoaProcuradoriaEntidade(int idPessoaProcuradoriaEntidade) {
		this.idPessoaProcuradoriaEntidade = idPessoaProcuradoriaEntidade;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_procuradoria")
	public Procuradoria getProcuradoria() {
		return procuradoria;
	}

	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa")
	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	@Column(name="id_pessoa", insertable=false, updatable=false)
	public Integer getIdPessoa() {
		return idPessoa;
	}

	public void setIdPessoa(Integer idPessoa) {
		this.idPessoa = idPessoa;
	}

	@OneToMany(mappedBy = "pessoaProcuradoriaEntidade", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	public List<PessoaProcuradorProcuradoria> getPessoaProcuradorProcuradoriaList() {
		return pessoaProcuradorProcuradoriaList;
	}

	public void setPessoaProcuradorProcuradoriaList(List<PessoaProcuradorProcuradoria> pessoaProcuradorProcuradoriaList) {
		this.pessoaProcuradorProcuradoriaList = pessoaProcuradorProcuradoriaList;
	}

	@Override
	public String toString() {
		return pessoa.getNome();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PessoaProcuradoriaEntidade)) {
			return false;
		}
		PessoaProcuradoriaEntidade other = (PessoaProcuradoriaEntidade) obj;
		if (getIdPessoaProcuradoriaEntidade() != other.getIdPessoaProcuradoriaEntidade()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPessoaProcuradoriaEntidade();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaProcuradoriaEntidade> getEntityClass() {
		return PessoaProcuradoriaEntidade.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPessoaProcuradoriaEntidade());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
