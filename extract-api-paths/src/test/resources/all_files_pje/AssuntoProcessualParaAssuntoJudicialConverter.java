/**
 * AssuntoProcessualParaAssuntoJudicialConverter.java
 * 
 * Data de criação: 17/12/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import br.jus.cnj.intercomunicacao.v222.beans.AssuntoProcessual;
import br.jus.cnj.pje.ws.AssuntoJudicial;

/**
 * Conversor de AssuntoProcessual para AssuntoJudicial.
 * 
 * @author Adriano Pamplona
 */
public class AssuntoProcessualParaAssuntoJudicialConverter
		extends
		IntercomunicacaoConverterAbstrato<AssuntoProcessual, AssuntoJudicial> {

	@Override
	public AssuntoJudicial converter(AssuntoProcessual assunto) {
		AssuntoJudicial resultado = null;
		
		if (isNotNull(assunto)) {
			resultado = new AssuntoJudicial();
			resultado.setCodigo(converterParaString(assunto.getCodigoNacional()));
		}
		return resultado;
	}
}
