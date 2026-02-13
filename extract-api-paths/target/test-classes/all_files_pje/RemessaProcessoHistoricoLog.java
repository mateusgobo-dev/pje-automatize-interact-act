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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

/**
 * Entidade que faz a relação 1x1 com Processo do core
 */
@Entity
@Table(name = RemessaProcessoHistoricoLog.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_remessa_proc_hist_log", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_remessa_proc_hist_log"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RemessaProcessoHistoricoLog implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RemessaProcessoHistoricoLog,Integer> {

	public static final String TABLE_NAME = "tb_remessa_proc_hist_log";
	private static final long serialVersionUID = 1L;

	private int idRemessaProcessoHistoricoLog;
	private Date dataCadastro;
	private RemessaProcessoHistorico remessaProcessoHistorico;
	private String analisePreventiva;
	private String logRemessa;
	private String logErro;
	private Integer totalOperacoes;
	private Integer quantidadeOperacoes;
	private Pessoa usuarioRemessa;

	public RemessaProcessoHistoricoLog() {
	}

	@Id
	@GeneratedValue(generator = "gen_remessa_proc_hist_log")
	@Column(name = "id_remessa_processo_hist_log", unique = true, nullable = false)
	public int getIdRemessaProcessoHistoricoLog() {
		return this.idRemessaProcessoHistoricoLog;
	}

	public void setIdRemessaProcessoHistoricoLog(int idRemessaProcessoHistoricoLog) {
		this.idRemessaProcessoHistoricoLog = idRemessaProcessoHistoricoLog;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cadastro")
	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_remessa_processo_historico", nullable = false)
	@NotNull
	public RemessaProcessoHistorico getRemessaProcessoHistorico() {
		return remessaProcessoHistorico;
	}

	public void setRemessaProcessoHistorico(RemessaProcessoHistorico remessaProcessoHistorico) {
		this.remessaProcessoHistorico = remessaProcessoHistorico;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_analise_preventiva")
	public String getAnalisePreventiva() {
		return analisePreventiva;
	}

	public void setAnalisePreventiva(String analisePreventiva) {
		this.analisePreventiva = analisePreventiva;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_log_remessa")
	public String getLogRemessa() {
		return logRemessa;
	}

	public void setLogRemessa(String logRemessa) {
		this.logRemessa = logRemessa;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_log_erro")
	public String getLogErro() {
		return logErro;
	}

	public void setLogErro(String logErro) {
		this.logErro = logErro;
	}

	@Column(name = "qt_possiveis_operacoes")
	public Integer getTotalOperacoes() {
		return totalOperacoes;
	}

	public void setTotalOperacoes(Integer totalOperacoes) {
		this.totalOperacoes = totalOperacoes;
	}

	@Column(name = "qt_operacoes")
	public Integer getQuantidadeOperacoes() {
		return quantidadeOperacoes;
	}

	public void setQuantidadeOperacoes(Integer quantidadeOperacoes) {
		this.quantidadeOperacoes = quantidadeOperacoes;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa")
	public Pessoa getUsuarioRemessa() {
		return usuarioRemessa;
	}

	public void setUsuarioRemessa(Pessoa usuarioRemessa) {
		this.usuarioRemessa = usuarioRemessa;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RemessaProcessoHistoricoLog> getEntityClass() {
		return RemessaProcessoHistoricoLog.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRemessaProcessoHistoricoLog());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
