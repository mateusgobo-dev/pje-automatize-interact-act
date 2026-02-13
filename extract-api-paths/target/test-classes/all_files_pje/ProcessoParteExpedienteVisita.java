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
@Table(name = "tb_proc_parte_exped_visita")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_parte_exped_visita", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_parte_exped_visita"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoParteExpedienteVisita implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoParteExpedienteVisita,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoParteExpedienteVisita;
	private ProcessoParteExpediente processoParteExpediente;
	private Visita visita;

	public ProcessoParteExpedienteVisita() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_parte_exped_visita")
	@Column(name = "id_proc_parte_expedente_visita", unique = true, nullable = false)
	public int getIdProcessoParteExpedienteVisita() {
		return this.idProcessoParteExpedienteVisita;
	}

	public void setIdProcessoParteExpedienteVisita(int idProcessoParteExpedienteVisita) {
		this.idProcessoParteExpedienteVisita = idProcessoParteExpedienteVisita;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte_expediente", nullable = false)
	@NotNull
	public ProcessoParteExpediente getProcessoParteExpediente() {
		return processoParteExpediente;
	}

	public void setProcessoParteExpediente(ProcessoParteExpediente processoParteExpediente) {
		this.processoParteExpediente = processoParteExpediente;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_visita", nullable = false)
	@NotNull
	public Visita getVisita() {
		return visita;
	}

	public void setVisita(Visita visita) {
		this.visita = visita;
	}

	@Override
	public String toString() {
		return processoParteExpediente != null ? processoParteExpediente.toString() : "";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoParteExpedienteVisita)) {
			return false;
		}
		ProcessoParteExpedienteVisita other = (ProcessoParteExpedienteVisita) obj;
		if (getIdProcessoParteExpedienteVisita() != other.getIdProcessoParteExpedienteVisita()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoParteExpedienteVisita();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoParteExpedienteVisita> getEntityClass() {
		return ProcessoParteExpedienteVisita.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoParteExpedienteVisita());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
