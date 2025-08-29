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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_grupo_modelo_documento")
@org.hibernate.annotations.GenericGenerator(name = "gen_grupo_modelo_documento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_grupo_modelo_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@Cacheable
public class GrupoModeloDocumento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<GrupoModeloDocumento,Integer> {

	private static final long serialVersionUID = 1L;

	private int idGrupoModeloDocumento;
	private String grupoModeloDocumento;
	private Boolean ativo;
	private List<ItemTipoDocumento> itemTipoDocumentoList = new ArrayList<ItemTipoDocumento>(0);
	private List<TipoModeloDocumento> tipoModeloDocumentoList = new ArrayList<TipoModeloDocumento>(0);

	public GrupoModeloDocumento() {
	}

	@Id
	@GeneratedValue(generator = "gen_grupo_modelo_documento")
	@Column(name = "id_grupo_modelo_documento", unique = true, nullable = false)
	public int getIdGrupoModeloDocumento() {
		return this.idGrupoModeloDocumento;
	}

	public void setIdGrupoModeloDocumento(int idGrupoModeloDocumento) {
		this.idGrupoModeloDocumento = idGrupoModeloDocumento;
	}

	@Column(name = "ds_grupo_modelo_documento", nullable = false, length = 50)
	@NotNull
	@Length(max = 50)
	public String getGrupoModeloDocumento() {
		return this.grupoModeloDocumento;
	}

	public void setGrupoModeloDocumento(String grupoModeloDocumento) {
		this.grupoModeloDocumento = grupoModeloDocumento;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "grupoModeloDocumento")
	public List<ItemTipoDocumento> getItemTipoDocumentoList() {
		return this.itemTipoDocumentoList;
	}

	public void setItemTipoDocumentoList(List<ItemTipoDocumento> itemTipoDocumentoList) {
		this.itemTipoDocumentoList = itemTipoDocumentoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "grupoModeloDocumento")
	public List<TipoModeloDocumento> getTipoModeloDocumentoList() {
		return this.tipoModeloDocumentoList;
	}

	public void setTipoModeloDocumentoList(List<TipoModeloDocumento> tipoModeloDocumentoList) {
		this.tipoModeloDocumentoList = tipoModeloDocumentoList;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return grupoModeloDocumento;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof GrupoModeloDocumento)) {
			return false;
		}
		GrupoModeloDocumento other = (GrupoModeloDocumento) obj;
		if (getIdGrupoModeloDocumento() != other.getIdGrupoModeloDocumento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdGrupoModeloDocumento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends GrupoModeloDocumento> getEntityClass() {
		return GrupoModeloDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdGrupoModeloDocumento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
