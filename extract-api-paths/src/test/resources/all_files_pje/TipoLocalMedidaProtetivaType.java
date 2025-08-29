package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoLocalMedidaProtetivaEnum;

public class TipoLocalMedidaProtetivaType extends
		EnumType<TipoLocalMedidaProtetivaEnum> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TipoLocalMedidaProtetivaType(Enum<TipoLocalMedidaProtetivaEnum> type) {
		super(type);
	}

	public TipoLocalMedidaProtetivaType() {
		super(TipoLocalMedidaProtetivaEnum.BAR);
	}

}
