package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@IdClass(ProcessoParteExpedienteEnderecoPK.class)
@Table(name = "tb_proc_parte_exp_endereco")
public class ProcessoParteExpedienteEndereco implements Serializable{
	
	private static final long serialVersionUID = -4981506112749618029L;

	@Id
	private ProcessoParteExpediente processoParteExpediente;

	@Id
	private Endereco endereco;	

	@Column(name = "num_ar")
	private String numeroAr;
	
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
	
	public String getNumeroAr() {
		return numeroAr;
	}
	
	public void setNumeroAr(String numeroAr) {
		this.numeroAr = numeroAr;
	}
}
