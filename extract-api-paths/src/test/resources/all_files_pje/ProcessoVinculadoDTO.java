package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto;

import java.io.Serializable;

/**
 * Classe que representa um processo usado pelo Domicílio Eletrônico.
 * 
 */
public class ProcessoVinculadoDTO implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	private String numeroProcesso;
	
	/**
	 * Construtor.
	 */
	public ProcessoVinculadoDTO() {
		//Construtor
	}
	
	
	/**
	 * Construtor.
	 * 
	 * @param processo
	 */
	public ProcessoVinculadoDTO(String numeroProcesso) {
		setNumeroProcesso(numeroProcesso);
	}

	/**
	 * @return the numeroProcesso
	 */
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	/**
	 * @param numeroProcesso the numeroProcesso to set
	 */
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}
}