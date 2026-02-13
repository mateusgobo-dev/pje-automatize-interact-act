package br.com.infox.pje.bean;

import java.util.List;

import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * concluidos
 * 
 * @author Wilson
 * 
 */
public class ConclusaoBean {

	private long totalClasse;
	private String classe;
	private List<ProcessoTrf> processoList;

	private long qtdRemanescente;
	private long qtdConclusoSentenca;
	private long qtdDevolvidoSentenca;
	private long qtdCovertidoDiligencia;
	private long qtdPendentesSentenca;

	public List<ProcessoTrf> getProcessoList() {
		return processoList;
	}

	public void setProcessoList(List<ProcessoTrf> processoList) {
		this.processoList = processoList;
		recalculaQuantidade();
	}

	private void recalculaQuantidade() {

	}

	public Long getQtdRemanescente() {
		return qtdRemanescente;
	}

	public Long getQtdConclusoSentenca() {
		return qtdConclusoSentenca;
	}

	public Long getQtdDevolvidoSentenca() {
		return qtdDevolvidoSentenca;
	}

	public Long getQtdCovertidoDiligencia() {
		return qtdCovertidoDiligencia;
	}

	public Long getQtdPendentesSentenca() {
		return qtdPendentesSentenca;
	}

	public long getTotalClasse() {
		return totalClasse;
	}

	public void setTotalClasse(long totalClasse) {
		this.totalClasse = totalClasse;
	}

	public String getClasse() {
		return classe;
	}

	public void setClasse(String classe) {
		this.classe = classe;
	}
}