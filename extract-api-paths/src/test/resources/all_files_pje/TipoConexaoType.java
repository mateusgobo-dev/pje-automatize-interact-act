package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;

public class TipoConexaoType extends EnumType<TipoConexaoEnum> {

	private static final long serialVersionUID = 1L;

	public TipoConexaoType() {
		super(TipoConexaoEnum.DP);
	}

}
