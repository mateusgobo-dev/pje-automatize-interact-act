package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoSolturaEnum;

public class TipoSolturaType extends EnumType<TipoSolturaEnum> {

	private static final long serialVersionUID = 1L;

	public TipoSolturaType() {
		super(TipoSolturaEnum.RLX);
	}

}
