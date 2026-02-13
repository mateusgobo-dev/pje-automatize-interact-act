package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * distribuídos no Ranking/Secao
 * 
 * @author Daniel
 * 
 */
public class EstatisticaRankingSecaoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1038599536900475716L;
	private String codEstado;
	private List<EstatisticaRankingListBean> estatisticaRankingListBean = new ArrayList<EstatisticaRankingListBean>();
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

	public void setEstatisticaRankingListBean(List<EstatisticaRankingListBean> estatistticaRankingListBean) {
		this.estatisticaRankingListBean = estatistticaRankingListBean;
	}

	public List<EstatisticaRankingListBean> getEstatisticaRankingListBean() {
		return estatisticaRankingListBean;
	}

}