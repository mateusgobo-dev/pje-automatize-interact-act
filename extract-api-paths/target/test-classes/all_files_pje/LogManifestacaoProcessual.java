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
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = LogManifestacaoProcessual.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_log_manifest_processual", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_log_manifest_processual"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class LogManifestacaoProcessual implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<LogManifestacaoProcessual,Long> {

	private static final long serialVersionUID = -8335729978644915251L;

	public static final String TABLE_NAME = "tb_log_manifest_processual";

	private Long idLogManifestacaoProcessual;
	private Date dataProcessamento;
	private String mensagem;
	private String status;
	private ManifestacaoProcessual manifestacaoProcessual;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "gen_log_manifest_processual")
	@Column(name = "id_log_manifest_processual", unique = true, nullable = false)
	public Long getIdLogManifestacaoProcessual() {
		return this.idLogManifestacaoProcessual;
	}

	public void setIdLogManifestacaoProcessual(Long idLogManifestacaoProcessual) {
		this.idLogManifestacaoProcessual = idLogManifestacaoProcessual;
	}

	@Column(name = "dt_processamento", nullable = false)
	public Date getDataProcessamento() {
		return dataProcessamento;
	}

	public void setDataProcessamento(Date dataProcessamento) {
		this.dataProcessamento = dataProcessamento;
	}

	@Column(name = "ds_mensagem", nullable = false)
	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	@Column(name = "in_status", nullable = false)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_manifestacao_processual", nullable = false)
	@NotNull
	public ManifestacaoProcessual getManifestacaoProcessual() {
		return manifestacaoProcessual;
	}

	public void setManifestacaoProcessual(ManifestacaoProcessual manifestacaoProcessual) {
		this.manifestacaoProcessual = manifestacaoProcessual;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends LogManifestacaoProcessual> getEntityClass() {
		return LogManifestacaoProcessual.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdLogManifestacaoProcessual();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
