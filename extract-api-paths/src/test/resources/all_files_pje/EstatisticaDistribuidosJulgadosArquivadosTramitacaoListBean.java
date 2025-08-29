package br.com.infox.pje.bean;

import java.io.Serializable;

/**
 * Bean para exibição da listagem do relatório na estatística de processos
 * distribuídos e arquivados
 * 
 * @author Geldo
 * 
 */
public class EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8404700183876551584L;
	private String varas;
	private double qtdProcessosDistribuidos;
	private double qtdProcessosJulgados;
	private double qtdProcessosArquivados;
	private double qtdProcessosTramitacao;

	public String getVaras() {
		return varas;
	}

	public void setVaras(String varas) {
		this.varas = varas;
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

	public void setQtdProcessosJulgados(double qtdProcessosJulgados) {
		this.qtdProcessosJulgados = qtdProcessosJulgados;
	}

	public double getQtdProcessosJulgados() {
		return qtdProcessosJulgados;
	}

	public void setQtdProcessosTramitacao(double qtdProcessosTramitacao) {
		this.qtdProcessosTramitacao = qtdProcessosTramitacao;
	}

	public double getQtdProcessosTramitacao() {
		return qtdProcessosTramitacao;
	}
}