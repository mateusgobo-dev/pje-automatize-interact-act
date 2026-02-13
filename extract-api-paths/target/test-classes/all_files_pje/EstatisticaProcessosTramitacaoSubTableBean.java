package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * @author Rafael
 * 
 */
public class EstatisticaProcessosTramitacaoSubTableBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5110433473302855924L;
	private String vara;
	private String totalVara;
	private Map<Integer, Long> qtPorMes;
	private String jurisdicao;

	public String getVara() {
		return vara;
	}

	public void setVara(String vara) {
		this.vara = vara;
	}

	public String getTotalVara() {
		return totalVara;
	}

	public void setTotalVara(String totalVara) {
		this.totalVara = totalVara;
	}

	public String getQuantidadePorMes(Integer i) {
		return "";
	}

	public Map<Integer, Long> getQtPorMes() {
		return qtPorMes;
	}

	public void setQtPorMes(Map<Integer, Long> qtPorMes) {
		this.qtPorMes = qtPorMes;
	}

	public Long getQuantidade(Integer mes) {
		return qtPorMes.containsKey(mes) ? qtPorMes.get(mes) : 0L;
	}

	public String getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(String jurisdicao) {
		this.jurisdicao = jurisdicao;
	}
}