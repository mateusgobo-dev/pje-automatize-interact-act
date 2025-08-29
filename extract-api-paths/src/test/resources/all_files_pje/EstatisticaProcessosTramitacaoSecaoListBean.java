package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * produtividade secao judiciaria
 * 
 * @author Allan
 * 
 */
public class EstatisticaProcessosTramitacaoSecaoListBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5951692845511807743L;
	private String localizacao;
	private int total;
	private List<EstatisticaProcTramitacaoSecaoListBean> estatisticaProcTramitacaoListBean = new ArrayList<EstatisticaProcTramitacaoSecaoListBean>();
	private long julgados;

	public void setLocalizacao(String localizacao) {
		this.localizacao = localizacao;
	}

	public String getLocalizacao() {
		return localizacao;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getTotal() {
		return total;
	}

	public void setEstatisticaProcTramitacaoListBean(
			List<EstatisticaProcTramitacaoSecaoListBean> estatisticaProcTramitacaoListBean) {
		this.estatisticaProcTramitacaoListBean = estatisticaProcTramitacaoListBean;
	}

	public List<EstatisticaProcTramitacaoSecaoListBean> getEstatisticaProcTramitacaoListBean() {
		return estatisticaProcTramitacaoListBean;
	}

	public int getRowspan() {
		return estatisticaProcTramitacaoListBean.size() + 1;
	}

	public void setJulgados(long julgados) {
		this.julgados = julgados;
	}

	public long getJulgados() {
		return julgados;
	}

	public long getTotalJulgados() {
		long tj = 0;
		for (EstatisticaProcTramitacaoSecaoListBean o : estatisticaProcTramitacaoListBean) {
			tj += o.getTotalJulgados();
		}
		return tj;
	}

}