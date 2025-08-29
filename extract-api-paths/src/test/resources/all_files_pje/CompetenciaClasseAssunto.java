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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = CompetenciaClasseAssunto.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_competencia_cl_assunto", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_competencia_cl_assunto"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CompetenciaClasseAssunto implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<CompetenciaClasseAssunto,Integer> {
	public static final String TABLE_NAME = "tb_competencia_cl_assunto";
	private static final long serialVersionUID = 1L;

	private int idCompClassAssu;
	private Competencia competencia;
	private ClasseAplicacao classeAplicacao;
	private AssuntoTrf assuntoTrf;
	private Date dataInicio;
	private Date dataFim;
	private Boolean check;
	private int nivelAcesso;
	private boolean segredoSigilo = false;

	public CompetenciaClasseAssunto() {
	}

	@Id
	@GeneratedValue(generator = "gen_competencia_cl_assunto")
	@Column(name = "id_comp_class_assu", unique = true, nullable = false)
	public int getIdCompClassAssu() {
		return this.idCompClassAssu;
	}

	public void setIdCompClassAssu(int idCompClassAssu) {
		this.idCompClassAssu = idCompClassAssu;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_competencia", nullable = false)
	@NotNull
	public Competencia getCompetencia() {
		return this.competencia;
	}

	public void setCompetencia(Competencia competencia) {
		this.competencia = competencia;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_assunto", nullable = false)
	@NotNull
	public AssuntoTrf getAssuntoTrf() {
		return this.assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assunto) {
		this.assuntoTrf = assunto;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio", nullable = false)
	@NotNull
	public Date getDataInicio() {
		return this.dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim")
	public Date getDataFim() {
		return this.dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	@Override
	public String toString() {
		return classeAplicacao.getClasseJudicial().getClasseJudicial();
	}

	@Transient
	public Boolean getCheck() {
		return check;
	}

	public void setCheck(Boolean check) {
		this.check = check;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CompetenciaClasseAssunto)) {
			return false;
		}
		CompetenciaClasseAssunto other = (CompetenciaClasseAssunto) obj;
		if (getIdCompClassAssu() != other.getIdCompClassAssu()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdCompClassAssu();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CompetenciaClasseAssunto> getEntityClass() {
		return CompetenciaClasseAssunto.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdCompClassAssu());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@Column(name="cd_nivel_acesso")
	@NotNull
	public int getNivelAcesso() {
		return nivelAcesso;
	}

	public void setNivelAcesso(int nivelAcesso) {
		this.nivelAcesso = nivelAcesso;
	}

	@Column(name="in_segredo_sigilo")
	public boolean isSegredoSigilo() {
		return segredoSigilo;
	}

	public void setSegredoSigilo(boolean segredoSigilo) {
		this.segredoSigilo = segredoSigilo;
	}

}
