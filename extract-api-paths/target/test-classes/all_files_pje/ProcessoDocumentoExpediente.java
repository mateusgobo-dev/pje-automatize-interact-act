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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;


@Entity
@Table(name = "tb_proc_doc_expediente")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_documnto_expediente", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_documnto_expediente"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoDocumentoExpediente implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoDocumentoExpediente,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoDocumentoExpediente;
	private ProcessoExpediente processoExpediente;
	private ProcessoDocumento processoDocumento;
	private ProcessoDocumento processoDocumentoAto;
	private Boolean anexo;
	private Date dtImpressao;

	public ProcessoDocumentoExpediente() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_documnto_expediente")
	@Column(name = "id_proc_documento_expediente", unique = true, nullable = false)
	public int getIdProcessoDocumentoExpediente() {
		return this.idProcessoDocumentoExpediente;
	}

	public void setIdProcessoDocumentoExpediente(int idProcessoDocumentoExpediente) {
		this.idProcessoDocumentoExpediente = idProcessoDocumentoExpediente;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_expediente", nullable = false)
	@NotNull
	public ProcessoExpediente getProcessoExpediente() {
		return this.processoExpediente;
	}

	public void setProcessoExpediente(ProcessoExpediente processoExpediente) {
		this.processoExpediente = processoExpediente;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento", nullable = false)
	@NotNull
	public ProcessoDocumento getProcessoDocumento() {
		return this.processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@Column(name = "in_anexo")
	public Boolean getAnexo() {
		return this.anexo;
	}

	public void setAnexo(Boolean anexo) {
		this.anexo = anexo;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento_ato")
	public ProcessoDocumento getProcessoDocumentoAto() {
		return processoDocumentoAto;
	}

	public void setProcessoDocumentoAto(ProcessoDocumento processoDocumentoAto) {
		this.processoDocumentoAto = processoDocumentoAto;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_impressao")
	public Date getDtImpressao() {
		return dtImpressao;
	}

	public void setDtImpressao(Date dtImpressao) {
		this.dtImpressao = dtImpressao;
	}

	@Transient
	public Boolean getImpresso() {
		return getDtImpressao() != null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoDocumentoExpediente)) {
			return false;
		}
		ProcessoDocumentoExpediente other = (ProcessoDocumentoExpediente) obj;
		if (getIdProcessoDocumentoExpediente() != other.getIdProcessoDocumentoExpediente()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoDocumentoExpediente();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumentoExpediente> getEntityClass() {
		return ProcessoDocumentoExpediente.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoDocumentoExpediente());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
