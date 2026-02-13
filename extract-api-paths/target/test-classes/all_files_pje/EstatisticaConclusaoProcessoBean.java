package br.com.infox.pje.bean;

import java.io.Serializable;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * concluídos
 * 
 * @author Wilson
 * 
 */
public class EstatisticaConclusaoProcessoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6912312456400985332L;
	private String secaoJustica;
	private int totalGeral;
	private EstatisticaJFConclusaoProcessoVara vara;

	public String getSecaoJustica() {
		return secaoJustica;
	}

	public void setSecaoJustica(String secaoJustica) {
		this.secaoJustica = secaoJustica;
	}

	public int getTotalGeral() {
		return totalGeral;
	}

	public void setTotalGeral(int totalGeral) {
		this.totalGeral = totalGeral;
	}

	public EstatisticaJFConclusaoProcessoVara getVara() {
		return vara;
	}

	public void setVara(EstatisticaJFConclusaoProcessoVara vara) {
		this.vara = vara;
	}
}