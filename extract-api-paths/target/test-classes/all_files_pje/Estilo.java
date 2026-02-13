package br.com.infox.editor.bean;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.editor.CssDocumento;

public class Estilo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String nome;
	private String conteudo;
	private boolean padrao;
	
	public Estilo() {
	}

	public Estilo(CssDocumento css) {
		this.nome = css.getNome();
		this.conteudo = css.getConteudo();
		this.padrao = css.getPadrao();
	}
	
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	public boolean isPadrao() {
		return padrao;
	}

	public void setPadrao(boolean padrao) {
		this.padrao = padrao;
	}
}
