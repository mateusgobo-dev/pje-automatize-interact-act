package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean para exibição da listagem do relatório na estatística de mapa de
 * distribuicao por varas
 * 
 * @author Rafael Fernandes
 * 
 */
public class EstatisticaMapaDistribuicaoVarasBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5917358246563526049L;
	private String codEstado;
	private Integer totalVarasEstado;
	private List<MapaDistribuicaoVarasListBean> mapaDistribuicaoVarasListBean = new ArrayList<MapaDistribuicaoVarasListBean>();

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public void setMapaDistribuicaoVarasListBean(List<MapaDistribuicaoVarasListBean> mapaDistribuicaoVarasListBean) {
		this.mapaDistribuicaoVarasListBean = mapaDistribuicaoVarasListBean;
	}

	public List<MapaDistribuicaoVarasListBean> getMapaDistribuicaoVarasListBean() {
		return mapaDistribuicaoVarasListBean;
	}

	public void setTotalVarasEstado(Integer totalVarasEstado) {
		this.totalVarasEstado = totalVarasEstado;
	}

	public Integer getTotalVarasEstado() {
		return totalVarasEstado;
	}
}