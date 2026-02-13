package br.jus.cnj.pje.entidades.vo;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;

/**
 * Classe que representa um destinatario da comunicação.
 * Esta é composta da {@link Pessoa} e do seu respectivo {@link Endereco}
 */
public class DestinatarioComunicacao implements Serializable {

	private static final long serialVersionUID = 1L;
	private static int seq = 0;
	
	private Integer id;
	private Pessoa pessoa;
	private Endereco endereco;
	
	public DestinatarioComunicacao(Pessoa pessoa, Endereco endereco) {
		this.id = incrementarSequenciador();
		this.pessoa = pessoa;
		this.endereco = endereco;
	}
	
	public Integer getId() {
		if (id == null) {
			id = incrementarSequenciador();
		}
		return id;
	}

	public Pessoa getPessoa() {
		return pessoa;
	}
	
	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	public Endereco getEndereco() {
		return endereco;
	}
	
	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}
	
	private static synchronized Integer incrementarSequenciador() {
		return ++seq;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((endereco == null) ? 0 : endereco.hashCode());
		result = prime * result
				+ ((pessoa == null) ? 0 : pessoa.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DestinatarioComunicacao other = (DestinatarioComunicacao) obj;
		if (endereco == null) {
			if (other.endereco != null)
				return false;
		} else if (!endereco.equals(other.endereco))
			return false;
		if (pessoa == null) {
			if (other.pessoa != null)
				return false;
		} else if (!pessoa.equals(other.pessoa))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return this.pessoa.toString();
	}
}
