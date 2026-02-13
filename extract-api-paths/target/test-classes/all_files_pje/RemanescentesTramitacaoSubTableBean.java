package br.com.infox.pje.bean;

import java.io.Serializable;

/**
 * 
 * @author Daniel
 * 
 */
public class RemanescentesTramitacaoSubTableBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3062905457173690180L;
	private String vara;
	private Long distribuidos;
	private Long julgados;
	private Long arquivados;
	private Long tramitacao;
	private Long remanescentes;

	public String getVara() {
		return vara;
	}

	public void setVara(String vara) {
		this.vara = vara;
	}

	public Long getDistribuidos() {
		return distribuidos;
	}

	public void setDistribuidos(Long distribuidos) {
		this.distribuidos = distribuidos;
	}

	public Long getJulgados() {
		return julgados;
	}

	public void setJulgados(Long julgados) {
		this.julgados = julgados;
	}

	public Long getArquivados() {
		return arquivados;
	}

	public void setArquivados(Long arquivados) {
		this.arquivados = arquivados;
	}

	public void setTramitacao(Long tramitacao) {
		this.tramitacao = tramitacao;
	}

	public Long getTramitacao() {
		return tramitacao;
	}

	public void setRemanescentes(Long remanescentes) {
		this.remanescentes = remanescentes;
	}

	public Long getRemanescentes() {
		return remanescentes;
	}

}