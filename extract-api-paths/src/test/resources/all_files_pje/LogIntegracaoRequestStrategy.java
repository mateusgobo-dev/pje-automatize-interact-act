package br.com.infox.pje.processor.strategy;

import br.jus.pje.nucleo.entidades.LogIntegracao;

/**
 * Interface de persistência do LogIntegracao.
 * 
 * @author Adriano Pamplona
 */
public interface LogIntegracaoRequestStrategy {

	/**
	 * @param log LogIntegracao
	 */
	public <T> T execute(LogIntegracao log);
}
