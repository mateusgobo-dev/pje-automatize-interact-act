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
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = EstatisticaProcessoJusticaFederal.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_est_proc_jus_fed", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_est_proc_jus_fed"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class EstatisticaProcessoJusticaFederal implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<EstatisticaProcessoJusticaFederal,Integer> {

	public static final String TABLE_NAME = "tb_est_proc_jus_fed";
	private static final long serialVersionUID = 1L;

	private int idEstatisticaProcessoJF;
	private ProcessoTrf processoTrf;
	private Date dataInclusao;
	private String secaoJudiciaria;
	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private Competencia competencia;
	private Cargo cargo;
	private PessoaMagistrado pessoaMagistrado;
	private String codEvento;
	private Date dtEvento;

	@Id
	@GeneratedValue(generator = "gen_est_proc_jus_fed")
	@Column(name = "id_est_proc_jus_fed", unique = true, nullable = false)
	public int getIdEstatisticaProcessoJF() {
		return idEstatisticaProcessoJF;
	}

	public void setIdEstatisticaProcessoJF(int idEstatisticaProcessoJF) {
		this.idEstatisticaProcessoJF = idEstatisticaProcessoJF;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao", nullable = false)
	@NotNull
	public Date getDataInclusao() {
		return dataInclusao;
	}

	public void setDataInclusao(Date dataInclusao) {
		this.dataInclusao = dataInclusao;
	}

	@Column(name = "cd_secao_judiciaria", nullable = false, length = 2)
	@Length(max = 2)
	@NotNull
	public String getSecaoJudiciaria() {
		return secaoJudiciaria;
	}

	public void setSecaoJudiciaria(String secaoJudiciaria) {
		this.secaoJudiciaria = secaoJudiciaria;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador", nullable = false)
	@NotNull
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_classe_judicial", nullable = false)
	@NotNull
	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_competencia")
	public Competencia getCompetencia() {
		return competencia;
	}

	public void setCompetencia(Competencia competencia) {
		this.competencia = competencia;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cargo", nullable = false)
	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_magistrado")
	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}

	@Column(name = "cd_evento", length = 30)
	@NotNull
	public String getCodEvento() {
		return codEvento;
	}

	public void setCodEvento(String codEvento) {
		this.codEvento = codEvento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_evento", nullable = false)
	@NotNull
	public Date getDtEvento() {
		return dtEvento;
	}

	public void setDtEvento(Date dtEvento) {
		this.dtEvento = dtEvento;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends EstatisticaProcessoJusticaFederal> getEntityClass() {
		return EstatisticaProcessoJusticaFederal.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdEstatisticaProcessoJF());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
