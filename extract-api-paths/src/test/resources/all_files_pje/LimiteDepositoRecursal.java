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
package br.jus.pje.jt.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = LimiteDepositoRecursal.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_limite_dep_recursal", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sal_minimo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class LimiteDepositoRecursal implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<LimiteDepositoRecursal,Integer> {

	public static final String TABLE_NAME = "tb_limite_dep_recursal";
	private static final long serialVersionUID = 1L;

	private int idLimiteDepositoRecursal;
	private Date dataInicioVigencia;
	private Date dataFimVigencia;
	private String moeda;
	private Double valor1Grau;
	private Double valor2Grau;
	private String fundamentacaoLegal;

	@Id
	@GeneratedValue(generator = "gen_limite_dep_recursal")
	@Column(name = "id_limite_dep_recursal", nullable = false, unique = true)
	@NotNull
	public int getIdLimiteDepositoRecursal() {
		return idLimiteDepositoRecursal;
	}

	public void setIdLimiteDepositoRecursal(int idLimiteDepositoRecursal) {
		this.idLimiteDepositoRecursal = idLimiteDepositoRecursal;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio_vigencia")
	public Date getDataInicioVigencia() {
		return dataInicioVigencia;
	}

	public void setDataInicioVigencia(Date dataInicioVigencia) {
		this.dataInicioVigencia = dataInicioVigencia;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim_vigencia")
	public Date getDataFimVigencia() {
		return dataFimVigencia;
	}

	public void setDataFimVigencia(Date dataFimVigencia) {
		this.dataFimVigencia = dataFimVigencia;
	}

	@Column(name = "ds_moeda", length = 50)
	@Length(max = 50)
	public String getMoeda() {
		return moeda;
	}

	public void setMoeda(String moeda) {
		this.moeda = moeda;
	}

	@Column(name = "vl_deposito_1grau")
	public Double getValor1Grau() {
		return valor1Grau;
	}

	public void setValor1Grau(Double valor1Grau) {
		this.valor1Grau = valor1Grau;
	}
	
	@Column(name = "vl_deposito_2grau")
	public Double getValor2Grau() {
		return valor2Grau;
	}

	public void setValor2Grau(Double valor2Grau) {
		this.valor2Grau = valor2Grau;
	}

	@Column(name = "ds_fund_legal")
	public String getFundamentacaoLegal() {
		return fundamentacaoLegal;
	}

	public void setFundamentacaoLegal(String fundamentacaoLegal) {
		this.fundamentacaoLegal = fundamentacaoLegal;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends LimiteDepositoRecursal> getEntityClass() {
		return LimiteDepositoRecursal.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdLimiteDepositoRecursal());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
