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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_rpv_parte_deducao")
@org.hibernate.annotations.GenericGenerator(name = "gen_rpv_parte_deducao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_rpv_parte_deducao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RpvParteDeducao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RpvParteDeducao,Integer> {
	private static final long serialVersionUID = 1L;

	private int idRpvParteDeducao;
	private RpvPessoaParte rpvPessoaParte;
	private Double valorDespesaJudicial;
	private Double valorHonorarioAdvogado;
	private Double valorPss;
	private Double valorPensaoAlimenticia;

	@Id
	@GeneratedValue(generator = "gen_rpv_parte_deducao")
	@Column(name = "id_rpv_parte_deducao")
	public int getIdRpvParteDeducao() {
		return idRpvParteDeducao;
	}

	public void setIdRpvParteDeducao(int idRpvParteDeducao) {
		this.idRpvParteDeducao = idRpvParteDeducao;
	}

	@OneToOne(cascade = { CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_rpv_pessoa_parte_deducao", nullable = false)
	@NotNull
	public RpvPessoaParte getRpvPessoaParte() {
		return rpvPessoaParte;
	}

	public void setRpvPessoaParte(RpvPessoaParte rpvPessoaParte) {
		this.rpvPessoaParte = rpvPessoaParte;
	}

	@Column(name = "vl_despesa_judicial")
	public Double getValorDespesaJudicial() {
		return valorDespesaJudicial;
	}

	public void setValorDespesaJudicial(Double valorDespesaJudicial) {
		this.valorDespesaJudicial = valorDespesaJudicial;
	}

	@Column(name = "vl_honorario_adv")
	public Double getValorHonorarioAdvogado() {
		return valorHonorarioAdvogado;
	}

	public void setValorHonorarioAdvogado(Double valorHonorarioAdvogado) {
		this.valorHonorarioAdvogado = valorHonorarioAdvogado;
	}

	@Column(name = "vl_valor_pss_deducao")
	public Double getValorPss() {
		return valorPss;
	}

	public void setValorPss(Double valorPss) {
		this.valorPss = valorPss;
	}

	@Column(name = "vl_pensao_alimenticia")
	public Double getValorPensaoAlimenticia() {
		return valorPensaoAlimenticia;
	}

	public void setValorPensaoAlimenticia(Double valorPensaoAlimenticia) {
		this.valorPensaoAlimenticia = valorPensaoAlimenticia;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdRpvParteDeducao();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof RpvParteDeducao)
			return false;
		RpvParteDeducao other = (RpvParteDeducao) obj;
		if (getIdRpvParteDeducao() != other.getIdRpvParteDeducao())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getRpvPessoaParte().getPessoa().getNome();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RpvParteDeducao> getEntityClass() {
		return RpvParteDeducao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRpvParteDeducao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
