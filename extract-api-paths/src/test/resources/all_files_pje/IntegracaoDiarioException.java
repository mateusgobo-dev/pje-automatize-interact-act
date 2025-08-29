package br.jus.csjt.pje.commons.exception;

/**
 * Esta classe dispara exceções relacionada a comunicação entre o Diário e o PJE.
 * 
 * 
 * @author Estevão Mognatto
 * @since 1.4.3
 * @see
 * @category PJE-JT
 * 
 * */

public class IntegracaoDiarioException extends Exception {

	private static final long serialVersionUID = 1L;

	public IntegracaoDiarioException() {
		super();
	}

	public IntegracaoDiarioException(String message) {
		super(message);
	}

	public IntegracaoDiarioException(String message, Throwable cause) {
		super(message, cause);
	}

	public IntegracaoDiarioException(Throwable cause) {
		super(cause);
	}
}
