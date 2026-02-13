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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = AplicacaoClasse.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_aplicacao_classe", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_aplicacao_classe"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AplicacaoClasse implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AplicacaoClasse,Integer> {

	public static final String TABLE_NAME = "tb_aplicacao_classe";
	private static final long serialVersionUID = 1L;

	private int idAplicacaoClasse;
	private String aplicacaoClasse;
	private String codigoAplicacaoClasse;
	private Boolean ativo;
	private List<AplicacaoClasseTipoProcessoDocumento> aplicacaoClasseTipoProcessoDocumentoList = new ArrayList<AplicacaoClasseTipoProcessoDocumento>(
			0);

	private List<AplicacaoClasseEvento> aplicacaoClasseEventoList = new ArrayList<AplicacaoClasseEvento>(0);

	private List<Peticao> peticaoList = new ArrayList<Peticao>(0);
	private List<ClasseAplicacao> classeAplicacaoList = new ArrayList<ClasseAplicacao>(0);

	public AplicacaoClasse() {
	}

	@Id
	@GeneratedValue(generator = "gen_aplicacao_classe")
	@Column(name = "id_aplicacao_classe", unique = true, nullable = false)
	public int getIdAplicacaoClasse() {
		return this.idAplicacaoClasse;
	}

	public void setIdAplicacaoClasse(int idAplicacaoClasse) {
		this.idAplicacaoClasse = idAplicacaoClasse;
	}

	@Column(name = "ds_aplicacao_classe", nullable = false, length = 50)
	@NotNull
	@Length(max = 50)
	public String getAplicacaoClasse() {
		return this.aplicacaoClasse;
	}

	public void setAplicacaoClasse(String aplicacaoClasse) {
		this.aplicacaoClasse = aplicacaoClasse;
	}
	
	@Column(name = "cd_aplicacao_classe", nullable = false, length = 2)
	@NotNull
	@Length(max = 2)
	public String getCodigoAplicacaoClasse() {
		return codigoAplicacaoClasse;
	}

	public void setCodigoAplicacaoClasse(String codigoAplicacaoClasse) {
		this.codigoAplicacaoClasse = codigoAplicacaoClasse;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "aplicacaoClasse")
	public List<AplicacaoClasseTipoProcessoDocumento> getAplicacaoClasseTipoProcessoDocumentoList() {
		return this.aplicacaoClasseTipoProcessoDocumentoList;
	}

	public void setAplicacaoClasseTipoProcessoDocumentoList(
			List<AplicacaoClasseTipoProcessoDocumento> aplicacaoClasseTipoProcessoDocumentoList) {
		this.aplicacaoClasseTipoProcessoDocumentoList = aplicacaoClasseTipoProcessoDocumentoList;
	}

	@Override
	public String toString() {
		return aplicacaoClasse;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "aplicacaoClasse")
	public List<AplicacaoClasseEvento> getAplicacaoClasseEventoList() {
		return aplicacaoClasseEventoList;
	}

	public void setAplicacaoClasseEventoList(List<AplicacaoClasseEvento> aplicacaoClasseEventoList) {
		this.aplicacaoClasseEventoList = aplicacaoClasseEventoList;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_aplicacao_classe_pticao", joinColumns = { @JoinColumn(name = "id_aplicacao_classe", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_peticao", nullable = false, updatable = false) })
	public List<Peticao> getPeticaoList() {
		return this.peticaoList;
	}

	public void setPeticaoList(List<Peticao> peticaoList) {
		this.peticaoList = peticaoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "aplicacaoClasse")
	public List<ClasseAplicacao> getClasseAplicacaoList() {
		return this.classeAplicacaoList;
	}

	public void setClasseAplicacaoList(List<ClasseAplicacao> classeAplicacaoList) {
		this.classeAplicacaoList = classeAplicacaoList;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AplicacaoClasse)) {
			return false;
		}
		AplicacaoClasse other = (AplicacaoClasse) obj;
		if (getIdAplicacaoClasse() != other.getIdAplicacaoClasse()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdAplicacaoClasse();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AplicacaoClasse> getEntityClass() {
		return AplicacaoClasse.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAplicacaoClasse());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
