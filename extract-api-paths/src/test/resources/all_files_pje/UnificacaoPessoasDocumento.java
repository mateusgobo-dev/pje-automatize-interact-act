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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = UnificacaoPessoasDocumento.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_unificacao_pessoas_doc", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_unificacao_pessoas_doc"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class UnificacaoPessoasDocumento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<UnificacaoPessoasDocumento,Integer> {

	public static final String TABLE_NAME = "tb_unificacao_pessoas_doc";
	private static final long serialVersionUID = 1L;

	private int idUnificacaoPessoasDocumento;
	private UnificacaoPessoas unificacao;
	private PessoaDocumentoIdentificacao documento;
	private Boolean ativo;

	public UnificacaoPessoasDocumento() {
	}

	@Id
	@GeneratedValue(generator = "gen_unificacao_pessoas_doc")
	@Column(name = "id_unificacao_pessoas_doc", unique = true, nullable = false)
	public int getIdUnificacaoPessoasDocumento() {
		return this.idUnificacaoPessoasDocumento;
	}

	public void setIdUnificacaoPessoasDocumento(int idUnificacaoPessoasDocumento) {
		this.idUnificacaoPessoasDocumento = idUnificacaoPessoasDocumento;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_unificacao_pessoas", nullable = false)
	@NotNull
	public UnificacaoPessoas getUnificacao() {
		return this.unificacao;
	}

	public void setUnificacao(UnificacaoPessoas unificacao) {
		this.unificacao = unificacao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_documento", nullable = false)
	@NotNull
	public PessoaDocumentoIdentificacao getDocumento() {
		return this.documento;
	}

	public void setDocumento(PessoaDocumentoIdentificacao documento) {
		this.documento = documento;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return documento.getNome();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends UnificacaoPessoasDocumento> getEntityClass() {
		return UnificacaoPessoasDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdUnificacaoPessoasDocumento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
