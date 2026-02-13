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

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Classe de representa um resultado de sentença de uma parte do processo
 */
@Entity
@Table(name = ResultadoSentencaParte.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_result_sentenca_parte", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_result_sentenca_parte"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ResultadoSentencaParte implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ResultadoSentencaParte,Integer> {

	private static final long serialVersionUID = 8762154720443065521L;

	public static final String TABLE_NAME = "tb_result_sentenca_parte";

	private int idResultadoSentencaParte;
	private Integer beneficioOrdem;
	private ProcessoParte processoParte;
	private ResultadoSentenca resultadoSentenca;
	private BigDecimal valorCondenacao;
	private BigDecimal valorCustasArrecadar;
	private BigDecimal valorCustasDispensadas;
	private Boolean assistenciaJudicialGratuita;
	private SolucaoSentenca solucaoSentenca;

	@Column(name = "in_asstnca_judicial_gratuita")
	public Boolean getAssistenciaJudicialGratuita() {
		return assistenciaJudicialGratuita;
	}

	public void setAssistenciaJudicialGratuita(Boolean assistenciaJudicialGratuita) {
		this.assistenciaJudicialGratuita = assistenciaJudicialGratuita;
	}

	@Id
	@GeneratedValue(generator = "gen_result_sentenca_parte")
	@Column(name = "id_resultado_sentenca_parte", unique = true, nullable = false)
	public int getIdResultadoSentencaParte() {
		return idResultadoSentencaParte;
	}

	public void setIdResultadoSentencaParte(int idResultadoSentencaParte) {
		this.idResultadoSentencaParte = idResultadoSentencaParte;
	}

	@Column(name = "vl_beneficio_ordem")
	public Integer getBeneficioOrdem() {
		return beneficioOrdem;
	}

	public void setBeneficioOrdem(Integer beneficioOrdem) {
		this.beneficioOrdem = beneficioOrdem;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte", nullable = false)
	@NotNull
	public ProcessoParte getProcessoParte() {
		return processoParte;
	}

	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_resultado_sentenca", nullable = false)
	@NotNull
	public ResultadoSentenca getResultadoSentenca() {
		return resultadoSentenca;
	}

	public void setResultadoSentenca(ResultadoSentenca resultadoSentenca) {
		this.resultadoSentenca = resultadoSentenca;
	}

	@Column(name = "vl_condenacao")
	public BigDecimal getValorCondenacao() {
		return valorCondenacao;
	}

	public void setValorCondenacao(BigDecimal valorCondenacao) {
		this.valorCondenacao = valorCondenacao;
	}

	@Column(name = "vl_custas_arrecadar")
	public BigDecimal getValorCustasArrecadar() {
		return valorCustasArrecadar;
	}

	public void setValorCustasArrecadar(BigDecimal valorCustasArrecadar) {
		this.valorCustasArrecadar = valorCustasArrecadar;
	}

	@Column(name = "vl_custas_dispensadas")
	public BigDecimal getValorCustasDispensadas() {
		return valorCustasDispensadas;
	}

	public void setValorCustasDispensadas(BigDecimal valorCustasDispensadas) {
		this.valorCustasDispensadas = valorCustasDispensadas;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_solucao_sentenca", nullable = false)
	@NotNull
	public SolucaoSentenca getSolucaoSentenca() {
		return solucaoSentenca;
	}

	public void setSolucaoSentenca(SolucaoSentenca solucaoSentenca) {
		this.solucaoSentenca = solucaoSentenca;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ResultadoSentencaParte> getEntityClass() {
		return ResultadoSentencaParte.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdResultadoSentencaParte());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
