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
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;

/**
 * Classe que representa um resultado de sentenca de um processo
 */
@Entity
@Table(name = ResultadoSentenca.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_resultado_sentenca", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_resultado_sentenca"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ResultadoSentenca implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ResultadoSentenca,Integer> {

	public static final String TABLE_NAME = "tb_resultado_sentenca";
	private static final long serialVersionUID = 1L;

	private Boolean solucaoUnica;
	private Date dataSentenca;
	private Boolean homologado = Boolean.FALSE;
	private int idResultadoSentenca;
	private ResultadoSentenca resultadoSentencaExcludente;
	private Boolean sentencaLiquida;
	private ProcessoTrf processoTrf;

	private List<ResultadoSentencaParte> resultados = new ArrayList<ResultadoSentencaParte>(0);

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "resultadoSentenca")
	@Cascade(value = { org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	public List<ResultadoSentencaParte> getResultados() {
		return resultados;
	}

	public void setResultados(List<ResultadoSentencaParte> resultados) {
		this.resultados = resultados;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false, updatable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Column(name = "in_solucao_unica")
	public Boolean getSolucaoUnica() {
		return solucaoUnica;
	}

	public void setSolucaoUnica(Boolean solucaoUnica) {
		this.solucaoUnica = solucaoUnica;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_sentenca")
	public Date getDataSentenca() {
		return dataSentenca;
	}

	public void setDataSentenca(Date dataSentenca) {
		this.dataSentenca = dataSentenca;
	}

	@Column(name = "in_homologado")
	public Boolean getHomologado() {
		return homologado;
	}

	public void setHomologado(Boolean homologado) {
		this.homologado = homologado;
	}

	@Id
	@GeneratedValue(generator = "gen_resultado_sentenca")
	@Column(name = "id_resultado_sentenca", unique = true, nullable = false)
	public int getIdResultadoSentenca() {
		return idResultadoSentenca;
	}

	public void setIdResultadoSentenca(int idResultadoSentenca) {
		this.idResultadoSentenca = idResultadoSentenca;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_rsltdo_sentenca_excludente", nullable = true)
	public ResultadoSentenca getResultadoSentencaExcludente() {
		return resultadoSentencaExcludente;
	}

	public void setResultadoSentencaExcludente(ResultadoSentenca resultadoSentencaExcludente) {
		this.resultadoSentencaExcludente = resultadoSentencaExcludente;
	}

	@Column(name = "in_sentenca_liquida")
	public Boolean getSentencaLiquida() {
		return sentencaLiquida;
	}

	public void setSentencaLiquida(Boolean sentencaLiquida) {
		this.sentencaLiquida = sentencaLiquida;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ResultadoSentenca> getEntityClass() {
		return ResultadoSentenca.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdResultadoSentenca());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
