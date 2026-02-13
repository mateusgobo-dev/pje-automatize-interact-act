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
package br.jus.pje.nucleo.entidades;

// Generated 30/10/2008 07:40:27 by Hibernate Tools 3.2.0.CR1

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * TipoPessoaQualificacaoId
 */
@Embeddable
public class TipoPessoaQualificacaoId implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private int idTipoPessoa;
	private int idQualificacao;

	public TipoPessoaQualificacaoId() {
	}

	@Column(name = "id_tipo_pessoa", nullable = false)
	@NotNull
	public int getIdTipoPessoa() {
		return this.idTipoPessoa;
	}

	public void setIdTipoPessoa(int idTipoPessoa) {
		this.idTipoPessoa = idTipoPessoa;
	}

	@Column(name = "id_qualificacao", nullable = false)
	@NotNull
	public int getIdQualificacao() {
		return this.idQualificacao;
	}

	public void setIdQualificacao(int idQualificacao) {
		this.idQualificacao = idQualificacao;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(getIdTipoPessoa());
		buffer.append("-");
		buffer.append(getIdQualificacao());
		return buffer.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoPessoaQualificacaoId)) {
			return false;
		}
		TipoPessoaQualificacaoId other = (TipoPessoaQualificacaoId) obj;
		if (getIdQualificacao() != other.getIdQualificacao() || getIdTipoPessoa() != other.getIdTipoPessoa()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdQualificacao();
		result = prime * result + getIdTipoPessoa();
		return result;
	}

}