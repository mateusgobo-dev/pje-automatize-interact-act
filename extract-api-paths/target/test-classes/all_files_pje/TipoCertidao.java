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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;


@Entity
@Table(name = "tb_tipo_certidao")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_certidao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_certidao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoCertidao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoCertidao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoCertidao;
	private String tipoCertidao;
	private ModeloDocumento modeloDocumento;
	private List<ClasseJudicialTipoCertidao> tipoCertidaoClasseJudicialList = new ArrayList<ClasseJudicialTipoCertidao>(
			0);
	private List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>(0);
	private Boolean ativo;

	public TipoCertidao() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_certidao")
	@Column(name = "id_tipo_certidao", unique = true, nullable = false)
	public int getIdTipoCertidao() {
		return this.idTipoCertidao;
	}

	public void setIdTipoCertidao(int idTipoCertidao) {
		this.idTipoCertidao = idTipoCertidao;
	}

	@Column(name = "ds_tipo_certidao", unique = true, nullable = false, length = 200)
	@Length(max = 200)
	public String getTipoCertidao() {
		return this.tipoCertidao;
	}

	public void setTipoCertidao(String tipoCertidao) {
		this.tipoCertidao = tipoCertidao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_modelo_documento")
	public ModeloDocumento getModeloDocumento() {
		return this.modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "tipoCertidao")
	public List<ClasseJudicialTipoCertidao> getTipoCertidaoClasseJudicialList() {
		return this.tipoCertidaoClasseJudicialList;
	}

	public void setTipoCertidaoClasseJudicialList(List<ClasseJudicialTipoCertidao> tipoCertidaoClasseJudicialList) {
		this.tipoCertidaoClasseJudicialList = tipoCertidaoClasseJudicialList;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_cl_judicial_tp_certidao", joinColumns = { @JoinColumn(name = "id_tipo_certidao", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_classe_judicial", nullable = false, updatable = false) })
	public List<ClasseJudicial> getClasseJudicialList() {
		return this.classeJudicialList;
	}

	public void setClasseJudicialList(List<ClasseJudicial> classeJudicialList) {
		this.classeJudicialList = classeJudicialList;
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
		return tipoCertidao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoCertidao)) {
			return false;
		}
		TipoCertidao other = (TipoCertidao) obj;
		if (getIdTipoCertidao() != other.getIdTipoCertidao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoCertidao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoCertidao> getEntityClass() {
		return TipoCertidao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoCertidao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
