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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = "tb_modelo_documento")
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_modelo_documento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_modelo_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ModeloDocumento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ModeloDocumento,Integer> {

	private static final long serialVersionUID = 1L;

	private int idModeloDocumento;
	private TipoModeloDocumento tipoModeloDocumento;
	private String tituloModeloDocumento;
	private String modeloDocumento;
	private Boolean ativo = Boolean.TRUE;

	public ModeloDocumento() {
	}

	@Id
	@GeneratedValue(generator = "gen_modelo_documento")
	@Column(name = "id_modelo_documento", unique = true, nullable = false)
	public int getIdModeloDocumento() {
		return this.idModeloDocumento;
	}

	public void setIdModeloDocumento(int idModeloDocumento) {
		this.idModeloDocumento = idModeloDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_modelo_documento", nullable = false)
	@NotNull
	public TipoModeloDocumento getTipoModeloDocumento() {
		return this.tipoModeloDocumento;
	}

	public void setTipoModeloDocumento(TipoModeloDocumento tipoModeloDocumento) {
		this.tipoModeloDocumento = tipoModeloDocumento;
	}

	@Column(name = "ds_titulo_modelo_documento", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getTituloModeloDocumento() {
		return this.tituloModeloDocumento;
	}

	public void setTituloModeloDocumento(String tituloModeloDocumento) {
		this.tituloModeloDocumento = tituloModeloDocumento;
	}

	@Transient
	public String getTituloModeloDocumentoComId(){
		return getTituloModeloDocumento()+" ("+getIdModeloDocumento()+")";
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_modelo_documento", nullable = false)
	@NotNull
	public String getModeloDocumento() {
		return this.modeloDocumento;
	}

	public void setModeloDocumento(String modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return tituloModeloDocumento;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ModeloDocumento)) {
			return false;
		}
		ModeloDocumento other = (ModeloDocumento) obj;
		if (getIdModeloDocumento() != other.getIdModeloDocumento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdModeloDocumento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ModeloDocumento> getEntityClass() {
		return ModeloDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdModeloDocumento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
