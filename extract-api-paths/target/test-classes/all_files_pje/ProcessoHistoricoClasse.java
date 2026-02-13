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

@Entity
@Table(name = ProcessoHistoricoClasse.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_historico_classe", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_historico_classe"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoHistoricoClasse implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoHistoricoClasse,Integer> {

	public static final String TABLE_NAME = "tb_proc_historico_classe";
	private static final long serialVersionUID = 1L;

	private int idProcessoHistoricoClasse;
	private ClasseJudicial classeJudicialAtual;
	private ClasseJudicial classeJudicialAnterior;
	private ProcessoTrf processoTrf;
	private Boolean inversaoPolos;

	private Date dataInicio;
	private Date dataFim;

	public ProcessoHistoricoClasse() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_historico_classe")
	@Column(name = "id_processo_historico_classe", unique = true, nullable = false)
	public int getIdProcessoHistoricoClasse() {
		return this.idProcessoHistoricoClasse;
	}

	public void setIdProcessoHistoricoClasse(int idProcessoHistoricoClasse) {
		this.idProcessoHistoricoClasse = idProcessoHistoricoClasse;
	}

	@Column(name = "in_inversao_polos")
	public Boolean getInversaoPolos() {
		return this.inversaoPolos;
	}

	public void setInversaoPolos(Boolean inversaoPolos) {
		this.inversaoPolos = inversaoPolos;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_classe_anterior")
	public ClasseJudicial getClasseJudicialAnterior() {
		return classeJudicialAnterior;
	}

	public void setClasseJudicialAnterior(ClasseJudicial classeJudicialAnterior) {
		this.classeJudicialAnterior = classeJudicialAnterior;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_classe_atual")
	public ClasseJudicial getClasseJudicialAtual() {
		return classeJudicialAtual;
	}

	public void setClasseJudicialAtual(ClasseJudicial classeJudicialAtual) {
		this.classeJudicialAtual = classeJudicialAtual;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Column(name = "dt_inicio", nullable = false)
	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	@Column(name = "dt_fim", nullable = false)
	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoHistoricoClasse> getEntityClass() {
		return ProcessoHistoricoClasse.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoHistoricoClasse());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
