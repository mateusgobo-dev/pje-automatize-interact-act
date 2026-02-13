package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EstatisticaProcessosConclusosTipoConclusaoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2028822277986406211L;
	private String codEvento;
	private String dsEvento;
	private List<EstatisticaProcessosSubListaTipoConclusosBean> estatisticaProcessosSubListaTipoConclusosBean = new ArrayList<EstatisticaProcessosSubListaTipoConclusosBean>();

	public EstatisticaProcessosConclusosTipoConclusaoBean() {
	}

	public EstatisticaProcessosConclusosTipoConclusaoBean(String codEvento) {
		this.codEvento = codEvento;
	}

	public void setCodEvento(String codEvento) {
		this.codEvento = codEvento;
	}

	public String getCodEvento() {
		return codEvento;
	}

	public String getDsEvento() {
		return dsEvento;
	}

	public void setDsEvento(String dsEvento) {
		this.dsEvento = dsEvento;
	}

	public int getQtdProcessos() {
		return getEstatisticaProcessosSubListaTipoConclusosBean().size();
	}

	@Override
	public String toString() {
		return this.codEvento;
	}

	public int getRowspan() {
		return getEstatisticaProcessosSubListaTipoConclusosBean().size();
	}

	public List<EstatisticaProcessosSubListaTipoConclusosBean> getEstatisticaProcessosSubListaTipoConclusosBean() {
		return estatisticaProcessosSubListaTipoConclusosBean;
	}

	public void setEstatisticaProcessosSubListaTipoConclusosBean(
			List<EstatisticaProcessosSubListaTipoConclusosBean> estatisticaProcessosSubListaTipoConclusosBean) {
		this.estatisticaProcessosSubListaTipoConclusosBean = estatisticaProcessosSubListaTipoConclusosBean;
	}
}