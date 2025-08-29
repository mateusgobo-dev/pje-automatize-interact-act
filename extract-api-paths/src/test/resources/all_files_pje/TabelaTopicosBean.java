package br.com.infox.editor.bean;

import java.io.Serializable;

public class TabelaTopicosBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Boolean colegiado;
	private Integer temDivergencia;
	private Integer temDivergenciaNaoConcluidaNaoLiberada;
	private Integer temDivergenciaAcaoPendente;
	private Integer temAnotacao;
	private Integer temAnotacaoNaoConcluida;
	private Integer temDestaqueDivergencia;
	private Integer temDestaqueDivergenciaNaoConcluidoNaoLiberado;
	private Integer temDestaqueAnotacao;
	private Integer temDestaqueAnotacaoNaoConcluidoNaoLiberado;

	public Boolean getColegiado() {
		return colegiado;
	}

	public void setColegiado(Boolean colegiado) {
		this.colegiado = colegiado;
	}

	public Integer getTemDivergencia() {
		return temDivergencia;
	}

	public void setTemDivergencia(Integer temDivergencia) {
		this.temDivergencia = temDivergencia;
	}

	public Integer getTemDivergenciaNaoConcluidaNaoLiberada() {
		return temDivergenciaNaoConcluidaNaoLiberada;
	}

	public void setTemDivergenciaNaoConcluidaNaoLiberada(Integer temDivergenciaNaoConcluidaNaoLiberada) {
		this.temDivergenciaNaoConcluidaNaoLiberada = temDivergenciaNaoConcluidaNaoLiberada;
	}

	public Integer getTemDivergenciaAcaoPendente() {
		return temDivergenciaAcaoPendente;
	}

	public void setTemDivergenciaAcaoPendente(Integer temDivergenciaAcaoPendente) {
		this.temDivergenciaAcaoPendente = temDivergenciaAcaoPendente;
	}

	public Integer getTemAnotacao() {
		return temAnotacao;
	}

	public void setTemAnotacao(Integer temAnotacao) {
		this.temAnotacao = temAnotacao;
	}

	public Integer getTemAnotacaoNaoConcluida() {
		return temAnotacaoNaoConcluida;
	}

	public void setTemAnotacaoNaoConcluida(Integer temAnotacaoNaoConcluida) {
		this.temAnotacaoNaoConcluida = temAnotacaoNaoConcluida;
	}

	public Integer getTemDestaqueDivergencia() {
		return temDestaqueDivergencia;
	}

	public void setTemDestaqueDivergencia(Integer temDestaqueDivergencia) {
		this.temDestaqueDivergencia = temDestaqueDivergencia;
	}

	public Integer getTemDestaqueDivergenciaNaoConcluidoNaoLiberado() {
		return temDestaqueDivergenciaNaoConcluidoNaoLiberado;
	}

	public void setTemDestaqueDivergenciaNaoConcluidoNaoLiberado(Integer temDestaqueDivergenciaNaoConcluidoNaoLiberado) {
		this.temDestaqueDivergenciaNaoConcluidoNaoLiberado = temDestaqueDivergenciaNaoConcluidoNaoLiberado;
	}

	public Integer getTemDestaqueAnotacao() {
		return temDestaqueAnotacao;
	}

	public void setTemDestaqueAnotacao(Integer temDestaqueAnotacao) {
		this.temDestaqueAnotacao = temDestaqueAnotacao;
	}

	public Integer getTemDestaqueAnotacaoNaoConcluidoNaoLiberado() {
		return temDestaqueAnotacaoNaoConcluidoNaoLiberado;
	}

	public void setTemDestaqueAnotacaoNaoConcluidoNaoLiberado(Integer temDestaqueAnotacaoNaoConcluidoNaoLiberado) {
		this.temDestaqueAnotacaoNaoConcluidoNaoLiberado = temDestaqueAnotacaoNaoConcluidoNaoLiberado;
	}

}
