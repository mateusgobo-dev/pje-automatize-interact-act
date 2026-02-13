package br.com.infox.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

@org.jboss.seam.annotations.faces.Validator(id = "telefoneDDDValidator")
@Name("telefoneDDDValidator")
@BypassInterceptors
public class TelefoneDDDValidator implements Validator {
	//só verifica se o string passado tem 0 ou 8 ou nove dígitos.
	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

		char[] ddd = value.toString().toCharArray();
		
		int n = 0;
		
		for( char c : ddd ){
			if( Character.isDigit(c) ) ++n;
		}
		
		if (n > 0 && n != 2) {
			String msgerro = "\'" + value.toString() + "\'" + " é um número de ddd inválido.";
			
			FacesMessage fm = new FacesMessage(msgerro);
			FacesMessages.instance().add(Severity.ERROR, msgerro);
			
			throw new ValidatorException(fm);
		}

	}

}