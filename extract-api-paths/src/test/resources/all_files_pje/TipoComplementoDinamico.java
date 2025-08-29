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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = TipoComplemento.TIPO_COMPLEMENTO_DINAMICO)
public class TipoComplementoDinamico extends TipoComplemento {

	private static final long serialVersionUID = 1L;

	private String expressaoBusca;

	public TipoComplementoDinamico() {
	}

	@Column(name = "el_expressao_busca")
	public String getExpressaoBusca() {
		return expressaoBusca;
	}

	public void setExpressaoBusca(String expressaoBusca) {
		this.expressaoBusca = expressaoBusca;
	}

	@Transient
	@Override
	public Class<? extends TipoComplemento> getEntityClass() {
		return TipoComplementoDinamico.class;
	}
}