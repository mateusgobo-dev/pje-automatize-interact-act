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

import br.jus.pje.nucleo.entidades.identidade.Papel;

@Entity
@Table(name = TipoModeloDocumentoPapel.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_tp_mdlo_documento_papel", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tp_mdlo_documento_papel"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoModeloDocumentoPapel implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoModeloDocumentoPapel,Integer> {

	public static final String TABLE_NAME = "tb_tipo_modelo_doc_papel";

	private static final long serialVersionUID = 1L;

	private int idTipoModeloDocumentoPapel;
	private TipoModeloDocumento tipoModeloDocumento;
	private Papel papel;

	@Id
	@GeneratedValue(generator = "gen_tp_mdlo_documento_papel")
	@Column(name = "id_tipo_modelo_documento_papel", unique = true, nullable = false)
	public int getIdTipoModeloDocumentoPapel() {
		return idTipoModeloDocumentoPapel;
	}

	public void setIdTipoModeloDocumentoPapel(int idTipoModeloDocumentoPapel) {
		this.idTipoModeloDocumentoPapel = idTipoModeloDocumentoPapel;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_modelo_documento", nullable = false)
	public TipoModeloDocumento getTipoModeloDocumento() {
		return tipoModeloDocumento;
	}

	public void setTipoModeloDocumento(TipoModeloDocumento tipoModeloDocumento) {
		this.tipoModeloDocumento = tipoModeloDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_papel", nullable = false)
	public Papel getPapel() {
		return papel;
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	@Override
	public String toString() {
		return tipoModeloDocumento + " / " + papel;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoModeloDocumentoPapel)) {
			return false;
		}
		TipoModeloDocumentoPapel other = (TipoModeloDocumentoPapel) obj;
		if (getIdTipoModeloDocumentoPapel() != other.getIdTipoModeloDocumentoPapel()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoModeloDocumentoPapel();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoModeloDocumentoPapel> getEntityClass() {
		return TipoModeloDocumentoPapel.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoModeloDocumentoPapel());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
