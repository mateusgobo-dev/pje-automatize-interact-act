package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

public class ProcessoStatusType extends EnumType<ProcessoStatusEnum> {

	private static final long serialVersionUID = 1L;

	public ProcessoStatusType() {
		super(ProcessoStatusEnum.E);
	}

}
