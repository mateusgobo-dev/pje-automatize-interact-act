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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.MotivoMovimentacaoEnum;

@Entity
@Table(name = LogHistoricoMovimentacao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_log_hist_movimentacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_log_hist_movimentacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class LogHistoricoMovimentacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<LogHistoricoMovimentacao,Integer> {

	public static final String TABLE_NAME  = "tb_log_hist_movimentacao";
	private static final long serialVersionUID = 1L;
	private Integer idLog;
	private UsuarioLogin usuario;
	private ProcessoTrf processoTrf;
	private ProcessoParteExpediente processoParteExpediente;
	private Date dataLog;
	private CaixaAdvogadoProcurador caixa;
	private MotivoMovimentacaoEnum motivoMovimentacao;
	private String textoMovimentacao;
	private String nomeCaixa;
	
	@Id
	@GeneratedValue(generator = "gen_log_hist_movimentacao")
	@Column(name = "id_log_hist_moviment", unique = true, nullable = false)
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
	@JoinColumn(name = "id_processo_trf",nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processo) {
		this.processoTrf = processo;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte_expediente")
	public ProcessoParteExpediente getProcessoParteExpediente() {
		return processoParteExpediente;
	}
	
	public void setProcessoParteExpediente(
			ProcessoParteExpediente processoParteExpediente) {
		this.processoParteExpediente = processoParteExpediente;
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

	@Column(name = "cd_motivo_movimentacao", length = 1)
	@Enumerated(EnumType.STRING)
	public MotivoMovimentacaoEnum getMotivoMovimentacao() {
		return motivoMovimentacao;
	}

	public void setMotivoMovimentacao(
			MotivoMovimentacaoEnum historicoMovimentacao) {
		this.motivoMovimentacao = historicoMovimentacao;
	}

	@Column(name = "ds_texto_movimentacao")
	public String getTextoMovimentacao() {
		return textoMovimentacao;
	}

	public void setTextoMovimentacao(String textoMovimentacao) {
		this.textoMovimentacao = textoMovimentacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_caixa_adv_proc")
	public CaixaAdvogadoProcurador getCaixa() {
		return caixa;
	}

	public void setCaixa(CaixaAdvogadoProcurador caixa) {
		this.caixa = caixa;
	}
	
	@Column(name = "nm_caixa_adv_proc")
	public String getNomeCaixa() {
		return nomeCaixa;
	}
	
	public void setNomeCaixa(String nomeCaixa) {
		this.nomeCaixa = nomeCaixa;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends LogHistoricoMovimentacao> getEntityClass() {
		return LogHistoricoMovimentacao.class;
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
