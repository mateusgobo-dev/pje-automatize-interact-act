package br.com.infox.pje.bean;

import java.util.Date;

public class EstatisticaProcessosSemConclusaoBean {

	private String numeroProcesso;
	private String ultimaFase;
	private Date dataUltimaFase;
	private long qntDias;

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setUltimaFase(String ultimaFase) {
		this.ultimaFase = ultimaFase;
	}

	public String getUltimaFase() {
		return ultimaFase;
	}

	public void setDataUltimaFase(Date dataUltimaFase) {
		this.dataUltimaFase = dataUltimaFase;
	}

	public Date getDataUltimaFase() {
		return dataUltimaFase;
	}

	public void setQntDias(long qntDias) {
		this.qntDias = qntDias;
	}

	public long getQntDias() {
		return qntDias;
	}

}