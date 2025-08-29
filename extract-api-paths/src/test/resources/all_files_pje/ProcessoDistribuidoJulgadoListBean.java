package br.com.infox.pje.bean;

import java.io.Serializable;

/**
 * Bean para exibição da listagem do relatório de procesos distribuídos e
 * julgados
 * 
 * @author thiago
 * 
 */
public class ProcessoDistribuidoJulgadoListBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2275523764320698152L;
	private String vara;
	private Double percentProcDistribuidosJulgados;
	private double qtdProcDistribuidos;
	private double qtdProcJulgados;

	public void setVara(String vara) {
		this.vara = vara;
	}

	public String getVara() {
		return vara;
	}

	public Double getPercentProcDistribuidosJulgados() {
		return percentProcDistribuidosJulgados;
	}

	public void setPercentProcDistribuidosJulgados(Double percentProcDistribuidosJulgados) {
		this.percentProcDistribuidosJulgados = percentProcDistribuidosJulgados;
	}

	public double getQtdProcDistribuidos() {
		return qtdProcDistribuidos;
	}

	public void setQtdProcDistribuidos(double qtdProcDistribuidos) {
		this.qtdProcDistribuidos = qtdProcDistribuidos;
	}

	public double getQtdProcJulgados() {
		return qtdProcJulgados;
	}

	public void setQtdProcJulgados(double qtdProcJulgados) {
		this.qtdProcJulgados = qtdProcJulgados;
	}

}