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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = ProcessoTrfLog.TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_trf_log", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_trf_log"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoTrfLog implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoTrfLog,Integer> {

	public static final String TABLE_NAME = "tb_processo_trf_log";
	private static final long serialVersionUID = 1L;

	private Integer idProcessoTrfLog;
	private ProcessoTrf processoTrf;
	private Date dataLog;
	private List<ItemsLog> itemsLogList = new ArrayList<ItemsLog>(0);

	public ProcessoTrfLog() {
		dataLog = new Date();
	}

	@Id
	@GeneratedValue(generator = "gen_processo_trf_log")
	@Column(name = "id_processo_trf_log", unique = true, nullable = false)
	public Integer getIdProcessoTrfLog() {
		return this.idProcessoTrfLog;
	}

	public void setIdProcessoTrfLog(Integer idProcessoTrfLog) {
		this.idProcessoTrfLog = idProcessoTrfLog;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Column(name = "dt_log", nullable = false)
	public Date getDataLog() {
		return dataLog;
	}

	public void setDataLog(Date dataLog) {
		this.dataLog = dataLog;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrfLog")
	public List<ItemsLog> getItemsLogList() {
		return this.itemsLogList;
	}

	public void setItemsLogList(List<ItemsLog> itemsLogList) {
		this.itemsLogList = itemsLogList;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoTrfLog> getEntityClass() {
		return ProcessoTrfLog.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdProcessoTrfLog();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return false;
	}

}
