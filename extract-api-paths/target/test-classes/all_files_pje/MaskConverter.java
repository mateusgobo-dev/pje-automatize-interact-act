package br.com.infox.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Strings;

/**
 * Conversor destinado a retirar os caracteres inseridos quando da utilização da tag mask.
 */
@org.jboss.seam.annotations.faces.Converter
@Name("maskConverter")
@BypassInterceptors
public class MaskConverter implements Converter {
	
	private static final String PATTERN = "(?:[^a-z0-9 ])";

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return Strings.nullIfEmpty(value.replaceAll(PATTERN, StringUtils.EMPTY).trim());
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return value == null ? null : value.toString();
	}

}
