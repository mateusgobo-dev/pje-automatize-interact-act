package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * distribuídos arquivados
 * 
 * @author Eldson
 * 
 */
public class EstatisticaProcessosArquivadosBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3817413741331687373L;
	private String codEstado;
	private long totalArquivados;
	private List<EstatisticaProcessosArquivadosSubTableBean> estatisticaArquivadosBeanList = new ArrayList<EstatisticaProcessosArquivadosSubTableBean>();
	private Long[] totalMes = { 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L };

	public EstatisticaProcessosArquivadosBean() {
	}

	public EstatisticaProcessosArquivadosBean(String codEstado) {
		this.codEstado = codEstado;
	}

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public List<EstatisticaProcessosArquivadosSubTableBean> getEstatisticaArquivadosBeanList() {
		return estatisticaArquivadosBeanList;
	}

	public void setEstatisticaArquivadosBeanList(
			List<EstatisticaProcessosArquivadosSubTableBean> estatisticaArquivadosBeanList) {
		this.estatisticaArquivadosBeanList = estatisticaArquivadosBeanList;
		recalcularTotalArquivados();
	}

	private void recalcularTotalArquivados() {
		totalArquivados = 0;
		for (EstatisticaProcessosArquivadosSubTableBean bean : estatisticaArquivadosBeanList) {
			totalArquivados += bean.getQtdVaras();
		}
	}

	public void setTotalArquivados(long totalArquivados) {
		this.totalArquivados = totalArquivados;
	}

	public long getTotalArquivados() {
		return totalArquivados;
	}

	public Long[] getTotalMes() {
		return totalMes;
	}

	public void setTotalMes(Long[] totalMes) {
		this.totalMes = totalMes;
	}

}