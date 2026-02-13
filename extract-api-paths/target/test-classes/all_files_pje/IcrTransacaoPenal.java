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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;

@Entity
@Table(name = "tb_icr_transacao_penal")
@PrimaryKeyJoinColumn(name = "id_icr_transacao_penal")
public class IcrTransacaoPenal extends InformacaoCriminalRelevante implements Serializable {

	private static final long serialVersionUID = -6666686326504870795L;
	private PessoaMagistrado pessoaMagistrado;
	private List<CondicaoIcrTransacaoPenal> condicaoIcrTransacaoList = new ArrayList<CondicaoIcrTransacaoPenal>();

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa_magistrado", nullable = false)
	@NotNull
	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}

	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "icrTransacaoPenal")
	@Cascade(value = { org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	public List<CondicaoIcrTransacaoPenal> getCondicaoIcrTransacaoList() {
		return condicaoIcrTransacaoList;
	}

	public void setCondicaoIcrTransacaoList(List<CondicaoIcrTransacaoPenal> condicaoIcrTransacaoList) {
		this.condicaoIcrTransacaoList = condicaoIcrTransacaoList;
	}

	@Transient
	public CondicaoIcrTransacaoPenal getUltimaCondicao() {
		if (!getCondicaoIcrTransacaoList().isEmpty()) {
			return getCondicaoIcrTransacaoList().get(0);
		}
		return null;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrTransacaoPenal.class;
	}
}
