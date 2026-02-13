package br.jus.csjt.pje.commons.exception;

import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Classe responsavel para lancar uma ecessao de negocio.
 * 
 * Lanca a ecessao e mostra na tela a mensagem passando no construtor uma key do
 * arquivo de mensagens padrao.
 * 
 * @author Rafael Carvalho
 * @categoryPJE-JT
 * @since versão 1.2.0
 */
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private LogProvider log = Logging.getLogProvider(BusinessException.class);

	public BusinessException(String messageKey, Object... params) {
		FacesMessages.instance().clearGlobalMessages();
		FacesMessages.instance().addFromResourceBundle(Severity.ERROR, messageKey, params);
		log.error(getCause());
	}

	public BusinessException(Severity severity, String messageKey, Object... params) {
		FacesMessages.instance().clearGlobalMessages();
		FacesMessages.instance().addFromResourceBundle(severity, messageKey, params);
		log.error(getCause());
	}

}