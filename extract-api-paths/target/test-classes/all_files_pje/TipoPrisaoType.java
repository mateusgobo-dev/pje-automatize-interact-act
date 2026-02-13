package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoPrisaoEnum;

public class TipoPrisaoType extends EnumType<TipoPrisaoEnum> {

	private static final long serialVersionUID = 1L;

	public TipoPrisaoType() {
		super(TipoPrisaoEnum.PRV);
	}

}