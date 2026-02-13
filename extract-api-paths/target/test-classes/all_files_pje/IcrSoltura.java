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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.apache.commons.beanutils.BeanUtils;

import br.jus.pje.nucleo.enums.TipoSolturaEnum;

@Entity
@Table(name = "tb_icr_soltura")
@PrimaryKeyJoinColumn(name = "id_icr_soltura")
public class IcrSoltura extends InformacaoCriminalRelevante implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private TipoSolturaEnum inTipoSoltura;
	private IcrPrisao icrPrisao;

	public IcrSoltura() {
		//
	}

	public IcrSoltura(Integer id) {
		super.setId(id);
	}

	public IcrSoltura(InformacaoCriminalRelevante icr) {
		copiarPropriedades(icr);
	}

	public void copiarPropriedades(InformacaoCriminalRelevante icr) {
		try {
			BeanUtils.copyProperties(this, icr);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_icr_prisao", nullable = false)
	public IcrPrisao getIcrPrisao() {
		return icrPrisao;
	}

	public void setIcrPrisao(IcrPrisao icrPrisao) {
		this.icrPrisao = icrPrisao;
	}

	@Column(name = "in_tipo_soltura", length = 3)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.TipoSolturaType")
	public TipoSolturaEnum getInTipoSoltura() {
		return inTipoSoltura;
	}

	public void setInTipoSoltura(TipoSolturaEnum inTipoSoltura) {
		this.inTipoSoltura = inTipoSoltura;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getIcrPrisao() == null) ? 0 : icrPrisao.hashCode());
		result = prime * result + ((getInTipoSoltura() == null) ? 0 : inTipoSoltura.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (obj instanceof IcrSoltura)
			return false;
		IcrSoltura other = (IcrSoltura) obj;
		if (getIcrPrisao() == null) {
			if (other.getIcrPrisao() != null)
				return false;
		} else if (!icrPrisao.equals(other.getIcrPrisao()))
			return false;
		if (getInTipoSoltura() == null) {
			if (other.getInTipoSoltura() != null)
				return false;
		} else if (!inTipoSoltura.equals(other.getInTipoSoltura()))
			return false;
		return true;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrSoltura.class;
	}
}
