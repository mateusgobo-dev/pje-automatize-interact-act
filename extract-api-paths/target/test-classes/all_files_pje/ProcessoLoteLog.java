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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_processo_lote_log")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_lote_log", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_lote_log"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoLoteLog implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoLoteLog,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoLoteLog;
	private ProcessoTrf processoTrf;
	private Lote lote;
	private Date dtInclusaoProcesso;
	private Date dtExclusaoProcesso;
	private Pessoa pessoaInclusao;
	private Pessoa pessoaExclusao;

	public ProcessoLoteLog() {
	}

	@Id
	@GeneratedValue(generator = "gen_processo_lote_log")
	@Column(name = "id_processo_lote_log", unique = true, nullable = false)
	public int getIdProcessoLoteLog() {
		return this.idProcessoLoteLog;
	}

	public void setIdProcessoLoteLog(int idProcessoLoteLog) {
		this.idProcessoLoteLog = idProcessoLoteLog;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_lote", nullable = false)
	@NotNull
	public Lote getLote() {
		return this.lote;
	}

	public void setLote(Lote lote) {
		this.lote = lote;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_inclusao", nullable = false)
	@NotNull
	public Pessoa getPessoaInclusao() {
		return this.pessoaInclusao;
	}

	public void setPessoaInclusao(Pessoa pessoaInclusao) {
		this.pessoaInclusao = pessoaInclusao;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaInclusao(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaInclusao(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaInclusao(pessoa.getPessoa());
		} else {
			setPessoaInclusao((Pessoa)null);
		}
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_exclusao", nullable = false)
	@NotNull
	public Pessoa getPessoaExclusao() {
		return this.pessoaExclusao;
	}

	public void setPessoaExclusao(Pessoa pessoaExclusao) {
		this.pessoaExclusao = pessoaExclusao;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaExclusao(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaExclusao(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaExclusao(pessoa.getPessoa());
		} else {
			setPessoaExclusao((Pessoa)null);
		}
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao_processo")
	public Date getDtInclusaoProcesso() {
		return this.dtInclusaoProcesso;
	}

	public void setDtInclusaoProcesso(Date dtInclusaoProcesso) {
		this.dtInclusaoProcesso = dtInclusaoProcesso;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_exclusao_processo")
	public Date getDtExclusaoProcesso() {
		return this.dtExclusaoProcesso;
	}

	public void setDtExclusaoProcesso(Date dtExclusaoProcesso) {
		this.dtExclusaoProcesso = dtExclusaoProcesso;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoLoteLog)) {
			return false;
		}
		ProcessoLoteLog other = (ProcessoLoteLog) obj;
		if (getIdProcessoLoteLog() != other.getIdProcessoLoteLog()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoLoteLog();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoLoteLog> getEntityClass() {
		return ProcessoLoteLog.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoLoteLog());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
