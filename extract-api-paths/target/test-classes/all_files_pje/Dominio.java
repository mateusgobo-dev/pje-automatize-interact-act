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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = Dominio.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_dominio", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_dominio"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Dominio implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<Dominio,Long>{

	private static final long serialVersionUID = 2745232781113282352L;

	public static final String TABLE_NAME = "tb_dominio";

	private Long idDominio;
	private String nomeDominio;
	private String codigo;
	private Boolean ativo;
	private List<ElementoDominio> elementoDominioList;

	public Dominio() {
		this.elementoDominioList = new ArrayList<ElementoDominio>();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_dominio")
	@Column(name = "id_dominio", unique = true, nullable = false)
	public Long getIdDominio() {
		return idDominio;
	}

	public void setIdDominio(Long idDominio) {
		this.idDominio = idDominio;
	}

	@Column(name = "ds_dominio")
	public String getNomeDominio() {
		return nomeDominio;
	}

	public void setNomeDominio(String nomeDominio) {
		this.nomeDominio = nomeDominio;
	}

	@Column(name = "cd_glossario")
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	@Column(name = "in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "dominio")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public List<ElementoDominio> getElementoDominioList() {
		return elementoDominioList;
	}

	public void setElementoDominioList(List<ElementoDominio> elementoDominioList) {
		this.elementoDominioList = elementoDominioList;
	}
	
	@Override
	public String toString(){
		return nomeDominio;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Dominio> getEntityClass() {
		return Dominio.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdDominio();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
