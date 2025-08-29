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
@Table(name = "tb_peticao_tp_modelo_doc")
@org.hibernate.annotations.GenericGenerator(name = "gen_ptco_tp_modelo_documento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_ptco_tp_modelo_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PeticaoTipoModeloDocumento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PeticaoTipoModeloDocumento,Integer> {

	private static final long serialVersionUID = 1L;

	private int idPeticaoTipoModeloDocumento;
	private Peticao peticao;
	private TipoModeloDocumento tipoModeloDocumento;

	public PeticaoTipoModeloDocumento() {
	}

	@Id
	@GeneratedValue(generator = "gen_ptco_tp_modelo_documento")
	@Column(name = "id_peticao_tp_modelo_documento", unique = true, nullable = false)
	public int getIdPeticaoTipoModeloDocumento() {
		return this.idPeticaoTipoModeloDocumento;
	}

	public void setIdPeticaoTipoModeloDocumento(int idPeticaoTipoModeloDocumento) {
		this.idPeticaoTipoModeloDocumento = idPeticaoTipoModeloDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_peticao")
	public Peticao getPeticao() {
		return this.peticao;
	}

	public void setPeticao(Peticao peticao) {
		this.peticao = peticao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_modelo_documento")
	public TipoModeloDocumento getTipoModeloDocumento() {
		return this.tipoModeloDocumento;
	}

	public void setTipoModeloDocumento(TipoModeloDocumento tipoModeloDocumento) {
		this.tipoModeloDocumento = tipoModeloDocumento;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PeticaoTipoModeloDocumento)) {
			return false;
		}
		PeticaoTipoModeloDocumento other = (PeticaoTipoModeloDocumento) obj;
		if (getIdPeticaoTipoModeloDocumento() != other.getIdPeticaoTipoModeloDocumento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPeticaoTipoModeloDocumento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PeticaoTipoModeloDocumento> getEntityClass() {
		return PeticaoTipoModeloDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPeticaoTipoModeloDocumento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
