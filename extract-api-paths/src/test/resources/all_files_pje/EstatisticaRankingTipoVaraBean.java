package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * distribuídos no Ranking/Secao
 * 
 * @author Fabio
 * 
 */
public class EstatisticaRankingTipoVaraBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5102164181268183913L;
	private String codEstado;
	private String competencia;
	private List<EstatisticaRankingTipoVaraListBean> estatisticaRankingTipoVaraListBean = new ArrayList<EstatisticaRankingTipoVaraListBean>();
	private Long totalProcessosEstado;
	private Integer totalVarasEstado;

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	@Column(name = "ds_competencia", length = 200)
	public String getCompetencia() {
		return competencia;
	}

	public void setCompetencia(String competencia) {
		this.competencia = competencia;
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

	public void setEstatisticaRankingTipoVaraListBean(
			List<EstatisticaRankingTipoVaraListBean> estatisticaRankingTipoVaraListBean) {
		this.estatisticaRankingTipoVaraListBean = estatisticaRankingTipoVaraListBean;
	}

	public List<EstatisticaRankingTipoVaraListBean> getEstatisticaRankingTipoVaraListBean() {
		return estatisticaRankingTipoVaraListBean;
	}
}