package br.com.infox.exceptions;

public class SendMailException extends Exception{

	private static final long serialVersionUID = 1L;

	public SendMailException(Exception e) {
		super(e);
	}

}
