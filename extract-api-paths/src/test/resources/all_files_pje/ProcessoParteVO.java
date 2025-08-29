/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.jus.je.pje.entity.vo;

import br.jus.pje.nucleo.entidades.ProcessoParte;

/**
 * Classe criada para encapsular informações dos polos para uso na tela de Autos 
 * digitais.
 *
 * @author Carlos Lisboa.
 */
public class ProcessoParteVO implements Comparable<ProcessoParteVO>{
    
	private int idRepresentado;
	private String nomeParteDetalhes;
	private String nomeParte;
	private ProcessoParte processoParte;
	private boolean representante;
	private boolean procuradoria;
	private boolean podeVisualizar;
	
	public int getIdRepresentado() {
		return idRepresentado;
	}
	public void setIdRepresentado(int idRepresentado) {
		this.idRepresentado = idRepresentado;
	}
	public String getNomeParteDetalhes() {
		return nomeParteDetalhes;
	}
	public void setNomeParteDetalhes(String nomeParte) {
		this.nomeParteDetalhes = nomeParte;
	}
	
	public ProcessoParte getProcessoParte() {
		return processoParte;
	}
	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
	}
	public boolean isRepresentante() {
		return representante;
	}
	public void setRepresentante(boolean representante) {
		this.representante = representante;
	}
	public boolean isProcuradoria() {
		return procuradoria;
	}
	public void setProcuradoria(boolean procuradoria) {
		this.procuradoria = procuradoria;
	}
	
	public String getNomeParte() {
		return nomeParte;
	}
	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((processoParte == null) ? 0 : processoParte.hashCode());
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
		ProcessoParteVO other = (ProcessoParteVO) obj;
		if (processoParte == null) {
			if (other.processoParte != null)
				return false;
		} else if (!processoParte.equals(other.processoParte))
			return false;
		return true;
	}
	@Override
	public int compareTo(ProcessoParteVO o) {
		// TODO Auto-generated method stub
		return (hashCode() == 0) ? 0 : (this.processoParte.getNomeParte().compareTo(o.getProcessoParte().getNomeParte()) == -1 ? -1 : 1);
	}
	public boolean isPodeVisualizar() {
		return podeVisualizar;
	}
	public void setPodeVisualizar(boolean podeVisualizar) {
		this.podeVisualizar = podeVisualizar;
	}
}
