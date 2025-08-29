package br.jus.pje.nucleo.util;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Classe responsável pelo armazenamento de informações gerais sobre a
 * requisição atual. Os objetos são armazenados em thread.
 * 
 * @author Adriano Pamplona
 */
public final class PJEHolder {
	private static ThreadLocal<Boolean> webhookAction = new ThreadLocal<>();

	/**
	 * Construtor.
	 */
	private PJEHolder() {
		// Construtor
	}

	/**
	 * @return True se a requisição atual foi feita via WebhookWrapperService rest.
	 */
	public static boolean isWebhookAction() {
		return ObjectUtils.firstNonNull(webhookAction.get(), Boolean.FALSE);
	}

	/**
	 * @param isWebhookAction Atribui true ou false.
	 */
	public static void setWebhookAction(Boolean isWebhookAction) {
		PJEHolder.webhookAction.set(isWebhookAction);
	}
	
	/**
	 * Limpa a variável webhookAction.
	 */
	public static void removeWebhookAction() {
		PJEHolder.webhookAction.remove();
	}
}
