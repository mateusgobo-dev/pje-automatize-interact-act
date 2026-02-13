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

import java.lang.reflect.InvocationTargetException;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.apache.commons.beanutils.BeanUtils;

@Entity
@Table(name = "tb_icr_fuga")
@PrimaryKeyJoinColumn(name = "id_icr")
public class IcrFuga extends InformacaoCriminalRelevante {

	private static final long serialVersionUID = 1L;

	private IcrPrisao icrPrisao;

	public IcrFuga() {
	}

	public IcrFuga(InformacaoCriminalRelevante icr) {
		copiarPropriedadesIcr(icr);
	}

	// ------ UTILS --------------------------------------------------//

	private void copiarPropriedadesIcr(InformacaoCriminalRelevante icr) {
		try {
			BeanUtils.copyProperties(this, icr);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	// ------ GETTERS and SETTERS ------------------------------------//

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_icr_prisao", nullable = false)
	public IcrPrisao getIcrPrisao() {
		return icrPrisao;
	}

	public void setIcrPrisao(IcrPrisao idPrisao) {
		this.icrPrisao = idPrisao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getIcrPrisao() == null) ? 0 : icrPrisao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof IcrFuga))
			return false;
		IcrFuga other = (IcrFuga) obj;
		if (getIcrPrisao() == null) {
			if (other.getIcrPrisao() != null)
				return false;
		} else if (!icrPrisao.equals(other.getIcrPrisao()))
			return false;
		return true;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrFuga.class;
	}
}
