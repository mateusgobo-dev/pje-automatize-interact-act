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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.entidades.log.Ignore;

@Entity
@Table(name = ProcessoTrfPreventoLog.TABLE_NAME)
@Ignore
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_trf_log_prev_item", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_trf_log_prev_item"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoTrfPreventoLog implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoTrfPreventoLog,Integer> {

	public static final String TABLE_NAME = "tb_proc_trf_log_prev_item";
	private static final long serialVersionUID = 1L;

	private Integer idProcessoTrfPreventoLog;
	private ProcessoTrf processoTrf;
	ProcessoTrfLogPrevencao processoTrfLogPrevencao;

	public ProcessoTrfPreventoLog() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_trf_log_prev_item")
	@Column(name = "id_processo_trf_log_prev_item", unique = true, nullable = false)
	public Integer getIdProcessoTrfPreventoLog() {
		return this.idProcessoTrfPreventoLog;
	}

	public void setIdProcessoTrfPreventoLog(Integer idProcessoTrfPreventoLog) {
		this.idProcessoTrfPreventoLog = idProcessoTrfPreventoLog;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf_log_prev")
	public ProcessoTrfLogPrevencao getProcessoTrfLogPrevencao() {
		return processoTrfLogPrevencao;
	}

	public void setProcessoTrfLogPrevencao(ProcessoTrfLogPrevencao processoTrfLogPrevencao) {
		this.processoTrfLogPrevencao = processoTrfLogPrevencao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoTrfPreventoLog> getEntityClass() {
		return ProcessoTrfPreventoLog.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdProcessoTrfPreventoLog();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return false;
	}

}
