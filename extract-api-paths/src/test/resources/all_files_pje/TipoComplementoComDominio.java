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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

@Entity
@DiscriminatorValue(value = TipoComplemento.TIPO_COMPLEMENTO_COM_DOMINIO)
public class TipoComplementoComDominio extends TipoComplemento {

	private static final long serialVersionUID = 1L;

	private List<AplicacaoDominio> aplicacaoDominioList;

	public TipoComplementoComDominio() {
		super();
		this.aplicacaoDominioList = new ArrayList<AplicacaoDominio>();
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "tb_tipo_complement_dominio", joinColumns = { @JoinColumn(name = "id_tipo_complemento", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_aplicacao_dominio", nullable = false, updatable = false) })
	@ForeignKey(inverseName = "tb_aplicacao_dominio_fkey", name = "tb_tipo_complemento_fkey")
	public List<AplicacaoDominio> getAplicacaoDominioList() {
		return aplicacaoDominioList;
	}

	public void setAplicacaoDominioList(List<AplicacaoDominio> aplicacaoDominioList) {
		this.aplicacaoDominioList = aplicacaoDominioList;
	}

	@Transient
	@Override
	public Class<? extends TipoComplemento> getEntityClass() {
		return TipoComplementoComDominio.class;
	}
}