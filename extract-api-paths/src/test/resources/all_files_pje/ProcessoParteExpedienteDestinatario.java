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


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@IdClass(ProcessoParteExpedienteDestinatarioPK.class)
@Table(name = "vs_destinatario_expediente")
public class ProcessoParteExpedienteDestinatario implements java.io.Serializable{


	private static final long serialVersionUID = 1L;

	
	private ProcessoParteExpediente processoParteExpediente;
	
	
	private Integer idDestinatario;

	@Id
	public ProcessoParteExpediente getProcessoParteExpediente(){
		return this.processoParteExpediente;
	}

	public void setProcessoParteExpediente(ProcessoParteExpediente processoParteExpediente){
		this.processoParteExpediente = processoParteExpediente;
	}
	
	@Id
	public Integer getIdDestinatario(){
		return idDestinatario;
	}

	public void setIdDestinatario(Integer idDestinatario){
		this.idDestinatario = idDestinatario;
	}


}