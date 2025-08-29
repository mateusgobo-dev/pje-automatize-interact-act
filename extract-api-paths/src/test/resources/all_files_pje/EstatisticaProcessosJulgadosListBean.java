package br.com.infox.pje.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * julgados
 * 
 * @author Luiz Carlos Menezes
 * 
 */
public class EstatisticaProcessosJulgadosListBean {

	private String varas;
	private String qntProcessos;
	private Map<Integer, Long> qtdProcessosMes = new HashMap<Integer, Long>();

	public void setVaras(String varas) {
		this.varas = varas;
	}

	public String getVaras() {
		return varas;
	}

	public void setQntProcessos(String qntProcessos) {
		this.qntProcessos = qntProcessos;
	}

	public String getQntProcessos() {
		return qntProcessos;
	}

	public void setQtdProcessosMes(Map<Integer, Long> qtdProcessosMes) {
		this.qtdProcessosMes = qtdProcessosMes;
	}

	public Map<Integer, Long> getQtdProcessosMes() {
		return qtdProcessosMes;
	}

	public Long getQuantidade(Integer mes) {
		return qtdProcessosMes.containsKey(mes) ? qtdProcessosMes.get(mes) : 0L;
	}

}