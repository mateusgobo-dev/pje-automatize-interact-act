package br.jus.cnj.pje.intercomunicacao.exception;

public class IntercomunicacaoException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4736024135577623883L;

	public IntercomunicacaoException() {
		super();
	}
	
	public IntercomunicacaoException(Exception e) {
		super(e);
	}
	
	public IntercomunicacaoException(String message) {
		super(message);
	}
	
	public IntercomunicacaoException(String message, Throwable cause) {
		super(message, cause);
	}

}
