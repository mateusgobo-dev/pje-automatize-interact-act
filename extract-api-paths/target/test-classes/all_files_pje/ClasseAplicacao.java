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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import br.jus.pje.nucleo.entidades.lancadormovimento.OrgaoJustica;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = ClasseAplicacao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_classe_aplicacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_classe_aplicacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ClasseAplicacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ClasseAplicacao,Integer> {

	public static final String TABLE_NAME = "tb_classe_aplicacao";
	private static final long serialVersionUID = 1L;

	private int idClasseAplicacao;
	private ClasseJudicial classeJudicial;
	private AplicacaoClasse aplicacaoClasse;
	private OrgaoJustica orgaoJustica;
	private Boolean distribuicaoAutomatica;
	private Boolean ativo;
	private String informativo;

	private List<ComplementoClasse> complementoClasseList = new ArrayList<ComplementoClasse>(0);
	private List<CompetenciaClasseAssunto> competenciaClasseAssuntoList = new ArrayList<CompetenciaClasseAssunto>(0);

	private List<PeticaoClasseAplicacao> peticaoClasseAplicacaoList = new ArrayList<PeticaoClasseAplicacao>(0);

	public ClasseAplicacao() {
	}

	@Id
	@GeneratedValue(generator = "gen_classe_aplicacao")
	@Column(name = "id_classe_aplicacao", unique = true, nullable = false)
	public int getIdClasseAplicacao() {
		return this.idClasseAplicacao;
	}

	public void setIdClasseAplicacao(int idClasseAplicacao) {
		this.idClasseAplicacao = idClasseAplicacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_classe_judicial")
	public ClasseJudicial getClasseJudicial() {
		return this.classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_aplicacao_classe")
	public AplicacaoClasse getAplicacaoClasse() {
		return this.aplicacaoClasse;
	}

	public void setAplicacaoClasse(AplicacaoClasse aplicacaoClasse) {
		this.aplicacaoClasse = aplicacaoClasse;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_justica")
	public OrgaoJustica getOrgaoJustica() {
		return orgaoJustica;
	}

	public void setOrgaoJustica(OrgaoJustica orgaoJustica) {
		this.orgaoJustica = orgaoJustica;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "in_distribuicao_automatica", nullable = false)
	@NotNull
	public Boolean getDistribuicaoAutomatica() {
		return this.distribuicaoAutomatica;
	}

	public void setDistribuicaoAutomatica(Boolean distribuicaoAutomatica) {
		this.distribuicaoAutomatica = distribuicaoAutomatica;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "classeAplicacao")
	public List<ComplementoClasse> getComplementoClasseList() {
		return this.complementoClasseList;
	}

	public void setComplementoClasseList(List<ComplementoClasse> complementoClasseList) {
		this.complementoClasseList = complementoClasseList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "classeAplicacao")
	public List<CompetenciaClasseAssunto> getCompetenciaClasseAssuntoList() {
		return this.competenciaClasseAssuntoList;
	}

	public void setCompetenciaClasseAssuntoList(List<CompetenciaClasseAssunto> competenciaClasseAssuntoList) {
		this.competenciaClasseAssuntoList = competenciaClasseAssuntoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "classeAplicacao")
	public List<PeticaoClasseAplicacao> getPeticaoClasseAplicacaoList() {
		return this.peticaoClasseAplicacaoList;
	}

	public void setPeticaoClasseAplicacaoList(List<PeticaoClasseAplicacao> peticaoClasseAplicacaoList) {
		this.peticaoClasseAplicacaoList = peticaoClasseAplicacaoList;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_informativo_classe")
	public String getInformativo() {
		return this.informativo;
	}

	public void setInformativo(String informativo) {
		this.informativo = informativo;
	}

	@Override
	public String toString() {
		return classeJudicial.getClasseJudicial();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ClasseAplicacao)) {
			return false;
		}
		ClasseAplicacao other = (ClasseAplicacao) obj;
		if (getIdClasseAplicacao() != other.getIdClasseAplicacao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdClasseAplicacao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ClasseAplicacao> getEntityClass() {
		return ClasseAplicacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdClasseAplicacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
