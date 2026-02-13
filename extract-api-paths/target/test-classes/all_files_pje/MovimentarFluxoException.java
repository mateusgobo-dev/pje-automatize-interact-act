package br.com.itx.exception;

import org.jboss.seam.annotations.ApplicationException;
import org.jboss.seam.annotations.exception.Redirect;
import org.jboss.seam.faces.FacesMessages;

@Redirect(viewId = "/errorMovimentarFluxo.seam")
@ApplicationException(rollback = true, end = true)
public class MovimentarFluxoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MovimentarFluxoException() {
		super();
	}

	public MovimentarFluxoException(String cause) {
		super(cause);
	}

	public MovimentarFluxoException(String message, Throwable cause) {
		super(message, cause);
	}

	public static String createMessage(String action, String method, String className, String project) {
		FacesMessages.instance().clearGlobalMessages();

		StringBuilder sb = new StringBuilder();

		sb.append("Erro ao ");
		sb.append(action);
		sb.append(".Método: ");
		sb.append(method);
		sb.append(".Classe: ");
		sb.append(className);
		sb.append(".Projeto: ");
		sb.append(project);

		return sb.toString();
	}

}