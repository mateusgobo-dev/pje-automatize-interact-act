package br.jus.cnj.pje.nucleo;


public class BNMPException extends PJeBusinessException{

	private static final long serialVersionUID = 1L;	


	/**
	 * @param message
	 */
/*	public BNMPException(String message) {
		super(message);
	}*/

	/**
	 * @param cause
	 */
	public BNMPException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public BNMPException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public BNMPException(String codigo, Throwable t, Object... params){
		super(codigo, t, params);
	}

	public BNMPException(String code){
		super(code);
	}	

}
