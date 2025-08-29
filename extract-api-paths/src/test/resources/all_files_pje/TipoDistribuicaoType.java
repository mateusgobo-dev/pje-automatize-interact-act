package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoDistribuicaoEnum;

public class TipoDistribuicaoType extends EnumType<TipoDistribuicaoEnum> {

	private static final long serialVersionUID = 1L;

	public TipoDistribuicaoType() {
		super(TipoDistribuicaoEnum.S);
	}

}
