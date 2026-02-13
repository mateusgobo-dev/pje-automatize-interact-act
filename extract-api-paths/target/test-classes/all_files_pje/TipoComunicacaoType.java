package br.com.infox.ibpm.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoComunicacaoEnum;

/**
 * @author Haroldo de Lima Arouca
 * @since @since 1.4.2Jt
 * @created 2011-08-30
 * @category PJE-JT
 */

public class TipoComunicacaoType extends EnumType<TipoComunicacaoEnum> {

	private static final long serialVersionUID = 1L;

	public TipoComunicacaoType() {
		super(TipoComunicacaoEnum.D);
	}
}
