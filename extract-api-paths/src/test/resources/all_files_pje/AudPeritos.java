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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vs_aud_peritos")
public class AudPeritos implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idPessoaPeritoEspecialidade;
	private int idPessoaPerito;
	private String nome;
	private String especialidade;
	private String cpf;
	private String cnpj;
	private String rg;
	private String titulo_eleitor;

	@Id
	@Column(name = "id_pessoa_perito_especialidade", unique = true, nullable = false)
	public int getIdPessoaPeritoEspecialidade() {
		return idPessoaPeritoEspecialidade;
	}

	public void setIdPessoaPeritoEspecialidade(int idPessoaPeritoEspecialidade) {
		this.idPessoaPeritoEspecialidade = idPessoaPeritoEspecialidade;
	}

	@Column(name = "id_pessoa_perito", nullable = false)
	public int getIdPessoaPerito() {
		return idPessoaPerito;
	}

	public void setIdPessoaPerito(int idPessoaPerito) {
		this.idPessoaPerito = idPessoaPerito;
	}

	@Column(name = "ds_nome", insertable = false, updatable = false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "ds_especialidade", insertable = false, updatable = false)
	public String getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(String especialidade) {
		this.especialidade = especialidade;
	}

	@Column(name = "cpf", insertable = false, updatable = false)
	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	@Column(name = "cnpj", insertable = false, updatable = false)
	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	@Column(name = "rg", insertable = false, updatable = false)
	public String getRg() {
		return rg;
	}

	public void setRg(String rg) {
		this.rg = rg;
	}

	@Column(name = "titulo_eleitor", insertable = false, updatable = false)
	public String getTitulo_eleitor() {
		return titulo_eleitor;
	}

	public void setTitulo_eleitor(String titulo_eleitor) {
		this.titulo_eleitor = titulo_eleitor;
	}

}
