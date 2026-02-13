/**
 * AssuntoProcessualParaProcessoAssuntoConverter.java
 * 
 * Data de criação: 10/08/2020
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.AssuntoProcessual;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;

/**
 * Conversor de AssuntoProcessual para ProcessoAssunto.
 * 
 * @author Adriano Pamplona
 */
@Name (AssuntoProcessualParaProcessoAssuntoConverter.NAME)
public class AssuntoProcessualParaProcessoAssuntoConverter
		extends
		IntercomunicacaoConverterAbstrato<AssuntoProcessual, ProcessoAssunto> {

	public static final String NAME = "v222.assuntoProcessualParaProcessoAssuntoConverter";
	
	/**
	 * @return Instância de AssuntoProcessualParaProcessoAssuntoConverter.
	 */
	public static AssuntoProcessualParaProcessoAssuntoConverter instance() {
		return ComponentUtil.getComponent(AssuntoProcessualParaProcessoAssuntoConverter.class);
	}
	
	@Override
	public ProcessoAssunto converter(AssuntoProcessual assunto) {
		ProcessoAssunto resultado = null;
		
		if (isNotNull(assunto)) {
			AssuntoTrf assuntoTrf = new AssuntoTrf();
			assuntoTrf.setAssuntoTrf(converterParaString(assunto.getCodigoNacional()));
			
			resultado = new ProcessoAssunto();
			resultado.setAssuntoTrf(assuntoTrf);
		}
		return resultado;
	}
}
