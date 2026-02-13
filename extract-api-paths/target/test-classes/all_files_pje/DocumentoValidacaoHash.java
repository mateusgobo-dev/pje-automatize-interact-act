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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = DocumentoValidacaoHash.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_documento_validacao_hash", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_documento_validacao_hash"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DocumentoValidacaoHash implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<DocumentoValidacaoHash,Integer> {

	public static final String TABLE_NAME = "tb_doc_validacao_hash";
	private static final long serialVersionUID = 1L;

	private int idDocumentoValidacaoHash;
	private ProcessoDocumento processoDocumento;
	private String validacaoHash;
	private Date dtAtualizacao;

	@Id
	@GeneratedValue(generator = "gen_documento_validacao_hash")
	@Column(name = "id_documento_validacao_hash", unique = true, nullable = false)
	public int getIdDocumentoValidacaoHash() {
		return idDocumentoValidacaoHash;
	}

	public void setIdDocumentoValidacaoHash(int idDocumentoValidacaoHash) {
		this.idDocumentoValidacaoHash = idDocumentoValidacaoHash;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento")
	@NotNull
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@Column(name = "ds_validacao_hash")
	public String getValidacaoHash() {
		return validacaoHash;
	}

	public void setValidacaoHash(String validacaoHash) {
		this.validacaoHash = validacaoHash;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_atualizacao")
	public Date getDtAtualizacao() {
		return dtAtualizacao;
	}

	public void setDtAtualizacao(Date dtAtualizacao) {
		this.dtAtualizacao = dtAtualizacao;
	}

	@Override
	public String toString() {
		return validacaoHash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DocumentoValidacaoHash)) {
			return false;
		}
		DocumentoValidacaoHash other = (DocumentoValidacaoHash) obj;
		if (getIdDocumentoValidacaoHash() != other.getIdDocumentoValidacaoHash()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdDocumentoValidacaoHash();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DocumentoValidacaoHash> getEntityClass() {
		return DocumentoValidacaoHash.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDocumentoValidacaoHash());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
