package br.com.infox.pje.bean;

import java.util.Map;

/**
 * 
 * @author Eldson
 * 
 */
public class EstatisticaProcessosJulgadosSubTableBean {
	private long totalVara;
	private String vara;
	private Map<Integer, Long> qtPorMes;
	private String jurisdicao;

	public long getTotalVara() {
		return totalVara;
	}

	public void setTotalVara(long totalVara) {
		this.totalVara = totalVara;
	}

	public String getVara() {
		return vara;
	}

	public void setVara(String vara) {
		this.vara = vara;
	}

	private void recalcularQuantidade() {
		if (qtPorMes.size() == 0) {
			return;
		}

		int soma = 0;
		for (Long qtd : qtPorMes.values()) {
			soma += qtd;
		}
		totalVara = soma;
	}

	public Map<Integer, Long> getQtPorMes() {
		return qtPorMes;
	}

	public void setQtPorMes(Map<Integer, Long> qtPorMes) {
		this.qtPorMes = qtPorMes;
		recalcularQuantidade();
	}

	public String getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(String jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	public Long getQuantidade(Integer mes) {
		return qtPorMes.containsKey(mes) ? qtPorMes.get(mes) : 0L;
	}
}
