package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MapaProdutividadeTipoVaraBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4277206469529444630L;

	private String competencia;

	private List<MapaProdutividadeVaraBean> mapaProdutividadeVaraBeanList = new ArrayList<MapaProdutividadeVaraBean>();
	private Double media;

	public String getCompetencia() {
		return competencia;
	}

	public void setCompetencia(String competencia) {
		this.competencia = competencia;
	}

	public Double getMedia() {
		return media;
	}

	public void setMapaProdutividadeVaraBeanList(List<MapaProdutividadeVaraBean> mapaProdutividadeVaraBeanList) {
		this.mapaProdutividadeVaraBeanList = mapaProdutividadeVaraBeanList;
		recalcularMedia();
	}

	public List<MapaProdutividadeVaraBean> getMapaProdutividadeVaraBeanList() {
		return mapaProdutividadeVaraBeanList;
	}

	private void recalcularMedia() {
		double soma = 0;
		for (MapaProdutividadeVaraBean bean : mapaProdutividadeVaraBeanList) {
			soma += bean.getMedia();
		}
		media = soma / mapaProdutividadeVaraBeanList.size();
	}

	public int getRowspan() {
		return mapaProdutividadeVaraBeanList.size() + 1;
	}
}