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

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;



/**
 * Classe que faz associacao entre pessoa e documento, utilizado para
 * resolver o problema relatado em [#PJEII-967], onde o Advogado ao 
 * incluir uma parte e ao informar os documentos da mesma
 * o documento que existe cadastrado na base ele não pode ver nem
 * cadastrar outro.
 * 
 * Com esta associacao o advogado ou outro ator (Magistrado, procurador, juspostuland) poderá
 * cadastrar os documentos que existe e caso acerte o número do documento ele poderá ver o mesmo.
 *
 */
@Entity
@Table(name="tb_visibilida_pess_doc_ind")
@org.hibernate.annotations.GenericGenerator(name = "gen_visibilidade_pessoa_doc_ind", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_visibilidade_pessoa_doc_ind"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class VisibilidadePessoaDocumentoIdentificacao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "gen_visibilidade_pessoa_doc_ind")
	@Column(name = "id_visibilidade_pessoa_doc_ind", nullable = false)
	private Long id;

	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name = "id_pessoa_doc_identificacao", nullable = false)
	@NotNull
	private PessoaDocumentoIdentificacao documento;

	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name = "id_pessoa", nullable = false)
	@NotNull
	private Pessoa pessoa;

	public VisibilidadePessoaDocumentoIdentificacao(){}

	public VisibilidadePessoaDocumentoIdentificacao(PessoaDocumentoIdentificacao documento, Pessoa pessoa){
		this.documento = documento;
		this.pessoa = pessoa;
	}


	public Long getId(){
		return id;
	}


	public void setId(Long id){
		this.id = id;
	}


	public PessoaDocumentoIdentificacao getDocumento(){
		return documento;
	}


	public void setDocumento(PessoaDocumentoIdentificacao documento){
		this.documento = documento;
	}


	public Pessoa getPessoa(){
		return pessoa;
	}


	public void setPessoa(Pessoa pessoa){
		this.pessoa = pessoa;
	}

	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof VisibilidadePessoaDocumentoIdentificacao))
			return false;
		VisibilidadePessoaDocumentoIdentificacao other = (VisibilidadePessoaDocumentoIdentificacao) obj;
		if (id == null){
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		return true;
	}


}
