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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_icr_encerr_trans_penal")
@PrimaryKeyJoinColumn(name = "id_icr_encrmnto_trnsacao_penal")
public class IcrEncerramentoDeTransacaoPenal extends InformacaoCriminalRelevante implements Serializable {
	private IcrTransacaoPenal transacaoPenal;
	private MotivoEncerramentoTransacaoPenal motivoEncerramento;
	private static final long serialVersionUID = -3239443255576984154L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_icr_transacao_penal", nullable = false)
	@NotNull
	public IcrTransacaoPenal getTransacaoPenal() {
		return transacaoPenal;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_motivo_encerramento", nullable = false)
	public MotivoEncerramentoTransacaoPenal getMotivoEncerramento() {
		return motivoEncerramento;
	}

	public void setTransacaoPenal(IcrTransacaoPenal transacaoPenal) {
		this.transacaoPenal = transacaoPenal;
	}

	public void setMotivoEncerramento(MotivoEncerramentoTransacaoPenal motivoEncerramento) {
		this.motivoEncerramento = motivoEncerramento;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrEncerramentoDeTransacaoPenal.class;
	}
}
