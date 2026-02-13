package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoRedistribuicaoEnum;

public class TipoRedistribuicaoType extends EnumType<TipoRedistribuicaoEnum> {

	private static final long serialVersionUID = 1L;

	public TipoRedistribuicaoType() {
		super(TipoRedistribuicaoEnum.S);
	}

}
