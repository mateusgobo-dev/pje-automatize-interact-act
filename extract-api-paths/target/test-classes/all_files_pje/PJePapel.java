package br.jus.cnj.pje.webservice.criminal.dto;

import java.util.Objects;

public class PJePapel {

	private String nome;
	private String identificador;

	public PJePapel(String nome, String identificador) {
		this.nome = nome;
		this.identificador = identificador;
	}

	public String getNome() {
		return nome;
	}

	public String getIdentificador() {
		return identificador;
	}

	@Override
	public int hashCode() {
		return Objects.hash(identificador);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PJePapel other = (PJePapel) obj;
		return Objects.equals(identificador, other.identificador);
	}

}
