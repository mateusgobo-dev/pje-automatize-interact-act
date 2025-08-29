package br.com.infox.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.apache.commons.codec.Charsets;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.PdfUtil;


/**
 * Conversor de String para String com encode UTF-8.
 * 
 * @author Adriano Pamplona
 */
@org.jboss.seam.annotations.faces.Converter
@Name("stringToUTF8Converter")
@BypassInterceptors
public class StringToUTF8Converter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
		return value;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
		String resultado = null;
		if (value != null && value instanceof String) {
			String string = value.toString();
			byte[] bytes = string.getBytes();
			resultado = new String(bytes, Charsets.UTF_8);
		}
		resultado = PdfUtil.formatarCSSEditor(resultado);
		return resultado;
	}

}