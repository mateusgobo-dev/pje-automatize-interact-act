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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

/**
 * TipoPessoaQualificacao
 */
@Entity
@Table(name = "tb_tipo_pess_qualificacao")
public class TipoPessoaQualificacao implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private TipoPessoaQualificacaoId id = new TipoPessoaQualificacaoId();
	private Qualificacao qualificacao;
	private TipoPessoa tipoPessoa;
	private Boolean obrigatorio;

	public TipoPessoaQualificacao() {
	}

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "idTipoPessoa", column = @Column(name = "id_tipo_pessoa", nullable = false)),
			@AttributeOverride(name = "idQualificacao", column = @Column(name = "id_qualificacao", nullable = false)) })
	@NotNull
	public TipoPessoaQualificacaoId getId() {
		return this.id;
	}

	public void setId(TipoPessoaQualificacaoId id) {
		this.id = id;
	}

	@Transient
	public int getIdTipoPessoa() {
		return id.getIdTipoPessoa();
	}

	public void setIdTipoPessoa(int idTipoPessoa) {
		id.setIdTipoPessoa(idTipoPessoa);
	}

	@Transient
	public int getIdQualificacao() {
		return id.getIdQualificacao();
	}

	public void setIdQualificacao(int idQualificacao) {
		id.setIdQualificacao(idQualificacao);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_pessoa", nullable = false, insertable = false, updatable = false)
	@NotNull
	public TipoPessoa getTipoPessoa() {
		return this.tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_qualificacao", nullable = false, insertable = false, updatable = false)
	@NotNull
	public Qualificacao getQualificacao() {
		return this.qualificacao;
	}

	public void setQualificacao(Qualificacao qualificacao) {
		this.qualificacao = qualificacao;
	}

	@Column(name = "in_obrigatorio", nullable = false)
	@NotNull
	public Boolean getObrigatorio() {
		return this.obrigatorio;
	}

	public void setObrigatorio(Boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoPessoaQualificacao)) {
			return false;
		}
		TipoPessoaQualificacao other = (TipoPessoaQualificacao) obj;
		if (getId() != other.getId()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}
}