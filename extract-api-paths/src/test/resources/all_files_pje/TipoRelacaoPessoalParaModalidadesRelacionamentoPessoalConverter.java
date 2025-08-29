/**
 * TipoRelacaoPessoalParaModalidadesRelacionamentoPessoalConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.HashMap;
import java.util.Map;

import br.jus.cnj.intercomunicacao.v222.beans.ModalidadesRelacionamentoPessoal;
import br.jus.pje.nucleo.entidades.TipoRelacaoPessoal;

/**
 * Conversor de TipoRelacaoPessoal para ModalidadesRelacionamentoPessoal.
 * 
 * @author Adriano Pamplona
 */
public class TipoRelacaoPessoalParaModalidadesRelacionamentoPessoalConverter
		extends
		IntercomunicacaoConverterAbstrato<TipoRelacaoPessoal, ModalidadesRelacionamentoPessoal> {

	@Override
	public ModalidadesRelacionamentoPessoal converter(TipoRelacaoPessoal tipo) {
		ModalidadesRelacionamentoPessoal resultado = null;
		
		if (isNotNull(tipo)) {
			Map<String ,ModalidadesRelacionamentoPessoal> mapa = new HashMap<String, ModalidadesRelacionamentoPessoal>();
			mapa.put("TUT", ModalidadesRelacionamentoPessoal.T);
			mapa.put("CUR", ModalidadesRelacionamentoPessoal.C);
			
			resultado = mapa.get(tipo.getCodigo());
		}
		return resultado;
	}
}
