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
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;

@Entity
@Table(name = "tb_icr_med_prttva_urgencia")
@PrimaryKeyJoinColumn(name = "id_icr_mdda_protetiva_urgencia")
public class IcrMedidaProtetivaUrgencia extends InformacaoCriminalRelevante{

	private static final long serialVersionUID = -3140061110144983223L;
	private List<MedidaProtetivaUrgencia> medidasProtetivasUrgencia = new ArrayList<MedidaProtetivaUrgencia>();

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "icr")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public List<MedidaProtetivaUrgencia> getMedidasProtetivasUrgencia(){
		return medidasProtetivasUrgencia;
	}

	public void setMedidasProtetivasUrgencia(
			List<MedidaProtetivaUrgencia> medidasProtetivasUrgencia){
		this.medidasProtetivasUrgencia = medidasProtetivasUrgencia;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrMedidaProtetivaUrgencia.class;
	}
}
