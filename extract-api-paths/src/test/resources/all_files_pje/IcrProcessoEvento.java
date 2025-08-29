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
@Table(name = "tb_icr_processo_evento")
@org.hibernate.annotations.GenericGenerator(name = "gen_icr_processo_evento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_icr_processo_evento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class IcrProcessoEvento implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<IcrProcessoEvento,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idIcrProcessoEvento;
	private Boolean ativo;
	private InformacaoCriminalRelevante icr;
	private ProcessoEvento processoEvento;

	public IcrProcessoEvento() {

	}

	public IcrProcessoEvento(int idIcrProcessoEvento, InformacaoCriminalRelevante icr, Boolean ativo) {
		this.idIcrProcessoEvento = idIcrProcessoEvento;
		this.icr = icr;
		this.ativo = ativo;
	}

	@Id
	@GeneratedValue(generator = "gen_icr_processo_evento")
	@Column(name = "id_icr_processo_evento", unique = true, nullable = false)
	public Integer getIdIcrProcessoEvento() {
		return idIcrProcessoEvento;
	}

	public void setIdIcrProcessoEvento(Integer idIcrProcessoEvento) {
		this.idIcrProcessoEvento = idIcrProcessoEvento;
	}

	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_icr", nullable = false)
	@NotNull
	public InformacaoCriminalRelevante getIcr() {
		return icr;
	}

	public void setIcr(InformacaoCriminalRelevante icr) {
		this.icr = icr;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_evento", nullable = false)
	@NotNull
	public ProcessoEvento getProcessoEvento() {
		return processoEvento;
	}

	public void setProcessoEvento(ProcessoEvento processoEvento) {
		this.processoEvento = processoEvento;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends IcrProcessoEvento> getEntityClass() {
		return IcrProcessoEvento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdIcrProcessoEvento();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
