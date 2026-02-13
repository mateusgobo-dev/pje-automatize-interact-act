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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = Unificacao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_unificacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_unificacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Unificacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Unificacao,Integer> {

	public static final String TABLE_NAME = "tb_unificacao";
	private static final long serialVersionUID = 1L;

	private int idUnificacao;
	private Pessoa pessoaPrincipal;
	private Date dataUnificacao;
	private Usuario usuarioUnificador;
	private List<UnificacaoPessoas> unificacaoPessoasList = new ArrayList<UnificacaoPessoas>(0);
	private Boolean ativo;
	private String nomePesquisa;

	public Unificacao() {}

	public Unificacao(Pessoa pessoaPrincipal, Usuario usuarioUnificador) {
		this.ativo = true;
		this.dataUnificacao = new Date();
		this.pessoaPrincipal = pessoaPrincipal;
		this.usuarioUnificador = usuarioUnificador;
	}

	@Id
	@GeneratedValue(generator = "gen_unificacao")
	@Column(name = "id_unificacao", unique = true, nullable = false)
	public int getIdUnificacao() {
		return this.idUnificacao;
	}

	public void setIdUnificacao(int idUnificacao) {
		this.idUnificacao = idUnificacao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa", nullable = false)
	@NotNull
	public Pessoa getPessoaPrincipal() {
		return this.pessoaPrincipal;
	}

	public void setPessoaPrincipal(Pessoa pessoaPrincipal) {
		this.pessoaPrincipal = pessoaPrincipal;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaPrincipal(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoaPrincipal(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaPrincipal(pessoa.getPessoa());
		} else {
			setPessoaPrincipal((Pessoa)null);
		}
	}

	@Column(name = "dt_unificacao", nullable = false)
	public Date getDataUnificacao() {
		return dataUnificacao;
	}

	public void setDataUnificacao(Date dataUnificacao) {
		this.dataUnificacao = dataUnificacao;
	}

	public void setUsuarioUnificador(Usuario usuario) {
		this.usuarioUnificador = usuario;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_usuario")
	public Usuario getUsuarioUnificador() {
		return usuarioUnificador;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "unificacao")
	public List<UnificacaoPessoas> getUnificacaoPessoasList() {
		return this.unificacaoPessoasList;
	}

	public void setUnificacaoPessoasList(List<UnificacaoPessoas> unificacaoPessoasList) {
		this.unificacaoPessoasList = unificacaoPessoasList;
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
		return "Unificação da pessoa principal: "+pessoaPrincipal.getNome()+" feita pelo usuario: "+usuarioUnificador.getNome();
	}

	@Transient
	public String getNomePesquisa() {
		return this.nomePesquisa;
	}

	public void setNomePesquisa(String nomePesquisa) {
		this.nomePesquisa = nomePesquisa;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Unificacao> getEntityClass() {
		return Unificacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdUnificacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
