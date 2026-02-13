package br.com.infox.ibpm.bean;

import java.io.Serializable;
import java.util.Date;

public class ConsultaProcesso implements Serializable {

	private static final long serialVersionUID = 1L;

	private String numeroProcesso;
	private Date dataInicio;
	private Date dataFim;
	private Boolean inPesquisa = false;
	private String fluxo;

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getFluxo() {
		return fluxo;
	}

	public void setFluxo(String fluxo) {
		this.fluxo = fluxo;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = br.jus.pje.nucleo.util.DateUtil.getEndOfDay(dataFim);
	}

	public Boolean getInPesquisa() {
		return inPesquisa;
	}

	public void setInPesquisa(Boolean inPesquisa) {
		this.inPesquisa = inPesquisa;
	}

	@Override
	public String toString() {
		return numeroProcesso;
	}

}