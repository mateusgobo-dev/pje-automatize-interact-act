package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TarefaDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private Long idTask; //<- jbpm_task.id_
	private String nome;
	
	public TarefaDTO() {
		super();
	}
	
	public TarefaDTO(Long idTask, String nome) {
		this.idTask = idTask;
		this.nome = nome;
	}

	public Long getIdTask() {
		return idTask;
	}

	public void setIdTask(Long idTask) {
		this.idTask = idTask;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
}
