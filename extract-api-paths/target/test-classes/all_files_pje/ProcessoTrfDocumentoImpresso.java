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
 * 
 * @author Marcone
 * 
 *         Entidade que faz a relação 1x1 com tb_processo_documento do core
 * 
 */
@Entity
@Table(name = "tb_proc_trf_doc_impresso")
public class ProcessoTrfDocumentoImpresso implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcessoDocumento;
	private ProcessoTrf processoTrf;
	private ProcessoDocumento processoDocumento;
	private Date dataImpressao;
	private Pessoa pessoaImpressao;

	private String certidao;

	public ProcessoTrfDocumentoImpresso() {
	}

	/**
	 * @return Retorna o id do ProcessoTrf que é igual ao id do
	 *         processoDocumento do core
	 */
	@Id
	@Column(name = "id_processo_documento", unique = true, nullable = false, updatable = false)
	public int getIdProcessoDocumento() {
		return idProcessoDocumento;
	}

	public void setIdProcessoDocumento(int idProcesso) {
		this.idProcessoDocumento = idProcesso;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_documento")
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
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

	@Transient
	public String getCertidao() {
		return certidao;
	}

	public void setCertidao(String certidao) {
		this.certidao = certidao;
	}

	@Override
	public String toString() {
		return getProcessoDocumento().toString() + "/" + (getImpresso() ? "Sim" : "Não");
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
	 * @param pessoa a pessoa especializada a ser atribuída.
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
		if (!(obj instanceof ProcessoTrfDocumentoImpresso)) {
			return false;
		}
		ProcessoTrfDocumentoImpresso other = (ProcessoTrfDocumentoImpresso) obj;
		if (getIdProcessoDocumento() != other.getIdProcessoDocumento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoDocumento();
		return result;
	}
}