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
public class EstatisticaProcessosDistribuidosBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1567753760194056890L;
	private String codEstado;
	private long totalDistribuidos;
	private List<EstatisticaDistribuidosListBean> estatisticaDistribuidosBeanList = new ArrayList<EstatisticaDistribuidosListBean>();
	private Long[] totalMes = { 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L };

	public EstatisticaProcessosDistribuidosBean() {
	}

	public EstatisticaProcessosDistribuidosBean(String codEstado) {
		this.codEstado = codEstado;
	}

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public List<EstatisticaDistribuidosListBean> getEstatisticaDistribuidosListBean() {
		return estatisticaDistribuidosBeanList;
	}

	public void setEstatisticaDistribuidosListBean(List<EstatisticaDistribuidosListBean> estatisticaDistribuidosListBean) {
		this.estatisticaDistribuidosBeanList = estatisticaDistribuidosListBean;
		recalcularTotalDistribuidos();
	}

	private void recalcularTotalDistribuidos() {
		totalDistribuidos = 0;
		for (EstatisticaDistribuidosListBean bean : estatisticaDistribuidosBeanList) {
			totalDistribuidos += bean.getQtdVaras();
		}
	}

	public void setTotalDistribuidos(long totalDistribuidos) {
		this.totalDistribuidos = totalDistribuidos;
	}

	public long getTotalDistribuidos() {
		return totalDistribuidos;
	}

	public Long[] getTotalMes() {
		return totalMes;
	}

	public void setTotalMes(Long[] totalMes) {
		this.totalMes = totalMes;
	}
}