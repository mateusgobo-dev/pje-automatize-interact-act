/**
 * DataddMMyyyyHHmmssValidator.java
 * 
 * Data de criação: 12/02/2015
 */
package br.com.infox.validator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@org.jboss.seam.annotations.faces.Validator(id = "dateValidator")
@Name("dateValidator")
@BypassInterceptors
public class DataddMMyyyyHHmmssValidator implements Validator {
	private static final String DEFAULT_PATTERN = "dd/MM/yyyy HH:mm:ss";

	private String pattern;

	@Override
	public void validate(FacesContext context, UIComponent component,
			Object object) throws ValidatorException {
		String value = object.toString();
	
		try {
			DateFormat f = new SimpleDateFormat(getPattern());
			f.setLenient(false);
			f.parse(value);
		} catch (ParseException e) {
			String mensagem = String.format(
					"Data inválida, a data '%s' não está no formato '%s'.",
					value, getPattern());
			throw new ValidatorException(new FacesMessage(mensagem));
		}
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		if (StringUtils.isBlank(pattern)) {
			return DEFAULT_PATTERN;
		}
		return pattern;
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

}