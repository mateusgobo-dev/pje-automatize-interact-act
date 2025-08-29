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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_contra_mandado")
@PrimaryKeyJoinColumn(name = "id_contra_mandado")
public class ContraMandado extends ProcessoExpedienteCriminal {

	private static final long serialVersionUID = 1L;

	private MandadoPrisao mandadoPrisao;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_mandado_prisao", nullable = false)
	public MandadoPrisao getMandadoPrisao() {
		return mandadoPrisao;
	}

	public void setMandadoPrisao(MandadoPrisao mandadoPrisao) {
		this.mandadoPrisao = mandadoPrisao;
	}

	@Override
	@Transient
	public Class<? extends ProcessoExpedienteCriminal> getEntityClass() {
		return ContraMandado.class;
	}
}
