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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_tipo_pichacao")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_pichacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_pichacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoPichacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoPichacao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoPichacao;
	private String tipoPichacao;
	private Boolean ativo;

	private List<TipoPichacaoClasseJudicial> tipoPichacaoClasseJudicialList = new ArrayList<TipoPichacaoClasseJudicial>(
			0);

	public TipoPichacao() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_pichacao")
	@Column(name = "id_tipo_pichacao", unique = true, nullable = false)
	public int getIdTipoPichacao() {
		return this.idTipoPichacao;
	}

	public void setIdTipoPichacao(int idTipoPichacao) {
		this.idTipoPichacao = idTipoPichacao;
	}

	@Column(name = "ds_tipo_pichacao", nullable = false, length = 100, unique = true)
	@NotNull
	@Length(max = 100)
	public String getTipoPichacao() {
		return this.tipoPichacao;
	}

	public void setTipoPichacao(String tipoPichacao) {
		this.tipoPichacao = tipoPichacao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return tipoPichacao;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "tipoPichacao")
	public List<TipoPichacaoClasseJudicial> getTipoPichacaoClasseJudicialList() {
		return this.tipoPichacaoClasseJudicialList;
	}

	public void setTipoPichacaoClasseJudicialList(List<TipoPichacaoClasseJudicial> tipoPichacaoClasseJudicialList) {
		this.tipoPichacaoClasseJudicialList = tipoPichacaoClasseJudicialList;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoPichacao)) {
			return false;
		}
		TipoPichacao other = (TipoPichacao) obj;
		if (getIdTipoPichacao() != other.getIdTipoPichacao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoPichacao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoPichacao> getEntityClass() {
		return TipoPichacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoPichacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
