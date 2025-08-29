package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.PrazoMinAnosMedidaSegurancaEnum;

/**
 * Tipo específio para prazo mínimo em anos a ser utilizado no cadastro do tipo
 * de Informação Criminal Relevante chamado Sentença Absolutória Imprópria
 * 
 * @author lucas.souza
 * 
 */
public class PrazoMinAnosMedidaSegurancaType extends EnumType<PrazoMinAnosMedidaSegurancaEnum> {

	private static final long serialVersionUID = 1L;

	public PrazoMinAnosMedidaSegurancaType() {
		super(PrazoMinAnosMedidaSegurancaEnum.ANO1);
	}

}
