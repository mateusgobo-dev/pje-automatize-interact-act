package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.ConciliacaoEnum;

public class ConciliacaoType extends EnumType<ConciliacaoEnum> {

	private static final long serialVersionUID = 1L;

	public ConciliacaoType() {
		super(ConciliacaoEnum.CC);
	}

}
