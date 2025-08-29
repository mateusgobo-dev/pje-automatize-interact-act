package br.jus.cnj.pje.visao.beans;

import java.io.Serializable;

import javax.faces.event.ValueChangeEvent;

import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ResultadoSentencaParte;

/**
 * Componente que encapsula os atributos da tela de Ajuste de Movimentos.
 * 
 * @since 1.4.2
 * @category PJE-JT
 * @created 2011-08-25
 * @author Emmanuel S. Magalhães, Guilherme Bispo
 */
public class ResultadoSentencaParteBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4124846416691607945L;
	private Boolean selected = Boolean.FALSE;
	private ResultadoSentencaParte resultadoSentencaParte;
	private Boolean ehAutor;

	public ResultadoSentencaParteBean(ProcessoParte processoParte) {
		ResultadoSentencaParte resultadoSentencaParte2 = new ResultadoSentencaParte();
		resultadoSentencaParte2.setProcessoParte(processoParte);
		this.resultadoSentencaParte = resultadoSentencaParte2;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public Boolean getSelected() {
		return selected;
	}

	public void setResultadoSentencaParte(ResultadoSentencaParte resultadoSentencaParte) {
		this.resultadoSentencaParte = resultadoSentencaParte;
	}

	public ResultadoSentencaParte getResultadoSentencaParte() {
		return resultadoSentencaParte;
	}

	public void setEhAutor(Boolean ehAutor) {
		this.ehAutor = ehAutor;
	}

	public Boolean getEhAutor() {
		return ehAutor;
	}

	// atualizar o modelo na fase 2(apply request), usando o immediate=true E
	// setando o changeListener,
	// somente para o atributo selected
	public void inputChanged(ValueChangeEvent event) {
		this.setSelected((Boolean) event.getNewValue());
	}
}
