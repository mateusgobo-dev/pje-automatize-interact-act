package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoMedidaProtetivaUrgenciaEnum;

public class TipoMedidaProtetivaUrgenciaType extends
		EnumType<TipoMedidaProtetivaUrgenciaEnum> {

	private static final long serialVersionUID = 7849999217092481736L;

	public TipoMedidaProtetivaUrgenciaType(
			Enum<TipoMedidaProtetivaUrgenciaEnum> type) {
		super(type);
	}

	public TipoMedidaProtetivaUrgenciaType() {
		super(TipoMedidaProtetivaUrgenciaEnum.MPArt22I);
	}
}
