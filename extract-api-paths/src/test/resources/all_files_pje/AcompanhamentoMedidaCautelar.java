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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_acmpnhmnto_med_cautelar")
@org.hibernate.annotations.GenericGenerator(name = "gen_acomp_med_caut", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_acmpnhmnto_med_cautelar"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AcompanhamentoMedidaCautelar  implements Serializable,
		Comparable<AcompanhamentoMedidaCautelar> {

	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private MedidaCautelarDiversa medidaCautelarDiversa;
	private Integer numeroSequencia;
	private Date dataPrevista;
	private Date dataCumprimento;
	private String observacoes;
	
	@Id
	@GeneratedValue(generator = "gen_acomp_med_caut")
	@Column(name = "id_acmpnhmnto_med_cautelar", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_medida_cautelar_diversa", nullable = false)
	public MedidaCautelarDiversa getMedidaCautelarDiversa() {
		return medidaCautelarDiversa;
	}

	public void setMedidaCautelarDiversa(MedidaCautelarDiversa medidaCautelarDiversa) {
		this.medidaCautelarDiversa = medidaCautelarDiversa;
	}
	
	@NotNull
	@Column(name = "nr_sequencia", nullable = false)
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}
	
	@NotNull
	@Column(name = "dt_prevista", nullable = false)
	public Date getDataPrevista() {
		return dataPrevista;
	}
	
	public void setDataPrevista(Date dataPrevista) {
		this.dataPrevista = dataPrevista;
	}

	@Column(name = "dt_cumprimento")
	public Date getDataCumprimento() {
		return dataCumprimento;
	}

	public void setDataCumprimento(Date dataCumprimento) {
		this.dataCumprimento = dataCumprimento;
	}
	
	@Length(max = 2000)
	@Column(name = "ds_obs")
	public String getObservacoes() {
		return observacoes;
	}
	
	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}
	
	@Override
	public int hashCode() {
		if (getId() != null) {
			return getId().hashCode();
		} else {
			int hash = 7;
			hash = 31 * hash + getMedidaCautelarDiversa().hashCode();
			hash = 31 * hash + getNumeroSequencia().hashCode();
			return hash;
		}
	}

	@Override
	public boolean equals(Object obj) {
		AcompanhamentoMedidaCautelar other = (AcompanhamentoMedidaCautelar) obj;
		if (other == null) {
			return false;
		}

		if (other.getId() != null && this.getId() != null) {
			return other.getId().equals(this.getId());
		} else {
			if (other.getMedidaCautelarDiversa() != null && this.getMedidaCautelarDiversa() != null
					&& other.getNumeroSequencia() != null && this.getNumeroSequencia() != null) {

				return (other.getMedidaCautelarDiversa().equals(this.getMedidaCautelarDiversa()) && other
						.getNumeroSequencia().equals(this.getNumeroSequencia()));
			} else {
				return false;
			}
		}
	}

	@Override
	public int compareTo(AcompanhamentoMedidaCautelar o) {
		if (this.getId() != null && o.getId() != null) {
			return this.getId().compareTo(o.getId());
		} else {
			int in = 0;

			if (this.getMedidaCautelarDiversa() != null
					&& this.getMedidaCautelarDiversa().getId() != null
					&& o.getMedidaCautelarDiversa() != null
					&& o.getMedidaCautelarDiversa().getId() != null) {
				in = this.getMedidaCautelarDiversa().getId().compareTo(o.getMedidaCautelarDiversa().getId());
			}

			if (this.getNumeroSequencia() != null && o.getNumeroSequencia() != null) {
				in = this.getNumeroSequencia().compareTo(o.getNumeroSequencia()) + in;
			}

			return in;
		}
	}
	
}
