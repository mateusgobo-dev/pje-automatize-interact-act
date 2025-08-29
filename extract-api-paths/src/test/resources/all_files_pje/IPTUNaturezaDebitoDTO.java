/*
 * IPTUNaturezaDebitoDTO.java
 *
 * Data: 20/05/2021
 */
package br.jus.pje.nucleo.dto;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.Cep;

/**
 * Classe de natureza de débito IPTU.
 * 
 * @author Adriano Pamplona
 */
public class IPTUNaturezaDebitoDTO implements Serializable {
	private String inscricao;
	private String cep;
	private String estado;
	private String municipio;
	private String bairro;
	private String logradouro;
	private String numero;
	private String complemento;

	/**
	 * Construtor.
	 *
	 */
	public IPTUNaturezaDebitoDTO() {
		super();
	}
	
	/**
	 * Construtor.
	 *
	 * @param cep
	 */
	public IPTUNaturezaDebitoDTO(Cep cep) {
		if (cep != null) {
			setCep(cep.getNumeroCep());
			setEstado(cep.getMunicipio().getEstado().getEstado());
			setMunicipio(cep.getMunicipio().getMunicipio());
			setBairro(cep.getNomeBairro());		
			setLogradouro(cep.getNomeLogradouro());
			setComplemento(cep.getComplemento());
			setNumero(cep.getNumeroEndereco());
		}
	}
	
	/**
	 * @return inscricao.
	 */
	public String getInscricao() {
		return inscricao;
	}

	/**
	 * @param inscricao Atribui inscricao.
	 */
	public void setInscricao(String inscricao) {
		this.inscricao = inscricao;
	}

	/**
	 * @return cep.
	 */
	public String getCep() {
		return cep;
	}

	/**
	 * @param cep Atribui cep.
	 */
	public void setCep(String cep) {
		this.cep = cep;
	}

	/**
	 * @return estado.
	 */
	public String getEstado() {
		return estado;
	}

	/**
	 * @param estado Atribui estado.
	 */
	public void setEstado(String estado) {
		this.estado = estado;
	}

	/**
	 * @return municipio.
	 */
	public String getMunicipio() {
		return municipio;
	}

	/**
	 * @param municipio Atribui municipio.
	 */
	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	/**
	 * @return bairro.
	 */
	public String getBairro() {
		return bairro;
	}

	/**
	 * @param bairro Atribui bairro.
	 */
	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	/**
	 * @return logradouro.
	 */
	public String getLogradouro() {
		return logradouro;
	}

	/**
	 * @param logradouro Atribui logradouro.
	 */
	public void setLogradouro(String logradouro) {
		this.logradouro = logradouro;
	}

	/**
	 * @return numero.
	 */
	public String getNumero() {
		return numero;
	}

	/**
	 * @param numero Atribui numero.
	 */
	public void setNumero(String numero) {
		this.numero = numero;
	}

	/**
	 * @return complemento.
	 */
	public String getComplemento() {
		return complemento;
	}

	/**
	 * @param complemento Atribui complemento.
	 */
	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

}
