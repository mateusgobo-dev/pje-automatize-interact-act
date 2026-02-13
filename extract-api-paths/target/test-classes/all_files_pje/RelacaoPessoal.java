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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_relacao_pessoal")
@org.hibernate.annotations.GenericGenerator(name = "gen_relacao_pessoal", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_relacao_pessoal"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RelacaoPessoal implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RelacaoPessoal,Integer> {

	private static final long serialVersionUID = 1L;

	private int id;
	private Date dataInicioRelacao;
	private Date dataFimRelacao;
	private Pessoa pessoaRepresentada;
	private Pessoa pessoaRepresentante;
	private TipoRelacaoPessoal tipoRelacaoPessoal;
	private Boolean ativo;

	public RelacaoPessoal() {
	}

	@Id
	@GeneratedValue(generator = "gen_relacao_pessoal")
	@Column(name = "id_relacao_pessoal", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio_relacao", nullable = false)
	@NotNull
	public Date getDataInicioRelacao() {
		return this.dataInicioRelacao;
	}

	public void setDataInicioRelacao(Date dataInicioRelacao) {
		this.dataInicioRelacao = dataInicioRelacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim_relacao", nullable = true)
	public Date getDataFimRelacao() {
		return this.dataFimRelacao;
	}

	public void setDataFimRelacao(Date dataFimRelacao) {
		this.dataFimRelacao = dataFimRelacao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_representado")
	public Pessoa getPessoaRepresentada() {
		if (this.pessoaRepresentada == null) {
			this.pessoaRepresentada = new Pessoa();
		}
		return this.pessoaRepresentada;
	}

	public void setPessoaRepresentada(Pessoa pessoaRepresentada) {
		this.pessoaRepresentada = pessoaRepresentada;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaRepresentada(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoaRepresentada(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaRepresentada(pessoa.getPessoa());
		} else {
			setPessoaRepresentada((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_representante")
	public Pessoa getPessoaRepresentante() {
		return this.pessoaRepresentante;
	}

	public void setPessoaRepresentante(Pessoa pessoaRepresentante) {
		this.pessoaRepresentante = pessoaRepresentante;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaRepresentante(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoaRepresentante(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaRepresentante(pessoa.getPessoa());
		} else {
			setPessoaRepresentante((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cd_tipo_relacao_pessoal")
	public TipoRelacaoPessoal getTipoRelacaoPessoal() {
		return this.tipoRelacaoPessoal;
	}

	public void setTipoRelacaoPessoal(TipoRelacaoPessoal tipoRelacaoPessoal) {
		this.tipoRelacaoPessoal = tipoRelacaoPessoal;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return "representado=" + (this.pessoaRepresentada == null ? "null" : this.pessoaRepresentada.getNome()) + "|"
				+ "representante=" + (this.pessoaRepresentante == null ? "null" : this.pessoaRepresentante.getNome())
				+ "|" + "relacao="
				+ (this.tipoRelacaoPessoal == null ? "null" : this.tipoRelacaoPessoal.getTipoRelacaoPessoal());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getPessoaRepresentada() == null) ? 0 : pessoaRepresentada.hashCode());
		result = prime * result + ((getPessoaRepresentante() == null) ? 0 : pessoaRepresentante.hashCode());
		result = prime * result + ((getTipoRelacaoPessoal() == null) ? 0 : tipoRelacaoPessoal.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RelacaoPessoal))
			return false;
		RelacaoPessoal other = (RelacaoPessoal) obj;
		if (getPessoaRepresentada() == null) {
			if (other.getPessoaRepresentada() != null)
				return false;
		} else if (!pessoaRepresentada.equals(other.getPessoaRepresentada()))
			return false;
		if (getPessoaRepresentante() == null) {
			if (other.getPessoaRepresentante() != null)
				return false;
		} else if (!pessoaRepresentante.equals(other.getPessoaRepresentante()))
			return false;
		if (getTipoRelacaoPessoal() == null) {
			if (other.getTipoRelacaoPessoal() != null)
				return false;
		} else if (!tipoRelacaoPessoal.equals(other.getTipoRelacaoPessoal()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RelacaoPessoal> getEntityClass() {
		return RelacaoPessoal.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getId());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
