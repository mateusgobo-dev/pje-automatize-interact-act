package br.com.infox.pje.bean;

import java.io.Serializable;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * distribuídos no Ranking/Secao
 * 
 * @author Daniel
 * 
 */
public class MapaDistribuicaoVarasListBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4389422047895634480L;
	private String varas;

	public void setVaras(String varas) {
		this.varas = varas;
	}

	public String getVaras() {
		return varas;
	}
}