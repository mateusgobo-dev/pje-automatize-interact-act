package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.MotivoEncerramentoPrisaoEnum;

public class MotivoEncerramentoPrisaoType extends EnumType<MotivoEncerramentoPrisaoEnum> {

	private static final long serialVersionUID = 1L;

	public MotivoEncerramentoPrisaoType() {
		super(MotivoEncerramentoPrisaoEnum.CP);
	}

}
