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
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_tipo_norma_penal")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_norma_penal", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_norma_penal"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoNormaPenal implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoNormaPenal,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String descricao;
	private Boolean inAtivo;

	public TipoNormaPenal() {

	}

	public TipoNormaPenal(Integer id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_norma_penal")
	@Column(name = "id_tipo_norma_penal", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "ds_tipo_norma", length = 100)
	@NotNull
	@Length(max = 100)
	public String getDescricao() {
		return this.descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getInAtivo() {
		return this.inAtivo;
	}

	public void setInAtivo(Boolean inAtivo) {
		this.inAtivo = inAtivo;
	}

	@Override
	public String toString() {
		return this.getDescricao();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoNormaPenal> getEntityClass() {
		return TipoNormaPenal.class;
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
