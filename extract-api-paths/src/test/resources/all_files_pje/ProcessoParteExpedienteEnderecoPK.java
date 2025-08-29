package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

public class ProcessoParteExpedienteEnderecoPK implements Serializable{
	
	private static final long serialVersionUID = 2348282372818010871L;

	@ManyToOne
	@JoinColumn(name="id_proc_parte_exp", referencedColumnName="id_processo_parte_expediente")
	private ProcessoParteExpediente processoParteExpediente;
	
	@ManyToOne
	@JoinColumn(name="id_endereco")
	private Endereco endereco;
	
	public ProcessoParteExpedienteEnderecoPK(){}
	
	public ProcessoParteExpedienteEnderecoPK(ProcessoParteExpediente processoParteExpediente, Endereco endereco){
		this.processoParteExpediente = processoParteExpediente;
		this.endereco = endereco;
	}
	
	public ProcessoParteExpediente getProcessoParteExpediente() {
		return processoParteExpediente;
	}
	
	public void setProcessoParteExpediente(
			ProcessoParteExpediente processoParteExpediente) {
		this.processoParteExpediente = processoParteExpediente;
	}
	
	public Endereco getEndereco() {
		return endereco;
	}
	
	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}
	
	@Override
	public boolean equals(Object obj) {		
		ProcessoParteExpedienteEnderecoPK pk = (ProcessoParteExpedienteEnderecoPK) obj;
		if(this.processoParteExpediente.equals(pk.getProcessoParteExpediente()) &&
				this.getEndereco().equals(pk.getEndereco())){
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		return (int) processoParteExpediente.hashCode() * endereco.hashCode();
	}
}