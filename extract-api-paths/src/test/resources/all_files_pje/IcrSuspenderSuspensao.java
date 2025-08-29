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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_icr_suspender_suspensao")
@PrimaryKeyJoinColumn(name = "id_icr_suspender_suspensao")
public class IcrSuspenderSuspensao extends InformacaoCriminalRelevante {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6361548421974606997L;

	private PessoaMagistrado pessoaMagistrado;
	private String motivoSuspensao;
	private IcrSuspensao icrAfetada;

	@ManyToOne
	@JoinColumn(name = "id_pessoa_magistrado", nullable = false)
	@NotNull
	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}

	@Column(name = "ds_motivo_suspensao", nullable = false)
	@NotNull
	public String getMotivoSuspensao() {
		return motivoSuspensao;
	}

	public void setMotivoSuspensao(String motivoSuspensao) {
		this.motivoSuspensao = motivoSuspensao;
	}

	@ManyToOne
	@JoinColumn(name = "id_icr_suspensao")
	public IcrSuspensao getIcrAfetada() {
		return icrAfetada;
	}

	public void setIcrAfetada(IcrSuspensao icrAfetada) {
		this.icrAfetada = icrAfetada;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrSuspenderSuspensao.class;
	}
}
