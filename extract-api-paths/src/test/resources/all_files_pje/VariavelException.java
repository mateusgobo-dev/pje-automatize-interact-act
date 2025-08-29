package br.com.infox.editor.exception;

public class VariavelException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public VariavelException() {
	}

	public VariavelException(String message) {
		super(message);
	}

	public VariavelException(Throwable cause) {
		super(cause);
	}

	public VariavelException(String message, Throwable cause) {
		super(message, cause);
	}
}
