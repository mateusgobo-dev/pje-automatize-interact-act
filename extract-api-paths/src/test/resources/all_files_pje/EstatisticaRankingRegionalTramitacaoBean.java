package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * tramitação no Ranking/Regional
 * 
 * @author thiago
 * 
 */
public class EstatisticaRankingRegionalTramitacaoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 766051221774096121L;
	private List<EstatisticaRankingRegionalTramitacaoListBean> estatisticaRankingListBean = new ArrayList<EstatisticaRankingRegionalTramitacaoListBean>();

	public List<EstatisticaRankingRegionalTramitacaoListBean> getEstatisticaRankingListBean() {
		return estatisticaRankingListBean;
	}

	public void setEstatisticaRankingListBean(
			List<EstatisticaRankingRegionalTramitacaoListBean> estatisticaRankingListBean) {
		this.estatisticaRankingListBean = estatisticaRankingListBean;
	}

}