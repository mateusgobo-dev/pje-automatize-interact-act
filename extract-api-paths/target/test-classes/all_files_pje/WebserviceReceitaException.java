package br.com.infox.trf.webservice;

public class WebserviceReceitaException extends Exception {

	private static final long serialVersionUID = 1L;

	public WebserviceReceitaException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebserviceReceitaException(String message) {
		super(message);
	}

	public WebserviceReceitaException(Throwable cause) {
		super(cause);
	}

}
