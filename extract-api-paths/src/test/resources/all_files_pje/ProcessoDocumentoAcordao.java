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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Entidade para representar os documentos selecionados para participar da composição de um acórdão.
 */
@Entity
@Table(name = "tb_proc_docs_elaboracao_acordao")
@Inheritance(strategy = InheritanceType.JOINED)
public class ProcessoDocumentoAcordao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoDocumentoAcordao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoDocumentosAcordao;
	private Date dataJuntadaAcordao;
	private int ordemDocumento;
	private ProcessoDocumento processoDocumento;
	private ProcessoDocumento processoDocumentoAcordao;


	@org.hibernate.annotations.GenericGenerator(name = "gen_processo_documento_acordao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_docs_elaboracao_acordao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Id
	@GeneratedValue(generator = "gen_processo_documento_acordao")
	@Column(name = "id_proc_docs_elaboracao_acordao", unique = true, nullable = false)
	@NotNull
	public int getIdProcessoDocumentosAcordao() {
		return this.idProcessoDocumentosAcordao;
	}

	public void setIdProcessoDocumentosAcordao(int idProcessoDocumentosAcordao) {
		this.idProcessoDocumentosAcordao = idProcessoDocumentosAcordao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento", nullable = false)
	@NotNull
	public ProcessoDocumento getProcessoDocumento() {
		return this.processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_juntada_acordao")
	public Date getDataJuntadaAcordao() {
		return this.dataJuntadaAcordao;
	}

	public void setDataJuntadaAcordao(Date dataAcordao) {
		this.dataJuntadaAcordao = dataAcordao;
	}

	@Column(name = "ordem_doc")
	public int getOrdemDocumento() {
		return this.ordemDocumento;
	}

	public void setOrdemDocumento(int ordemDocumento) {
		this.ordemDocumento = ordemDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento_acordao")
	public ProcessoDocumento getProcessoDocumentoAcordao() {
		return this.processoDocumentoAcordao;
	}

	public void setProcessoDocumentoAcordao(
			ProcessoDocumento processoDocumentoAcordao) {
		this.processoDocumentoAcordao = processoDocumentoAcordao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoDocumentosAcordao();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		ProcessoDocumentoAcordao entity = (ProcessoDocumentoAcordao) obj;
		if (this.getIdProcessoDocumentosAcordao() == 0 && entity.getIdProcessoDocumentosAcordao() == 0) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
		EqualsBuilder builder = new EqualsBuilder();
		builder.appendSuper(super.equals(obj));
		builder.append(getIdProcessoDocumentosAcordao(), entity.getIdProcessoDocumentosAcordao());
		return builder.isEquals();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumentoAcordao> getEntityClass() {
		return ProcessoDocumentoAcordao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoDocumentosAcordao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
