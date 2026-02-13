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
@Table(name = "tb_icr_sent_impronuncia")
@PrimaryKeyJoinColumn(name = "id_icr_sentenca_impronuncia")
public class IcrSentencaImpronuncia extends InformacaoCriminalRelevante implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	private PessoaMagistrado orgao;
	private Date dtPublicacao;

	public IcrSentencaImpronuncia(){
		//
	}

	public IcrSentencaImpronuncia(InformacaoCriminalRelevante icr){
		copiarPropriedadesIcr(icr);
	}

	private void copiarPropriedadesIcr(InformacaoCriminalRelevante icr){
		try{
			BeanUtils.copyProperties(this, icr);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void setIcr(InformacaoCriminalRelevante icr){
		copiarPropriedadesIcr(icr);
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_orgao", nullable = false)
	public PessoaMagistrado getOrgao(){
		return orgao;
	}

	public void setOrgao(PessoaMagistrado orgao){
		this.orgao = orgao;
	}

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_publicacao", nullable = false)
	public Date getDtPublicacao(){
		return dtPublicacao;
	}

	public void setDtPublicacao(Date dtPublicacao){
		this.dtPublicacao = dtPublicacao;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getDtPublicacao() == null) ? 0 : dtPublicacao.hashCode());
		result = prime * result + ((getOrgao() == null) ? 0 : orgao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (obj instanceof IcrSentencaImpronuncia)
			return false;
		IcrSentencaImpronuncia other = (IcrSentencaImpronuncia) obj;
		if (getDtPublicacao() == null){
			if (other.getDtPublicacao() != null)
				return false;
		}
		else if (!dtPublicacao.equals(other.getDtPublicacao()))
			return false;
		if (getOrgao() == null){
			if (other.getOrgao() != null)
				return false;
		}
		else if (!orgao.equals(other.getOrgao()))
			return false;
		return true;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrSentencaImpronuncia.class;
	}
}
