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

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.nucleo.entidades.ProcessoAudiencia;

@Entity
@Table(name = ProcessoAudienciaJT.TABLE_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "id_processo_audiencia" }) })
public class ProcessoAudienciaJT implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_processo_audiencia_jt";

	private int idProcessoAudienciaJt;
	private ProcessoAudiencia processoAudiencia;
	private Boolean verificada;
	private String observacoes;

	@Id
	@Column(name = "id_processo_audiencia_jt", unique = true, nullable = false)
	@NotNull
	public int getIdProcessoAudienciaJt() {
		return idProcessoAudienciaJt;
	}

	public void setIdProcessoAudienciaJt(int idProcessoAudienciaJt) {
		this.idProcessoAudienciaJt = idProcessoAudienciaJt;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_audiencia", nullable = false)
	@ForeignKey(name = "fk_tb_proc_aud_jt_tb_proc_aud")
	@NotNull
	public ProcessoAudiencia getProcessoAudiencia() {
		return processoAudiencia;
	}

	public void setProcessoAudiencia(ProcessoAudiencia processoAudiencia) {
		this.processoAudiencia = processoAudiencia;
	}

	@Column(name = "in_verificada")
	public Boolean getVerificada() {
		return verificada;
	}

	public void setVerificada(Boolean verificada) {
		this.verificada = verificada;
	}

	@Column(name = "ds_observacoes")
	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}
}
