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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.SigiloStatusEnum;

@Entity
@Table(name = "tb_proc_documento_segredo")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_documento_segredo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_documento_segredo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoDocumentoSegredo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoDocumentoSegredo,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoDocumentoSegredo;
	private Pessoa pessoa;
	private ProcessoDocumento processoDocumento;
	private String motivo;
	private Date dtAlteracao;
	private SigiloStatusEnum status;

	@Id
	@GeneratedValue(generator = "gen_proc_documento_segredo")
	@Column(name = "id_processo_documento_segredo", nullable = false)
	public int getIdProcessoDocumentoSegredo() {
		return idProcessoDocumentoSegredo;
	}

	public void setIdProcessoDocumentoSegredo(int idProcessoDocumentoSegredo) {
		this.idProcessoDocumentoSegredo = idProcessoDocumentoSegredo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa")
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento")
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@Column(name = "ds_motivo", length = 200)
	@Length(max = 200)
	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_alteracao")
	public Date getDtAlteracao() {
		return dtAlteracao;
	}

	public void setDtAlteracao(Date dtAlteracao) {
		this.dtAlteracao = dtAlteracao;
	}

	@Column(name = "in_situacao_documento", length = 1)
	@Enumerated(EnumType.STRING)
	public SigiloStatusEnum getStatus() {
		return status;
	}

	public void setStatus(SigiloStatusEnum status) {
		this.status = status;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoDocumentoSegredo)) {
			return false;
		}
		ProcessoDocumentoSegredo other = (ProcessoDocumentoSegredo) obj;
		if (getIdProcessoDocumentoSegredo() != other.getIdProcessoDocumentoSegredo()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoDocumentoSegredo();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumentoSegredo> getEntityClass() {
		return ProcessoDocumentoSegredo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoDocumentoSegredo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
