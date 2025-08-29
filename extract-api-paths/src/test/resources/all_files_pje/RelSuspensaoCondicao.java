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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_rel_condicao_suspensao")
@org.hibernate.annotations.GenericGenerator(name = "gen_rel_suspensao_condicao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_rel_suspensao_condicao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RelSuspensaoCondicao implements IEntidade<RelSuspensaoCondicao, Integer> {

	private static final long serialVersionUID = 1L;
	private IcrSuspensao suspensao;
	private CondicaoSuspensao condicaoSuspensao;
	private String textoLivre;
	private Integer idRelSuspensaoCondicao;
	private Boolean ativo;

	public RelSuspensaoCondicao(){
		ativo = true;
	}

	public RelSuspensaoCondicao(IcrSuspensao suspensao, CondicaoSuspensao condicaoSuspensao, String texto){
		this.suspensao = suspensao;
		this.condicaoSuspensao = condicaoSuspensao;
		this.textoLivre = texto;
		this.ativo = true;
	}

	@Id
	@GeneratedValue(generator = "gen_rel_suspensao_condicao")
	@Column(name = "id_rel_suspensao_condicao", unique = true, nullable = false)
	public Integer getIdRelSuspensaoCondicao(){
		return idRelSuspensaoCondicao;
	}

	public void setIdRelSuspensaoCondicao(Integer idRelSuspensaoCondicao){
		this.idRelSuspensaoCondicao = idRelSuspensaoCondicao;
	}

	public void setAtivo(Boolean b){
		this.ativo = b;
	}

	@NotNull
	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo(){
		return this.ativo;
	}

	@Column(name = "ds_campo_texto_livre")
	public String getTextoLivre(){
		return textoLivre;
	}

	public void setTextoLivre(String condicaoTextoLivre){
		this.textoLivre = condicaoTextoLivre;
	}

	@ManyToOne
	@JoinColumn(name = "id_suspensao", nullable = false)
	public IcrSuspensao getSuspensao(){
		return suspensao;
	}

	public void setSuspensao(IcrSuspensao suspensao){
		this.suspensao = suspensao;
	}

	@ManyToOne
	@JoinColumn(name = "id_condicao_suspensao", nullable = false)
	public CondicaoSuspensao getCondicaoSuspensao(){
		return condicaoSuspensao;
	}

	public void setCondicaoSuspensao(CondicaoSuspensao condicaoSuspensao){
		this.condicaoSuspensao = condicaoSuspensao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ativo == null) ? 0 : ativo.hashCode());
		result = prime
				* result
				+ ((getCondicaoSuspensao() == null) ? 0 : condicaoSuspensao
						.hashCode());
		result = prime * result
				+ ((getSuspensao() == null) ? 0 : suspensao.hashCode());
		result = prime * result
				+ ((getTextoLivre() == null) ? 0 : textoLivre.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelSuspensaoCondicao other = (RelSuspensaoCondicao) obj;
		if (ativo == null) {
			if (other.ativo != null)
				return false;
		} else if (!ativo.equals(other.ativo))
			return false;
		if (getCondicaoSuspensao() == null) {
			if (other.getCondicaoSuspensao() != null)
				return false;
		} else if (!condicaoSuspensao.equals(other.getCondicaoSuspensao()))
			return false;
		if (getSuspensao() == null) {
			if (other.getSuspensao() != null)
				return false;
		} else if (!suspensao.equals(other.getSuspensao()))
			return false;
		if (getTextoLivre() == null) {
			if (other.getTextoLivre() != null)
				return false;
		} else if (!textoLivre.equals(other.getTextoLivre()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RelSuspensaoCondicao> getEntityClass() {
		return RelSuspensaoCondicao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdRelSuspensaoCondicao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
