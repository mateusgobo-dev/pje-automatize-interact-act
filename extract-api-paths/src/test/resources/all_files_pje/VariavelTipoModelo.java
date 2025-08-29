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
@Table(name = "tb_variavel_tipo_modelo")
@org.hibernate.annotations.GenericGenerator(name = "gen_variavel_tipo_modelo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_variavel_tipo_modelo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class VariavelTipoModelo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<VariavelTipoModelo,Integer> {

	private static final long serialVersionUID = 1L;
	private Integer idVariavelTipoModelo;
	private Variavel variavel;
	private TipoModeloDocumento tipoModeloDocumento;

	@Id
	@GeneratedValue(generator = "gen_variavel_tipo_modelo")
	@Column(name = "id_variavel_tipo_modelo", unique = true, nullable = false)
	public Integer getIdVariavelTipoModelo() {
		return idVariavelTipoModelo;
	}

	public void setIdVariavelTipoModelo(Integer idVariavelTipoModelo) {
		this.idVariavelTipoModelo = idVariavelTipoModelo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_variavel", nullable = false)
	@NotNull
	public Variavel getVariavel() {
		return variavel;
	}

	public void setVariavel(Variavel variavel) {
		this.variavel = variavel;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_modelo_documento", nullable = false)
	@NotNull
	public TipoModeloDocumento getTipoModeloDocumento() {
		return tipoModeloDocumento;
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
		if (getIdVariavelTipoModelo() == null) {
			return false;
		}
		if (!(obj instanceof VariavelTipoModelo)) {
			return false;
		}
		VariavelTipoModelo other = (VariavelTipoModelo) obj;
		if (other.getIdVariavelTipoModelo() == null
				|| (getIdVariavelTipoModelo().intValue() != other.getIdVariavelTipoModelo().intValue())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdVariavelTipoModelo();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends VariavelTipoModelo> getEntityClass() {
		return VariavelTipoModelo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdVariavelTipoModelo();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
