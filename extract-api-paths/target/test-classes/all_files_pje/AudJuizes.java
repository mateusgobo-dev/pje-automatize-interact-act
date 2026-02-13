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
@Table(name = "vs_aud_juizes")
//@PrimaryKeyJoinColumn(name = "id_pessoa_magistrado")
public class AudJuizes implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idPessoaMagistrado;
	private String nome;
	private String sexo;

	public void setIdPessoaMagistrado(int idPessoaMagistrado) {
		this.idPessoaMagistrado = idPessoaMagistrado;
	}

	@Id
	@Column(name = "id_pessoa_magistrado", unique = true, nullable = false)
	public int getIdPessoaMagistrado() {
		return idPessoaMagistrado;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "ds_nome", insertable = false, updatable = false)
	public String getNome() {
		return nome;
	}

	public void setSexo(String sexo) {
		this.sexo = sexo;
	}

	@Column(name = "in_sexo", insertable = false, updatable = false)
	public String getSexo() {
		return sexo;
	}
}
