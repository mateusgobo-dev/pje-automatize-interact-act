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
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.BeanUtils;

@Entity
@Table(name = "tb_icr_desclassificacao")
@PrimaryKeyJoinColumn(name = "id_icr_desclassificacao")
public class IcrDesclassificacao extends InformacaoCriminalRelevante implements Serializable{

	private static final long serialVersionUID = -3858730085785620778L;
	private PessoaMagistrado julgador;
	private Date dataPublicacao;

	public IcrDesclassificacao(){
	}

	public IcrDesclassificacao(InformacaoCriminalRelevante instance){
		copiarPropriedades(instance);
	}

	private void copiarPropriedades(InformacaoCriminalRelevante icr){
		try{
			BeanUtils.copyProperties(this, icr);
			icr.setIcrProcessoEventoList(null);
		} catch (IllegalAccessException e){
			e.printStackTrace();
		} catch (InvocationTargetException e){
			e.printStackTrace();
		}
	}

	public void setJulgador(PessoaMagistrado julgador){
		this.julgador = julgador;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_julgador", nullable = false)
	public PessoaMagistrado getJulgador(){
		return julgador;
	}

	public void setDataPublicacao(Date dataPublicacao){
		this.dataPublicacao = dataPublicacao;
	}

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_publicacao", nullable = false)
	public Date getDataPublicacao(){
		return dataPublicacao;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrDesclassificacao.class;
	}
}
