package br.com.infox.pje.bean;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * distribuídos no Ranking/Secao
 * 
 * @author Daniel
 * 
 */
public class EstatisticaRankingListBean extends EstatisticaRankingBaseListBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6954432079108718260L;
	private String rankingSecao;

	public void setRankingSecao(String rankingSecao) {
		this.rankingSecao = rankingSecao;
	}

	public String getRankingSecao() {
		return rankingSecao;
	}

}