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
package br.jus.pje.nucleo.entidades.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.IEntidade;
import br.jus.pje.nucleo.enums.TipoOperacaoLogEnum;

@Ignore
@Entity
@Table(name = "tb_log")
@javax.persistence.Cacheable(false)
@Cache(usage = CacheConcurrencyStrategy.NONE)
public class EntityLog implements IEntidade<EntityLog, Long>, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private Long idLog;
	private Integer idUsuario;
	private String urlRequisicao;
	private String ip;
	private String nomeEntidade;
	private String nomePackage;
	private String idEntidade;
	private TipoOperacaoLogEnum tipoOperacao;
	private Date dataLog;
	private List<EntityLogDetail> entityLogDetailList = new ArrayList<EntityLogDetail>(0);

	public EntityLog() {
	}

	@Id
	@GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@Parameter(name = "sequence", value = "sq_tb_log")
		, @Parameter(name = "allocationSize", value = "-1")})
	@GeneratedValue(generator = "generator", strategy = GenerationType.AUTO)
	@Column(name = "id_log", unique = true, nullable = false, updatable = false)
	public Long getIdLog() {
		return idLog;
	}

	public void setIdLog(Long idLog) {
		this.idLog = idLog;
	}

	@Column(name = "id_usuario")
	public Integer getIdUsuario() {
		return this.idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
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

	@Column(name = "ds_entidade", length = 50)
	@Length(max = 50)
	public String getNomeEntidade() {
		return nomeEntidade;
	}

	public void setNomeEntidade(String nomeEntidade) {
		this.nomeEntidade = nomeEntidade;
	}

	@Column(name = "ds_package", length = 150)
	@Length(max = 150)
	public String getNomePackage() {
		return nomePackage;
	}

	public void setNomePackage(String nomePackage) {
		this.nomePackage = nomePackage;
	}

	@Column(name = "ds_id_entidade", length = 200)
	@Length(max = 200)
	public String getIdEntidade() {
		return idEntidade;
	}

	public void setIdEntidade(String idEntidade) {
		this.idEntidade = idEntidade;
	}

	@Column(name = "tp_operacao")
	@Enumerated(EnumType.STRING)
	public TipoOperacaoLogEnum getTipoOperacao() {
		return tipoOperacao;
	}

	public void setTipoOperacao(TipoOperacaoLogEnum tipoOperacao) {
		this.tipoOperacao = tipoOperacao;
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

	@OneToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY, mappedBy = "entityLog")
	@Cache(usage = CacheConcurrencyStrategy.NONE)
	public List<EntityLogDetail> getLogDetalheList() {
		if (entityLogDetailList == null) {
			entityLogDetailList = new ArrayList<EntityLogDetail>(1);
		}
		return entityLogDetailList;
	}

	public void setLogDetalheList(List<EntityLogDetail> logDetalheList) {
		this.entityLogDetailList = logDetalheList;
	}

	@Override
	public String toString() {
		return nomeEntidade;
	}

	@Override
	@Transient
	public Class<? extends EntityLog> getEntityClass() {
		return EntityLog.class;
	}

	@Override
	@Transient
	public Long getEntityIdObject() {
		return idLog;
	}

	@Override
	@Transient
	public boolean isLoggable() {
		return false;
	}

}
