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


@Entity
@Table(name = "tb_proc_doc_associacao")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_doc_associacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_doc_associacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoDocumentoAssociacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoDocumentoAssociacao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoDocumentoAssociacao;
	private ProcessoDocumento processoDocumento;
	private ProcessoDocumento documentoAssociado;

	public ProcessoDocumentoAssociacao() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_doc_associacao")
	@Column(name = "id_proc_doc_associacao", unique = true, nullable = false)
	public int getIdProcessoDocumentoAssociacao() {
		return this.idProcessoDocumentoAssociacao;
	}

	public void setIdProcessoDocumentoAssociacao(int idProcessoDocumentoAssociacao) {
		this.idProcessoDocumentoAssociacao = idProcessoDocumentoAssociacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_proc_doc")
	public ProcessoDocumento getProcessoDocumento() {
		return this.processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_doc_associado")
	public ProcessoDocumento getDocumentoAssociado() {
		return this.documentoAssociado;
	}

	public void setDocumentoAssociado(ProcessoDocumento documentoAssociado) {
		this.documentoAssociado = documentoAssociado;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoDocumentoAssociacao)) {
			return false;
		}
		ProcessoDocumentoAssociacao other = (ProcessoDocumentoAssociacao) obj;
		if (getIdProcessoDocumentoAssociacao() != other.getIdProcessoDocumentoAssociacao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoDocumentoAssociacao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumentoAssociacao> getEntityClass() {
		return ProcessoDocumentoAssociacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoDocumentoAssociacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
