package br.com.infox.pje.bean;

import java.io.Serializable;

/**
 * Bean para exibição da listagem do relatório na justiça federal estatística de
 * procesos distribuídos
 * 
 * @author Rafael
 * 
 */
public class EstatisticaJusticaFederalProcessosDistribuidosBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4083645980303412579L;
	private String secao;
	private EstatisticaJFProcessosDistribuidosVara vara;

	private int totalGeral;

	public String getSecao() {
		return secao;
	}

	public void setSecao(String secao) {
		this.secao = secao;
	}

	public int getTotalGeral() {
		return totalGeral;
	}

	public void setTotalGeral(int totalGeral) {
		this.totalGeral = totalGeral;
	}

	public EstatisticaJFProcessosDistribuidosVara getVara() {
		return vara;
	}

	public void setVara(EstatisticaJFProcessosDistribuidosVara vara) {
		this.vara = vara;
	}

}