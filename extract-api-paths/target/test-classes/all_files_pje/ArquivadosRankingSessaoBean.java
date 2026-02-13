package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Edson
 * 
 */
public class ArquivadosRankingSessaoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4839766110431779622L;
	private String codEstado;
	private List<VaraProcessosArquivadosRankingSessaoBean> estatisticaRankingListBean = new ArrayList<VaraProcessosArquivadosRankingSessaoBean>();
	private Long totalProcessosEstado;
	private Integer totalVarasEstado;

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public List<VaraProcessosArquivadosRankingSessaoBean> getEstatisticaRankingListBean() {
		return estatisticaRankingListBean;
	}

	public void setEstatisticaRankingListBean(List<VaraProcessosArquivadosRankingSessaoBean> estatisticaRankingListBean) {
		this.estatisticaRankingListBean = estatisticaRankingListBean;
	}

	public Long getTotalProcessosEstado() {
		return totalProcessosEstado;
	}

	public void setTotalProcessosEstado(Long totalProcessosEstado) {
		this.totalProcessosEstado = totalProcessosEstado;
	}

	public Integer getTotalVarasEstado() {
		return totalVarasEstado;
	}

	public void setTotalVarasEstado(Integer totalVarasEstado) {
		this.totalVarasEstado = totalVarasEstado;
	}
}
