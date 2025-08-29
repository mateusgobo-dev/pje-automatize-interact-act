package br.jus.pje.nucleo.entidades.min;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = OrgaoJulgadorColegiadoMin.TABLE_NAME)
@Immutable
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler", "session", "flushMode", "persistenceContext"})
public class OrgaoJulgadorColegiadoMin implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_orgao_julgador_colgiado";

	private Integer id;
	private String nome;
	private Boolean ativo;
	
	@Id
	@Column(name = "id_orgao_julgador_colegiado", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
		
	@Column(name = "ds_orgao_julgador_colegiado", insertable = false, updatable = false)
	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
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
		return nome;
	}

}