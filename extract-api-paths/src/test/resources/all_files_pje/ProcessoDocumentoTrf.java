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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;


@Entity
@Table(name = "vs_processo_documento_trf")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_documento_trf", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoDocumentoTrf implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoDocumentoTrf,Integer> {

	private static final long serialVersionUID = 1L;
	private ProcessoTrf processoTrf;
	private ProcessoDocumento processoDocumento;
	private int idProcessoDocumento;
	private String tarefa;
	private boolean selecionado;

	@Id
	@GeneratedValue(generator = "gen_processo_documento_trf")
	@Column(name = "id_processo_documento_trf", unique = true, nullable = false)
	public int getIdProcessoDocumento() {
		return this.idProcessoDocumento;
	}

	public void setIdProcessoDocumento(int idProcessoDocumento) {
		this.idProcessoDocumento = idProcessoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", insertable = false, updatable = false)
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento_trf", insertable = false, updatable = false)
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@Column(name = "tarefa", insertable = false, updatable = false)
	public String getTarefa() {
		return tarefa;
	}

	public void setTarefa(String tarefa) {
		this.tarefa = tarefa;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoDocumentoTrf)) {
			return false;
		}
		ProcessoDocumentoTrf other = (ProcessoDocumentoTrf) obj;
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

	@Transient
	public boolean getSelecionado() {
		return selecionado;
	}

	public void setSelecionado(boolean selecionado) {
		this.selecionado = selecionado;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumentoTrf> getEntityClass() {
		return ProcessoDocumentoTrf.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoDocumento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
