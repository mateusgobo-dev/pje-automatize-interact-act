package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.jus.pje.nucleo.enums.EstadosBrasileirosEnum;

/**
 * Bean para exibição da listagem do relatório na estatística
 * 
 * @author Allan
 * 
 */
public class EstatisticaProcessosTramitacaoSecaoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5998328597557961889L;
	private String codEstado;
	private List<EstatisticaProcessosTramitacaoSecaoListBean> estatisticaProcessosTramitacaoSecaoListBean = new ArrayList<EstatisticaProcessosTramitacaoSecaoListBean>();

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public void setEstatisticaProcessosTramitacaoSecaoListBean(
			List<EstatisticaProcessosTramitacaoSecaoListBean> estatisticaProcessosTramitacaoSecaoListBean) {
		this.estatisticaProcessosTramitacaoSecaoListBean = estatisticaProcessosTramitacaoSecaoListBean;
	}

	public List<EstatisticaProcessosTramitacaoSecaoListBean> getEstatisticaProcessosTramitacaoSecaoListBean() {
		return estatisticaProcessosTramitacaoSecaoListBean;
	}

	public int getRowspan() {
		int rowspan = 1;
		for (EstatisticaProcessosTramitacaoSecaoListBean bean : estatisticaProcessosTramitacaoSecaoListBean) {
			rowspan += bean.getRowspan() + 2;
		}
		return rowspan;
	}

	public int getRowspanPDF() {
		int rowspanPDF = 1;
		for (EstatisticaProcessosTramitacaoSecaoListBean bean : estatisticaProcessosTramitacaoSecaoListBean) {
			rowspanPDF += bean.getEstatisticaProcTramitacaoListBean().size() + 2;
		}
		return rowspanPDF;
	}

	public int getTotal() {
		int total = 0;
		for (EstatisticaProcessosTramitacaoSecaoListBean bean : estatisticaProcessosTramitacaoSecaoListBean) {
			total += bean.getTotal();
		}
		return total;
	}

	public int getTotalJulgadosAnaliticos() {
		int totalJulgados = 0;
		for (EstatisticaProcessosTramitacaoSecaoListBean bean : estatisticaProcessosTramitacaoSecaoListBean) {
			totalJulgados += bean.getTotalJulgados();
		}
		return totalJulgados;
	}

	public int getTotalJulgados() {
		int totalJulgados = 0;
		for (EstatisticaProcessosTramitacaoSecaoListBean bean : estatisticaProcessosTramitacaoSecaoListBean) {
			totalJulgados += bean.getJulgados();
		}
		return totalJulgados;
	}

	public String getLabelEstadoBrasileiro() {
		return EstadosBrasileirosEnum.valueOf(codEstado).getLabel();
	}

}