package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author Rafael
 * 
 */
public class EstatisticaJFProcessosDistribuidosVara implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6318809794069235001L;
	private String vara;
	private List<EstatisticaJFProcessosDistribuidosClasses> subListClasse;
	private List<EstatisticaJFProcessosDistribuidosEntidades> subListEntidade;
	private List<EstatisticaJFProcessosDistribuidosClasseEntidade> subList;
	private int totalRemGeral;
	private int totalDistrGeral;
	private int totalDevolvGeral;
	private int totalReativGeral;
	private int totalMudReeGeral;
	private int totalMudBaixaGeral;
	private int totalBaixadGeral;
	private int totalRedistrGeral;
	private int totalRemetGeral;
	private int totalSuspGeral;
	private int totalArqGeral;
	private int totalAjustGeral;

	public String getVara() {
		return vara;
	}

	public void setVara(String vara) {
		this.vara = vara;
	}

	public List<EstatisticaJFProcessosDistribuidosClasses> getSubListClasse() {
		return subListClasse;
	}

	public void setSubListClasse(List<EstatisticaJFProcessosDistribuidosClasses> subListClasse) {
		this.subListClasse = subListClasse;
	}

	public List<EstatisticaJFProcessosDistribuidosEntidades> getSubListEntidade() {
		return subListEntidade;
	}

	public void setSubListEntidade(List<EstatisticaJFProcessosDistribuidosEntidades> subListEntidade) {
		this.subListEntidade = subListEntidade;
	}

	public int getTotalRemGeral() {
		return totalRemGeral;
	}

	public void setTotalRemGeral(int totalRemGeral) {
		this.totalRemGeral = totalRemGeral;
	}

	public int getTotalDistrGeral() {
		return totalDistrGeral;
	}

	public void setTotalDistrGeral(int totalDistrGeral) {
		this.totalDistrGeral = totalDistrGeral;
	}

	public int getTotalDevolvGeral() {
		return totalDevolvGeral;
	}

	public void setTotalDevolvGeral(int totalDevolvGeral) {
		this.totalDevolvGeral = totalDevolvGeral;
	}

	public int getTotalReativGeral() {
		return totalReativGeral;
	}

	public void setTotalReativGeral(int totalReativGeral) {
		this.totalReativGeral = totalReativGeral;
	}

	public int getTotalMudReeGeral() {
		return totalMudReeGeral;
	}

	public void setTotalMudReeGeral(int totalMudReeGeral) {
		this.totalMudReeGeral = totalMudReeGeral;
	}

	public int getTotalMudBaixaGeral() {
		return totalMudBaixaGeral;
	}

	public void setTotalMudBaixaGeral(int totalMudBaixaGeral) {
		this.totalMudBaixaGeral = totalMudBaixaGeral;
	}

	public int getTotalBaixadGeral() {
		return totalBaixadGeral;
	}

	public void setTotalBaixadGeral(int totalBaixadGeral) {
		this.totalBaixadGeral = totalBaixadGeral;
	}

	public int getTotalRedistrGeral() {
		return totalRedistrGeral;
	}

	public void setTotalRedistrGeral(int totalRedistrGeral) {
		this.totalRedistrGeral = totalRedistrGeral;
	}

	public int getTotalRemetGeral() {
		return totalRemetGeral;
	}

	public void setTotalRemetGeral(int totalRemetGeral) {
		this.totalRemetGeral = totalRemetGeral;
	}

	public int getTotalSuspGeral() {
		return totalSuspGeral;
	}

	public void setTotalSuspGeral(int totalSuspGeral) {
		this.totalSuspGeral = totalSuspGeral;
	}

	public int getTotalArqGeral() {
		return totalArqGeral;
	}

	public void setTotalArqGeral(int totalArqGeral) {
		this.totalArqGeral = totalArqGeral;
	}

	public int getTotalAjustGeral() {
		return totalAjustGeral;
	}

	public void setTotalAjustGeral(int totalAjustGeral) {
		this.totalAjustGeral = totalAjustGeral;
	}

	public List<EstatisticaJFProcessosDistribuidosClasseEntidade> getSubList() {
		return subList;
	}

	public void setSubList(List<EstatisticaJFProcessosDistribuidosClasseEntidade> subList) {
		this.subList = subList;
	}

}