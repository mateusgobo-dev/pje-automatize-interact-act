package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Daniel
 * 
 */
public class RemanescentesTramitacaoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2553622130281241968L;
	private String codEstado;
	private List<RemanescentesTramitacaoSubTableBean> subList = new ArrayList<RemanescentesTramitacaoSubTableBean>();
	private Long totalDistribuidos = 0l;
	private Long totalJulgados = 0l;
	private Long totalArquivados = 0l;
	private Long totalTramitacao = 0l;
	private Long totalRemanescentes = 0l;

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public void setSubList(List<RemanescentesTramitacaoSubTableBean> subList) {
		this.subList = subList;
	}

	public List<RemanescentesTramitacaoSubTableBean> getSubList() {
		return subList;
	}

	public void setTotalJulgados(Long totalJulgados) {
		this.totalJulgados = totalJulgados;
	}

	public Long getTotalJulgados() {
		return totalJulgados;
	}

	public void setTotalArquivados(Long totalArquivados) {
		this.totalArquivados = totalArquivados;
	}

	public Long getTotalArquivados() {
		return totalArquivados;
	}

	public void setTotalDistribuidos(Long totalDistribuidos) {
		this.totalDistribuidos = totalDistribuidos;
	}

	public Long getTotalDistribuidos() {
		return totalDistribuidos;
	}

	public void setTotalRemanescentes(Long totalRemanescentes) {
		this.totalRemanescentes = totalRemanescentes;
	}

	public Long getTotalRemanescentes() {
		return totalRemanescentes;
	}

	public void setTotalTramitacao(Long totalTramitacao) {
		this.totalTramitacao = totalTramitacao;
	}

	public Long getTotalTramitacao() {
		return totalTramitacao;
	}

}