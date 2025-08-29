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

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_tp_proc_documto_detalhe")
@org.hibernate.annotations.GenericGenerator(name = "gen_tp_proc_documnto_detalhe", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tp_proc_documnto_detalhe"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoProcessoDocumentoDetalhe implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoProcessoDocumentoDetalhe,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idTipoProcessoDocumentoDetalhe;
	private String tipoProcessoDocumentoDetalhe;
	private Boolean obrigatorio;
	private Boolean ativo;
	private TipoProcessoDocumento tipoProcessoDocumento;

	public TipoProcessoDocumentoDetalhe() {
	}

	@Id
	@GeneratedValue(generator = "gen_tp_proc_documnto_detalhe")
	@Column(name = "id_tp_proc_documento_detalhe", unique = true, nullable = false)
	public Integer getIdTipoProcessoDocumentoDetalhe() {
		return idTipoProcessoDocumentoDetalhe;
	}

	public void setIdTipoProcessoDocumentoDetalhe(Integer idTipoProcessoDocumentoDetalhe) {
		this.idTipoProcessoDocumentoDetalhe = idTipoProcessoDocumentoDetalhe;
	}

	@Column(name = "ds_tp_proc_documento_detalhe", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getTipoProcessoDocumentoDetalhe() {
		return tipoProcessoDocumentoDetalhe;
	}

	public void setTipoProcessoDocumentoDetalhe(String tipoProcessoDocumentoDetalhe) {
		this.tipoProcessoDocumentoDetalhe = tipoProcessoDocumentoDetalhe;
	}

	@Column(name = "in_obrigatorio", nullable = false)
	@NotNull
	public Boolean getObrigatorio() {
		return obrigatorio;
	}

	public void setObrigatorio(Boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_processo_documento", nullable = false)
	@NotNull
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getIdTipoProcessoDocumentoDetalhe() == null) {
			return false;
		}
		if (!(obj instanceof TipoProcessoDocumentoDetalhe)) {
			return false;
		}
		TipoProcessoDocumentoDetalhe other = (TipoProcessoDocumentoDetalhe) obj;
		if (!idTipoProcessoDocumentoDetalhe.equals(other.getIdTipoProcessoDocumentoDetalhe())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getIdTipoProcessoDocumentoDetalhe() == null) ? 0 : getIdTipoProcessoDocumentoDetalhe().hashCode());
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoProcessoDocumentoDetalhe> getEntityClass() {
		return TipoProcessoDocumentoDetalhe.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdTipoProcessoDocumentoDetalhe();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
