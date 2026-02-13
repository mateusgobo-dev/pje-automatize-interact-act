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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


@Entity
@Table(name = "tb_unificacao_proc_parte_exp")
@org.hibernate.annotations.GenericGenerator(name = "gen_unificacao_proc_parte_exp", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_unificacao_proc_parte_exp"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoParteExpedienteHistorico implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoParteExpedienteHistorico,Integer> {
	
	private static final long serialVersionUID = 1L;

	private int idProcessoParteExpedienteHistorico;
	private ProcessoParteExpediente processoParteExpediente;
	private UnificacaoPessoas unificacao;
	private Boolean ativo = Boolean.TRUE;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "gen_unificacao_proc_parte_exp")
	@Column(name = "id_proc_parte_exp_historico")
	public int getIdProcessoParteExpedienteHistorico() {
		return idProcessoParteExpedienteHistorico;
	}
	
	public void setIdProcessoParteExpedienteHistorico(int idProcessoParteExpedienteHistorico){
		this.idProcessoParteExpedienteHistorico = idProcessoParteExpedienteHistorico;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte_expediente", nullable = false)	
	public ProcessoParteExpediente getProcessoParteExpediente() {
		return processoParteExpediente;
	}

	public void setProcessoParteExpediente(ProcessoParteExpediente processoParteExpediente) {
		this.processoParteExpediente = processoParteExpediente;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_unificacao_pessoas", nullable = false)
	@NotNull	
	public UnificacaoPessoas getUnificacao() {
		return this.unificacao;
	}

	public void setUnificacao(UnificacaoPessoas unificacao) {
		this.unificacao = unificacao;
	}	

	@Column(name = "unificacao_ativo", nullable = false)	
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoParteExpedienteHistorico> getEntityClass() {
		return ProcessoParteExpedienteHistorico.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoParteExpedienteHistorico());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
