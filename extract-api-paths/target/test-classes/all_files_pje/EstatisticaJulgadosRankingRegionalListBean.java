package br.com.infox.pje.bean;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * distribuídos no Ranking/Regional
 * 
 * @author Rafael
 * 
 */
public class EstatisticaJulgadosRankingRegionalListBean {

	private String varas;
	private String qntProcessos;
	private String rankingRegional;

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

	public void setRankingRegional(String rankingRegional) {
		this.rankingRegional = rankingRegional;
	}

	public String getRankingRegional() {
		return rankingRegional;
	}

}