package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.SemanaEnum;

public class DiaSemanaType extends EnumType<SemanaEnum> {

	private static final long serialVersionUID = 1L;

	public DiaSemanaType() {
		super(SemanaEnum.SEG);
	}

}
