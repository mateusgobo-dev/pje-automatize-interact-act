package br.jus.cnj.pje.entidades.listeners;

import javax.persistence.PostPersist;

import org.jboss.seam.core.Events;

import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;

/**
 * Observador de evento de persistência inicial da entidade {@link ProcessoTrfConexao}, com o objetivo
 * de viabilizar a verificação de prevenção em fluxo.
 *  
 * @author Antonio Augusto Silva Martins
 *
 */
public class ProcessoTrfConexaoListener {

	@PostPersist
	public void onPostPersist(ProcessoTrfConexao processoTrfConexao) {
		Events.instance().raiseAsynchronousEvent(Eventos.CONEXAO_PROCESSUAL_CRIADA, processoTrfConexao);
	}
	
}
