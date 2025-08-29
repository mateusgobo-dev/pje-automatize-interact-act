package br.com.infox.pje.bean;

import java.io.Serializable;

/**
 * Bean que servirá de base para exibição da listagem do relatório na
 * estatística de procesos arquivaos ranking
 * 
 * @author Wilson
 * 
 */
public class EstatisticaArquivadoRankingListBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -907448737305299346L;
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