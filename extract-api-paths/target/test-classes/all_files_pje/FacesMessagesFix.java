package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.util.Strings;

@Scope(ScopeType.CONVERSATION)
@Name(StatusMessages.COMPONENT_NAME)
@Install(precedence = FRAMEWORK, classDependencies = "javax.faces.context.FacesContext")
@BypassInterceptors
public class FacesMessagesFix extends FacesMessages {

	private static final long serialVersionUID = 1L;

	@Override
	public void beforeRenderResponse() {
		for (StatusMessage statusMessage : getMessages()) {

			// Corrigindo bug que ocorre quando o seam tenta adicionar uma
			// mensagem nula ao FacesContext
			FacesMessage facesMessage = toFacesMessage(statusMessage);

			if (facesMessage != null)
				FacesContext.getCurrentInstance().addMessage(null, facesMessage);

		}
		for (Map.Entry<String, List<StatusMessage>> entry : getKeyedMessages().entrySet()) {
			for (StatusMessage statusMessage : entry.getValue()) {
				String clientId = getClientId(entry.getKey());
				FacesContext.getCurrentInstance().addMessage(clientId, toFacesMessage(statusMessage));
			}
		}
		clear();
	}
	
	/**
	 * @return Lista de StatusMessage
	 * @see FacesMessages#getMessages()
	 */
	public List<StatusMessage> getCurrentStatusMessages() {
		return super.getMessages();
	}
	
	/**
	 * @return True se existir mensagem do tipo 'Error'.
	 */
	public Boolean isExistsErrorMessage() {
		Boolean resultado = Boolean.FALSE;
		
		List<StatusMessage> mensagens = getCurrentStatusMessages();
		for (int index = 0; index < mensagens.size() && !resultado; index++) {
			StatusMessage mensagem = mensagens.get(index);
			resultado = (mensagem.getSeverity() == Severity.ERROR);
		}
		return resultado;
	}
	
	/**
	 * @return Primeira mensagem do tipo 'Error'.
	 */
	public StatusMessage getErrorMessage() {
		StatusMessage resultado = null;
		
		List<StatusMessage> mensagens = getCurrentStatusMessages();
		for (int index = 0; index < mensagens.size() && resultado == null; index++) {
			StatusMessage mensagem = mensagens.get(index);
			if (mensagem.getSeverity() == Severity.ERROR) {
				resultado = mensagem;
			}
		}
		return resultado;
	}

	/**
	 * Convert a StatusMessage to a FacesMessage
	 */
	private static FacesMessage toFacesMessage(StatusMessage statusMessage) {
		if (!Strings.isEmpty(statusMessage.getSummary())) {
			return new FacesMessage(toSeverity(statusMessage.getSeverity()), statusMessage.getSummary(),
					statusMessage.getDetail());
		} else {
			return null;
		}
	}

	/**
	 * Convert a StatusMessage.Severity to a FacesMessage.Severity
	 */
	private static javax.faces.application.FacesMessage.Severity toSeverity(
			org.jboss.seam.international.StatusMessage.Severity severity) {
		switch (severity) {
		case ERROR:
			return FacesMessage.SEVERITY_ERROR;
		case FATAL:
			return FacesMessage.SEVERITY_FATAL;
		case INFO:
			return FacesMessage.SEVERITY_INFO;
		case WARN:
			return FacesMessage.SEVERITY_WARN;
		default:
			return null;
		}
	}

	/**
	 * Calculate the JSF client ID from the provided widget ID
	 */
	private String getClientId(String id) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return getClientId(facesContext.getViewRoot(), id, facesContext);
	}

	@SuppressWarnings("unchecked")
	private static String getClientId(UIComponent component, String id, FacesContext facesContext) {
		String componentId = component.getId();
		if (componentId != null && componentId.equals(id)) {
			return component.getClientId(facesContext);
		} else {
			Iterator iter = component.getFacetsAndChildren();
			while (iter.hasNext()) {
				UIComponent child = (UIComponent) iter.next();
				String clientId = getClientId(child, id, facesContext);
				if (clientId != null)
					return clientId;
			}
			return null;
		}
	}

}
