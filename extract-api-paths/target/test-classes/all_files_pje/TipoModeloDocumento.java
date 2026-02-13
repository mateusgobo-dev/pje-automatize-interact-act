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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.identidade.Papel;

@Entity
@Table(name = "tb_tipo_modelo_documento")
@Cacheable
public class TipoModeloDocumento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoModeloDocumento,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoModeloDocumento;
	private GrupoModeloDocumento grupoModeloDocumento;
	private String tipoModeloDocumento;
	private String abreviacao;
	private Boolean ativo;
	private TipoModeloDocumento tipoModeloDocumentoCombo;

	private List<ModeloDocumento> modeloDocumentoList = new ArrayList<ModeloDocumento>(0);

	private List<VariavelTipoModelo> variavelTipoModeloList = new ArrayList<VariavelTipoModelo>(0);

	private List<Papel> papeis = new ArrayList<Papel>(0);

	public TipoModeloDocumento() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_modelo_documento")
	@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_modelo_documento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_modelo_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Column(name = "id_tipo_modelo_documento", unique = true, nullable = false)
	public int getIdTipoModeloDocumento() {
		return this.idTipoModeloDocumento;
	}

	public void setIdTipoModeloDocumento(int idTipoModeloDocumento) {
		this.idTipoModeloDocumento = idTipoModeloDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_grupo_modelo_documento", nullable = false)
	@NotNull
	public GrupoModeloDocumento getGrupoModeloDocumento() {
		return this.grupoModeloDocumento;
	}

	public void setGrupoModeloDocumento(GrupoModeloDocumento grupoModeloDocumento) {
		this.grupoModeloDocumento = grupoModeloDocumento;
	}

	@Column(name = "ds_tipo_modelo_documento", nullable = false, length = 50)
	@NotNull
	@Length(max = 50)
	public String getTipoModeloDocumento() {
		return this.tipoModeloDocumento;
	}

	public void setTipoModeloDocumento(String tipoModeloDocumento) {
		this.tipoModeloDocumento = tipoModeloDocumento;
	}

	@Column(name = "ds_abreviacao", nullable = false, length = 50)
	@NotNull
	@Length(max = 50)
	public String getAbreviacao() {
		return this.abreviacao;
	}

	public void setAbreviacao(String abreviacao) {
		this.abreviacao = abreviacao;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "tipoModeloDocumento")
	public List<ModeloDocumento> getModeloDocumentoList() {
		return this.modeloDocumentoList;
	}

	public void setModeloDocumentoList(List<ModeloDocumento> modeloDocumentoList) {
		this.modeloDocumentoList = modeloDocumentoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "tipoModeloDocumento")
	public List<VariavelTipoModelo> getVariavelTipoModeloList() {
		return variavelTipoModeloList;
	}

	public void setVariavelTipoModeloList(List<VariavelTipoModelo> variavelTipoModeloList) {
		this.variavelTipoModeloList = variavelTipoModeloList;
	}

	@OneToMany(cascade = { CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = TipoModeloDocumentoPapel.TABLE_NAME, joinColumns = { @JoinColumn(name = "id_tipo_modelo_documento") }, inverseJoinColumns = { @JoinColumn(name = "id_papel") })
	public List<Papel> getPapeis() {
		return papeis;
	}

	public void setPapeis(List<Papel> papeis) {
		this.papeis = papeis;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Transient
	public TipoModeloDocumento getTipoModeloDocumentoCombo() {
		return tipoModeloDocumentoCombo;
	}

	public void setTipoModeloDocumentoCombo(TipoModeloDocumento tipoModeloDocumentoCombo) {
		this.tipoModeloDocumentoCombo = tipoModeloDocumentoCombo;
	}

	@Override
	public String toString() {
		return tipoModeloDocumento;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoModeloDocumento)) {
			return false;
		}
		TipoModeloDocumento other = (TipoModeloDocumento) obj;
		if (getIdTipoModeloDocumento() != other.getIdTipoModeloDocumento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoModeloDocumento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoModeloDocumento> getEntityClass() {
		return TipoModeloDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoModeloDocumento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
