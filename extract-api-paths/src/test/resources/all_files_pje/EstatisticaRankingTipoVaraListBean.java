package br.com.infox.pje.bean;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * distribuídos no Ranking/Secao
 * 
 * @author Daniel
 * 
 */
public class EstatisticaRankingTipoVaraListBean {

	private String varas;
	private String qntProcessos;
	private String rankingTipoVara;

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

	public void setRankingTipoVara(String rankingTipoVara) {
		this.rankingTipoVara = rankingTipoVara;
	}

	public String getRankingTipoVara() {
		return rankingTipoVara;
	}

}