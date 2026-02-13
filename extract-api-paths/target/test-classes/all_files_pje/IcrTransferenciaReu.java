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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.BeanUtils;

@Entity
@Table(name = "tb_icr_transferencia_reu")
@PrimaryKeyJoinColumn(name = "id_icr_transferencia_reu")
public class IcrTransferenciaReu extends InformacaoCriminalRelevante implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3230208720288031554L;
	private IcrPrisao icrPrisao;
	private EstabelecimentoPrisional estabelecimentoPrisional;

	public IcrTransferenciaReu() {

	}

	public IcrTransferenciaReu(InformacaoCriminalRelevante icr) {
		copiarPropriedadesIcr(icr);
	}

	public IcrTransferenciaReu(IcrPrisao icrPrisao, EstabelecimentoPrisional estabelecimentoPrisional) {
		this.icrPrisao = icrPrisao;
		this.estabelecimentoPrisional = estabelecimentoPrisional;
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

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_icr_prisao", nullable = false)
	@NotNull
	public IcrPrisao getIcrPrisao() {
		return icrPrisao;
	}

	public void setIcrPrisao(IcrPrisao icrPrisao) {
		this.icrPrisao = icrPrisao;
	}

	@NotNull
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_estabelecimento_prisional", nullable = false)
	public EstabelecimentoPrisional getEstabelecimentoPrisional() {
		return estabelecimentoPrisional;
	}

	public void setEstabelecimentoPrisional(EstabelecimentoPrisional estabelecimentoPrisional) {
		this.estabelecimentoPrisional = estabelecimentoPrisional;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getEstabelecimentoPrisional() == null) ? 0 : estabelecimentoPrisional.hashCode());
		result = prime * result + ((getIcrPrisao() == null) ? 0 : icrPrisao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof IcrTransferenciaReu))
			return false;
		IcrTransferenciaReu other = (IcrTransferenciaReu) obj;
		if (getEstabelecimentoPrisional() == null) {
			if (other.getEstabelecimentoPrisional() != null)
				return false;
		} else if (!estabelecimentoPrisional.equals(other.getEstabelecimentoPrisional()))
			return false;
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
		return IcrTransferenciaReu.class;
	}
}
