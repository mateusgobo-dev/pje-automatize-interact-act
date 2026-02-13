package br.com.infox.pje.bean;

import java.util.List;

import br.jus.pje.nucleo.enums.EstadosBrasileirosEnum;

/**
 * 
 * @author Edson
 * 
 */
public class SecaoLocalizacaoProcessosDistribuidoSessaoListBean {
	private String codEstado;
	private List<LocalizacaoProcessosDistribuidoSessaoListBean> listaSecaoLocalizacao;
	private long qtdProcSecao = (long) 0.0;

	public long getQtdProcSecao() {
		qtdProcSecao = (long) 0.0;
		if (null != listaSecaoLocalizacao) {
			// soma o total de processos das varas da seção
			for (LocalizacaoProcessosDistribuidoSessaoListBean lpdsb : listaSecaoLocalizacao) {
				qtdProcSecao += lpdsb.getQtdProcVara();
			}
		}
		return qtdProcSecao;
	}

	public void setQtdProcSecao(long qtdProcSecao) {
		this.qtdProcSecao = qtdProcSecao;
	}

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public List<LocalizacaoProcessosDistribuidoSessaoListBean> getListaSecaoLocalizacao() {
		return listaSecaoLocalizacao;
	}

	public void setListaSecaoLocalizacao(List<LocalizacaoProcessosDistribuidoSessaoListBean> listaSecaoLocalizacao) {
		this.listaSecaoLocalizacao = listaSecaoLocalizacao;
	}

	public String getLabelEstadoBrasileiro() {
		return EstadosBrasileirosEnum.valueOf(codEstado).getLabel();
	}

}