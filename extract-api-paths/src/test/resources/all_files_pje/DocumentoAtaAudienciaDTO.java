package br.jus.csjt.pje.commons.model.dto;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.ProcessoAudiencia;

public class DocumentoAtaAudienciaDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private ProcessoAudiencia processoAudiencia;
	private boolean assina;
	private Integer idTarefa;
	private String nomeTarefa;

	public ProcessoAudiencia getProcessoAudiencia() {
		return processoAudiencia;
	}

	public void setProcessoAudiencia(ProcessoAudiencia processoAudiencia) {
		this.processoAudiencia = processoAudiencia;
	}

	public boolean isAssina() {
		return assina;
	}

	public void setAssina(boolean assina) {
		this.assina = assina;
	}

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

}
