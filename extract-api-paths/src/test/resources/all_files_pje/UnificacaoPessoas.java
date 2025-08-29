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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = UnificacaoPessoas.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_unificacao_pessoas", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_unificacao_pessoas"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class UnificacaoPessoas implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<UnificacaoPessoas,Integer> {

	public static final String TABLE_NAME = "tb_unificacao_pessoas";
	private static final long serialVersionUID = 1L;

	private int idUnificacaoPessoas;
	private Unificacao unificacao;
	private Pessoa pessoaSecundariaUnificada;
	private Date dataUnificacao;
	private Usuario usuarioUnificador;
	private List<UnificacaoPessoasObjeto> unificacaoPessoasObjeto = new ArrayList<UnificacaoPessoasObjeto>(0);
	private Date dataDesunificacao;
	private Usuario usuarioDesunificador;
	private List<UnificacaoPessoasDocumento> documentosList = new ArrayList<UnificacaoPessoasDocumento>(0);
	private List<UnificacaoPessoasParte> partesList = new ArrayList<UnificacaoPessoasParte>(0);
	private Boolean ativo;

	public UnificacaoPessoas() {}

	public UnificacaoPessoas(Unificacao unificacao, Pessoa pessoaSecundaria) {
		this.unificacao = unificacao;
		this.pessoaSecundariaUnificada = pessoaSecundaria;
		this.dataUnificacao = unificacao.getDataUnificacao();
		this.usuarioUnificador = unificacao.getUsuarioUnificador();
		this.ativo = true;
	}

	@Id
	@GeneratedValue(generator = "gen_unificacao_pessoas")
	@Column(name = "id_unificacao_pessoas", unique = true, nullable = false)
	public int getIdUnificacaoPessoas() {
		return this.idUnificacaoPessoas;
	}

	public void setIdUnificacaoPessoas(int idUnificacaoPessoas) {
		this.idUnificacaoPessoas = idUnificacaoPessoas;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_unificacao", nullable = false)
	@NotNull
	public Unificacao getUnificacao() {
		return this.unificacao;
	}

	public void setUnificacao(Unificacao unificacao) {
		this.unificacao = unificacao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa", nullable = false)
	@NotNull
	public Pessoa getPessoaSecundariaUnificada() {
		return this.pessoaSecundariaUnificada;
	}

	public void setPessoaSecundariaUnificada(Pessoa pessoa) {
		this.pessoaSecundariaUnificada = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaSecundariaUnificada(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoaSecundariaUnificada(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaSecundariaUnificada(pessoa.getPessoa());
		} else {
			setPessoaSecundariaUnificada((Pessoa)null);
		}
	}

	/**
	 * indica se a unificaçao desta pessoa esta ativa, ou seja, se esta pessoa ainda esta unificada com a pessoa principal.
	 * @return true / false
	 */
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "unificacaoPessoas")
	public List<UnificacaoPessoasObjeto> getUnificacaoPessoasObjetos() {
		return this.unificacaoPessoasObjeto;
	}
	
	public void setUnificacaoPessoasObjetos(List<UnificacaoPessoasObjeto> unificacaoPessoasObjetos) {
		this.unificacaoPessoasObjeto = unificacaoPessoasObjetos;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "unificacao")
	public List<UnificacaoPessoasDocumento> getDocumentosList() {
		return this.documentosList;
	}

	public void setDocumentosList(List<UnificacaoPessoasDocumento> documentosList) {
		this.documentosList = documentosList;
	}

	public void setPartesList(List<UnificacaoPessoasParte> partesList) {
		this.partesList = partesList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "unificacao")
	public List<UnificacaoPessoasParte> getPartesList() {
		return this.partesList;
	}
	
	@Column(name = "dt_unificacao")
	public Date getDataUnificacao() {
		return dataUnificacao;
	}

	public void setDataUnificacao(Date dataUnificacao) {
		this.dataUnificacao = dataUnificacao;
	}

	@Column(name = "dt_desunificacao")
	public Date getDataDesunificacao() {
		return dataDesunificacao;
	}

	public void setDataDesunificacao(Date dataDesunificacao) {
		this.dataDesunificacao = dataDesunificacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_unificacao")
	public Usuario getUsuarioUnificacao() {
		return usuarioUnificador;
	}
	
	public void setUsuarioUnificacao(Usuario usuarioUnificacao) {
		this.usuarioUnificador = usuarioUnificacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_desunificacao")
	public Usuario getUsuarioDesunificador() {
		return usuarioDesunificador;
	}

	public void setUsuarioDesunificador(Usuario usuarioDesunificacao) {
		this.usuarioDesunificador = usuarioDesunificacao;
	}
	
	@Override
	public String toString() {
		return "Unificação da pessoa principal:"+unificacao.getPessoaPrincipal().getNome()+" com a pessoa secundária: "+pessoaSecundariaUnificada.getNome();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends UnificacaoPessoas> getEntityClass() {
		return UnificacaoPessoas.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdUnificacaoPessoas());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
