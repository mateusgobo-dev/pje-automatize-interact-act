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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name="tb_manifestacao_proc_doc")
@org.hibernate.annotations.GenericGenerator(name = "gen_manifestacao_proc_doc", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_manifestacao_proc_doc"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ManifestacaoProcessualDocumento implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ManifestacaoProcessualDocumento,Long>{
	
	private static final long serialVersionUID = -9033907281126251721L;

	private Long idManifestacaoProcessualDocumento;
	
	private ManifestacaoProcessual manifestacaoProcessual;
	
	private ProcessoDocumento processoDocumento;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_manifestacao_proc_doc")
	@Column(name = "id_mfstco_processual_documento", unique = true, nullable = false)
	public Long getIdManifestacaoProcessualDocumento() {
		return idManifestacaoProcessualDocumento;
	}

	public void setIdManifestacaoProcessualDocumento(
			Long idManifestacaoProcessualDocumento) {
		this.idManifestacaoProcessualDocumento = idManifestacaoProcessualDocumento;
	}

	@ManyToOne
	@JoinColumn(name="id_manifestacao_processual")
	public ManifestacaoProcessual getManifestacaoProcessual() {
		return manifestacaoProcessual;
	}

	public void setManifestacaoProcessual(
			ManifestacaoProcessual manifestacaoProcessual) {
		this.manifestacaoProcessual = manifestacaoProcessual;
	}

	@ManyToOne
	@JoinColumn(name="id_processo_documento")
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ManifestacaoProcessualDocumento> getEntityClass() {
		return ManifestacaoProcessualDocumento.class;
	}
	
	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdManifestacaoProcessualDocumento();
	}
	
	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}
