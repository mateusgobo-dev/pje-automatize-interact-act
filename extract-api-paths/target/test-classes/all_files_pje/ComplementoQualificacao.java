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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = ComplementoQualificacao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_complemento_qualificacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_complemento_qualificacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ComplementoQualificacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ComplementoQualificacao,Integer> {

	public static final String TABLE_NAME = "tb_complem_qualificacao";
	private static final long serialVersionUID = 1L;

	private int idComplementoQualificacao;
	private Qualificacao qualificacao;
	private String complementoQualificacao;
	private Boolean obrigatorio;
	private String componenteValidacao;
	private List<ComplementoPessoaQualificacao> complementoPessoaQualificacaoList = new ArrayList<ComplementoPessoaQualificacao>(
			0);

	public ComplementoQualificacao() {
	}

	@Id
	@GeneratedValue(generator = "gen_complemento_qualificacao")
	@Column(name = "id_complemento_qualificacao", unique = true, nullable = false)
	public int getIdComplementoQualificacao() {
		return this.idComplementoQualificacao;
	}

	public void setIdComplementoQualificacao(int idComplementoQualificacao) {
		this.idComplementoQualificacao = idComplementoQualificacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_qualificacao", nullable = false)
	@NotNull
	public Qualificacao getQualificacao() {
		return this.qualificacao;
	}

	public void setQualificacao(Qualificacao qualificacao) {
		this.qualificacao = qualificacao;
	}

	@Column(name = "ds_componente_validacao", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getComponenteValidacao() {
		return this.componenteValidacao;
	}

	public void setComponenteValidacao(String componenteValidacao) {
		this.componenteValidacao = componenteValidacao;
	}

	@Column(name = "ds_complemento_qualificacao", nullable = false, length = 50)
	@NotNull
	@Length(max = 50)
	public String getComplementoQualificacao() {
		return this.complementoQualificacao;
	}

	public void setComplementoQualificacao(String complementoQualificacao) {
		this.complementoQualificacao = complementoQualificacao;
	}

	@Column(name = "in_obrigatorio", nullable = false)
	@NotNull
	public Boolean getObrigatorio() {
		return this.obrigatorio;
	}

	public void setObrigatorio(Boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "complementoQualificacao")
	public List<ComplementoPessoaQualificacao> getComplementoPessoaQualificacaoList() {
		return this.complementoPessoaQualificacaoList;
	}

	public void setComplementoPessoaQualificacaoList(
			List<ComplementoPessoaQualificacao> complementoPessoaQualificacaoList) {
		this.complementoPessoaQualificacaoList = complementoPessoaQualificacaoList;
	}

	@Override
	public String toString() {
		return complementoQualificacao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ComplementoQualificacao)) {
			return false;
		}
		ComplementoQualificacao other = (ComplementoQualificacao) obj;
		if (getIdComplementoQualificacao() != other.getIdComplementoQualificacao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdComplementoQualificacao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ComplementoQualificacao> getEntityClass() {
		return ComplementoQualificacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdComplementoQualificacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
