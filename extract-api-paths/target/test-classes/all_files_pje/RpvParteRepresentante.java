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
@Table(name = "tb_rpv_parte_representante")
@org.hibernate.annotations.GenericGenerator(name = "gen_rpv_parte_representante", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_rpv_parte_representante"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RpvParteRepresentante implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RpvParteRepresentante,Integer> {
	private static final long serialVersionUID = 1L;

	private int idRpvParteRepresentante;
	private RpvPessoaParte rpvPessoaParte;
	private RpvPessoaParte rpvPessoaRepresentante;
	private Double valorPagoPessoa;

	@Id
	@GeneratedValue(generator = "gen_rpv_parte_representante")
	@Column(name = "id_rpv_parte_representante", unique = true, nullable = false)
	public int getIdRpvParteRepresentante() {
		return idRpvParteRepresentante;
	}

	public void setIdRpvParteRepresentante(int idRpvParteRepresentante) {
		this.idRpvParteRepresentante = idRpvParteRepresentante;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_rpv_pessoa_parte", nullable = false)
	@NotNull
	public RpvPessoaParte getRpvPessoaParte() {
		return rpvPessoaParte;
	}

	public void setRpvPessoaParte(RpvPessoaParte rpvPessoaParte) {
		this.rpvPessoaParte = rpvPessoaParte;
	}

	@Column(name = "vl_valor_pago_pessoa")
	public Double getValorPagoPessoa() {
		return valorPagoPessoa;
	}

	public void setValorPagoPessoa(Double valorPagoPessoa) {
		this.valorPagoPessoa = valorPagoPessoa;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_rpv_pessoa_representante", nullable = false)
	@NotNull
	public RpvPessoaParte getRpvPessoaRepresentante() {
		return rpvPessoaRepresentante;
	}

	public void setRpvPessoaRepresentante(RpvPessoaParte rpvPessoaRepresentante) {
		this.rpvPessoaRepresentante = rpvPessoaRepresentante;
	}

	@Override
	public String toString() {
		return rpvPessoaRepresentante.getPessoa().getNome();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RpvParteRepresentante)) {
			return false;
		}
		RpvParteRepresentante other = (RpvParteRepresentante) obj;
		if (getIdRpvParteRepresentante() != other.getIdRpvParteRepresentante()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdRpvParteRepresentante();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RpvParteRepresentante> getEntityClass() {
		return RpvParteRepresentante.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRpvParteRepresentante());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
