package br.com.infox.pje.bean;

import java.io.Serializable;

/**
 * 
 * @author Rafael
 * 
 */
public class EstatisticaJFProcessosDistribuidosEntidades implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4481551696126740766L;
	private String entidade;
	private int totalEntidade;
	private int totalEventoRem;
	private int totalEventoDistr;
	private int totalEventoDevolv;
	private int totalEventoReativ;
	private int totalEventoBaixad;
	private int totalEventoRedistrib;
	private int totalEventoRemet;

	public String getEntidade() {
		return entidade;
	}

	public void setEntidade(String entidade) {
		this.entidade = entidade;
	}

	public int getTotalEntidade() {
		return totalEntidade;
	}

	public void setTotalEntidade(int totalEntidade) {
		this.totalEntidade = totalEntidade;
	}

	public void setTotalEventoRem(int totalEventoRem) {
		this.totalEventoRem = totalEventoRem;
	}

	public int getTotalEventoRem() {
		return totalEventoRem;
	}

	public int getTotalEventoDistr() {
		return totalEventoDistr;
	}

	public void setTotalEventoDistr(int totalEventoDistr) {
		this.totalEventoDistr = totalEventoDistr;
	}

	public int getTotalEventoDevolv() {
		return totalEventoDevolv;
	}

	public void setTotalEventoDevolv(int totalEventoDevolv) {
		this.totalEventoDevolv = totalEventoDevolv;
	}

	public int getTotalEventoReativ() {
		return totalEventoReativ;
	}

	public void setTotalEventoReativ(int totalEventoReativ) {
		this.totalEventoReativ = totalEventoReativ;
	}

	public int getTotalEventoBaixad() {
		return totalEventoBaixad;
	}

	public void setTotalEventoBaixad(int totalEventoBaixad) {
		this.totalEventoBaixad = totalEventoBaixad;
	}

	public int getTotalEventoRedistrib() {
		return totalEventoRedistrib;
	}

	public void setTotalEventoRedistrib(int totalEventoRedistrib) {
		this.totalEventoRedistrib = totalEventoRedistrib;
	}

	public int getTotalEventoRemet() {
		return totalEventoRemet;
	}

	public void setTotalEventoRemet(int totalEventoRemet) {
		this.totalEventoRemet = totalEventoRemet;
	}

}