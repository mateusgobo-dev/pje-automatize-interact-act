package br.jus.csjt.pje.commons.model.type;

import br.com.itx.type.EnumType;
import br.jus.pje.jt.enums.ObrigacaoFazerEnum;

public class ObrigacaoFazerType extends EnumType<ObrigacaoFazerEnum> {

	private static final long serialVersionUID = 1L;

	public ObrigacaoFazerType() {
		super(ObrigacaoFazerEnum.ACP);
	}

}
