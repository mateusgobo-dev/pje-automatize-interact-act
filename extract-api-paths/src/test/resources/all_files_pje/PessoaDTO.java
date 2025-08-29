package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.util.StringUtil;


/**
 * Classe que representa um assunto usado pelo Domicílio Eletrônico.
 * 
 */
public class PessoaDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nome;
	private String modalidadeDocumentoIdentificador;
	private String numeroDocumentoPrincipal;
	
	/**
	 * Construtor.
	 * 
	 */
	public PessoaDTO() {
		// Construtor.
	}
	
	/**
	 * Construtor.
	 * 
	 * @param pessoa
	 */
	public PessoaDTO(Pessoa pessoa) {
		if (pessoa != null) {
			setNome(pessoa.getNome());
			setNumeroDocumentoPrincipal(pessoa.getDocumentoCpfCnpj());
			if(!StringUtil.isEmpty(getNumeroDocumentoPrincipal())) {
				setModalidadeDocumentoIdentificador(pessoa.getModalidadeDocumentoCpfCnpj());
			}
			
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
	/**
	 * @return the modalidadeDocumentoIdentificador
	 */
	public String getModalidadeDocumentoIdentificador() {
		return modalidadeDocumentoIdentificador;
	}
	/**
	 * @param modalidadeDocumentoIdentificador the modalidadeDocumentoIdentificador to set
	 */
	public void setModalidadeDocumentoIdentificador(String modalidadeDocumentoIdentificador) {
		this.modalidadeDocumentoIdentificador = modalidadeDocumentoIdentificador;
	}
	/**
	 * @return the numeroDocumentoPrincipal
	 */
	public String getNumeroDocumentoPrincipal() {
		return numeroDocumentoPrincipal;
	}
	/**
	 * @param numeroDocumentoPrincipal the numeroDocumentoPrincipal to set
	 */
	public void setNumeroDocumentoPrincipal(String numeroDocumentoPrincipal) {
		this.numeroDocumentoPrincipal = numeroDocumentoPrincipal;
	}
}
