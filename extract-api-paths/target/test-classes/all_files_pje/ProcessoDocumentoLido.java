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
@Table(name = "tb_processo_documento_lido")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_documento_lido", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_documento_lido"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoDocumentoLido implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoDocumentoLido,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoDocumentoLido;
	private ProcessoDocumento processoDocumento;
	private Pessoa pessoa;
	private Date dataApreciacao;

	public ProcessoDocumentoLido() {
	}

	@Id
	@GeneratedValue(generator = "gen_processo_documento_lido")
	@Column(name = "id_processo_documento_lido", unique = true, nullable = false)
	public int getIdProcessoDocumentoLido() {
		return idProcessoDocumentoLido;
	}

	public void setIdProcessoDocumentoLido(int idProcessoDocumentoLido) {
		this.idProcessoDocumentoLido = idProcessoDocumentoLido;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento", nullable = false)
	@NotNull
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_leu")
	@NotNull
	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_apreciacao", nullable = false)
	@NotNull
	public Date getDataApreciacao() {
		return dataApreciacao;
	}

	public void setDataApreciacao(Date dataApreciacao) {
		this.dataApreciacao = dataApreciacao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoDocumentoLido)) {
			return false;
		}
		ProcessoDocumentoLido other = (ProcessoDocumentoLido) obj;
		if (getIdProcessoDocumentoLido() != other.getIdProcessoDocumentoLido()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoDocumentoLido();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumentoLido> getEntityClass() {
		return ProcessoDocumentoLido.class;
	}
	
	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoDocumentoLido());
	}
	
	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}