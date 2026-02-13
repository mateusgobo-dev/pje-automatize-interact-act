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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "vs_proc_expediente_webservice")
public class ConsultaProcessoExpedienteWebservice implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcessoExpediente;
	private int idProcessoTrf;
	private Date dataCriacaoExpediente;
	private Character meioExpedicao;
	private Boolean urgencia;
	private String nomeTipoProcessoDocumento;

	private List<ConsultaProcessoDocumentoExpedienteWebservice> documentosList;
	private List<ConsultaProcessoParteExpedienteWebservice> partesList;
	private List<ConsultaProcessoExpedienteDiligenciaWebservice> diligenciasList;

	@Id
	@Column(name = "id_processo_expediente", insertable = false, updatable = false)
	public int getIdProcessoExpediente() {
		return idProcessoExpediente;
	}

	public void setIdProcessoExpediente(int idProcessoExpediente) {
		this.idProcessoExpediente = idProcessoExpediente;
	}

	@Column(name = "id_processo_trf", insertable = false, updatable = false)
	public int getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(int idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao_expediente", insertable = false, updatable = false)
	public Date getDataCriacaoExpediente() {
		return dataCriacaoExpediente;
	}

	public void setDataCriacaoExpediente(Date dataCriacaoExpediente) {
		this.dataCriacaoExpediente = dataCriacaoExpediente;
	}

	@Column(name = "in_meio_expedicao_expediente", insertable = false, updatable = false)
	public Character getMeioExpedicao() {
		return meioExpedicao;
	}

	public void setMeioExpedicao(Character meioExpedicao) {
		this.meioExpedicao = meioExpedicao;
	}

	@Column(name = "in_urgencia", insertable = false, updatable = false)
	public Boolean getUrgencia() {
		return urgencia;
	}

	public void setUrgencia(Boolean urgencia) {
		this.urgencia = urgencia;
	}

	@Column(name = "ds_tipo_processo_documento", insertable = false, updatable = false)
	public String getNomeTipoProcessoDocumento() {
		return nomeTipoProcessoDocumento;
	}

	public void setNomeTipoProcessoDocumento(String nomeTipoProcessoDocumento) {
		this.nomeTipoProcessoDocumento = nomeTipoProcessoDocumento;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "tb_proc_doc_expediente", joinColumns = { @JoinColumn(name = "id_processo_expediente", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_proc_documento_expediente", nullable = false, updatable = false) })
	public List<ConsultaProcessoDocumentoExpedienteWebservice> getDocumentosList() {
		return documentosList;
	}

	public void setDocumentosList(List<ConsultaProcessoDocumentoExpedienteWebservice> documentosList) {
		this.documentosList = documentosList;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "tb_proc_parte_expediente", joinColumns = { @JoinColumn(name = "id_processo_expediente", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_processo_parte_expediente", nullable = false, updatable = false) })
	public List<ConsultaProcessoParteExpedienteWebservice> getPartesList() {
		return partesList;
	}

	public void setPartesList(List<ConsultaProcessoParteExpedienteWebservice> partesList) {
		this.partesList = partesList;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "tb_proc_exped_diligencia", joinColumns = { @JoinColumn(name = "id_processo_expediente", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_proc_expediente_diligencia", nullable = false, updatable = false) })
	public List<ConsultaProcessoExpedienteDiligenciaWebservice> getDiligenciasList() {
		return diligenciasList;
	}

	public void setDiligenciasList(List<ConsultaProcessoExpedienteDiligenciaWebservice> diligenciasList) {
		this.diligenciasList = diligenciasList;
	}

}
