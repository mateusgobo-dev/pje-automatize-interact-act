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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_cond_suspensa_associada")
@org.hibernate.annotations.GenericGenerator(name = "gen_cndco_suspnsao_associada", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_cndco_suspnsao_associada"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CondicaoSuspensaoAssociada implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<CondicaoSuspensaoAssociada
,Integer>{

	private static final long serialVersionUID = -1308923580398204115L;
	private Integer id;
	private IcrSuspensao icrSuspensao;
	private CondicaoSuspensao condicaoSuspensao;
	private Boolean ativo = true;
	private String textoLivre;
	private List<AcompanhamentoCondicaoSuspensao> acompanhamentoCondicaoSuspensaoList = new ArrayList<AcompanhamentoCondicaoSuspensao>(0);

	public CondicaoSuspensaoAssociada(){

	}

	public CondicaoSuspensaoAssociada(IcrSuspensao icrSuspensao){
		this.icrSuspensao = icrSuspensao;
	}

	@Id
	@GeneratedValue(generator = "gen_cndco_suspnsao_associada")
	@Column(name = "id_condcao_suspensao_associada", nullable = false)
	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name = "id_icr_suspensao", nullable = false)
	public IcrSuspensao getIcrSuspensao(){
		return icrSuspensao;
	}

	public void setIcrSuspensao(IcrSuspensao icrSuspensao){
		this.icrSuspensao = icrSuspensao;
	}

	@ManyToOne
	@JoinColumn(name = "id_condicao_suspensao", nullable = false)
	public CondicaoSuspensao getCondicaoSuspensao(){
		return condicaoSuspensao;
	}

	public void setCondicaoSuspensao(CondicaoSuspensao condicaoSuspensao){
		this.condicaoSuspensao = condicaoSuspensao;
	}

	@OneToMany(mappedBy = "condicaoSuspensaoAssociada", cascade = CascadeType.ALL)
	@OrderBy("numeroTarefa")
	public List<AcompanhamentoCondicaoSuspensao> getAcompanhamentoCondicaoSuspensaoList(){
		return acompanhamentoCondicaoSuspensaoList;
	}

	public void setAcompanhamentoCondicaoSuspensaoList(
			List<AcompanhamentoCondicaoSuspensao> acompanhamentoCondicaoSuspensaoList){
		this.acompanhamentoCondicaoSuspensaoList = acompanhamentoCondicaoSuspensaoList;
	}

	@NotNull
	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo(){
		return ativo;
	}

	public void setAtivo(Boolean ativo){
		this.ativo = ativo;
	}

	@Column(name = "ds_texto_livre")
	public String getTextoLivre(){
		return textoLivre;
	}

	public void setTextoLivre(String textoLivre){
		this.textoLivre = textoLivre;
	}

	@Transient
	public String getDescricao(){
		if (getCondicaoSuspensao() != null && !getCondicaoSuspensao().getIsCampoTextoLivre()){
			return getCondicaoSuspensao().getDescricao();
		}
		else
			return getTextoLivre();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CondicaoSuspensaoAssociada> getEntityClass() {
		return CondicaoSuspensaoAssociada.class;
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
