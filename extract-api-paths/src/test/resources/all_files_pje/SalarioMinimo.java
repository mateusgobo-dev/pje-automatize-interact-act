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
@Table(name = SalarioMinimo.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_sal_minimo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sal_minimo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SalarioMinimo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<SalarioMinimo,Integer> {

	public static final String TABLE_NAME = "tb_sal_minimo";
	private static final long serialVersionUID = 1L;

	private int idSalarioMinimo;
	private Date dataInicioVigencia;
	private Date dataFimVigencia;
	private String moeda;
	private Double valor;
	private String fundamentacaoLegal;

	@Id
	@GeneratedValue(generator = "gen_sal_minimo")
	@Column(name = "id_sal_minimo", columnDefinition = "integer", nullable = false, unique = true)
	@NotNull
	public int getIdSalarioMinimo() {
		return idSalarioMinimo;
	}

	public void setIdSalarioMinimo(int idSalarioMinimo) {
		this.idSalarioMinimo = idSalarioMinimo;
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

	@Column(name = "vl_salario")
	public Double getValor() {
		return valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
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
	public Class<? extends SalarioMinimo> getEntityClass() {
		return SalarioMinimo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdSalarioMinimo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
