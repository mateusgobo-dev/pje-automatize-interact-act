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
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.TitularidadeOrgaoEnum;

@Entity
@Table(name = Lote.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_lote", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_lote"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Lote implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Lote,Integer> {

	public static final String TABLE_NAME = "tb_lote";
	private static final long serialVersionUID = 1L;

	private int idLote;
	private String lote;
	private String descricao;
	private Date dtCriacao;
	private Date dtFim;
	private Usuario usuario;
	private Boolean ativo;
	private String processoTrfSearch;
	private TitularidadeOrgaoEnum titularidade = TitularidadeOrgaoEnum.A;
	private Tarefa tarefa;

	private List<ProcessoLote> processoLoteList = new ArrayList<ProcessoLote>(0);
	private List<ProcessoTrf> processoTrfList = new ArrayList<ProcessoTrf>(0);

	public Lote() {
	}

	@Id
	@GeneratedValue(generator = "gen_lote")
	@Column(name = "id_lote", unique = true, nullable = false)
	public int getIdLote() {
		return this.idLote;
	}

	public void setIdLote(int idLote) {
		this.idLote = idLote;
	}

	@Column(name = "ds_lote", nullable = false, length = 100, unique = true)
	@NotNull
	@Length(max = 100)
	public String getLote() {
		return this.lote;
	}

	public void setLote(String lote) {
		this.lote = lote;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao")
	public Date getDtCriacao() {
		return this.dtCriacao;
	}

	public void setDtCriacao(Date dtCriacao) {
		this.dtCriacao = dtCriacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim")
	public Date getDtFim() {
		return this.dtFim;
	}

	public void setDtFim(Date dtFim) {
		this.dtFim = dtFim;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", nullable = false)
	@NotNull
	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Column(name = "in_ativo")
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lote")
	public List<ProcessoLote> getProcessoLoteList() {
		return this.processoLoteList;
	}

	public void setProcessoLoteList(List<ProcessoLote> processoLoteList) {
		this.processoLoteList = processoLoteList;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_processo_lote", joinColumns = { @JoinColumn(name = "id_lote", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_processo_trf", nullable = false, updatable = false) })
	public List<ProcessoTrf> getProcessoTrfList() {
		return this.processoTrfList;
	}

	public void setProcessoTrfList(List<ProcessoTrf> processoTrfList) {
		this.processoTrfList = processoTrfList;
	}

	@Transient
	public String getProcessoTrfSearch() {
		return processoTrfSearch;
	}

	public void setProcessoTrfSearch(String processoTrfSearch) {
		this.processoTrfSearch = processoTrfSearch;
	}

	@Override
	public String toString() {
		return lote;
	}

	@Column(name = "ds_descricao", length = 200)
	@Length(max = 200)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name = "in_titularidade", length = 1)
	@Enumerated(EnumType.STRING)
	public TitularidadeOrgaoEnum getTitularidade() {
		return titularidade;
	}

	public void setTitularidade(TitularidadeOrgaoEnum titularidade) {
		this.titularidade = titularidade;
	}

	public void setTarefa(Tarefa tarefa) {
		this.tarefa = tarefa;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tarefa")
	public Tarefa getTarefa() {
		return tarefa;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Lote)) {
			return false;
		}
		Lote other = (Lote) obj;
		if (getIdLote() != other.getIdLote()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdLote();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Lote> getEntityClass() {
		return Lote.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdLote());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
