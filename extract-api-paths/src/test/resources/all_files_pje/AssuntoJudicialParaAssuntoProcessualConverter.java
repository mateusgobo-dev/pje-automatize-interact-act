/**
 * AssuntoJudicialParaAssuntoProcessualConverter.java
 * 
 * Data de criação: 17/12/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.List;

import br.jus.cnj.intercomunicacao.v222.beans.AssuntoProcessual;
import br.jus.cnj.pje.ws.AssuntoJudicial;

/**
 * Conversor de AssuntoJudicial para AssuntoProcessual.
 * 
 * @author Adriano Pamplona
 */
public class AssuntoJudicialParaAssuntoProcessualConverter
		extends
		IntercomunicacaoConverterAbstrato<AssuntoJudicial, AssuntoProcessual> {

	private int indiceLoop = 0;
	
	@Override
	public AssuntoProcessual converter(AssuntoJudicial assunto) {
		AssuntoProcessual resultado = null;
		
		if (isNotNull(assunto)) {
			resultado = new AssuntoProcessual();
			resultado.setCodigoNacional(converterParaInt(assunto.getCodigo()));
			resultado.setPrincipal(getIndiceLoop() == 0);
			
			setIndiceLoop(getIndiceLoop() + 1);
		}
		return resultado;
	}
	
	@Override
	public List<AssuntoProcessual> converterColecao(List<AssuntoJudicial> colecaoObjeto) {
		setIndiceLoop(0);
		return super.converterColecao(colecaoObjeto);
	}

	/**
	 * @return the indiceLoop
	 */
	protected int getIndiceLoop() {
		return indiceLoop;
	}

	/**
	 * @param indiceLoop the indiceLoop to set
	 */
	protected void setIndiceLoop(int indiceLoop) {
		this.indiceLoop = indiceLoop;
	}

}
