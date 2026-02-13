package br.com.infox.validator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
/**
 * 
 * @author Carlos Lisboa.
 * Data: 13-02-2014
 */
@org.jboss.seam.annotations.faces.Converter
@Name("dateToStringConverter")
@Install(precedence = Install.FRAMEWORK)
@BypassInterceptors
public class DateToStringConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		// TODO Auto-generated method stub
		return arg2;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		// TODO Auto-generated method stub
		return null;
	}

}
