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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;

@Entity
@Table(name = "tb_log_consulta_processo")
@org.hibernate.annotations.GenericGenerator(name = "gen_log_consulta_processo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_log_consulta_processo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class LogConsultaProcesso implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<LogConsultaProcesso,Integer> {

	private static final long serialVersionUID = 1L;
	private Integer idLog;
	private UsuarioLogin usuario;
	private String urlRequisicao;
	private ProcessoTrf processo;
	private String ip;
	private Date dataLog;

	public LogConsultaProcesso() {
	}

	@Id
	@GeneratedValue(generator = "gen_log_consulta_processo")
	@Column(name = "id_log", unique = true, nullable = false)
	public Integer getIdLog() {
		return idLog;
	}

	public void setIdLog(Integer idLog) {
		this.idLog = idLog;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario")
	public UsuarioLogin getUsuario() {
		return this.usuario;
	}

	public void setUsuario(UsuarioLogin usuario) {
		this.usuario = usuario;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo")
	public ProcessoTrf getProcesso() {
		return this.processo;
	}

	public void setProcesso(ProcessoTrf processo) {
		this.processo = processo;
	}

	@Column(name = "id_pagina", length = 200)
	@Length(max = 200)
	public String getUrlRequisicao() {
		return urlRequisicao;
	}

	public void setUrlRequisicao(String urlRequisicao) {
		this.urlRequisicao = urlRequisicao;
	}

	@Column(name = "ds_ip", length = 15)
	@Length(max = 15)
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
	
	@Override
	@javax.persistence.Transient
	public Class<? extends LogConsultaProcesso> getEntityClass() {
		return LogConsultaProcesso.class;
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
