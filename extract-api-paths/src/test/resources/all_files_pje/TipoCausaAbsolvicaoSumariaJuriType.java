package br.com.infox.cliente.type;

import br.com.itx.type.EnumType;
import br.jus.pje.nucleo.enums.TipoCausaAbsolvicaoSumariaJuriEnum;

/**
 * Enum para Tipo Causa da Absolvicao Sumária do Júria serem usados como
 * referência para cadastro de de Informação Criminal Relevante do tipo Sentença
 * Absolvição Sumária
 * 
 * Caso de uso PJE_UC047
 * 
 * @author kledson.oliveira
 * 
 */
public class TipoCausaAbsolvicaoSumariaJuriType extends EnumType<TipoCausaAbsolvicaoSumariaJuriEnum> {

	private static final long serialVersionUID = 1L;

	public TipoCausaAbsolvicaoSumariaJuriType() {
		super(TipoCausaAbsolvicaoSumariaJuriEnum.DIP);
	}

}
