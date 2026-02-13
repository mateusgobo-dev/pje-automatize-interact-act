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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_processo_lote")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_lote", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_lote"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoLote implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoLote,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoLote;
	private ProcessoTrf processoTrf;
	private Lote lote;
	private Date dtInclusaoProcesso;
	private Pessoa pessoaInclusao;

	public ProcessoLote() {
	}

	@Id
	@GeneratedValue(generator = "gen_processo_lote")
	@Column(name = "id_processo_lote", unique = true, nullable = false)
	public int getIdProcessoLote() {
		return this.idProcessoLote;
	}

	public void setIdProcessoLote(int idProcessoLote) {
		this.idProcessoLote = idProcessoLote;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_lote", nullable = false)
	@NotNull
	public Lote getLote() {
		return this.lote;
	}

	public void setLote(Lote lote) {
		this.lote = lote;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_inclusao", nullable = false)
	@NotNull
	public Pessoa getPessoaInclusao() {
		return this.pessoaInclusao;
	}

	public void setPessoaInclusao(Pessoa pessoaInclusao) {
		this.pessoaInclusao = pessoaInclusao;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaInclusao(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaInclusao(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaInclusao(pessoa.getPessoa());
		} else {
			setPessoaInclusao((Pessoa)null);
		}
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao_processo")
	public Date getDtInclusaoProcesso() {
		return this.dtInclusaoProcesso;
	}

	public void setDtInclusaoProcesso(Date dtInclusaoProcesso) {
		this.dtInclusaoProcesso = dtInclusaoProcesso;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoLote)) {
			return false;
		}
		ProcessoLote other = (ProcessoLote) obj;
		if (getIdProcessoLote() != other.getIdProcessoLote()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoLote();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoLote> getEntityClass() {
		return ProcessoLote.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoLote());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
