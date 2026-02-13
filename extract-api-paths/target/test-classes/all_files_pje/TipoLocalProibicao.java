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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tb_tipo_local_proibicao")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_local_proibicao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_local_proibicao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoLocalProibicao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoLocalProibicao,Integer>, Comparable<TipoLocalProibicao>{

	private static final long serialVersionUID = 3708616021434906572L;
	private Integer idTipoLocalProibicao;
	private String descricao;
	private Boolean inAtivo = true;
	private List<MedidaCautelarDiversa> medidaCautelarDiversa = new ArrayList<MedidaCautelarDiversa>(0);

	@Id
	@GeneratedValue(generator = "gen_tipo_local_proibicao")
	@Column(name = "id_tipo_local_proibicao", unique = true, nullable = false)
	public Integer getIdTipoLocalProibicao(){
		return idTipoLocalProibicao;
	}

	public void setIdTipoLocalProibicao(Integer id){
		this.idTipoLocalProibicao = id;
	}

	public TipoLocalProibicao(){
	}

	public TipoLocalProibicao(String descricao){
		this.idTipoLocalProibicao = descricao.hashCode();
		this.descricao = descricao;
		this.inAtivo = true;
	}

	@ManyToMany(cascade = CascadeType.ALL, mappedBy = "tipoLocalProibicaoList")
	public List<MedidaCautelarDiversa> getMedidaCautelarDiversa(){
		return medidaCautelarDiversa;
	}

	public void setMedidaCautelarDiversa(List<MedidaCautelarDiversa> medidaCautelarDiversa){
		this.medidaCautelarDiversa =
				medidaCautelarDiversa;
	}

	public void setDescricao(String descricao){
		this.descricao = descricao;
	}

	@Column(name = "ds_tipo_local_proibicao", unique = true, nullable = false)
	public String getDescricao(){
		return descricao;
	}

	public void setInAtivo(Boolean inAtivo){
		this.inAtivo = inAtivo;
	}

	@Column(name = "in_ativo", nullable = false)
	public Boolean getInAtivo(){
		return inAtivo;
	}

	@Override
	public int compareTo(TipoLocalProibicao o){
		return getDescricao().compareTo(o.getDescricao());
	}

	@Override
	public String toString(){
		return getDescricao();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoLocalProibicao> getEntityClass() {
		return TipoLocalProibicao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdTipoLocalProibicao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
