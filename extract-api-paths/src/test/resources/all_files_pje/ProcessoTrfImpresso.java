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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * Entidade que faz a relação 1x1 com tb_processo_trf do core
 */
@Entity
@Table(name = "tb_processo_trf_impresso")
public class ProcessoTrfImpresso implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoTrfImpresso,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoTrf;
	private ProcessoTrf processoTrf;
	private Date dataImpressao;
	private Pessoa pessoaImpressao;

	public ProcessoTrfImpresso() {
	}

	/**
	 * @return Retorna o id do ProcessoTrf que é igual ao id do processoTrf do
	 *         core
	 */
	@Id
	@Column(name = "id_processo_trf", unique = true, nullable = false, updatable = false)
	public int getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(int idProcesso) {
		this.idProcessoTrf = idProcesso;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processo) {
		this.processoTrf = processo;
	}

	@Transient
	public Boolean getImpresso() {
		return getDataImpressao() != null ? Boolean.TRUE : Boolean.FALSE;
	}

	public void setImpresso(Boolean impresso) {
		if (impresso) {
			setDataImpressao(new Date());
		}
	}

	@Override
	public String toString() {
		return getProcessoTrf().toString() + "/" + (getImpresso() ? "Sim" : "Não");
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_impressao")
	public Date getDataImpressao() {
		return dataImpressao;
	}

	public void setDataImpressao(Date dataImpressao) {
		this.dataImpressao = dataImpressao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_impressao")
	public Pessoa getPessoaImpressao() {
		return pessoaImpressao;
	}

	public void setPessoaImpressao(Pessoa pessoaImpressao) {
		this.pessoaImpressao = pessoaImpressao;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaImpressao(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoaImpressao(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaImpressao(pessoa.getPessoa());
		} else {
			setPessoaImpressao((Pessoa)null);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoTrfImpresso)) {
			return false;
		}
		ProcessoTrfImpresso other = (ProcessoTrfImpresso) obj;
		if (getIdProcessoTrf() != other.getIdProcessoTrf()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoTrf();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoTrfImpresso> getEntityClass() {
		return ProcessoTrfImpresso.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoTrf());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
