package br.com.infox.cliente.exception;

public class ConexaoReceitaFederalException extends Exception {

	private static final long serialVersionUID = 1L;

	public ConexaoReceitaFederalException(Exception e) {
		super(e.getMessage());
	}

}
