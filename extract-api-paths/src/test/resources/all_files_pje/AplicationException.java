package br.com.itx.exception;

import org.jboss.seam.annotations.ApplicationException;
import org.jboss.seam.annotations.exception.Redirect;
import org.jboss.seam.faces.FacesMessages;

@Redirect(viewId = "/error.seam")
@ApplicationException(rollback = true, end = true)
public class AplicationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AplicationException() {
		super();
	}

	public AplicationException(String cause) {
		super(cause);
	}

	public AplicationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public AplicationException(Throwable cause) {
		super(cause);
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