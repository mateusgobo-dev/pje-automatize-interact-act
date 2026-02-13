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
package br.jus.pje.nucleo.entidades.ws.consulta;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "vs_proc_parte_expdnte_websrvce")
public class ConsultaProcessoParteExpedienteWebservice implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcessoParteExpediente;
	private String nomeParteExpediente;
	private int idProcessoExpediente;
	private Date dataCienciaParte;
	private int prazoLegalParte;
	private Date dataPrazoLegalParte;
	private String nomeParteCiencia;

	@Id
	@Column(name = "id_processo_parte_expediente", insertable = false, updatable = false)
	public int getIdProcessoParteExpediente() {
		return idProcessoParteExpediente;
	}

	public void setIdProcessoParteExpediente(int idProcessoParteExpediente) {
		this.idProcessoParteExpediente = idProcessoParteExpediente;
	}

	@Column(name = "parte_expediente", insertable = false, updatable = false)
	public String getNomeParteExpediente() {
		return nomeParteExpediente;
	}

	public void setNomeParteExpediente(String nomeParteExpediente) {
		this.nomeParteExpediente = nomeParteExpediente;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ciencia_parte", insertable = false, updatable = false)
	public Date getDataCienciaParte() {
		return dataCienciaParte;
	}

	public void setDataCienciaParte(Date dataCienciaParte) {
		this.dataCienciaParte = dataCienciaParte;
	}

	@Column(name = "qt_prazo_legal_parte", insertable = false, updatable = false)
	public int getPrazoLegalParte() {
		return prazoLegalParte;
	}

	public void setPrazoLegalParte(int prazoLegalParte) {
		this.prazoLegalParte = prazoLegalParte;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_prazo_legal_parte", insertable = false, updatable = false)
	public Date getDataPrazoLegalParte() {
		return dataPrazoLegalParte;
	}

	public void setDataPrazoLegalParte(Date dataPrazoLegalParte) {
		this.dataPrazoLegalParte = dataPrazoLegalParte;
	}

	@Column(name = "parte_ciencia", insertable = false, updatable = false)
	public String getNomeParteCiencia() {
		return nomeParteCiencia;
	}

	public void setNomeParteCiencia(String nomeParteCiencia) {
		this.nomeParteCiencia = nomeParteCiencia;
	}

	@Column(name = "id_processo_expediente", insertable = false, updatable = false)
	public int getIdProcessoExpediente() {
		return idProcessoExpediente;
	}

	public void setIdProcessoExpediente(int idProcessoExpediente) {
		this.idProcessoExpediente = idProcessoExpediente;
	}

}
