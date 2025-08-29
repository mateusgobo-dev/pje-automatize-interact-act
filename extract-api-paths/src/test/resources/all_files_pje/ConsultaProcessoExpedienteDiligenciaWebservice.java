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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vs_proc_exped_dlgnca_websrvce")
public class ConsultaProcessoExpedienteDiligenciaWebservice implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcessoExpedienteDiligencia;
	private int idProcessoExpediente;
	private String nomeTipoDiligencia;

	@Id
	@Column(name = "id_proc_expediente_diligencia", insertable = false, updatable = false)
	public int getIdProcessoExpedienteDiligencia() {
		return idProcessoExpedienteDiligencia;
	}

	public void setIdProcessoExpedienteDiligencia(int idProcessoExpedienteDiligencia) {
		this.idProcessoExpedienteDiligencia = idProcessoExpedienteDiligencia;
	}

	@Column(name = "id_processo_expediente", insertable = false, updatable = false)
	public int getIdProcessoExpediente() {
		return idProcessoExpediente;
	}

	public void setIdProcessoExpediente(int idProcessoExpediente) {
		this.idProcessoExpediente = idProcessoExpediente;
	}

	@Column(name = "ds_tipo_diligencia", insertable = false, updatable = false)
	public String getNomeTipoDiligencia() {
		return nomeTipoDiligencia;
	}

	public void setNomeTipoDiligencia(String nomeTipoDiligencia) {
		this.nomeTipoDiligencia = nomeTipoDiligencia;
	}

}
