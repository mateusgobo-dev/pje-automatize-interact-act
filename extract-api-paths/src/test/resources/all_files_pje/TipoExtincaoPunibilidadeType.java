package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoExtincaoPunibilidadeEnum;

;

/**
 * Tipo específico de Medida de Segurança a serem usados como referência para
 * cadastro de Informação Criminal Relevante chamado Sentença de Extinção de
 * Punibilidade
 * 
 * Caso de Uso PJE_UC024 Regras: RD017
 * 
 * @author lucas.souza
 * 
 */
public class TipoExtincaoPunibilidadeType extends EnumType<TipoExtincaoPunibilidadeEnum> {

	private static final long serialVersionUID = 1L;

	public TipoExtincaoPunibilidadeType() {
		super(TipoExtincaoPunibilidadeEnum.ANI);
	}
}
