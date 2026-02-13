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
@Table(name = ComplementoClasse.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_complemento_classe", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_complemento_classe"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ComplementoClasse implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ComplementoClasse,Integer> {

	public static final String TABLE_NAME = "tb_complemento_classe";
	private static final long serialVersionUID = 1L;

	private int idComplementoClasse;
	private ClasseAplicacao classeAplicacao;
	private String complementoClasse;
	private Boolean obrigatorio;
	private String componenteValidacao;
	private List<ComplementoClasseProcessoTrf> complementoClasseProcessoTrfList = new ArrayList<ComplementoClasseProcessoTrf>(
			0);

	public ComplementoClasse() {
	}

	@Id
	@GeneratedValue(generator = "gen_complemento_classe")
	@Column(name = "id_complemento_classe", unique = true, nullable = false)
	public int getIdComplementoClasse() {
		return this.idComplementoClasse;
	}

	public void setIdComplementoClasse(int idComplementoClasse) {
		this.idComplementoClasse = idComplementoClasse;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_classe_aplicacao", nullable = false)
	@NotNull
	public ClasseAplicacao getClasseAplicacao() {
		return this.classeAplicacao;
	}

	public void setClasseAplicacao(ClasseAplicacao classeAplicacao) {
		this.classeAplicacao = classeAplicacao;
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

	@Column(name = "ds_complemento_classe", nullable = false, length = 50)
	@NotNull
	@Length(max = 50)
	public String getComplementoClasse() {
		return this.complementoClasse;
	}

	public void setComplementoClasse(String complementoClasse) {
		this.complementoClasse = complementoClasse;
	}

	@Column(name = "in_obrigatorio", nullable = false)
	@NotNull
	public Boolean getObrigatorio() {
		return this.obrigatorio;
	}

	public void setObrigatorio(Boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "complementoClasse")
	public List<ComplementoClasseProcessoTrf> getComplementoClasseProcessoTrfList() {
		return this.complementoClasseProcessoTrfList;
	}

	public void setComplementoClasseProcessoTrfList(List<ComplementoClasseProcessoTrf> complementoClasseProcessoTrfList) {
		this.complementoClasseProcessoTrfList = complementoClasseProcessoTrfList;
	}

	@Override
	public String toString() {
		return complementoClasse;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ComplementoClasse)) {
			return false;
		}
		ComplementoClasse other = (ComplementoClasse) obj;
		if (getIdComplementoClasse() != other.getIdComplementoClasse()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdComplementoClasse();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ComplementoClasse> getEntityClass() {
		return ComplementoClasse.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdComplementoClasse());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
