package br.com.infox.validator;

import java.io.UnsupportedEncodingException;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@org.jboss.seam.annotations.faces.Validator(id = "iso88591Validator")
@Name("iso88591Validator")
@BypassInterceptors
public class Iso88591Validator implements Validator {

	@Override
	public void validate(final FacesContext context, final UIComponent component, final Object value) throws ValidatorException {
		if (value instanceof String) {
			String str = (String) value;
			String iso8859Value = this.normalizarStringIso88591(str);
			if (!iso8859Value.equals(str)) {
				StringBuilder builder = new StringBuilder("O texto digitado contém caracteres que não podem ser utilizados, são eles: ");
				for (char c : str.toCharArray()) {
					if (!this.isIso88591(c)) {
						builder.append(c).append(" ");
					}
				}
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, builder.toString(), null);
				throw new ValidatorException(message);
			}
		}
	}

	private String normalizarStringIso88591(final String str) {
		try {
			return new String(str.getBytes("ISO-8859-1"), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}

	private boolean isIso88591(final char c) {
		return c <= 255;
	}

}
