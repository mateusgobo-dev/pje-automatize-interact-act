package br.com.infox.pje.bean;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.ClasseJudicial;

/**
 * 
 * @author Rafael
 * 
 */
public class EstatisticaJFProcessosDistribuidosClasses implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7660439562731986106L;
	private ClasseJudicial classe;
	private int totalClasse;
	private int totalClasseAjustado;
	private int totalEventoRem;
	private int totalEventoDistr;
	private int totalEventoDevolv;
	private int totalEventoReativ;
	private int totalEventoMudClassRee;
	private int totalEventoMudClassBaixa;
	private int totalEventoBaixad;
	private int totalEventoRedistrib;
	private int totalEventoRemet;
	private int totalEventoSusp;
	private int totalEventoArq;

	public ClasseJudicial getClasse() {
		return classe;
	}

	public void setClasse(ClasseJudicial classe) {
		this.classe = classe;
	}

	public int getTotalClasse() {
		return totalClasse;
	}

	public void setTotalClasse(int totalClasse) {
		this.totalClasse = totalClasse;
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

	public int getTotalEventoMudClassRee() {
		return totalEventoMudClassRee;
	}

	public void setTotalEventoMudClassRee(int totalEventoMudClassRee) {
		this.totalEventoMudClassRee = totalEventoMudClassRee;
	}

	public int getTotalEventoMudClassBaixa() {
		return totalEventoMudClassBaixa;
	}

	public void setTotalEventoMudClassBaixa(int totalEventoMudClassBaixa) {
		this.totalEventoMudClassBaixa = totalEventoMudClassBaixa;
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

	public int getTotalEventoSusp() {
		return totalEventoSusp;
	}

	public void setTotalEventoSusp(int totalEventoSusp) {
		this.totalEventoSusp = totalEventoSusp;
	}

	public int getTotalEventoArq() {
		return totalEventoArq;
	}

	public void setTotalEventoArq(int totalEventoArq) {
		this.totalEventoArq = totalEventoArq;
	}

	public int getTotalClasseAjustado() {
		return totalClasseAjustado;
	}

	public void setTotalClasseAjustado(int totalClasseAjustado) {
		this.totalClasseAjustado = totalClasseAjustado;
	}

}