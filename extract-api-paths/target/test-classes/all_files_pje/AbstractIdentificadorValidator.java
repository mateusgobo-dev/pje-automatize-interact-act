package br.com.infox.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

public abstract class AbstractIdentificadorValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		String identificador = value.toString();
		
		if (ehInvalido(identificador)) {
			String msgerro = "\'" + value.toString() + "\'" + " é um identificador de "+tipoIdentificador()+ " inválido.";
			
			FacesMessage fm = new FacesMessage(msgerro);
			FacesMessages.instance().add(Severity.ERROR, msgerro);
			
			throw new ValidatorException(fm);
		}

	}

	/**
	 * retorna tipo do identificador: Papel ou Funcionalidade (Recurso) 
	 * 
	 * 
	 * @return
	 */
	public abstract String tipoIdentificador();

	/**
	 * condicional para validação de acordo com o identificador: Papel ou Funcionalidade (Recurso) 
	 * 
	 * @param identificador
	 * @return
	 */
	public abstract boolean ehInvalido(String identificador);

}
