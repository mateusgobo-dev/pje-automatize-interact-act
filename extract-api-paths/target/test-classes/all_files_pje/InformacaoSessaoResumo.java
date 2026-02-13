package br.jus.cnj.pje.webservice;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

public class InformacaoSessaoResumo implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8416798895610674170L;
	
	private String tipo;
	private Integer quantidade;
	
	@XmlElement(name="tipo")
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	@XmlElement(name="quantidade")
	public Integer getQuantidade() {
		return quantidade;
	}
	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}
}
