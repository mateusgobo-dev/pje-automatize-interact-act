package br.jus.pje.nucleo.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_log_consulta_expediente")
public class LogConsultaExpediente implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<LogConsultaExpediente,Integer> {

	private static final long serialVersionUID = -8517779582343020403L;
	
	private Integer idLog;
	private String urlRequisicao;
	private ProcessoParteExpediente processoParteExpediente;
	private String ip;
	private Date dataLog;
	private String idProcessoParteExpedienteCriptografadoHex;

	public LogConsultaExpediente() {
	}

	@org.hibernate.annotations.GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_log_consulta_expediente"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id_log_consulta_expediente", unique = true, nullable = false)
	public Integer getIdLog() {
		return idLog;
	}

	public void setIdLog(Integer idLog) {
		this.idLog = idLog;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte_expediente")
	public ProcessoParteExpediente getProcessoParteExpediente() {
		return this.processoParteExpediente;
	}

	public void setProcessoParteExpediente(ProcessoParteExpediente processoParteExpediente) {
		this.processoParteExpediente = processoParteExpediente;
	}

	@Column(name = "id_pagina", length = 200)
	@Length(max = 200)
	public String getUrlRequisicao() {
		return urlRequisicao;
	}

	public void setUrlRequisicao(String urlRequisicao) {
		this.urlRequisicao = urlRequisicao;
	}

	@Column(name = "ds_ip", length = 39)
	@Length(max = 39)
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_log", nullable = false)
	@NotNull
	public Date getDataLog() {
		return dataLog;
	}

	public void setDataLog(Date dataLog) {
		this.dataLog = dataLog;
	}

	@Column(name = "ds_codigo", length = 50)
	@Length(max = 50)
	public String getCodigo() {
		return idProcessoParteExpedienteCriptografadoHex;
	}

	public void setCodigo(String codigo) {
		this.idProcessoParteExpedienteCriptografadoHex = codigo;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends LogConsultaExpediente> getEntityClass() {
		return LogConsultaExpediente.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdLog();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
