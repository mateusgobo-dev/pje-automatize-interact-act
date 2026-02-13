package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * distribuídos no Ranking/Regional
 * 
 * @author Rafael
 * 
 */
public class EstatisticaJulgadosRankingRegionalBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1414026591412643006L;
	private String codEstado;
	private List<EstatisticaJulgadosRankingRegionalListBean> estatisticaRankingRegionalListBean = new ArrayList<EstatisticaJulgadosRankingRegionalListBean>();
	private Long totalProcessosEstado;
	private Integer totalVarasEstado;

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public void setTotalProcessosEstado(Long totalProcessosEstado) {
		this.totalProcessosEstado = totalProcessosEstado;
	}

	public Long getTotalProcessosEstado() {
		return totalProcessosEstado;
	}

	public void setTotalVarasEstado(Integer totalVarasEstado) {
		this.totalVarasEstado = totalVarasEstado;
	}

	public Integer getTotalVarasEstado() {
		return totalVarasEstado;
	}

	public void setEstatisticaRankingRegionalListBean(
			List<EstatisticaJulgadosRankingRegionalListBean> estatisticaRankingRegionalListBean) {
		this.estatisticaRankingRegionalListBean = estatisticaRankingRegionalListBean;
	}

	public List<EstatisticaJulgadosRankingRegionalListBean> getEstatisticaRankingRegionalListBean() {
		return estatisticaRankingRegionalListBean;
	}
}