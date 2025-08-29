package br.com.infox.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@org.jboss.seam.annotations.faces.Validator(id = "requiredCheckboxValidator")
@Name("requiredCheckboxValidator")
@BypassInterceptors
public class RequiredCheckboxValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		if (value.equals(Boolean.FALSE)) {
			throw new ValidatorException(new FacesMessage("Para concluir o cadastro é necessário dar ciência."));
		}
	}

}
