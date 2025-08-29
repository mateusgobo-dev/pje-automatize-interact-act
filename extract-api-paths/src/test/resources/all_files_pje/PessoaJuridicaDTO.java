package br.jus.cnj.pje.webservice.requisitorio.dto;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.PessoaJuridica;

public class PessoaJuridicaDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nomePessoa;
	private String documentoPrincipal;
	
	public PessoaJuridicaDTO() {}
	
	public PessoaJuridicaDTO(PessoaJuridica pessoaJuridica) {
		this.nomePessoa = pessoaJuridica.getNome();
		this.documentoPrincipal = pessoaJuridica.getDocumentoCpfCnpj();
	}

	public String getNomePessoa() {
		return nomePessoa;
	}

	public void setNomePessoa(String nomePessoa) {
		this.nomePessoa = nomePessoa;
	}

	public String getDocumentoPrincipal() {
		return documentoPrincipal;
	}

	public void setDocumentoPrincipal(String documentoPrincipal) {
		this.documentoPrincipal = documentoPrincipal;
	}
	

}
