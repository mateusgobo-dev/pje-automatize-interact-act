package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.PericiaStatusEnum;

public class PericiaStatusType extends EnumType<PericiaStatusEnum> {

	private static final long serialVersionUID = 1L;

	public PericiaStatusType() {
		super(PericiaStatusEnum.C);
	}

}
