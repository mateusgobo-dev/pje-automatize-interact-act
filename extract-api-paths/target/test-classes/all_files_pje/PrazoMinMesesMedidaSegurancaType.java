package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.PrazoMinMesesMedidaSegurancaEnum;

/**
 * Tipo específio para prazo mínimo em meses a ser utilizado no cadastro do tipo
 * de Informação Criminal Relevante chamado Sentença Absolutória Imprópria
 * 
 * @author lucas.souza
 * 
 */
public class PrazoMinMesesMedidaSegurancaType extends EnumType<PrazoMinMesesMedidaSegurancaEnum> {

	private static final long serialVersionUID = 1L;

	public PrazoMinMesesMedidaSegurancaType() {
		super(PrazoMinMesesMedidaSegurancaEnum.MES1);
	}
}
