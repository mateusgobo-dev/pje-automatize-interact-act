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

@org.jboss.seam.annotations.faces.Validator(id = "telefoneValidator")
@Name("telefoneValidator")
@BypassInterceptors
public class TelefoneValidator implements Validator {
	//só verifica se o string passado tem 0 ou 9 ou nove dígitos.
	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

		char[] fone = value.toString().toCharArray();
		
		int n = 0;
		
		for( char c : fone ){
			if( Character.isDigit(c) ) ++n;
		}
		
		if (n > 0 && (n < 8 || n > 10) ) {
			String msgerro = "\'" + value.toString() + "\'" + " é um número de telefone inválido.";
			
			FacesMessage fm = new FacesMessage(msgerro);
			FacesMessages.instance().add(Severity.ERROR, msgerro);
			
			throw new ValidatorException(fm);
		}

	}

}