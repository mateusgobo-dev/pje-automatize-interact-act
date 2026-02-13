/**
 * ProcessoAssuntoParaAssuntoProcessualConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import br.jus.cnj.intercomunicacao.v222.beans.AssuntoProcessual;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;

/**
 * Conversor de ProcessoAssunto para AssuntoProcessual.
 * 
 * @author Adriano Pamplona
 */
public class ProcessoAssuntoParaAssuntoProcessualConverter
		extends
		IntercomunicacaoConverterAbstrato<ProcessoAssunto, AssuntoProcessual> {

	@Override
	public AssuntoProcessual converter(ProcessoAssunto assunto) {
		AssuntoProcessual resultado = null;
		
		if (isNotNull(assunto)) {
			resultado = new AssuntoProcessual();
			resultado.setCodigoNacional(obterCodigoNacional(assunto));
			resultado.setPrincipal(assunto.getAssuntoPrincipal());
		}
		return resultado;
	}

	/**
	 * @param assunto
	 * @return código nacional.
	 */
	protected Integer obterCodigoNacional(ProcessoAssunto assunto) {
		AssuntoTrf assuntoTrf = assunto.getAssuntoTrf();
		return (isNotNull(assuntoTrf)?converterParaInt(assuntoTrf.getCodAssuntoTrf()): null);
	}
}
