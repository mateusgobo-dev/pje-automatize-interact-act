package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoPessoaContatoEnum;

public class TipoPessoaContatoType extends EnumType<TipoPessoaContatoEnum> {

	private static final long serialVersionUID = 1L;

	public TipoPessoaContatoType() {
		super(TipoPessoaContatoEnum.A);
	}
}
