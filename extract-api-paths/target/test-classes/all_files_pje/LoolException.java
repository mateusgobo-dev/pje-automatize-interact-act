package br.jus.cnj.pje.editor.lool;

public class LoolException extends Exception {

	private static final long serialVersionUID = 1L;

	public LoolException() {
	}

	public LoolException(String message) {
		super(message);
	}

	public LoolException(Throwable cause) {
		super(cause);
	}

	public LoolException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
