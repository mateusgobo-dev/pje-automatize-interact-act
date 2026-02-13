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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;

@Entity
@Table(name = "tb_icr_retmr_trnscao_penal")
@PrimaryKeyJoinColumn(name = "id_retomar_transacao_penal")
public class IcrRetomarTransacaoPenal extends InformacaoCriminalRelevante{

	private static final long serialVersionUID = 6856948177833631866L;
	private static TipoIcrEnum[] tiposDeIcrAceitos = {TipoIcrEnum.STP};
	private IcrSuspensaoTransacaoPenal suspensaoTransacaoPenal;
	private Date dataDecisao;
	private PessoaMagistrado magistrado;
	private String motivo;

	@Transient
	public static TipoIcrEnum[] getTiposDeIcrAceitos(){
		return tiposDeIcrAceitos;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_icr_suspensao_trnscao_penal", nullable = false)
	@NotNull
	public IcrSuspensaoTransacaoPenal getSuspensaoTransacaoPenal(){
		return suspensaoTransacaoPenal;
	}

	public void setSuspensaoTransacaoPenal(IcrSuspensaoTransacaoPenal suspensaoTransacaoPenal){
		this.suspensaoTransacaoPenal = suspensaoTransacaoPenal;
	}

	public void setMagistrado(PessoaMagistrado orgaoJulgador){
		this.magistrado = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_magistrado", nullable = false)
	@NotNull
	public PessoaMagistrado getMagistrado(){
		return magistrado;
	}

	public void setMotivo(String motivo){
		this.motivo = motivo;
	}

	@Column(name = "ds_motivo_retomada")
	public String getMotivo(){
		return motivo;
	}

	public void setDataDecisao(Date dataDecisao){
		this.dataDecisao = dataDecisao;
	}

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_decisao", nullable = false)
	public Date getDataDecisao(){
		return dataDecisao;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrRetomarTransacaoPenal.class;
	}
}
