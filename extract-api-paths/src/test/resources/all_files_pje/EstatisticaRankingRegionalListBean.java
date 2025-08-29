package br.com.infox.pje.bean;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * arquivados no Ranking/Regional
 * 
 * @author thiago
 * 
 */
public class EstatisticaRankingRegionalListBean {

	private String vara;
	private Long qtdProcessos;
	private String rankingRegional;

	public void setVara(String vara) {
		this.vara = vara;
	}

	public String getVara() {
		return vara;
	}

	public Long getQtdProcessos() {
		return qtdProcessos;
	}

	public void setQtdProcessos(Long qtdProcessos) {
		this.qtdProcessos = qtdProcessos;
	}

	public String getRankingRegional() {
		return rankingRegional;
	}

	public void setRankingRegional(String rankingRegional) {
		this.rankingRegional = rankingRegional;
	}

}