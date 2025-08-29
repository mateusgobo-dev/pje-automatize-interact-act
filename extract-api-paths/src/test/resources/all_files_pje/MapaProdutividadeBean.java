package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MapaProdutividadeBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1554578663099868037L;
	private String codEstado;
	private List<MapaProdutividadeTipoVaraBean> mapaProdutividadeTipoVaraBeanList = new ArrayList<MapaProdutividadeTipoVaraBean>();

	public MapaProdutividadeBean() {
	}

	public MapaProdutividadeBean(String codEstado) {
		this.codEstado = codEstado;
	}

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public void setMapaProdutividadeTipoVaraBeanList(
			List<MapaProdutividadeTipoVaraBean> mapaProdutividadeTipoVaraBeanList) {
		this.mapaProdutividadeTipoVaraBeanList = mapaProdutividadeTipoVaraBeanList;
	}

	public List<MapaProdutividadeTipoVaraBean> getMapaProdutividadeTipoVaraBeanList() {
		return mapaProdutividadeTipoVaraBeanList;
	}

	/**
	 * Retorna o somatório de linhas correspondentes aos registros do tipo de
	 * vara mais uma linha reservada para a média geral de cada tipo de vara.
	 * 
	 * @return rowspan para o registro do MapaProdutividadeBean
	 */
	public int getRowspan() {
		int rowspan = 1;
		for (MapaProdutividadeTipoVaraBean bean : mapaProdutividadeTipoVaraBeanList) {
			rowspan += bean.getRowspan() + 2;
		}
		return rowspan;
	}

	public int getRowspanPdf() {
		int rowspan = 1;
		for (MapaProdutividadeTipoVaraBean bean : mapaProdutividadeTipoVaraBeanList) {
			rowspan += bean.getMapaProdutividadeVaraBeanList().size();
		}
		rowspan += mapaProdutividadeTipoVaraBeanList.size() + mapaProdutividadeTipoVaraBeanList.size();
		return rowspan;
	}
}