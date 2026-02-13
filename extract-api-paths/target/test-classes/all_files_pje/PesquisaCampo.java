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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_pesquisa_campo")
@org.hibernate.annotations.GenericGenerator(name = "gen_pesquisa_campo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pesquisa_campo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PesquisaCampo implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<PesquisaCampo,Integer> {

	private static final long serialVersionUID = 1L;
	private int idPesquisaCampo;
	private String nome;
	private String valor;
	private Pesquisa pesquisa;

	public void setIdPesquisaCampo(int idPesquisaCampo) {
		this.idPesquisaCampo = idPesquisaCampo;
	}

	@Id
	@GeneratedValue(generator = "gen_pesquisa_campo")
	@Column(name = "id_pesquisa_campo", unique = true, nullable = false)
	public int getIdPesquisaCampo() {
		return idPesquisaCampo;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "ds_nome", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getNome() {
		return nome;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	@Column(name = "ds_valor", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getValor() {
		return valor;
	}

	public void setPesquisa(Pesquisa pesquisa) {
		this.pesquisa = pesquisa;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pesquisa", nullable = false)
	@NotNull
	public Pesquisa getPesquisa() {
		return pesquisa;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PesquisaCampo> getEntityClass() {
		return PesquisaCampo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPesquisaCampo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
