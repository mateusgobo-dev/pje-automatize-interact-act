package br.com.infox.ibpm.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoExpedienteEnum;

/**
 * @author Haroldo de Lima Arouca
 * @since @since 1.4.2Jt
 * @created 2011-08-31
 * @category PJE-JT
 */

public class TipoExpedienteType extends EnumType<TipoExpedienteEnum> {

	private static final long serialVersionUID = 1L;

	public TipoExpedienteType() {
		super(TipoExpedienteEnum.M);
	}
}
