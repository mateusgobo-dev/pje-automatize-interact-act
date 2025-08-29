package br.com.infox.pje.bean;

import java.io.Serializable;

/**
 * 
 * @author Eldson
 * 
 */
public class MapaDeProdutividadeDaSecaoJudiciariaListBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 375747567572142264L;
	private double qtdProcessos;
	private String codEstado;

	public void setQtdProcessos(double qtdProcessos) {
		this.qtdProcessos = qtdProcessos;
	}

	public double getQtdProcessos() {
		return qtdProcessos;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public String getCodEstado() {
		return codEstado;
	}

}