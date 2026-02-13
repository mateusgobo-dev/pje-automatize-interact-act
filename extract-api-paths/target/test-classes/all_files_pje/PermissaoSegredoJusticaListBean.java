package br.com.infox.pje.bean;

import java.io.Serializable;

/**
 * Bean para exibição da listagem do relatório de permissão para processos com
 * Segredo de Justiça
 * 
 * @author thiago
 */
public class PermissaoSegredoJusticaListBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6668059232312930856L;
	private String processo;
	private String classeJudicial;
	private boolean segredoJustica;
	private boolean textoSigiloso;

	public String getProcesso() {
		return processo;
	}

	public void setProcesso(String processo) {
		this.processo = processo;
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public boolean getSegredoJustica() {
		return segredoJustica;
	}

	public void setSegredoJustica(boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	public boolean getTextoSigiloso() {
		return textoSigiloso;
	}

	public void setTextoSigiloso(boolean textoSigiloso) {
		this.textoSigiloso = textoSigiloso;
	}

}