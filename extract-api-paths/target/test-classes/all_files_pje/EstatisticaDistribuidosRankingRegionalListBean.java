package br.com.infox.pje.bean;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * distribuídos no Ranking Regional
 * 
 * @author Paulo
 * 
 */
public class EstatisticaDistribuidosRankingRegionalListBean extends EstatisticaRankingBaseListBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2715587713561118327L;
	private String rankingRegiao;

	public void setRankingRegiao(String rankingRegiao) {
		this.rankingRegiao = rankingRegiao;
	}

	public String getRankingRegiao() {
		return rankingRegiao;
	}

}