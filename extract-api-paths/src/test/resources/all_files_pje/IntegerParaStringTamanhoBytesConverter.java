/**
 * IntegerParaStringTamanhoBytesConverter.java.
 *
 * Data de criação: 13/06/2014
 */
package br.com.infox.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe responsável em formatar um tamanho de quantidade de bytes para uma
 * string com aparência de leitura humana.
 * 
 * @author Adriano Pamplona
 * 
 */
@org.jboss.seam.annotations.faces.Converter
@Name("integerParaStringTamanhoBytesConverter")
@BypassInterceptors
public class IntegerParaStringTamanhoBytesConverter implements Converter {

	/**
	 * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent, java.lang.String)
	 */
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		Integer resultado = null;

		if (StringUtils.isNotBlank(value)) {
			resultado = new Integer(StringUtil.removeNaoNumericos(value));
		}
		return resultado;
	}

	/**
	 * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent, java.lang.Object)
	 */
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		String resultado = "0 B";

		if (value != null && value instanceof Number) {
			long bytes = ((Number) value).longValue();
			resultado = StringUtil.tamanhoBytes(bytes, false);
		}
		return resultado;
	};
}
