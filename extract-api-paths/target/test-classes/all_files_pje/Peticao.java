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
@Table(name = "tb_peticao")
@org.hibernate.annotations.GenericGenerator(name = "gen_peticao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_peticao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Peticao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Peticao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idPeticao;
	private String peticao;
	private String codPeticao;
	private String codPeticaoCorrelacionado;
	private String norma;
	private String leiArtigo;
	private String lei;
	private Boolean ativo;

	private String peticaoGlossario;

	private List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>(0);
	private List<AplicacaoClasse> aplicacaoClasseList = new ArrayList<AplicacaoClasse>(0);
	private List<PeticaoTipoModeloDocumento> peticaoTipoModeloDocumentoList = new ArrayList<PeticaoTipoModeloDocumento>(
			0);
	private List<TipoModeloDocumento> tipoModeloDocumentoList = new ArrayList<TipoModeloDocumento>(0);
	private List<PeticaoClasseAplicacao> peticaoClasseAplicacaoList = new ArrayList<PeticaoClasseAplicacao>(0);

	public Peticao() {
	}

	@Id
	@GeneratedValue(generator = "gen_peticao")
	@Column(name = "id_peticao", unique = true, nullable = false)
	public int getIdPeticao() {
		return this.idPeticao;
	}

	public void setIdPeticao(int idPeticao) {
		this.idPeticao = idPeticao;
	}

	@Column(name = "ds_peticao", unique = true, nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getPeticao() {
		return this.peticao;
	}

	public void setPeticao(String peticao) {
		this.peticao = peticao;
	}

	@Column(name = "cd_peticao", length = 30, unique = true, nullable = false)
	@NotNull
	@Length(max = 30)
	public String getCodPeticao() {
		return this.codPeticao;
	}

	public void setCodPeticao(String codPeticao) {
		this.codPeticao = codPeticao;
	}

	@Column(name = "cd_peticao_correlacionado", length = 30)
	@Length(max = 30)
	public String getCodPeticaoCorrelacionado() {
		return this.codPeticaoCorrelacionado;
	}

	public void setCodPeticaoCorrelacionado(String codPeticaoCorrelacionado) {
		this.codPeticaoCorrelacionado = codPeticaoCorrelacionado;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_peticao_classe_judicial", joinColumns = { @JoinColumn(name = "id_peticao", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_classe_judicial", nullable = false, updatable = false) })
	public List<ClasseJudicial> getClasseJudicialList() {
		return this.classeJudicialList;
	}

	public void setClasseJudicialList(List<ClasseJudicial> classeJudicialList) {
		this.classeJudicialList = classeJudicialList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "peticao")
	public List<PeticaoClasseAplicacao> getPeticaoClasseAplicacaoList() {
		return this.peticaoClasseAplicacaoList;
	}

	public void setPeticaoClasseAplicacaoList(List<PeticaoClasseAplicacao> peticaoClasseAplicacaoList) {
		this.peticaoClasseAplicacaoList = peticaoClasseAplicacaoList;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "ds_norma", length = 200)
	@Length(max = 200)
	public String getNorma() {
		return this.norma;
	}

	public void setNorma(String norma) {
		this.norma = norma;
	}

	@Column(name = "ds_lei_artigo")
	public String getLeiArtigo() {
		return this.leiArtigo;
	}

	public void setLeiArtigo(String leiArtigo) {
		this.leiArtigo = leiArtigo;
	}

	@Column(name = "ds_lei")
	public String getLei() {
		return this.lei;
	}

	public void setLei(String lei) {
		this.lei = lei;
	}

	@Column(name = "ds_glossario")
	public String getPeticaoGlossario() {
		return this.peticaoGlossario;
	}

	public void setPeticaoGlossario(String peticaoGlossario) {
		this.peticaoGlossario = peticaoGlossario;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_aplicacao_classe_pticao", joinColumns = { @JoinColumn(name = "id_peticao", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_aplicacao_classe", nullable = false, updatable = false) })
	public List<AplicacaoClasse> getAplicacaoClasseList() {
		return this.aplicacaoClasseList;
	}

	public void setAplicacaoClasseList(List<AplicacaoClasse> aplicacaoClasseList) {
		this.aplicacaoClasseList = aplicacaoClasseList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "peticao")
	public List<PeticaoTipoModeloDocumento> getPeticaoTipoModeloDocumentoList() {
		return this.peticaoTipoModeloDocumentoList;
	}

	public void setPeticaoTipoModeloDocumentoList(List<PeticaoTipoModeloDocumento> peticaoTipoModeloDocumentoList) {
		this.peticaoTipoModeloDocumentoList = peticaoTipoModeloDocumentoList;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_peticao_tp_modelo_doc", joinColumns = { @JoinColumn(name = "id_peticao", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_tipo_modelo_documento", nullable = false, updatable = false) })
	public List<TipoModeloDocumento> getTipoModeloDocumentoList() {
		return this.tipoModeloDocumentoList;
	}

	public void setTipoModeloDocumentoList(List<TipoModeloDocumento> tipoModeloDocumentoList) {
		this.tipoModeloDocumentoList = tipoModeloDocumentoList;
	}

	@Override
	public String toString() {
		return peticao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Peticao)) {
			return false;
		}
		Peticao other = (Peticao) obj;
		if (getIdPeticao() != other.getIdPeticao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPeticao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Peticao> getEntityClass() {
		return Peticao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPeticao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
