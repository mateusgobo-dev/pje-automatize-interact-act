package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * distribuídos julgados
 * 
 * @author Wilson
 * 
 */
public class EstatisticaProcessosJulgadosBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4350530916633187621L;
	private String codEstado;
	private long totalJulgados;
	private List<EstatisticaProcessosJulgadosSubTableBean> subList = new ArrayList<EstatisticaProcessosJulgadosSubTableBean>();
	private Long[] totalMes = { 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L };

	public EstatisticaProcessosJulgadosBean() {
	}

	public EstatisticaProcessosJulgadosBean(String codEstado) {
		this.codEstado = codEstado;
	}

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public List<EstatisticaProcessosJulgadosSubTableBean> getSubList() {
		return subList;
	}

	public void setSubList(List<EstatisticaProcessosJulgadosSubTableBean> subList) {
		this.subList = subList;
		recalcularTotalJulgados();
	}

	private void recalcularTotalJulgados() {
		totalJulgados = 0;
		for (EstatisticaProcessosJulgadosSubTableBean bean : subList) {
			totalJulgados += bean.getTotalVara();
		}
	}

	public long getTotalJulgados() {
		return totalJulgados;
	}

	public void setTotalJulgados(long totalJulgados) {
		this.totalJulgados = totalJulgados;
	}

	public Long[] getTotalMes() {
		return totalMes;
	}

	public void setTotalMes(Long[] totalMes) {
		this.totalMes = totalMes;
	}
}