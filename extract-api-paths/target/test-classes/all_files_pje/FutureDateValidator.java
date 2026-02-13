package br.com.infox.validator;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@org.jboss.seam.annotations.faces.Validator(id = "futureDateValidator")
@Name("futureDateValidator")
@BypassInterceptors
public class FutureDateValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

		GregorianCalendar date = new GregorianCalendar();
		date.setTime((Date) value);
		date.set(GregorianCalendar.MINUTE, 00);
		date.set(GregorianCalendar.MILLISECOND, 00);
		date.set(GregorianCalendar.SECOND, 00);
		date.set(GregorianCalendar.HOUR_OF_DAY, 00);

		GregorianCalendar now = new GregorianCalendar();
		now.setTime(new Date());
		now.set(GregorianCalendar.MINUTE, 00);
		now.set(GregorianCalendar.MILLISECOND, 00);
		now.set(GregorianCalendar.SECOND, 00);
		now.set(GregorianCalendar.HOUR_OF_DAY, 00);
		if (date != null && date.compareTo(now) < 0 ) {
			throw new ValidatorException(new FacesMessage("A data deve ser maior ou igual que a atual."));
		}

	}

}
