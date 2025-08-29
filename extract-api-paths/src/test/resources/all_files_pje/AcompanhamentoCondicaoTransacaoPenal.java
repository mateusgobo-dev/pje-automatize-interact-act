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
@Table(name = "tb_acompa_cond_trans_penal")
@org.hibernate.annotations.GenericGenerator(name = "gen_acomp_trans_pen", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_acompanh_cndco_trnsco_penal"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AcompanhamentoCondicaoTransacaoPenal implements Serializable,
		Comparable<AcompanhamentoCondicaoTransacaoPenal> {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private CondicaoIcrTransacaoPenal condicaoIcrTransacaoPenal;
	private Integer numeroSequencia;
	private Date dataPrevista;
	private Date dataCumprimento;
	private String observacoes;

	@Id
	@GeneratedValue(generator = "gen_acomp_trans_pen")
	@Column(name = "id_acompanh_cndco_trnsco_penal", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_condcao_icr_transacao_penal", nullable = false)
	public CondicaoIcrTransacaoPenal getCondicaoIcrTransacaoPenal() {
		return condicaoIcrTransacaoPenal;
	}

	public void setCondicaoIcrTransacaoPenal(CondicaoIcrTransacaoPenal condicaoIcrTransacaoPenal) {
		this.condicaoIcrTransacaoPenal = condicaoIcrTransacaoPenal;
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
			hash = 31 * hash + getCondicaoIcrTransacaoPenal().hashCode();
			hash = 31 * hash + getNumeroSequencia().hashCode();
			return hash;
		}
	}

	@Override
	public boolean equals(Object obj) {
		AcompanhamentoCondicaoTransacaoPenal other = (AcompanhamentoCondicaoTransacaoPenal) obj;
		if (other == null) {
			return false;
		}

		if (other.getId() != null && this.getId() != null) {
			return other.getId().equals(this.getId());
		} else {
			if (other.getCondicaoIcrTransacaoPenal() != null && this.getCondicaoIcrTransacaoPenal() != null
					&& other.getNumeroSequencia() != null && this.getNumeroSequencia() != null) {

				return (other.getCondicaoIcrTransacaoPenal().equals(this.getCondicaoIcrTransacaoPenal()) && other
						.getNumeroSequencia().equals(this.getNumeroSequencia()));
			} else {
				return false;
			}
		}
	}

	@Override
	public int compareTo(AcompanhamentoCondicaoTransacaoPenal o) {
		if (this.getId() != null && o.getId() != null) {
			return this.getId().compareTo(o.getId());
		} else {
			int in = 0;

			if (this.getCondicaoIcrTransacaoPenal() != null
					&& this.getCondicaoIcrTransacaoPenal().getId() != null
					&& o.getCondicaoIcrTransacaoPenal() != null
					&& o.getCondicaoIcrTransacaoPenal().getId() != null) {
				in = this.getCondicaoIcrTransacaoPenal().getId().compareTo(o.getCondicaoIcrTransacaoPenal().getId());
			}

			if (this.getNumeroSequencia() != null && o.getNumeroSequencia() != null) {
				in = this.getNumeroSequencia().compareTo(o.getNumeroSequencia()) + in;
			}

			return in;
		}
	}
}
