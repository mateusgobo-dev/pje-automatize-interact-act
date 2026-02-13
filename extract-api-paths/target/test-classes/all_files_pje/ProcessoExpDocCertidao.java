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
import javax.validation.constraints.NotNull;


@Entity
@Table(name = "tb_proc_exped_doc_certidao")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_exped_doc_certidao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_exped_doc_certidao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoExpDocCertidao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoExpDocCertidao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcExpedienteDocCertidao;
	private ProcessoDocumento processoDocumentoCertidao;
	private ProcessoParteExpediente processoParteExpediente;

	public ProcessoExpDocCertidao() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_exped_doc_certidao")
	@Column(name = "id_proc_expedente_doc_certidao", unique = true, nullable = false)
	public int getIdProcessoExpediente() {
		return this.idProcExpedienteDocCertidao;
	}

	public void setIdProcessoExpediente(int idProcExpedienteDocCertidao) {
		this.idProcExpedienteDocCertidao = idProcExpedienteDocCertidao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento_certidao", nullable = false)
	@NotNull
	public ProcessoDocumento getProcessoDocumentoCertidao() {
		return processoDocumentoCertidao;
	}

	public void setProcessoDocumentoCertidao(ProcessoDocumento processoDocumentoCertidao) {
		this.processoDocumentoCertidao = processoDocumentoCertidao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte_expediente", nullable = false)
	@NotNull
	public ProcessoParteExpediente getProcessoParteExpediente() {
		return processoParteExpediente;
	}

	public void setProcessoParteExpediente(ProcessoParteExpediente processoParteExpediente) {
		this.processoParteExpediente = processoParteExpediente;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoExpDocCertidao> getEntityClass() {
		return ProcessoExpDocCertidao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoExpediente());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
