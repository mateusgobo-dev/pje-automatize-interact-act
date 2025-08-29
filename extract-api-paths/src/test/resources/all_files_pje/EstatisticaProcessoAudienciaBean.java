package br.com.infox.pje.bean;

import java.io.Serializable;

public class EstatisticaProcessoAudienciaBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 182061699458285033L;
	private String secaoJustica;
	private int totalGeral;
	private EstatisticaJFProcessosAudienciaVara vara;

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

	public EstatisticaJFProcessosAudienciaVara getVara() {
		return vara;
	}

	public void setVara(EstatisticaJFProcessosAudienciaVara vara2) {
		this.vara = vara2;
	}
}