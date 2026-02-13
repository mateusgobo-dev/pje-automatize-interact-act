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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_tp_proc_doc_tipo_pessoa")
@org.hibernate.annotations.GenericGenerator(name = "gen_tp_proc_doc_tipo_pessoa", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tp_proc_doc_tipo_pessoa"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoProcessoDocumentoTipoPessoa implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoProcessoDocumentoTipoPessoa,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idTipoProcessoDocumentoTipoPessoa;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private TipoPessoa tipoPessoa;

	public TipoProcessoDocumentoTipoPessoa() {
	}

	@Id
	@GeneratedValue(generator = "gen_tp_proc_doc_tipo_pessoa")
	@Column(name = "id_tp_proc_dcmento_tipo_pessoa", unique = true, nullable = false)
	public Integer getIdTipoProcessoDocumentoTipoPessoa() {
		return this.idTipoProcessoDocumentoTipoPessoa;
	}

	public void setIdTipoProcessoDocumentoTipoPessoa(Integer idTipoProcessoDocumentoTipoPessoa) {
		this.idTipoProcessoDocumentoTipoPessoa = idTipoProcessoDocumentoTipoPessoa;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_processo_documento", nullable = false)
	@NotNull
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return this.tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_pessoa", nullable = false)
	@NotNull
	public TipoPessoa getTipoPessoa() {
		return this.tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getIdTipoProcessoDocumentoTipoPessoa() == null) {
			return false;
		}
		if (!(obj instanceof TipoProcessoDocumentoTipoPessoa)) {
			return false;
		}
		TipoProcessoDocumentoTipoPessoa other = (TipoProcessoDocumentoTipoPessoa) obj;
		if (!idTipoProcessoDocumentoTipoPessoa.equals(other.getIdTipoProcessoDocumentoTipoPessoa())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((getIdTipoProcessoDocumentoTipoPessoa() == null) ? 0 : getIdTipoProcessoDocumentoTipoPessoa()
						.hashCode());
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoProcessoDocumentoTipoPessoa> getEntityClass() {
		return TipoProcessoDocumentoTipoPessoa.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdTipoProcessoDocumentoTipoPessoa();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
