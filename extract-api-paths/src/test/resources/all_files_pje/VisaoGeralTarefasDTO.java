package br.jus.pje.nucleo.dto;

import java.util.ArrayList;
import java.util.List;

public class VisaoGeralTarefasDTO {
	
	private Integer idTarefa;
	private String nomeTarefa;
	
	private Integer totalPendencias = 0;
	
	private List<VisaoGeralTarefaLocalizacaoDTO> locaisTarefas = new ArrayList<VisaoGeralTarefaLocalizacaoDTO>();

	public Integer getIdTarefa() {
		return idTarefa;
	}

	public void setIdTarefa(Integer idTarefa) {
		this.idTarefa = idTarefa;
	}

	public String getNomeTarefa() {
		return nomeTarefa;
	}

	public void setNomeTarefa(String nomeTarefa) {
		this.nomeTarefa = nomeTarefa;
	}

	public Integer getTotalPendencias() {
		return totalPendencias;
	}

	public void setTotalPendencias(Integer totalPendencias) {
		this.totalPendencias = totalPendencias;
	}

	public List<VisaoGeralTarefaLocalizacaoDTO> getLocaisTarefas() {
		return locaisTarefas;
	}

	public void setLocaisTarefas(List<VisaoGeralTarefaLocalizacaoDTO> locaisTarefas) {
		this.locaisTarefas = locaisTarefas;
	}
	
	public String getNomeTarefaDisplay(){
		return nomeTarefa.substring(0, nomeTarefa.lastIndexOf(":"));
	}
	
	
	
}
