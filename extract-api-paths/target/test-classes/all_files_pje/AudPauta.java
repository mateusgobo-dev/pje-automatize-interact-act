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
package br.jus.pje.jt.entidades;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vs_aud_pauta")
//@PrimaryKeyJoinColumn(name = "id_processo_audiencia")
public class AudPauta implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcessoAudiencia;
	private int idClasseJudicial;
	private int idProcesso;
	private String classeJudicial;
	private int idOrgaoJulgador;
	private int vara;
	private int idTipoAudiencia;
	private String tipoAudiencia;
	private Date dataAudiencia;
	private int numeroProcesso;
	private int anoProcesso;
	private int origemProcesso;
	private int orgaoJustica;
	private int regional;
	private int dv;

	@Id
	@Column(name = "id_processo_audiencia", nullable = false, insertable = false, updatable = false)
	public int getIdProcessoAudiencia() {
		return idProcessoAudiencia;
	}

	public void setIdProcessoAudiencia(int idProcessoAudiencia) {
		this.idProcessoAudiencia = idProcessoAudiencia;
	}

	@Column(name = "id_processo_trf", nullable = false, insertable = false, updatable = false)
	public int getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(int idProcesso) {
		this.idProcesso = idProcesso;
	}

	public void setIdClasseJudicial(int idClasseJudicial) {
		this.idClasseJudicial = idClasseJudicial;
	}

	@Column(name = "id_classe_judicial", nullable = false, insertable = false, updatable = false)
	public int getIdClasseJudicial() {
		return idClasseJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@Column(name = "ds_classe_judicial", insertable = false, updatable = false)
	public String getClasseJudicial() {
		return classeJudicial;
	}

	@Column(name = "id_orgao_julgador", nullable = false, insertable = false, updatable = false)
	public int getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}

	public void setIdOrgaoJulgador(int idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
	}

	@Column(name = "nr_vara", insertable = false, updatable = false)
	public int getVara() {
		return vara;
	}

	public void setVara(int vara) {
		this.vara = vara;
	}

	@Column(name = "id_tipo_audiencia", nullable = false, insertable = false, updatable = false)
	public int getIdTipoAudiencia() {
		return idTipoAudiencia;
	}

	public void setIdTipoAudiencia(int idTipoAudiencia) {
		this.idTipoAudiencia = idTipoAudiencia;
	}

	@Column(name = "ds_tipo_audiencia", insertable = false, updatable = false)
	public String getTipoAudiencia() {
		return tipoAudiencia;
	}

	public void setTipoAudiencia(String tipoAudiencia) {
		this.tipoAudiencia = tipoAudiencia;
	}

	@Column(name = "dt_inicio", insertable = false, updatable = false)
	public Date getDataAudiencia() {
		return dataAudiencia;
	}

	public void setDataAudiencia(Date dataAudiencia) {
		this.dataAudiencia = dataAudiencia;
	}

	@Column(name = "nr_sequencia", nullable = false, insertable = false, updatable = false)
	public int getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(int numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	@Column(name = "nr_ano", nullable = false, insertable = false, updatable = false)
	public int getAnoProcesso() {
		return anoProcesso;
	}

	public void setAnoProcesso(int anoProcesso) {
		this.anoProcesso = anoProcesso;
	}

	@Column(name = "nr_origem_processo", nullable = false, insertable = false, updatable = false)
	public int getOrigemProcesso() {
		return origemProcesso;
	}

	public void setOrigemProcesso(int origemProcesso) {
		this.origemProcesso = origemProcesso;
	}

	@Column(name = "nr_identificacao_orgao_justica", nullable = false, insertable = false, updatable = false)
	public int getOrgaoJustica() {
		return orgaoJustica;
	}

	public void setOrgaoJustica(int orgaoJustica) {
		this.orgaoJustica = orgaoJustica;
	}

	public void setRegional(int regional) {
		this.regional = regional;
	}

	@Column(name = "nr_identificacao_orgao_justica", nullable = false, insertable = false, updatable = false)
	public int getRegional() {
		return regional;
	}

	@Column(name = "nr_digito_verificador", nullable = false, insertable = false, updatable = false)
	public int getDv() {
		return dv;
	}

	public void setDv(int dv) {
		this.dv = dv;
	}
}
