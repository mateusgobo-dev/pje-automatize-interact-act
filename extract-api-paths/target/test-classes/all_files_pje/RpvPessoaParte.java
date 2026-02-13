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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import br.jus.pje.nucleo.enums.CondicaoPssEnum;
import br.jus.pje.nucleo.enums.RpvPessoaParteParticipacaoEnum;

@Entity
@Table(name = "tb_rpv_pessoa_parte")
@org.hibernate.annotations.GenericGenerator(name = "gen_rpv_pessoa_parte", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_rpv_pessoa_parte"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RpvPessoaParte implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RpvPessoaParte,Integer> {

	private static final long serialVersionUID = 4197419781029619553L;
	private int idRpvPessoaParte;
	private Rpv rpv;
	private Pessoa pessoa;
	private double valorPagoSucumbencia = 0;
	private double valorExecucao = 0;
	private double valorPagoPessoa = 0;
	private double valorHonorario = 0;
	private boolean inIsentoPss = false;
	private double valorPss = 0;
	private CondicaoPssEnum condicaoPss = CondicaoPssEnum.A;
	private RpvUnidadeGestora unidadeExecutadaPss;
	private double valorPagoContratual = 0;
	private TipoParte tipoParte;
	private boolean inInsentoIr = false;
	private Integer numeroMesExercicioAnterior;
	private double valorExercicioAnterior = 0;
	private String motivoNaoPreenchimentoNumMes;
	private Integer numeroMesExercicioCorrente;
	private double valorExercicioCorrente = 0;
	private Date dataBaseValorCompensar;
	private Date dataTransitoJulgadoCompensa;
	private boolean inDoencaGrave = false;
	private boolean incapaz = false;
	private double valorPercentualHonorario = 0;
	private List<RpvParteRepresentante> rpvRepresentanteList = new ArrayList<RpvParteRepresentante>(0);
	private RpvParteDeducao rpvParteDeducao;
	private List<RpvParteValorCompensar> rpvParteValorCompensarList = new ArrayList<RpvParteValorCompensar>(0);
	private RpvPessoaParteParticipacaoEnum inParticipacao;
	private double valorIntegralDebito = 0;
	private double valorIR;
	
	public RpvPessoaParte() {
	}

	@Id
	@GeneratedValue(generator = "gen_rpv_pessoa_parte")
	@Column(name = "id_rpv_pessoa_parte", unique = true, nullable = false)
	public int getIdRpvPessoaParte() {
		return idRpvPessoaParte;
	}

	public void setIdRpvPessoaParte(int idRpvPessoaParte) {
		this.idRpvPessoaParte = idRpvPessoaParte;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_rpv", nullable = false)
	@NotNull
	public Rpv getRpv() {
		return rpv;
	}

	public void setRpv(Rpv rpv) {
		this.rpv = rpv;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa", nullable = false, updatable = false)
	@NotNull
	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	@Column(name = "vl_valor_pago_sucumbencia")
	public double getValorPagoSucumbencia() {
		return valorPagoSucumbencia;
	}

	public void setValorPagoSucumbencia(double valorPagoSucumbencia) {
		this.valorPagoSucumbencia = valorPagoSucumbencia;
	}

	@Column(name = "vl_valor_execucao")
	public double getValorExecucao() {
		return valorExecucao;
	}

	public void setValorExecucao(double valorExecucao) {
		this.valorExecucao = valorExecucao;
	}

	@Column(name = "vl_valor_pago_pessoa")
	public double getValorPagoPessoa() {
		return valorPagoPessoa;
	}

	public void setValorPagoPessoa(double valorPagoPessoa) {
		this.valorPagoPessoa = valorPagoPessoa;
	}

	@Column(name = "vl_valor_honorario")
	public double getValorHonorario() {
		return valorHonorario;
	}

	public void setValorHonorario(double valorHonorario) {
		this.valorHonorario = valorHonorario;
	}

	@Column(name = "in_isento_pss", length = 1)
	public boolean getInIsentoPss() {
		return inIsentoPss;
	}

	public void setInIsentoPss(boolean inIsentoPss) {
		this.inIsentoPss = inIsentoPss;
	}

	@Column(name = "vl_valor_pss")
	public double getValorPss() {
		return valorPss;
	}

	public void setValorPss(double valorPss) {
		this.valorPss = valorPss;
	}

	@Column(name = "in_condicao_pss", length = 1)
	@Enumerated(EnumType.STRING)
	public CondicaoPssEnum getCondicaoPss() {
		return condicaoPss;
	}

	public void setCondicaoPss(CondicaoPssEnum condicaoPss) {
		this.condicaoPss = condicaoPss;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cd_unidade_executada_pss")
	public RpvUnidadeGestora getUnidadeExecutadaPss() {
		return unidadeExecutadaPss;
	}

	public void setUnidadeExecutadaPss(RpvUnidadeGestora unidadeExecutadaPss) {
		this.unidadeExecutadaPss = unidadeExecutadaPss;
	}

	@Column(name = "vl_valor_pago_contratual")
	public double getValorPagoContratual() {
		return valorPagoContratual;
	}

	public void setValorPagoContratual(double valorPagoContratual) {
		this.valorPagoContratual = valorPagoContratual;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_parte")
	public TipoParte getTipoParte() {
		return tipoParte;
	}

	public void setTipoParte(TipoParte tipoParte) {
		this.tipoParte = tipoParte;
	}

	@Column(name = "in_insento_ir", length = 1)
	public boolean getInInsentoIr() {
		return inInsentoIr;
	}

	public void setInInsentoIr(boolean inInsentoIr) {
		this.inInsentoIr = inInsentoIr;
	}

	@Column(name = "nr_mes_exerc_anterior")
	public Integer getNumeroMesExercicioAnterior() {
		return numeroMesExercicioAnterior;
	}

	public void setNumeroMesExercicioAnterior(Integer numeroMesExercicioAnterior) {
		this.numeroMesExercicioAnterior = numeroMesExercicioAnterior;
	}

	@Column(name = "vl_exerc_anterior")
	public double getValorExercicioAnterior() {
		return valorExercicioAnterior;
	}

	public void setValorExercicioAnterior(double valorExercicioAnterior) {
		this.valorExercicioAnterior = valorExercicioAnterior;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_motivo_nao_preench_nr_mes")
	public String getMotivoNaoPreenchimentoNumMes() {
		return motivoNaoPreenchimentoNumMes;
	}

	public void setMotivoNaoPreenchimentoNumMes(String motivoNaoPreenchimentoNumMes) {
		this.motivoNaoPreenchimentoNumMes = motivoNaoPreenchimentoNumMes;
	}

	@Column(name = "nr_mes_exerc_corrente")
	public Integer getNumeroMesExercicioCorrente() {
		return numeroMesExercicioCorrente;
	}

	public void setNumeroMesExercicioCorrente(Integer numeroMesExercicioCorrente) {
		this.numeroMesExercicioCorrente = numeroMesExercicioCorrente;
	}

	@Column(name = "vl_exerc_corrente")
	public double getValorExercicioCorrente() {
		return valorExercicioCorrente;
	}

	public void setValorExercicioCorrente(double valorExercicioCorrente) {
		this.valorExercicioCorrente = valorExercicioCorrente;
	}

	@Column(name = "dt_base_valor_compensar")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDataBaseValorCompensar() {
		return dataBaseValorCompensar;
	}

	public void setDataBaseValorCompensar(Date dataBaseValorCompensar) {
		this.dataBaseValorCompensar = dataBaseValorCompensar;
	}

	@Column(name = "dt_transito_julgado_compensa")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDataTransitoJulgadoCompensa() {
		return dataTransitoJulgadoCompensa;
	}

	public void setDataTransitoJulgadoCompensa(Date dataTransitoJulgadoCompensa) {
		this.dataTransitoJulgadoCompensa = dataTransitoJulgadoCompensa;
	}

	@Column(name = "in_doenca_grave")
	public boolean getInDoencaGrave() {
		return inDoencaGrave;
	}

	public void setInDoencaGrave(boolean inDoencaGrave) {
		this.inDoencaGrave = inDoencaGrave;
	}

	@Column(name = "in_incapaz")
	public boolean getIncapaz() {
		return incapaz;
	}

	public void setIncapaz(boolean incapaz) {
		this.incapaz = incapaz;
	}

	@Column(name = "vl_percentual_honorario")
	public double getValorPercentualHonorario() {
		return valorPercentualHonorario;
	}

	public void setValorPercentualHonorario(double valorPercentualHonorario) {
		this.valorPercentualHonorario = valorPercentualHonorario;
	}

	@OneToMany(cascade = { CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "rpvPessoaParte")
	public List<RpvParteRepresentante> getRpvRepresentanteList() {
		return rpvRepresentanteList;
	}

	public void setRpvRepresentanteList(List<RpvParteRepresentante> rpvRepresentanteList) {
		this.rpvRepresentanteList = rpvRepresentanteList;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "rpvPessoaParte")
	public RpvParteDeducao getRpvParteDeducao() {
		return rpvParteDeducao;
	}

	public void setRpvParteDeducao(RpvParteDeducao rpvParteDeducao) {
		this.rpvParteDeducao = rpvParteDeducao;
	}

	@OneToMany(cascade = { CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "rpvPessoaParte")
	public List<RpvParteValorCompensar> getRpvParteValorCompensarList() {
		return rpvParteValorCompensarList;
	}

	public void setRpvParteValorCompensarList(List<RpvParteValorCompensar> rpvParteValorCompensarList) {
		this.rpvParteValorCompensarList = rpvParteValorCompensarList;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "in_participacao", nullable = false)
	@NotNull
	public RpvPessoaParteParticipacaoEnum getInParticipacao() {
		return inParticipacao;
	}

	public void setInParticipacao(RpvPessoaParteParticipacaoEnum inParticipacao) {
		this.inParticipacao = inParticipacao;
	}

	@Column(name = "vl_valor_integral_debito")
	public double getValorIntegralDebito() {
		return valorIntegralDebito;
	}

	public void setValorIntegralDebito(double valorIntegralDebito) {
		this.valorIntegralDebito = valorIntegralDebito;
	}

	@Column(name = "vl_ir", nullable = false)
	@NotNull
	public double getValorIR() {
		return valorIR;
	}
	
	public void setValorIR(double valorIR) {
		this.valorIR = valorIR;
	}	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RpvPessoaParte)) {
			return false;
		}
		RpvPessoaParte other = (RpvPessoaParte) obj;
		if (getIdRpvPessoaParte() != other.getIdRpvPessoaParte()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdRpvPessoaParte();
		return result;
	}

	@Override
	public String toString() {
		return getPessoa().getNome();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RpvPessoaParte> getEntityClass() {
		return RpvPessoaParte.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRpvPessoaParte());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
