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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_pena_concurso_crime")
@PrimaryKeyJoinColumn(name="id_pena_concurso_crime")
public class PenaConcursoCrime extends PenaIndividualizada {

	private static final long serialVersionUID = -8400961918145453980L;
	private ConcursoCrime concursoCrime;
	
	@NotNull
	@ManyToOne	
	@JoinColumn(name = "id_concurso_crime", nullable = false)
	public ConcursoCrime getConcursoCrime() {
		return concursoCrime;
	}

	public void setConcursoCrime(ConcursoCrime concursoCrime) {
		this.concursoCrime = concursoCrime;
	}

	@Transient
	@Override
	public String getDetalheDelito() {
		return getConcursoCrime().getTipoAgrupamento().getLabel() + " - "
				+ getConcursoCrime().getDelitosAssociadosString();
	}	

	@Override
	@Transient
	public Class<? extends Pena> getEntityClass() {
		return PenaConcursoCrime.class;
	}
}
