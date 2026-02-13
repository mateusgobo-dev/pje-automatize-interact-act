package br.jus.cnj.pje.nucleo;

import org.jboss.seam.annotations.ApplicationException;
import org.jboss.seam.annotations.exception.HttpError;
import org.jboss.seam.annotations.exception.Redirect;

@Redirect(viewId = "/403.seam")
@HttpError(errorCode = 403)
@ApplicationException(rollback = true, end = true)
public class AcessoNegadoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AcessoNegadoException() {
		super();
	}

	public AcessoNegadoException(String message) {
		super(message);
	}

	public AcessoNegadoException(String message, Throwable cause) {
		super(message, cause);
	}

	public AcessoNegadoException(Throwable cause) {
		super(cause);
	}	
}
