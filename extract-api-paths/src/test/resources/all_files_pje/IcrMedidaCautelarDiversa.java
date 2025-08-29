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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "tb_icr_mdda_cautlr_diversa")
@PrimaryKeyJoinColumn(name = "id_icr_medida_cautelar_diversa")
public class IcrMedidaCautelarDiversa extends InformacaoCriminalRelevante{

	private static final long serialVersionUID = -4718359855910670926L;
	private List<MedidaCautelarDiversa> medidasCautelaresDiversas = new ArrayList<MedidaCautelarDiversa>();

	public void setMedidasCautelaresDiversas(List<MedidaCautelarDiversa> medidasCautelaresDiversas){
		this.medidasCautelaresDiversas = medidasCautelaresDiversas;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "icr")
	// @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@OrderBy("tipo")
	public List<MedidaCautelarDiversa> getMedidasCautelaresDiversas(){
		return medidasCautelaresDiversas;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrMedidaCautelarDiversa.class;
	}
}
