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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_ramo_atividade")
@org.hibernate.annotations.GenericGenerator(name = "gen_ramo_atividade", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_ramo_atividade"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@Cacheable
public class RamoAtividade implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RamoAtividade,Integer> {

	private static final long serialVersionUID = 1L;

	private int idRamoAtividade;
	private String codRamoAtividade;
	private String ramoAtividade;
	private RamoAtividade ramoAtividadePai;
	private Boolean ativo;
	private List<RamoAtividade> ramoAtividadeList = new ArrayList<RamoAtividade>(0);
	private List<PessoaJuridica> pessoaJuridicaList = new ArrayList<PessoaJuridica>(0);

	public RamoAtividade() {
	}

	@Id
	@GeneratedValue(generator = "gen_ramo_atividade")
	@Column(name = "id_ramo_atividade", unique = true, nullable = false)
	public int getIdRamoAtividade() {
		return this.idRamoAtividade;
	}

	public void setIdRamoAtividade(int idRamoAtividade) {
		this.idRamoAtividade = idRamoAtividade;
	}

	@Column(name = "cd_ramo_atividade", length = 15, unique = true)
	@Length(max = 15)
	public String getCodRamoAtividade() {
		return this.codRamoAtividade;
	}

	public void setCodRamoAtividade(String codRamoAtividade) {
		this.codRamoAtividade = codRamoAtividade;
	}

	@Column(name = "ds_ramo_atividade", nullable = false, length = 200, unique = true)
	@NotNull
	@Length(max = 200)
	public String getRamoAtividade() {
		return this.ramoAtividade;
	}

	public void setRamoAtividade(String ramoAtividade) {
		this.ramoAtividade = ramoAtividade;
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
	@JoinColumn(name = "id_ramo_atividade_pai")
	public RamoAtividade getRamoAtividadePai() {
		return this.ramoAtividadePai;
	}

	public void setRamoAtividadePai(RamoAtividade ramoAtividadePai) {
		this.ramoAtividadePai = ramoAtividadePai;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "ramoAtividadePai")
	@OrderBy("ramoAtividade")
	public List<RamoAtividade> getRamoAtividadeList() {
		return this.ramoAtividadeList;
	}

	public void setRamoAtividadeList(List<RamoAtividade> ramoAtividadeList) {
		this.ramoAtividadeList = ramoAtividadeList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "ramoAtividade")
	public List<PessoaJuridica> getPessoaJuridicaList() {
		return this.pessoaJuridicaList;
	}

	public void setPessoaJuridicaList(List<PessoaJuridica> pessoaJuridicaList) {
		this.pessoaJuridicaList = pessoaJuridicaList;
	}

	@Override
	public String toString() {
		return ramoAtividade;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RamoAtividade)) {
			return false;
		}
		RamoAtividade other = (RamoAtividade) obj;
		if (getIdRamoAtividade() != other.getIdRamoAtividade()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdRamoAtividade();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RamoAtividade> getEntityClass() {
		return RamoAtividade.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRamoAtividade());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
