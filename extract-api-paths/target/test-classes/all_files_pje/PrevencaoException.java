package br.jus.cnj.pje.nucleo;

public class PrevencaoException extends Exception {

	private static final long serialVersionUID = 1L;

	public PrevencaoException() {
		super();
	}

	public PrevencaoException(String message) {
		super(message);
	}

	public PrevencaoException(String message, Throwable cause) {
		super(message, cause);
	}

	public PrevencaoException(Throwable cause) {
		super(cause);
	}	
}
