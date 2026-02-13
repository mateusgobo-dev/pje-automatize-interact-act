package br.com.infox.converter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Strings;

@org.jboss.seam.annotations.faces.Converter
@Name("bigDecimalConverter")
@BypassInterceptors
public class BigDecimalConverter implements Converter {

	private static final NumberFormat formatter = new DecimalFormat("#,##0.00");

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
		if (Strings.isEmpty(value)) {
			return null;
		}
		BigDecimal valor = null;
		try {
			valor = new BigDecimal(value.replace(".", "").replace(",", "."));
		} catch (Exception e) {
			throw new ConverterException(new FacesMessage("Formato inválido: " + value));
		}
		return valor;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
		return value == null ? null : formatter.format(value);
	}

}
