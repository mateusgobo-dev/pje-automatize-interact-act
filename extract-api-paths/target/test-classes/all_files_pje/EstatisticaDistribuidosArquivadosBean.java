package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * distribuídos arquivados
 * 
 * @author Wilson
 * 
 */
public class EstatisticaDistribuidosArquivadosBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4241550725201936061L;
	private String codEstado;
	private List<EstatisticaDistribuidosArquivadosListBean> distribuidosArquivadosListBean = new ArrayList<EstatisticaDistribuidosArquivadosListBean>();
	private Long totalVarasEstados;
	private Long totalProcDistribuidos;
	private Long totalProcArquivados;
	private double somaPercDistribuidosArquivados;
	private double percTotalDistribuidosArquivados;

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public List<EstatisticaDistribuidosArquivadosListBean> getDistribuidosArquivadosListBean() {
		return distribuidosArquivadosListBean;
	}

	public void setDistribuidosArquivadosListBean(
			List<EstatisticaDistribuidosArquivadosListBean> distribuidosArquivadosListBean) {
		this.distribuidosArquivadosListBean = distribuidosArquivadosListBean;
	}

	public Long getTotalVarasEstados() {
		return totalVarasEstados;
	}

	public void setTotalVarasEstados(Long totalVarasEstados) {
		this.totalVarasEstados = totalVarasEstados;
	}

	public Long getTotalProcDistribuidos() {
		return totalProcDistribuidos;
	}

	public void setTotalProcDistribuidos(Long totalProcDistribuidos) {
		this.totalProcDistribuidos = totalProcDistribuidos;
	}

	public Long getTotalProcArquivados() {
		return totalProcArquivados;
	}

	public void setTotalProcArquivados(Long totalProcArquivados) {
		this.totalProcArquivados = totalProcArquivados;
	}

	public double getSomaPercDistribuidosArquivados() {
		return somaPercDistribuidosArquivados;
	}

	public void setSomaPercDistribuidosArquivados(double somaPercDistribuidosArquivados) {
		this.somaPercDistribuidosArquivados = somaPercDistribuidosArquivados;
	}

	public double getPercTotalDistribuidosArquivados() {
		Double totalPercentual = (totalProcArquivados * 100) / totalProcDistribuidos.doubleValue();
		if (totalPercentual.equals(Double.NaN)) {
			return 0;
		} else {
			return totalPercentual;
		}
	}

	public void setPercTotalDistribuidosArquivados(double percTotalDistribuidosArquivados) {
		this.percTotalDistribuidosArquivados = percTotalDistribuidosArquivados;
	}
}