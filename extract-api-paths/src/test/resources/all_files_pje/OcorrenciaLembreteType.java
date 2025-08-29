package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.OcorrenciaLembreteEnum;

public class OcorrenciaLembreteType extends EnumType<OcorrenciaLembreteEnum> {

	private static final long serialVersionUID = 1L;

	public OcorrenciaLembreteType() {
		super(OcorrenciaLembreteEnum.DIA);
	}
}
