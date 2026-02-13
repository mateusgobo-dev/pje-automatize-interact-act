package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "tb_prioridade_processo")
public class PrioridadeProcessoMin implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String nome;
	private Boolean ativo;

	public PrioridadeProcessoMin() {
	}

	@Id
	@Column(name = "id_prioridade_processo", unique = true, nullable = false,insertable = false,updatable = false)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "ds_prioridade", insertable = false,updatable = false)
	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "in_ativo",insertable = false,updatable = false)
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return nome;
	}
}