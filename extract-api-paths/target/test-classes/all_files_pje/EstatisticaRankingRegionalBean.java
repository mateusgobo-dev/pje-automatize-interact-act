package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * arquivados no Ranking/Regional
 * 
 * @author thiago
 * 
 */
public class EstatisticaRankingRegionalBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8028760946538397223L;
	private List<EstatisticaRankingRegionalListBean> estatisticaRankingListBean = new ArrayList<EstatisticaRankingRegionalListBean>();

	public List<EstatisticaRankingRegionalListBean> getEstatisticaRankingListBean() {
		return estatisticaRankingListBean;
	}

	public void setEstatisticaRankingListBean(List<EstatisticaRankingRegionalListBean> estatisticaRankingListBean) {
		this.estatisticaRankingListBean = estatisticaRankingListBean;
	}

}