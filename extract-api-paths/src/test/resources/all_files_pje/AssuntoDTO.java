package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.AssuntoTrf;

/**
 * Classe que representa um assunto usado pelo Domicílio Eletrônico.
 * 
 */
public class AssuntoDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nome;
	
	/**
	 * Construtor.
	 * 
	 */
	public AssuntoDTO() {
		// Construtor.
	}
	
	/**
	 * Construtor.
	 * 
	 * @param assunto
	 */
	public AssuntoDTO(AssuntoTrf assunto) {
		if (assunto != null) {
			setNome(assunto.getAssuntoTrf());
		}
	}

	/**
	 * @return the nome
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * @param nome the nome to set
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}
}
