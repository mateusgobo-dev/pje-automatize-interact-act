package br.com.infox.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Strings;

@org.jboss.seam.annotations.faces.Converter
@Name("htmlStringConverter")
@BypassInterceptors
public class HtmlStringConverter extends StringConverter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
		if (value != null) {
			String valueCopy = removeTags(value);
			if (Strings.isEmpty(valueCopy)) {
				return null;
			}
		}else if(value == null){
			return null;
		}
		return super.getAsObject(context, component, value);
	}

	private String removeTags(String modelo) {
		return modelo.replaceAll("\\<.*?\\>", "").replaceAll("\n", "").replaceAll("\r", "").replaceAll("&nbsp;", "");
	}

}
