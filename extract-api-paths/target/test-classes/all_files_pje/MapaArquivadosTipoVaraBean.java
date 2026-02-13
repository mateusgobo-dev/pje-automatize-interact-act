package br.com.infox.pje.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean para exibição da listagem do relatório na estatística de processos
 * distribuídos e arquivados
 * 
 * @author Wilson
 * 
 */
public class MapaArquivadosTipoVaraBean {

	private String codEstado;
	private List<EstatisticaProcessosArquivadosSubTableBean> subList = new ArrayList<EstatisticaProcessosArquivadosSubTableBean>();
	private String totalGeral;
	private Long[] totalMes = { 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L };

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public void setSubList(List<EstatisticaProcessosArquivadosSubTableBean> subList) {
		this.subList = subList;
	}

	public List<EstatisticaProcessosArquivadosSubTableBean> getSubList() {
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