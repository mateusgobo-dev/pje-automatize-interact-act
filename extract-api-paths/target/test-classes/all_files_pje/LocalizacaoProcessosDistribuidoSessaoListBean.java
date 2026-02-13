package br.com.infox.pje.bean;

import java.util.List;

/**
 * 
 * @author Edson
 * 
 */
public class LocalizacaoProcessosDistribuidoSessaoListBean {
	private String orgaoJulgador;
	private List<ProcessosDistribuidoSessaoBean> listaProcessos;
	private long qtdProcVara = (long) 0.0;

	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(String orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public List<ProcessosDistribuidoSessaoBean> getListaProcessos() {
		return listaProcessos;
	}

	public void setListaProcessos(List<ProcessosDistribuidoSessaoBean> listaProcessos) {
		this.listaProcessos = listaProcessos;
	}

	public long getQtdProcVara() {
		qtdProcVara = (long) 0.0;
		if (null != listaProcessos) {
			qtdProcVara = listaProcessos.size();
		}
		return qtdProcVara;
	}

	public void setQtdProcVara(long qtdProcVara) {
		this.qtdProcVara = qtdProcVara;
	}

}