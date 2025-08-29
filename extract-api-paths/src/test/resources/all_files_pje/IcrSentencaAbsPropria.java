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
@Table(name = "tb_icr_sentnca_abs_propria")
@PrimaryKeyJoinColumn(name = "id_icr_sentenca_abs_propria")
public class IcrSentencaAbsPropria extends InformacaoCriminalRelevante implements Serializable {

	private static final long serialVersionUID = 1L;

	private PessoaMagistrado pessoaMagistrado;
	private Date dtPublicacao;

	public IcrSentencaAbsPropria() {
	}

	public IcrSentencaAbsPropria(InformacaoCriminalRelevante icr) {
		copiarPropriedadesIcr(icr);
	}

	private void copiarPropriedadesIcr(InformacaoCriminalRelevante icr) {
		try {
			BeanUtils.copyProperties(this, icr);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_pessoa_magistrado", nullable = false)
	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}
	
	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_publicacao", nullable = false)
	public Date getDtPublicacao() {
		return dtPublicacao;
	}

	public void setDtPublicacao(Date dtPublicacao) {
		this.dtPublicacao = dtPublicacao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getDtPublicacao() == null) ? 0 : dtPublicacao.hashCode());
		result = prime * result + ((getPessoaMagistrado() == null) ? 0 : pessoaMagistrado.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof IcrSentencaAbsPropria))
			return false;
		IcrSentencaAbsPropria other = (IcrSentencaAbsPropria) obj;
		if (getDtPublicacao() == null) {
			if (other.getDtPublicacao() != null)
				return false;
		} else if (!dtPublicacao.equals(other.getDtPublicacao()))
			return false;
		if (getPessoaMagistrado() == null) {
			if (other.getPessoaMagistrado() != null)
				return false;
		} else if (!pessoaMagistrado.equals(other.getPessoaMagistrado()))
			return false;
		return true;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrSentencaAbsPropria.class;
	}
}
