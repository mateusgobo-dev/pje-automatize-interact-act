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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;

@Entity
@Table(name = "tb_proc_parte_historico")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_parte_historico", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_parte_historico"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoParteHistorico implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoParteHistorico,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoParteHistorico;
	private ProcessoParte processoParte;
	private UsuarioLogin usuarioLogin;
	private Date dataHistorico;
	private String justificativa;
	private ProcessoParteSituacaoEnum inSituacao;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "gen_proc_parte_historico")
	@Column(name = "id_processo_parte_historico", unique = true, nullable = false)
	public int getIdProcessoParteHistorico() {
		return idProcessoParteHistorico;
	}

	public void setIdProcessoParteHistorico(int idProcessoParteHistorico) {
		this.idProcessoParteHistorico = idProcessoParteHistorico;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_parte", nullable = false)
	@NotNull
	public ProcessoParte getProcessoParte() {
		return this.processoParte;
	}

	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_usuario", nullable = false)
	@NotNull
	public UsuarioLogin getUsuarioLogin() {
		return this.usuarioLogin;
	}

	public void setUsuarioLogin(UsuarioLogin usuarioLogin) {
		this.usuarioLogin = usuarioLogin;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_historico")
	public Date getDataHistorico() {
		return dataHistorico;
	}

	public void setDataHistorico(Date dataHistorico) {
		this.dataHistorico = dataHistorico;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_justificativa")
	public String getJustificativa() {
		return justificativa;
	}

	public void setJustificativa(String justificativa) {
		this.justificativa = justificativa;
	}

	@Column(name = "in_situacao", length = 1)
	@Enumerated(EnumType.STRING)
	public ProcessoParteSituacaoEnum getInSituacao() {
		return inSituacao;
	}

	public void setInSituacao(ProcessoParteSituacaoEnum inSituacao) {
		this.inSituacao = inSituacao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoParteHistorico> getEntityClass() {
		return ProcessoParteHistorico.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoParteHistorico());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
