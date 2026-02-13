package br.com.infox.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@org.jboss.seam.annotations.faces.Validator(id = "maiorQueZeroValidator")
@Name("maiorQueZeroValidator")
@BypassInterceptors
public class MaiorQueZeroValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

		if(value instanceof Integer){
			Integer valor = (Integer) value;
			if (!(valor > 0)) {
				throw new ValidatorException(new FacesMessage("O valor tem de ser maior que zero."));
			}
		}else{
			Double valor = (Double) value;
			if (!(valor > 0)) {
				throw new ValidatorException(new FacesMessage("O valor tem de ser maior que zero."));
			}
		}

	}

}
