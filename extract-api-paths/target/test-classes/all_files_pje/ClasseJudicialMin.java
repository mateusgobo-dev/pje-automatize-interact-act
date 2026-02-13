package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Table(name = ClasseJudicialMin.TABLE_NAME)
@Immutable
public class ClasseJudicialMin implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_classe_judicial";

	private Integer id;
	private String codigo;
	private String nome;

	private Boolean ativo;

	@Id
	@Column(name = "id_classe_judicial", insertable = false, updatable = false)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "cd_classe_judicial", insertable = false, updatable = false)
	public String getCodigo() {
		return this.codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	@Column(name = "ds_classe_judicial", insertable = false, updatable = false)
	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome= nome;
	}

	@Column(name = "in_ativo", insertable = false, updatable = false)
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return nome + " - " + codigo;
	}

}