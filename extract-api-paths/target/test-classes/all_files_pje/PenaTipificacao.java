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
@Table(name = "tb_pena_tipificacao")
@PrimaryKeyJoinColumn(name="id_pena_tipificacao")
public class PenaTipificacao extends PenaIndividualizada {

	private static final long serialVersionUID = -4911807302091935040L;
	private TipificacaoDelito tipificacaoDelito;	

	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_tipificacao_delito", nullable = false)
	public TipificacaoDelito getTipificacaoDelito() {
		return tipificacaoDelito;
	}

	public void setTipificacaoDelito(TipificacaoDelito tipificacaoDelito) {
		this.tipificacaoDelito = tipificacaoDelito;
	}

	@Transient
	@Override
	public String getDetalheDelito() {
		return "(" + getTipificacaoDelito().getNumeroReferencia() + ") " + getTipificacaoDelito().getDelitoString();
	}
	
	@Transient
	@Override
	public Class<? extends Pena> getEntityClass() {
		return PenaTipificacao.class;
	}
}
