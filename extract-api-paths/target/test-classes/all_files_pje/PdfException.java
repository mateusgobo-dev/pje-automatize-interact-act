package br.jus.csjt.pje.business.pdf;

public class PdfException extends Exception {

	private static final long serialVersionUID = 1L;

	public PdfException() {
		super();
	}

	public PdfException(String message) {
		super(message);
	}

	public PdfException(Throwable cause) {
		super(cause);
	}

	public PdfException(String message, Throwable cause) {
		super(message, cause);
	}

}
