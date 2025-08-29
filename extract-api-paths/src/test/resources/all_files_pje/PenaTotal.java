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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.jus.pje.nucleo.enums.RegimePenaEnum;

@Entity
@Table(name = "tb_pena_total")
@PrimaryKeyJoinColumn(name="id_pena_total")
public class PenaTotal extends Pena{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2405690344405691042L;
	
	private RegimePenaEnum regimePena = RegimePenaEnum.A;

	@Column(name = "in_regime_pena")
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.RegimePenaType")
	public RegimePenaEnum getRegimePena() {
		return regimePena;
	}

	public void setRegimePena(RegimePenaEnum regimePena) {
		this.regimePena = regimePena;
	}

	@Transient
	@Override
	public Class<? extends Pena> getEntityClass() {
		return PenaTotal.class;
	}
}
