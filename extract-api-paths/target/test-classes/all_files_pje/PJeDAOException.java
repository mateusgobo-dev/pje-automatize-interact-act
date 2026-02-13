package br.jus.cnj.pje.nucleo;

/**
 * Exceção anotada como ApplicationException para forçar rollback em qualquer erro na camada de acesso a dados
 * 
 * @author Rodrigo Alves Reis / CNJ
 * @author Daniel Castro Machado Miranda / CNJ
 * 
 */
public class PJeDAOException extends PJeRuntimeException{

	private static final long serialVersionUID = -7914602564250750839L;

	public PJeDAOException(Throwable e){
		super(e);
	}

	public PJeDAOException(String code, Throwable t, Object... params){
		super(code, t, params);
	}

	public PJeDAOException(String code){
		super(code);
	}

}
