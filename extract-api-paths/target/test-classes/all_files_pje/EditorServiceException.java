package br.com.infox.editor.exception;


public class EditorServiceException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public EditorServiceException() {
	}
	
	public EditorServiceException(String cause) {
		super(cause);
	}
	
	public EditorServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
