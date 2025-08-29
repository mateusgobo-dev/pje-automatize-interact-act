package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Rafael
 * 
 */
public class EstatisticaProcessosTramitacaoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1589126079202907567L;
	private String codEstado;
	private List<EstatisticaProcessosTramitacaoSubTableBean> subList = new ArrayList<EstatisticaProcessosTramitacaoSubTableBean>();
	private String totalGeral;
	private Long[] totalMes = { 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L };

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public void setSubList(List<EstatisticaProcessosTramitacaoSubTableBean> subList) {
		this.subList = subList;
	}

	public List<EstatisticaProcessosTramitacaoSubTableBean> getSubList() {
		return subList;
	}

	public String getTotalGeral() {
		return totalGeral;
	}

	public void setTotalGeral(String totalGeral) {
		this.totalGeral = totalGeral;
	}

	public void setTotalMes(Long[] totalMes) {
		this.totalMes = totalMes;
	}

	public Long[] getTotalMes() {
		return totalMes;
	}
}