package br.com.infox.editor.exception;

public class AnotacaoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AnotacaoException() {
	}
	
	public AnotacaoException(String cause) {
		super(cause);
	}
	
	public AnotacaoException(String message, Throwable cause) {
		super(message, cause);
	}
}
