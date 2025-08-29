package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoMedidaSegurancaEnum;

/**
 * Tipo específico de Medida de Segurança a serem usados como referência para
 * cadastro de Informação Criminal Relevante chamado Sentença Absolutória
 * Imprópria
 * 
 * @author lucas.souza
 * 
 */
public class TipoMedidaSegurancaType extends EnumType<TipoMedidaSegurancaEnum> {

	private static final long serialVersionUID = 1L;

	public TipoMedidaSegurancaType() {
		super(TipoMedidaSegurancaEnum.INT);
	}

}
