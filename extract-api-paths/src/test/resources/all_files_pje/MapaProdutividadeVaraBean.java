package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.Map;

public class MapaProdutividadeVaraBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7763875568604519290L;
	private String vara;
	private Map<Integer, Long> qtPorMes;
	private double media;

	public void setVara(String vara) {
		this.vara = vara;
	}

	public String getVara() {
		return vara;
	}

	public void setMedia(double media) {
		this.media = media;
	}

	public double getMedia() {
		return media;
	}

	public void setQtPorMes(Map<Integer, Long> qtPorMes) {
		this.qtPorMes = qtPorMes;
		recalcularMedia();
	}

	private void recalcularMedia() {
		if (qtPorMes.size() == 0) {
			return;
		}

		int soma = 0;
		for (Long qtd : qtPorMes.values()) {
			soma += qtd;
		}
		media = soma / qtPorMes.size();
	}

	public Long getQuantidade(Integer mes) {
		return qtPorMes.containsKey(mes) ? qtPorMes.get(mes) : 0L;
	}

	public Map<Integer, Long> getQtPorMes() {
		return qtPorMes;
	}
}