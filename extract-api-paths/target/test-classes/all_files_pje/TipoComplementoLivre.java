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
@DiscriminatorValue(value = TipoComplemento.TIPO_COMPLEMENTO_LIVRE)
public class TipoComplementoLivre extends TipoComplemento {

	private static final long serialVersionUID = 1L;
	
	private Boolean temMascara = false;
	private String mascara;

	public TipoComplementoLivre() {
	}
	
	@Column(name = "in_mascara")
	public Boolean getTemMascara() {
		return temMascara;
	}

	public void setTemMascara(Boolean temMascara) {
		this.temMascara = temMascara;
	}

	@Column(name = "ds_mascara")
	public String getMascara() {
		return mascara;
	}

	public void setMascara(String mascara) {
		this.mascara = mascara;
	}

	@Transient
	@Override
	public Class<? extends TipoComplemento> getEntityClass() {
		return TipoComplementoLivre.class;
	}
}