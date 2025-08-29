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
package br.jus.pje.jt.entidades;

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

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;

/**
 * Classe de representa o histórico de débito trabalhista no Cadastro Nacional
 * de Débito Trabalhista
 * 
 * @author kelly leal
 * @since versão 1.2.3
 * @category PJE-JT
 */
@Entity
@Table(name = DebitoTrabalhistaHistorico.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_dbto_trblhista_historico", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_dbto_trblhista_historico"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DebitoTrabalhistaHistorico implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<DebitoTrabalhistaHistorico,Integer> {

	public static final String TABLE_NAME = "tb_dbto_trblhsta_historico";
	private static final long serialVersionUID = 1L;

	private int idDebitoTrabalhistaHistorico;
	private ProcessoParte processoParte;
	private SituacaoDebitoTrabalhista situacaoDebitoTrabalhista;
	private TipoOperacaoEnum operacao;
	private Date dataAlteracao;
	private Date dataEnvio;
	private Pessoa usuarioResponsavel;
	private MotivoAlteracaoDebitoTrabalhista motivo;
	private String respostaEnvio;

	@Id
	@GeneratedValue(generator = "gen_dbto_trblhista_historico")
	@Column(name = "id_debto_trabalhista_historico", unique = true, nullable = false)
	public int getIdDebitoTrabalhistaHistorico() {
		return idDebitoTrabalhistaHistorico;
	}

	public void setIdDebitoTrabalhistaHistorico(int idDebitoTrabalhistaHistorico) {
		this.idDebitoTrabalhistaHistorico = idDebitoTrabalhistaHistorico;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte", nullable = false)
	@NotNull
	public ProcessoParte getProcessoParte() {
		return processoParte;
	}

	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_situacao_debito_trabalhista", nullable = false)
	@NotNull
	public SituacaoDebitoTrabalhista getSituacaoDebitoTrabalhista() {
		return situacaoDebitoTrabalhista;
	}

	public void setSituacaoDebitoTrabalhista(SituacaoDebitoTrabalhista situacao) {
		this.situacaoDebitoTrabalhista = situacao;
	}

	@Column(name = "in_tipo_operacao")
	@Enumerated(EnumType.STRING)
	public TipoOperacaoEnum getOperacao() {
		return operacao;
	}

	public void setOperacao(TipoOperacaoEnum operacao) {
		this.operacao = operacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_alteracao")
	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_envio")
	public Date getDataEnvio() {
		return dataEnvio;
	}

	public void setDataEnvio(Date dataEnvio) {
		this.dataEnvio = dataEnvio;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", nullable = false)
	@NotNull
	public Pessoa getUsuarioResponsavel() {
		return usuarioResponsavel;
	}

	public void setUsuarioResponsavel(Pessoa usuarioResponsavel) {
		this.usuarioResponsavel = usuarioResponsavel;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_motivo", nullable = true)
	public MotivoAlteracaoDebitoTrabalhista getMotivo() {
		return motivo;
	}

	public void setMotivo(MotivoAlteracaoDebitoTrabalhista motivo) {
		this.motivo = motivo;
	}

	@Column(name = "ds_resposta_envio")
	public String getRespostaEnvio() {
		return respostaEnvio;
	}

	public void setRespostaEnvio(String respostaEnvio) {
		this.respostaEnvio = respostaEnvio;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DebitoTrabalhistaHistorico> getEntityClass() {
		return DebitoTrabalhistaHistorico.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDebitoTrabalhistaHistorico());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
