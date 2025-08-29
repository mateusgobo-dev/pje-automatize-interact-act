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
@Table(name = RpvParteValorCompensar.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_rpv_parte_vr_compensar", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_rpv_parte_vr_compensar"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RpvParteValorCompensar implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RpvParteValorCompensar,Integer> {

	public static final String TABLE_NAME = "tb_rpv_parte_vr_compensar";
	private static final long serialVersionUID = 1L;
	private int idRpvParteCompensar;
	private RpvPessoaParte rpvPessoaParte;
	private String codigoIdentificacao;
	private double valorCompensar = 0;
	private RpvNaturezaDebito rpvNaturezaDebito;

	@Id
	@GeneratedValue(generator = "gen_rpv_parte_vr_compensar")
	@Column(name = "id_rpv_parte_compensar", unique = true, nullable = false)
	public int getIdRpvParteCompensar() {
		return idRpvParteCompensar;
	}

	public void setIdRpvParteCompensar(int idRpvParteCompensar) {
		this.idRpvParteCompensar = idRpvParteCompensar;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_rpv_pessoa_parte_compensar", nullable = false)
	@NotNull
	public RpvPessoaParte getRpvPessoaParte() {
		return rpvPessoaParte;
	}

	public void setRpvPessoaParte(RpvPessoaParte rpvPessoaParte) {
		this.rpvPessoaParte = rpvPessoaParte;
	}

	@Column(name = "cd_identificacao", length = 30)
	public String getCodigoIdentificacao() {
		return codigoIdentificacao;
	}

	public void setCodigoIdentificacao(String codigoIdentificacao) {
		this.codigoIdentificacao = codigoIdentificacao;
	}

	@Column(name = "vl_valor_compensar_compensado")
	public double getValorCompensar() {
		return valorCompensar;
	}

	public void setValorCompensar(double valorCompensar) {
		this.valorCompensar = valorCompensar;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_rpv_natureza_debito")
	@NotNull
	public RpvNaturezaDebito getRpvNaturezaDebito() {
		return rpvNaturezaDebito;
	}

	public void setRpvNaturezaDebito(RpvNaturezaDebito rpvNaturezaDebito) {
		this.rpvNaturezaDebito = rpvNaturezaDebito;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdRpvParteCompensar();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof RpvParteValorCompensar)
			return false;
		RpvParteValorCompensar other = (RpvParteValorCompensar) obj;
		if (getIdRpvParteCompensar() != other.getIdRpvParteCompensar())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getRpvPessoaParte().getPessoa().getNome();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RpvParteValorCompensar> getEntityClass() {
		return RpvParteValorCompensar.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRpvParteCompensar());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
