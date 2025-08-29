package br.jus.cnj.pje.util;

import java.io.Serializable;

public class ParAssinatura implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4700878417873545017L;
	private String conteudo;
	private String assinatura;

	public String getConteudo(){
		return conteudo;
	}

	public void setConteudo(String conteudo){
		this.conteudo = conteudo;
	}

	public String getAssinatura(){
		return assinatura;
	}

	public void setAssinatura(String assinatura){
		this.assinatura = assinatura;
	}
}