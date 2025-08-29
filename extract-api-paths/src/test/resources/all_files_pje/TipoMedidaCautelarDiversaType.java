package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoMedidaCautelarDiversaEnum;

public class TipoMedidaCautelarDiversaType extends EnumType<TipoMedidaCautelarDiversaEnum> {
	private static final long serialVersionUID = 5187320858452127691L;

	public TipoMedidaCautelarDiversaType(Enum<TipoMedidaCautelarDiversaEnum> type) {
		super(type);
	}

	public TipoMedidaCautelarDiversaType() {
		super(TipoMedidaCautelarDiversaEnum.CPP319I);
	}
}
