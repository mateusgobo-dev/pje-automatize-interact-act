package br.com.infox.cliente.home.icrrefactory;

public class IcrValidationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8396501839338145650L;

	private Object[] params;

	public IcrValidationException() {

	}

	public IcrValidationException(String message) {
		super(message);
	}

	public IcrValidationException(Throwable e) {
		super(e);
	}

	public IcrValidationException(String message, Throwable e) {
		super(message, e);
	}

	public IcrValidationException(String message, Object... params) {
		super(message);
		this.params = params;
	}

	public Object[] getParams() {
		return params;
	}

}
