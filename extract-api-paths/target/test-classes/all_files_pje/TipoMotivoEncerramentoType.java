package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.MotivoEncerramentoSuspensaoEnum;

public class TipoMotivoEncerramentoType extends EnumType<MotivoEncerramentoSuspensaoEnum> {

	private static final long serialVersionUID = 1L;

	public TipoMotivoEncerramentoType() {
		super(MotivoEncerramentoSuspensaoEnum.OBG);
	}

}
