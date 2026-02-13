package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.SegredoStatusEnum;

public class SegredoStatusType extends EnumType<SegredoStatusEnum> {

	private static final long serialVersionUID = 1L;

	public SegredoStatusType() {
		super(SegredoStatusEnum.C);
	}

}
