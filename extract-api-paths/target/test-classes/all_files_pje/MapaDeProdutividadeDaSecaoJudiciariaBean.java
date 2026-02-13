package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Eldson
 * 
 */
public class MapaDeProdutividadeDaSecaoJudiciariaBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8440933553215489574L;
	private String codEstado;
	private String competencia;
	private List<MapaDeProdutividadeDaSecaoJudiciariaListBean> mapaDeProdutividadeDaSecaoJudiciariaListBean = new ArrayList<MapaDeProdutividadeDaSecaoJudiciariaListBean>();
	private double totalProcessosEstado;
	private int qtdMeses;
	private int qtdCompetencias;
	private double mediaPorCompetencia;

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public String getCompetencia() {
		return competencia;
	}

	public void setCompetencia(String competencia) {
		this.competencia = competencia;
	}

	public void setTotalProcessosEstado(double totalProcessosEstado) {
		this.totalProcessosEstado = totalProcessosEstado;
	}

	public double getTotalProcessosEstado() {
		return totalProcessosEstado;
	}

	public void setMapaDeProdutividadeDaSecaoJudiciariaListBean(
			List<MapaDeProdutividadeDaSecaoJudiciariaListBean> mapaDeProdutividadeDaSecaoJudiciariaListBean) {
		this.mapaDeProdutividadeDaSecaoJudiciariaListBean = mapaDeProdutividadeDaSecaoJudiciariaListBean;
	}

	public List<MapaDeProdutividadeDaSecaoJudiciariaListBean> getMapaDeProdutividadeDaSecaoJudiciariaListBean() {
		return mapaDeProdutividadeDaSecaoJudiciariaListBean;
	}

	public void setQtdMeses(int qtdMeses) {
		this.qtdMeses = qtdMeses;
	}

	public int getQtdMeses() {
		return qtdMeses;
	}

	public void setQtdCompetencias(int qtdCompetencias) {
		this.qtdCompetencias = qtdCompetencias;
	}

	public int getQtdCompetencias() {
		return qtdCompetencias;
	}

	public void setMediaPorCompetencia(double mediaPorCompetencia) {
		this.mediaPorCompetencia = mediaPorCompetencia;
	}

	public double getMediaPorCompetencia() {
		return mediaPorCompetencia;
	}

}