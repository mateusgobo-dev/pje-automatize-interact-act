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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_variavel")
@org.hibernate.annotations.GenericGenerator(name = "gen_variavel", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_variavel"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Variavel implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Variavel,Integer> {

	private static final long serialVersionUID = 1L;

	private int idVariavel;
	private String variavel;
	private String valorVariavel;
	private String descricao;
	private Boolean ativo = Boolean.TRUE;

	private List<VariavelTipoModelo> variavelTipoModeloList = new ArrayList<VariavelTipoModelo>(0);

	public Variavel() {
	}

	@Id
	@GeneratedValue(generator = "gen_variavel")
	@Column(name = "id_variavel", unique = true, nullable = false)
	public int getIdVariavel() {
		return this.idVariavel;
	}

	public void setIdVariavel(int idVariavel) {
		this.idVariavel = idVariavel;
	}

	@Column(name = "ds_variavel", nullable = false, length = 100, unique = true)
	@NotNull
	@Length(max = 100)
	public String getVariavel() {
		return this.variavel;
	}

	public void setVariavel(String variavel) {
		String var = "";
		if (variavel != null && variavel.trim().length() > 0) {
			var = variavel.replace(" ", "_");
		}
		this.variavel = var;
	}

	@Column(name = "vl_variavel", nullable = false, length = 4000)
	@NotNull
	@Length(max = 4000)
	public String getValorVariavel() {
		return this.valorVariavel;
	}

	public void setValorVariavel(String valorVariavel) {
		this.valorVariavel = valorVariavel;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Column(name = "ds_descricao", nullable = true, length = 250)
	@Length(max = 250)
	public String getDescricao() {
		return this.descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}


	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "variavel")
	public List<VariavelTipoModelo> getVariavelTipoModeloList() {
		return variavelTipoModeloList;
	}

	public void setVariavelTipoModeloList(List<VariavelTipoModelo> variavelTipoModeloList) {
		this.variavelTipoModeloList = variavelTipoModeloList;
	}

	@Override
	public String toString() {
		return variavel;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Variavel)) {
			return false;
		}
		Variavel other = (Variavel) obj;
		if (getIdVariavel() != other.getIdVariavel()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdVariavel();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Variavel> getEntityClass() {
		return Variavel.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdVariavel());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
