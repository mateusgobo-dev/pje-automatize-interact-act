package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * distribuídos no Ranking Regional
 * 
 * @author Paulo
 * 
 */
public class EstatisticaDistribuidosRankingRegionalBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7344491092117627439L;
	private List<EstatisticaDistribuidosRankingRegionalListBean> estatisticaRankingRegionalListBean = new ArrayList<EstatisticaDistribuidosRankingRegionalListBean>();
	private Long totalProcessos;
	private Integer totalVaras;

	public void setTotalProcessos(Long totalProcessos) {
		this.totalProcessos = totalProcessos;
	}

	public Long getTotalProcessos() {
		return totalProcessos;
	}

	public void setTotalVaras(Integer totalVaras) {
		this.totalVaras = totalVaras;
	}

	public Integer getTotalVaras() {
		return totalVaras;
	}

	public void setEstatisticaRankingListBean(
			List<EstatisticaDistribuidosRankingRegionalListBean> estatistticaRankingRegionalListBean) {
		this.estatisticaRankingRegionalListBean = estatistticaRankingRegionalListBean;
	}

	public List<EstatisticaDistribuidosRankingRegionalListBean> getEstatisticaRankingRegionalListBean() {
		return estatisticaRankingRegionalListBean;
	}

}