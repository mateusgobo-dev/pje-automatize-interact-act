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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;

@Entity
@Table(name = "tb_icr_susp_transcao_penal")
@PrimaryKeyJoinColumn(name = "id_suspensao_transacao_penal")
public class IcrSuspensaoTransacaoPenal extends InformacaoCriminalRelevante {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4099286221627126590L;
	private IcrTransacaoPenal transacaoPenal;
	private PessoaMagistrado magistrado;
	private String motivo;
	private static TipoIcrEnum[] tiposDeIcrAceitos = { TipoIcrEnum.TRP };

	@Transient
	public static TipoIcrEnum[] getTiposDeIcrAceitos() {
		return tiposDeIcrAceitos;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_icr_transacao_penal", nullable = false)
	@NotNull
	public IcrTransacaoPenal getTransacaoPenal() {
		return transacaoPenal;
	}

	public void setTransacaoPenal(IcrTransacaoPenal transacaoPenal) {
		this.transacaoPenal = transacaoPenal;
	}

	public void setMagistrado(PessoaMagistrado orgaoJulgador) {
		this.magistrado = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_magistrado", nullable = false)
	@NotNull
	public PessoaMagistrado getMagistrado() {
		return magistrado;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	@Column(name = "ds_motivo_suspensao")
	public String getMotivo() {
		return motivo;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrSuspensaoTransacaoPenal.class;
	}
}
