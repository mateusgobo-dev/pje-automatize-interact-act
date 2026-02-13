package br.com.infox.pje.bean;

import java.io.Serializable;

/**
 * Bean que servirá de base para exibição da listagem do relatório na
 * estatística de procesos distribuídos
 * 
 * @author Paulo
 * 
 */
public class EstatisticaRankingBaseListBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1392394670855741763L;
	private String varas;
	private String qntProcessos;

	public void setVaras(String varas) {
		this.varas = varas;
	}

	public String getVaras() {
		return varas;
	}

	public void setQntProcessos(String qntProcessos) {
		this.qntProcessos = qntProcessos;
	}

	public String getQntProcessos() {
		return qntProcessos;
	}

}