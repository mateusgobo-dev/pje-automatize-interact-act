/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.jt.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AudAdvogadoPK implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String oab;
	private String cpf;
	private String nome;
	
	public AudAdvogadoPK() {
	}

	public AudAdvogadoPK(String oab, String cpf, String nome) {
		super();
		this.oab = oab;
		this.cpf = cpf;
		this.nome = nome;
	}

	@Column(name = "nr_oab", unique = true, nullable = false)
	public String getOab() {
		return oab;
	}

	public void setOab(String oab) {
		this.oab = oab;
	}
	
	@Column(name = "cpf", insertable = false, updatable = false)
	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	@Column(name = "ds_nome", insertable = false, updatable = false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cpf == null) ? 0 : cpf.hashCode());
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
		result = prime * result + ((oab == null) ? 0 : oab.hashCode());
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
		AudAdvogadoPK other = (AudAdvogadoPK) obj;
		if (cpf == null) {
			if (other.cpf != null)
				return false;
		} else if (!cpf.equals(other.cpf))
			return false;
		if (nome == null) {
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
			return false;
		if (oab == null) {
			if (other.oab != null)
				return false;
		} else if (!oab.equals(other.oab))
			return false;
		return true;
	}
	
}
