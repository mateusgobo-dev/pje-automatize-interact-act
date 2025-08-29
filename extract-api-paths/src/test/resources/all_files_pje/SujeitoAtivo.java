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
package br.jus.pje.nucleo.entidades.lancadormovimento;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Representa aquele que tem autoridade para realizar o ato processual.
 */
@Entity
@javax.persistence.Cacheable(true)
@Table(name = SujeitoAtivo.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_sujeito_ativo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sujeito_ativo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SujeitoAtivo implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<SujeitoAtivo,Long>{

	private static final long serialVersionUID = -5888751920361255593L;

	public static final String TABLE_NAME = "tb_sujeito_ativo";

	private Long idSujeitoAtivo;
	private String nome;
	private boolean ativo;

	public SujeitoAtivo() {
	}

	@Id
	@GeneratedValue(generator = "gen_sujeito_ativo")
	@Column(name = "id_sujeito_ativo", nullable = false)
	public Long getIdSujeitoAtivo() {
		return idSujeitoAtivo;
	}

	public void setIdSujeitoAtivo(Long idSujeitoAtivo) {
		this.idSujeitoAtivo = idSujeitoAtivo;
	}

	@Column(name = "ds_sujeito_ativo")
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	@Column(name = "in_ativo", nullable = false)
	@NotNull
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
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SujeitoAtivo> getEntityClass() {
		return SujeitoAtivo.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdSujeitoAtivo();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
