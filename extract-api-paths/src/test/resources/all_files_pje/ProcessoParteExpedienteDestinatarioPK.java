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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

public class ProcessoParteExpedienteDestinatarioPK implements java.io.Serializable{


	private static final long serialVersionUID = -3394438418894150135L;
	private ProcessoParteExpediente processoParteExpediente;
	private Integer idDestinatario;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte_expediente", nullable=false,insertable = false, updatable = false)
	public ProcessoParteExpediente getProcessoParteExpediente(){
		return this.processoParteExpediente;
	}

	public void setProcessoParteExpediente(ProcessoParteExpediente processoParteExpediente){
		this.processoParteExpediente = processoParteExpediente;
	}
	
	@Column(name = "id_destinatario", insertable = false, updatable = false)
	public Integer getIdDestinatario(){
		return idDestinatario;
	}

	public void setIdDestinatario(Integer idDestinatario){
		this.idDestinatario = idDestinatario;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idDestinatario == null) ? 0 : idDestinatario.hashCode());
		result = prime * result + ((processoParteExpediente == null) ? 0 : processoParteExpediente.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessoParteExpedienteDestinatarioPK other = (ProcessoParteExpedienteDestinatarioPK) obj;
		if (idDestinatario == null) {
			if (other.idDestinatario != null)
				return false;
		} else if (!idDestinatario.equals(other.idDestinatario))
			return false;
		if (processoParteExpediente == null) {
			if (other.processoParteExpediente != null)
				return false;
		} else if (!processoParteExpediente.equals(other.processoParteExpediente))
			return false;
		return true;
	}


}