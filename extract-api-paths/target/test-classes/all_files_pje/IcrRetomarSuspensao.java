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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_icr_retomar_suspensao")
@PrimaryKeyJoinColumn(name = "id_icr_retomar_suspensao")
public class IcrRetomarSuspensao extends InformacaoCriminalRelevante{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6361548421974606997L;

	private Date dataDecisao;
	private PessoaMagistrado pessoaMagistrado;
	private String motivoRetomada;
	private IcrSuspenderSuspensao icrAfetada;

	@ManyToOne
	@JoinColumn(name = "id_pessoa_magistrado", nullable = false)
	@NotNull
	public PessoaMagistrado getPessoaMagistrado(){
		return pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado){
		this.pessoaMagistrado = pessoaMagistrado;
	}

	@Column(name = "ds_motivo_retomada", nullable = false)
	@NotNull
	public String getMotivoRetomada(){
		return motivoRetomada;
	}

	public void setMotivoRetomada(String motivoRetomada){
		this.motivoRetomada = motivoRetomada;
	}

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_decisao", nullable = false)
	public Date getDataDecisao(){
		return dataDecisao;
	}

	public void setDataDecisao(Date dataDecisao){
		this.dataDecisao = dataDecisao;
	}

	@ManyToOne
	@JoinColumn(name = "id_icr_suspender_suspensao")
	public IcrSuspenderSuspensao getIcrAfetada(){
		return icrAfetada;
	}

	public void setIcrAfetada(IcrSuspenderSuspensao icrAfetada){
		this.icrAfetada = icrAfetada;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrRetomarSuspensao.class;
	}
}