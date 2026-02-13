package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.NaturezaCreditoEnum;

public class NaturezaCreditoType extends EnumType<NaturezaCreditoEnum> {

	private static final long serialVersionUID = 1L;

	public NaturezaCreditoType() {
		super(NaturezaCreditoEnum.C);
	}

}