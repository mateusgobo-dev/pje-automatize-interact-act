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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_pais", uniqueConstraints = { @UniqueConstraint(columnNames = "cd_pais"),
		@UniqueConstraint(columnNames = "ds_pais") })
@org.hibernate.annotations.GenericGenerator(name = "gen_pais", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pais"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler", "session", "flushMode", "persistenceContext"})
public class Pais implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Pais,Integer> {

	private static final long serialVersionUID = 1L;
	private Integer id;
	private String codigo;
	private String descricao;
	private String sigla;

	public Pais() {

	}

	@Id
	@GeneratedValue(generator = "gen_pais")
	@Column(name = "id_pais", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "cd_pais", length = 15)
	@Length(max = 15)
	@NotNull
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	@Column(name = "ds_pais", length = 500)
	@Length(max = 500)
	@NotNull
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	
	@Column(name = "ds_sigla", length = 2)
	@Length(max = 2)
	@NotNull
	public String getSigla() {
		return sigla;
	}

	public void setSigla(String sigla) {
		this.sigla = sigla;
	}

	@Override
	public String toString() {
		return getDescricao();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Pais> getEntityClass() {
		return Pais.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
