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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.TipoPessoaRelacaoEnum;

/**
 * TipoRelacaoPessoal
 */

@Entity
@Table(name = "tb_tipo_relacao_pessoal")
public class TipoRelacaoPessoal implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private String codigo;
	private String tipoRelacaoPessoal;
	private TipoPessoaRelacaoEnum tipoPessoaRelacao;
	private Boolean ativo;

	public TipoRelacaoPessoal() {
	}

	@Id
	@Column(name = "cd_tipo_relacao_pessoal", unique = true, nullable = false)
	public String getCodigo() {
		return this.codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	@Column(name = "ds_tipo_relacao_pessoal", nullable = false, length = 200)
	@NotNull
	@Length(max = 50)
	public String getTipoRelacaoPessoal() {
		return this.tipoRelacaoPessoal;
	}

	public void setTipoRelacaoPessoal(String tipoRelacaoPessoal) {
		this.tipoRelacaoPessoal = tipoRelacaoPessoal;
	}

	@Column(name = "in_tipo_pessoa_relacao", length = 1)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.TipoPessoaRelacaoType")
	public TipoPessoaRelacaoEnum getTipoPessoaRelacao() {
		return this.tipoPessoaRelacao;
	}

	public void setTipoPessoaRelacao(TipoPessoaRelacaoEnum tipoPessoaRelacao) {
		this.tipoPessoaRelacao = tipoPessoaRelacao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return this.tipoRelacaoPessoal;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoRelacaoPessoal)) {
			return false;
		}
		TipoRelacaoPessoal other = (TipoRelacaoPessoal) obj;
		if (getCodigo() == null || !getCodigo().equalsIgnoreCase(other.getCodigo())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getCodigo() == null) ? 0 : getCodigo().hashCode());
		return result;
	}
}