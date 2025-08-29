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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_condicao_suspensao")
@org.hibernate.annotations.GenericGenerator(name = "gen_condicao_suspensao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_condicao_suspensao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CondicaoSuspensao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<CondicaoSuspensao,Integer>{

	private static final long serialVersionUID = -1308923580398204115L;
	private Integer id;
	private String descricao;
	private Boolean campoTextoLivre = false;
	private TipoSuspensao tipoSuspensao;
	private Boolean ativo = true;
	private List<CondicaoSuspensaoAssociada> condicaoSuspensaoAssociadaList = new ArrayList<CondicaoSuspensaoAssociada>(
			0);

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "condicaoSuspensao", cascade = CascadeType.ALL)
	public List<CondicaoSuspensaoAssociada> getCondicaoSuspensaoAssociadaList(){
		return condicaoSuspensaoAssociadaList;
	}

	public void setCondicaoSuspensaoAssociadaList(
			List<CondicaoSuspensaoAssociada> condicaoSuspensaoAssociadaList){
		this.condicaoSuspensaoAssociadaList = condicaoSuspensaoAssociadaList;
	}

	@Id
	@GeneratedValue(generator = "gen_condicao_suspensao")
	@Column(name = "id_condicao_suspensao", unique = true, nullable = false)
	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	@NotNull
	@Column(name = "ds_condicao_suspensao", nullable = false)
	public String getDescricao(){
		return descricao;
	}

	public void setDescricao(String descricao){
		this.descricao = descricao;
	}

	@NotNull
	@Column(name = "in_campo_texto_livre")
	public Boolean getCampoTextoLivre(){
		return this.campoTextoLivre;
	}

	@Transient
	public Boolean getIsCampoTextoLivre(){
		return getCampoTextoLivre();
	}

	public void setCampoTextoLivre(Boolean campoTextoLivre){
		this.campoTextoLivre = campoTextoLivre;
	}

	@ManyToOne
	@JoinColumn(name = "id_tipo_suspensao", nullable = false)
	public TipoSuspensao getTipoSuspensao(){
		return tipoSuspensao;
	}

	public void setTipoSuspensao(TipoSuspensao tipoSuspensao){
		this.tipoSuspensao = tipoSuspensao;
	}

	@NotNull
	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo(){
		return ativo;
	}

	public void setAtivo(Boolean ativo){
		this.ativo = ativo;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ativo == null) ? 0 : ativo.hashCode());
		result = prime * result
				+ ((descricao == null) ? 0 : descricao.hashCode());
		result = prime * result
				+ ((tipoSuspensao == null) ? 0 : tipoSuspensao.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CondicaoSuspensao other = (CondicaoSuspensao) obj;
		if (ativo == null) {
			if (other.ativo != null)
				return false;
		} else if (!ativo.equals(other.ativo))
			return false;
		if (descricao == null) {
			if (other.descricao != null)
				return false;
		} else if (!descricao.equals(other.descricao))
			return false;
		if (tipoSuspensao == null) {
			if (other.tipoSuspensao != null)
				return false;
		} else if (!tipoSuspensao.equals(other.tipoSuspensao))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CondicaoSuspensao> getEntityClass() {
		return CondicaoSuspensao.class;
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
