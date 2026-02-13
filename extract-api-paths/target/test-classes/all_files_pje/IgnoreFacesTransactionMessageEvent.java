package br.jus.cnj.pje.interceptor;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.core.Events;
import org.jboss.seam.transaction.FacesTransactionEvents;

/**
 * Responsável por desabilitar do framework seam a mensagem de falha na transação.
 * <br><br>
 * Ao executar o evento de ignorar a mensagem de falha este componente coloca na
 * fila para disparar um evento de habilitar a mensagem após a transação ter
 * sido completada, independente de ter falhado ou não.
 * 
 * @see FacesTransactionEvents
 */
@Name("ignoreSeamTransactionMessage")
public class IgnoreFacesTransactionMessageEvent {

	private static final String ENABLE_MESSAGE = "ignoreSeamTransactionMessage_enable";

	public static final String IGNORE_MESSAGE = "ignoreSeamTransactionMessage_ignore";

	/**
	 * Desabilita a mensagem de falha da transação de ser adicionada no faces messages.
	 */
	@Observer(IGNORE_MESSAGE)
	public void disableMessage() {
		getFacesTransactionEvents().setTransactionFailedMessageEnabled(false);
		Events.instance().raiseTransactionCompletionEvent(ENABLE_MESSAGE);
	}

	/**
	 * Habilita a mensagem de falha da transação de ser adicionada no faces messages.
	 */
	@Observer(ENABLE_MESSAGE)
	public void rollbackDisableMessage() {
		getFacesTransactionEvents().setTransactionFailedMessageEnabled(true);
	}

	/**
	 * Obtém o {@link FacesTransactionEvents} que adiciona a mensagem de falha na transação.
	 * 
	 * @return {@link FacesTransactionEvents} do framework seam.
	 */
	private FacesTransactionEvents getFacesTransactionEvents() {
		return (FacesTransactionEvents) Component.getInstance(FacesTransactionEvents.class);
	}
}
