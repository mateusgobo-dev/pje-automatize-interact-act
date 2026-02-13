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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_aplicacao_cmptnca_prrde")
@org.hibernate.annotations.GenericGenerator(name = "gen_aplic_comp_prior", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_aplic_compet_prioridade"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AplicacaoCompetenciaPrioridade implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AplicacaoCompetenciaPrioridade,Integer> {

	private static final long serialVersionUID = 1L;

	private int idAplicacaoCompetenciaPrioridade;
	private AplicacaoClasse aplicacaoClasse;
	private PrioridadeProcesso prioridadeProcesso;
	private Competencia competencia;
	private ClasseJudicial classeJudicial;

	public AplicacaoCompetenciaPrioridade() {
	}

	@Id
	@GeneratedValue(generator = "gen_aplic_comp_prior")
	@Column(name = "id_aplic_competenc_prioridade", unique = true, nullable = false)
	public int getIdAplicacaoCompetenciaPrioridade() {
		return this.idAplicacaoCompetenciaPrioridade;
	}

	public void setIdAplicacaoCompetenciaPrioridade(int idAplicacaoCompetenciaPrioridade) {
		this.idAplicacaoCompetenciaPrioridade = idAplicacaoCompetenciaPrioridade;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_aplicacao_classe", nullable = false)
	@NotNull
	public AplicacaoClasse getAplicacaoClasse() {
		return this.aplicacaoClasse;
	}

	public void setAplicacaoClasse(AplicacaoClasse aplicacaoClasse) {
		this.aplicacaoClasse = aplicacaoClasse;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_prioridade_processo", nullable = false)
	@NotNull
	public PrioridadeProcesso getPrioridadeProcesso() {
		return this.prioridadeProcesso;
	}

	public void setPrioridadeProcesso(PrioridadeProcesso prioridadeProcesso) {
		this.prioridadeProcesso = prioridadeProcesso;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_competencia", nullable = false)
	@NotNull
	public Competencia getCompetencia() {
		return competencia;
	}

	public void setCompetencia(Competencia competencia) {
		this.competencia = competencia;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_classe_judicial", nullable = false)
	@NotNull
	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AplicacaoCompetenciaPrioridade)) {
			return false;
		}
		AplicacaoCompetenciaPrioridade other = (AplicacaoCompetenciaPrioridade) obj;
		if (getIdAplicacaoCompetenciaPrioridade() != other.getIdAplicacaoCompetenciaPrioridade()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdAplicacaoCompetenciaPrioridade();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AplicacaoCompetenciaPrioridade> getEntityClass() {
		return AplicacaoCompetenciaPrioridade.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAplicacaoCompetenciaPrioridade());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
