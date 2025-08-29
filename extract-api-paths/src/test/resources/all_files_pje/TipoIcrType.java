package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;

public class TipoIcrType extends EnumType<TipoIcrEnum> {

	private static final long serialVersionUID = 1L;

	public TipoIcrType() {
		super(TipoIcrEnum.IND);
	}

}
