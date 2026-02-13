package br.com.infox.pje.bean;

import java.io.Serializable;

/**
 * Bean para exibição da listagem do relatório na estatística de processos
 * distribuídos e arquivados
 * 
 * @author Wilson
 * 
 */
public class EstatisticaDistribuidosArquivadosListBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5281470241306938093L;
	private String varas;
	private double percenteProcDistribuidosArquivados;
	private double qtdProcessosDistribuidos;
	private double qtdProcessosArquivados;

	public String getVaras() {
		return varas;
	}

	public void setVaras(String varas) {
		this.varas = varas;
	}

	public double getPercenteProcDistribuidosArquivados() {
		return percenteProcDistribuidosArquivados;
	}

	public void setPercenteProcDistribuidosArquivados(double percenteProcDistribuidosArquivados) {
		this.percenteProcDistribuidosArquivados = percenteProcDistribuidosArquivados;
	}

	public double getQtdProcessosDistribuidos() {
		return qtdProcessosDistribuidos;
	}

	public void setQtdProcessosDistribuidos(double qtdProcessosDistribuidos) {
		this.qtdProcessosDistribuidos = qtdProcessosDistribuidos;
	}

	public double getQtdProcessosArquivados() {
		return qtdProcessosArquivados;
	}

	public void setQtdProcessosArquivados(double qtdProcessosArquivados) {
		this.qtdProcessosArquivados = qtdProcessosArquivados;
	}

}