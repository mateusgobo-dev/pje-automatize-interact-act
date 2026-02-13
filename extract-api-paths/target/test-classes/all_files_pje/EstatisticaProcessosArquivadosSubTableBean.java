package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * @author Eldson
 * 
 */
public class EstatisticaProcessosArquivadosSubTableBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -13120107832858804L;
	private long qtdVaras;
	private String vara;
	private Map<Integer, Long> qtPorMes;
	private String competencias;

	public long getQtdVaras() {
		return qtdVaras;
	}

	public void setQtdVaras(long qtdVaras) {
		this.qtdVaras = qtdVaras;
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
		qtdVaras = soma;
	}

	public Map<Integer, Long> getQtPorMes() {
		return qtPorMes;
	}

	public void setQtPorMes(Map<Integer, Long> qtPorMes) {
		this.qtPorMes = qtPorMes;
		recalcularQuantidade();
	}

	public String getCompetencias() {
		return competencias;
	}

	public void setCompetencias(String competencias) {
		this.competencias = competencias;
	}

	public Long getQuantidade(Integer mes) {
		return qtPorMes.containsKey(mes) ? qtPorMes.get(mes) : 0L;
	}
}
