package br.jus.csjt.pje.commons.model;

public class AdvogadoAudiencia {

	private String nome;
	private String OAB;
	private String UF;

	// getters e setters

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getNome() {
		return this.nome;
	}

	public void setOAB(String oAB) {
		this.OAB = oAB;
	}

	public String getOAB() {
		return this.OAB;
	}

	public void setUF(String uF) {
		this.UF = uF;
	}

	public String getUF() {
		return this.UF;
	}

}
